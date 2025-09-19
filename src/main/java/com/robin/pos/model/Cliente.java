package com.robin.pos.model;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente {

    private final StringProperty noCliente = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty telefono = new SimpleStringProperty();
    private final StringProperty ruc = new SimpleStringProperty();
    private final StringProperty tipoCliente = new SimpleStringProperty();
    private final StringProperty tipoPersona = new SimpleStringProperty();
    private final StringProperty nuDocumento = new SimpleStringProperty();
    private final StringProperty tipoDocumento = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty direccion = new SimpleStringProperty();
    private final StringProperty codiDepa = new SimpleStringProperty();
    private final StringProperty codiProv = new SimpleStringProperty();
    private final StringProperty codiDist = new SimpleStringProperty();
    private final StringProperty estabSunat = new SimpleStringProperty();

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

    public String getTelefono() {
        return telefono.get();
    }

    public StringProperty telefonoProperty() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono.set(telefono);
    }

    public String getRuc() {
        return ruc.get();
    }

    public StringProperty rucProperty() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc.set(ruc);
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

    public String getNuDocumento() {
        return nuDocumento.get();
    }

    public StringProperty nuDocumentoProperty() {
        return nuDocumento;
    }

    public void setNuDocumento(String nuDocumento) {
        this.nuDocumento.set(nuDocumento);
    }

    public String getTipoDocumento() {
        return tipoDocumento.get();
    }

    public StringProperty tipoDocumentoProperty() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento.set(tipoDocumento);
    }

    public String getEmail() {
        return email.get();
    }

    public StringProperty emailProperty() {
        return email;
    }

    public void setEmail(String email) {
        this.email.set(email);
    }

    public String getDireccion() {
        return direccion.get();
    }

    public StringProperty direccionProperty() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion.set(direccion);
    }

    public String getCodiDepa() {
        return codiDepa.get();
    }

    public StringProperty codiDepaProperty() {
        return codiDepa;
    }

    public void setCodiDepa(String codiDepa) {
        this.codiDepa.set(codiDepa);
    }

    public String getCodiProv() {
        return codiProv.get();
    }

    public StringProperty codiProvProperty() {
        return codiProv;
    }

    public void setCodiProv(String codiProv) {
        this.codiProv.set(codiProv);
    }

    public String getCodiDist() {
        return codiDist.get();
    }

    public StringProperty codiDistProperty() {
        return codiDist;
    }

    public void setCodiDist(String codiDist) {
        this.codiDist.set(codiDist);
    }

    public String getEstabSunat() {
        return estabSunat.get();
    }

    public StringProperty estabSunatProperty() {
        return estabSunat;
    }

    public void setEstabSunat(String estabSunat) {
        this.estabSunat.set(estabSunat);
    }
}
