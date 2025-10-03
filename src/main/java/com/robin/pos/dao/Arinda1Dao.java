package com.robin.pos.dao;

import com.robin.pos.model.Arinda1;
import com.robin.pos.model.Cliente;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Arinda1Dao {

    public ObservableList<Arinda1> buscarProducto(String noCia, String descripcion) {
        ObservableList<Arinda1> listArinda1 = FXCollections.observableArrayList();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_ARTI, DESCRIPCION ");
        sql.append("FROM INVE.ARINDA1 ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("AND DESCRIPCION LIKE UPPER(?) ");
        sql.append("ORDER BY DESCRIPCION");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, descripcion);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Arinda1 arinda1 = new Arinda1(rs.getString("NO_ARTI"),
                        rs.getString("DESCRIPCION"));
                listArinda1.add(arinda1);
            }
            ConexionBD.cerrarCxOracle(cx);
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Consultar numero documento","Error en la consulta de cliente.");
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }
        return listArinda1;
    }

}
