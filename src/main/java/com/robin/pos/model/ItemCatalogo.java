package com.robin.pos.model;

import java.util.Objects;

/**
 * Elemento genérico de catálogo SUNAT (código + descripción) para ComboBox.
 * Ej.: Cat. 20 motivo de traslado, Cat. 06 tipo de documento, Cat. 61 doc. relacionado.
 */
public class ItemCatalogo {

    private final String codigo;
    private final String descripcion;

    public ItemCatalogo(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return codigo + " - " + descripcion;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemCatalogo other && Objects.equals(codigo, other.codigo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(codigo);
    }
}
