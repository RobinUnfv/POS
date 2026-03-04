package com.robin.pos.model;

public class DocumentoPago {
    private String codigo;
    private String descripcion;

    public  DocumentoPago() {}

    public DocumentoPago(String codigo, String descripcion) {
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

}
