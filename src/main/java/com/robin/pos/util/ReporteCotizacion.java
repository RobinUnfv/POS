package com.robin.pos.util;

import java.util.logging.Logger;

public class ReporteCotizacion {
    private static final Logger LOGGER = Logger.getLogger(ReporteCotizacion.class.getName());

    // Rutas de archivos
    private static final String JRXML_PATH = "/com/robin/pos/reportes/cotizacion.jrxml";
    private static final String JASPER_PATH = "/com/robin/pos/reportes/cotizacion.jasper";
    private static final String LOGO_PATH = "/com/robin/pos/imagenes/logos-nexer.png";

    // Datos de la empresa (configurables)
    private String empresaNombre = "CORPORACION TEXTIL CELIA E.I.R.L.";
    private String empresaTagline = "Your Business Partner";
    private String empresaTelefono = "1321 - 456 - 78860";
    private String empresaDireccion = "123 Anywhere St., Any City, ST 12345";
    private String empresaEmail = "thynkagencia@com";

    // Método de pago predeterminado
    private String metodoPago = "Banco de Crédito del Perú (BCP)";
    private String numeroCuenta = "1234";
    private String numeroCuentaCompleto = "CCI: 123-4567-90";

}
