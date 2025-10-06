package com.robin.pos.util;

import com.robin.pos.model.Arinda1;

import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class AutoCompleteTextField<T> extends TextField {

    private ContextMenu suggestionMenu;
    private List<Arinda1> productos;
    private Arinda1 productoSeleccionado;
    private Consumer<Arinda1> onProductoSeleccionado;

    public AutoCompleteTextField() {
        super();
        this.suggestionMenu = new ContextMenu();
        this.productos = new ArrayList<>();

        setupEventHandlers();
        setPromptText("Buscar producto por código o descripción...");
    }

    private void setupEventHandlers() {
        // Listener para detectar cuando el usuario deja de escribir
        textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                suggestionMenu.hide();
                productoSeleccionado = null;
            }
        });

        // Manejar teclas especiales
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.DOWN && suggestionMenu.isShowing()) {
                suggestionMenu.getSkin().getNode().lookup(".menu-item").requestFocus();
                event.consume();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                suggestionMenu.hide();
            }
        });

        // Ocultar menú cuando pierde el foco
        focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                suggestionMenu.hide();
            }
        });
    }

    public void mostrarSugerencias(List<Arinda1> productosEncontrados) {
        productos.clear();
        suggestionMenu.getItems().clear();

        if (productosEncontrados == null || productosEncontrados.isEmpty()) {
            suggestionMenu.hide();
            return;
        }

        productos.addAll(productosEncontrados);

        for (Arinda1 producto : productosEncontrados) {
            VBox itemBox = new VBox(2);
            itemBox.setStyle("-fx-padding: 5px;");

            Label lblCodigo = new Label(producto.getCodigo());
            lblCodigo.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

            Label lblDescripcion = new Label(producto.getDescripcion());
            lblDescripcion.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

            itemBox.getChildren().addAll(lblCodigo, lblDescripcion);

            CustomMenuItem item = new CustomMenuItem(itemBox, true);
            item.setOnAction(e -> seleccionarProducto(producto));

            suggestionMenu.getItems().add(item);
        }

        if (!suggestionMenu.isShowing()) {
            suggestionMenu.show(this, Side.BOTTOM, 0, 0);
        }
    }

    private void seleccionarProducto(Arinda1 producto) {
        this.productoSeleccionado = producto;
        setText(producto.toString());
        positionCaret(getText().length());
        suggestionMenu.hide();

        // Notificar al listener si existe
        if (onProductoSeleccionado != null) {
            onProductoSeleccionado.accept(producto);
        }
    }

    public Arinda1 getProductoSeleccionado() {
        return productoSeleccionado;
    }

    public void setProducto(Arinda1 producto) {
        this.productoSeleccionado = producto;
        if (producto != null) {
            setText(producto.toString());
        }
    }

    public void setOnProductoSeleccionado(Consumer<Arinda1> callback) {
        this.onProductoSeleccionado = callback;
    }

    @Override
    public void clear() {
        super.clear();
        productoSeleccionado = null;
        productos.clear();
        suggestionMenu.hide();
    }

}
