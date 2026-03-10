package com.robin.pos.model;

/**
 * Modelo para la tabla FACTU.SUCURSAL_PTOVTA
 * Representa las sucursales y puntos de venta
 *
 * @author Robin POS
 * @version 1.0
 */
public class SucursalPtovta {

    private String noCia;
    private String codSucursal;
    private String codPtoVta;
    private String nombreSucuPtovta;
    private String codiDepa;
    private String codiProv;
    private String codiDist;
    private String telef1;
    private String telef2;
    private String correoElectro;
    private String estadoSuc;
    private String direccion;
    private String nomComercial;

    public SucursalPtovta() {
    }

    public SucursalPtovta(String noCia, String codSucursal, String codPtoVta,
                          String nombreSucuPtovta, String codiDepa, String codiProv,
                          String codiDist, String telef1, String telef2,
                          String correoElectro, String estadoSuc, String direccion,
                          String nomComercial) {
        this.noCia = noCia;
        this.codSucursal = codSucursal;
        this.codPtoVta = codPtoVta;
        this.nombreSucuPtovta = nombreSucuPtovta;
        this.codiDepa = codiDepa;
        this.codiProv = codiProv;
        this.codiDist = codiDist;
        this.telef1 = telef1;
        this.telef2 = telef2;
        this.correoElectro = correoElectro;
        this.estadoSuc = estadoSuc;
        this.direccion = direccion;
        this.nomComercial = nomComercial;
    }

    // Getters y Setters

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getCodSucursal() {
        return codSucursal;
    }

    public void setCodSucursal(String codSucursal) {
        this.codSucursal = codSucursal;
    }

    public String getCodPtoVta() {
        return codPtoVta;
    }

    public void setCodPtoVta(String codPtoVta) {
        this.codPtoVta = codPtoVta;
    }

    public String getNombreSucuPtovta() {
        return nombreSucuPtovta;
    }

    public void setNombreSucuPtovta(String nombreSucuPtovta) {
        this.nombreSucuPtovta = nombreSucuPtovta;
    }

    public String getCodiDepa() {
        return codiDepa;
    }

    public void setCodiDepa(String codiDepa) {
        this.codiDepa = codiDepa;
    }

    public String getCodiProv() {
        return codiProv;
    }

    public void setCodiProv(String codiProv) {
        this.codiProv = codiProv;
    }

    public String getCodiDist() {
        return codiDist;
    }

    public void setCodiDist(String codiDist) {
        this.codiDist = codiDist;
    }

    public String getTelef1() {
        return telef1;
    }

    public void setTelef1(String telef1) {
        this.telef1 = telef1;
    }

    public String getTelef2() {
        return telef2;
    }

    public void setTelef2(String telef2) {
        this.telef2 = telef2;
    }

    public String getCorreoElectro() {
        return correoElectro;
    }

    public void setCorreoElectro(String correoElectro) {
        this.correoElectro = correoElectro;
    }

    public String getEstadoSuc() {
        return estadoSuc;
    }

    public void setEstadoSuc(String estadoSuc) {
        this.estadoSuc = estadoSuc;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNomComercial() {
        return nomComercial;
    }

    public void setNomComercial(String nomComercial) {
        this.nomComercial = nomComercial;
    }

    /**
     * Obtiene la dirección completa formateada
     */
    public String getDireccionCompleta() {
        StringBuilder sb = new StringBuilder();
        if (direccion != null && !direccion.isEmpty()) {
            sb.append(direccion);
        }
        return sb.toString();
    }

    /**
     * Obtiene el estado con descripción
     */
    public String getEstadoDescripcion() {
        if (estadoSuc == null) return "";
        return switch (estadoSuc) {
            case "A" -> "Activo";
            case "I" -> "Inactivo";
            default -> estadoSuc;
        };
    }

    @Override
    public String toString() {
        return "SucursalPtovta{" +
                "noCia='" + noCia + '\'' +
                ", codSucursal='" + codSucursal + '\'' +
                ", nombreSucuPtovta='" + nombreSucuPtovta + '\'' +
                ", direccion='" + direccion + '\'' +
                '}';
    }
}
