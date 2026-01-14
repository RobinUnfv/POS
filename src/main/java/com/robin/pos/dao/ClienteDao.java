package com.robin.pos.dao;

import com.robin.pos.model.Cliente;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para operaciones con clientes
 * Tablas: CXC.ARCCMC (Cabecera), CXC.ARCCTDA (Dirección)
 *
 * @author Robin POS
 * @version 2.0
 */
public class ClienteDao {

    private static final Logger LOGGER = Logger.getLogger(ClienteDao.class.getName());

    /**
     * Lista todos los clientes de una compañía
     *
     * @param noCia Código de compañía
     * @return Lista de clientes
     */
    public List<Cliente> listarTodos(String noCia) {
        List<Cliente> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOCUMENTO, NO_CLIENTE, NOMBRE,
                   TIPO_PERSONA, ACTIVO, TELEFONO1, EMAIL,
                   RUC, NU_DOCUMENTO, EXTRANJERO
            FROM CXC.ARCCMC
            WHERE NO_CIA = ?
            ORDER BY NOMBRE
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setNoCia(rs.getString("NO_CIA"));
                cliente.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
                cliente.setNoCliente(rs.getString("NO_CLIENTE"));
                cliente.setNombre(rs.getString("NOMBRE"));
                cliente.setTipoPersona(rs.getString("TIPO_PERSONA"));
                cliente.setActivo(rs.getString("ACTIVO"));
                cliente.setTelefono(rs.getString("TELEFONO1"));
                cliente.setEmail(rs.getString("EMAIL"));
                cliente.setRuc(rs.getString("RUC"));
                cliente.setNuDocumento(rs.getString("NU_DOCUMENTO"));
                cliente.setExtranjero(rs.getString("EXTRANJERO"));
                lista.add(cliente);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar todos los clientes", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Lista clientes activos
     *
     * @param noCia Código de compañía
     * @return Lista de clientes activos
     */
    public List<Cliente> listarActivos(String noCia) {
        List<Cliente> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOCUMENTO, NO_CLIENTE, NOMBRE,
                   TIPO_PERSONA, ACTIVO, TELEFONO1, EMAIL,
                   RUC, NU_DOCUMENTO, EXTRANJERO
            FROM CXC.ARCCMC
            WHERE NO_CIA = ?
            AND ACTIVO = 'S'
            ORDER BY NOMBRE
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setNoCia(rs.getString("NO_CIA"));
                cliente.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
                cliente.setNoCliente(rs.getString("NO_CLIENTE"));
                cliente.setNombre(rs.getString("NOMBRE"));
                cliente.setTipoPersona(rs.getString("TIPO_PERSONA"));
                cliente.setActivo(rs.getString("ACTIVO"));
                cliente.setTelefono(rs.getString("TELEFONO1"));
                cliente.setEmail(rs.getString("EMAIL"));
                cliente.setRuc(rs.getString("RUC"));
                cliente.setNuDocumento(rs.getString("NU_DOCUMENTO"));
                cliente.setExtranjero(rs.getString("EXTRANJERO"));
                lista.add(cliente);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar clientes activos", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Busca un cliente por su número de identificación con datos completos
     *
     * @param noCia Código de compañía
     * @param noCliente Número de cliente
     * @return Cliente o null si no existe
     */
    public Cliente buscarPorNumId(String noCia, String noCliente) {
        Cliente cliente = null;

        String sql = """
            SELECT A.NO_CIA, A.NO_CLIENTE, A.NOMBRE, A.TELEFONO1, A.TELEFONO2,
                   A.RUC, A.TIPO_CLIENTE, A.TIPO_PERSONA, A.NU_DOCUMENTO, 
                   A.TIPO_DOCUMENTO, A.EMAIL, A.ACTIVO, A.EXTRANJERO,
                   D.DIRECCION, D.CODI_DEPA, D.CODI_PROV, D.CODI_DIST, D.ESTAB_SUNAT
            FROM CXC.ARCCMC A
            LEFT JOIN CXC.ARCCTDA D ON D.NO_CIA = A.NO_CIA 
                                    AND D.NO_CLIENTE = A.NO_CLIENTE 
                                    AND D.COD_TIENDA = '001'
                                    AND D.ACTIVO = 'S'
            WHERE A.NO_CIA = ?
            AND A.NO_CLIENTE = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, noCliente);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                cliente = new Cliente();
                cliente.setNoCia(rs.getString("NO_CIA"));
                cliente.setNoCliente(rs.getString("NO_CLIENTE"));
                cliente.setNombre(rs.getString("NOMBRE"));
                cliente.setTelefono(rs.getString("TELEFONO1"));
                cliente.setTelefono2(rs.getString("TELEFONO2"));
                cliente.setRuc(rs.getString("RUC"));
                cliente.setTipoCliente(rs.getString("TIPO_CLIENTE"));
                cliente.setTipoPersona(rs.getString("TIPO_PERSONA"));
                cliente.setNuDocumento(rs.getString("NU_DOCUMENTO"));
                cliente.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
                cliente.setEmail(rs.getString("EMAIL"));
                cliente.setActivo(rs.getString("ACTIVO"));
                cliente.setExtranjero(rs.getString("EXTRANJERO"));
                cliente.setDireccion(rs.getString("DIRECCION"));
                cliente.setCodiDepa(rs.getString("CODI_DEPA"));
                cliente.setCodiProv(rs.getString("CODI_PROV"));
                cliente.setCodiDist(rs.getString("CODI_DIST"));
                cliente.setEstabSunat(rs.getString("ESTAB_SUNAT"));
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar cliente por número: " + noCliente, ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cliente;
    }

    /**
     * Busca clientes por nombre (búsqueda parcial)
     *
     * @param noCia Código de compañía
     * @param nombre Nombre a buscar
     * @return Lista de clientes que coinciden
     */
    public List<Cliente> buscarPorNombre(String noCia, String nombre) {
        List<Cliente> lista = new ArrayList<>();

        String sql = """
            SELECT NO_CIA, TIPO_DOCUMENTO, NO_CLIENTE, NOMBRE,
                   TIPO_PERSONA, ACTIVO, TELEFONO1, EMAIL,
                   RUC, NU_DOCUMENTO, EXTRANJERO
            FROM CXC.ARCCMC
            WHERE NO_CIA = ?
            AND UPPER(NOMBRE) LIKE UPPER(?)
            AND ROWNUM <= 50
            ORDER BY NOMBRE
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, "%" + nombre + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setNoCia(rs.getString("NO_CIA"));
                cliente.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
                cliente.setNoCliente(rs.getString("NO_CLIENTE"));
                cliente.setNombre(rs.getString("NOMBRE"));
                cliente.setTipoPersona(rs.getString("TIPO_PERSONA"));
                cliente.setActivo(rs.getString("ACTIVO"));
                cliente.setTelefono(rs.getString("TELEFONO1"));
                cliente.setEmail(rs.getString("EMAIL"));
                cliente.setRuc(rs.getString("RUC"));
                cliente.setNuDocumento(rs.getString("NU_DOCUMENTO"));
                cliente.setExtranjero(rs.getString("EXTRANJERO"));
                lista.add(cliente);
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar clientes por nombre", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lista;
    }

    /**
     * Guarda un cliente (insert o update) usando el procedimiento almacenado
     * Llama a CXC.PR_CLIENTE.GUARDAR_CLIENTE
     *
     * @param cliente Objeto Cliente con los datos
     * @return true si se guardó correctamente, false en caso contrario
     */
    public boolean guardarCliente(Cliente cliente) throws SQLException {
        Connection cx = null;
        CallableStatement cs = null;

        try {
            cx = ConexionBD.oracle();

            String sql = "{CALL CXC.PR_CLIENTE.GUARDAR_CLIENTE(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            cs = cx.prepareCall(sql);

            // Parámetros del procedimiento
            cs.setString(1, cliente.getNoCia());                // p_cNoCia
            cs.setString(2, cliente.getNoCliente());            // p_cNoCliente
            cs.setString(3, cliente.getNombre());               // p_cNombre
            cs.setString(4, cliente.getTelefono());             // p_cCelular
            cs.setString(5, cliente.getTelefono2());            // p_cTelefono
            cs.setString(6, cliente.getTipoPersona());          // p_cTipoPersona
            cs.setString(7, cliente.getExtranjero());           // p_cExtranjero
            cs.setString(8, cliente.getActivo());               // p_cActivo
            cs.setString(9, cliente.getTipoDocumento());        // p_cTipoDocumento
            cs.setString(10, cliente.getEmail());               // p_cEmail
            cs.setString(11, cliente.getDireccion());           // p_cDireccion
            cs.setString(12, cliente.getCodiDepa());            // p_cCodiDepa
            cs.setString(13, cliente.getCodiProv());            // p_cCodiProv
            cs.setString(14, cliente.getCodiDist());            // p_cCodiDist

            cs.execute();

            LOGGER.info("Cliente guardado exitosamente: " + cliente.getNoCliente());
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al guardar cliente", ex);
            throw ex;
        } finally {
            if (cs != null) {
                try { cs.close(); } catch (SQLException ignored) {}
            }
            ConexionBD.cerrarCxOracle(cx);
        }
    }

    /**
     * Valida si un cliente ya existe
     *
     * @param noCia Código de compañía
     * @param noCliente Número de cliente
     * @return "S" si existe, "N" si no existe
     */
    public String validarClienteExistente(String noCia, String noCliente) {
        String resultado = "N";

        String sql = """
            SELECT COUNT(*) AS CANTIDAD
            FROM CXC.ARCCMC
            WHERE NO_CIA = ?
            AND NO_CLIENTE = ?
            """;

        Connection cx = null;
        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql);
            ps.setString(1, noCia);
            ps.setString(2, noCliente);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cantidad = rs.getInt("CANTIDAD");
                resultado = cantidad > 0 ? "S" : "N";
            }

            rs.close();
            ps.close();
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al validar cliente existente", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }

    /**
     * Cuenta el total de clientes
     */
    public int contarClientes(String noCia) {
        int cantidad = 0;

        String sql = "SELECT COUNT(*) AS CANTIDAD FROM CXC.ARCCMC WHERE NO_CIA = ?";

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
            LOGGER.log(Level.SEVERE, "Error al contar clientes", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cantidad;
    }

    /**
     * Cuenta clientes activos
     */
    public int contarActivos(String noCia) {
        int cantidad = 0;

        String sql = "SELECT COUNT(*) AS CANTIDAD FROM CXC.ARCCMC WHERE NO_CIA = ? AND ACTIVO = 'S'";

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
            LOGGER.log(Level.SEVERE, "Error al contar clientes activos", ex);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return cantidad;
    }
}