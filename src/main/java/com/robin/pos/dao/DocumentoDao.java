package com.robin.pos.dao;

import com.robin.pos.util.ConexionBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

public class DocumentoDao {

    public static String obtenerSerieDocumento(String noCia, String centro, String tipoDoc) {
        // ── Validación ──────────────────────────────────────────
        if (noCia == null || noCia.trim().isEmpty()) {
            return null;
        }
        if (centro == null || centro.trim().isEmpty()) {
            return null;
        }
        if (tipoDoc == null || tipoDoc.trim().isEmpty()) {
            return null;
        }

        Connection conn = null;
        CallableStatement cstmt = null;

        try {
            conn = ConexionBD.oracle();
            String sql = "{? = call FACTU.PR_DOCUMENTO.GET_NUM_DOCUMENTO(?, ?, ?)}";
            cstmt = conn.prepareCall(sql);

            cstmt.registerOutParameter(1, Types.VARCHAR);

            cstmt.setString(2, noCia.trim());    // p_cNoCia
            cstmt.setString(3, centro.trim());   // p_cCentro
            cstmt.setString(4, tipoDoc.trim());  // p_cTipoDoc

            cstmt.execute();

            String numero = cstmt.getString(1);

            if (numero == null || numero.trim().isEmpty()) {
                return null;
            }

            numero = numero.trim();
            return numero;

        } catch (java.sql.SQLException ex) {
            System.out.println("Error SQL al obtener número: " + ex.getMessage());
            return null;

        } catch (Exception ex) {
            System.out.println("Error inesperado: " + ex.getMessage());
            return null;
        } finally {
            try {
                if (cstmt != null) cstmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
