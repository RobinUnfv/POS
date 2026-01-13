package com.robin.pos.model;

/**
 * Modelo para Unidades de Medida
 * Tabla: INVE.ARINUM
 *
 * @author Robin POS
 * @version 1.0
 */
public class Arinum {

    private String noCia;
    private String unidad;
    private String nom;
    private String estado;
    private String codSunat;

    public Arinum() {
    }

    public Arinum(String unidad, String nom) {
        this.unidad = unidad;
        this.nom = nom;
    }

    public Arinum(String noCia, String unidad, String nom, String estado, String codSunat) {
        this.noCia = noCia;
        this.unidad = unidad;
        this.nom = nom;
        this.estado = estado;
        this.codSunat = codSunat;
    }

    // ==================== GETTERS Y SETTERS ====================

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
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
     * Verifica si la unidad está activa
     */
    public boolean isActivo() {
        return "A".equalsIgnoreCase(estado);
    }

    /**
     * Retorna el nombre para mostrar en ComboBox
     * Formato: "UNIDAD - NOMBRE"
     */
    public String getNombreCompleto() {
        return unidad + " - " + nom;
    }

    /**
     * Para mostrar en ComboBox (solo el nombre)
     */
    @Override
    public String toString() {
        return nom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arinum arinum = (Arinum) o;
        return unidad != null && unidad.equals(arinum.unidad);
    }

    @Override
    public int hashCode() {
        return unidad != null ? unidad.hashCode() : 0;
    }
}
