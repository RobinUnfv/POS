package com.robin.pos.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;

/**
 * Línea (bien transportado) de la Guía de Remisión Electrónica.
 * Origen: FACTU.ARPFFL  ->  Destino: FACTU.ARGUIL
 *
 * Usa propiedades JavaFX para poder editarse en la tabla del formulario;
 * los getters normales permiten usarla también como fuente del reporte Jasper.
 */
public class GuiaRemisionDetalle {

    private final IntegerProperty itemOrden = new SimpleIntegerProperty();
    private final StringProperty codProducto = new SimpleStringProperty("");
    private final StringProperty descripcion = new SimpleStringProperty("");
    private final ObjectProperty<BigDecimal> cantidad = new SimpleObjectProperty<>(BigDecimal.ONE);
    private final StringProperty undMedida = new SimpleStringProperty("NIU");
    private final ObjectProperty<BigDecimal> pesoTotal = new SimpleObjectProperty<>();
    private final StringProperty noLote = new SimpleStringProperty();

    public GuiaRemisionDetalle() {
    }

    public GuiaRemisionDetalle(int itemOrden, String codProducto, String descripcion,
                               BigDecimal cantidad, String undMedida) {
        setItemOrden(itemOrden);
        setCodProducto(codProducto);
        setDescripcion(descripcion);
        setCantidad(cantidad);
        setUndMedida(undMedida);
    }

    public int getItemOrden() { return itemOrden.get(); }
    public void setItemOrden(int v) { itemOrden.set(v); }
    public IntegerProperty itemOrdenProperty() { return itemOrden; }

    public String getCodProducto() { return codProducto.get(); }
    public void setCodProducto(String v) { codProducto.set(v); }
    public StringProperty codProductoProperty() { return codProducto; }

    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String v) { descripcion.set(v); }
    public StringProperty descripcionProperty() { return descripcion; }

    public BigDecimal getCantidad() { return cantidad.get(); }
    public void setCantidad(BigDecimal v) { cantidad.set(v); }
    public ObjectProperty<BigDecimal> cantidadProperty() { return cantidad; }

    public String getUndMedida() { return undMedida.get(); }
    public void setUndMedida(String v) { undMedida.set(v); }
    public StringProperty undMedidaProperty() { return undMedida; }

    public BigDecimal getPesoTotal() { return pesoTotal.get(); }
    public void setPesoTotal(BigDecimal v) { pesoTotal.set(v); }
    public ObjectProperty<BigDecimal> pesoTotalProperty() { return pesoTotal; }

    public String getNoLote() { return noLote.get(); }
    public void setNoLote(String v) { noLote.set(v); }
    public StringProperty noLoteProperty() { return noLote; }
}
