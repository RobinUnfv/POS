package com.robin.pos.util;

import oracle.jdbc.OracleDriver;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConexionBD {
    // DESKTOP-UAQQ6TN --> PC ROBIN
    // Mat
    private static String host = "Mat";
    private static String puerto = "1521";
    private static String sid = "BDNX1";
    private static String usuario = "LLE";
    private static String password = "YVL";


    public static Connection oracle() {
        Connection conexion = null;
        try {
            DriverManager.registerDriver(new OracleDriver());
            conexion = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":" + puerto + ":" + sid, usuario, password);
        } catch (SQLException var3) {

            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, var3);
            Mensaje.error(null, "Conexión","Error en la conexión de oracle");
        }
        return conexion;
    }

    public static void cerrarCxOracle(Connection conexion) {
        if (conexion != null) {
            try {
                if(!conexion.isClosed()){
                    conexion.close();
                }
            } catch (SQLException e) {

                Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE,
                        "Error al cerrar la conexión", e);
                Mensaje.error(null, "Cerrar","Error al cerrar la conexión de oracle");
            }
        }
    }

}
