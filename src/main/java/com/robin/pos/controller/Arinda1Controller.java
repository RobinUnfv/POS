package com.robin.pos.controller;

import com.robin.pos.dao.Arinda1Dao;
import com.robin.pos.dao.ArinumDao;
import com.robin.pos.model.Arinda1;
import com.robin.pos.model.Arinum;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Arinda1Controller implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(Arinda1Controller.class.getName());

    // === COMPONENTES FXML ===
    @FXML private VBox vbxPrincipal;
    @FXML private VBox vbxCuerpo;
    @FXML private HBox hbxPie;
    @FXML private HBox hbxStockIndicator;

    // Campos de texto
    @FXML private TextField txtCodigo;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtCostoUnitario;
    @FXML private TextField txtStockMinimo;
    @FXML private TextField txtStockMaximo;
    @FXML private TextField txtStockActual;

    // ComboBox
    @FXML private ComboBox<String> cbxTipoArticulo;
    @FXML private ComboBox<Arinum> cbxMedida;
    @FXML private ComboBox<String> cbxMoneda;

    // CheckBox
    @FXML private CheckBox chkVigente;

    // Botones
    @FXML private Button btnRegistrar;

    // === CONSTANTES ===
    private static final String NO_CIA = "01";

    // Stage para indicador de carga
    private Stage loadingStage;

    // Artículo pendiente de cargar (para cuando se carga antes de que los combos estén listos)
    private Arinda1 articuloPendiente;

    // Bandera para indicar si el modal debe cerrarse después de guardar
    private boolean modoEdicion = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarCamposNumericos();
        cargarTiposArticulo();
        cargarUnidadesMedida();
        cargarMonedas();
        configurarValidaciones();
    }

    /**
     * Configura los campos numéricos para aceptar solo números
     */
    private void configurarCamposNumericos() {
        // Costo unitario - acepta decimales
        txtCostoUnitario.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                txtCostoUnitario.setText(oldVal);
            }
        });

        // Stock mínimo - solo enteros
        txtStockMinimo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockMinimo.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Stock máximo - solo enteros
        txtStockMaximo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtStockMaximo.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Carga los tipos de artículo en el ComboBox
     */
    private void cargarTiposArticulo() {
        ObservableList<String> tipos = FXCollections.observableArrayList(
                "VENTAS"
        );
        cbxTipoArticulo.setItems(tipos);
        cbxTipoArticulo.setValue("VENTAS");
    }

    /**
     * Carga las unidades de medida desde la base de datos
     */
    private void cargarUnidadesMedida() {
        Task<List<Arinum>> task = new Task<>() {
            @Override
            protected List<Arinum> call() throws Exception {
                ArinumDao dao = new ArinumDao();
                return dao.listarActivas(NO_CIA);
            }
        };

        task.setOnSucceeded(event -> {
            List<Arinum> unidades = task.getValue();
            ObservableList<Arinum> items = FXCollections.observableArrayList(unidades);
            cbxMedida.setItems(items);

            // Configurar el StringConverter para mostrar el nombre
            cbxMedida.setConverter(new StringConverter<Arinum>() {
                @Override
                public String toString(Arinum arinum) {
                    return arinum != null ? arinum.getNom() : "";
                }

                @Override
                public Arinum fromString(String string) {
                    return cbxMedida.getItems().stream()
                            .filter(item -> item.getNom().equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });

            // Seleccionar "UNIDAD" por defecto si existe
            cbxMedida.getItems().stream()
                    .filter(u -> "NIU".equals(u.getUnidad()) || "UND".equals(u.getUnidad()))
                    .findFirst()
                    .ifPresent(cbxMedida::setValue);

            LOGGER.info("Unidades de medida cargadas: " + unidades.size());

            // Si hay un artículo pendiente de cargar, seleccionar la medida
            if (articuloPendiente != null) {
                seleccionarMedida(articuloPendiente.getMedida());
            }
        });

        task.setOnFailed(event -> {
            LOGGER.log(Level.SEVERE, "Error al cargar unidades de medida", task.getException());
            Mensaje.error(null, "Error", "No se pudieron cargar las unidades de medida.");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Carga las monedas en el ComboBox
     */
    private void cargarMonedas() {
        ObservableList<String> monedas = FXCollections.observableArrayList(
                "SOLES",
                "DÓLARES"
        );
        cbxMoneda.setItems(monedas);
        cbxMoneda.setValue("SOLES");
    }

    /**
     * Configura las validaciones de los campos
     */
    private void configurarValidaciones() {
        // Validar stock máximo > stock mínimo
        txtStockMaximo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Cuando pierde el foco
                validarStock();
            }
        });

        txtStockMinimo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Cuando pierde el foco
                validarStock();
            }
        });
    }

    /**
     * Valida que el stock máximo sea mayor al mínimo
     */
    private void validarStock() {
        try {
            int minimo = txtStockMinimo.getText().isEmpty() ? 0 : Integer.parseInt(txtStockMinimo.getText());
            int maximo = txtStockMaximo.getText().isEmpty() ? 0 : Integer.parseInt(txtStockMaximo.getText());

            if (maximo > 0 && maximo < minimo) {
                txtStockMaximo.setStyle("-fx-border-color: #e74c3c;");
                hbxStockIndicator.setVisible(false);
            } else if (maximo > 0) {
                txtStockMaximo.setStyle("");
                hbxStockIndicator.setVisible(true);
            }
        } catch (NumberFormatException e) {
            // Ignorar si no son números válidos
        }
    }

    /**
     * Guarda el artículo en la base de datos
     */
    @FXML
    void guardarArticulo(ActionEvent event) throws SQLException {
        // Validar campos obligatorios
        if (!validarCamposObligatorios()) {
            return;
        }
        String registrar = btnRegistrar.getText();
        String mensaje = registrar.equals("REGISTRAR") ?
                "¿Está seguro de registrar el artículo?" :
                "¿Está seguro de actualizar el artículo?";
        if (Mensaje.confirmacion(null, "Confirmar", mensaje).get() != ButtonType.CANCEL) {
            // Mostrar indicador de carga
            mostrarCargando("Procesando...");

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return ejecutarGuardarArticulo();
                }
            };

            task.setOnSucceeded(e -> {
                ocultarCargando();
                String estadoEnv = registrar.equals("REGISTRAR") ? "registrado" : "actualizado";
                if (task.getValue()) {
                    Mensaje.alerta(null, "Éxito", "Artículo " + estadoEnv + " correctamente.");

                    // Si está en modo edición, cerrar el modal
                    if (modoEdicion) {
                        cerrarVentana();
                    } else {
                        limpiarFormulario();
                    }
                } else {
                    estadoEnv = registrar.equals("REGISTRAR") ? "registrar" : "actualizar";
                    Mensaje.error(null, "Error", "No se pudo " + estadoEnv + " el artículo.");
                }
            });

            task.setOnFailed(e -> {
                ocultarCargando();
                Throwable ex = task.getException();
                LOGGER.log(Level.SEVERE, "Error al guardar artículo", ex);
                Mensaje.error(null, "Error al guardar",
                        ex != null ? ex.getMessage() : "Error desconocido");
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Cierra la ventana actual (modal)
     */
    private void cerrarVentana() {
        Platform.runLater(() -> {
            Stage stage = (Stage) vbxPrincipal.getScene().getWindow();
            if (stage != null) {
                stage.close();
            }
        });
    }

    /**
     * Valida los campos obligatorios del formulario
     */
    private boolean validarCamposObligatorios() throws SQLException {
        StringBuilder errores = new StringBuilder();

        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            String noArti = Metodos.generarTextoAleatorio(10);
            txtCodigo.setText(noArti);
        } else {
            String registrar = btnRegistrar.getText();
            if (registrar.equals("REGISTRAR")) {
                Arinda1Dao dao = new Arinda1Dao();
                String existe = dao.validarCodigoExistente(NO_CIA, txtCodigo.getText().trim().toUpperCase());
                if (existe.equals("S")) {
                    txtCodigo.setStyle("-fx-border-color: #e74c3c;");
                    errores.append("• El código del artículo ya existe.\n");
                } else {
                    txtCodigo.setStyle("");
                }
            }
        }

        if (txtDescripcion.getText() == null || txtDescripcion.getText().trim().isEmpty()) {
            errores.append("• La descripción es obligatoria.\n");
            txtDescripcion.setStyle("-fx-border-color: #e74c3c;");
        } else {
            txtDescripcion.setStyle("");
        }

        if (cbxTipoArticulo.getValue() == null) {
            errores.append("• Debe seleccionar un tipo de artículo.\n");
            cbxTipoArticulo.setStyle("-fx-border-color: #e74c3c;");
        } else {
            cbxTipoArticulo.setStyle("");
        }

        if (cbxMedida.getValue() == null) {
            errores.append("• Debe seleccionar una unidad de medida.\n");
            cbxMedida.setStyle("-fx-border-color: #e74c3c;");
        } else {
            cbxMedida.setStyle("");
        }

        if (cbxMoneda.getValue() == null) {
            errores.append("• Debe seleccionar una moneda.\n");
            cbxMoneda.setStyle("-fx-border-color: #e74c3c;");
        } else {
            cbxMoneda.setStyle("");
        }

        if (txtCostoUnitario.getText() == null || txtCostoUnitario.getText().trim().isEmpty()) {
            errores.append("• El costo unitario es obligatorio.\n");
            txtCostoUnitario.setStyle("-fx-border-color: #e74c3c;");
        } else {
            txtCostoUnitario.setStyle("");
        }

        if (errores.length() > 0) {
            Mensaje.alerta(null, "Campos obligatorios", errores.toString());
            return false;
        }

        return true;
    }

    /**
     * Ejecuta el procedimiento almacenado para guardar el artículo
     */
    private boolean ejecutarGuardarArticulo() {
        boolean resultado = false;
        try {
            Arinda1Dao dao = new Arinda1Dao();
            resultado = dao.ejecutarGuardarArticulo(
                    NO_CIA,
                    obtenerCodigoTipoArticulo(),
                    txtCodigo.getText().trim().toUpperCase(),
                    txtDescripcion.getText().trim().toUpperCase(),
                    cbxMedida.getValue().getUnidad(),
                    obtenerCodigoMoneda(),
                    chkVigente.isSelected() ? "S" : "N",
                    obtenerCostoUnitario(),
                    obtenerStockMinimo(),
                    obtenerStockMaximo()
            );
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error al ejecutar procedimiento GUARDAR", ex);
        }
        return resultado;
    }

    /**
     * Obtiene el código del tipo de artículo seleccionado
     */
    private String obtenerCodigoTipoArticulo() {
        String valor = cbxTipoArticulo.getValue();
        if (valor.equals("VENTAS")) {
            valor = "1";
        }
        return valor;
    }

    /**
     * Obtiene el código de la moneda seleccionada
     */
    private String obtenerCodigoMoneda() {
        String valor = cbxMoneda.getValue();
        switch (valor) {
            case "DÓLARES":
                return "DOL";
            case "SOLES":
                return "SOL";
            default:
                break;
        }
        return valor;
    }

    /**
     * Obtiene el costo unitario como BigDecimal
     */
    private BigDecimal obtenerCostoUnitario() {
        try {
            String texto = txtCostoUnitario.getText();
            if (texto == null || texto.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Obtiene el stock mínimo como BigDecimal
     */
    private BigDecimal obtenerStockMinimo() {
        try {
            String texto = txtStockMinimo.getText();
            if (texto == null || texto.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Obtiene el stock máximo como BigDecimal
     */
    private BigDecimal obtenerStockMaximo() {
        try {
            String texto = txtStockMaximo.getText();
            if (texto == null || texto.trim().isEmpty()) {
                return BigDecimal.ZERO;
            }
            return new BigDecimal(texto.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Limpia el formulario después de guardar
     */
    private void limpiarFormulario() {
        txtCodigo.clear();
        txtDescripcion.clear();
        txtCostoUnitario.clear();
        txtStockMinimo.setText("0");
        txtStockMaximo.setText("0");
        txtStockActual.setText("0");
        chkVigente.setSelected(true);
        hbxStockIndicator.setVisible(false);

        // Resetear estilos
        txtCodigo.setStyle("");
        txtDescripcion.setStyle("");
        txtCostoUnitario.setStyle("");
        cbxTipoArticulo.setStyle("");
        cbxMedida.setStyle("");
        cbxMoneda.setStyle("");

        // Enfocar en el código
        txtCodigo.requestFocus();
    }

    // ==================== INDICADOR DE CARGA ====================

    /**
     * Muestra el indicador de carga modal
     */
    private void mostrarCargando(String mensaje) {
        Platform.runLater(() -> {
            try {
                loadingStage = new Stage();
                loadingStage.initStyle(StageStyle.UNDECORATED);
                loadingStage.initModality(Modality.APPLICATION_MODAL);

                if (vbxPrincipal.getScene() != null && vbxPrincipal.getScene().getWindow() != null) {
                    loadingStage.initOwner(vbxPrincipal.getScene().getWindow());
                }

                VBox vbox = new VBox(15);
                vbox.setAlignment(Pos.CENTER);
                vbox.setStyle("-fx-background-color: white; -fx-padding: 30; " +
                        "-fx-background-radius: 10; " +
                        "-fx-border-color: #e2e8f0; -fx-border-radius: 10; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 20, 0, 0, 5);");

                ProgressIndicator progress = new ProgressIndicator();
                progress.setStyle("-fx-progress-color: #f39c12;");
                progress.setPrefSize(50, 50);

                Label label = new Label(mensaje);
                label.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-text-fill: #475569;");

                vbox.getChildren().addAll(progress, label);

                Scene scene = new Scene(vbox);
                scene.setFill(null);
                loadingStage.setScene(scene);
                loadingStage.show();
                loadingStage.centerOnScreen();

            } catch (Exception e) {
                LOGGER.warning("No se pudo mostrar indicador de carga: " + e.getMessage());
            }
        });
    }

    /**
     * Oculta el indicador de carga
     */
    private void ocultarCargando() {
        Platform.runLater(() -> {
            try {
                if (loadingStage != null) {
                    loadingStage.close();
                    loadingStage = null;
                }
            } catch (Exception e) {
                LOGGER.warning("Error al ocultar indicador de carga: " + e.getMessage());
            }
        });
    }

    // ==================== MÉTODOS PÚBLICOS ====================

    /**
     * Permite cargar un artículo para edición
     */
    public void cargarArticulo(Arinda1 arinda1) {
        // Guardar referencia para cargar la medida cuando el combo esté listo
        this.articuloPendiente = arinda1;

        txtCodigo.setText(arinda1.getCodigo());
        txtDescripcion.setText(arinda1.getDescripcion());

        // Checkbox vigente
        chkVigente.setSelected("S".equals(arinda1.getVigente()));

        // Costo unitario
        if (arinda1.getCostoUni() != null) {
            txtCostoUnitario.setText(arinda1.getCostoUni().toString());
        } else {
            txtCostoUnitario.setText("0");
        }

        // Stock
        if (arinda1.getStkMinimo() != null) {
            txtStockMinimo.setText(arinda1.getStkMinimo().toString());
        }
        if (arinda1.getStkMaximo() != null) {
            txtStockMaximo.setText(arinda1.getStkMaximo().toString());
        }

        // Seleccionar moneda
        seleccionarMoneda(arinda1.getMoneda());

        // Intentar seleccionar la medida (puede que el combo aún no esté cargado)
        if (cbxMedida.getItems() != null && !cbxMedida.getItems().isEmpty()) {
            seleccionarMedida(arinda1.getMedida());
        }
        // Si el combo no está cargado, se seleccionará en el callback de cargarUnidadesMedida()

        txtCodigo.setDisable(true); // No permitir editar el código
    }

    /**
     * Selecciona la moneda en el ComboBox según el código de la base de datos
     *
     * @param codigoMoneda Código de moneda (SOL, DOL, S, D, etc.)
     */
    private void seleccionarMoneda(String codigoMoneda) {
        if (codigoMoneda == null) {
            cbxMoneda.setValue("SOLES");
            return;
        }

        switch (codigoMoneda.toUpperCase().trim()) {
            case "SOL":
            case "S":
            case "PEN":
            case "SOLES":
                cbxMoneda.setValue("SOLES");
                break;
            case "DOL":
            case "D":
            case "USD":
            case "DOLARES":
            case "DÓLARES":
                cbxMoneda.setValue("DÓLARES");
                break;
            default:
                // Por defecto SOLES
                cbxMoneda.setValue("SOLES");
                LOGGER.warning("Código de moneda no reconocido: " + codigoMoneda + ". Se seleccionó SOLES por defecto.");
                break;
        }
    }

    /**
     * Selecciona la unidad de medida en el ComboBox según el código
     *
     * @param codigoMedida Código de la unidad de medida
     */
    private void seleccionarMedida(String codigoMedida) {
        if (codigoMedida == null || cbxMedida.getItems() == null) {
            return;
        }

        // Buscar la unidad de medida por código
        cbxMedida.getItems().stream()
                .filter(u -> codigoMedida.equals(u.getUnidad()))
                .findFirst()
                .ifPresentOrElse(
                        cbxMedida::setValue,
                        () -> LOGGER.warning("Unidad de medida no encontrada: " + codigoMedida)
                );
    }

    /**
     * Establece el modo de edición
     */
    public void setModoEdicion(boolean edicion) {
        this.modoEdicion = edicion;
        txtCodigo.setDisable(edicion);
        btnRegistrar.setText(edicion ? "ACTUALIZAR" : "REGISTRAR");
    }
}