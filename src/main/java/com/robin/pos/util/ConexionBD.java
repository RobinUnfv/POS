package com.robin.pos.util;

import javafx.application.Platform;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Conexión a Oracle - versión mejorada.
 *
 * Cambios respecto a la versión anterior:
 *  1. Los datos de conexión ya no están en el código: se leen de config.properties
 *     (ver {@link AppConfig}). Para cambiar entre CASA ROBIN / NEXER / CELIA solo se
 *     edita el archivo, sin recompilar.
 *  2. El driver se registra UNA sola vez (antes se registraba en cada conexión).
 *  3. Soporta SID (jdbc:oracle:thin:@host:puerto:SID) o SERVICE_NAME
 *     (jdbc:oracle:thin:@//host:puerto/servicio).
 *  4. Timeouts de conexión y de lectura: la app ya no se congela si la BD no responde.
 *  5. {@link #obtenerConexion()} lanza SQLException para que los DAO nuevos manejen
 *     el error y las transacciones con try-with-resources.
 *  6. Los mensajes de error se muestran siempre en el hilo de JavaFX (antes, si la
 *     conexión fallaba dentro de un Task, Mensaje.error lanzaba IllegalStateException)
 *     y no se repiten en ráfaga.
 *  7. Utilidades para transacciones: {@link #rollback(Connection)} y {@link #cerrar(AutoCloseable...)}.
 *  8. Se mantienen oracle() y cerrarCxOracle() para no romper los DAO existentes.
 *
 * @author Robin POS
 * @version 2.0
 */
public final class ConexionBD {

    private static final Logger LOGGER = Logger.getLogger(ConexionBD.class.getName());

    private static final String HOST = AppConfig.get("db.host",
            System.getenv("COMPUTERNAME") != null ? System.getenv("COMPUTERNAME") : "localhost");
    private static final String PUERTO = AppConfig.get("db.port", "1521");
    private static final String SID = AppConfig.get("db.sid", "BDNX1");
    private static final String SERVICIO = AppConfig.get("db.service");      // opcional
    private static final String USUARIO = AppConfig.get("db.user", "LLE");
    private static final String PASSWORD = AppConfig.get("db.password", "YVL");
    private static final int TIMEOUT_CONEXION_SEG = AppConfig.getInt("db.connectTimeoutSeconds", 10);
    private static final int TIMEOUT_LECTURA_SEG = AppConfig.getInt("db.readTimeoutSeconds", 120);

    private static final String URL = (SERVICIO != null)
            ? "jdbc:oracle:thin:@//" + HOST + ":" + PUERTO + "/" + SERVICIO
            : "jdbc:oracle:thin:@" + HOST + ":" + PUERTO + ":" + SID;

    /** Evita mostrar decenas de alertas si la BD está caída (una cada 5 s como máximo). */
    private static final AtomicLong ULTIMO_AVISO = new AtomicLong(0);

    static {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "No se encontró el driver de Oracle (ojdbc) en el classpath", e);
        }
        DriverManager.setLoginTimeout(TIMEOUT_CONEXION_SEG);
    }

    private ConexionBD() {
    }

    /**
     * Abre una conexión nueva. Úsela con try-with-resources:
     * <pre>
     * try (Connection cx = ConexionBD.obtenerConexion();
     *      PreparedStatement ps = cx.prepareStatement(sql)) { ... }
     * </pre>
     */
    public static Connection obtenerConexion() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", USUARIO);
        props.setProperty("password", PASSWORD);
        props.setProperty("oracle.net.CONNECT_TIMEOUT", String.valueOf(TIMEOUT_CONEXION_SEG * 1000));
        props.setProperty("oracle.jdbc.ReadTimeout", String.valueOf(TIMEOUT_LECTURA_SEG * 1000));
        // Identifica la aplicación en V$SESSION (útil para el DBA)
        props.setProperty("v$session.program", "RobinPOS");
        return DriverManager.getConnection(URL, props);
    }

    /**
     * Método heredado: devuelve la conexión o null si falla (muestra el error al usuario).
     * Se conserva para que los DAO existentes sigan funcionando sin cambios.
     */
    public static Connection oracle() {
        try {
            return obtenerConexion();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar con Oracle (" + URL + ")", e);
            notificarError("Conexión", "No se pudo conectar con la base de datos.\n" + e.getMessage());
            return null;
        }
    }

    /** Método heredado para cerrar la conexión. */
    public static void cerrarCxOracle(Connection conexion) {
        cerrar(conexion);
    }

    /** Cierra en orden cualquier recurso JDBC (ResultSet, Statement, Connection) sin lanzar excepción. */
    public static void cerrar(AutoCloseable... recursos) {
        if (recursos == null) {
            return;
        }
        for (AutoCloseable r : recursos) {
            if (r == null) {
                continue;
            }
            try {
                if (r instanceof Connection c && c.isClosed()) {
                    continue;
                }
                r.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error al cerrar recurso JDBC", e);
            }
        }
    }

    /** Deshace la transacción sin lanzar excepción (para bloques catch). */
    public static void rollback(Connection cx) {
        if (cx == null) {
            return;
        }
        try {
            if (!cx.isClosed() && !cx.getAutoCommit()) {
                cx.rollback();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error al hacer rollback", e);
        }
    }

    /** Verifica que la base de datos responda (útil en el login o en un botón "Probar conexión"). */
    public static boolean probarConexion() {
        try (Connection cx = obtenerConexion()) {
            return cx.isValid(5);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Prueba de conexión fallida", e);
            return false;
        }
    }

    public static String getUrl() {
        return URL;
    }

    public static String getUsuario() {
        return USUARIO;
    }

    /** Muestra el mensaje en el hilo de JavaFX, evitando ráfagas de alertas repetidas. */
    private static void notificarError(String titulo, String mensaje) {
        long ahora = System.currentTimeMillis();
        long ultimo = ULTIMO_AVISO.get();
        if (ahora - ultimo < 5000 || !ULTIMO_AVISO.compareAndSet(ultimo, ahora)) {
            return;
        }
        try {
            if (Platform.isFxApplicationThread()) {
                Mensaje.error(null, titulo, mensaje);
            } else {
                Platform.runLater(() -> Mensaje.error(null, titulo, mensaje));
            }
        } catch (IllegalStateException toolkitNoIniciado) {
            // Sin JavaFX (pruebas o procesos batch): basta con el log
        }
    }
}
