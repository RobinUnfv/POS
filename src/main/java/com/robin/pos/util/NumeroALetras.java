package com.robin.pos.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Clase utilitaria para convertir números a letras
 * Usado para mostrar el monto en letras en los comprobantes de pago
 *
 * Ejemplo: 661.00 -> "SEISCIENTOS SESENTIUNO Y 00/100 SOLES"
 *
 * @author Robin
 */
public class NumeroALetras {

    private static final String[] UNIDADES = {
            "", "UNO", "DOS", "TRES", "CUATRO", "CINCO",
            "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
            "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE",
            "DIECISEIS", "DIECISIETE", "DIECIOCHO", "DIECINUEVE", "VEINTE"
    };

    private static final String[] DECENAS = {
            "", "", "VEINTI", "TREINTA", "CUARENTA", "CINCUENTA",
            "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };

    private static final String[] CENTENAS = {
            "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
            "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    /**
     * Convierte un número a su representación en letras
     *
     * @param numero El número a convertir
     * @param moneda La moneda (SOL, SOLES, USD, DOLARES)
     * @return El número en letras con la moneda
     */
    public static String convertir(double numero, String moneda) {
        if (numero < 0) {
            return "MENOS " + convertir(-numero, moneda);
        }

        if (numero == 0) {
            return "CERO " + obtenerMonedaPlural(moneda);
        }

        // Separar parte entera y decimal
        BigDecimal bd = BigDecimal.valueOf(numero).setScale(2, RoundingMode.HALF_UP);
        long parteEntera = bd.longValue();
        int parteDecimal = bd.remainder(BigDecimal.ONE)
                .multiply(BigDecimal.valueOf(100))
                .intValue();

        // Convertir parte entera
        String letras = convertirEntero(parteEntera);

        // Formatear decimales
        String decimales = String.format("%02d", parteDecimal);

        // Obtener nombre de la moneda
        String nombreMoneda = parteEntera == 1 ?
                obtenerMonedaSingular(moneda) :
                obtenerMonedaPlural(moneda);

        return letras + " Y " + decimales + "/100 " + nombreMoneda;
    }

    /**
     * Convierte un número entero a letras
     */
    private static String convertirEntero(long numero) {
        if (numero == 0) {
            return "CERO";
        }

        if (numero < 0) {
            return "MENOS " + convertirEntero(-numero);
        }

        StringBuilder resultado = new StringBuilder();

        // Millones
        if (numero >= 1000000) {
            long millones = numero / 1000000;
            if (millones == 1) {
                resultado.append("UN MILLON ");
            } else {
                resultado.append(convertirEntero(millones)).append(" MILLONES ");
            }
            numero %= 1000000;
        }

        // Miles
        if (numero >= 1000) {
            long miles = numero / 1000;
            if (miles == 1) {
                resultado.append("MIL ");
            } else {
                resultado.append(convertirEntero(miles)).append(" MIL ");
            }
            numero %= 1000;
        }

        // Centenas
        if (numero >= 100) {
            if (numero == 100) {
                resultado.append("CIEN ");
            } else {
                resultado.append(CENTENAS[(int)(numero / 100)]).append(" ");
            }
            numero %= 100;
        }

        // Decenas y unidades
        if (numero > 0) {
            if (numero <= 20) {
                resultado.append(UNIDADES[(int)numero]);
            } else if (numero < 30) {
                resultado.append("VEINTI").append(UNIDADES[(int)(numero % 10)]);
            } else {
                int decena = (int)(numero / 10);
                int unidad = (int)(numero % 10);
                resultado.append(DECENAS[decena]);
                if (unidad > 0) {
                    resultado.append(" Y ").append(UNIDADES[unidad]);
                }
            }
        }

        return resultado.toString().trim();
    }

    /**
     * Obtiene el nombre de la moneda en singular
     */
    private static String obtenerMonedaSingular(String moneda) {
        if (moneda == null) return "SOL";

        switch (moneda.toUpperCase()) {
            case "SOL":
            case "SOLES":
            case "PEN":
                return "SOL";
            case "USD":
            case "DOLAR":
            case "DOLARES":
                return "DOLAR AMERICANO";
            case "EUR":
            case "EURO":
            case "EUROS":
                return "EURO";
            default:
                return moneda.toUpperCase();
        }
    }

    /**
     * Obtiene el nombre de la moneda en plural
     */
    private static String obtenerMonedaPlural(String moneda) {
        if (moneda == null) return "SOLES";

        switch (moneda.toUpperCase()) {
            case "SOL":
            case "SOLES":
            case "PEN":
                return "SOLES";
            case "USD":
            case "DOLAR":
            case "DOLARES":
                return "DOLARES AMERICANOS";
            case "EUR":
            case "EURO":
            case "EUROS":
                return "EUROS";
            default:
                return moneda.toUpperCase();
        }
    }


}
