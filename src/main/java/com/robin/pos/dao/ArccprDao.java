package com.robin.pos.dao;

import com.robin.pos.model.Arccdp;
import com.robin.pos.model.Arccpr;
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

public class ArccprDao {

    public List<Arccpr> listaProvincias(String noCia, String codiDepa) {
        List<Arccpr> lstProvincias = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CODI_PROV as codiProv, DESC_PROV as descProv ");
        sql.append("FROM CXC.ARCCPR ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("AND CODI_DEPA = ? ");
        sql.append("ORDER BY DESC_PROV");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codiDepa);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Arccpr departamento = new Arccpr(rs.getString("codiProv"),
                                                 rs.getString("descProv"));

                lstProvincias.add(departamento);
            }
            ConexionBD.cerrarCxOracle(cx);
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Lista Provincias","Error cuando se consulta provincias.");
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }
        return lstProvincias;
    }
}
