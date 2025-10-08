package com.robin.pos.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Arinda1 {
    /*
    private String codigo;
    private String descripcion;
    */
    private final StringProperty codigo = new SimpleStringProperty();
    private final StringProperty descripcion = new SimpleStringProperty();

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
        this.descripcion.set(descripcion);
    }

}
