package com.robin.pos.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;

public class DetalleVenta {

    private final IntegerProperty item = new SimpleIntegerProperty();
    private Arinda1 arinda1;
    private final DoubleProperty cantidad = new SimpleDoubleProperty();
    private final DoubleProperty precio = new SimpleDoubleProperty();
    private final DoubleProperty igv = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    public DetalleVenta() {
        NumberBinding multiplicacion = Bindings.multiply(this.precioProperty(), this.cantidadProperty());
        this.totalProperty().bind(multiplicacion);
    }

    public Integer getItem() {
        return item.get();
    }
    public IntegerProperty itemProperty() {
        return item;
    }
    public void setItem(Integer item) {
        this.item.set(item);
    }

    public Arinda1 getArinda1() {
        return arinda1;
    }
    public void setArinda1(Arinda1 arinda1) {
        this.arinda1 = arinda1;
    }

    public DoubleProperty getIgv() {
        return igv;
    }
    public void setIgv(Double igv) {
        this.igv.set(igv);
    }

    public DoubleProperty igvProperty() {
        return igv;
    }

    public double getCantidad() {
        return cantidad.get();}
    public DoubleProperty cantidadProperty() {
        return cantidad;}
    public void setCantidad(Double cantidad) {
        this.cantidad.set(cantidad);}

    public double getPrecio() {
        return precio.get();}
    public DoubleProperty precioProperty() {
        return precio;}
    public void setPrecio(double precio) {
        this.precio.set(precio);}

    public double getTotal() {
        return total.get();}
    public DoubleProperty totalProperty() {
        return total;}
    public void setTotal(double total) {
        this.total.set(total);}
}
