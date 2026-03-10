package com.robin.pos.model;

import java.math.BigDecimal;

/**
 * Modelo para la tabla FACTU.ARFAMC
 * Representa los datos de la compañía
 *
 * @author Robin POS
 * @version 1.0
 */
public class Arfamc {

    private String noCia;
    private String nombre;
    private String verificaStock;
    private String descripcion;
    private String ruc;
    private String razonSocial;
    private String banco;
    private String cuentaSol;
    private String cci;
    private BigDecimal porcIgv;
    private BigDecimal porcIsc;

    public Arfamc() {
    }

    public Arfamc(String noCia, String nombre, String verificaStock, String descripcion,
                  String ruc, String razonSocial, String banco, String cuentaSol,
                  String cci, BigDecimal porcIgv, BigDecimal porcIsc) {
        this.noCia = noCia;
        this.nombre = nombre;
        this.verificaStock = verificaStock;
        this.descripcion = descripcion;
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.banco = banco;
        this.cuentaSol = cuentaSol;
        this.cci = cci;
        this.porcIgv = porcIgv;
        this.porcIsc = porcIsc;
    }

    // Getters y Setters

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getVerificaStock() {
        return verificaStock;
    }

    public void setVerificaStock(String verificaStock) {
        this.verificaStock = verificaStock;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getCuentaSol() {
        return cuentaSol;
    }

    public void setCuentaSol(String cuentaSol) {
        this.cuentaSol = cuentaSol;
    }

    public String getCci() {
        return cci;
    }

    public void setCci(String cci) {
        this.cci = cci;
    }

    public BigDecimal getPorcIgv() {
        return porcIgv;
    }

    public void setPorcIgv(BigDecimal porcIgv) {
        this.porcIgv = porcIgv;
    }

    public BigDecimal getPorcIsc() {
        return porcIsc;
    }

    public void setPorcIsc(BigDecimal porcIsc) {
        this.porcIsc = porcIsc;
    }

    /**
     * Obtiene el IGV formateado como porcentaje
     */
    public String getPorcIgvFormateado() {
        if (porcIgv == null) return "0%";
        return porcIgv + "%";
    }

    /**
     * Obtiene el ISC formateado como porcentaje
     */
    public String getPorcIscFormateado() {
        if (porcIsc == null) return "0%";
        return porcIsc + "%";
    }

    @Override
    public String toString() {
        return "Arfamc{" +
                "noCia='" + noCia + '\'' +
                ", nombre='" + nombre + '\'' +
                ", ruc='" + ruc + '\'' +
                ", razonSocial='" + razonSocial + '\'' +
                '}';
    }
}
