package com.robin.pos.dao;

import com.robin.pos.model.SucursalPtovta;
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

/**
 * DAO para la tabla FACTU.SUCURSAL_PTOVTA
 * Gestión de sucursales y puntos de venta
 *
 * @author Robin POS
 * @version 1.0
 */
public class SucursalPtovtaDao {

    private static final Logger LOGGER = Logger.getLogger(SucursalPtovtaDao.class.getName());

    /**
     * Lista todas las sucursales de una compañía
     *
     * @param noCia Número de compañía
     * @return Lista de sucursales
     */
    public List<SucursalPtovta> listarSucursales(String noCia) {
        List<SucursalPtovta> lstSucursales = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_SUCURSAL, COD_PTO_VTA, NOMBRE_SUCU_PTOVTA, ");
        sql.append("CODI_DEPA, CODI_PROV, CODI_DIST, ");
        sql.append("TELEF1, TELEF2, CORREOELECTRO, ESTADO_SUC, CXC.PR_CLIENTE.GET_DIRECC_CIA( ? ) AS DIRECCION, NOM_COMERCIAL ");
        sql.append("FROM FACTU.SUCURSAL_PTOVTA ");
        sql.append("WHERE NO_CIA = ? ");
        sql.append("ORDER BY COD_SUCURSAL, COD_PTO_VTA");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, noCia);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                SucursalPtovta sucursal = new SucursalPtovta();
                sucursal.setNoCia(rs.getString("NO_CIA"));
                sucursal.setCodSucursal(rs.getString("COD_SUCURSAL"));
                sucursal.setCodPtoVta(rs.getString("COD_PTO_VTA"));
                sucursal.setNombreSucuPtovta(rs.getString("NOMBRE_SUCU_PTOVTA"));
                sucursal.setCodiDepa(rs.getString("CODI_DEPA"));
                sucursal.setCodiProv(rs.getString("CODI_PROV"));
                sucursal.setCodiDist(rs.getString("CODI_DIST"));
                sucursal.setTelef1(rs.getString("TELEF1"));
                sucursal.setTelef2(rs.getString("TELEF2"));
                sucursal.setCorreoElectro(rs.getString("CORREOELECTRO"));
                sucursal.setEstadoSuc(rs.getString("ESTADO_SUC"));
                sucursal.setDireccion(rs.getString("DIRECCION"));
                sucursal.setNomComercial(rs.getString("NOM_COMERCIAL"));

