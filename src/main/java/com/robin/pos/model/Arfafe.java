package com.robin.pos.model;

import java.math.BigDecimal;
import java.util.Date;

public class Arfafe {
    private String tipoDoc;
    private String tipoCliente;
    private String tipoCambio;
    private String indDoc;
    private String mDsctoGlobal;

    private String noCia;
    private String noFactu;
    private String noCliente;
    private String tipoDocCli;
    private String numDocCli;
    private Date fecha;
    private String nbrCliente;
    private String moneda;
    private String noOrden;
    private BigDecimal subTotal;
    private BigDecimal impuesto;
    private BigDecimal total;
    private String estado;
    private BigDecimal valorVenta;
    private BigDecimal totalBruto;
    private BigDecimal operGravadas;
    private String guiaTemp;
    private String direccion;

    public Arfafe(){}

    public Arfafe(String tipoDoc, String tipoCliente, String tipoCambio, String indDoc, String mDsctoGlobal) {
        this.tipoDoc = tipoDoc;
        this.tipoCliente = tipoCliente;
        this.tipoCambio = tipoCambio;
        this.indDoc = indDoc;
        this.mDsctoGlobal = mDsctoGlobal;
    }

    public Arfafe(String noCia,String tipoDoc, String noFactu, String noCliente, String tipoDocCli, String numDocCli, Date fecha,
                  String nbrCliente, String moneda, String noOrden, BigDecimal subTotal, BigDecimal impuesto,
                  BigDecimal total, String estado, BigDecimal valorVenta, BigDecimal totalBruto,
                  BigDecimal operGravadas, String guiaTemp, String direccion) {
        this.noCia = noCia;
        this.tipoDoc = tipoDoc;
        this.noFactu = noFactu;
        this.noCliente = noCliente;
        this.tipoDocCli = tipoDocCli;
        this.numDocCli = numDocCli;
        this.fecha = fecha;
        this.nbrCliente = nbrCliente;
        this.moneda = moneda;
        this.noOrden = noOrden;
        this.subTotal = subTotal;
        this.impuesto = impuesto;
        this.total = total;
        this.estado = estado;
        this.valorVenta = valorVenta;
        this.totalBruto = totalBruto;
        this.operGravadas = operGravadas;
        this.guiaTemp = guiaTemp;
        this.direccion = direccion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getNoFactu() {
        return noFactu;
    }

    public void setNoFactu(String noFactu) {
        this.noFactu = noFactu;
    }

    public String getNoCliente() {
        return noCliente;
    }

    public void setNoCliente(String noCliente) {
        this.noCliente = noCliente;
    }

    public String getTipoDocCli() {
        return tipoDocCli;
    }

    public void setTipoDocCli(String tipoDocCli) {
        this.tipoDocCli = tipoDocCli;
    }

    public String getNumDocCli() {
        return numDocCli;
    }

    public void setNumDocCli(String numDocCli) {
        this.numDocCli = numDocCli;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNbrCliente() {
        return nbrCliente;
    }

    public void setNbrCliente(String nbrCliente) {
        this.nbrCliente = nbrCliente;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getNoOrden() {
        return noOrden;
    }

    public void setNoOrden(String noOrden) {
        this.noOrden = noOrden;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getValorVenta() {
        return valorVenta;
    }

    public void setValorVenta(BigDecimal valorVenta) {
        this.valorVenta = valorVenta;
    }

    public BigDecimal getTotalBruto() {
        return totalBruto;
    }

    public void setTotalBruto(BigDecimal totalBruto) {
        this.totalBruto = totalBruto;
    }

    public BigDecimal getOperGravadas() {
        return operGravadas;
    }

    public void setOperGravadas(BigDecimal operGravadas) {
        this.operGravadas = operGravadas;
    }

    public String getGuiaTemp() {
        return guiaTemp;
    }

    public void setGuiaTemp(String guiaTemp) {
        this.guiaTemp = guiaTemp;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public String getTipoCambio() {
        return tipoCambio;
    }

    public void setTipoCambio(String tipoCambio) {
        this.tipoCambio = tipoCambio;
    }

    public String getIndDoc() {
        return indDoc;
    }

    public void setIndDoc(String indDoc) {
        this.indDoc = indDoc;
    }

    public String getmDsctoGlobal() {
        return mDsctoGlobal;
    }

    public void setmDsctoGlobal(String mDsctoGlobal) {
        this.mDsctoGlobal = mDsctoGlobal;
    }
}
