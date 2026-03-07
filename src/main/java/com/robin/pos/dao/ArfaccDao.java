package com.robin.pos.dao;

import com.robin.pos.model.Arfacc;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla FACTU.ARFACC
 * Gestión de series y correlativos de documentos
 *
 * @author Robin POS
 * @version 1.0
 */
public class ArfaccDao {

    private static final Logger LOGGER = Logger.getLogger(ArfaccDao.class.getName());

    /**
     * Lista todas las series de una compañía y centro
     *
     * @param noCia Número de compañía
     * @param centro Centro de costo
     * @return Lista de series
     */
    public List<Arfacc> listarSeries(String noCia, String centro) {
        List<Arfacc> lstSeries = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? AND CENTRO = ? ");
        sql.append("ORDER BY TIPO_DOC, SERIE");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, centro);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfacc serie = new Arfacc();
                serie.setNoCia(rs.getString("NO_CIA"));
                serie.setCentro(rs.getString("CENTRO"));
                serie.setTipoDoc(rs.getString("TIPO_DOC"));
                serie.setSerie(rs.getString("SERIE"));
                serie.setConsDesde(rs.getInt("CONS_DESDE"));
                serie.setLineas(rs.getInt("LINEAS"));
                serie.setIndControlAuto(rs.getString("IND_CONTROL_AUTO"));
                serie.setActivo(rs.getString("ACTIVO"));
                serie.setNoCaba(rs.getString("NO_CABA"));