                lstSucursales.add(sucursal);
            }

            ConexionBD.cerrarCxOracle(cx);
            LOGGER.info("Se encontraron " + lstSucursales.size() + " sucursales");

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al listar sucursales", ex);
            Mensaje.error(null, "Error al Listar Sucursales",
                    "No se pudo obtener la lista de sucursales.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return lstSucursales;
    }

    /**
     * Busca una sucursal específica
     *
     * @param noCia Número de compañía
     * @param codSucursal Código de sucursal
     * @param codPtoVta Código de punto de venta
     * @return Sucursal encontrada o null
     */
    public SucursalPtovta buscarSucursal(String noCia, String codSucursal, String codPtoVta) {
        SucursalPtovta sucursal = null;

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT NO_CIA, COD_SUCURSAL, COD_PTO_VTA, NOMBRE_SUCU_PTOVTA, ");
        sql.append("CODI_DEPA, CODI_PROV, CODI_DIST, ");
        sql.append("TELEF1, TELEF2, CORREOELECTRO, ESTADO_SUC, DIRECCION, NOM_COMERCIAL ");
        sql.append("FROM FACTU.SUCURSAL_PTOVTA ");
        sql.append("WHERE NO_CIA = ? AND COD_SUCURSAL = ? AND COD_PTO_VTA = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, noCia);
            ps.setString(2, codSucursal);
            ps.setString(3, codPtoVta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                sucursal = new SucursalPtovta();
                sucursal.setNoCia(rs.getString("NO_CIA"));
                sucursal.setCodSucursal(rs.getString("COD_SUCURSAL"));
                sucursal.setCodPtoVta(rs.getString("COD_PTO_VTA"));
                sucursal.setNombreSucuPtovta(rs.getString("NOMBRE_SUCU_PTOVTA"));
                sucursal.setCodiDepa(rs.getString("CODI_DEPA"));
                sucursal.setCodiProv(rs.getString("CODI_PROV"));
                sucursal.setCodiDist(rs.getString("CODI_DIST"));
                sucursal.setTelef1(rs.getString("TELEF1"));
                sucursal.setTelef2(rs.getString("TELEF2"));
                sucursal.setCorreoElectro(rs.getString("CORREOELECTRO"));
                sucursal.setEstadoSuc(rs.getString("ESTADO_SUC"));
                sucursal.setDireccion(rs.getString("DIRECCION"));
                sucursal.setNomComercial(rs.getString("NOM_COMERCIAL"));
            }

            ConexionBD.cerrarCxOracle(cx);

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al buscar sucursal", ex);
            Mensaje.error(null, "Error de Búsqueda",
                    "No se pudo buscar la sucursal.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return sucursal;
    }

    /**
     * Actualiza los datos de una sucursal
     *
     * @param sucursal Sucursal con datos actualizados
     * @return true si se actualizó correctamente, false en caso contrario
     */
    public boolean actualizarSucursal(SucursalPtovta sucursal) {
        boolean resultado = false;

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE FACTU.SUCURSAL_PTOVTA SET ");
        sql.append("NOMBRE_SUCU_PTOVTA = ?, ");
        sql.append("CODI_DEPA = ?, ");
        sql.append("CODI_PROV = ?, ");
        sql.append("CODI_DIST = ?, ");
        sql.append("TELEF1 = ?, ");
        sql.append("TELEF2 = ?, ");
        sql.append("CORREOELECTRO = ?, ");
        sql.append("ESTADO_SUC = ?, ");
        sql.append("DIRECCION = ?, ");
        sql.append("NOM_COMERCIAL = ? ");
        sql.append("WHERE NO_CIA = ? AND COD_SUCURSAL = ? AND COD_PTO_VTA = ?");

        Connection cx = null;

        try {
            cx = ConexionBD.oracle();
            PreparedStatement ps = cx.prepareStatement(sql.toString());
            ps.setString(1, sucursal.getNombreSucuPtovta());
            ps.setString(2, sucursal.getCodiDepa());
            ps.setString(3, sucursal.getCodiProv());
            ps.setString(4, sucursal.getCodiDist());
            ps.setString(5, sucursal.getTelef1());
            ps.setString(6, sucursal.getTelef2());
            ps.setString(7, sucursal.getCorreoElectro());
            ps.setString(8, sucursal.getEstadoSuc());
            ps.setString(9, sucursal.getDireccion());
            ps.setString(10, sucursal.getNomComercial());
            ps.setString(11, sucursal.getNoCia());
            ps.setString(12, sucursal.getCodSucursal());
            ps.setString(13, sucursal.getCodPtoVta());

            int filasAfectadas = ps.executeUpdate();
            resultado = filasAfectadas > 0;

            ConexionBD.cerrarCxOracle(cx);

            if (resultado) {
                LOGGER.info("Sucursal actualizada: " + sucursal.getCodSucursal());
                Mensaje.alerta(null, "Actualización Exitosa",
                        "Los datos de la sucursal se actualizaron correctamente.");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al actualizar sucursal", ex);
            Mensaje.error(null, "Error al Actualizar",
                    "No se pudieron actualizar los datos de la sucursal.\n" + ex.getMessage());
            ConexionBD.cerrarCxOracle(cx);
        } finally {
            ConexionBD.cerrarCxOracle(cx);
        }

        return resultado;
    }
}
