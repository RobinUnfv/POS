package com.robin.pos.model;

/**
 * Modelo para la tabla FACTU.ARFACC
 * Representa las series y correlativos de documentos
 *
 * @author Robin POS
 * @version 1.0
 */
public class Arfacc {

    private String noCia;
    private String centro;
    private String tipoDoc;
    private String descripcion;
    private String serie;
    private int consDesde;
    private int lineas;
    private String indControlAuto;
    private String activo;
    private String noCaba;

    public Arfacc() {
    }

    public Arfacc(String noCia, String centro, String tipoDoc, String serie, int consDesde,
                  int lineas, String indControlAuto, String activo, String noCaba) {
        this.noCia = noCia;
        this.centro = centro;
        this.tipoDoc = tipoDoc;
        this.serie = serie;
        this.consDesde = consDesde;
        this.lineas = lineas;
        this.indControlAuto = indControlAuto;
        this.activo = activo;
        this.noCaba = noCaba;
    }

    public Arfacc(String noCia, String centro, String tipoDoc, String descripcion, String serie, int consDesde,
                  int lineas, String indControlAuto, String activo, String noCaba) {
        this.noCia = noCia;
        this.centro = centro;
        this.tipoDoc = tipoDoc;
        this.descripcion = descripcion;
        this.serie = serie;
        this.consDesde = consDesde;
        this.lineas = lineas;
        this.indControlAuto = indControlAuto;
        this.activo = activo;
        this.noCaba = noCaba;
    }

    // Getters y Setters

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getCentro() {
        return centro;
    }

    public void setCentro(String centro) {
        this.centro = centro;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getSerie() {
        return serie;
    }

    public void setSerie(String serie) {
        this.serie = serie;
    }

    public int getConsDesde() {
        return consDesde;
    }

    public void setConsDesde(int consDesde) {
        this.consDesde = consDesde;
    }

    public int getLineas() {
        return lineas;
    }

    public void setLineas(int lineas) {
        this.lineas = lineas;
    }

    public String getIndControlAuto() {
        return indControlAuto;
    }

    public void setIndControlAuto(String indControlAuto) {
        this.indControlAuto = indControlAuto;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }

    public String getNoCaba() {
        return noCaba;
    }

    public void setNoCaba(String noCaba) {
        this.noCaba = noCaba;
    }

    /**
     * Obtiene la descripción del control automático
     */
    public String getControlAutoDescripcion() {
        if (indControlAuto == null) return "";
        return "S".equals(indControlAuto) ? "Sí" : "No";
    }

    /**
     * Obtiene la descripción del estado activo
     */
    public String getActivoDescripcion() {
        if (activo == null) return "";
        return "S".equals(activo) ? "Activo" : "Inactivo";
    }

    @Override
    public String toString() {
        return "Arfacc{" +
                "noCia='" + noCia + '\'' +
                ", centro='" + centro + '\'' +
                ", tipoDoc='" + tipoDoc + '\'' +
                ", serie='" + serie + '\'' +
                ", consDesde=" + consDesde +
                ", lineas=" + lineas +
                ", indControlAuto='" + indControlAuto + '\'' +
                ", activo='" + activo + '\'' +
                ", noCaba='" + noCaba + '\'' +
                '}';
    }
}