package com.robin.pos.model;

public class Arccmc {
    private String noCliente;
    private String nombre;
    private String tipoCliente;
    private String tipoPersona;
    private String activo;

    public Arccmc(String noCliente, String nombre, String tipoCliente, String tipoPersona, String activo) {
        this.noCliente = noCliente;
        this.nombre = nombre;
        this.tipoCliente = tipoCliente;
        this.tipoPersona = tipoPersona;
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

    public String getTipoPersona() {
        return tipoPersona;
    }

    public void setTipoPersona(String tipoPersona) {
        this.tipoPersona = tipoPersona;
    }

    @Override
    public String toString() {
        return "Arccmc{" +
                "noCliente='" + noCliente + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipoCliente='" + tipoCliente + '\'' +
                ", tipoPersona='" + tipoPersona + '\'' +
                ", activo='" + activo + '\'' +
                '}';
    }
}
