package com.robin.pos.dao;

import com.robin.pos.model.RegVta;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para Registro de Ventas
 * Gestiona el procedimiento FACTU.REGISTRO_VENTA y consultas
 *
 * @author Robin POS
 * @version 1.0
 */
public class RegistroVentaDao {

    private static final Logger LOGGER = Logger.getLogger(RegistroVentaDao.class.getName());

    /**
     * Ejecuta el procedimiento almacenado FACTU.REGISTRO_VENTA
     *
     * @param noCia Número de compañía
     * @param fechaInicio Fecha inicial
     * @param fechaFin Fecha final
     * @param tipoDoc Tipo de documento (TODOS, F, B, etc)
     * @param moneda Moneda (Soles, Dolares)
     * @return Usuario que ejecutó (pUser OUT)
     */
    public String ejecutarRegistroVenta(String noCia, LocalDate fechaInicio,
                                        LocalDate fechaFin, String tipoDoc,
                                        String moneda) {
        String usuario = null;
        Connection cx = null;
        CallableStatement cs = null;

        try {
            cx = ConexionBD.oracle();

            // Preparar llamada al procedimiento
            String sql = "{call FACTU.PR_FACTURA.REGISTRO_VENTA(?, ?, ?, ?, ?, ?)}";
            cs = cx.prepareCall(sql);

            // Parámetros IN
            cs.setString(1, noCia);
            cs.setDate(2, Date.valueOf(fechaInicio));
            cs.setDate(3, Date.valueOf(fechaFin));
            cs.setString(4, tipoDoc);
            cs.setString(5, moneda);

            // Parámetro OUT
            cs.registerOutParameter(6, Types.VARCHAR);

            // Ejecutar procedimiento
            cs.execute();

            // Obtener parámetro OUT
            usuario = cs.getString(6);

            LOGGER.info("Procedimiento REGISTRO_VENTA ejecutado. Usuario: " + usuario);

            Mensaje.alerta(null, "Proceso Exitoso",
                    "El registro de ventas se generó correctamente.\n" +
                            "Período: " + fechaInicio + " al " + fechaFin);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al ejecutar REGISTRO_VENTA", ex);
            Mensaje.error(null, "Error al Procesar",
                    "No se pudo generar el registro de ventas.\n" + ex.getMessage());
        } finally {
            try {
                if (cs != null) cs.close();
                ConexionBD.cerrarCxOracle(cx);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar recursos", e);
            }
        }

        return usuario;
    }

    /**
     * Obtiene los registros de venta para el reporte
     *
     * @param noCia Número de compañía
     * @param userId ID de usuario (audsid)
     * @param fechaDesde Fecha desde
     * @param fechaHasta Fecha hasta
     * @return Lista de registros de venta
     */
    public List<RegVta> obtenerRegistrosVenta(String noCia, String userId,
                                              LocalDate fechaDesde, LocalDate fechaHasta) {
        List<RegVta> registros = new ArrayList<>();
        Connection cx = null;
        PreparedStatement ps = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT no_cia, fecha, cod_sunat, nbr_sunat, ");
        sql.append("decode(length(no_factu), 11, substr(no_factu,1,4), substr(no_factu,1,3)) serie, ");
        sql.append("decode(length(no_factu), 11, substr(no_factu,5,11), substr(no_factu,4,10)) factura, ");
        sql.append("decode(ind_anu_dev,'A',NULL,no_ruc) ruc, ");
        sql.append("nbr_cliente nombre, ");
        sql.append("imp_gravable, imp_exonerado, imp_igv, imp_isc, imp_otros, imp_total, ");
        sql.append("tipo_cambio, imp_original, valor_fob, transaccion, tipo_obse, ");
        sql.append("no_factu, tipo_doc, correlativo, fec_refe_factu, fecha_vence, ");
        sql.append("tipo_refe_factu, ");
        sql.append("decode(length(no_refe_factu), 11, substr(no_refe_factu,1,4), substr(no_refe_factu,1,3)) serie_refe, ");
        sql.append("decode(length(no_refe_factu), 11, substr(no_refe_factu,5,11), substr(no_refe_factu,4,10)) factura_refe, ");
        sql.append("sunat_refe, tipo_doc_emp, num_doc_emp, redondeo ");
        sql.append("FROM FACTU.REG_VTA ");
        sql.append("WHERE no_cia = ? ");
        sql.append("AND audsid = ? ");
        sql.append("AND fecha BETWEEN ? AND ? ");
        sql.append("ORDER BY cod_sunat, fecha");

        try {
            cx = ConexionBD.oracle();
            ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, userId);
            ps.setDate(3, Date.valueOf(fechaDesde));
            ps.setDate(4, Date.valueOf(fechaHasta));

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                RegVta reg = new RegVta();
                reg.setNoCia(rs.getString("no_cia"));
                reg.setFecha(rs.getDate("fecha") != null ? rs.getDate("fecha").toLocalDate() : null);
                reg.setCodSunat(rs.getString("cod_sunat"));
                reg.setNbrSunat(rs.getString("nbr_sunat"));
                reg.setSerie(rs.getString("serie"));
                reg.setFactura(rs.getString("factura"));
                reg.setRuc(rs.getString("ruc"));
                reg.setNombre(rs.getString("nombre"));
                reg.setImpGravable(rs.getBigDecimal("imp_gravable"));
                reg.setImpExonerado(rs.getBigDecimal("imp_exonerado"));
                reg.setImpIgv(rs.getBigDecimal("imp_igv"));
                reg.setImpIsc(rs.getBigDecimal("imp_isc"));
                reg.setImpOtros(rs.getBigDecimal("imp_otros"));
                reg.setImpTotal(rs.getBigDecimal("imp_total"));
                reg.setTipoCambio(rs.getBigDecimal("tipo_cambio"));
                reg.setImpOriginal(rs.getBigDecimal("imp_original"));
                reg.setValorFob(rs.getBigDecimal("valor_fob"));
                reg.setTransaccion(rs.getString("transaccion"));
                reg.setTipoObse(rs.getString("tipo_obse"));
                reg.setNoFactu(rs.getString("no_factu"));
                reg.setTipoDoc(rs.getString("tipo_doc"));
                reg.setCorrelativo(rs.getInt("correlativo"));
                reg.setFecRefeFactu(rs.getDate("fec_refe_factu") != null ? rs.getDate("fec_refe_factu").toLocalDate() : null);
                reg.setFechaVence(rs.getDate("fecha_vence") != null ? rs.getDate("fecha_vence").toLocalDate() : null);
                reg.setTipoRefeFactu(rs.getString("tipo_refe_factu"));
                reg.setSerieRefe(rs.getString("serie_refe"));
                reg.setFacturaRefe(rs.getString("factura_refe"));
                reg.setSunatRefe(rs.getString("sunat_refe"));
                reg.setTipoDocEmp(rs.getString("tipo_doc_emp"));
                reg.setNumDocEmp(rs.getString("num_doc_emp"));
                reg.setRedondeo(rs.getBigDecimal("redondeo"));

                registros.add(reg);
            }

            LOGGER.info("Se obtuvieron " + registros.size() + " registros de venta");

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener registros de venta", ex);
            Mensaje.error(null, "Error de Consulta",
                    "No se pudieron obtener los registros.\n" + ex.getMessage());
        } finally {
            try {
                if (ps != null) ps.close();
                ConexionBD.cerrarCxOracle(cx);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar recursos", e);
            }
        }

        return registros;
    }
}
