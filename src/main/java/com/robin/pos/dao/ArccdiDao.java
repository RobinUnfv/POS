package com.robin.pos.dao;

import com.robin.pos.model.Arccdi;
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

public class ArccdiDao {

    public List<Arccdi> listaDistrito(String noCia, String codiDepa, String codiProv) {
        List<Arccdi> lstDistritos = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CODI_DIST AS codiDist, DESC_DIST AS descDist ");
        sql.append("FROM CXC.ARCCDI ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("AND CODI_DEPA = ? ");
        sql.append("AND CODI_PROV = ? ");
        sql.append("ORDER BY DESC_DIST");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codiDepa);
            ps.setString(3, codiProv);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Arccdi distrito = new Arccdi(rs.getString("codiDist"),
                                             rs.getString("descDist"));
                lstDistritos.add(distrito);
            }
            ConexionBD.cerrarCxOracle(cx);
        }catch ( SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Lista Distritos","Error cuando se consulta distritos.");
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstDistritos;
    }
}
