package com.robin.pos.dao;

import com.robin.pos.model.EntidadTributaria;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArcctdaDao {
    public static int registrar(EntidadTributaria entidad) {
        int resultado = 0;
        String query = "INSERT INTO CXC.ARCCTDA(NO_CIA,NO_CLIENTE,COD_TIENDA,NOMBRE,DIRECCION,CODI_DEPA,CODI_PROV," +
                "CODI_DIST,TIPO_DIR,ACTIVO,COD_SUC,ESTAB_SUNAT) VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

        String depar="";
        String prov="";
        String dist="";
        String ubigeo="";
        String direccion="";

        // NATURAL: depa= '15', prov='01', dist='01'
        if(entidad.getTipoDocumento().equals("1")) {
            depar = "15";
            prov = "01";
            dist = "01";
            ubigeo = depar + prov + dist;
            direccion = "******";
        } else if (entidad.getTipoDocumento().equals("6")) {
            direccion = entidad.getDireccion().trim();
            ubigeo = entidad.getUbigeo().trim();
            if (!ubigeo.equals("-")) {
                depar = ubigeo.substring(0, 2);
                prov = ubigeo.substring(2, 4);
                dist = ubigeo.substring(4, 6);
            }
            if (direccion.length() > 150) {
                direccion = direccion.substring(0, 150).trim();
            }
        }

        try {
            Connection cx = ConexionBD.oracle();
            PreparedStatement stmt2 = cx.prepareStatement(query);
            stmt2.setString(1, "01");
            stmt2.setString(2, entidad.getNumeroDocumento());
            stmt2.setString(3, "001");
            stmt2.setString(4, "LEGAL");
            stmt2.setString(5, direccion);
            stmt2.setString(6, depar);
            stmt2.setString(7, prov);
            stmt2.setString(8, dist);
            stmt2.setString(9, "LEG");
            stmt2.setString(10, "S");
            stmt2.setString(11, "001");
            stmt2.setString(12, "0000");

            resultado = stmt2.executeUpdate();

            stmt2.close();
            ConexionBD.cerrarCxOracle(cx);
        }catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Registrar ARCCTDA","Error cuando se intenta registra.");
        }
        return resultado;
    }

}
