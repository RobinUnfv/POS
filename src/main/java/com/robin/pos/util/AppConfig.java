package com.robin.pos.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Configuración centralizada de la aplicación.
 *
 * Orden de carga (cada nivel sobrescribe al anterior):
 *   1. classpath:/config.properties            (valores por defecto empaquetados)
 *   2. ${user.home}/.robinpos/config.properties (configuración por equipo)
 *   3. ./config.properties                      (junto al .jar)
 *   4. Propiedades de sistema: -Ddb.sid=DICOFE  (útil para pruebas)
 *
 * Así cada instalación (CASA ROBIN, NEXER, CELIA) usa su propia BD sin
 * recompilar ni comentar/descomentar líneas en el código.
 *
 * @author Robin POS
 */
public final class AppConfig {

    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final Properties PROPS = new Properties();

    static {
        cargarClasspath("/config.properties");
        cargarArchivo(Paths.get(System.getProperty("user.home"), ".robinpos", "config.properties"));
        cargarArchivo(Paths.get("config.properties"));
        // Las propiedades de sistema tienen la última palabra
        System.getProperties().forEach((k, v) -> {
            String key = String.valueOf(k);
            if (key.startsWith("db.") || key.startsWith("empresa.") || key.startsWith("app.")) {
                PROPS.setProperty(key, String.valueOf(v));
            }
        });
    }

    private AppConfig() {
    }

    private static void cargarClasspath(String recurso) {
        try (InputStream in = AppConfig.class.getResourceAsStream(recurso)) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer " + recurso, e);
        }
    }

    private static void cargarArchivo(Path ruta) {
        if (!Files.isRegularFile(ruta)) {
            return;
        }
        try (InputStream in = Files.newInputStream(ruta)) {
            PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            LOGGER.info("Configuración cargada desde " + ruta.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "No se pudo leer " + ruta, e);
        }
    }

    public static String get(String clave, String porDefecto) {
        String valor = PROPS.getProperty(clave);
        return valor == null || valor.isBlank() ? porDefecto : valor.trim();
    }

    public static String get(String clave) {
        return get(clave, null);
    }

    public static int getInt(String clave, int porDefecto) {
        try {
            return Integer.parseInt(get(clave, String.valueOf(porDefecto)));
        } catch (NumberFormatException e) {
            return porDefecto;
        }
    }

    // ===== Atajos para los datos de la empresa emisora =====

    public static String empresaRuc() {
        return get("empresa.ruc", "20609272016");
    }

    public static String empresaRazonSocial() {
        return get("empresa.razonSocial", "CORPORACION TEXTIL CELIA E.I.R.L.");
    }

    public static String empresaDireccion() {
        return get("empresa.direccion", "JR. MARISCAL AGUSTIN GAMARRA 676 INT. 262 URB. EL PORVENIR - LA VICTORIA - LIMA - LIMA");
    }

    public static String empresaUbigeo() {
        return get("empresa.ubigeo", "150115");
    }

    public static String empresaDescripcion() {
        return get("empresa.descripcion", "VENTA EXCLUSIVA DE CHOMPAS PARA DAMAS, CABALLEROS Y NIÑOS");
    }

    public static String empresaTelefonos() {
        return get("empresa.telefonos", "");
    }

    public static String empresaEmail() {
        return get("empresa.email", "");
    }

    public static String noCia() {
        return get("app.noCia", "01");
    }
}
