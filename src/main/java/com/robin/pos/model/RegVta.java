package com.robin.pos.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo para la tabla FACTU.REG_VTA
 * Representa los registros de venta
 *
 * @author Robin POS
 * @version 1.0
 */
public class RegVta {

    private String noCia;
    private LocalDate fecha;
    private String codSunat;
    private String nbrSunat;
    private String serie;
    private String factura;
    private String ruc;
    private String nombre;
    private BigDecimal impGravable;
    private BigDecimal impExonerado;
    private BigDecimal impIgv;
    private BigDecimal impIsc;
    private BigDecimal impOtros;
    private BigDecimal impTotal;
    private BigDecimal tipoCambio;
    private BigDecimal impOriginal;
    private BigDecimal valorFob;
    private String transaccion;
    private String tipoObse;
    private String noFactu;
    private String tipoDoc;
    private Integer correlativo;
    private LocalDate fecRefeFactu;
    private LocalDate fechaVence;
    private String tipoRefeFactu;
    private String serieRefe;
    private String facturaRefe;
    private String sunatRefe;
    private String tipoDocEmp;
    private String numDocEmp;
    private BigDecimal redondeo;

    public RegVta() {
    }

    // Getters y Setters

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getCodSunat() {
        return codSunat;
    }

    public void setCodSunat(String codSunat) {
        this.codSunat = codSunat;
    }

    public String getNbrSunat() {
        return nbrSunat;
    }

    public void setNbrSunat(String nbrSunat) {
        this.nbrSunat = nbrSunat;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getImpGravable() {
        return impGravable;
    }

    public void setImpGravable(BigDecimal impGravable) {
        this.impGravable = impGravable;
    }

    public BigDecimal getImpExonerado() {
        return impExonerado;
    }

    public void setImpExonerado(BigDecimal impExonerado) {
        this.impExonerado = impExonerado;
    }

    public BigDecimal getImpIgv() {
        return impIgv;
    }

    public void setImpIgv(BigDecimal impIgv) {
        this.impIgv = impIgv;
    }

    public BigDecimal getImpIsc() {
        return impIsc;
    }

    public void setImpIsc(BigDecimal impIsc) {
        this.impIsc = impIsc;
    }

    public BigDecimal getImpOtros() {
        return impOtros;
    }

    public void setImpOtros(BigDecimal impOtros) {
        this.impOtros = impOtros;
    }

    public BigDecimal getImpTotal() {
        return impTotal;
    }

    public void setImpTotal(BigDecimal impTotal) {
        this.impTotal = impTotal;
    }

    public BigDecimal getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(BigDecimal tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public BigDecimal getImpOriginal() {
        return impOriginal;
    }

    public void setImpOriginal(BigDecimal impOriginal) {
        this.impOriginal = impOriginal;
    }

    public BigDecimal getValorFob() {
        return valorFob;
    }

    public void setValorFob(BigDecimal valorFob) {
        this.valorFob = valorFob;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    public String getTipoObse() {
        return tipoObse;
    }

    public void setTipoObse(String tipoObse) {
        this.tipoObse = tipoObse;
    }

    public String getNoFactu() {
        return noFactu;
    }

    public void setNoFactu(String noFactu) {
        this.noFactu = noFactu;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public Integer getCorrelativo() {
        return correlativo;
    }

    public void setCorrelativo(Integer correlativo) {
        this.correlativo = correlativo;
    }

    public LocalDate getFecRefeFactu() {
        return fecRefeFactu;
    }

    public void setFecRefeFactu(LocalDate fecRefeFactu) {
        this.fecRefeFactu = fecRefeFactu;
    }

    public LocalDate getFechaVence() {
        return fechaVence;
    }

    public void setFechaVence(LocalDate fechaVence) {
        this.fechaVence = fechaVence;
    }

    public String getTipoRefeFactu() {
        return tipoRefeFactu;
    }

    public void setTipoRefeFactu(String tipoRefeFactu) {
        this.tipoRefeFactu = tipoRefeFactu;
    }

    public String getSerieRefe() {
        return serieRefe;
    }

    public void setSerieRefe(String serieRefe) {
        this.serieRefe = serieRefe;
    }

    public String getFacturaRefe() {
        return facturaRefe;
    }

    public void setFacturaRefe(String facturaRefe) {
        this.facturaRefe = facturaRefe;
    }

    public String getSunatRefe() {
        return sunatRefe;
    }

    public void setSunatRefe(String sunatRefe) {
        this.sunatRefe = sunatRefe;
    }

    public String getTipoDocEmp() {
        return tipoDocEmp;
    }

    public void setTipoDocEmp(String tipoDocEmp) {
        this.tipoDocEmp = tipoDocEmp;
    }

    public String getNumDocEmp() {
        return numDocEmp;
    }

    public void setNumDocEmp(String numDocEmp) {
        this.numDocEmp = numDocEmp;
    }

    public BigDecimal getRedondeo() {
        return redondeo;
    }

    public void setRedondeo(BigDecimal redondeo) {
        this.redondeo = redondeo;
    }

}