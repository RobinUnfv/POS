package com.robin.pos.model;

public class Arinda1 {
    private String codigo;
    private String descripcion;

    public Arinda1() {
    }
    public Arinda1(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return codigo + " - " + descripcion;
    }
}
