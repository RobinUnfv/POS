package com.robin.pos.util;


import oracle.jdbc.OracleDriver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static String host = "Mat";
    private static String puerto = "1521";
    private static String sid = "BDNX1";
    private static String usuario = "LLE";
    private static String password = "YVL";

    public static Connection oracle() throws SQLException {
        Connection conexion = null;
        try {
            DriverManager.registerDriver(new OracleDriver());
            conexion = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":" + puerto + ":" + sid, usuario, password);
        } catch (SQLException var3) {
            System.err.println("Error conexión : " + var3.getMessage());

        }
        return conexion;
    }
}
