package com.robin.pos.util;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoubleCell<T, S> extends TableCell<T, Double> {

    private final TextField textField;

    private final NumberFormat numberFormat = NumberFormat.getNumberInstance();
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");


    public DoubleCell() {


        this.textField = new TextField();

        textField.setOnAction(e ->{
            try {
                commitEdit(numberFormat.parse(textField.getText()).doubleValue());
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
        if (empty) {
            setText(null);
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        } else if (isEditing()) {
            textField.setText(item.toString());
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setText(numberFormat.format(item));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        textField.setText(numberFormat.format(getItem()));
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.requestFocus();
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(numberFormat.format(getItem()));
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(Double newValue) {
        super.commitEdit(newValue);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        getTableView().requestFocus();
    }

}
