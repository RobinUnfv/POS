package com.robin.pos.model;

public class Arccmc {
    private String noCliente;
    private String nombre;
    private String tipoCliente;
    private String activo;

    public Arccmc(String noCliente, String nombre, String tipoCliente, String activo) {
        this.noCliente = noCliente;
        this.nombre = nombre;
        this.tipoCliente = tipoCliente;
        this.activo = activo;
    }

    public Arccmc(){}

    public String getNoCliente() {
        return noCliente;
    }

    public void setNoCliente(String noCliente) {
        this.noCliente = noCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
    }

    public String getActivo() {
        return activo;
    }

    public void setActivo(String activo) {
        this.activo = activo;
    }
}
