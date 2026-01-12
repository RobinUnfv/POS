package com.robin.pos.dao;

import com.robin.pos.model.Arfafe;
import com.robin.pos.util.ConexionBD;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones con la tabla FACTU.ARFAFE
 * Cabecera de Comprobantes de Pago (Facturas, Boletas, etc.)
 *
 * @author Robin POS
 * @version 1.0
 */
public class ArfafeDao {

    private static final Logger LOGGER = Logger.getLogger(ArfafeDao.class.getName());

    /**
     * Busca un comprobante de pago por número de factura
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento (F=Factura, B=Boleta)
     * @param noFactu Número de factura/boleta
     * @return Arfafe o null si no existe
     */
    public Arfafe buscarPorNumero(String noCia, String tipoDoc, String noFactu) {
        Arfafe arfafe = null;

        String sql = """
            SELECT F.NO_CIA, F.TIPO_DOC, F.NO_FACTU, F.NO_CLIENTE,F.TIPO_DOC_CLI, F.NUM_DOC_CLI, F.FECHA, F.NBR_CLIENTE, 
                   F.MONEDA, F.NO_ORDEN, F.SUB_TOTAL, F.IMPUESTO, F.TOTAL, F.ESTADO, F.VALOR_VENTA, F.TOTAL_BRUTO, 
                   F.OPER_GRAVADAS, F.GUIA_TEMP, D.DIRECCION
            FROM FACTU.ARFAFE F, CXC.ARCCTDA D
            WHERE F.NO_CIA = ?
            AND F.TIPO_DOC = ?
            AND F.NO_FACTU = ?
            AND D.NO_CIA = F.NO_CIA
            AND D.COD_TIENDA = ?
            AND D.NO_CLIENTE = F.NO_CLIENTE
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, noFactu);
            ps.setString(4, "001");

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arfafe = mapearResultSet(rs);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar comprobante por número: " + noFactu, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arfafe;
    }

    /**
     * Lista comprobantes de pago por tipo de documento y estado
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento (F=Factura, B=Boleta)
     * @param estado Estado del documento (D=Despachado, P=Pendiente, A=Anulado)
     * @return Lista de comprobantes
     */
    public List<Arfafe> listarPorTipoYEstado(String noCia, String tipoDoc, String estado) {
        List<Arfafe> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOC, NO_FACTU, NO_CLIENTE, TIPO_DOC_CLI, NUM_DOC_CLI, 
                   FECHA, NBR_CLIENTE, MONEDA, NO_ORDEN, SUB_TOTAL, IMPUESTO, TOTAL, 
                   ESTADO, VALOR_VENTA, TOTAL_BRUTO, OPER_GRAVADAS, GUIA_TEMP
            FROM FACTU.ARFAFE
            WHERE NO_CIA = ?
            AND TIPO_DOC = ?
            AND ESTADO = ?
            ORDER BY FECHA DESC, NO_FACTU DESC
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, estado);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar comprobantes por tipo y estado", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista comprobantes de pago por rango de fechas
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento
     * @param estado Estado del documento
     * @param fechaInicio Fecha inicio
     * @param fechaFin Fecha fin
     * @return Lista de comprobantes
     */
    public List<Arfafe> listarPorFechas(String noCia, String tipoDoc, String estado,
                                        LocalDate fechaInicio, LocalDate fechaFin) {
        List<Arfafe> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOC, NO_FACTU, NO_CLIENTE, TIPO_DOC_CLI, NUM_DOC_CLI, 
                   FECHA, NBR_CLIENTE, MONEDA, NO_ORDEN, SUB_TOTAL, IMPUESTO, TOTAL, 
                   ESTADO, VALOR_VENTA, TOTAL_BRUTO, OPER_GRAVADAS, GUIA_TEMP
            FROM FACTU.ARFAFE
            WHERE NO_CIA = ?
            AND TIPO_DOC = ?
            AND ESTADO = ?
            AND TRUNC(FECHA) BETWEEN ? AND ?
            ORDER BY FECHA DESC, NO_FACTU DESC
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, estado);
            ps.setDate(4, Date.valueOf(fechaInicio));
            ps.setDate(5, Date.valueOf(fechaFin));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar comprobantes por fechas", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista todos los comprobantes de pago por rango de fechas (sin filtro de tipo/estado)
     *
     * @param noCia Código de compañía
     * @param fechaInicio Fecha inicio
     * @param fechaFin Fecha fin
     * @return Lista de comprobantes
     */
    public List<Arfafe> listarTodosPorFechas(String noCia, LocalDate fechaInicio, LocalDate fechaFin) {
        List<Arfafe> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOC, NO_FACTU, NO_CLIENTE, TIPO_DOC_CLI, NUM_DOC_CLI, 
                   FECHA, NBR_CLIENTE, MONEDA, NO_ORDEN, SUB_TOTAL, IMPUESTO, TOTAL, 
                   ESTADO, VALOR_VENTA, TOTAL_BRUTO, OPER_GRAVADAS, GUIA_TEMP
            FROM FACTU.ARFAFE
            WHERE NO_CIA = ?
            AND TRUNC(FECHA) BETWEEN ? AND ?
            ORDER BY FECHA DESC, NO_FACTU DESC
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setDate(2, Date.valueOf(fechaInicio));
            ps.setDate(3, Date.valueOf(fechaFin));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar todos los comprobantes por fechas", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca comprobantes por número de documento del cliente
     *
     * @param noCia Código de compañía
     * @param numDocCli Número de documento del cliente (RUC/DNI)
     * @return Lista de comprobantes
     */
    public List<Arfafe> buscarPorDocumentoCliente(String noCia, String numDocCli) {
        List<Arfafe> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOC, NO_FACTU, NO_CLIENTE, TIPO_DOC_CLI, NUM_DOC_CLI, 
                   FECHA, NBR_CLIENTE, MONEDA, NO_ORDEN, SUB_TOTAL, IMPUESTO, TOTAL, 
                   ESTADO, VALOR_VENTA, TOTAL_BRUTO, OPER_GRAVADAS, GUIA_TEMP
            FROM FACTU.ARFAFE
            WHERE NO_CIA = ?
            AND NUM_DOC_CLI = ?
            ORDER BY FECHA DESC, NO_FACTU DESC
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, numDocCli);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar comprobantes por documento cliente: " + numDocCli, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca comprobantes por número de orden
     *
     * @param noCia Código de compañía
     * @param noOrden Número de orden
     * @return Arfafe o null si no existe
     */
    public Arfafe buscarPorOrden(String noCia, String noOrden) {
        Arfafe arfafe = null;

        String sql = """
            SELECT NO_CIA, TIPO_DOC, NO_FACTU, NO_CLIENTE, TIPO_DOC_CLI, NUM_DOC_CLI, 
                   FECHA, NBR_CLIENTE, MONEDA, NO_ORDEN, SUB_TOTAL, IMPUESTO, TOTAL, 
                   ESTADO, VALOR_VENTA, TOTAL_BRUTO, OPER_GRAVADAS, GUIA_TEMP
            FROM FACTU.ARFAFE
            WHERE NO_CIA = ?
            AND NO_ORDEN = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, noOrden);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arfafe = mapearResultSet(rs);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar comprobante por orden: " + noOrden, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arfafe;
    }

    /**
     * Mapea un ResultSet a un objeto Arfafe
     */
    private Arfafe mapearResultSet(ResultSet rs) throws SQLException {
        Arfafe arfafe = new Arfafe();

        arfafe.setNoCia(rs.getString("NO_CIA"));
        arfafe.setTipoDoc(rs.getString("TIPO_DOC"));
        arfafe.setNoFactu(rs.getString("NO_FACTU"));
        arfafe.setNoCliente(rs.getString("NO_CLIENTE"));
        arfafe.setTipoDocCli(rs.getString("TIPO_DOC_CLI"));
        arfafe.setNumDocCli(rs.getString("NUM_DOC_CLI"));
        arfafe.setFecha(rs.getDate("FECHA"));
        arfafe.setNbrCliente(rs.getString("NBR_CLIENTE"));
        arfafe.setMoneda(rs.getString("MONEDA"));
        arfafe.setNoOrden(rs.getString("NO_ORDEN"));
        arfafe.setSubTotal(rs.getBigDecimal("SUB_TOTAL"));
        arfafe.setImpuesto(rs.getBigDecimal("IMPUESTO"));
        arfafe.setTotal(rs.getBigDecimal("TOTAL"));
        arfafe.setEstado(rs.getString("ESTADO"));
        arfafe.setValorVenta(rs.getBigDecimal("VALOR_VENTA"));
        arfafe.setTotalBruto(rs.getBigDecimal("TOTAL_BRUTO"));
        arfafe.setOperGravadas(rs.getBigDecimal("OPER_GRAVADAS"));
        arfafe.setGuiaTemp(rs.getString("GUIA_TEMP"));
        arfafe.setDireccion(rs.getString("DIRECCION"));

        return arfafe;
    }
}