package com.robin.pos.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.function.Function;

public class AutoCompleteTextField<T> extends TextField {

    private final Popup popup;
    private final ListView<T> listView;
    private final ObservableList<T> data;
    private final ObjectProperty<Function<String, ObservableList<T>>> suggestionProvider;
    private Timeline delayTimeline;

    public AutoCompleteTextField() {
        super();

        this.data = FXCollections.observableArrayList();
        this.suggestionProvider = new SimpleObjectProperty<>();

        // Configurar el popup
        this.popup = new Popup();
        this.popup.setAutoHide(true);
        this.popup.setHideOnEscape(true);

        // Configurar el ListView
        this.listView = new ListView<>(data);
        this.listView.setPrefWidth(300);
        this.listView.setPrefHeight(200);

        popup.getContent().add(listView);

        // Configurar el delay para las búsquedas
        this.delayTimeline = new Timeline(new KeyFrame(Duration.millis(300)));

        // Configurar eventos
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        // Evento para mostrar sugerencias cuando se escribe
        textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                hidePopup();
                return;
            }

            if (suggestionProvider.get() != null) {
                // Cancelar búsqueda anterior
                delayTimeline.stop();

                // Programar nueva búsqueda después del delay
                delayTimeline.getKeyFrames().setAll(new KeyFrame(Duration.millis(300), e -> {
                    ObservableList<T> suggestions = suggestionProvider.get().apply(newValue);
                    data.setAll(suggestions);

                    if (!suggestions.isEmpty()) {
                        showPopup();
                    } else {
                        hidePopup();
                    }
                }));
                delayTimeline.play();
            }
        });

        // Eventos del teclado
        setOnKeyPressed(this::handleKeyPressed);

        // Evento cuando se selecciona un item
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                selectItem();
            }
        });
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.DOWN) {
            if (!popup.isShowing()) {
                showPopup();
            }
            listView.requestFocus();
            if (!listView.getItems().isEmpty()) {
                listView.getSelectionModel().select(0);
            }
        } else if (event.getCode() == KeyCode.ENTER) {
            if (popup.isShowing() && !listView.getSelectionModel().isEmpty()) {
                selectItem();
            }
        } else if (event.getCode() == KeyCode.ESCAPE) {
            hidePopup();
        }
    }

    private void showPopup() {
        if (data.isEmpty()) return;

        if (!popup.isShowing()) {
            popup.show(this,
                    this.getScene().getWindow().getX() + this.localToScene(0, 0).getX() + this.getScene().getX(),
                    this.getScene().getWindow().getY() + this.localToScene(0, this.getHeight()).getY() + this.getScene().getY()
            );
        }
    }

    private void hidePopup() {
        popup.hide();
    }

    private void selectItem() {
        T selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            setText(selectedItem.toString());
            hidePopup();

            // Disparar evento de selección
            fireEvent(new ActionEvent());
        }
    }

    // Propiedades

    public Function<String, ObservableList<T>> getSuggestionProvider() {
        return suggestionProvider.get();
    }

    public void setSuggestionProvider(Function<String, ObservableList<T>> suggestionProvider) {
        this.suggestionProvider.set(suggestionProvider);
    }

    public ObjectProperty<Function<String, ObservableList<T>>> suggestionProviderProperty() {
        return suggestionProvider;
    }

    public T getSelectedItem() {
        return listView.getSelectionModel().getSelectedItem();
    }

    // Método para limpiar la selección
    public void clearSelection() {
        listView.getSelectionModel().clearSelection();
        hidePopup();
    }
}
