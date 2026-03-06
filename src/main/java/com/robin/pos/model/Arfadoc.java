package com.robin.pos.model;

/**
 * Modelo para la tabla FACTU.ARFADOC
 * Representa los tipos de documentos del sistema
 *
 * @author Robin POS
 * @version 1.0
 */
public class Arfadoc {

    private String noCia;
    private String codDoc;
    private String descripcion;
    private String tipo;
    private String estado;
    private String codSunat;

    public Arfadoc() {
    }

    public Arfadoc(String noCia, String codDoc, String descripcion, String tipo, String estado, String codSunat) {
        this.noCia = noCia;
        this.codDoc = codDoc;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.estado = estado;
        this.codSunat = codSunat;
    }

    // Getters y Setters

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getCodDoc() {
        return codDoc;
    }

    public void setCodDoc(String codDoc) {
        this.codDoc = codDoc;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCodSunat() {
        return codSunat;
    }

    public void setCodSunat(String codSunat) {
        this.codSunat = codSunat;
    }

    /**
     * Obtiene la descripción del estado
     */
    public String getEstadoDescripcion() {
        if (estado == null) return "";
        return switch (estado) {
            case "A" -> "Activo";
            case "I" -> "Inactivo";
            default -> estado;
        };
    }

    @Override
    public String toString() {
        return "Arfadoc{" +
                "noCia='" + noCia + '\'' +
                ", codDoc='" + codDoc + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", tipo='" + tipo + '\'' +
                ", estado='" + estado + '\'' +
                ", codSunat='" + codSunat + '\'' +
                '}';
    }
}
