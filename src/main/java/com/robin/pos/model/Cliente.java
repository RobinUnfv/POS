package com.robin.pos.model;

import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Cliente {
    private final IntegerProperty idCliente = new SimpleIntegerProperty();
    private final StringProperty tipoDocumento = new SimpleStringProperty();
    private final StringProperty numDocumento = new SimpleStringProperty();
    private final StringProperty nombre = new SimpleStringProperty();
    private final StringProperty direccion = new SimpleStringProperty();

    

}
