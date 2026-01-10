package com.robin.pos.controller;

import com.robin.pos.dao.ArccmcDao;
import com.robin.pos.model.Arccmc;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador mejorado para la Lista de Clientes
 * Incluye búsqueda avanzada, atajos de teclado y mejor UX
 *
 * @author Robin POS
 * @version 2.0
 */
public class ListaClienteController implements Initializable {

    // === COMPONENTES FXML ===
    @FXML private BorderPane root;
    @FXML private TableView<Arccmc> tListaCliente;
    @FXML private TextField txtBuscarCliente;

    // Columnas de la tabla
    @FXML private TableColumn<Arccmc, String> colCodigo;
    @FXML private TableColumn<Arccmc, String> colNombre;
    @FXML private TableColumn<Arccmc, String> colDocumento;
    @FXML private TableColumn<Arccmc, String> colTipCliente;
    @FXML private TableColumn<Arccmc, String> colTipPersona;
    @FXML private TableColumn<Arccmc, String> colEstado;

    // Labels
    @FXML private Label lblContador;
    @FXML private Label lblEstado;
    @FXML private Label lblListaCliente;

    // Botones
    @FXML private Button btnNuevoCliente;
    @FXML private Button btnEditarCliente;
    @FXML private Button btnRefrescar;

    // Contenedores
    @FXML private StackPane tableContainer;
    @FXML private VBox vbxPrincipal;
    @FXML private HBox hbxCabecera;

    // === DATOS ===
    private final ObservableList<Arccmc> listaClientes = FXCollections.observableArrayList();
    private FilteredList<Arccmc> filteredData;
    private SortedList<Arccmc> sortedData;

