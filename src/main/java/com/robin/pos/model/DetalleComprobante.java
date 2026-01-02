package com.robin.pos.model;

import java.math.BigDecimal;

public class DetalleComprobante {
    private String codigo;
    private String descripcion;
    private String unidad;
    private BigDecimal cantidad;
    private BigDecimal valorUnitario;
    private BigDecimal descuento;
    private BigDecimal igv;
    private BigDecimal valorTotal;

    // Constructor vacío
    public DetalleComprobante() {
    }

    // Constructor completo
    public DetalleComprobante(String codigo, String descripcion, String unidad,
                              BigDecimal cantidad, BigDecimal valorUnitario,
                              BigDecimal descuento, BigDecimal igv, BigDecimal valorTotal) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.unidad = unidad;
        this.cantidad = cantidad;
        this.valorUnitario = valorUnitario;
        this.descuento = descuento;
        this.igv = igv;
        this.valorTotal = valorTotal;
    }

    /**
     * Constructor desde DetalleVenta (para conversión fácil)
     */
    public static DetalleComprobante fromDetalleVenta(DetalleVenta dv, int porcentajeIgv) {
        DetalleComprobante dc = new DetalleComprobante();

        dc.setCodigo(dv.getArinda1().getCodigo());
        dc.setDescripcion(dv.getArinda1().getDescripcion());
        dc.setUnidad("NIU"); // Unidad por defecto
        dc.setCantidad(BigDecimal.valueOf(dv.getCantidad()));

        // Calcular valores
        double precioConIgv = dv.getPrecio();
        double precioSinIgv = precioConIgv / (1 + porcentajeIgv / 100.0);
        double totalSinIgv = precioSinIgv * dv.getCantidad();
        double igvMonto = totalSinIgv * (porcentajeIgv / 100.0);
        double totalConIgv = totalSinIgv + igvMonto;

        dc.setValorUnitario(BigDecimal.valueOf(precioSinIgv));
        dc.setDescuento(BigDecimal.ZERO);
        dc.setIgv(BigDecimal.valueOf(igvMonto));
        dc.setValorTotal(BigDecimal.valueOf(totalConIgv));

        return dc;
    }

    // ========== GETTERS Y SETTERS ==========

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getCantidad() {
        return cantidad;
    }

    public void setCantidad(BigDecimal cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public BigDecimal getIgv() {
        return igv;
    }

    public void setIgv(BigDecimal igv) {
        this.igv = igv;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    @Override
    public String toString() {
        return "DetalleComprobante{" +
                "codigo='" + codigo + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", cantidad=" + cantidad +
                ", valorUnitario=" + valorUnitario +
                ", valorTotal=" + valorTotal +
                '}';
    }
}
