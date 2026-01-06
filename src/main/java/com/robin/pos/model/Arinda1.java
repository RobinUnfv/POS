package com.robin.pos.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.math.BigDecimal;

public class Arinda1 {

    private final StringProperty codigo = new SimpleStringProperty();
    private final StringProperty descripcion = new SimpleStringProperty();

    private final StringProperty noCia = new SimpleStringProperty();
    private final StringProperty tipoArti = new SimpleStringProperty();
    private final StringProperty medida = new SimpleStringProperty();           // Unidad de medida
    private final StringProperty moneda = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> costoUni = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> stkMinimo = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> stkMaximo = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final StringProperty indCodBarra = new SimpleStringProperty();      // Código de barras
    private final StringProperty vigente = new SimpleStringProperty();

    public Arinda1() {
    }

    public Arinda1(String codigo, String descripcion) {
        this.codigo.set(codigo);
        this.descripcion.set(descripcion);
    }

    public Arinda1(String noCia, String tipoArti, String noArti, String descripcion,
                   String medida, String moneda, BigDecimal costoUni,
                   BigDecimal stkMinimo, BigDecimal stkMaximo,
                   String indCodBarra, String vigente) {
        this.noCia.set(noCia);
        this.tipoArti.set(tipoArti);
        this.codigo.set(noArti);
        this.descripcion.set(descripcion);
        this.medida.set(medida);
        this.moneda.set(moneda);
        this.costoUni.set(costoUni);
        this.stkMinimo.set(stkMinimo);
        this.stkMaximo.set(stkMaximo);
        this.indCodBarra.set(indCodBarra);
        this.vigente.set(vigente);
    }

    // ==================== NO_CIA ====================

    public String getNoCia() {
        return noCia.get();
    }

    public StringProperty noCiaProperty() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia.set(noCia);
    }

    // ==================== TIPO_ARTI ====================

    public String getTipoArti() {
        return tipoArti.get();
    }

    public StringProperty tipoArtiProperty() {
        return tipoArti;
    }

    public void setTipoArti(String tipoArti) {
        this.tipoArti.set(tipoArti);
    }

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

    // ==================== MEDIDA ====================

    public String getMedida() {
        return medida.get();
    }

    public StringProperty medidaProperty() {
        return medida;
    }

    public void setMedida(String medida) {
        this.medida.set(medida);
    }

    // ==================== MONEDA ====================

    public String getMoneda() {
        return moneda.get();
    }

    public StringProperty monedaProperty() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda.set(moneda);
    }

    // ==================== COSTO_UNI ====================

    public BigDecimal getCostoUni() {
        return costoUni.get();
    }

    public ObjectProperty<BigDecimal> costoUniProperty() {
        return costoUni;
    }

    public void setCostoUni(BigDecimal costoUni) {
        this.costoUni.set(costoUni);
    }

    /**
     * Obtener costo como double (conveniencia)
     */
    public double getCostoUniDouble() {
        BigDecimal costo = costoUni.get();
        return costo != null ? costo.doubleValue() : 0.0;
    }

    // ==================== STK_MINIMO ====================

    public BigDecimal getStkMinimo() {
        return stkMinimo.get();
    }

    public ObjectProperty<BigDecimal> stkMinimoProperty() {
        return stkMinimo;
    }

    public void setStkMinimo(BigDecimal stkMinimo) {
        this.stkMinimo.set(stkMinimo);
    }

    // ==================== STK_MAXIMO ====================

    public BigDecimal getStkMaximo() {
        return stkMaximo.get();
    }

    public ObjectProperty<BigDecimal> stkMaximoProperty() {
        return stkMaximo;
    }

    public void setStkMaximo(BigDecimal stkMaximo) {
        this.stkMaximo.set(stkMaximo);
    }

    // ==================== IND_COD_BARRA ====================

    public String getIndCodBarra() {
        return indCodBarra.get();
    }

    public StringProperty indCodBarraProperty() {
        return indCodBarra;
    }

    public void setIndCodBarra(String indCodBarra) {
        this.indCodBarra.set(indCodBarra);
    }

    /**
     * Alias para código de barras
     */
    public String getCodigoBarra() {
        return indCodBarra.get();
    }

    public void setCodigoBarra(String codigoBarra) {
        this.indCodBarra.set(codigoBarra);
    }

    // ==================== VIGENTE ====================

    public String getVigente() {
        return vigente.get();
    }

    public StringProperty vigenteProperty() {
        return vigente;
    }

    public void setVigente(String vigente) {
        this.vigente.set(vigente);
    }

    /**
     * Verifica si el artículo está vigente
     */
    public boolean isVigente() {
        return "S".equalsIgnoreCase(vigente.get());
    }

    @Override
    public String toString() {
        return getDescripcion(); // Para mostrar en ComboBox
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arinda1 arinda1 = (Arinda1) o;
        String thisNoArti = codigo.get();
        String otherNoArti = arinda1.codigo.get();
        if (thisNoArti == null || otherNoArti == null) return false;
        return thisNoArti.equals(otherNoArti);
    }

}
