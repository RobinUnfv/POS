package com.robin.pos.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

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



}
