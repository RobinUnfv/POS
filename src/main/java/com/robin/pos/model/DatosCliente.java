package com.robin.pos.model;

public class DatosCliente {
    private String nombre;
    private String numeroDocumento;
    private String tipoDocumento;
    private String direccion;

    public DatosCliente() {}

    public DatosCliente(String nombre, String numeroDocumento, String tipoDocumento, String direccion) {
        this.nombre = nombre;
        this.numeroDocumento = numeroDocumento;
        this.tipoDocumento = tipoDocumento;
        this.direccion = direccion;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
