package com.robin.pos.util;

import javafx.scene.control.TableColumn;
import javafx.scene.text.Text;

import javafx.scene.control.TableView;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Random;

public class Metodos {

    public static void changeSizeOnColumn(TableColumn tc, TableView table, int row) {
        try {
            Text title = new Text(tc.getText());

            double ancho = title.getLayoutBounds().getWidth()+50;

            Object value = null;
            for (int i = ((row==-1)?0:row); i < ((row==-1)?table.getItems().size():(row+1) ); i++) {
                value = tc.getCellData(i);

                if(value instanceof Double){
                    title = new Text((value == null) ?"": NumberFormat.getCurrencyInstance().format(value));
                }else if(value instanceof String){
                    title = new Text((value == null) ?"":value.toString());
                }

                if (title.getLayoutBounds().getWidth() > ancho) {
                    ancho = title.getLayoutBounds().getWidth() + 140;
                }
            }
            tc.setPrefWidth(ancho);
        } catch (HeadlessException ex) {
            System.err.println(ex);
        }
    }

    public static String generarTextoAleatorio(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            int index = random.nextInt(caracteres.length());
            sb.append(caracteres.charAt(index));
        }
        return sb.toString();
    }

}
