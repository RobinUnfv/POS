package com.robin.pos.dao;

import com.robin.pos.model.Arccmc;
import com.robin.pos.model.Arinda1;
import com.robin.pos.model.EntidadTributaria;
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

public class ArccmcDao {

    public static int contadorRegistros(String numeroDocumento) {
        int contador = 0;
        String query = "SELECT COUNT(*) AS CONTADOR FROM CXC.ARCCMC WHERE NO_CIA = ? AND NO_CLIENTE = ?";
        try {
            Connection cx = ConexionBD.oracle();
            PreparedStatement stmt = cx.prepareStatement(query);
            stmt.setString(1, "01");
            stmt.setString(2, numeroDocumento);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                contador = rs.getInt("CONTADOR");
            }
            rs.close();
            stmt.close();
            ConexionBD.cerrarCxOracle(cx);
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Contador de ARCCMC","Error cuando se intenta contar los registros.");
        }
        return contador;
    }

    public static int registrar(EntidadTributaria entidad) {
        int resultado = 0;
        String query = "INSERT INTO CXC.ARCCMC(NO_CIA,NO_CLIENTE,ACTIVO,TIPO_PERSONA,EXTRANJERO,TIPO_DOCUMENTO,NU_DOCUMENTO," +  //7
                "RUC,NOMBRE,DIRECCION,UBIGEO,CODI_DEPA,CODI_PROV,CODI_DIST,CLASE,COD_PAIS,COD_VEN_COB,TIPO_FPAGO,COD_FPAGO," +  //12
                "TIPO_CLIENTE,GRUPO,MONEDA,LIMITE_CREDI_N,EXCENTO_IMP,USUARIO,IND_VAL,IND_TIENDAS,COD_CLASIF,COD_CATEG," +  //10
                "IND_AGEN_RET,IND_BUEN_CON,IND_SIST_DEFR,COD_CALI,IND_RENOVACION,ORIGEN,IND_PROVE,TIPO_ENTI,COD_SUC,STATUS," + //10
                "CHEQUE_DIFERIDO) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; //1

        String nombre= entidad.getNombre().trim();
        if (nombre.length() > 80) {
            nombre = nombre.substring(0, 80).trim();
        }
        String tipoPersona = entidad.getTipoDocumento();
        String tipoDocumento = null;
        String nuDocumento = null;
        String ruc = null;
        String depar = null;
        String prov = null;
        String dist = null;
        String ubigeo = null;
        if (tipoPersona.equals("1")) {
            tipoPersona = "N";
            tipoDocumento = "DNI";
            nuDocumento = entidad.getNumeroDocumento();
            depar = "15";
            prov = "01";
            dist = "01";
            ubigeo = depar + prov + dist;

        } else if (tipoPersona.equals("6")) {
            tipoPersona = "J";
            tipoDocumento = "RUC";
            ruc = entidad.getNumeroDocumento();
            ubigeo = entidad.getUbigeo().trim();
            if (!ubigeo.equals("-")) {
                depar = ubigeo.substring(0, 2);
                prov = ubigeo.substring(2, 4);
                dist = ubigeo.substring(4, 6);
            }

        }

        try {
            Connection cx = ConexionBD.oracle();
            PreparedStatement stmt = cx.prepareStatement(query);
            stmt.setString(1, "01");
            stmt.setString(2, entidad.getNumeroDocumento());
            stmt.setString(3, "S");
            stmt.setString(4, tipoPersona);
            stmt.setString(5, "N");
            stmt.setString(6, tipoDocumento);
            stmt.setString(7, nuDocumento);
            stmt.setString(8, ruc);
            stmt.setString(9, nombre);
            stmt.setString(10, "-");
            stmt.setString(11, ubigeo);
            stmt.setString(12, depar);
            stmt.setString(13, prov);
            stmt.setString(14, dist);
            stmt.setString(15, "020");
            stmt.setString(16, "001");
            stmt.setString(17, "001");
            stmt.setString(18, "20");
            stmt.setString(19, "01");
            stmt.setString(20, "B");
            stmt.setString(21, "00");
            stmt.setString(22, "SOL");
            stmt.setString(23, "0");
            stmt.setString(24, "N");
            stmt.setString(25, "LLE");
            stmt.setString(26, "L");
            stmt.setString(27, "N");
            stmt.setString(28, "A");
            stmt.setString(29, "A");
            stmt.setString(30, "N");
            stmt.setString(31, "N");
            stmt.setString(32, "N");
            stmt.setString(33, "1");
            stmt.setString(34, "N");
            stmt.setString(35, "01");
            stmt.setString(36, "N");
            stmt.setString(37, "C");
            stmt.setString(38, "001");
            stmt.setString(39, "1");
            stmt.setString(40, "N");
//            stmt.setString(41, null);

            resultado = stmt.executeUpdate();

            stmt.close();
            ConexionBD.cerrarCxOracle(cx);
        }catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Registrar ARCCMC","Error cuando se intenta registra.");
        }
        return resultado;
    }

    public List<Arccmc> listar(String noCia) {
        List<Arccmc> listArccmc = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("SELECT NO_CLIENTE, NOMBRE, TIPO_CLIENTE, TIPO_PERSONA, ACTIVO ");
        query.append("FROM CXC.ARCCMC ");
        query.append("WHERE NO_CIA = ? ");
        query.append("ORDER BY NOMBRE ASC");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(query.toString());
            ps.setString(1, noCia);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Arccmc arccmc = new Arccmc();
                arccmc.setNoCliente(rs.getString("NO_CLIENTE"));
                arccmc.setNombre(rs.getString("NOMBRE"));
                arccmc.setTipoCliente(rs.getString("TIPO_CLIENTE"));
                arccmc.setActivo(rs.getString("ACTIVO"));

                listArccmc.add(arccmc);
            }
            ConexionBD.cerrarCxOracle(cx);
        } catch (SQLException ex) {
            Logger.getLogger(ConexionBD.class.getName()).log(Level.SEVERE, null, ex);
            Mensaje.error(null, "Lista clientes","Error en la consulta de cliente.");
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return listArccmc;

    }

}
