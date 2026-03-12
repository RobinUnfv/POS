package com.robin.pos.dao;

import com.robin.pos.model.ConfigSunat;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para gestión de configuración SUNAT
 * Tabla: FACTU.CONFIG_SUNAT
 *
 * @author Robin POS
 * @version 1.0
 */
public class ConfigSunatDao {

    private static final Logger LOGGER = Logger.getLogger(ConfigSunatDao.class.getName());
    private static final String TABLA = "FACTU.CONFIG_SUNAT";

    /**
     * Lista todas las configuraciones de una compañía
     */
    public List<ConfigSunat> listarPorCompania(String noCia) {
        List<ConfigSunat> lista = new ArrayList<>();

        String sql = "SELECT NO_CIA, CODIGO, VALOR, DESCRIPCION, GRUPO, TIPO_DATO, ACTIVO " +
                "FROM " + TABLA + " " +
                "WHERE NO_CIA = ? " +
                "ORDER BY GRUPO, CODIGO";

        Connection cx = null;

        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ConfigSunat config = new ConfigSunat();
                    config.setNoCia(rs.getString("NO_CIA"));
                    config.setCodigo(rs.getString("CODIGO"));
                    config.setValor(rs.getString("VALOR"));
                    config.setDescripcion(rs.getString("DESCRIPCION"));
                    config.setGrupo(rs.getString("GRUPO"));
                    config.setTipoDato(rs.getString("TIPO_DATO"));
                    config.setActivo(rs.getString("ACTIVO"));

                    lista.add(config);
                }
            }

            LOGGER.info("Se listaron " + lista.size() + " configuraciones para NO_CIA: " + noCia);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar configuraciones SUNAT", e);
            Mensaje.error(null, "Error de Base de Datos",
                    "No se pudo listar las configuraciones:\n" + e.getMessage());
        }

        return lista;
    }

    /**
     * Lista configuraciones por grupo
     */
    public List<ConfigSunat> listarPorGrupo(String noCia, String grupo) {
        List<ConfigSunat> lista = new ArrayList<>();

        String sql = "SELECT NO_CIA, CODIGO, VALOR, DESCRIPCION, GRUPO, TIPO_DATO, ACTIVO " +
                "FROM " + TABLA + " " +
                "WHERE NO_CIA = ? AND GRUPO = ? " +
                "ORDER BY CODIGO";
        Connection cx = null;
        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, grupo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ConfigSunat config = new ConfigSunat();
                    config.setNoCia(rs.getString("NO_CIA"));
                    config.setCodigo(rs.getString("CODIGO"));
                    config.setValor(rs.getString("VALOR"));
                    config.setDescripcion(rs.getString("DESCRIPCION"));
                    config.setGrupo(rs.getString("GRUPO"));
                    config.setTipoDato(rs.getString("TIPO_DATO"));
                    config.setActivo(rs.getString("ACTIVO"));

                    lista.add(config);
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar configuraciones por grupo", e);
        }

        return lista;
    }

    /**
     * Busca una configuración específica
     */
    public ConfigSunat buscarPorCodigo(String noCia, String codigo) {
        ConfigSunat config = null;

        String sql = "SELECT NO_CIA, CODIGO, VALOR, DESCRIPCION, GRUPO, TIPO_DATO, ACTIVO " +
                "FROM " + TABLA + " " +
                "WHERE NO_CIA = ? AND CODIGO = ?";
        Connection cx = null;
        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());

            ps.setString(1, noCia);
            ps.setString(2, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    config = new ConfigSunat();
                    config.setNoCia(rs.getString("NO_CIA"));
                    config.setCodigo(rs.getString("CODIGO"));
                    config.setValor(rs.getString("VALOR"));
                    config.setDescripcion(rs.getString("DESCRIPCION"));
                    config.setGrupo(rs.getString("GRUPO"));
                    config.setTipoDato(rs.getString("TIPO_DATO"));
                    config.setActivo(rs.getString("ACTIVO"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al buscar configuración", e);
        }

        return config;
    }

    /**
     * Obtiene el valor de una configuración
     */
    public String obtenerValor(String noCia, String codigo) {
        String valor = null;

        String sql = "SELECT VALOR FROM " + TABLA + " " +
                "WHERE NO_CIA = ? AND CODIGO = ? AND ACTIVO = 'S'";
        Connection cx = null;
        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    valor = rs.getString("VALOR");
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al obtener valor de configuración", e);
        }

        return valor;
    }

    /**
     * Inserta una nueva configuración
     */
    public boolean insertar(ConfigSunat config) {
        String sql = "INSERT INTO " + TABLA + " " +
                "(NO_CIA, CODIGO, VALOR, DESCRIPCION, GRUPO, TIPO_DATO, ACTIVO) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
            Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, config.getNoCia());
            ps.setString(2, config.getCodigo());
            ps.setString(3, config.getValor());
            ps.setString(4, config.getDescripcion());
            ps.setString(5, config.getGrupo());
            ps.setString(6, config.getTipoDato());
            ps.setString(7, config.getActivo());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                LOGGER.info("Configuración insertada: " + config.getCodigo());
                Mensaje.alerta(null, "Registro Exitoso",
                        "La configuración se registró correctamente.");
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al insertar configuración", e);

            if (e.getErrorCode() == 1) { // ORA-00001: unique constraint violated
                Mensaje.error(null, "Código Duplicado",
                        "Ya existe una configuración con el código: " + config.getCodigo());
            } else {
                Mensaje.error(null, "Error de Base de Datos",
                        "No se pudo insertar la configuración:\n" + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Actualiza una configuración existente
     */
    public boolean actualizar(ConfigSunat config) {
        String sql = "UPDATE " + TABLA + " SET " +
                "VALOR = ?, " +
                "DESCRIPCION = ?, " +
                "GRUPO = ?, " +
                "TIPO_DATO = ?, " +
                "ACTIVO = ? " +
                "WHERE NO_CIA = ? AND CODIGO = ?";
        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, config.getValor());
            ps.setString(2, config.getDescripcion());
            ps.setString(3, config.getGrupo());
            ps.setString(4, config.getTipoDato());
            ps.setString(5, config.getActivo());
            ps.setString(6, config.getNoCia());
            ps.setString(7, config.getCodigo());

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                LOGGER.info("Configuración actualizada: " + config.getCodigo());
                Mensaje.alerta(null, "Actualización Exitosa",
                        "La configuración se actualizó correctamente.");
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al actualizar configuración", e);
            Mensaje.error(null, "Error de Base de Datos",
                    "No se pudo actualizar la configuración:\n" + e.getMessage());
        }

        return false;
    }

    /**
     * Elimina una configuración
     */
    public boolean eliminar(String noCia, String codigo) {
        String sql = "DELETE FROM " + TABLA + " WHERE NO_CIA = ? AND CODIGO = ?";
        Connection cx = null;
        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codigo);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                LOGGER.info("Configuración eliminada: " + codigo);
                Mensaje.alerta(null, "Eliminación Exitosa",
                        "La configuración se eliminó correctamente.");
                return true;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al eliminar configuración", e);

            if (e.getErrorCode() == 2292) { // ORA-02292: integrity constraint violated
                Mensaje.error(null, "No se puede Eliminar",
                        "La configuración no puede eliminarse porque está siendo utilizada.");
            } else {
                Mensaje.error(null, "Error de Base de Datos",
                        "No se pudo eliminar la configuración:\n" + e.getMessage());
            }
        }

        return false;
    }

    /**
     * Obtiene la lista de grupos únicos
     */
    public List<String> listarGrupos(String noCia) {
        List<String> grupos = new ArrayList<>();

        String sql = "SELECT DISTINCT GRUPO FROM " + TABLA + " " +
                "WHERE NO_CIA = ? AND GRUPO IS NOT NULL " +
                "ORDER BY GRUPO";
        Connection cx = null;
        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    grupos.add(rs.getString("GRUPO"));
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al listar grupos", e);
        }

        return grupos;
    }

    /**
     * Verifica si existe un código
     */
    public boolean existeCodigo(String noCia, String codigo) {
        String sql = "SELECT COUNT(*) FROM " + TABLA + " WHERE NO_CIA = ? AND CODIGO = ?";
        Connection cx = null;
        try  {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al verificar código", e);
        }

        return false;
    }
}
