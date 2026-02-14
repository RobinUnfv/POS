package com.robin.pos.dao;

import com.robin.pos.model.ComprobanteBaja;
import com.robin.pos.util.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones con Comunicaciones de Baja
 * Tabla: FACTU.COMUNICACION_BAJA
 *
 * @author Robin POS
 * @version 1.0
 */
public class ComunicacionBajaDao {

    private static final Logger LOGGER = Logger.getLogger(ComunicacionBajaDao.class.getName());

    /**
     * Registra una comunicación de baja en la base de datos
     *
     * @param comprobante ComprobanteBaja a registrar
     * @return ID generado de la comunicación de baja, 0 si falla
     */
    public int registrarBaja(ComprobanteBaja comprobante) {

        int idGenerado = 0;

        String sql = "{ call FACTU.PR_FACTURA.REG_COMUNI_BAJA(?, ?, ?, ?, ?) }";

        Connection conexion = null;
        CallableStatement cstmt = null;
        try {
            conexion = ConexionBD.oracle();
            cstmt = conexion.prepareCall(sql);

            cstmt.setString(1, comprobante.getNoCia());
            cstmt.setString(2, comprobante.getNumeroComprobante());
            cstmt.setDate(3, Date.valueOf(comprobante.getFechaBaja()));
            cstmt.setString(4, comprobante.getCodigoMotivo());
            cstmt.setString(5, comprobante.getDescripcionMotivo());

            // Ejecutar el procedimiento
            cstmt.execute();

            idGenerado = 1;

            LOGGER.info("Baja registrada con ID: " + idGenerado);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al registrar baja", ex);
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error en rollback", e);
                }
            }
        } finally {
            ConexionBD.cerrarCxOracle(conexion);
        }

        return idGenerado;
    }

    /**
     * Registra múltiples bajas en una sola comunicación
     *
     * @param comprobantes Lista de comprobantes a dar de baja
     * @param numeroComunicacion Número de comunicación generado
     * @return Número de registros insertados
     */
    public int registrarBajasMasivas(List<ComprobanteBaja> comprobantes, String numeroComunicacion) {

        int totalRegistrados = 0;

        String sql = """
            INSERT INTO FACTU.COMUNICACION_BAJA (
                NO_CIA, TIPO_DOC, NO_FACTU, FECHA_EMISION, FECHA_BAJA,
                CODIGO_MOTIVO, DESCRIPCION_MOTIVO, ESTADO_BAJA, NRO_COMUNICACION
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            cx.setAutoCommit(false);

            PreparedStatement ps = cx.prepareStatement(sql);

            for (ComprobanteBaja comprobante : comprobantes) {
                ps.setString(1, comprobante.getNoCia());
                ps.setString(2, comprobante.getTipoDocumento());
                ps.setString(3, comprobante.getNumeroComprobante());
                ps.setDate(4, Date.valueOf(comprobante.getFechaEmision()));
                ps.setDate(5, Date.valueOf(comprobante.getFechaBaja()));
                ps.setString(6, comprobante.getCodigoMotivo());
                ps.setString(7, comprobante.getDescripcionMotivo());
                ps.setString(8, comprobante.getEstadoBaja());
                ps.setString(9, numeroComunicacion);

                ps.addBatch();
            }

            int[] resultados = ps.executeBatch();
            totalRegistrados = resultados.length;

            ps.close();
            cx.commit();

            LOGGER.info("Bajas masivas registradas: " + totalRegistrados);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al registrar bajas masivas", ex);
            if (cx != null) {
                try {
                    cx.rollback();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error en rollback", e);
                }
            }
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return totalRegistrados;
    }

    /**
     * Actualiza el estado de una comunicación de baja
     *
     * @param idBaja ID de la baja
     * @param estado Nuevo estado
     * @param ticketSunat Ticket de SUNAT (opcional)
     * @return true si se actualizó correctamente
     */
    public boolean actualizarEstadoBaja(int idBaja, String estado, String ticketSunat) {

        boolean actualizado = false;

        String sql = """
            UPDATE FACTU.COMUNICACION_BAJA
            SET ESTADO_BAJA = ?,
                TICKET_SUNAT = ?,
                FECHA_PROCESO = SYSTIMESTAMP
            WHERE ID_BAJA = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);

            ps.setString(1, estado);
            ps.setString(2, ticketSunat);
            ps.setInt(3, idBaja);

            int filasAfectadas = ps.executeUpdate();
            actualizado = filasAfectadas > 0;

            ps.close();
            cx.commit();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar estado de baja", ex);
            if (cx != null) {
                try {
                    cx.rollback();
                } catch (SQLException e) {
                    LOGGER.log(Level.SEVERE, "Error en rollback", e);
                }
            }
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return actualizado;
    }

    /**
     * Lista las bajas pendientes de envío
     *
     * @param noCia Código de compañía
     * @return Lista de comprobantes pendientes de baja
     */
    public List<ComprobanteBaja> listarBajasPendientes(String noCia) {

        List<ComprobanteBaja> lista = new ArrayList<>();

        String sql = """
            SELECT ID_BAJA, NO_CIA, TIPO_DOC, NO_FACTU, FECHA_EMISION, 
                   FECHA_BAJA, CODIGO_MOTIVO, DESCRIPCION_MOTIVO, ESTADO_BAJA,
                   NRO_COMUNICACION, TICKET_SUNAT
            FROM FACTU.COMUNICACION_BAJA
            WHERE NO_CIA = ?
            AND ESTADO_BAJA = 'PENDIENTE'
            ORDER BY FECHA_BAJA DESC
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar bajas pendientes", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista las bajas por número de comunicación
     *
     * @param numeroComunicacion Número de comunicación
     * @return Lista de comprobantes en la comunicación
     */
    public List<ComprobanteBaja> listarPorComunicacion(String numeroComunicacion) {

        List<ComprobanteBaja> lista = new ArrayList<>();

        String sql = """
            SELECT ID_BAJA, NO_CIA, TIPO_DOC, NO_FACTU, FECHA_EMISION, 
                   FECHA_BAJA, CODIGO_MOTIVO, DESCRIPCION_MOTIVO, ESTADO_BAJA,
                   NRO_COMUNICACION, TICKET_SUNAT
            FROM FACTU.COMUNICACION_BAJA
            WHERE NRO_COMUNICACION = ?
            ORDER BY TIPO_DOC, NO_FACTU
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, numeroComunicacion);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar por comunicación", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Verifica si un comprobante ya está registrado para baja
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento
     * @param noFactu Número de factura
     * @return true si ya está registrado
     */
    public boolean existeBaja(String noCia, String tipoDoc, String noFactu) {

        boolean existe = false;

        String sql = """
            SELECT COUNT(*) AS TOTAL
            FROM FACTU.COMUNICACION_BAJA
            WHERE NO_CIA = ?
            AND NO_FACTU = ?
            AND ESTADO = 'A'
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, noFactu);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                existe = rs.getInt("TOTAL") > 0;
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al verificar existencia de baja", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return existe;
    }

    /**
     * Genera el siguiente número de comunicación
     * Formato: RC-YYYYMMDD-###
     *
     * @param noCia Código de compañía
     * @return Número de comunicación generado
     */
    public String generarNumeroComunicacion(String noCia) {

        String numero = null;

        String sql = """
            SELECT 'RC-' || TO_CHAR(SYSDATE, 'YYYYMMDD') || '-' || 
                   LPAD(NVL(MAX(TO_NUMBER(SUBSTR(NRO_COMUNICACION, -3))), 0) + 1, 3, '0') AS NUMERO
            FROM FACTU.COMUNICACION_BAJA
            WHERE NO_CIA = ?
            AND FECHA_BAJA = TRUNC(SYSDATE)
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                numero = rs.getString("NUMERO");
            }

            rs.close();
            ps.close();

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al generar número de comunicación", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return numero != null ? numero : "RC-" + LocalDate.now().toString().replace("-", "") + "-001";
    }

    /**
     * Mapea un ResultSet a un objeto ComprobanteBaja
     */
    private ComprobanteBaja mapearResultSet(ResultSet rs) throws SQLException {

        ComprobanteBaja comprobante = new ComprobanteBaja();

        comprobante.setNoCia(rs.getString("NO_CIA"));
        comprobante.setTipoDocumento(rs.getString("TIPO_DOC"));
        comprobante.setNumeroComprobante(rs.getString("NO_FACTU"));

        Date fechaEmision = rs.getDate("FECHA_EMISION");
        if (fechaEmision != null) {
            comprobante.setFechaEmision(fechaEmision.toLocalDate());
        }

        Date fechaBaja = rs.getDate("FECHA_BAJA");
        if (fechaBaja != null) {
            comprobante.setFechaBaja(fechaBaja.toLocalDate());
        }

        comprobante.setCodigoMotivo(rs.getString("CODIGO_MOTIVO"));
        comprobante.setDescripcionMotivo(rs.getString("DESCRIPCION_MOTIVO"));
        comprobante.setEstadoBaja(rs.getString("ESTADO_BAJA"));
        comprobante.setTicketSunat(rs.getString("TICKET_SUNAT"));

        return comprobante;
    }
}