    // === CONSTANTES ===
    private static final String NO_CIA = "01";
    private static final String MSG_CARGANDO = "Cargando clientes...";
    private static final String MSG_LISTO = "Listo";
    private static final String MSG_ERROR = "Error al cargar los datos";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarColumnas();
        configurarFiltro();
        configurarTabla();
        configurarAtajosTeclado();
        cargarClientes();
    }

    /**
     * Configura las columnas de la tabla con sus respectivos valores
     */
    private void configurarColumnas() {
        // Columnas básicas
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("noCliente"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipCliente.setCellValueFactory(new PropertyValueFactory<>("tipoCliente"));
        colTipPersona.setCellValueFactory(new PropertyValueFactory<>("tipoPersona"));

        // Columna de documento (si existe el campo)
        if (colDocumento != null) {
            colDocumento.setCellValueFactory(new PropertyValueFactory<>("nuDocumento"));
        }

        // Columna de estado con formato personalizado
        colEstado.setCellValueFactory(new PropertyValueFactory<>("activo"));
        colEstado.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Label badge = new Label();
                    badge.setAlignment(Pos.CENTER);
                    badge.setMaxWidth(Double.MAX_VALUE);

                    if ("S".equalsIgnoreCase(item)) {
                        badge.setText("Activo");
                        badge.setStyle(
                                "-fx-background-color: #dcfce7;" +
                                        "-fx-background-radius: 12;" +
                                        "-fx-padding: 4 10;" +
                                        "-fx-text-fill: #166534;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;"
                        );
                    } else {
                        badge.setText("Inactivo");
                        badge.setStyle(
                                "-fx-background-color: #fee2e2;" +
                                        "-fx-background-radius: 12;" +
                                        "-fx-padding: 4 10;" +
                                        "-fx-text-fill: #991b1b;" +
                                        "-fx-font-size: 11px;" +
                                        "-fx-font-weight: bold;"
                        );
                    }

                    HBox container = new HBox(badge);
                    container.setAlignment(Pos.CENTER);
                    setGraphic(container);
                    setText(null);
                }
            }
        });

        // Columna de tipo cliente con formato legible
        colTipCliente.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTipoCliente(item));
                }
            }
        });

        // Columna de tipo persona con formato legible
        colTipPersona.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTipoPersona(item));
                }
            }
        });
    }

    /**
     * Configura el filtro de búsqueda con múltiples criterios
     */
    private void configurarFiltro() {
        filteredData = new FilteredList<>(listaClientes, p -> true);

        txtBuscarCliente.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(cliente -> {
                // Si el filtro está vacío, mostrar todos
                if (newValue == null || newValue.trim().isEmpty()) {
                    return true;
                }

                String filtroLower = newValue.toLowerCase().trim();

                // Buscar en múltiples campos
                if (safeContains(cliente.getNoCliente(), filtroLower)) return true;
                if (safeContains(cliente.getNombre(), filtroLower)) return true;
                /*
                if (safeContains(cliente.getRuc(), filtroLower)) return true;
                if (safeContains(cliente.getNuDocumento(), filtroLower)) return true;
                */
                return false;
            });

            actualizarContador();
        });

        sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tListaCliente.comparatorProperty());
        tListaCliente.setItems(sortedData);
    }

    /**
     * Configura comportamientos adicionales de la tabla
     */
    private void configurarTabla() {
        // Doble clic para editar
        tListaCliente.setOnMouseClicked(this::handleTableClick);

        // Placeholder personalizado cuando no hay datos
        Label placeholder = new Label("No se encontraron clientes");
        placeholder.setStyle(
                "-fx-font-size: 14px;" +
                        "-fx-text-fill: #94a3b8;"
        );
        tListaCliente.setPlaceholder(placeholder);

        // Selección de fila completa
        tListaCliente.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        // Listener para habilitar/deshabilitar botón editar
        tListaCliente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (btnEditarCliente != null) {
                btnEditarCliente.setDisable(newVal == null);
            }
        });
    }

    /**
     * Configura los atajos de teclado globales
     */
    private void configurarAtajosTeclado() {
        Platform.runLater(() -> {
            if (root.getScene() != null) {
                root.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    switch (event.getCode()) {
                        case F2 -> {
                            editarClienteSeleccionado();
                            event.consume();
                        }
                        case F3 -> {
                            try {
                                crearCliente(null);
                            } catch (IOException e) {
                                mostrarError("Error al abrir formulario", e.getMessage());
                            }
                            event.consume();
                        }
                        case F5 -> {
                            refrescarLista(null);
                            event.consume();
                        }
                        case ESCAPE -> {
                            cerrarVentana();
                            event.consume();
                        }
                        case F -> {
                            if (event.isControlDown()) {
                                txtBuscarCliente.requestFocus();
                                txtBuscarCliente.selectAll();
                                event.consume();
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * Carga los clientes desde la base de datos de forma asíncrona
     */
    private void cargarClientes() {
        actualizarEstado(MSG_CARGANDO);

        // Mostrar indicador de progreso
        ProgressIndicator progress = new ProgressIndicator();
        progress.setMaxSize(50, 50);
        tListaCliente.setPlaceholder(progress);

        Task<List<Arccmc>> task = new Task<>() {
            @Override
            protected List<Arccmc> call() throws Exception {
                ArccmcDao dao = new ArccmcDao();
                return dao.listar(NO_CIA);
            }
        };

        task.setOnSucceeded(event -> {
            listaClientes.setAll(task.getValue());
            actualizarContador();
            actualizarEstado(MSG_LISTO);
            restaurarPlaceholder();
        });

        task.setOnFailed(event -> {
            actualizarEstado(MSG_ERROR);
            restaurarPlaceholder();

            Throwable ex = task.getException();
            if (ex != null) {
                mostrarError("Error de conexión",
                        "No se pudieron cargar los clientes: " + ex.getMessage());
            }
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    // === ACCIONES DE BOTONES ===

    @FXML
    void crearCliente(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/robin/pos/fxml/Cliente.fxml")
        );
        VBox vbox = loader.load();

        Scene scene = new Scene(vbox);
        Stage stage = new Stage();

        stage.setTitle("Nuevo Cliente");
        stage.setScene(scene);
        stage.initOwner(root.getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);

        // Recargar lista al cerrar el formulario
        stage.setOnHidden(e -> cargarClientes());

        stage.showAndWait();
    }

    @FXML
    void editarClienteAction(ActionEvent event) {
        editarClienteSeleccionado();
    }

    @FXML
    void refrescarLista(ActionEvent event) {
        txtBuscarCliente.clear();
        cargarClientes();
    }

    @FXML
    void buscarCliente(KeyEvent event) {
        switch (event.getCode()) {
            case DOWN -> {
                tListaCliente.requestFocus();
                if (!tListaCliente.getItems().isEmpty()) {
                    tListaCliente.getSelectionModel().selectFirst();
                }
            }
            case ENTER -> {
                if (!tListaCliente.getItems().isEmpty()) {
                    tListaCliente.requestFocus();
                    tListaCliente.getSelectionModel().selectFirst();
                }
            }
            case ESCAPE -> {
                txtBuscarCliente.clear();
                tListaCliente.requestFocus();
            }
        }
    }

    // === MÉTODOS AUXILIARES ===

    private void editarClienteSeleccionado() {
        Arccmc clienteSeleccionado = tListaCliente.getSelectionModel().getSelectedItem();

        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Selección requerida",
                    "Por favor, seleccione un cliente para editar.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/robin/pos/fxml/Cliente.fxml")
            );
            VBox vbox = loader.load();

            // Obtener el controlador y pasar el cliente a editar
            // ClienteController controller = loader.getController();
            // controller.setCliente(clienteSeleccionado);

            Scene scene = new Scene(vbox);
            Stage stage = new Stage();

            stage.setTitle("Editar Cliente - " + clienteSeleccionado.getNombre());
            stage.setScene(scene);
            stage.initOwner(root.getScene().getWindow());
            stage.initModality(Modality.WINDOW_MODAL);
            stage.setResizable(false);

            stage.setOnHidden(e -> cargarClientes());

            stage.showAndWait();

        } catch (IOException e) {
            mostrarError("Error", "No se pudo abrir el formulario de edición: " + e.getMessage());
        }
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
            editarClienteSeleccionado();
        }
    }

    private void actualizarContador() {
        if (lblContador != null) {
            int total = listaClientes.size();
            int filtrados = filteredData.size();

            if (total == filtrados) {
                lblContador.setText(total + " cliente" + (total != 1 ? "s" : ""));
            } else {
                lblContador.setText(filtrados + " de " + total + " clientes");
            }
        }
    }

    private void actualizarEstado(String mensaje) {
        if (lblEstado != null) {
            lblEstado.setText(mensaje);
        }
    }

    private void restaurarPlaceholder() {
        Label placeholder = new Label("No se encontraron clientes");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #94a3b8;");
        tListaCliente.setPlaceholder(placeholder);
    }

    private void cerrarVentana() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }

    private boolean safeContains(String value, String search) {
        return value != null && value.toLowerCase().contains(search);
    }

    private String formatTipoCliente(String tipo) {
        if (tipo == null) return "";
        return switch (tipo.toUpperCase()) {
            case "N" -> "Normal";
            case "E" -> "Especial";
            case "M" -> "Mayorista";
            case "V" -> "VIP";
            default -> tipo;
        };
    }

    private String formatTipoPersona(String tipo) {
        if (tipo == null) return "";
        return switch (tipo.toUpperCase()) {
            case "N" -> "Natural";
            case "J" -> "Jurídica";
            default -> tipo;
        };
    }

    // === DIÁLOGOS ===

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.initOwner(root.getScene().getWindow());
        estilizarDialogo(alert);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        alert.initOwner(root.getScene().getWindow());
        estilizarDialogo(alert);
        alert.showAndWait();
    }

    private void estilizarDialogo(Alert alert) {
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle(
                "-fx-font-family: 'Segoe UI', sans-serif;" +
                        "-fx-background-color: white;"
        );
    }

    // === MÉTODOS PÚBLICOS PARA ACCESO EXTERNO ===

    /**
     * Permite refrescar la lista desde otro controlador
     */
    public void refresh() {
        cargarClientes();
    }

    /**
     * Permite buscar un cliente específico
     * @param criterio texto a buscar
     */
    public void buscar(String criterio) {
        txtBuscarCliente.setText(criterio);
    }

    /**
     * Obtiene el cliente actualmente seleccionado
     * @return cliente seleccionado o null
     */
    public Arccmc getClienteSeleccionado() {
        return tListaCliente.getSelectionModel().getSelectedItem();
    }
}