package com.robin.pos.dao;

import com.robin.pos.model.Arinum;
import com.robin.pos.util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones con la tabla INVE.ARINUM
 * Unidades de Medida
 *
 * @author Robin POS
 * @version 1.0
 */
public class ArinumDao {

    private static final Logger LOGGER = Logger.getLogger(ArinumDao.class.getName());

    /**
     * Lista todas las unidades de medida ordenadas por nombre
     *
     * @param noCia Código de compañía
     * @return Lista de unidades de medida
     */
    public List<Arinum> listarTodas(String noCia) {
        List<Arinum> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, UNIDAD, NOM, ESTADO, COD_SUNAT1 AS COD_SUNAT
            FROM INVE.ARINUM
            WHERE NO_CIA = ?
            ORDER BY NOM
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
            LOGGER.log(Level.SEVERE, "Error al listar unidades de medida", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista solo las unidades de medida activas
     *
     * @param noCia Código de compañía
     * @return Lista de unidades de medida activas
     */
    public List<Arinum> listarActivas(String noCia) {
        List<Arinum> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, UNIDAD, NOM, ESTADO, COD_SUNAT1 AS COD_SUNAT
            FROM INVE.ARINUM
            WHERE NO_CIA = ?
            AND ESTADO = 'A'
            ORDER BY NOM
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
            LOGGER.log(Level.SEVERE, "Error al listar unidades de medida activas", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca una unidad de medida por su código
     *
     * @param noCia Código de compañía
     * @param unidad Código de la unidad
     * @return Arinum o null si no existe
     */
    public Arinum buscarPorCodigo(String noCia, String unidad) {
        Arinum arinum = null;

        String sql = """
            SELECT NO_CIA, UNIDAD, NOM, ESTADO, COD_SUNAT1 AS COD_SUNAT
            FROM INVE.ARINUM
            WHERE NO_CIA = ?
            AND UNIDAD = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, unidad);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arinum = mapearResultSet(rs);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar unidad de medida: " + unidad, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arinum;
    }

    /**
     * Busca unidades de medida por nombre (búsqueda parcial)
     *
     * @param noCia Código de compañía
     * @param nombre Nombre a buscar
     * @return Lista de unidades que coinciden
     */
    public List<Arinum> buscarPorNombre(String noCia, String nombre) {
        List<Arinum> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, UNIDAD, NOM, ESTADO, COD_SUNAT1 AS COD_SUNAT
            FROM INVE.ARINUM
            WHERE NO_CIA = ?
            AND UPPER(NOM) LIKE UPPER(?)
            ORDER BY NOM
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, "%" + nombre + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearResultSet(rs));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar unidades por nombre: " + nombre, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca una unidad de medida por código SUNAT
     *
     * @param noCia Código de compañía
     * @param codSunat Código SUNAT de la unidad
     * @return Arinum o null si no existe
     */
    public Arinum buscarPorCodSunat(String noCia, String codSunat) {
        Arinum arinum = null;

        String sql = """
            SELECT NO_CIA, UNIDAD, NOM, ESTADO, COD_SUNAT1 AS COD_SUNAT
            FROM INVE.ARINUM
            WHERE NO_CIA = ?
            AND COD_SUNAT1 = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, codSunat);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                arinum = mapearResultSet(rs);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar unidad por código SUNAT: " + codSunat, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arinum;
    }

    /**
     * Cuenta el total de unidades de medida
     *
     * @param noCia Código de compañía
     * @return Cantidad de unidades
     */
    public int contarUnidades(String noCia) {
        int cantidad = 0;

        String sql = """
            SELECT COUNT(*) AS CANTIDAD
            FROM INVE.ARINUM
            WHERE NO_CIA = ?
            """;

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
            LOGGER.log(Level.SEVERE, "Error al contar unidades de medida", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cantidad;
    }

    /**
     * Mapea un ResultSet a un objeto Arinum
     */
    private Arinum mapearResultSet(ResultSet rs) throws SQLException {
        Arinum arinum = new Arinum();

        arinum.setNoCia(rs.getString("NO_CIA"));
        arinum.setUnidad(rs.getString("UNIDAD"));
        arinum.setNom(rs.getString("NOM"));
        arinum.setEstado(rs.getString("ESTADO"));
        arinum.setCodSunat(rs.getString("COD_SUNAT"));

        return arinum;
    }
}
