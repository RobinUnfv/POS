package com.robin.pos.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {

  private final StringProperty docIdentidad = new SimpleStringProperty();
  private final StringProperty numDocumento = new SimpleStringProperty();
  private Cliente cliente;
  private final ObjectProperty<LocalDateTime> fecha = new SimpleObjectProperty<>();
  private final StringProperty moneda = new SimpleStringProperty();
  private final StringProperty formaPago = new SimpleStringProperty();
  private final StringProperty motContingencia = new SimpleStringProperty();
  private final DoubleProperty pago = new SimpleDoubleProperty();
  private final DoubleProperty vuelto = new SimpleDoubleProperty();
  private final DoubleProperty subTotal = new SimpleDoubleProperty();
  private final DoubleProperty igv = new SimpleDoubleProperty();
  private final DoubleProperty gravada = new SimpleDoubleProperty();
  private final DoubleProperty total = new SimpleDoubleProperty();

  private List<DetalleVenta> detalleVentas = new ArrayList<>();

    public String getDocIdentidad() {
        return docIdentidad.get();
    }
    public void setDocIdentidad(String docIdentidad) {
        this.docIdentidad.set(docIdentidad);
    }
    public StringProperty docIdentidadProperty() {
        return this.docIdentidad;
    }

    public String getNumDocumento() {
        return numDocumento.get();
    }
    public void setNumDocumento(String numDocumento) {
        this.numDocumento.set(numDocumento);
    }
    public StringProperty numDocumentoProperty() {
        return this.numDocumento;
    }

    public Cliente getCliente() {
        return cliente;
    }
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFecha() {
        return fecha.get();
    }
    public void setFecha(LocalDateTime fecha) {
        this.fecha.set(fecha);
    }
    public ObjectProperty<LocalDateTime> fechaProperty() {
        return this.fecha;
    }

    public String getMoneda() {
        return moneda.get();
    }
    public void setMoneda(String moneda) {
        this.moneda.set(moneda);
    }
    public StringProperty monedaProperty() {
        return this.moneda;
    }

    public String getFormaPago() {
        return formaPago.get();
    }
    public void setFormaPago(String formaPago) {
        this.formaPago.set(formaPago);
    }
    public StringProperty formaPagoProperty() {
        return this.formaPago;
    }

    public String getMotContingencia() {
        return motContingencia.get();
    }
    public void setMotContingencia(String motContingencia) {
        this.motContingencia.set(motContingencia);
    }
    public StringProperty motContingenciaProperty() {
        return this.motContingencia;
    }

    public double getPago() {
        return pago.get();
    }
    public void setPago(double pago) {
        this.pago.set(pago);
    }
    public DoubleProperty pagoProperty() {
        return this.pago;
    }

    public double getVuelto() {
        return vuelto.get();
    }
    public void setVuelto(double vuelto) {
        this.vuelto.set(vuelto);
    }
    public DoubleProperty vueltoProperty() {
        return this.vuelto;
    }

    public double getSubTotal() {
        return subTotal.get();
    }
    public void setSubTotal(double subTotal) {
        this.subTotal.set(subTotal);
    }
    public DoubleProperty subTotalProperty() {
        return this.subTotal;
    }

    public double getIgv() {
        return igv.get();
    }
    public void setIgv(double igv) {
        this.igv.set(igv);
    }
    public DoubleProperty igvProperty() {
        return this.igv;
    }

    public double getGravada() {
        return gravada.get();
    }
    public void setGravada(double gravada) {
        this.gravada.set(gravada);
    }
    public DoubleProperty gravadaProperty() {
        return this.gravada;
    }

    public double getTotal() {
        return total.get();
    }
    public void setTotal(double total) {
        this.total.set(total);
    }
    public DoubleProperty totalProperty() {
        return this.total;
    }

    public List<DetalleVenta> getDetalleVentas() {
        return detalleVentas;
    }
    public void setDetalleVentas(List<DetalleVenta> detalleVentas) {
        this.detalleVentas = detalleVentas;
    }

}
