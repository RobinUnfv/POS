package com.robin.pos.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase contenedora para todos los datos convertidos de un comprobante
 */
public class DatosComprobanteCompleto {
    private ResultadoEmision resultadoEmision;
    private DatosCliente datosCliente;
    private DatosVenta datosVenta;
    private List<DetalleVenta> detalles;

    public DatosComprobanteCompleto() {
        this.detalles = new ArrayList<>();
    }

    public ResultadoEmision getResultadoEmision() {
        return resultadoEmision;
    }

    public void setResultadoEmision(ResultadoEmision resultadoEmision) {
        this.resultadoEmision = resultadoEmision;
    }

    public DatosCliente getDatosCliente() {
        return datosCliente;
    }

    public void setDatosCliente(DatosCliente datosCliente) {
        this.datosCliente = datosCliente;
    }

    public DatosVenta getDatosVenta() {
        return datosVenta;
    }

    public void setDatosVenta(DatosVenta datosVenta) {
        this.datosVenta = datosVenta;
    }

    public List<DetalleVenta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVenta> detalles) {
        this.detalles = detalles;
    }

    /**
     * Verifica si los datos están completos
     */
    public boolean isCompleto() {
        return resultadoEmision != null
                && datosCliente != null
                && datosVenta != null
                && detalles != null
                && !detalles.isEmpty();
    }
}
