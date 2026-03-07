package com.robin.pos.dao;

import com.robin.pos.model.Arfadoc;
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
 * DAO para la tabla FACTU.ARFADOC
 * Gestión de tipos de documentos del sistema
 *
 * @author Robin POS
 * @version 1.0
 */
public class ArfadocDao {

    private static final Logger LOGGER = Logger.getLogger(ArfadocDao.class.getName());

    /**
     * Lista todos los documentos de una compañía
     *
     * @param noCia Número de compañía
     * @return Lista de documentos
     */
    public List<Arfadoc> listarDocumentos(String noCia) {
        List<Arfadoc> lstDocumentos = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_DOC, DESCRIPCION, TIPO, ESTADO, COD_SUNAT ");
        sql.append("FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("ORDER BY DESCRIPCION ASC");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfadoc documento = new Arfadoc();
                documento.setNoCia(rs.getString("NO_CIA"));
                documento.setCodDoc(rs.getString("COD_DOC"));
                documento.setDescripcion(rs.getString("DESCRIPCION"));
                documento.setTipo(rs.getString("TIPO"));
                documento.setEstado(rs.getString("ESTADO"));
                documento.setCodSunat(rs.getString("COD_SUNAT"));

                lstDocumentos.add(documento);
            }

            ConexionBD.cerrarCxOracle(cx);
            LOGGER.info("Se encontraron " + lstDocumentos.size() + " documentos");

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar documentos", ex);
            Mensaje.error(null, "Error al Listar Documentos",
                    "No se pudo obtener la lista de documentos.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstDocumentos;
    }

    /**
     * Busca un documento por su código
     *
     * @param noCia Número de compañía
     * @param codDoc Código del documento
     * @return Documento encontrado o null
     */
    public Arfadoc buscarPorCodigo(String noCia, String codDoc) {
        Arfadoc documento = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_DOC, DESCRIPCION, TIPO, ESTADO, COD_SUNAT ");
        sql.append("FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? AND COD_DOC = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codDoc);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                documento = new Arfadoc();
                documento.setNoCia(rs.getString("NO_CIA"));
                documento.setCodDoc(rs.getString("COD_DOC"));
                documento.setDescripcion(rs.getString("DESCRIPCION"));
                documento.setTipo(rs.getString("TIPO"));
                documento.setEstado(rs.getString("ESTADO"));
                documento.setCodSunat(rs.getString("COD_SUNAT"));
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar documento", ex);
            Mensaje.error(null, "Error de Búsqueda",
                    "No se pudo buscar el documento.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return documento;
    }

    /**
     * Filtra documentos por tipo
     *
     * @param noCia Número de compañía
     * @param tipo Tipo de documento
     * @return Lista de documentos filtrados
     */
    public List<Arfadoc> filtrarPorTipo(String noCia, String tipo) {
        List<Arfadoc> lstDocumentos = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_DOC, DESCRIPCION, TIPO, ESTADO, COD_SUNAT ");
        sql.append("FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? AND TIPO = ? ");
        sql.append("ORDER BY DESCRIPCION ASC");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, tipo);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfadoc documento = new Arfadoc();
                documento.setNoCia(rs.getString("NO_CIA"));
                documento.setCodDoc(rs.getString("COD_DOC"));
                documento.setDescripcion(rs.getString("DESCRIPCION"));
                documento.setTipo(rs.getString("TIPO"));
                documento.setEstado(rs.getString("ESTADO"));
                documento.setCodSunat(rs.getString("COD_SUNAT"));

                lstDocumentos.add(documento);
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al filtrar por tipo", ex);
            Mensaje.error(null, "Error de Filtro",
                    "No se pudo filtrar por tipo.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstDocumentos;
    }

    /**
     * Filtra documentos por estado
     *
     * @param noCia Número de compañía
     * @param estado Estado del documento (A: Activo, I: Inactivo)
     * @return Lista de documentos filtrados
     */
    public List<Arfadoc> filtrarPorEstado(String noCia, String estado) {
        List<Arfadoc> lstDocumentos = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_DOC, DESCRIPCION, TIPO, ESTADO, COD_SUNAT ");
        sql.append("FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? AND ESTADO = ? ");
        sql.append("ORDER BY DESCRIPCION ASC");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, estado);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arfadoc documento = new Arfadoc();
                documento.setNoCia(rs.getString("NO_CIA"));
                documento.setCodDoc(rs.getString("COD_DOC"));
                documento.setDescripcion(rs.getString("DESCRIPCION"));
                documento.setTipo(rs.getString("TIPO"));
                documento.setEstado(rs.getString("ESTADO"));
                documento.setCodSunat(rs.getString("COD_SUNAT"));

                lstDocumentos.add(documento);
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

        return lstDocumentos;
    }

    /**
     * Busca documentos por texto (código, descripción o código SUNAT)
     *
     * @param noCia Número de compañía
     * @param texto Texto a buscar
     * @return Lista de documentos que coinciden
     */
    public List<Arfadoc> buscarPorTexto(String noCia, String texto) {
        List<Arfadoc> lstDocumentos = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_DOC, DESCRIPCION, TIPO, ESTADO, COD_SUNAT ");
        sql.append("FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("AND (UPPER(COD_DOC) LIKE ? ");
        sql.append("OR UPPER(DESCRIPCION) LIKE ? ");
        sql.append("OR UPPER(COD_SUNAT) LIKE ?) ");
        sql.append("ORDER BY DESCRIPCION ASC");

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
                Arfadoc documento = new Arfadoc();
                documento.setNoCia(rs.getString("NO_CIA"));
                documento.setCodDoc(rs.getString("COD_DOC"));
                documento.setDescripcion(rs.getString("DESCRIPCION"));
                documento.setTipo(rs.getString("TIPO"));
                documento.setEstado(rs.getString("ESTADO"));
                documento.setCodSunat(rs.getString("COD_SUNAT"));

                lstDocumentos.add(documento);
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

        return lstDocumentos;
    }

    /**
     * Inserta un nuevo documento
     *
     * @param documento Documento a insertar
     * @return true si se insertó correctamente, false en caso contrario
     */
    public boolean insertar(Arfadoc documento) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO FACTU.ARFADOC ");
        sql.append("(NO_CIA, COD_DOC, DESCRIPCION, TIPO, ESTADO, COD_SUNAT) ");
        sql.append("VALUES (?, ?, ?, ?, ?, ?)");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, documento.getNoCia());
            ps.setString(2, documento.getCodDoc());
            ps.setString(3, documento.getDescripcion());
            ps.setString(4, documento.getTipo());
            ps.setString(5, documento.getEstado());
            ps.setString(6, documento.getCodSunat());

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Documento insertado: " + documento.getCodDoc());
                Mensaje.alerta (null, "Registro Exitoso",
                        "El documento se registró correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al insertar documento", ex);
            Mensaje.error(null, "Error al Insertar",
                    "No se pudo registrar el documento.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Actualiza un documento existente
     *
     * @param documento Documento con datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizar(Arfadoc documento) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE FACTU.ARFADOC SET ");
        sql.append("DESCRIPCION = ?, ");
        sql.append("TIPO = ?, ");
        sql.append("ESTADO = ?, ");
        sql.append("COD_SUNAT = ? ");
        sql.append("WHERE NO_CIA = ? AND COD_DOC = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, documento.getDescripcion());
            ps.setString(2, documento.getTipo());
            ps.setString(3, documento.getEstado());
            ps.setString(4, documento.getCodSunat());
            ps.setString(5, documento.getNoCia());
            ps.setString(6, documento.getCodDoc());

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Documento actualizado: " + documento.getCodDoc());
                Mensaje.alerta(null, "Actualización Exitosa",
                        "El documento se actualizó correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar documento", ex);
            Mensaje.error(null, "Error al Actualizar",
                    "No se pudo actualizar el documento.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Elimina un documento
     *
     * @param noCia Número de compañía
     * @param codDoc Código del documento a eliminar
     * @return true si se eliminó correctamente, false en caso contrario
     */
    public boolean eliminar(String noCia, String codDoc) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? AND COD_DOC = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codDoc);

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Documento eliminado: " + codDoc);
                Mensaje.alerta(null, "Eliminación Exitosa",
                        "El documento se eliminó correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al eliminar documento", ex);
            Mensaje.error(null, "Error al Eliminar",
                    "No se pudo eliminar el documento.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Verifica si existe un documento con el código especificado
     *
     * @param noCia Número de compañía
     * @param codDoc Código del documento
     * @return true si existe, false en caso contrario
     */
    public boolean existe(String noCia, String codDoc) {
        boolean existe = false;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) as total ");
        sql.append("FROM FACTU.ARFADOC ");
        sql.append("WHERE NO_CIA = ? AND COD_DOC = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codDoc);
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