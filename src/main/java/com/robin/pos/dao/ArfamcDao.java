package com.robin.pos.dao;

import com.robin.pos.model.Arfamc;
import com.robin.pos.util.ConexionBD;
import com.robin.pos.util.Mensaje;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la tabla FACTU.ARFAMC
 * Gestión de datos de la compañía
 *
 * @author Robin POS
 * @version 1.0
 */
public class ArfamcDao {

    private static final Logger LOGGER = Logger.getLogger(ArfamcDao.class.getName());

    /**
     * Obtiene los datos de la compañía
     *
     * @param noCia Número de compañía
     * @return Datos de la compañía o null
     */
    public Arfamc obtenerDatosCompania(String noCia) {
        Arfamc compania = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, NOMBRE, VERIFICA_STOCK, NOMBRE_ANO AS DESCRIPCION, ");
        sql.append("NO_CLIENTE_ONLINE AS RUC, RAZON_SOCIAL, ");
        sql.append("BANCO, CUENTA_SOL, CUENTA_DOL AS CCI, ");
        sql.append("CONTA.F_PORC_TASA(NO_CIA, TIPO_TASA_IGV, CLAVE) AS PORC_IGV, ");
        sql.append("CONTA.F_PORC_TASA(NO_CIA, TIPO_TASA_ISC, ISC) AS PORC_ISC ");
        sql.append("FROM FACTU.ARFAMC ");
        sql.append("WHERE NO_CIA = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                compania = new Arfamc();
                compania.setNoCia(rs.getString("NO_CIA"));
                compania.setNombre(rs.getString("NOMBRE"));
                compania.setVerificaStock(rs.getString("VERIFICA_STOCK"));
                compania.setDescripcion(rs.getString("DESCRIPCION"));
                compania.setRuc(rs.getString("RUC"));
                compania.setRazonSocial(rs.getString("RAZON_SOCIAL"));
                compania.setBanco(rs.getString("BANCO"));
                compania.setCuentaSol(rs.getString("CUENTA_SOL"));
                compania.setCci(rs.getString("CCI"));
                compania.setPorcIgv(rs.getBigDecimal("PORC_IGV"));
                compania.setPorcIsc(rs.getBigDecimal("PORC_ISC"));
            }

            ConexionBD.cerrarCxOracle(cx);
            LOGGER.info("Datos de compañía obtenidos: " + noCia);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al obtener datos de compañía", ex);
            Mensaje.error(null, "Error al Obtener Datos",
                    "No se pudieron obtener los datos de la compañía.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return compania;
    }

    /**
     * Actualiza los datos de la compañía
     *
     * @param compania Datos de la compañía a actualizar
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarDatosCompania(Arfamc compania) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE FACTU.ARFAMC SET ");
        sql.append("NOMBRE = ?, ");
        sql.append("NOMBRE_ANO = ?, ");
        sql.append("NO_CLIENTE_ONLINE = ?, ");
        sql.append("RAZON_SOCIAL = ?, ");
        sql.append("BANCO = ?, ");
        sql.append("CUENTA_SOL = ?, ");
        sql.append("CUENTA_DOL = ? ");
        sql.append("WHERE NO_CIA = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, compania.getNombre());
            ps.setString(2, compania.getDescripcion());
            ps.setString(3, compania.getRuc());
            ps.setString(4, compania.getRazonSocial());
            ps.setString(5, compania.getBanco());
            ps.setString(6, compania.getCuentaSol());
            ps.setString(7, compania.getCci());
            ps.setString(8, compania.getNoCia());

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Datos de compañía actualizados: " + compania.getNoCia());
                Mensaje.alerta (null, "Actualización Exitosa",
                        "Los datos de la compañía se actualizaron correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar datos de compañía", ex);
            Mensaje.error(null, "Error al Actualizar",
                    "No se pudieron actualizar los datos de la compañía.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }
}