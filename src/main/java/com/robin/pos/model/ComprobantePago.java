package com.robin.pos.model;

import javafx.beans.property.*;

import java.time.LocalDate;

/**
 * Clase modelo para Comprobante de Pago
 * Tabla: FACTU.ARFAFE
 *
 */
public class ComprobantePago {

    // === CAMPOS DE FILTRO (WHERE) ===
    private final StringProperty noCia = new SimpleStringProperty();
    private final StringProperty tipoDoc = new SimpleStringProperty();
    private final StringProperty estado = new SimpleStringProperty();

    // === CAMPOS DE CONSULTA (SELECT) ===
    private final StringProperty noFactu = new SimpleStringProperty();
    private final ObjectProperty<LocalDate> fecha = new SimpleObjectProperty<>();
    private final StringProperty tipoDocCli = new SimpleStringProperty();
    private final StringProperty numDocCli = new SimpleStringProperty();
    private final StringProperty nbrCliente = new SimpleStringProperty();
    private final StringProperty moneda = new SimpleStringProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();

    // === CONSTRUCTORES ===

    public ComprobantePago() {
    }

    public ComprobantePago(String noFactu, LocalDate fecha, String tipoDocCli,
                           String numDocCli, String nbrCliente, String moneda, Double total) {
        setNoFactu(noFactu);
        setFecha(fecha);
        setTipoDocCli(tipoDocCli);
        setNumDocCli(numDocCli);
        setNbrCliente(nbrCliente);
        setMoneda(moneda);
        setTotal(total);
    }

    // === NO_CIA ===
    public String getNoCia() {
        return noCia.get();
    }

    public void setNoCia(String value) {
        noCia.set(value);
    }

    public StringProperty noCiaProperty() {
        return noCia;
    }

    // === TIPO_DOC ===
    public String getTipoDoc() {
        return tipoDoc.get();
    }

    public void setTipoDoc(String value) {
        tipoDoc.set(value);
    }

    public StringProperty tipoDocProperty() {
        return tipoDoc;
    }

    // === ESTADO ===
    public String getEstado() {
        return estado.get();
    }

    public void setEstado(String value) {
        estado.set(value);
    }

    public StringProperty estadoProperty() {
        return estado;
    }

    // === NO_FACTU ===
    public String getNoFactu() {
        return noFactu.get();
    }

    public void setNoFactu(String value) {
        noFactu.set(value);
    }

    public StringProperty noFactuProperty() {
        return noFactu;
    }

    // === FECHA ===
    public LocalDate getFecha() {
        return fecha.get();
    }

    public void setFecha(LocalDate value) {
        fecha.set(value);
    }

    public ObjectProperty<LocalDate> fechaProperty() {
        return fecha;
    }

    // === TIPO_DOC_CLI ===
    public String getTipoDocCli() {
        return tipoDocCli.get();
    }

    public void setTipoDocCli(String value) {
        tipoDocCli.set(value);
    }

    public StringProperty tipoDocCliProperty() {
        return tipoDocCli;
    }

    // === NUM_DOC_CLI ===
    public String getNumDocCli() {
        return numDocCli.get();
    }

    public void setNumDocCli(String value) {
        numDocCli.set(value);
    }

    public StringProperty numDocCliProperty() {
        return numDocCli;
    }

    // === NBR_CLIENTE ===
    public String getNbrCliente() {
        return nbrCliente.get();
    }

    public void setNbrCliente(String value) {
        nbrCliente.set(value);
    }

    public StringProperty nbrClienteProperty() {
        return nbrCliente;
    }

    // === MONEDA ===
    public String getMoneda() {
        return moneda.get();
    }

    public void setMoneda(String value) {
        moneda.set(value);
    }

    public StringProperty monedaProperty() {
        return moneda;
    }

    // === TOTAL ===
    public Double getTotal() {
        return total.get();
    }

    public void setTotal(Double value) {
        total.set(value != null ? value : 0.0);
    }

    public DoubleProperty totalProperty() {
        return total;
    }

    // === MÉTODOS UTILITARIOS ===

    /**
     * Retorna el tipo de documento del cliente en formato legible
     */
    public String getTipoDocCliDescripcion() {
        String tipo = getTipoDocCli();
        if (tipo == null) return "";
        return switch (tipo) {
            case "1" -> "DNI";
            case "6" -> "RUC";
            case "7" -> "CE";
            case "4" -> "C.EXT";
            case "0" -> "OTROS";
            default -> tipo;
        };
    }

    /**
     * Retorna el símbolo de la moneda
     */
    public String getSimboloMoneda() {
        String mon = getMoneda();
        if (mon == null) return "";
        return switch (mon.toUpperCase()) {
            case "S", "PEN", "SOL", "SOLES" -> "S/";
            case "D", "USD", "DOLAR", "DOLARES" -> "$";
            case "E", "EUR", "EURO", "EUROS" -> "€";
            default -> mon;
        };
    }

    /**
     * Retorna el total formateado con símbolo de moneda
     */
    public String getTotalFormateado() {
        return String.format("%s %.2f", getSimboloMoneda(), getTotal());
    }

    @Override
    public String toString() {
        return "ComprobantePago{" +
                "noFactu='" + getNoFactu() + '\'' +
                ", fecha=" + getFecha() +
                ", nbrCliente='" + getNbrCliente() + '\'' +
                ", total=" + getTotalFormateado() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComprobantePago that = (ComprobantePago) o;
        return getNoFactu() != null && getNoFactu().equals(that.getNoFactu());
    }

    @Override
    public int hashCode() {
        return getNoFactu() != null ? getNoFactu().hashCode() : 0;
    }
}
