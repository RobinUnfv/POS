package com.robin.pos.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class EntidadTributaria {
    private final StringProperty nombre;
    private final StringProperty tipoDocumento;
    private final StringProperty numeroDocumento;
    private final StringProperty estado;
    private final StringProperty condicion;
    private final StringProperty direccion;
    private final StringProperty ubigeo;
    private final StringProperty viaTipo;
    private final StringProperty viaNombre;
    private final StringProperty zonaCodigo;
    private final StringProperty zonaTipo;
    private final StringProperty numero;
    private final StringProperty interior;
    private final StringProperty lote;
    private final StringProperty dpto;
    private final StringProperty manzana;
    private final StringProperty kilometro;
    private final StringProperty distrito;
    private final StringProperty provincia;
    private final StringProperty departamento;

    public EntidadTributaria() {
        this.nombre = new SimpleStringProperty();
        this.tipoDocumento = new SimpleStringProperty();
        this.numeroDocumento = new SimpleStringProperty();
        this.estado = new SimpleStringProperty();
        this.condicion = new SimpleStringProperty();
        this.direccion = new SimpleStringProperty();
        this.ubigeo = new SimpleStringProperty();
        this.viaTipo = new SimpleStringProperty();
        this.viaNombre = new SimpleStringProperty();
        this.zonaCodigo = new SimpleStringProperty();
        this.zonaTipo = new SimpleStringProperty();
        this.numero = new SimpleStringProperty();
        this.interior = new SimpleStringProperty();
        this.lote = new SimpleStringProperty();
        this.dpto = new SimpleStringProperty();
        this.manzana = new SimpleStringProperty();
        this.kilometro = new SimpleStringProperty();
        this.distrito = new SimpleStringProperty();
        this.provincia = new SimpleStringProperty();
        this.departamento = new SimpleStringProperty();
    }

    // Constructor completo
    public EntidadTributaria(String nombre, String tipoDocumento, String numeroDocumento,
                             String estado, String condicion, String direccion, String ubigeo,
                             String viaTipo, String viaNombre, String zonaCodigo, String zonaTipo,
                             String numero, String interior, String lote, String dpto,
                             String manzana, String kilometro, String distrito,
                             String provincia, String departamento) {
        this.nombre = new SimpleStringProperty(nombre);
        this.tipoDocumento = new SimpleStringProperty(tipoDocumento);
        this.numeroDocumento = new SimpleStringProperty(numeroDocumento);
        this.estado = new SimpleStringProperty(estado);
        this.condicion = new SimpleStringProperty(condicion);
        this.direccion = new SimpleStringProperty(direccion);
        this.ubigeo = new SimpleStringProperty(ubigeo);
        this.viaTipo = new SimpleStringProperty(viaTipo);
        this.viaNombre = new SimpleStringProperty(viaNombre);
        this.zonaCodigo = new SimpleStringProperty(zonaCodigo);
        this.zonaTipo = new SimpleStringProperty(zonaTipo);
        this.numero = new SimpleStringProperty(numero);
        this.interior = new SimpleStringProperty(interior);
        this.lote = new SimpleStringProperty(lote);
        this.dpto = new SimpleStringProperty(dpto);
        this.manzana = new SimpleStringProperty(manzana);
        this.kilometro = new SimpleStringProperty(kilometro);
        this.distrito = new SimpleStringProperty(distrito);
        this.provincia = new SimpleStringProperty(provincia);
        this.departamento = new SimpleStringProperty(departamento);
    }

    // Getters y Setters para propiedades JavaFX
    public String getNombre() { return nombre.get(); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public StringProperty nombreProperty() { return nombre; }

    public String getTipoDocumento() { return tipoDocumento.get(); }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento.set(tipoDocumento); }
    public StringProperty tipoDocumentoProperty() { return tipoDocumento; }

    public String getNumeroDocumento() { return numeroDocumento.get(); }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento.set(numeroDocumento); }
    public StringProperty numeroDocumentoProperty() { return numeroDocumento; }

    public String getEstado() { return estado.get(); }
    public void setEstado(String estado) { this.estado.set(estado); }
    public StringProperty estadoProperty() { return estado; }

    public String getCondicion() { return condicion.get(); }
    public void setCondicion(String condicion) { this.condicion.set(condicion); }
    public StringProperty condicionProperty() { return condicion; }

    public String getDireccion() { return direccion.get(); }
    public void setDireccion(String direccion) { this.direccion.set(direccion); }
    public StringProperty direccionProperty() { return direccion; }

    public String getUbigeo() { return ubigeo.get(); }
    public void setUbigeo(String ubigeo) { this.ubigeo.set(ubigeo); }
    public StringProperty ubigeoProperty() { return ubigeo; }

    public String getViaTipo() { return viaTipo.get(); }
    public void setViaTipo(String viaTipo) { this.viaTipo.set(viaTipo); }
    public StringProperty viaTipoProperty() { return viaTipo; }

    public String getViaNombre() { return viaNombre.get(); }
    public void setViaNombre(String viaNombre) { this.viaNombre.set(viaNombre); }
    public StringProperty viaNombreProperty() { return viaNombre; }

    public String getZonaCodigo() { return zonaCodigo.get(); }
    public void setZonaCodigo(String zonaCodigo) { this.zonaCodigo.set(zonaCodigo); }
    public StringProperty zonaCodigoProperty() { return zonaCodigo; }

    public String getZonaTipo() { return zonaTipo.get(); }
    public void setZonaTipo(String zonaTipo) { this.zonaTipo.set(zonaTipo); }
    public StringProperty zonaTipoProperty() { return zonaTipo; }

    public String getNumero() { return numero.get(); }
    public void setNumero(String numero) { this.numero.set(numero); }
    public StringProperty numeroProperty() { return numero; }

    public String getInterior() { return interior.get(); }
    public void setInterior(String interior) { this.interior.set(interior); }
    public StringProperty interiorProperty() { return interior; }

    public String getLote() { return lote.get(); }
    public void setLote(String lote) { this.lote.set(lote); }
    public StringProperty loteProperty() { return lote; }

    public String getDpto() { return dpto.get(); }
    public void setDpto(String dpto) { this.dpto.set(dpto); }
    public StringProperty dptoProperty() { return dpto; }

    public String getManzana() { return manzana.get(); }
    public void setManzana(String manzana) { this.manzana.set(manzana); }
    public StringProperty manzanaProperty() { return manzana; }

    public String getKilometro() { return kilometro.get(); }
    public void setKilometro(String kilometro) { this.kilometro.set(kilometro); }
    public StringProperty kilometroProperty() { return kilometro; }

    public String getDistrito() { return distrito.get(); }
    public void setDistrito(String distrito) { this.distrito.set(distrito); }
    public StringProperty distritoProperty() { return distrito; }

    public String getProvincia() { return provincia.get(); }
    public void setProvincia(String provincia) { this.provincia.set(provincia); }
    public StringProperty provinciaProperty() { return provincia; }

    public String getDepartamento() { return departamento.get(); }
    public void setDepartamento(String departamento) { this.departamento.set(departamento); }
    public StringProperty departamentoProperty() { return departamento; }

    @Override
    public String toString() {
        return "EntidadTributaria{" +
                "nombre=" + nombre.get() +
                ", numeroDocumento=" + numeroDocumento.get() +
                ", estado=" + estado.get() +
                ", condicion=" + condicion.get() +
                '}';
    }
}
