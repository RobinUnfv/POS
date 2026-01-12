package com.robin.pos.model;

import java.math.BigDecimal;

public class Arfafl {

    private String noCia;
    private String tipoDoc;
    private String noFactu;
    private String noArti;
    private String descripcion;
    private String medida;
    private Integer consecutivo;
    private BigDecimal cantidadFact;
    private BigDecimal precioUnit;
    private BigDecimal impIgv;
    private BigDecimal total;
    private BigDecimal totalLin;
    private BigDecimal precIgv;

    public Arfafl() {
    }

    public Arfafl(String noCia, String tipoDoc, String noFactu, String noArti, String descripcion,
                  String medida, Integer consecutivo,  BigDecimal cantidadFact, BigDecimal precioUnit,
                  BigDecimal impIgv, BigDecimal total, BigDecimal totalLin, BigDecimal precIgv) {
        this.noCia = noCia;
        this.tipoDoc = tipoDoc;
        this.noFactu = noFactu;
        this.noArti = noArti;
        this.descripcion = descripcion;
        this.medida = medida;
        this.consecutivo = consecutivo;
        this.cantidadFact = cantidadFact;
        this.precioUnit = precioUnit;
        this.impIgv = impIgv;
        this.total = total;
        this.totalLin = totalLin;
        this.precIgv = precIgv;
    }

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getNoFactu() {
        return noFactu;
    }

    public void setNoFactu(String noFactu) {
        this.noFactu = noFactu;
    }

    public String getNoArti() {
        return noArti;
    }

    public void setNoArti(String noArti) {
        this.noArti = noArti;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMedida() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida = medida;
    }

    public Integer getConsecutivo() {
        return consecutivo;
    }

    public void setConsecutivo(Integer consecutivo) {
        this.consecutivo = consecutivo;
    }

    public BigDecimal getCantidadFact() {
        return cantidadFact;
    }

    public void setCantidadFact(BigDecimal cantidadFact) {
        this.cantidadFact = cantidadFact;
    }

    public BigDecimal getPrecioUnit() {
        return precioUnit;
    }

    public void setPrecioUnit(BigDecimal precioUnit) {
        this.precioUnit = precioUnit;
    }

    public BigDecimal getImpIgv() {
        return impIgv;
    }

    public void setImpIgv(BigDecimal impIgv) {
        this.impIgv = impIgv;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getTotalLin() {
        return totalLin;
    }

    public void setTotalLin(BigDecimal totalLin) {
        this.totalLin = totalLin;
    }

    public BigDecimal getPrecIgv() {
        return precIgv;
    }

    public void setPrecIgv(BigDecimal precIgv) {
        this.precIgv = precIgv;
    }
}
