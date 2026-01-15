package com.robin.pos.dao;

import com.robin.pos.model.Arfafl;
import com.robin.pos.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones con la tabla FACTU.ARFAFL
 * Detalle de Comprobantes de Pago (líneas de productos)
 *
 * @author Robin POS
 * @version 1.0
 */
public class ArfaflDao {

    private static final Logger LOGGER = Logger.getLogger(ArfaflDao.class.getName());

    /**
     * Lista el detalle de un comprobante de pago con descripción del producto
     * Une ARFAFL con ARFAFE y ARPFOL para obtener la descripción
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento (F=Factura, B=Boleta)
     * @param noFactu Número de factura/boleta
     * @return Lista de líneas de detalle
     */
    public List<Arfafl> listarDetallePorFactura(String noCia, String tipoDoc, String noFactu) {
        List<Arfafl> lista = new ArrayList<>();

        String sql = """
            SELECT F.NO_CIA, F.TIPO_DOC, F.NO_FACTU, F.NO_ARTI, P.DESCRIPCION, 
                   F.MEDIDA, F.CONSECUTIVO, F.CANTIDAD_FACT, F.PRECIO_UNIT, 
                   F.IMP_IGV, F.TOTAL, F.TOTAL_LIN, F.PREC_IGV
            FROM FACTU.ARFAFL F, FACTU.ARFAFE X, FACTU.ARPFOL P 
            WHERE F.NO_CIA = ?
            AND F.TIPO_DOC = ?
            AND F.NO_FACTU = ?
            AND X.NO_CIA = F.NO_CIA
            AND X.TIPO_DOC = F.TIPO_DOC
            AND X.NO_FACTU = F.NO_FACTU
            AND P.NO_CIA = X.NO_CIA
            AND P.NO_ARTI = F.NO_ARTI
            AND P.NO_ORDEN = X.NO_ORDEN
            ORDER BY F.CONSECUTIVO
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, noFactu);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar detalle de factura: " + noFactu, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista el detalle de un comprobante de pago (solo tabla ARFAFL)
     * Sin join con otras tablas, descripción puede venir vacía
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento
     * @param noFactu Número de factura
     * @return Lista de líneas de detalle
     */
    public List<Arfafl> listarDetalleSimple(String noCia, String tipoDoc, String noFactu) {
        List<Arfafl> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOC, NO_FACTU, NO_ARTI, '' AS DESCRIPCION, 
                   MEDIDA, CONSECUTIVO, CANTIDAD_FACT, PRECIO_UNIT, 
                   IMP_IGV, TOTAL, TOTAL_LIN, PREC_IGV
            FROM FACTU.ARFAFL
            WHERE NO_CIA = ?
            AND TIPO_DOC = ?
            AND NO_FACTU = ?
            ORDER BY CONSECUTIVO
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, noFactu);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar detalle simple de factura: " + noFactu, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista el detalle con descripción desde tabla de artículos ARINDA
     * Alternativa cuando no hay datos en ARPFOL
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento
     * @param noFactu Número de factura
     * @return Lista de líneas de detalle
     */
    public List<Arfafl> listarDetalleConArticulo(String noCia, String tipoDoc, String noFactu) {
        List<Arfafl> lista = new ArrayList<>();

        String sql = """
            SELECT F.NO_CIA, F.TIPO_DOC, F.NO_FACTU, F.NO_ARTI, 
                   NVL(A.DESCRIPCION, F.NO_ARTI) AS DESCRIPCION, 
                   F.MEDIDA, F.CONSECUTIVO, F.CANTIDAD_FACT, F.PRECIO_UNIT, 
                   F.IMP_IGV, F.TOTAL, F.TOTAL_LIN, F.PREC_IGV
            FROM FACTU.ARFAFL F
            LEFT JOIN INV.ARINDA A ON A.NO_CIA = F.NO_CIA AND A.NO_ARTI = F.NO_ARTI
            WHERE F.NO_CIA = ?
            AND F.TIPO_DOC = ?
            AND F.NO_FACTU = ?
            ORDER BY F.CONSECUTIVO
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, noFactu);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar detalle con artículo: " + noFactu, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca una línea específica del detalle
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento
     * @param noFactu Número de factura
     * @param consecutivo Número de línea
     * @return Arfafl o null si no existe
     */
    public Arfafl buscarLinea(String noCia, String tipoDoc, String noFactu, Integer consecutivo) {
        Arfafl arfafl = null;

        String sql = """
            SELECT F.NO_CIA, F.TIPO_DOC, F.NO_FACTU, F.NO_ARTI, P.DESCRIPCION, 
                   F.MEDIDA, F.CONSECUTIVO, F.CANTIDAD_FACT, F.PRECIO_UNIT, 
                   F.IMP_IGV, F.TOTAL, F.TOTAL_LIN, F.PREC_IGV, F.MEDIDA
            FROM FACTU.ARFAFL F, FACTU.ARFAFE X, FACTU.ARPFOL P 
            WHERE F.NO_CIA = ?
            AND F.TIPO_DOC = ?
            AND F.NO_FACTU = ?
            AND F.CONSECUTIVO = ?
            AND X.NO_CIA = F.NO_CIA
            AND X.TIPO_DOC = F.TIPO_DOC
            AND X.NO_FACTU = F.NO_FACTU
            AND P.NO_CIA = X.NO_CIA
            AND P.NO_ARTI = F.NO_ARTI
            AND P.NO_ORDEN = X.NO_ORDEN
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, noFactu);
            ps.setInt(4, consecutivo);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arfafl = mapearResultSet(rs);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar línea de detalle: " + noFactu + "-" + consecutivo, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arfafl;
    }

    /**
     * Cuenta las líneas de un comprobante
     *
     * @param noCia Código de compañía
     * @param tipoDoc Tipo de documento
     * @param noFactu Número de factura
     * @return Cantidad de líneas
     */
    public int contarLineas(String noCia, String tipoDoc, String noFactu) {
        int cantidad = 0;

        String sql = """
            SELECT COUNT(*) AS CANTIDAD
            FROM FACTU.ARFAFL
            WHERE NO_CIA = ?
            AND TIPO_DOC = ?
            AND NO_FACTU = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ps.setString(3, noFactu);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cantidad = rs.getInt("CANTIDAD");
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al contar líneas de factura: " + noFactu, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cantidad;
    }

    /**
     * Mapea un ResultSet a un objeto Arfafl
     */
    private Arfafl mapearResultSet(ResultSet rs) throws SQLException {
        Arfafl arfafl = new Arfafl();

        arfafl.setNoCia(rs.getString("NO_CIA"));
        arfafl.setTipoDoc(rs.getString("TIPO_DOC"));
        arfafl.setNoFactu(rs.getString("NO_FACTU"));
        arfafl.setNoArti(rs.getString("NO_ARTI"));
        arfafl.setDescripcion(rs.getString("DESCRIPCION"));
        arfafl.setMedida(rs.getString("MEDIDA"));
        arfafl.setConsecutivo(rs.getInt("CONSECUTIVO"));
        arfafl.setCantidadFact(rs.getBigDecimal("CANTIDAD_FACT"));
        arfafl.setPrecioUnit(rs.getBigDecimal("PRECIO_UNIT"));
        arfafl.setImpIgv(rs.getBigDecimal("IMP_IGV"));
        arfafl.setTotal(rs.getBigDecimal("TOTAL"));
        arfafl.setTotalLin(rs.getBigDecimal("TOTAL_LIN"));
        arfafl.setPrecIgv(rs.getBigDecimal("PREC_IGV"));

        System.out.println("FACTU => "+arfafl.getMedida());

        return arfafl;
    }
}