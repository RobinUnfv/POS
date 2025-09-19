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
}
