package com.robin.pos.dao;

import com.robin.pos.model.Cliente;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClienteDao {

    public String guardarCliente() {
        return null;
    }

    public List<Cliente> listarClientes() {
        return null;
    }

    public Cliente buscarPorNumId(String noCia, String noCliente) {

        Cliente cliente = null;
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT A.NO_CLIENTE, A.NOMBRE, A.TELEFONO1, A.RUC, A.TIPO_CLIENTE, A.TIPO_PERSONA, A.NU_DOCUMENTO, A.TIPO_DOCUMENTO, A.EMAIL, ");
        sql.append("D.DIRECCION, D.CODI_DEPA, D.CODI_PROV, D.CODI_DIST, D.ESTAB_SUNAT ");
        sql.append("FROM CXC.ARCCMC A, CXC.ARCCTDA D ");
        sql.append("WHERE A.ACTIVO = ? ");
        sql.append("AND D.ACTIVO = A.ACTIVO ");
        sql.append("AND D.COD_TIENDA = ? ");
        sql.append("AND A.NO_CIA = ? ");
        sql.append("AND D.NO_CIA = A.NO_CIA ");
        sql.append("AND A.NO_CLIENTE = ? ");
        sql.append("AND D.NO_CLIENTE = A.NO_CLIENTE");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, "S");
            ps.setString(2, "001");
            ps.setString(3, noCia);
            ps.setString(4, noCliente);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                cliente = new Cliente();
                cliente.setNoCliente(rs.getString("NO_CLIENTE"));
                cliente.setNombre(rs.getString("NOMBRE"));
                cliente.setTelefono(rs.getString("TELEFONO1"));
                cliente.setRuc(rs.getString("RUC"));
                cliente.setTipoCliente(rs.getString("TIPO_CLIENTE"));
                cliente.setTipoPersona(rs.getString("TIPO_PERSONA"));
                cliente.setNuDocumento(rs.getString("NU_DOCUMENTO"));
                cliente.setTipoDocumento(rs.getString("TIPO_DOCUMENTO"));
                cliente.setEmail(rs.getString("EMAIL"));
                cliente.setDireccion(rs.getString("DIRECCION"));
                cliente.setCodiDepa(rs.getString("CODI_DEPA"));
                cliente.setCodiProv(rs.getString("CODI_PROV"));
                cliente.setCodiDist(rs.getString("CODI_DIST"));
                cliente.setEstabSunat(rs.getString("ESTAB_SUNAT"));
            }

        } catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Consultar numero documento","Error en la consulta de cliente.");
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }
        return cliente;
    }

}
