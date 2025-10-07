package com.robin.pos.model;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;

public class DetalleVenta {
    private final StringProperty item = new SimpleStringProperty();
    private Arinda1 arinda1;
    /*
    private final StringProperty codigo = new SimpleStringProperty();
    private final StringProperty descripcion = new SimpleStringProperty();
    */
    private final IntegerProperty cantidad = new SimpleIntegerProperty();
    private final DoubleProperty precio = new SimpleDoubleProperty();
    private final DoubleProperty igv = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    public DetalleVenta() {
        NumberBinding multiplicacion = Bindings.multiply(this.precioProperty(), this.cantidadProperty());
        this.totalProperty().bind(multiplicacion);
    }

    public String getItem() {
        return item.get();
    }
    public StringProperty itemProperty() {
        return item;
    }
    public void setItem(String item) {
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


    /*
        public String getCodigo() {
            return codigo.get();
        }
        public StringProperty codigoProperty() {
            return codigo;
        }
        public void setCodigo(String codigo) {
            this.codigo.set(codigo);
        }

        public String getDescripcion() {
            return descripcion.get();
        }
        public StringProperty descripcionProperty() {
            return descripcion;
        }
        public void setDescripcion(String descripcion) {
            this.descripcion.set(descripcion);}
        */
    public int getCantidad() {
        return cantidad.get();}
    public IntegerProperty cantidadProperty() {
        return cantidad;}
    public void setCantidad(int cantidad) {
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
