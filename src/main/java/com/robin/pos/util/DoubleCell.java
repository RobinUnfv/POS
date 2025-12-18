package com.robin.pos.util;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoubleCell<T> extends TableCell<T, Double> {

    private final TextField textField;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();


    public DoubleCell() {


        this.textField = new TextField();

        textField.setOnAction(e ->{
            try {
                String txt = textField.getText();
                if (txt == null || txt.trim().isEmpty()) {
                    commitEdit(null);
                } else {
                    commitEdit(numberFormat.parse(txt).doubleValue());
                }
            } catch (ParseException ex) {
                Logger.getLogger(DoubleCell.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        textField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });

        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
    }

    @Override
    protected void updateItem(Double item, boolean empty) {
        super.updateItem(item, empty);
        // Proteger contra null y celda vacía
        if (empty) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else if (isEditing()) {
            // Si está en edición, mostrar el texto en el TextField (formateado si es Number)
            Object raw = getItem();
            textField.setText(formatNumber(raw) == null ? "" : formatNumber(raw));
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            // Mostrar formato numérico si hay valor
            Object raw = getItem();
            setText(formatNumber(raw));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        // Evitar formatear null o valores no numéricos
        Object raw = getItem();
        textField.setText(formatNumber(raw) == null ? "" : formatNumber(raw));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        // Manejar null o valores no numéricos al cancelar edición
        Object raw = getItem();
        setText(formatNumber(raw));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(Double newValue) {
        // Asegurarse de no provocar formateo de null y delegar
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        if (getTableView() != null) {
            getTableView().requestFocus();
        }
    }

    private String formatNumber(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Number) {
            return numberFormat.format(((Number) obj).doubleValue());
        }
        // Si no es Number, devolver su toString() para evitar IllegalArgumentException
        return obj.toString();
    }

}
