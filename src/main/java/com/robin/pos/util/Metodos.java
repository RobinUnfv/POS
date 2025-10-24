package com.robin.pos.util;

import com.google.gson.Gson;
import com.robin.pos.model.EntidadTributaria;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;

import javafx.scene.control.TableView;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Random;
import java.util.function.UnaryOperator;

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

    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    public static EntidadTributaria convertirJson(String json) {
        Gson gson = new Gson();
        Map<String, Object> map = gson.fromJson(json, Map.class);

        EntidadTributaria entidad = new EntidadTributaria();

        // Asignar valores desde el mapa
        entidad.setNombre(getStringValue(map, "nombre"));
        entidad.setTipoDocumento(getStringValue(map, "tipoDocumento"));
        entidad.setNumeroDocumento(getStringValue(map, "numeroDocumento"));
        entidad.setEstado(getStringValue(map, "estado"));
        entidad.setCondicion(getStringValue(map, "condicion"));
        entidad.setDireccion(getStringValue(map, "direccion"));
        entidad.setUbigeo(getStringValue(map, "ubigeo"));
        entidad.setViaTipo(getStringValue(map, "viaTipo"));
        entidad.setViaNombre(getStringValue(map, "viaNombre"));
        entidad.setZonaCodigo(getStringValue(map, "zonaCodigo"));
        entidad.setZonaTipo(getStringValue(map, "zonaTipo"));
        entidad.setNumero(getStringValue(map, "numero"));
        entidad.setInterior(getStringValue(map, "interior"));
        entidad.setLote(getStringValue(map, "lote"));
        entidad.setDpto(getStringValue(map, "dpto"));
        entidad.setManzana(getStringValue(map, "manzana"));
        entidad.setKilometro(getStringValue(map, "kilometro"));
        entidad.setDistrito(getStringValue(map, "distrito"));
        entidad.setProvincia(getStringValue(map, "provincia"));
        entidad.setDepartamento(getStringValue(map, "departamento"));

        return entidad;
    }

    public static void configuracionNumeroDocumento(TextField textField, String tipoDocumento) {
        UnaryOperator<TextFormatter.Change> filter;
        if (tipoDocumento.equals("RUC")) {
            filter = change -> {
                String newText = change.getControlNewText();
                // Permite solo dígitos y máximo 11 caracteres
                if (newText.matches("\\d{0,11}")) {
//                    txtNumeroDocumento.setStyle("-fx-border-color: green; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5;");
                    return change;
                }
                return null; // Rechaza el cambio si no cumple
            };
        } else {
            filter = change -> {
                String newText = change.getControlNewText();
                // Permite solo dígitos y máximo 8 caracteres
                if (newText.matches("\\d{0,8}")) {
                    return change;
                }
//                txtNumeroDocumento.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                return null;
            };
        }
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

}
