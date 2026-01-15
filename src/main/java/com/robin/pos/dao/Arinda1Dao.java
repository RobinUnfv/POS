package com.robin.pos.dao;

import com.robin.pos.model.Arinda1;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones con la tabla INVE.ARINDA1
 * Artículos / Productos
 *
 * @author Robin POS
 * @version 1.1
 */
public class Arinda1Dao {

    private static final Logger LOGGER = Logger.getLogger(Arinda1Dao.class.getName());

    /**
     * Lista todos los artículos de una compañía
     *
     * @param noCia Código de compañía
     * @return Lista de artículos
     */
    public List<Arinda1> listarTodos(String noCia) {
        List<Arinda1> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_ARTI, NO_ARTI, MEDIDA, DESCRIPCION,
                   MONEDA, COSTO_UNI, VIGENTE, STK_MINIMO, STK_MAXIMO, IND_COD_BARRA
            FROM INVE.ARINDA1
            WHERE NO_CIA = ?
            ORDER BY DESCRIPCION
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSetCompleto(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar todos los artículos", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista artículos vigentes de una compañía
     *
     * @param noCia Código de compañía
     * @return Lista de artículos vigentes
     */
    public List<Arinda1> listarVigentes(String noCia) {
        List<Arinda1> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_ARTI, NO_ARTI, MEDIDA, DESCRIPCION,
                   MONEDA, COSTO_UNI, VIGENTE, STK_MINIMO, STK_MAXIMO, IND_COD_BARRA
            FROM INVE.ARINDA1
            WHERE NO_CIA = ?
            AND VIGENTE = 'S'
            ORDER BY DESCRIPCION
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSetCompleto(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar artículos vigentes", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista artículos por tipo
     *
     * @param noCia Código de compañía
     * @param tipoArti Tipo de artículo (PT, MP, SU, etc.)
     * @return Lista de artículos del tipo especificado
     */
    public List<Arinda1> listarPorTipo(String noCia, String tipoArti) {
        List<Arinda1> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_ARTI, NO_ARTI, MEDIDA, DESCRIPCION,
                   MONEDA, COSTO_UNI, VIGENTE, STK_MINIMO, STK_MAXIMO, IND_COD_BARRA
            FROM INVE.ARINDA1
            WHERE NO_CIA = ?
            AND TIPO_ARTI = ?
            ORDER BY DESCRIPCION
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, tipoArti);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSetCompleto(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar artículos por tipo", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca un artículo por su código
     *
     * @param noCia Código de compañía
     * @param noArti Código del artículo
     * @return Arinda1 o null si no existe
     */
    public Arinda1 buscarPorCodigo(String noCia, String noArti) {
        Arinda1 arinda1 = null;

        String sql = """
            SELECT NO_CIA, TIPO_ARTI, NO_ARTI, MEDIDA, DESCRIPCION,
                   MONEDA, COSTO_UNI, VIGENTE, STK_MINIMO, STK_MAXIMO, IND_COD_BARRA
            FROM INVE.ARINDA1
            WHERE NO_CIA = ?
            AND NO_ARTI = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, noArti);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arinda1 = mapearResultSetCompleto(rs);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar artículo por código: " + noArti, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arinda1;
    }

    /**
     * Busca artículos por descripción (búsqueda parcial)
     *
     * @param noCia Código de compañía
     * @param descripcion Descripción a buscar
     * @return Lista de artículos que coinciden
     */
    public List<Arinda1> buscarPorDescripcion(String noCia, String descripcion) {
        List<Arinda1> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_ARTI, NO_ARTI, MEDIDA, DESCRIPCION,
                   MONEDA, COSTO_UNI, VIGENTE, STK_MINIMO, STK_MAXIMO, IND_COD_BARRA
            FROM INVE.ARINDA1
            WHERE NO_CIA = ?
            AND UPPER(DESCRIPCION) LIKE UPPER(?)
            AND ROWNUM <= 50
            ORDER BY DESCRIPCION
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, "%" + descripcion + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSetCompleto(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar artículos por descripción", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca productos (método original simplificado)
     */
    public List<Arinda1> buscarProducto(String noCia) {
        List<Arinda1> listArinda1 = new ArrayList<>();

        String sql = """
            SELECT I.NO_ARTI AS CODIGO, I.DESCRIPCION, I.COSTO_UNI, M.COD_SUNAT1
            FROM INVE.ARINDA1 I, INVE.ARINUM M
            WHERE I.NO_CIA = ?
            AND I.VIGENTE = ?
            AND M.NO_CIA = I.NO_CIA
            AND M.UNIDAD = I.MEDIDA
            ORDER BY I.DESCRIPCION
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, "S");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Arinda1 arinda1 = new Arinda1();
                arinda1.setCodigo(rs.getString("CODIGO"));
                arinda1.setDescripcion(rs.getString("DESCRIPCION"));
                arinda1.setMedida(rs.getString("COD_SUNAT1"));
                arinda1.setCostoUni(rs.getBigDecimal("COSTO_UNI"));
                listArinda1.add(arinda1);
            }
            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar productos", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return listArinda1;
    }

    /**
     * Busca productos por descripción (método original)
     */
    public List<Arinda1> buscarProducto(String noCia, String descripcion) {
        List<Arinda1> listArinda1 = new ArrayList<>();

        String sql = """
            SELECT NO_ARTI AS CODIGO, DESCRIPCION
            FROM INVE.ARINDA1
            WHERE NO_CIA = ?
            AND DESCRIPCION LIKE ?
            AND ROWNUM <= 10
            ORDER BY DESCRIPCION
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, "%" + descripcion.toUpperCase() + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Arinda1 arinda1 = new Arinda1();
                arinda1.setCodigo(rs.getString("CODIGO"));
                arinda1.setDescripcion(rs.getString("DESCRIPCION"));
                listArinda1.add(arinda1);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar productos por descripción", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return listArinda1;
    }

    /**
     * Cuenta el total de artículos
     */
    public int contarArticulos(String noCia) {
        int cantidad = 0;

        String sql = "SELECT COUNT(*) AS CANTIDAD FROM INVE.ARINDA1 WHERE NO_CIA = ?";

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cantidad = rs.getInt("CANTIDAD");
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al contar artículos", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cantidad;
    }

    /**
     * Cuenta artículos vigentes
     */
    public int contarVigentes(String noCia) {
        int cantidad = 0;

        String sql = "SELECT COUNT(*) AS CANTIDAD FROM INVE.ARINDA1 WHERE NO_CIA = ? AND VIGENTE = 'S'";

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cantidad = rs.getInt("CANTIDAD");
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al contar artículos vigentes", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cantidad;
    }

    /**
     * Ejecuta el procedimiento almacenado para guardar el artículo
     */
    public boolean ejecutarGuardarArticulo(String cia, String tipoArti, String noArti, String descrip,
                                           String medida, String moneda, String vigente, BigDecimal costoUni,
                                           BigDecimal stkMinimo, BigDecimal stkMaximo) throws SQLException {
        Connection cx = null;
        CallableStatement cs = null;

        try {
            cx = ConexionBD.oracle();

            String sql = "{CALL INVE.PR_ARTICULO.GUARDAR(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            cs = cx.prepareCall(sql);

            cs.setString(1, cia);
            cs.setString(2, tipoArti);
            cs.setString(3, noArti);
            cs.setString(4, descrip);
            cs.setString(5, medida);
            cs.setString(6, moneda);
            cs.setString(7, vigente);
            cs.setBigDecimal(8, costoUni);
            cs.setBigDecimal(9, stkMinimo);
            cs.setBigDecimal(10, stkMaximo);
            cs.setString(11, "N");

            cs.execute();

            LOGGER.info("Artículo guardado exitosamente: " + noArti);
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al ejecutar procedimiento GUARDAR", ex);
            throw ex;
        } finally {
            if (cs != null) {
                try { cs.close(); } catch (SQLException ignored) {}
            }
            ConexionBD.cerrarCxOracle(cx);
        }
    }

    /**
     * Valida si el código del artículo ya existe en la base de datos
     */
    /**
     * Cuenta artículos vigentes
     */
    public String validarCodigoExistente(String cia, String noArti) {

        String existe = "N";

        String sql = "SELECT INVE.PR_ARTICULO.VALIDAR_CODIGO (?,?) as VALIDAR FROM DUAL";

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, cia);
            ps.setString(2, noArti);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                existe = rs.getString("VALIDAR");
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al validar código de artículo", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return existe;
    }
    /*
    public String validarCodigoExistente(String cia, String noArti) throws SQLException {
        Connection cx = null;
        CallableStatement cs = null;
        String resultado = "N";

        try {
            cx = ConexionBD.oracle();

            String sql = "{? = CALL INVE.PR_ARTICULO.VALIDAR_CODIGO(?, ?)}";
            cs = cx.prepareCall(sql);

            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, cia);
            cs.setString(3, noArti);

            cs.execute();
            resultado = cs.getString(1);

            LOGGER.info("Validación de código '" + noArti + "': " + (resultado.equals("S") ? "EXISTE" : "NO EXISTE"));

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al validar código de artículo", ex);
            throw ex;
        } finally {
            if (cs != null) {
                try { cs.close(); } catch (SQLException ignored) {}
            }
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }
    */
    /**
     * Mapea un ResultSet completo a un objeto Arinda1
     */
    private Arinda1 mapearResultSetCompleto(ResultSet rs) throws SQLException {
        Arinda1 arinda1 = new Arinda1();

        arinda1.setNoCia(rs.getString("NO_CIA"));
        arinda1.setTipoArti(rs.getString("TIPO_ARTI"));
        arinda1.setCodigo(rs.getString("NO_ARTI"));
        arinda1.setDescripcion(rs.getString("DESCRIPCION"));
        arinda1.setMedida(rs.getString("MEDIDA"));
        arinda1.setMoneda(rs.getString("MONEDA"));
        arinda1.setCostoUni(rs.getBigDecimal("COSTO_UNI"));
        arinda1.setVigente(rs.getString("VIGENTE"));
        arinda1.setStkMinimo(rs.getBigDecimal("STK_MINIMO"));
        arinda1.setStkMaximo(rs.getBigDecimal("STK_MAXIMO"));
        arinda1.setIndCodBarra(rs.getString("IND_COD_BARRA"));

        return arinda1;
    }
}