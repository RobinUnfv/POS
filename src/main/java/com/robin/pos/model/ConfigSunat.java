package com.robin.pos.model;

import javafx.beans.property.*;

/**
 * Modelo para la configuración de parámetros SUNAT
 * Tabla: FACTU.CONFIG_SUNAT
 *
 * @author Robin POS
 * @version 1.0
 */
public class ConfigSunat {

    private final StringProperty noCia;
    private final StringProperty codigo;
    private final StringProperty valor;
    private final StringProperty descripcion;
    private final StringProperty grupo;
    private final StringProperty tipoDato;
    private final StringProperty activo;

    /**
     * Constructor vacío
     */
    public ConfigSunat() {
        this.noCia = new SimpleStringProperty();
        this.codigo = new SimpleStringProperty();
        this.valor = new SimpleStringProperty();
        this.descripcion = new SimpleStringProperty();
        this.grupo = new SimpleStringProperty();
        this.tipoDato = new SimpleStringProperty();
        this.activo = new SimpleStringProperty();
    }

    /**
     * Constructor completo
     */
    public ConfigSunat(String noCia, String codigo, String valor, String descripcion,
                       String grupo, String tipoDato, String activo) {
        this.noCia = new SimpleStringProperty(noCia);
        this.codigo = new SimpleStringProperty(codigo);
        this.valor = new SimpleStringProperty(valor);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.grupo = new SimpleStringProperty(grupo);
        this.tipoDato = new SimpleStringProperty(tipoDato);
        this.activo = new SimpleStringProperty(activo);
    }

    // ==================== GETTERS Y SETTERS ====================

    // NO_CIA
    public String getNoCia() {
        return noCia.get();
    }

    public void setNoCia(String noCia) {
        this.noCia.set(noCia);
    }

    public StringProperty noCiaProperty() {
        return noCia;
    }

    // CODIGO
    public String getCodigo() {
        return codigo.get();
    }

    public void setCodigo(String codigo) {
        this.codigo.set(codigo);
    }

    public StringProperty codigoProperty() {
        return codigo;
    }

    // VALOR
    public String getValor() {
        return valor.get();
    }

    public void setValor(String valor) {
        this.valor.set(valor);
    }

    public StringProperty valorProperty() {
        return valor;
    }

    // DESCRIPCION
    public String getDescripcion() {
        return descripcion.get();
    }

    public void setDescripcion(String descripcion) {
        this.descripcion.set(descripcion);
    }

    public StringProperty descripcionProperty() {
        return descripcion;
    }

    // GRUPO
    public String getGrupo() {
        return grupo.get();
    }

    public void setGrupo(String grupo) {
        this.grupo.set(grupo);
    }

    public StringProperty grupoProperty() {
        return grupo;
    }

    // TIPO_DATO
    public String getTipoDato() {
        return tipoDato.get();
    }

    public void setTipoDato(String tipoDato) {
        this.tipoDato.set(tipoDato);
    }

    public StringProperty tipoDatoProperty() {
        return tipoDato;
    }

    // ACTIVO
    public String getActivo() {
        return activo.get();
    }

    public void setActivo(String activo) {
        this.activo.set(activo);
    }

    public StringProperty activoProperty() {
        return activo;
    }

    // ==================== MÉTODOS HELPER ====================

    /**
     * Obtiene el texto descriptivo del estado
     */
    public String getEstadoDescripcion() {
        return "S".equals(getActivo()) ? "Activo" : "Inactivo";
    }

    /**
     * Verifica si está activo
     */
    public boolean isActivo() {
        return "S".equals(getActivo());
    }

    /**
     * Verifica si es tipo PASSWORD
     */
    public boolean isPassword() {
        return "PASSWORD".equals(getTipoDato());
    }

    /**
     * Obtiene el valor enmascarado si es password
     */
    public String getValorMostrar() {
        if (isPassword()) {
            return "••••••••";
        }
        return getValor();
    }

    /**
     * Obtiene ícono según el grupo
     */
    public String getIconoGrupo() {
        if (grupo.get() == null) return "📋";

        switch (grupo.get()) {
            case "RUTAS": return "📁";
            case "CERTIFICADO": return "🔐";
            case "SUNAT_AMBIENTE": return "🌐";
            case "SUNAT_CREDENCIALES_BETA": return "🔧";
            case "SUNAT_CREDENCIALES_PROD": return "✅";
            case "LIMITES": return "⚙️";
            case "IMPUESTOS": return "💰";
            default: return "📋";
        }
    }

}
