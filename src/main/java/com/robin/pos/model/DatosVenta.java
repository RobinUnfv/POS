package com.robin.pos.model;

import java.time.LocalDate;

public class DatosVenta {
    private LocalDate fechaEmision;
    private String condicionPago = "VENTA CONTADO";
    private String moneda = "SOL";
    private String vendedor;
    private String ordenCompra;
    private int porcentajeIgv = 18;

    public DatosVenta() {
        this.fechaEmision = LocalDate.now();
    }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public String getCondicionPago() { return condicionPago; }
    public void setCondicionPago(String condicionPago) { this.condicionPago = condicionPago; }

    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }

    public String getVendedor() { return vendedor; }
    public void setVendedor(String vendedor) { this.vendedor = vendedor; }

    public String getOrdenCompra() { return ordenCompra; }
    public void setOrdenCompra(String ordenCompra) { this.ordenCompra = ordenCompra; }

    public int getPorcentajeIgv() { return porcentajeIgv; }
    public void setPorcentajeIgv(int porcentajeIgv) { this.porcentajeIgv = porcentajeIgv; }
}
