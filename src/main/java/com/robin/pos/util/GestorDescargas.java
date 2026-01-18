package com.robin.pos.util;

import java.io.File;

public class GestorDescargas {
    public static String getCarpetaDescargas() {
        String carpetaDescargas = null;
        String sistemaOperativo = System.getProperty("os.name").toLowerCase();
        String usuario = System.getProperty("user.name");

        if (sistemaOperativo.contains("win")) {
            // Windows
            carpetaDescargas = obtenerCarpetaDescargasWindows(usuario);
        } else if (sistemaOperativo.contains("mac")) {
            // macOS
            carpetaDescargas = "/Users/" + usuario + "/Downloads";
        } else if (sistemaOperativo.contains("nix") ||
                sistemaOperativo.contains("nux") ||
                sistemaOperativo.contains("aix")) {
            // Linux/Unix
            carpetaDescargas = "/home/" + usuario + "/Downloads";
            // Alternativa para algunos sistemas
            if (!new File(carpetaDescargas).exists()) {
                carpetaDescargas = "/home/" + usuario + "/Descargas";
            }
        }

        // Verificar si existe, si no, crear
        if (carpetaDescargas != null) {
            File carpeta = new File(carpetaDescargas);
            if (!carpeta.exists()) {
                if (carpeta.mkdirs()) {
                    System.out.println("Carpeta de descargas creada: " + carpetaDescargas);
                }
            }
        }

        return carpetaDescargas;
    }

    private static String obtenerCarpetaDescargasWindows(String usuario) {
        // Método 1: Usar variable de entorno
        String downloadsEnv = System.getenv("USERPROFILE") + "\\Downloads";
        if (new File(downloadsEnv).exists()) {
            return downloadsEnv;
        }

        // Método 2: Ruta estándar
        String rutaEstandar = "C:\\Users\\" + usuario + "\\Downloads";
        if (new File(rutaEstandar).exists()) {
            return rutaEstandar;
        }

        // Método 3: Buscar en otras unidades
        for (char drive = 'C'; drive <= 'Z'; drive++) {
            String ruta = drive + ":\\Users\\" + usuario + "\\Downloads";
            if (new File(ruta).exists()) {
                return ruta;
            }
        }

        // Método 4: Usar la carpeta de documentos como alternativa
        return System.getProperty("user.home") + "\\Documents";
    }

}
