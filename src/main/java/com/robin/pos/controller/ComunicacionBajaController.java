package com.robin.pos.controller;

import com.robin.pos.dao.ArfafeDao;
import com.robin.pos.dao.ArfaflDao;
import com.robin.pos.model.Arfafe;
import com.robin.pos.model.ComprobanteBaja;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.ProgressDialog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador para el formulario de Comunicación de Baja SUNAT
 * Permite dar de baja comprobantes de pago (Facturas, Boletas, Notas de Crédito)
 * 
 * @author Robin POS
 * @version 1.0
 */
public class ComunicacionBajaController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ComunicacionBajaController.class.getName());
    
    // ==================== CAMPOS FXML - BÚSQUEDA ====================
    
    @FXML private Label lblFecha;
    @FXML private ComboBox<String> cmbTipoDocumento;
    @FXML private TextField txtNumeroComprobante;
    @FXML private Button btnBuscar;
    @FXML private DatePicker dpFechaEmision;
    @FXML private Button btnBuscarPorFecha;
    
    // ==================== CAMPOS FXML - DATOS COMPROBANTE ====================
    
    @FXML private TextField txtComprobante;
    @FXML private TextField txtEstado;
    @FXML private TextField txtCliente;
    @FXML private TextField txtDocumentoCliente;
    @FXML private TextField txtMoneda;
    @FXML private TextField txtTotal;
    @FXML private TextField txtEstadoSunat;
    
    // ==================== CAMPOS FXML - MOTIVO ====================
    
    @FXML private ComboBox<String> cmbMotivoTipo;
    @FXML private TextArea txtMotivoDescripcion;
    
    // ==================== CAMPOS FXML - TABLA ====================
    
    @FXML private TableView<ComprobanteBaja> tblComprobantesBaja;
    @FXML private TableColumn<ComprobanteBaja, String> colTipoDoc;
    @FXML private TableColumn<ComprobanteBaja, String> colNumero;
    @FXML private TableColumn<ComprobanteBaja, String> colFechaEmision;
    @FXML private TableColumn<ComprobanteBaja, String> colCliente;
    @FXML private TableColumn<ComprobanteBaja, String> colDocCliente;
    @FXML private TableColumn<ComprobanteBaja, String> colTotal;
    @FXML private TableColumn<ComprobanteBaja, String> colMotivo;
    
    // ==================== CAMPOS FXML - BOTONES ====================
    
    @FXML private Button btnAgregar;
    @FXML private Button btnQuitar;
    @FXML private Label lblContador;
    @FXML private Button btnNuevo;
    @FXML private Button btnEnviarSunat;
    @FXML private Button btnCerrar;
    
    // ==================== VARIABLES DE INSTANCIA ====================
    
    private final ObservableList<ComprobanteBaja> listaComprobantesBaja = FXCollections.observableArrayList();
    private Arfafe comprobanteActual;
    private final ArfafeDao arfafeDao = new ArfafeDao();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private static final String NO_CIA = "01"; // Código de compañía (obtener de configuración)
    
    // ==================== INITIALIZE ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        // Configurar fecha actual
        lblFecha.setText("Fecha: " + LocalDate.now().format(dateFormatter));
        
        // Configurar ComboBox Tipo Documento
        cmbTipoDocumento.setItems(FXCollections.observableArrayList(
            "FACTURA",
            "BOLETA",
            "NOTA DE CRÉDITO"
        ));
        cmbTipoDocumento.setValue("FACTURA");
        
        // Configurar ComboBox Motivos según catálogo SUNAT
        configurarMotivos();
        
        // Configurar tabla
        configurarTabla();
        
        // Configurar bindings
        configurarBindings();
        
        // Fecha de emisión por defecto: hoy
        dpFechaEmision.setValue(LocalDate.now());
        
        // Focus inicial
        txtNumeroComprobante.requestFocus();
    }
    
    /**
     * Configura los motivos de baja según catálogo SUNAT
     */
    private void configurarMotivos() {
        cmbMotivoTipo.setItems(FXCollections.observableArrayList(
            "01 - Error en el RUC",
            "02 - Error en la descripción",
            "03 - Error en el monto",
            "04 - Error en la fecha de emisión",
            "05 - Comprobante duplicado",
            "06 - Devolución de mercadería",
            "07 - Anulación de la operación",
            "08 - Otros (especificar)"
        ));
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colTipoDoc.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getTipoDocumentoDescripcion()));
        
        colNumero.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNumeroComprobante()));
        
        colFechaEmision.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFechaEmision().format(dateFormatter)));
        
        colCliente.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getNombreCliente()));
        
        colDocCliente.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDocumentoCliente()));
        
        colTotal.setCellValueFactory(cellData -> 
            new SimpleStringProperty(String.format("%.2f", cellData.getValue().getTotal())));
        
        colMotivo.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCodigoMotivo() + " - " + 
                                     cellData.getValue().getDescripcionMotivo()));
        
        // Asignar lista observable
        tblComprobantesBaja.setItems(listaComprobantesBaja);
        
        // Listener para habilitar botón quitar
        tblComprobantesBaja.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldVal, newVal) -> btnQuitar.setDisable(newVal == null)
        );
    }
    
    /**
     * Configura bindings de controles
     */
    private void configurarBindings() {
        // Habilitar botón agregar solo si hay comprobante cargado y motivo seleccionado
        btnAgregar.disableProperty().bind(
            cmbMotivoTipo.valueProperty().isNull()
        );
        
        // Habilitar botón enviar solo si hay comprobantes en la lista
        btnEnviarSunat.disableProperty().bind(
            javafx.beans.binding.Bindings.isEmpty(listaComprobantesBaja)
        );
        
        // Actualizar contador
        listaComprobantesBaja.addListener((javafx.collections.ListChangeListener.Change<? extends ComprobanteBaja> c) -> {
            lblContador.setText("Total: " + listaComprobantesBaja.size() + " comprobante(s)");
        });
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
        
        if (numeroComprobante.isEmpty()) {
            Mensaje.alerta(null, "Validación", "Debe ingresar el número de comprobante");
            txtNumeroComprobante.requestFocus();
            return;
        }
        
        // Validar formato (básico)
        if (numeroComprobante.length() < 10) {
            Mensaje.alerta(null, "Formato Inválido", 
                "El número de comprobante debe tener al menos 10 caracteres.\n" +
                "Formato esperado: F001-00000123 o B001-00000456");
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
        
        // Mostrar diálogo de selección con comprobantes de esa fecha
        mostrarDialogoSeleccionFecha(tipoDoc, fecha);
    }
    
    /**
     * Cambiar configuración al cambiar tipo de documento
     */
    @FXML
    private void cambiarTipoDocumento(ActionEvent event) {
        txtNumeroComprobante.clear();
        limpiarDatosComprobante();
        txtNumeroComprobante.requestFocus();
    }
    
    /**
     * Busca el comprobante en la base de datos
     */
    private void buscarEnBaseDatos(String tipoDoc, String numeroComprobante) {
        
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Buscando");
        progressDialog.setMessage("Buscando comprobante...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        
        Task<Arfafe> buscarTask = new Task<Arfafe>() {
            @Override
            protected Arfafe call() throws Exception {
                return arfafeDao.buscarPorNumero(NO_CIA, tipoDoc, numeroComprobante);
            }
        };
        
        buscarTask.setOnSucceeded(e -> {
            progressDialog.close();
            
            Arfafe arfafe = buscarTask.getValue();
            
            if (arfafe != null) {
                cargarDatosComprobante(arfafe);
            } else {
                Mensaje.alerta(null, "No Encontrado", 
                    "No se encontró el comprobante: " + numeroComprobante);
                limpiarDatosComprobante();
            }
        });
        
        buscarTask.setOnFailed(e -> {
            progressDialog.close();
            Throwable error = buscarTask.getException();
            LOGGER.log(Level.SEVERE, "Error al buscar comprobante", error);
            Mensaje.error(null, "Error de Búsqueda", 
                "Error al buscar el comprobante:\n" + error.getMessage());
        });
        
        new Thread(buscarTask).start();
    }
    
    /**
     * Carga los datos del comprobante encontrado en los campos
     */
    private void cargarDatosComprobante(Arfafe arfafe) {
        
        this.comprobanteActual = arfafe;
        
        txtComprobante.setText(arfafe.getNoFactu());
        txtEstado.setText(obtenerDescripcionEstado(arfafe.getEstado()));
        txtCliente.setText(arfafe.getNbrCliente());
        txtDocumentoCliente.setText(arfafe.getNumDocCli());
        txtMoneda.setText(arfafe.getMoneda());
        txtTotal.setText(String.format("%.2f", arfafe.getTotal()));
        
        // Estado SUNAT (si existe)
        String estadoSunat = ""; //arfafe.getEstadoSunat();
        if (estadoSunat != null && !estadoSunat.isEmpty()) {
            txtEstadoSunat.setText(estadoSunat);
        } else {
            txtEstadoSunat.setText("NO ENVIADO");
        }
        
        // Validar si se puede dar de baja
        validarSiPuedeDarseDeBaja(arfafe);
    }
    
    /**
     * Valida si el comprobante puede darse de baja
     */
    private void validarSiPuedeDarseDeBaja(Arfafe arfafe) {
        
        // Verificar estado
        if ("A".equals(arfafe.getEstado())) {
            Mensaje.advertencia(null, "Comprobante Anulado", 
                "Este comprobante ya se encuentra anulado.\n" +
                "No es necesario enviarlo nuevamente a SUNAT.");
            btnAgregar.setDisable(true);
            return;
        }
        
        // Verificar fecha de emisión (máximo 7 días)
        LocalDate fechaEmision = arfafe.getFecha().toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        
        long diasTranscurridos = java.time.temporal.ChronoUnit.DAYS.between(fechaEmision, LocalDate.now());
        
        if (diasTranscurridos > 7) {
            Mensaje.advertencia(null, "Plazo Vencido", 
                "Han transcurrido " + diasTranscurridos + " días desde la emisión.\n" +
                "SUNAT permite dar de baja hasta 7 días después de la emisión.\n\n" +
                "Deberá emitir una Nota de Crédito en su lugar.");
            btnAgregar.setDisable(true);
            return;
        }
        
        // Si todo está OK, informar
        if (diasTranscurridos >= 5) {
            Mensaje.informacion(null, "Advertencia de Plazo", 
                "Quedan " + (7 - diasTranscurridos) + " día(s) para dar de baja este comprobante.");
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
            txtMotivoDescripcion.setPromptText("OBLIGATORIO: Especifique el motivo detalladamente...");
        } else {
            txtMotivoDescripcion.setPromptText("Ingrese una descripción adicional (opcional)...");
        }
    }
    
    // ==================== EVENTOS - LISTA ====================
    
    /**
     * Agregar comprobante a la lista de bajas
     */
    @FXML
    private void agregarALista(ActionEvent event) {
        
        if (comprobanteActual == null) {
            Mensaje.alerta(null, "Validación", "Debe buscar y seleccionar un comprobante primero");
            return;
        }
        
        String motivo = cmbMotivoTipo.getValue();
        if (motivo == null) {
            Mensaje.alerta(null, "Validación", "Debe seleccionar el motivo de la baja");
            return;
        }
        
        // Validar que no esté ya en la lista
        boolean yaExiste = listaComprobantesBaja.stream()
            .anyMatch(cb -> cb.getNumeroComprobante().equals(comprobanteActual.getNoFactu()));
        
        if (yaExiste) {
            Mensaje.alerta(null, "Duplicado", 
                "Este comprobante ya está en la lista de bajas");
            return;
        }
        
        // Si es motivo "Otros", validar descripción
        if (motivo.startsWith("08")) {
            String descripcion = txtMotivoDescripcion.getText().trim();
            if (descripcion.isEmpty() || descripcion.length() < 10) {
                Mensaje.alerta(null, "Validación", 
                    "Para el motivo 'Otros', debe especificar una descripción de al menos 10 caracteres");
                txtMotivoDescripcion.requestFocus();
                return;
            }
        }
        
        // Crear objeto ComprobanteBaja
        ComprobanteBaja comprobanteBaja = new ComprobanteBaja();
        comprobanteBaja.setNoCia(comprobanteActual.getNoCia());
        comprobanteBaja.setTipoDocumento(comprobanteActual.getTipoDoc());
        comprobanteBaja.setNumeroComprobante(comprobanteActual.getNoFactu());
        
        LocalDate fechaEmision = comprobanteActual.getFecha().toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        comprobanteBaja.setFechaEmision(fechaEmision);
        
        comprobanteBaja.setNombreCliente(comprobanteActual.getNbrCliente());
        comprobanteBaja.setDocumentoCliente(comprobanteActual.getNumDocCli());
        comprobanteBaja.setTotal(comprobanteActual.getTotal().doubleValue());
        
        // Extraer código de motivo
        String codigoMotivo = motivo.substring(0, 2);
        comprobanteBaja.setCodigoMotivo(codigoMotivo);
        comprobanteBaja.setDescripcionMotivo(txtMotivoDescripcion.getText().trim());
        
        // Agregar a la lista
        listaComprobantesBaja.add(comprobanteBaja);
        
        Mensaje.informacion(null, "Agregado", 
            "Comprobante agregado a la lista de bajas exitosamente");
        
        // Limpiar para siguiente
        limpiarParaNuevo();
    }
    
    /**
     * Quitar comprobante de la lista
     */
    @FXML
    private void quitarDeLista(ActionEvent event) {
        
        ComprobanteBaja seleccionado = tblComprobantesBaja.getSelectionModel().getSelectedItem();
        
        if (seleccionado == null) {
            Mensaje.alerta(null, "Selección", "Debe seleccionar un comprobante de la lista");
            return;
        }
        
        if (Mensaje.confirmacion(null, "Confirmar", 
            "¿Quitar el comprobante " + seleccionado.getNumeroComprobante() + " de la lista?")
            .get() == ButtonType.OK) {
            
            listaComprobantesBaja.remove(seleccionado);
        }
    }
    
    // ==================== EVENTOS - ACCIONES PRINCIPALES ====================
    
    /**
     * Enviar comunicación de baja a SUNAT
     */
    @FXML
    private void enviarComunicacionBaja(ActionEvent event) {
        
        if (listaComprobantesBaja.isEmpty()) {
            Mensaje.alerta(null, "Lista Vacía", 
                "Debe agregar al menos un comprobante para dar de baja");
            return;
        }
        
        // Confirmación
        if (Mensaje.confirmacion(null, "Confirmar Envío a SUNAT", 
            "¿Está seguro de enviar la comunicación de baja de " + 
            listaComprobantesBaja.size() + " comprobante(s) a SUNAT?\n\n" +
            "Esta acción NO se puede revertir.")
            .get() != ButtonType.OK) {
            return;
        }
        
        // Aquí iría la lógica de envío a SUNAT
        enviarASunat();
    }
    
    /**
     * Lógica de envío a SUNAT
     */
    private void enviarASunat() {
        
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Enviando a SUNAT");
        progressDialog.setMessage("Generando XML y enviando comunicación de baja...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        
        Task<String> enviarTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                
                // TODO: Implementar lógica real de envío
                // 1. Generar XML de comunicación de baja
                // 2. Firmar digitalmente
                // 3. Enviar a SUNAT
                // 4. Procesar respuesta
                
                Thread.sleep(3000); // Simulación
                
                return "ACEPTADO - Ticket: 123456789";
            }
        };
        
        enviarTask.setOnSucceeded(e -> {
            progressDialog.close();
            
            String resultado = enviarTask.getValue();
            
            Mensaje.informacion(null, "Envío Exitoso", 
                "Comunicación de baja enviada a SUNAT correctamente.\n\n" +
                "Resultado: " + resultado + "\n\n" +
                "Los comprobantes serán anulados una vez SUNAT procese la comunicación.");
            
            // Limpiar todo
            nuevo(null);
        });
        
        enviarTask.setOnFailed(e -> {
            progressDialog.close();
            
            Throwable error = enviarTask.getException();
            LOGGER.log(Level.SEVERE, "Error al enviar a SUNAT", error);
            
            Mensaje.error(null, "Error de Envío", 
                "No se pudo enviar la comunicación de baja:\n" + error.getMessage());
        });
        
        new Thread(enviarTask).start();
    }
    
    /**
     * Limpiar formulario para nuevo
     */
    @FXML
    private void nuevo(ActionEvent event) {
        limpiarParaNuevo();
        listaComprobantesBaja.clear();
        cmbMotivoTipo.setValue(null);
        txtMotivoDescripcion.clear();
    }
    
    /**
     * Cerrar ventana
     */
    @FXML
    private void cerrar(ActionEvent event) {
        
        if (!listaComprobantesBaja.isEmpty()) {
            if (Mensaje.confirmacion(null, "Confirmar Salida", 
                "Hay comprobantes sin enviar.\n¿Está seguro de cerrar?")
                .get() != ButtonType.OK) {
                return;
            }
        }
        
        btnCerrar.getScene().getWindow().hide();
    }
    
    // ==================== MÉTODOS AUXILIARES ====================
    
    /**
     * Mostrar diálogo de selección por fecha
     */
    private void mostrarDialogoSeleccionFecha(String tipoDoc, LocalDate fecha) {
        // TODO: Implementar diálogo con lista de comprobantes de esa fecha
        Mensaje.informacion(null, "Función en Desarrollo", 
            "Esta función mostrará un diálogo con todos los comprobantes del día seleccionado");
    }
    
    /**
     * Extrae el tipo de documento del número de comprobante
     */
    private String extraerTipoDocumento(String numeroComprobante) {
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
        return switch (descripcion) {
            case "FACTURA" -> "F";
            case "BOLETA" -> "B";
            case "NOTA DE CRÉDITO" -> "N";
            default -> "F";
        };
    }
    
    /**
     * Obtiene descripción del estado
     */
    private String obtenerDescripcionEstado(String codigo) {
        return switch (codigo) {
            case "D" -> "DESPACHADO";
            case "P" -> "PENDIENTE";
            case "A" -> "ANULADO";
            default -> codigo;
        };
    }
    
    /**
     * Limpia los datos del comprobante
     */
    private void limpiarDatosComprobante() {
        comprobanteActual = null;
        txtComprobante.clear();
        txtEstado.clear();
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
}