                lstSeries.add(serie);
            }

            ConexionBD.cerrarCxOracle(cx);
            LOGGER.info("Se encontraron " + lstSeries.size() + " series");

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar series", ex);
            Mensaje.error(null, "Error al Listar Series",
                    "No se pudo obtener la lista de series.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstSeries;
    }

    /**
     * Lista todas las series de una compañía
     *
     * @param noCia Número de compañía
     * @return Lista de series
     */
    public List<Arfacc> listarTodasSeries(String noCia) {
        List<Arfacc> lstSeries = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("ORDER BY CENTRO, TIPO_DOC, SERIE");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfacc serie = new Arfacc();
                serie.setNoCia(rs.getString("NO_CIA"));
                serie.setCentro(rs.getString("CENTRO"));
                serie.setTipoDoc(rs.getString("TIPO_DOC"));
                serie.setSerie(rs.getString("SERIE"));
                serie.setConsDesde(rs.getInt("CONS_DESDE"));
                serie.setLineas(rs.getInt("LINEAS"));
                serie.setIndControlAuto(rs.getString("IND_CONTROL_AUTO"));
                serie.setActivo(rs.getString("ACTIVO"));
                serie.setNoCaba(rs.getString("NO_CABA"));

                lstSeries.add(serie);
            }

            ConexionBD.cerrarCxOracle(cx);
            LOGGER.info("Se encontraron " + lstSeries.size() + " series totales");

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar todas las series", ex);
            Mensaje.error(null, "Error al Listar Series",
                    "No se pudo obtener la lista de series.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstSeries;
    }

    /**
     * Busca una serie específica
     *
     * @param noCia Número de compañía
     * @param centro Centro de costo
     * @param tipoDoc Tipo de documento
     * @param serie Número de serie
     * @return Serie encontrada o null
     */
    public Arfacc buscarSerie(String noCia, String centro, String tipoDoc, String serie) {
        Arfacc arfacc = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? AND CENTRO = ? AND TIPO_DOC = ? AND SERIE = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, centro);
            ps.setString(3, tipoDoc);
            ps.setString(4, serie);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                arfacc = new Arfacc();
                arfacc.setNoCia(rs.getString("NO_CIA"));
                arfacc.setCentro(rs.getString("CENTRO"));
                arfacc.setTipoDoc(rs.getString("TIPO_DOC"));
                arfacc.setSerie(rs.getString("SERIE"));
                arfacc.setConsDesde(rs.getInt("CONS_DESDE"));
                arfacc.setLineas(rs.getInt("LINEAS"));
                arfacc.setIndControlAuto(rs.getString("IND_CONTROL_AUTO"));
                arfacc.setActivo(rs.getString("ACTIVO"));
                arfacc.setNoCaba(rs.getString("NO_CABA"));
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar serie", ex);
            Mensaje.error(null, "Error de Búsqueda",
                    "No se pudo buscar la serie.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return arfacc;
    }

    /**
     * Filtra series por centro
     *
     * @param noCia Número de compañía
     * @param centro Centro de costo
     * @return Lista de series filtradas
     */
    public List<Arfacc> filtrarPorCentro(String noCia, String centro) {
        return listarSeries(noCia, centro);
    }

    /**
     * Filtra series por tipo de documento
     *
     * @param noCia Número de compañía
     * @param tipoDoc Tipo de documento
     * @return Lista de series filtradas
     */
    public List<Arfacc> filtrarPorTipoDoc(String noCia, String tipoDoc) {
        List<Arfacc> lstSeries = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? AND TIPO_DOC = ? ");
        sql.append("ORDER BY CENTRO, SERIE");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, tipoDoc);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfacc serie = new Arfacc();
                serie.setNoCia(rs.getString("NO_CIA"));
                serie.setCentro(rs.getString("CENTRO"));
                serie.setTipoDoc(rs.getString("TIPO_DOC"));
                serie.setSerie(rs.getString("SERIE"));
                serie.setConsDesde(rs.getInt("CONS_DESDE"));
                serie.setLineas(rs.getInt("LINEAS"));
                serie.setIndControlAuto(rs.getString("IND_CONTROL_AUTO"));
                serie.setActivo(rs.getString("ACTIVO"));
                serie.setNoCaba(rs.getString("NO_CABA"));

                lstSeries.add(serie);
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al filtrar por tipo documento", ex);
            Mensaje.error(null, "Error de Filtro",
                    "No se pudo filtrar por tipo de documento.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstSeries;
    }

    /**
     * Filtra series por estado
     *
     * @param noCia Número de compañía
     * @param activo Estado (S: Activo, N: Inactivo)
     * @return Lista de series filtradas
     */
    public List<Arfacc> filtrarPorEstado(String noCia, String activo) {
        List<Arfacc> lstSeries = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? AND ACTIVO = ? ");
        sql.append("ORDER BY CENTRO, TIPO_DOC, SERIE");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, activo);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfacc serie = new Arfacc();
                serie.setNoCia(rs.getString("NO_CIA"));
                serie.setCentro(rs.getString("CENTRO"));
                serie.setTipoDoc(rs.getString("TIPO_DOC"));
                serie.setSerie(rs.getString("SERIE"));
                serie.setConsDesde(rs.getInt("CONS_DESDE"));
                serie.setLineas(rs.getInt("LINEAS"));
                serie.setIndControlAuto(rs.getString("IND_CONTROL_AUTO"));
                serie.setActivo(rs.getString("ACTIVO"));
                serie.setNoCaba(rs.getString("NO_CABA"));

                lstSeries.add(serie);
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al filtrar por estado", ex);
            Mensaje.error(null, "Error de Filtro",
                    "No se pudo filtrar por estado.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstSeries;
    }

    /**
     * Busca series por texto (serie, centro o tipo documento)
     *
     * @param noCia Número de compañía
     * @param texto Texto a buscar
     * @return Lista de series que coinciden
     */
    public List<Arfacc> buscarPorTexto(String noCia, String texto) {
        List<Arfacc> lstSeries = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("AND (UPPER(SERIE) LIKE ? ");
        sql.append("OR UPPER(CENTRO) LIKE ? ");
        sql.append("OR UPPER(TIPO_DOC) LIKE ?) ");
        sql.append("ORDER BY CENTRO, TIPO_DOC, SERIE");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            String textoBusqueda = "%" + texto.toUpperCase() + "%";
            ps.setString(2, textoBusqueda);
            ps.setString(3, textoBusqueda);
            ps.setString(4, textoBusqueda);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfacc serie = new Arfacc();
                serie.setNoCia(rs.getString("NO_CIA"));
                serie.setCentro(rs.getString("CENTRO"));
                serie.setTipoDoc(rs.getString("TIPO_DOC"));
                serie.setSerie(rs.getString("SERIE"));
                serie.setConsDesde(rs.getInt("CONS_DESDE"));
                serie.setLineas(rs.getInt("LINEAS"));
                serie.setIndControlAuto(rs.getString("IND_CONTROL_AUTO"));
                serie.setActivo(rs.getString("ACTIVO"));
                serie.setNoCaba(rs.getString("NO_CABA"));

                lstSeries.add(serie);
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error en búsqueda por texto", ex);
            Mensaje.error(null, "Error de Búsqueda",
                    "No se pudo realizar la búsqueda.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstSeries;
    }

    /**
     * Inserta una nueva serie
     *
     * @param serie Serie a insertar
     * @return true si se insertó correctamente, false en caso contrario
     */
    public boolean insertar(Arfacc serie) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO FACTU.ARFACC ");
        sql.append("(NO_CIA, CENTRO, TIPO_DOC, SERIE, CONS_DESDE, ");
        sql.append("LINEAS, IND_CONTROL_AUTO, ACTIVO, NO_CABA) ");
        sql.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, serie.getNoCia());
            ps.setString(2, serie.getCentro());
            ps.setString(3, serie.getTipoDoc());
            ps.setString(4, serie.getSerie());
            ps.setInt(5, serie.getConsDesde());
            ps.setInt(6, serie.getLineas());
            ps.setString(7, serie.getIndControlAuto());
            ps.setString(8, serie.getActivo());
            ps.setString(9, serie.getNoCaba());

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Serie insertada: " + serie.getSerie());
                Mensaje.alerta (null, "Registro Exitoso",
                        "La serie se registró correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al insertar serie", ex);
            Mensaje.error(null, "Error al Insertar",
                    "No se pudo registrar la serie.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Actualiza una serie existente
     *
     * @param serie Serie con datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Arfacc serie) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE FACTU.ARFACC SET ");
        sql.append("CONS_DESDE = ?, ");
        sql.append("LINEAS = ?, ");
        sql.append("IND_CONTROL_AUTO = ?, ");
        sql.append("ACTIVO = ?, ");
        sql.append("NO_CABA = ? ");
        sql.append("WHERE NO_CIA = ? AND CENTRO = ? AND TIPO_DOC = ? AND SERIE = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setInt(1, serie.getConsDesde());
            ps.setInt(2, serie.getLineas());
            ps.setString(3, serie.getIndControlAuto());
            ps.setString(4, serie.getActivo());
            ps.setString(5, serie.getNoCaba());
            ps.setString(6, serie.getNoCia());
            ps.setString(7, serie.getCentro());
            ps.setString(8, serie.getTipoDoc());
            ps.setString(9, serie.getSerie());

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Serie actualizada: " + serie.getSerie());
                Mensaje.alerta(null, "Actualización Exitosa",
                        "La serie se actualizó correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar serie", ex);
            Mensaje.error(null, "Error al Actualizar",
                    "No se pudo actualizar la serie.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Elimina una serie
     *
     * @param noCia Número de compañía
     * @param centro Centro de costo
     * @param tipoDoc Tipo de documento
     * @param serie Número de serie a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(String noCia, String centro, String tipoDoc, String serie) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? AND CENTRO = ? AND TIPO_DOC = ? AND SERIE = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, centro);
            ps.setString(3, tipoDoc);
            ps.setString(4, serie);

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Serie eliminada: " + serie);
                Mensaje.alerta(null, "Eliminación Exitosa",
                        "La serie se eliminó correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al eliminar serie", ex);
            Mensaje.error(null, "Error al Eliminar",
                    "No se pudo eliminar la serie.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Verifica si existe una serie
     *
     * @param noCia Número de compañía
     * @param centro Centro de costo
     * @param tipoDoc Tipo de documento
     * @param serie Número de serie
     * @return true si existe, false en caso contrario
     */
    public boolean existe(String noCia, String centro, String tipoDoc, String serie) {
        boolean existe = false;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) as total ");
        sql.append("FROM FACTU.ARFACC ");
        sql.append("WHERE NO_CIA = ? AND CENTRO = ? AND TIPO_DOC = ? AND SERIE = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, centro);
            ps.setString(3, tipoDoc);
            ps.setString(4, serie);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                existe = rs.getInt("total") > 0;
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al verificar existencia", ex);
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return existe;
    }
}