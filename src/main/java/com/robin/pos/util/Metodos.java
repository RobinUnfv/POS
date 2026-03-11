package com.robin.pos.util;

import com.google.gson.Gson;
import com.robin.pos.model.Arfact;
import com.robin.pos.model.DocumentoPago;
import com.robin.pos.model.EntidadTributaria;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.text.Text;

import javafx.scene.control.TableView;
import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
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

    public static String getTipoComprobante(String noFactu) {
        if (noFactu == null || noFactu.isEmpty()) {
            return "COMPROBANTE";
        }

        //char primerCaracter = noFactu.charAt(0);
        String dosCaracter = noFactu.substring(0, 2);
        switch (dosCaracter) {
            case "F0":
                return "FACTURA";
            case "B0":
                return "BOLETA";
            case "NV":
                return "NOTA DE VENTA";
            case "NC":
                return "NOTA DE CRÉDITO";
            case "ND":
                return "NOTA DE DÉBITO";
            default:
                return "COMPROBANTE";
        }
    }

    /**
     * Obtiene descripción del estado del comprobante
     */
    public static String getEstadoSunat(String codigo) {
        if (codigo == null) return "";

        return switch (codigo) {
            case "N" -> "NUEVO";
            case "B" -> "BLOQUEADO";
            case "E" -> "ENVIADO A SUNAT";
            case "X" -> "ERROR DE ENVIO";
            default -> codigo;
        };
    }

    public static String getTipoDocumentoCliente(String noFactu, String nocliente) {

        String tipoDocumento = getTipoComprobante(noFactu);

        if (tipoDocumento.equalsIgnoreCase("FACTURA")) {
            return "RUC";
        } else if (tipoDocumento.equalsIgnoreCase("BOLETA")) {
            char primerCaracter = nocliente.charAt(0);
            if (primerCaracter == '9') {
                return "SIN DOC.";
            } else {
                return "DNI";
            }
        } else {
            return "DOC.";
        }

    }

    public static List<DocumentoPago> getDocumentosPago() {
        List<DocumentoPago> documentosPagos = new ArrayList<>();
        documentosPagos.add(new DocumentoPago("B", "BOLETA"));
        documentosPagos.add(new DocumentoPago("F", "FACTURA"));
        documentosPagos.add(new DocumentoPago("C", "COTIZACIÓN"));
        documentosPagos.add(new DocumentoPago("NV", "NOTA DE VENTA"));

        return documentosPagos;
    }

    public static Arfact getTipoDocumento(String codDoc) {
        return switch (codDoc) {
            case "01" -> new Arfact("01", "01", "ORDENES DE COMPRA/SERVICIOS", "D");
            case "02" -> new Arfact("01", "02", "COMPROBANTES DE VENTA", "D");
            case "03" -> new Arfact("01", "03", "COMPROBANTES DE STOCK", "D");
            case "04" -> new Arfact("01", "04", "PEDIDOS", "D");
            case "05" -> new Arfact("01", "05", "GUIAS DE REMISION", "D");
            case "06" -> new Arfact("01", "06", "ORDENES DE PRODUCCION", "D");
            case "80" -> new Arfact("01", "80", "DOCS. INTERNOS", "D");
            case "90" -> new Arfact("01", "90", "DOCS. EXTERNOS", "D");
            case "99" -> new Arfact("01", "99", "OTROS", "D");
            default -> new Arfact();
        };
    }

    public static List<Arfact> getArfacts() {
        List<Arfact> arfacts = new ArrayList<>();
        arfacts.add(new Arfact("01", "01", "ORDENES DE COMPRA/SERVICIOS", "D"));
        arfacts.add(new Arfact("01", "02", "COMPROBANTES DE VENTA", "D"));
        arfacts.add(new Arfact("01", "03", "COMPROBANTES DE STOCK", "D"));
        arfacts.add(new Arfact("01", "04", "PEDIDOS", "D"));
        arfacts.add(new Arfact("01", "05", "GUIAS DE REMISION", "D"));
        arfacts.add(new Arfact("01", "06", "ORDENES DE PRODUCCION", "D"));
        arfacts.add(new Arfact("01", "80", "DOCS. INTERNOS", "D"));
        arfacts.add(new Arfact("01", "90", "DOCS. EXTERNOS", "D"));
        arfacts.add(new Arfact("01", "99", "OTROS", "D"));
        return arfacts;
    }

    public static String getTipoDocumentoSunat(String codDoc) {

            return switch (codDoc) {
                case "01" -> "F";
                case "03" -> "B";
                case "07" -> "NC";
                default -> "01";
            };
    }



}
