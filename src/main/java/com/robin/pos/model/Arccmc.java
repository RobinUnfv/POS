package com.robin.pos.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Arccmc {

    private final StringProperty noCliente = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty tipoCliente = new SimpleStringProperty();
    private final StringProperty tipoPersona = new SimpleStringProperty();
    private final StringProperty activo = new SimpleStringProperty();

    public String getNoCliente() {
        return noCliente.get();
    }
    public StringProperty noClienteProperty() {
        return noCliente;
    }
    public void setNoCliente(String noCliente) {
        this.noCliente.set(noCliente);
    }

    public String getNombre() {
        return nombre.get();
    }
    public StringProperty nombreProperty() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre.set(nombre);
    }
    public String getTipoCliente() {
        return tipoCliente.get();
    }
    public StringProperty tipoClienteProperty() {
        return tipoCliente;
    }
    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente.set(tipoCliente);
    }
    public String getTipoPersona() {
        return tipoPersona.get();
    }
    public StringProperty tipoPersonaProperty() {
        return tipoPersona;
    }
    public void setTipoPersona(String tipoPersona) {
        this.tipoPersona.set(tipoPersona);
    }
    public String getActivo() {
        return activo.get();
    }
    public StringProperty activoProperty() {
        return activo;
    }
    public void setActivo(String activo) {
        this.activo.set(activo);
    }

}
