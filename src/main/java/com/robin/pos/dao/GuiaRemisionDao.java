package com.robin.pos.dao;

import com.robin.pos.model.GuiaRemision;
import com.robin.pos.model.GuiaRemisionDetalle;
import com.robin.pos.model.GuiaResumen;
import com.robin.pos.model.ItemCatalogo;
import com.robin.pos.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO de la Guía de Remisión Electrónica Remitente (GRE 2022).
 *
 * Lectura del origen : FACTU.ARPFFE (cabecera guía interna) + FACTU.ARPFFL (detalle)
 * Escritura          : FACTU.ARGUIA (cabecera GRE) + FACTU.ARGUIL (detalle GRE)
 * Correlativos       : FACTU.GRE_SERIE_CTRL (bloqueo SELECT ... FOR UPDATE)
 * Lista              : FACTU.V_GUIA_PENDIENTE
 *
 * @author Robin POS
 */
public class GuiaRemisionDao {

    private static final Logger LOGGER = Logger.getLogger(GuiaRemisionDao.class.getName());
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    // =====================================================================
    // 1. ORIGEN: ARPFFE / ARPFFL
    // =====================================================================

    /**
     * Busca las guías internas (ARPFFE) asociadas a una factura o boleta.
     * Acepta el número como está en BD (F0010000066) o con guion (F001-66, F001-00000066).
     */
    public List<GuiaRemision> buscarDocumentoOrigen(String noCia, String numeroDocumento) throws SQLException {
        String[] candidatos = candidatosNoFactu(numeroDocumento);
        String sql = """
            SELECT E.NO_CIA, E.BODEGA, E.NO_GUIA, E.FECHA, E.NO_CLIENTE, E.NOMBRE,
                   E.TIPO_DOC, E.NO_FACTU, E.ESTADO, E.OBSERVACIONES, E.FECHA_INICIO,
                   E.PUNTO_PARTIDA, E.PUNTO_LLEGADA, E.LLEGADA_DISTRITO,
                   E.LLEGADA_PROVINCIA, E.LLEGADA_DEPARTAMENTO,
                   E.RAZON_SOCIAL_DESTINATARIO, E.RUC_DESTINATARIO,
                   E.TIPO_DOCUMENTO, E.NUMERO_DOCUMENTO,
                   E.MARCA, E.PLACA, E.CERTIFICADO_INSCRIPCION, E.BREVETE,
                   E.RUC_TRANSP, E.RAZON_SOCIAL, E.CHOFER, E.MOTIVO_TRASLADO
              FROM FACTU.ARPFFE E
             WHERE E.NO_CIA = ?
               AND E.NO_FACTU IN (?, ?)
             ORDER BY E.FECHA DESC, E.NO_GUIA DESC
            """;

        List<GuiaRemision> lista = new ArrayList<>();
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, noCia);
            ps.setString(2, candidatos[0]);
            ps.setString(3, candidatos[1]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearOrigen(rs));
                }
            }
        }
        return lista;
    }

    private GuiaRemision mapearOrigen(ResultSet rs) throws SQLException {
        GuiaRemision g = new GuiaRemision();
        g.setNoCia(rs.getString("NO_CIA"));
        g.setBodega(rs.getString("BODEGA"));
        g.setNoGuia(rs.getString("NO_GUIA"));
        g.setNoCliente(rs.getString("NO_CLIENTE"));
        g.setObservaciones(rs.getString("OBSERVACIONES"));
        g.setFecInicioTraslado(toLocalDate(rs.getDate("FECHA_INICIO")));
        if (g.getFecInicioTraslado() == null) {
            g.setFecInicioTraslado(toLocalDate(rs.getDate("FECHA")));
        }

        // Destinatario (se completa luego con el maestro de clientes)
        String nroDoc = primeroNoVacio(rs.getString("RUC_DESTINATARIO"), rs.getString("NO_CLIENTE"));
        g.setNroDocDestin(nroDoc);
        g.setRazonSocDestin(primeroNoVacio(rs.getString("RAZON_SOCIAL_DESTINATARIO"), rs.getString("NOMBRE")));
        g.setTipoDocDestin(tipoDocSunatPorNumero(nroDoc));

        // Puntos de partida y llegada
        g.setPartDirec(rs.getString("PUNTO_PARTIDA"));
        g.setLlegdDireccion(rs.getString("PUNTO_LLEGADA"));
        g.setLlegdDist(rs.getString("LLEGADA_DISTRITO"));
        g.setLlegdProv(rs.getString("LLEGADA_PROVINCIA"));
        g.setLlegdDepar(rs.getString("LLEGADA_DEPARTAMENTO"));

        // Transporte
        g.setMarcaVehiculo(rs.getString("MARCA"));
        g.setPlacaVehiculo(limpiarPlaca(rs.getString("PLACA")));
        g.setCertInscripcion(rs.getString("CERTIFICADO_INSCRIPCION"));
        g.setBrevete(rs.getString("BREVETE"));
        g.setRucTransportista(rs.getString("RUC_TRANSP"));
        g.setRazonSocTransp(rs.getString("RAZON_SOCIAL"));
        g.setNombreConduc(rs.getString("CHOFER"));
        if (g.getRucTransportista() != null && !g.getRucTransportista().isBlank()) {
            g.setModalidadTraslado("01");
        }

        // Motivo (en ARPFFE puede venir como '1' o '01')
        String motivo = rs.getString("MOTIVO_TRASLADO");
        if (motivo != null && !motivo.isBlank()) {
            motivo = motivo.trim();
            g.setMotivoTraslado(motivo.length() == 1 ? "0" + motivo : motivo);
        } else {
            g.setMotivoTraslado("01");
        }

        // Documento relacionado: la factura / boleta
        String noFactu = rs.getString("NO_FACTU");
        g.setNoFactuRef(noFactu);
        if (noFactu != null && noFactu.length() > 4) {
            g.setSerieDocRef(noFactu.substring(0, 4));
            g.setCorrDocRef(sinCerosIzquierda(noFactu.substring(4)));
            g.setTipoDocRef(tipoDocRefSunat(rs.getString("TIPO_DOC"), noFactu));
        }
        g.setDescEstado(rs.getString("ESTADO"));
        return g;
    }

    /**
     * Detalle de la guía interna (ARPFFL).
     * NOTA: en ARPFFL la DESCRIPCION suele venir vacía. Si tiene el maestro de
     * artículos (p.ej. INVE.ARINDA), una este SQL con él para traer la descripción
     * y la unidad de medida SUNAT. Mientras tanto se muestra el código y el usuario
     * puede editar la descripción en la tabla antes de registrar.
     */
    public List<GuiaRemisionDetalle> listarDetalleOrigen(String noCia, String bodega, String noGuia) throws SQLException {
        String sql = """
            SELECT L.NO_LINEA, L.NO_ARTI, L.DESCRIPCION, L.CANTIDAD, L.NO_ENTRADA
              FROM FACTU.ARPFFL L
             WHERE L.NO_CIA = ?
               AND L.BODEGA = ?
               AND L.NO_GUIA = ?
             ORDER BY L.NO_LINEA, L.NO_ARTI
            """;
        List<GuiaRemisionDetalle> lineas = new ArrayList<>();
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, noCia);
            ps.setString(2, bodega);
            ps.setString(3, noGuia);
            try (ResultSet rs = ps.executeQuery()) {
                int item = 1;
                while (rs.next()) {
                    String codigo = rs.getString("NO_ARTI");
                    String desc = rs.getString("DESCRIPCION");
                    if (desc == null || desc.isBlank()) {
                        desc = "ARTICULO " + codigo;
                    }
                    BigDecimal cant = rs.getBigDecimal("CANTIDAD");
                    GuiaRemisionDetalle d = new GuiaRemisionDetalle(item++, codigo, desc.trim(),
                            cant != null ? cant : BigDecimal.ONE, "NIU");
                    lineas.add(d);
                }
            }
        }
        return lineas;
    }

    /**
     * Devuelve el número de GRE (T001-00000012) si la guía interna ya tiene una
     * guía electrónica vigente, o null si no tiene.
     */
    public String buscarGreExistente(String noCia, String bodega, String noGuia) throws SQLException {
        String sql = """
            SELECT SERIE_ELECT, CORR_ELECT
              FROM FACTU.ARGUIA
             WHERE NO_CIA = ? AND BODEGA = ? AND NO_GUIA = ?
               AND STS_SUNAT NOT IN ('X', 'N')
               AND ROWNUM = 1
            """;
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, noCia);
            ps.setString(2, bodega);
            ps.setString(3, noGuia);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1) + "-" + String.format("%08d", Long.parseLong(rs.getString(2)));
                }
            }
        }
        return null;
    }

    // =====================================================================
    // 2. CATÁLOGOS Y SERIES
    // =====================================================================

    public List<String> listarSeriesActivas(String noCia) {
        List<String> series = new ArrayList<>();
        String sql = "SELECT SERIE FROM FACTU.GRE_SERIE_CTRL WHERE NO_CIA = ? AND ACTIVO = 'S' ORDER BY SERIE";
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, noCia);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    series.add(rs.getString(1));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar series GRE", e);
        }
        return series;
    }

    /** Correlativo que se asignaría (solo referencial: el definitivo se toma al grabar). */
    public long siguienteCorrelativo(String noCia, String serie) {
        String sql = "SELECT ULTIMO_CORR + 1 FROM FACTU.GRE_SERIE_CTRL WHERE NO_CIA = ? AND SERIE = ?";
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            ps.setString(1, noCia);
            ps.setString(2, serie);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al consultar correlativo", e);
        }
        return 0;
    }

    public List<ItemCatalogo> listarMotivosTraslado() {
        List<ItemCatalogo> lista = listarCatalogo("SELECT CODIGO, DESCRIPCION FROM FACTU.GRE_MOTIVO_TRASLADO ORDER BY CODIGO");
        if (lista.isEmpty()) {
            lista.add(new ItemCatalogo("01", "Venta"));
            lista.add(new ItemCatalogo("02", "Compra"));
            lista.add(new ItemCatalogo("03", "Venta con entrega a terceros"));
            lista.add(new ItemCatalogo("04", "Traslado entre establecimientos de la misma empresa"));
            lista.add(new ItemCatalogo("05", "Consignación"));
            lista.add(new ItemCatalogo("06", "Devolución"));
            lista.add(new ItemCatalogo("07", "Recojo de bienes transformados"));
            lista.add(new ItemCatalogo("08", "Importación"));
            lista.add(new ItemCatalogo("09", "Exportación"));
            lista.add(new ItemCatalogo("13", "Otros"));
            lista.add(new ItemCatalogo("14", "Venta sujeta a confirmación del comprador"));
            lista.add(new ItemCatalogo("17", "Traslado de bienes para transformación"));
            lista.add(new ItemCatalogo("18", "Traslado emisor itinerante CP"));
        }
        return lista;
    }

    public List<ItemCatalogo> listarDocumentosRelacionados() {
        List<ItemCatalogo> lista = listarCatalogo("SELECT CODIGO, DESCRIPCION FROM FACTU.GRE_DOC_RELACIONADO ORDER BY CODIGO");
        if (lista.isEmpty()) {
            lista.add(new ItemCatalogo("01", "Factura"));
            lista.add(new ItemCatalogo("03", "Boleta de Venta"));
            lista.add(new ItemCatalogo("09", "Guía de Remisión Remitente"));
        }
        return lista;
    }

    private List<ItemCatalogo> listarCatalogo(String sql) {
        List<ItemCatalogo> lista = new ArrayList<>();
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ItemCatalogo(rs.getString(1), rs.getString(2)));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer el catálogo: " + sql, e);
        }
        return lista;
    }

    // =====================================================================
    // 3. REGISTRO (transacción: correlativo + cabecera + detalle)
    // =====================================================================

    /**
     * Registra la GRE en ARGUIA/ARGUIL en una sola transacción.
     * El correlativo se toma de GRE_SERIE_CTRL con bloqueo FOR UPDATE, de modo que
     * dos cajas grabando al mismo tiempo nunca obtienen el mismo número.
     *
     * @return la misma guía con ID_GUIA, CORR_ELECT y HORA_EMISION asignados.
     */
    public GuiaRemision registrar(GuiaRemision g) throws SQLException {
        Connection cx = null;
        try {
            cx = ConexionBD.obtenerConexion();
            cx.setAutoCommit(false);

            // 3.1 Correlativo
            long corr;
            try (PreparedStatement ps = cx.prepareStatement(
                    "SELECT ULTIMO_CORR FROM FACTU.GRE_SERIE_CTRL WHERE NO_CIA = ? AND SERIE = ? AND ACTIVO = 'S' FOR UPDATE")) {
                ps.setString(1, g.getNoCia());
                ps.setString(2, g.getSerieElect());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("La serie " + g.getSerieElect()
                                + " no está registrada o está inactiva en FACTU.GRE_SERIE_CTRL.");
                    }
                    corr = rs.getLong(1) + 1;
                }
            }
            if (corr > 99_999_999L) {
                throw new SQLException("La serie " + g.getSerieElect() + " llegó al correlativo máximo (99999999).");
            }
            try (PreparedStatement ps = cx.prepareStatement(
                    "UPDATE FACTU.GRE_SERIE_CTRL SET ULTIMO_CORR = ? WHERE NO_CIA = ? AND SERIE = ?")) {
                ps.setLong(1, corr);
                ps.setString(2, g.getNoCia());
                ps.setString(3, g.getSerieElect());
                ps.executeUpdate();
            }

            // 3.2 ID de la guía
            long idGuia;
            try (PreparedStatement ps = cx.prepareStatement("SELECT FACTU.SEQ_ARGUIA.NEXTVAL FROM DUAL");
                 ResultSet rs = ps.executeQuery()) {
                rs.next();
                idGuia = rs.getLong(1);
            }

            g.setIdGuia(idGuia);
            g.setCorrElect(String.valueOf(corr));
            g.setHoraEmision(LocalTime.now().format(HORA));

            // 3.3 Cabecera
            insertarCabecera(cx, g);

            // 3.4 Detalle
            insertarDetalle(cx, g);

            cx.commit();
            LOGGER.info("GRE registrada: " + g.getNumeroCompleto() + " (ID " + idGuia + ")");
            return g;
        } catch (SQLException e) {
            ConexionBD.rollback(cx);
            g.setIdGuia(null);
            g.setCorrElect(null);
            LOGGER.log(Level.SEVERE, "Error al registrar la guía de remisión", e);
            throw e;
        } finally {
            if (cx != null) {
                try {
                    cx.setAutoCommit(true);
                } catch (SQLException ignored) {
                    // la conexión se cierra a continuación
                }
            }
            ConexionBD.cerrar(cx);
        }
    }

    private void insertarCabecera(Connection cx, GuiaRemision g) throws SQLException {
        String sql = """
            INSERT INTO FACTU.ARGUIA (
                ID_GUIA, NO_CIA, BODEGA, NO_GUIA,
                SERIE_ELECT, CORR_ELECT, ID_EXTERNO, FEC_EMISION, HORA_EMISION, TIPO_GUIA, OBSERVACIONES,
                RUC_REMIT, TIPO_DOC_REMIT, RAZON_SOC_REMIT,
                NRO_DOC_DESTIN, TIPO_DOC_DESTIN, RAZON_SOC_DESTIN,
                NRO_DOC_COMPR, TIPO_DOC_COMPR, RAZON_SOC_COMPR,
                MOTIVO_TRASLADO, DESC_MOTIVO, IND_TRANSBORDO, IND_VEHICULO_M1L,
                PESO_BRUTO_TOTAL, UND_PESO, NRO_BULTOS_PALLETS, MODALIDAD_TRASLADO, FEC_INICIO_TRASLADO,
                RUC_TRANSPORTISTA, TIPO_DOC_TRANSP, RAZON_SOC_TRANSP, MTC_TRANSPORTISTA,
                PLACA_VEHICULO, MARCA_VEHICULO, CERT_INSCRIPCION,
                NRO_DOC_CONDUC, TIPO_DOC_CONDUC, NOMBRE_CONDUC, APELLIDO_CONDUC, BREVETE,
                LLEGD_UBIGEO, LLEGD_DIRECCION, LLEGD_COD_ESTAB, LLEGD_DIST, LLEGD_PROV, LLEGD_DEPAR,
                PART_UBIGEO, PART_DIREC, PART_COD_ESTAB,
                TIPO_DOC_REF, SERIE_DOC_REF, CORR_DOC_REF, NO_FACTU_REF, RUC_EMISOR_DOC_REF,
                STS_SUNAT, CDR_STS, INTENTOS_ENVIO, INTENTOS_CONSULTA,
                FEC_CREA, USU_CREA, USUARIO
            ) VALUES (
                ?, ?, ?, ?,
                ?, ?, ?, ?, ?, '09', ?,
                ?, '6', ?,
                ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, '6', ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?, ?,
                ?, ?, ?, ?, ?, ?,
                ?, ?, ?,
                ?, ?, ?, ?, ?,
                'P', 'D', 0, 0,
                SYSDATE, ?, ?
            )
            """;
        boolean publico = "01".equals(g.getModalidadTraslado());
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            int i = 1;
            ps.setLong(i++, g.getIdGuia());
            ps.setString(i++, g.getNoCia());
            ps.setString(i++, g.getBodega());
            ps.setString(i++, g.getNoGuia());

            ps.setString(i++, g.getSerieElect());
            ps.setString(i++, g.getCorrElect());
            ps.setString(i++, g.getRucRemit() + "-09-" + g.getSerieElect() + "-" + g.getCorrElect());
            ps.setDate(i++, Date.valueOf(g.getFecEmision()));
            ps.setString(i++, g.getHoraEmision());
            setStr(ps, i++, g.getObservaciones(), 500);

            ps.setString(i++, g.getRucRemit());
            setStr(ps, i++, g.getRazonSocRemit(), 150);

            ps.setString(i++, g.getNroDocDestin());
            ps.setString(i++, g.getTipoDocDestin());
            setStr(ps, i++, g.getRazonSocDestin(), 150);

            setStr(ps, i++, g.getNroDocCompr(), 15);
            setStr(ps, i++, g.getTipoDocCompr(), 2);
            setStr(ps, i++, g.getRazonSocCompr(), 150);

            ps.setString(i++, g.getMotivoTraslado());
            setStr(ps, i++, g.getDescMotivo(), 100);
            ps.setString(i++, nvl(g.getIndTransbordo(), "N"));
            ps.setString(i++, nvl(g.getIndVehiculoM1L(), "N"));

            ps.setBigDecimal(i++, g.getPesoBrutoTotal());
            ps.setString(i++, nvl(g.getUndPeso(), "KGM"));
            if (g.getNroBultos() != null) {
                ps.setInt(i++, g.getNroBultos());
            } else {
                ps.setNull(i++, Types.NUMERIC);
            }
            ps.setString(i++, g.getModalidadTraslado());
            ps.setDate(i++, Date.valueOf(g.getFecInicioTraslado()));

            // Transportista: solo en transporte público
            setStr(ps, i++, publico ? g.getRucTransportista() : null, 11);
            setStr(ps, i++, publico ? g.getRazonSocTransp() : null, 150);
            setStr(ps, i++, publico ? g.getMtcTransportista() : null, 20);

            // Vehículo y conductor: transporte privado
            setStr(ps, i++, publico ? null : limpiarPlaca(g.getPlacaVehiculo()), 15);
            setStr(ps, i++, publico ? null : g.getMarcaVehiculo(), 30);
            setStr(ps, i++, publico ? null : g.getCertInscripcion(), 25);
            setStr(ps, i++, publico ? null : g.getNroDocConduc(), 15);
            setStr(ps, i++, publico ? null : g.getTipoDocConduc(), 2);
            setStr(ps, i++, publico ? null : g.getNombreConduc(), 80);
            setStr(ps, i++, publico ? null : g.getApellidoConduc(), 80);
            setStr(ps, i++, publico ? null : g.getBrevete(), 15);

            ps.setString(i++, g.getLlegdUbigeo());
            setStr(ps, i++, g.getLlegdDireccion(), 150);
            setStr(ps, i++, g.getLlegdCodEstab(), 4);
            setStr(ps, i++, g.getLlegdDist(), 80);
            setStr(ps, i++, g.getLlegdProv(), 80);
            setStr(ps, i++, g.getLlegdDepar(), 80);

            ps.setString(i++, g.getPartUbigeo());
            setStr(ps, i++, g.getPartDirec(), 150);
            setStr(ps, i++, g.getPartCodEstab(), 4);

            setStr(ps, i++, g.getTipoDocRef(), 2);
            setStr(ps, i++, g.getSerieDocRef(), 4);
            setStr(ps, i++, g.getCorrDocRef(), 8);
            setStr(ps, i++, g.getNoFactuRef(), 11);
            setStr(ps, i++, g.getRucEmisorDocRef(), 11);

            setStr(ps, i++, g.getUsuario(), 30);
            setStr(ps, i, g.getUsuario(), 30);
            ps.executeUpdate();
        }
    }

    private void insertarDetalle(Connection cx, GuiaRemision g) throws SQLException {
        String sql = """
            INSERT INTO FACTU.ARGUIL (
                ID_LINEA, ID_GUIA, NO_CIA, ITEM_ORDEN, CANTIDAD, UND_MEDIDA, DESCRIPCION,
                COD_PRODUCTO, COD_ARTI, NO_LOTE, PESO_TOTAL_ITEM, FEC_CREA, USU_CREA
            ) VALUES (
                FACTU.SEQ_ARGUIL.NEXTVAL, ?, ?, ?, ?, ?, ?,
                ?, ?, ?, ?, SYSDATE, ?
            )
            """;
        try (PreparedStatement ps = cx.prepareStatement(sql)) {
            int orden = 1;
            for (GuiaRemisionDetalle d : g.getLineas()) {
                int i = 1;
                ps.setLong(i++, g.getIdGuia());
                ps.setString(i++, g.getNoCia());
                ps.setInt(i++, orden++);
                ps.setBigDecimal(i++, d.getCantidad());
                ps.setString(i++, nvl(d.getUndMedida(), "NIU"));
                setStr(ps, i++, d.getDescripcion(), 500);
                setStr(ps, i++, d.getCodProducto(), 30);
                setStr(ps, i++, d.getCodProducto(), 15);
                setStr(ps, i++, d.getNoLote(), 20);
                ps.setBigDecimal(i++, d.getPesoTotal());
                setStr(ps, i, g.getUsuario(), 30);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // =====================================================================
    // 4. CONSULTAS: lista (V_GUIA_PENDIENTE) y guía completa
    // =====================================================================

    /**
     * Lista las guías desde la vista FACTU.V_GUIA_PENDIENTE, complementada con el
     * destinatario, el documento de referencia y el motivo (FACTU.ARGUIA).
     *
     * @param estado código STS_SUNAT o null para todos
     */
    public List<GuiaResumen> listarGuias(String noCia, LocalDate desde, LocalDate hasta, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT V.ID_GUIA, V.BODEGA, V.NO_GUIA, V.SERIE_ELECT, V.CORR_ELECT, V.FEC_EMISION,
                   V.TIPO_GUIA, V.RUC_REMIT, V.STS_SUNAT, V.CDR_STS, V.INTENTOS_ENVIO,
                   V.FEC_ENVIO, V.TICKET_SUNAT, V.RESUL_SUNAT, V.DESC_ESTADO,
                   A.RAZON_SOC_DESTIN, A.NRO_DOC_DESTIN, A.SERIE_DOC_REF, A.CORR_DOC_REF,
                   A.MOTIVO_TRASLADO, M.DESCRIPCION AS DESC_MOTIVO, A.PESO_BRUTO_TOTAL
              FROM FACTU.V_GUIA_PENDIENTE V
              LEFT JOIN FACTU.ARGUIA A ON A.ID_GUIA = V.ID_GUIA
              LEFT JOIN FACTU.GRE_MOTIVO_TRASLADO M ON M.CODIGO = A.MOTIVO_TRASLADO
             WHERE V.NO_CIA = ?
               AND V.FEC_EMISION >= ?
               AND V.FEC_EMISION < ?
            """);
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND V.STS_SUNAT = ?");
        }
        sql.append(" ORDER BY V.FEC_EMISION DESC, V.SERIE_ELECT, TO_NUMBER(V.CORR_ELECT) DESC");

        List<GuiaResumen> lista = new ArrayList<>();
        try (Connection cx = ConexionBD.obtenerConexion();
             PreparedStatement ps = cx.prepareStatement(sql.toString())) {
            ps.setString(1, noCia);
            ps.setDate(2, Date.valueOf(desde));
            ps.setDate(3, Date.valueOf(hasta.plusDays(1)));
            if (estado != null && !estado.isBlank()) {
                ps.setString(4, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GuiaResumen r = new GuiaResumen();
                    r.setIdGuia(rs.getLong("ID_GUIA"));
                    r.setBodega(rs.getString("BODEGA"));
                    r.setNoGuia(rs.getString("NO_GUIA"));
                    r.setSerieElect(rs.getString("SERIE_ELECT"));
                    r.setCorrElect(rs.getString("CORR_ELECT"));
                    r.setFecEmision(toLocalDate(rs.getDate("FEC_EMISION")));
                    r.setTipoGuia(rs.getString("TIPO_GUIA"));
                    r.setRucRemit(rs.getString("RUC_REMIT"));
                    r.setStsSunat(rs.getString("STS_SUNAT"));
                    r.setCdrSts(rs.getString("CDR_STS"));
                    r.setIntentosEnvio(rs.getInt("INTENTOS_ENVIO"));
                    Timestamp envio = rs.getTimestamp("FEC_ENVIO");
                    r.setFecEnvio(envio != null ? envio.toLocalDateTime() : null);
                    r.setTicketSunat(rs.getString("TICKET_SUNAT"));
                    r.setResulSunat(rs.getString("RESUL_SUNAT"));
                    r.setDescEstado(rs.getString("DESC_ESTADO"));
                    r.setDestinatario(rs.getString("RAZON_SOC_DESTIN"));
                    r.setNroDocDestin(rs.getString("NRO_DOC_DESTIN"));
                    String serieRef = rs.getString("SERIE_DOC_REF");
                    String corrRef = rs.getString("CORR_DOC_REF");
                    r.setDocReferencia(serieRef != null ? serieRef + "-" + nvl(corrRef, "") : "");
                    String motivo = rs.getString("MOTIVO_TRASLADO");
                    String descMotivo = rs.getString("DESC_MOTIVO");
                    r.setMotivo(motivo == null ? "" : descMotivo != null ? descMotivo : motivo);
                    r.setPesoBruto(rs.getBigDecimal("PESO_BRUTO_TOTAL"));
                    lista.add(r);
                }
            }
        }
        return lista;
    }

    /** Obtiene la guía completa (cabecera + detalle) para reimpresión o consulta. */
    public GuiaRemision obtenerPorId(long idGuia) throws SQLException {
        String sqlCab = """
            SELECT A.*, M.DESCRIPCION AS DESC_MOTIVO_CAT
              FROM FACTU.ARGUIA A
              LEFT JOIN FACTU.GRE_MOTIVO_TRASLADO M ON M.CODIGO = A.MOTIVO_TRASLADO
             WHERE A.ID_GUIA = ?
            """;
        String sqlDet = """
            SELECT ITEM_ORDEN, COD_PRODUCTO, DESCRIPCION, CANTIDAD, UND_MEDIDA, PESO_TOTAL_ITEM, NO_LOTE
              FROM FACTU.ARGUIL
             WHERE ID_GUIA = ?
             ORDER BY ITEM_ORDEN
            """;
        try (Connection cx = ConexionBD.obtenerConexion()) {
            GuiaRemision g = null;
            try (PreparedStatement ps = cx.prepareStatement(sqlCab)) {
                ps.setLong(1, idGuia);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        g = mapearGuia(rs);
                    }
                }
            }
            if (g == null) {
                return null;
            }
            try (PreparedStatement ps = cx.prepareStatement(sqlDet)) {
                ps.setLong(1, idGuia);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GuiaRemisionDetalle d = new GuiaRemisionDetalle(
                                rs.getInt("ITEM_ORDEN"), rs.getString("COD_PRODUCTO"),
                                rs.getString("DESCRIPCION"), rs.getBigDecimal("CANTIDAD"),
                                rs.getString("UND_MEDIDA"));
                        d.setPesoTotal(rs.getBigDecimal("PESO_TOTAL_ITEM"));
                        d.setNoLote(rs.getString("NO_LOTE"));
                        g.getLineas().add(d);
                    }
                }
            }
            return g;
        }
    }

    private GuiaRemision mapearGuia(ResultSet rs) throws SQLException {
        GuiaRemision g = new GuiaRemision();
        g.setIdGuia(rs.getLong("ID_GUIA"));
        g.setNoCia(rs.getString("NO_CIA"));
        g.setBodega(rs.getString("BODEGA"));
        g.setNoGuia(rs.getString("NO_GUIA"));
        g.setSerieElect(rs.getString("SERIE_ELECT"));
        g.setCorrElect(rs.getString("CORR_ELECT"));
        g.setFecEmision(toLocalDate(rs.getDate("FEC_EMISION")));
        g.setHoraEmision(rs.getString("HORA_EMISION"));
        g.setObservaciones(rs.getString("OBSERVACIONES"));
        g.setRucRemit(rs.getString("RUC_REMIT"));
        g.setRazonSocRemit(rs.getString("RAZON_SOC_REMIT"));
        g.setTipoDocDestin(rs.getString("TIPO_DOC_DESTIN"));
        g.setNroDocDestin(rs.getString("NRO_DOC_DESTIN"));
        g.setRazonSocDestin(rs.getString("RAZON_SOC_DESTIN"));
        g.setTipoDocCompr(rs.getString("TIPO_DOC_COMPR"));
        g.setNroDocCompr(rs.getString("NRO_DOC_COMPR"));
        g.setRazonSocCompr(rs.getString("RAZON_SOC_COMPR"));
        g.setMotivoTraslado(rs.getString("MOTIVO_TRASLADO"));
        String desc = rs.getString("DESC_MOTIVO");
        g.setDescMotivo(desc != null ? desc : rs.getString("DESC_MOTIVO_CAT"));
        g.setIndTransbordo(rs.getString("IND_TRANSBORDO"));
        g.setIndVehiculoM1L(rs.getString("IND_VEHICULO_M1L"));
        g.setPesoBrutoTotal(rs.getBigDecimal("PESO_BRUTO_TOTAL"));
        g.setUndPeso(rs.getString("UND_PESO"));
        int bultos = rs.getInt("NRO_BULTOS_PALLETS");
        g.setNroBultos(rs.wasNull() ? null : bultos);
        g.setModalidadTraslado(rs.getString("MODALIDAD_TRASLADO"));
        g.setFecInicioTraslado(toLocalDate(rs.getDate("FEC_INICIO_TRASLADO")));
        g.setRucTransportista(rs.getString("RUC_TRANSPORTISTA"));
        g.setRazonSocTransp(rs.getString("RAZON_SOC_TRANSP"));
        g.setMtcTransportista(rs.getString("MTC_TRANSPORTISTA"));
        g.setPlacaVehiculo(rs.getString("PLACA_VEHICULO"));
        g.setMarcaVehiculo(rs.getString("MARCA_VEHICULO"));
        g.setCertInscripcion(rs.getString("CERT_INSCRIPCION"));
        g.setTipoDocConduc(rs.getString("TIPO_DOC_CONDUC"));
        g.setNroDocConduc(rs.getString("NRO_DOC_CONDUC"));
        g.setNombreConduc(rs.getString("NOMBRE_CONDUC"));
        g.setApellidoConduc(rs.getString("APELLIDO_CONDUC"));
        g.setBrevete(rs.getString("BREVETE"));
        g.setLlegdUbigeo(rs.getString("LLEGD_UBIGEO"));
        g.setLlegdDireccion(rs.getString("LLEGD_DIRECCION"));
        g.setLlegdCodEstab(rs.getString("LLEGD_COD_ESTAB"));
        g.setLlegdDist(rs.getString("LLEGD_DIST"));
        g.setLlegdProv(rs.getString("LLEGD_PROV"));
        g.setLlegdDepar(rs.getString("LLEGD_DEPAR"));
        g.setPartUbigeo(rs.getString("PART_UBIGEO"));
        g.setPartDirec(rs.getString("PART_DIREC"));
        g.setPartCodEstab(rs.getString("PART_COD_ESTAB"));
        g.setTipoDocRef(rs.getString("TIPO_DOC_REF"));
        g.setSerieDocRef(rs.getString("SERIE_DOC_REF"));
        g.setCorrDocRef(rs.getString("CORR_DOC_REF"));
        g.setNoFactuRef(rs.getString("NO_FACTU_REF"));
        g.setRucEmisorDocRef(rs.getString("RUC_EMISOR_DOC_REF"));
        g.setStsSunat(rs.getString("STS_SUNAT"));
        g.setDescEstado(descripcionEstado(g.getStsSunat()));
        g.setHashcode(rs.getString("HASHCODE"));
        g.setUsuario(rs.getString("USUARIO"));
        return g;
    }

    // =====================================================================
    // 5. UTILIDADES
    // =====================================================================

    public static String descripcionEstado(String sts) {
        if (sts == null) {
            return "";
        }
        return switch (sts) {
            case "P" -> "PENDIENTE";
            case "E" -> "ENVIADA - Esperando CDR";
            case "A" -> "ACEPTADA";
            case "O" -> "ACEPTADA CON OBSERVACIONES";
            case "R" -> "RECHAZADA";
            case "B" -> "BAJA SOLICITADA";
            case "X" -> "BAJA CONFIRMADA";
            case "N" -> "NO ENVIAR";
            case "G" -> "ERROR INTERNO";
            default -> sts;
        };
    }

    /** Tipo de documento de identidad SUNAT (Cat. 06) a partir de la longitud del número. */
    public static String tipoDocSunatPorNumero(String numero) {
        if (numero == null) {
            return "6";
        }
        String n = numero.trim();
        if (n.matches("\\d{11}")) {
            return "6";   // RUC
        }
        if (n.matches("\\d{8}")) {
            return "1";   // DNI
        }
        return "4";       // Carnet de extranjería
    }

    /** Convierte el tipo de documento del maestro de clientes (RUC/DNI/CE) al Cat. 06. */
    public static String tipoDocSunat(String tipoDocumentoCliente, String numero) {
        if (tipoDocumentoCliente == null) {
            return tipoDocSunatPorNumero(numero);
        }
        return switch (tipoDocumentoCliente.trim().toUpperCase()) {
            case "RUC", "6" -> "6";
            case "DNI", "1" -> "1";
            case "CE", "4" -> "4";
            case "PAS", "PASAPORTE", "7" -> "7";
            default -> tipoDocSunatPorNumero(numero);
        };
    }

    private static String tipoDocRefSunat(String tipoDoc, String noFactu) {
        if (tipoDoc != null) {
            switch (tipoDoc.trim().toUpperCase()) {
                case "FA", "F", "01":
                    return "01";
                case "BO", "B", "03":
                    return "03";
                default:
                    break;
            }
        }
        if (noFactu != null && !noFactu.isEmpty()) {
            char c = Character.toUpperCase(noFactu.charAt(0));
            if (c == 'B') {
                return "03";
            }
        }
        return "01";
    }

    /** Devuelve [numero tal como se escribió, numero normalizado SERIE(4)+CORRELATIVO(7)]. */
    static String[] candidatosNoFactu(String entrada) {
        String limpio = entrada == null ? "" : entrada.trim().toUpperCase();
        String normalizado = limpio.replace(" ", "");
        if (normalizado.contains("-")) {
            String[] partes = normalizado.split("-", 2);
            String corr = partes[1].replaceFirst("^0+(?=\\d)", "");
            if (corr.length() <= 7) {
                corr = "0".repeat(7 - corr.length()) + corr;
            }
            normalizado = partes[0] + corr;
        }
        return new String[]{limpio, normalizado};
    }

    public static String limpiarPlaca(String placa) {
        return placa == null ? null : placa.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    private static String sinCerosIzquierda(String s) {
        if (s == null) {
            return null;
        }
        String r = s.trim().replaceFirst("^0+(?=\\d)", "");
        return r.isEmpty() ? s.trim() : r;
    }

    private static void setStr(PreparedStatement ps, int idx, String valor, int max) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(idx, Types.VARCHAR);
        } else {
            String v = valor.trim();
            ps.setString(idx, v.length() > max ? v.substring(0, max) : v);
        }
    }

    private static String nvl(String v, String def) {
        return v == null || v.isBlank() ? def : v;
    }

    private static String primeroNoVacio(String a, String b) {
        return a != null && !a.isBlank() ? a.trim() : (b != null ? b.trim() : null);
    }

    private static LocalDate toLocalDate(Date d) {
        return d == null ? null : d.toLocalDate();
    }
}
