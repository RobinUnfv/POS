package com.robin.pos.model;

public class Arfact {
    private String noCia;
    private String tipo;
    private String descripcion;
    private String tipoMov;

    public Arfact() {
    }

    public Arfact(String noCia, String tipo, String descripcion, String tipoMov) {
        this.noCia = noCia;
        this.tipo = tipo;
        this.descripcion = descripcion;
        this.tipoMov = tipoMov;
    }

    public String getNoCia() {
        return noCia;
    }

    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipoMov() {
        return tipoMov;
    }

    public void setTipoMov(String tipoMov) {
        this.tipoMov = tipoMov;
    }

}
