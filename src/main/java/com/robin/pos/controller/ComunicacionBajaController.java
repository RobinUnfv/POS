package com.robin.pos.controller;

import com.robin.pos.dao.ArfafeDao;
import com.robin.pos.dao.ComunicacionBajaDao;
import com.robin.pos.model.Arfafe;
import com.robin.pos.model.ComprobanteBaja;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import com.robin.pos.util.ProgressDialog;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComunicacionBajaController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ComunicacionBajaController.class.getName());

    // ==================== CAMPOS FXML - HEADER ====================

    @FXML private Label lblFecha;
    @FXML private Label lblHora;

    // ==================== CAMPOS FXML - BÚSQUEDA ====================

    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNumeroComprobante;
    @FXML private Button btnBuscar;
    @FXML private DatePicker dpFechaEmision;
    @FXML private Button btnBuscarPorFecha;

    // ==================== CAMPOS FXML - DATOS COMPROBANTE ====================

    @FXML private TextField txtComprobante;
    @FXML private TextField txtEstado;
    @FXML private Label lblEstadoBadge;
    @FXML private TextField txtCliente;
    @FXML private TextField txtDocumentoCliente;
    @FXML private TextField txtMoneda;
    @FXML private TextField txtTotal;
    @FXML private TextField txtEstadoSunat;

    // ==================== CAMPOS FXML - MOTIVO ====================

    @FXML private ComboBox<String> cmbMotivoTipo;
    @FXML private TextArea txtMotivoDescripcion;

    // ==================== CAMPOS FXML - BOTONES ====================

    @FXML private Button btnNuevo;
    @FXML private Button btnEnviarSunat;
    @FXML private Button btnCerrar;

    // ==================== VARIABLES DE INSTANCIA ====================

    private Arfafe comprobanteActual;
    private final ArfafeDao arfafeDao = new ArfafeDao();
    private final ComunicacionBajaDao bajaDao = new ComunicacionBajaDao();
    private Timeline clockTimeline;

    private static final String NO_CIA = "01"; // Código de compañía (obtener de configuración)

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(
            "dd 'de' MMMM, yyyy",
            new Locale("es", "PE")
    );

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    // ==================== INITIALIZE ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        LOGGER.info("=".repeat(60));
        LOGGER.info("Inicializando Controlador de Comunicación de Baja v3.0");
        LOGGER.info("Diseño moderno sin tabla de múltiples comprobantes");
        LOGGER.info("=".repeat(60));

        try {
            // Inicializar reloj en tiempo real
            inicializarReloj();

            // Configurar ComboBox Tipo Documento
            configurarTipoDocumento();

            // Configurar ComboBox Motivos
            configurarMotivos();

            // Configurar bindings
            configurarBindings();

            // Fecha de emisión por defecto: hoy
            dpFechaEmision.setValue(LocalDate.now());

            // Focus inicial
            txtNumeroComprobante.requestFocus();

            LOGGER.info("✓ Controlador inicializado correctamente");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al inicializar controlador", e);
            Mensaje.error(null, "Error de Inicialización",
                    "Error al inicializar el formulario:\n" + e.getMessage());
        }
    }

    /**
     * Inicializa el reloj en tiempo real en el header
     */
    private void inicializarReloj() {
        // Establecer fecha inicial
        lblFecha.setText(LocalDate.now().format(dateFormatter));

        // Crear timeline para actualizar hora cada segundo
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            lblHora.setText(LocalTime.now().format(timeFormatter));
        }));

        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();

        // Establecer hora inicial
        lblHora.setText(LocalTime.now().format(timeFormatter));

        LOGGER.fine("✓ Reloj en tiempo real inicializado");
    }

    /**
     * Configura los tipos de documento disponibles
     */
    private void configurarTipoDocumento() {
        cmbTipoDocumento.getItems().addAll(
                "FACTURA",
                "BOLETA",
                "NOTA DE CRÉDITO"
        );
        cmbTipoDocumento.setValue("FACTURA");

        LOGGER.fine("✓ Tipos de documento configurados");
    }

    /**
     * Configura los motivos de baja según catálogo SUNAT
     */
    private void configurarMotivos() {
        cmbMotivoTipo.getItems().addAll(
                "01 - Error en el RUC del adquiriente o usuario",
                "02 - Error en la descripción del bien o servicio",
                "03 - Error en el monto del comprobante",
                "04 - Error en la fecha de emisión",
                "05 - Comprobante emitido duplicado",
                "06 - Devolución de la mercadería",
                "07 - Anulación de la operación",
                "08 - Otros motivos (especificar)"
        );

        LOGGER.fine("✓ Motivos SUNAT configurados (8 opciones)");
    }

    /**
     * Configura bindings y listeners
     */
    private void configurarBindings() {
        // Habilitar botón enviar solo cuando hay motivo seleccionado y comprobante cargado
        btnEnviarSunat.disableProperty().bind(
                cmbMotivoTipo.valueProperty().isNull()
                        .or(txtComprobante.textProperty().isEmpty())
        );

        LOGGER.fine("✓ Bindings configurados");
    }

    // ==================== EVENTOS - BÚSQUEDA ====================

    /**
     * Buscar comprobante al presionar Enter
     */
    @FXML
    private void buscarConEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            buscarComprobante(new ActionEvent());
        }
    }

    /**
     * Buscar comprobante por número
     */
    @FXML
    private void buscarComprobante(ActionEvent event) {

        String numeroComprobante = txtNumeroComprobante.getText().trim();

        // Validar que se ingresó un número
        if (numeroComprobante.isEmpty()) {
            Mensaje.alerta(null, "Validación", "Debe ingresar el número de comprobante");
            txtNumeroComprobante.requestFocus();
            return;
        }

        // Validar formato básico (mínimo 10 caracteres)
        if (numeroComprobante.length() < 10) {
            Mensaje.alerta(null, "Formato Inválido",
                    "El número de comprobante debe tener al menos 10 caracteres.\n" +
                            "Formato esperado: F00100000123 o B00100000456");
            txtNumeroComprobante.requestFocus();
            return;
        }

        // Extraer tipo de documento del número
        String tipoDoc = extraerTipoDocumento(numeroComprobante);

        if (tipoDoc == null) {
            Mensaje.alerta(null, "Tipo Inválido",
                    "No se pudo identificar el tipo de documento.\n" +
                            "Asegúrese que el número comience con F, B o N");
            return;
        }

        LOGGER.info("Buscando comprobante: " + numeroComprobante + " (Tipo: " + tipoDoc + ")");

        // Buscar en base de datos
        buscarEnBaseDatos(tipoDoc, numeroComprobante);
    }

    /**
     * Buscar comprobantes por fecha
     */
    @FXML
    private void buscarPorFecha(ActionEvent event) {

        LocalDate fecha = dpFechaEmision.getValue();

        if (fecha == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar una fecha");
            return;
        }

        String tipoDoc = mapearTipoDocumento(cmbTipoDocumento.getValue());

        LOGGER.info("Búsqueda por fecha solicitada: " + fecha + " (Tipo: " + tipoDoc + ")");

        // TODO: Implementar diálogo de selección de comprobantes por fecha
        Mensaje.alerta(null, "Función en Desarrollo",
                "Esta función mostrará un diálogo con todos los comprobantes " +
                        "del tipo '" + cmbTipoDocumento.getValue() + "' emitidos en la fecha:\n\n" +
                        fecha.format(dateFormatter) + "\n\n" +
                        "Podrá seleccionar uno de la lista para dar de baja.");
    }

    /**
     * Cambiar configuración al cambiar tipo de documento
     */
    @FXML
    private void cambiarTipoDocumento(ActionEvent event) {
        txtNumeroComprobante.clear();
        limpiarDatosComprobante();
        txtNumeroComprobante.requestFocus();

        LOGGER.fine("Tipo de documento cambiado a: " + cmbTipoDocumento.getValue());
    }

    /**
     * Busca el comprobante en la base de datos
     */
    private void buscarEnBaseDatos(String tipoDoc, String numeroComprobante) {

        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Buscando Comprobante");
        progressDialog.setMessage("Buscando "+numeroComprobante);
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        Task<Arfafe> buscarTask = new Task<Arfafe>() {
            @Override
            protected Arfafe call() throws Exception {
                LOGGER.info("Consultando base de datos...");
                return arfafeDao.buscarPorNumero(NO_CIA, tipoDoc, numeroComprobante);
            }
        };

        buscarTask.setOnSucceeded(e -> {
            progressDialog.close();

            Arfafe arfafe = buscarTask.getValue();

            if (arfafe != null) {
                LOGGER.info("✓ Comprobante encontrado: " + arfafe.getNoFactu());
                cargarDatosComprobante(arfafe);
            } else {
                LOGGER.warning("✗ Comprobante no encontrado: " + numeroComprobante);
                Mensaje.alerta(null, "No Encontrado",
                        "No se encontró el comprobante: " + numeroComprobante + "\n\n" +
                                "Verifique que el número sea correcto y que el comprobante " +
                                "esté registrado en el sistema.");
                limpiarDatosComprobante();
            }
        });

        buscarTask.setOnFailed(e -> {
            progressDialog.close();
            Throwable error = buscarTask.getException();
            LOGGER.log(Level.SEVERE, "✗ Error al buscar comprobante", error);
            Mensaje.error(null, "Error de Búsqueda",
                    "Error al buscar el comprobante:\n\n" + error.getMessage());
        });

        new Thread(buscarTask).start();
    }

    /**
     * Carga los datos del comprobante encontrado
     */
    private void cargarDatosComprobante(Arfafe arfafe) {

        this.comprobanteActual = arfafe;

        // Datos básicos
        txtComprobante.setText(arfafe.getNoFactu());
        txtEstado.setText(obtenerDescripcionEstado(arfafe.getEstado()));
        txtCliente.setText(arfafe.getNbrCliente());
        txtDocumentoCliente.setText(arfafe.getNumDocCli());
        txtMoneda.setText(arfafe.getMoneda());
        txtTotal.setText(String.format("S/ %.2f", arfafe.getTotal()));

        // Badge de estado
        actualizarBadgeEstado(arfafe.getEstado());

        // Estado SUNAT (si existe)
        String estadoSunat =  arfafe.getProceStatus();
        if (estadoSunat != null && !estadoSunat.isEmpty()) {
            //estadoSunat = Metodos.getEstadoSunat(estadoSunat);
            txtEstadoSunat.setText(Metodos.getEstadoSunat(estadoSunat));
        } else {
            txtEstadoSunat.setText("NO ENVIADO");
        }

        // Validar si se puede dar de baja
        validarSiPuedeDarseDeBaja(arfafe);

    }

    /**
     * Actualiza el badge de estado según el estado del comprobante
     */
    private void actualizarBadgeEstado(String estado) {
        if (estado == null) return;

        switch (estado) {
            case "D":
                lblEstadoBadge.setText("DESPACHADO");
                lblEstadoBadge.setStyle(
                        "-fx-background-color: #10b981; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 9999px; " +
                                "-fx-padding: 5px 14px; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold;"
                );
                break;
            case "A":
                lblEstadoBadge.setText("ANULADO");
                lblEstadoBadge.setStyle(
                        "-fx-background-color: #ef4444; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 9999px; " +
                                "-fx-padding: 5px 14px; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold;"
                );
                break;
            case "P":
                lblEstadoBadge.setText("PENDIENTE");
                lblEstadoBadge.setStyle(
                        "-fx-background-color: #f59e0b; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 9999px; " +
                                "-fx-padding: 5px 14px; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold;"
                );
                break;
            default:
                lblEstadoBadge.setText(estado);
                lblEstadoBadge.setStyle(
                        "-fx-background-color: #6b7280; " +
                                "-fx-text-fill: white; " +
                                "-fx-background-radius: 9999px; " +
                                "-fx-padding: 5px 14px; " +
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold;"
                );
        }
    }

    /**
     * Valida si el comprobante puede darse de baja
     */
    private void validarSiPuedeDarseDeBaja(Arfafe arfafe) {

        // Verificar si ya está anulado
        if ("A".equals(arfafe.getEstado())) {
            LOGGER.warning("El comprobante ya está ANULADO");
            Mensaje.alerta(null, "Comprobante Anulado",
                    "Este comprobante ya se encuentra anulado.\n" +
                            "No es necesario enviarlo nuevamente a SUNAT.");
            btnEnviarSunat.setDisable(true);
            return;
        }

        // Verificar fecha de emisión (máximo 7 días)
        LocalDate fechaEmision = arfafe.getFecha().toInstant()
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS
                .between(fechaEmision, LocalDate.now());

        LOGGER.info("Días transcurridos desde emisión: " + diasTranscurridos);

        if (diasTranscurridos > 7) {
            LOGGER.warning("Plazo VENCIDO - " + diasTranscurridos + " días transcurridos");
            Mensaje.alerta(null, "Plazo Vencido",
                    "Han transcurrido " + diasTranscurridos + " días desde la emisión.\n" +
                            "SUNAT permite dar de baja hasta 7 días después de la emisión.\n\n" +
                            "⚠️ Deberá emitir una Nota de Crédito en su lugar.");
            btnEnviarSunat.setDisable(true);
            return;
        }

        // Si quedan pocos días, informar
        if (diasTranscurridos >= 5) {
            long diasRestantes = 7 - diasTranscurridos;
            LOGGER.info("Advertencia: Quedan " + diasRestantes + " día(s) para dar de baja");
            Mensaje.alerta(null, "Advertencia de Plazo",
                    "⏰ Quedan " + diasRestantes + " día(s) para dar de baja este comprobante.\n\n" +
                            "Después del plazo deberá usar una Nota de Crédito.");
        }

        // Verificar si ya existe una baja para este comprobante
        if (bajaDao.existeBaja(arfafe.getNoCia(), arfafe.getTipoDoc(), arfafe.getNoFactu())) {
            LOGGER.warning("Ya existe una baja registrada para este comprobante");
            Mensaje.alerta(null, "Baja Existente",
                    "Ya existe una comunicación de baja registrada para este comprobante.\n\n" +
                            "Verifique el estado en la tabla COMUNICACION_BAJA de la base de datos.");
        }
    }

    // ==================== EVENTOS - MOTIVO ====================

    /**
     * Al seleccionar un motivo
     */
    @FXML
    private void seleccionarMotivo(ActionEvent event) {
        String motivo = cmbMotivoTipo.getValue();

        if (motivo != null && motivo.startsWith("08")) {
            // Si es "Otros", hacer que la descripción sea obligatoria
            txtMotivoDescripcion.setPromptText(
                    "OBLIGATORIO: Especifique el motivo detalladamente (mínimo 10 caracteres)..."
            );
            txtMotivoDescripcion.setStyle(
                    "-fx-border-color: #ef4444; " +
                            "-fx-border-width: 2px; " +
                            "-fx-border-radius: 6px; " +
                            "-fx-background-radius: 6px;"
            );
            LOGGER.fine("Motivo '08 - Otros' seleccionado - Descripción OBLIGATORIA");
        } else {
            txtMotivoDescripcion.setPromptText(
                    "Ingrese una descripción adicional (opcional)..."
            );
            txtMotivoDescripcion.setStyle("");
            LOGGER.fine("Motivo seleccionado: " + motivo);
        }
    }

    // ==================== EVENTOS - ACCIONES PRINCIPALES ====================

    /**
     * Enviar comunicación de baja a SUNAT
     */
    @FXML
    private void enviarComunicacionBaja(ActionEvent event) {

        LOGGER.info("=".repeat(60));
        LOGGER.info("INICIANDO PROCESO DE ENVÍO A SUNAT");
        LOGGER.info("=".repeat(60));

        // Validar que hay comprobante seleccionado
        if (comprobanteActual == null) {
            Mensaje.alerta(null, "Validación",
                    "Debe buscar y seleccionar un comprobante primero");
            return;
        }

        // Validar motivo
        String motivo = cmbMotivoTipo.getValue();
        if (motivo == null) {
            Mensaje.alerta(null, "Validación",
                    "Debe seleccionar el motivo de la baja");
            cmbMotivoTipo.requestFocus();
            return;
        }

        // Si es motivo "Otros", validar descripción
        if (motivo.startsWith("08")) {
            String descripcion = txtMotivoDescripcion.getText().trim();
            if (descripcion.isEmpty() || descripcion.length() < 10) {
                Mensaje.alerta(null, "Validación",
                        "Para el motivo '08 - Otros', debe especificar una descripción " +
                                "de al menos 10 caracteres");
                txtMotivoDescripcion.requestFocus();
                return;
            }
            LOGGER.info("✓ Validación de motivo '08 - Otros': " + descripcion);
        }

        // Log de datos a enviar
        LOGGER.info("Comprobante: " + comprobanteActual.getNoFactu());
        LOGGER.info("Cliente: " + comprobanteActual.getNbrCliente());
        LOGGER.info("Total: S/ " + String.format("%.2f", comprobanteActual.getTotal()));
        LOGGER.info("Motivo: " + motivo);

        // Confirmación final
        if (Mensaje.confirmacion(null, "Confirmar Envío a SUNAT",
                        "¿Está seguro de enviar la comunicación de baja del comprobante?\n\n" +
                                "📄 Comprobante: " + comprobanteActual.getNoFactu() + "\n" +
                                "👤 Cliente: " + comprobanteActual.getNbrCliente() + "\n" +
                                "💰 Total: S/ " + String.format("%.2f", comprobanteActual.getTotal()) + "\n" +
                                "📝 Motivo: " + motivo + "\n\n" +
                                "⚠️ Esta acción NO se puede revertir.\n" +
                                "⚠️ El comprobante quedará ANULADO en SUNAT.")
                .get() != ButtonType.OK) {
            LOGGER.info("Envío CANCELADO por el usuario");
            return;
        }

        // Enviar a SUNAT
        enviarASunat();
    }

    /**
     * Lógica de envío a SUNAT
     */
    private void enviarASunat() {

        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Enviando a SUNAT");
        progressDialog.setMessage("Procesando comunicación de baja...\n\n" +
                "• Generando XML\n" +
                "• Firmando digitalmente\n" +
                "• Enviando a SUNAT");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        Task<String> enviarTask = new Task<String>() {
            @Override
            protected String call() throws Exception {

                LOGGER.info("→ Creando objeto ComprobanteBaja");

                // 1. Crear objeto ComprobanteBaja
                ComprobanteBaja comprobante = new ComprobanteBaja();
                comprobante.setNoCia(comprobanteActual.getNoCia());
                comprobante.setTipoDocumento(comprobanteActual.getTipoDoc());
                comprobante.setNumeroComprobante(comprobanteActual.getNoFactu());

                LocalDate fechaEmision = comprobanteActual.getFecha().toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
                comprobante.setFechaEmision(fechaEmision);

                comprobante.setNombreCliente(comprobanteActual.getNbrCliente());
                comprobante.setDocumentoCliente(comprobanteActual.getNumDocCli());
                comprobante.setTotal(comprobanteActual.getTotal().doubleValue());

                // Extraer código de motivo
                String motivoCompleto = cmbMotivoTipo.getValue();
                String codigoMotivo = motivoCompleto.substring(0, 2);
                comprobante.setCodigoMotivo(codigoMotivo);
                comprobante.setDescripcionMotivo(txtMotivoDescripcion.getText().trim());

                LOGGER.info("→ Registrando en base de datos");

                // 2. Registrar en base de datos
                int idBaja = bajaDao.registrarBaja(comprobante);

                if (idBaja == 0) {
                    throw new Exception("No se pudo registrar la baja en la base de datos");
                }

                LOGGER.info("✓ Baja registrada con ID: " + idBaja);

                // TODO: 3. Generar XML de comunicación de baja
                LOGGER.info("→ Generando XML de comunicación de baja...");
                // Aquí usar XMLGeneratorService para generar el XML
                Thread.sleep(500);

                // TODO: 4. Firmar digitalmente
                LOGGER.info("→ Firmando XML con certificado digital...");
                // Aquí usar DigitalSignature para firmar
                Thread.sleep(500);

                // TODO: 5. Enviar a SUNAT vía SOAP
                LOGGER.info("→ Enviando a SUNAT vía SOAP...");
                // Aquí usar SOAPClientService para enviar
                Thread.sleep(1000);

                // TODO: 6. Procesar respuesta (CDR)
                LOGGER.info("→ Procesando respuesta de SUNAT...");
                Thread.sleep(500);

                // TODO: 7. Actualizar estado en BD
                LOGGER.info("→ Actualizando estado en base de datos...");
                // Actualizar ARFAFE.ESTADO = 'A'

                // Simulación (remover cuando se implemente la integración real)
                String ticketSunat = "TICKET-" + System.currentTimeMillis();

                LOGGER.info("✓ ENVÍO COMPLETADO - Ticket: " + ticketSunat);

                return ticketSunat;
            }
        };

        enviarTask.setOnSucceeded(e -> {
            progressDialog.close();

            String ticket = enviarTask.getValue();

            LOGGER.info("=".repeat(60));
            LOGGER.info("✓ COMUNICACIÓN DE BAJA ENVIADA EXITOSAMENTE");
            LOGGER.info("Ticket SUNAT: " + ticket);
            LOGGER.info("=".repeat(60));

            Mensaje.alerta(null, "✓ Envío Exitoso",
                    "Comunicación de baja enviada correctamente.\n\n" +
                            "📋 Ticket SUNAT: " + ticket + "\n" +
                            "📄 Comprobante: " + comprobanteActual.getNoFactu() + "\n\n" +
                            "El comprobante será anulado una vez SUNAT procese la comunicación.\n\n" +
                            "Puede verificar el estado en:\n" +
                            "• Tabla: COMUNICACION_BAJA\n" +
                            "• Log: LOG_ENVIO_SUNAT");

            // Limpiar formulario
            nuevo(null);
        });

        enviarTask.setOnFailed(e -> {
            progressDialog.close();

            Throwable error = enviarTask.getException();
            LOGGER.log(Level.SEVERE, "✗ ERROR AL ENVIAR A SUNAT", error);

            Mensaje.error(null, "✗ Error de Envío",
                    "No se pudo enviar la comunicación de baja:\n\n" +
                            error.getMessage() + "\n\n" +
                            "Verifique:\n" +
                            "• Conexión a internet\n" +
                            "• Configuración de SUNAT\n" +
                            "• Certificado digital\n" +
                            "• Credenciales SOL");
        });

        new Thread(enviarTask).start();
    }

    /**
     * Limpiar formulario para nuevo
     */
    @FXML
    private void nuevo(ActionEvent event) {
        LOGGER.info("Limpiando formulario para nuevo comprobante");

        limpiarParaNuevo();
        cmbMotivoTipo.setValue(null);
        txtMotivoDescripcion.clear();
        txtMotivoDescripcion.setStyle("");

        LOGGER.info("✓ Formulario limpiado");
    }

    /**
     * Cerrar ventana
     */
    @FXML
    private void cerrar(ActionEvent event) {

        // Si hay un comprobante cargado sin enviar, confirmar
        if (comprobanteActual != null && cmbMotivoTipo.getValue() != null) {
            if (Mensaje.confirmacion(null, "Confirmar Salida",
                            "Hay un comprobante cargado sin enviar.\n" +
                                    "¿Está seguro de cerrar?")
                    .get() != ButtonType.OK) {
                return;
            }
        }

        // Detener el reloj
        if (clockTimeline != null) {
            clockTimeline.stop();
            LOGGER.fine("✓ Reloj detenido");
        }

        // Cerrar ventana
        btnCerrar.getScene().getWindow().hide();

        LOGGER.info("=".repeat(60));
        LOGGER.info("Ventana cerrada - Sesión finalizada");
        LOGGER.info("=".repeat(60));
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Extrae el tipo de documento del número de comprobante
     */
    private String extraerTipoDocumento(String numeroComprobante) {
        if (numeroComprobante == null || numeroComprobante.isEmpty()) {
            return null;
        }

        char primerCaracter = numeroComprobante.charAt(0);
        return switch (primerCaracter) {
            case 'F', 'f' -> "F";
            case 'B', 'b' -> "B";
            case 'N', 'n' -> "N";
            default -> null;
        };
    }

    /**
     * Mapea descripción de tipo documento a código
     */
    private String mapearTipoDocumento(String descripcion) {
        if (descripcion == null) return "F";

        return switch (descripcion) {
            case "FACTURA" -> "F";
            case "BOLETA" -> "B";
            case "NOTA DE CRÉDITO" -> "N";
            default -> "F";
        };
    }

    /**
     * Obtiene descripción del estado del comprobante
     */
    private String obtenerDescripcionEstado(String codigo) {
        if (codigo == null) return "";

        return switch (codigo) {
            case "D" -> "DESPACHADO";
            case "P" -> "PENDIENTE";
            case "A" -> "ANULADO";
            default -> codigo;
        };
    }

    /**
     * Limpia los datos del comprobante cargado
     */
    private void limpiarDatosComprobante() {
        comprobanteActual = null;
        txtComprobante.clear();
        txtEstado.clear();
        lblEstadoBadge.setText("");
        lblEstadoBadge.setStyle("");
        txtCliente.clear();
        txtDocumentoCliente.clear();
        txtMoneda.clear();
        txtTotal.clear();
        txtEstadoSunat.clear();
    }

    /**
     * Limpia para agregar nuevo comprobante
     */
    private void limpiarParaNuevo() {
        txtNumeroComprobante.clear();
        limpiarDatosComprobante();
        txtNumeroComprobante.requestFocus();
    }

    // ==================== GETTERS (para testing) ====================

    public Arfafe getComprobanteActual() {
        return comprobanteActual;
    }

    public Timeline getClockTimeline() {
        return clockTimeline;
    }
}