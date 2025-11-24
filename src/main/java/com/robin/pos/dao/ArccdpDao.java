package com.robin.pos.dao;

import com.robin.pos.model.Arccdp;
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

public class ArccdpDao {

    public List<Arccdp> listarDepartamentos(String noCia) {
        List<Arccdp> lstDepartamentos = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CODI_DEPA AS CODDEPA , DESC_DEPA AS DESDEPA ");
        sql.append("FROM CXC.ARCCDP ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("ORDER BY CODI_DEPA");

        Connection cx = null;

        try {

            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Arccdp departamento = new Arccdp();
                departamento.setCodDepa(rs.getString("CODDEPA"));
                departamento.setDesDepa(rs.getString("DESDEPA"));
                lstDepartamentos.add(departamento);
            }
            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Lista clientes","Error en la consulta de cliente.");
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }
        return lstDepartamentos;
    }

}
