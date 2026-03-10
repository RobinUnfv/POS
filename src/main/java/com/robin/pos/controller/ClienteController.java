package com.robin.pos.controller;

import com.robin.pos.dao.*;
import com.robin.pos.model.*;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador mejorado para el formulario de Cliente
 * Incluye validación en tiempo real, atajos de teclado y mejor UX
 *
 * @author Robin POS
 * @version 2.1
 */
public class ClienteController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(ClienteController.class.getName());

    // === COMPONENTES FXML ===
    @FXML private VBox vbxPrincipal;
    @FXML private VBox vbxCuerpo;
    @FXML private HBox hbxPie;

    // Secciones del formulario
    @FXML private GridPane gpDocumento;
    @FXML private GridPane gpDos;
    //@FXML private VBox gpTres;
    @FXML private VBox gpCuatro;
    @FXML private VBox gpCinco;
    @FXML private VBox gpSeis;
    @FXML private VBox gpContacto;
    @FXML private VBox vbxNacionalidad;

    // Labels
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblTipNum;
    @FXML private Label lblTipPersona;
    @FXML private Label lblNacionalidad;

    // ComboBox
    @FXML private ComboBox<String> cbxTipDoc;
    @FXML private ComboBox<Arccdp> cbxDepartamento;
    @FXML private ComboBox<Arccpr> cbxProvincia;
    @FXML private ComboBox<Arccdi> cbxDistrito;

    // TextField
    @FXML private TextField txtNumDoc;
    /*
    @FXML private TextField txtApePat;
    @FXML private TextField txtApeMat;
    @FXML private TextField txtPriNom;
    @FXML private TextField txtSegNom;
    */
    @FXML private TextField txtRazSocial;
    @FXML private TextField txtDirec;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;

    // RadioButton
    @FXML private RadioButton rbnJuridico;
    @FXML private RadioButton rbnNatural;
    @FXML private RadioButton rbnNacional;
    @FXML private RadioButton rbnExtranjero;

    // CheckBox
    @FXML private CheckBox chkActivo;

    // Botones
    @FXML private Button btnRegistrar;
    @FXML private Button btnSalir;


    @FXML
    private Label lblEmpresa;

    @FXML
    private Label lblNombre;

    // === GRUPOS DE TOGGLE ===
    private ToggleGroup tipoPersonaGroup;
    private ToggleGroup nacionalidadGroup;

    // === CONSTANTES ===
    private static final String NO_CIA = "01";
    private static final String ESTILO_ERROR = "validation-error";
    private static final String ESTILO_EXITO = "validation-success";

    // === MODO EDICIÓN ===
    private boolean modoEdicion = false;
    private Cliente clienteActual;

    // Stage para indicador de carga
    private Stage loadingStage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTipoDocumento();
        configurarRadioButtons();
        configurarListeners();
        configurarAtajosTeclado();
        cargarDepartamentos(NO_CIA);

        // Listener para cambio de tipo de documento
        cbxTipDoc.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!modoEdicion) {
                txtNumDoc.setText("");
            }
            limpiarEstilosValidacion(txtNumDoc);
            updateVisibilityByTipoDoc(newVal);
            Metodos.configuracionNumeroDocumento(txtNumDoc, newVal != null ? newVal : "RUC");
        });

        updateVisibilityByTipoDoc("RUC");

        // Enfocar campo de documento al iniciar
        Platform.runLater(() -> txtNumDoc.requestFocus());
    }

    /**
     * Configura el ComboBox de tipo de documento
     */
    private void configurarTipoDocumento() {
        cbxTipDoc.getItems().addAll("RUC", "DNI", "CE");
        cbxTipDoc.setValue("RUC");
        Metodos.configuracionNumeroDocumento(txtNumDoc, "RUC");
    }

    /**
     * Configura los RadioButtons con sus grupos
     */
    private void configurarRadioButtons() {
        tipoPersonaGroup = new ToggleGroup();
        nacionalidadGroup = new ToggleGroup();

        rbnJuridico.setToggleGroup(tipoPersonaGroup);
        rbnNatural.setToggleGroup(tipoPersonaGroup);
        rbnNacional.setToggleGroup(nacionalidadGroup);
        rbnExtranjero.setToggleGroup(nacionalidadGroup);

        rbnNacional.setUserData("N");
        rbnExtranjero.setUserData("S");
        rbnJuridico.setUserData("J");
        rbnNatural.setUserData("N");

        // Selección por defecto
        rbnJuridico.setSelected(true);
        rbnNacional.setSelected(true);
    }

    /**
     * Configura listeners para validación en tiempo real
     */
    private void configurarListeners() {
        // Validación en tiempo real del número de documento
        txtNumDoc.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty()) {
                validarCampoEnTiempoReal(txtNumDoc, validarLongitudDocumento(newVal));
            } else {
                limpiarEstilosValidacion(txtNumDoc);
            }
        });

        // Convertir a mayúsculas automáticamente
        configurarMayusculas(txtRazSocial);
        configurarMayusculas(txtDirec);

        // Validar email
        if (txtEmail != null) {
            txtEmail.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.isEmpty()) {
                    txtEmail.setText(newVal.toLowerCase());
                }
            });
        }

        // Validar teléfono (solo números)
        if (txtTelefono != null) {
            txtTelefono.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.matches("\\d*")) {
                    txtTelefono.setText(newVal.replaceAll("[^\\d]", ""));
                }
            });
        }
    }

    /**
     * Configura un campo para convertir texto a mayúsculas
     */
    public void configurarMayusculas(TextField campo) {
        if (campo != null) {
            campo.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && !newVal.equals(newVal.toUpperCase())) {
                    campo.setText(newVal.toUpperCase());
                }
            });
        }
    }

    /**
     * Configura atajos de teclado
     */
    private void configurarAtajosTeclado() {
        Platform.runLater(() -> {
            if (vbxPrincipal.getScene() != null) {
                vbxPrincipal.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                    if (event.getCode() == KeyCode.ESCAPE) {
                        cerrarModal(null);
                        event.consume();
                    } else if (event.isControlDown() && event.getCode() == KeyCode.S) {
                        guardarCliente(null);
                        event.consume();
                    }
                });
            }
        });
    }

    /**
     * Actualiza la visibilidad de campos según el tipo de documento
     */
    private void updateVisibilityByTipoDoc(String tipoDoc) {
        boolean isRUC = "RUC".equals(tipoDoc);
        boolean isDNI = "DNI".equals(tipoDoc);
        boolean isCE = "CE".equals(tipoDoc);

        // Mostrar/ocultar nacionalidad
        if (vbxNacionalidad != null) {
            vbxNacionalidad.setVisible(isDNI || isCE);
            vbxNacionalidad.setManaged(isDNI || isCE);
        }

        // Datos personales (DNI/CE) vs Razón Social (RUC)
        /*
        gpTres.setVisible(!isRUC);
        gpTres.setManaged(!isRUC);
        */
        gpCuatro.setVisible(true);
        gpCuatro.setManaged(true);

        // Caso especial para CE


        // Actualizar etiquetas y selecciones
        if (isRUC) {
            lblEmpresa.setText("Datos de la Empresa");
            lblNombre.setText("Razón Social*");
            txtRazSocial.setPromptText("Ingrese la razón social de la empresa");
            lblTipNum.setText("Número de RUC");
            rbnJuridico.setSelected(true);
            rbnNacional.setSelected(true);
            if (lblSubtitulo != null && !modoEdicion) {
                lblSubtitulo.setText("Complete los datos de la empresa");
            }
        } else if (isDNI) {
            lblEmpresa.setText("Datos Personales");
            lblNombre.setText("Apellido Paterno Materno Nombre*");
            txtRazSocial.setPromptText("Ingrese los apellidos y nombres del cliente");
            lblTipNum.setText("Número de DNI");
            rbnNatural.setSelected(true);
            rbnNacional.setSelected(true);
            if (lblSubtitulo != null && !modoEdicion) {
                lblSubtitulo.setText("Complete los datos personales del cliente");
            }
        } else if (isCE) {
            lblTipNum.setText("Número de CE");
            lblEmpresa.setText("Datos Personales");
            lblNombre.setText("Nombre*");
            txtRazSocial.setPromptText("Ingrese el nombre del cliente");
            rbnNatural.setSelected(true);
            rbnExtranjero.setSelected(true);
            if (lblSubtitulo != null && !modoEdicion) {
                lblSubtitulo.setText("Complete los datos del cliente extranjero");
            }
        }
    }

    // === ACCIONES DE FORMULARIO ===

    @FXML
    void cerrarModal(ActionEvent event) {
        Stage stage = (Stage) vbxPrincipal.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    @FXML
    void guardarCliente(ActionEvent event) {
        // Validar número de documento
        String numDoc = txtNumDoc.getText().trim();
        if (numDoc.isEmpty()) {
            mostrarErrorCampo(txtNumDoc, "Debe ingresar un número de documento.");
            return;
        }

        // Validar según tipo de documento
        String tipoDoc = cbxTipDoc.getValue();
        if (!validarFormularioSegunTipo(tipoDoc)) {
            return;
        }

        // Mensaje de confirmación
        String accion = modoEdicion ? "actualizar" : "registrar";
        String mensaje = "¿Está seguro de " + accion + " este cliente?";

        if (Mensaje.confirmacion(null, "Confirmar", mensaje).get() == ButtonType.OK) {
            // Mostrar indicador de carga
            mostrarCargando(modoEdicion ? "Actualizando cliente..." : "Registrando cliente...");

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    return ejecutarGuardarCliente();
                }
            };

            task.setOnSucceeded(e -> {
                ocultarCargando();
                if (task.getValue()) {
                    String accionPasada = modoEdicion ? "actualizado" : "registrado";
                    Mensaje.alerta(null, "Éxito", "Cliente " + accionPasada + " correctamente.");
                    cerrarModal(null);
                } else {
                    Mensaje.error(null, "Error", "No se pudo " + accion + " el cliente.");
                }
            });

            task.setOnFailed(e -> {
                ocultarCargando();
                Throwable ex = task.getException();
                LOGGER.log(Level.SEVERE, "Error al guardar cliente", ex);
                Mensaje.error(null, "Error", ex != null ? ex.getMessage() : "Error desconocido");
            });

            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    /**
     * Ejecuta el guardado del cliente usando el DAO
     */
    private boolean ejecutarGuardarCliente() throws SQLException {
        Cliente cliente = obtenerDatosClienteParaGuardar();
        ClienteDao dao = new ClienteDao();
        return dao.guardarCliente(cliente);
    }

    /**
     * Obtiene los datos del cliente del formulario para guardar
     */
    private Cliente obtenerDatosClienteParaGuardar() {
        Cliente cliente = new Cliente();
        String tipoDoc = cbxTipDoc.getValue();

        cliente.setNoCia(NO_CIA);
        cliente.setNoCliente(txtNumDoc.getText().trim());
        cliente.setTipoDocumento(tipoDoc);
        cliente.setNombre(txtRazSocial.getText().trim());
        switch (tipoDoc) {
            case "RUC" -> {
                this.lblEmpresa.setText("Datos de la Empresa");
                this.lblNombre.setText("Razón Social *");
                cliente.setTipoPersona("J");
            }
            case "DNI" -> {
                this.lblEmpresa.setText("Datos Personales");
                this.lblNombre.setText("Apellido Paterno Materno Nombre*");
                cliente.setTipoPersona("N");
            }
            case "CE" -> {
                this.lblEmpresa.setText("Datos Personales");
                this.lblNombre.setText("Nombre*");
                cliente.setTipoPersona("N");
            }
        }

        cliente.setDireccion(txtDirec.getText().trim());
        cliente.setTelefono(txtTelefono != null ? txtTelefono.getText().trim() : "");
        cliente.setEmail(txtEmail != null ? txtEmail.getText().trim() : "");
        cliente.setActivo(chkActivo != null && chkActivo.isSelected() ? "S" : "N");
        cliente.setExtranjero(rbnExtranjero.isSelected() ? "S" : "N");

        // Ubigeo
        Arccdp dep = cbxDepartamento.getValue();
        Arccpr prov = cbxProvincia.getValue();
        Arccdi dist = cbxDistrito.getValue();

        if (dep != null) cliente.setCodiDepa(dep.getCodDepa());
        if (prov != null) cliente.setCodiProv(prov.getCodiProv());
        if (dist != null) cliente.setCodiDist(dist.getCodiDist());

        return cliente;
    }

    /**
     * Valida el formulario según el tipo de documento seleccionado
     */
    private boolean validarFormularioSegunTipo(String tipoDoc) {
        return switch (tipoDoc) {
            case "RUC" -> validarCamposRuc();
            case "DNI" -> validarCamposDni();
            case "CE" -> validarCamposCE();
            default -> false;
        };
    }

    private boolean validarCamposRuc() {
        String numDoc = txtNumDoc.getText().trim();

        if (numDoc.length() != 11) {
            mostrarErrorCampo(txtNumDoc, "El número de RUC debe tener 11 dígitos.");
            return false;
        }

        if (txtRazSocial.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtRazSocial, "Debe ingresar la razón social.");
            return false;
        }

        if (txtDirec.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtDirec, "Debe ingresar la dirección.");
            return false;
        }

        return validarUbigeo();
    }

    private boolean validarCamposDni() {
        String numDoc = txtNumDoc.getText().trim();

        if (numDoc.length() != 8) {
            mostrarErrorCampo(txtNumDoc, "El número de DNI debe tener 8 dígitos.");
            return false;
        }
        /*
        if (txtApePat.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtApePat, "Debe ingresar el apellido paterno.");
            return false;
        }

        if (txtApeMat.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtApeMat, "Debe ingresar el apellido materno.");
            return false;
        }

        if (txtPriNom.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtPriNom, "Debe ingresar el primer nombre.");
            return false;
        }
        */
        if (txtDirec.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtDirec, "Debe ingresar la dirección.");
            return false;
        }

        return validarUbigeo();
    }

    private boolean validarCamposCE() {
        String numDoc = txtNumDoc.getText().trim();

        if (numDoc.length() < 9 || numDoc.length() > 12) {
            mostrarErrorCampo(txtNumDoc, "El número de CE debe tener entre 9 y 12 caracteres.");
            return false;
        }



        return true;
    }

    private boolean validarUbigeo() {
        if (cbxDepartamento.getValue() == null) {
            Mensaje.alerta(null, "Error", "Debe seleccionar un departamento.");
            cbxDepartamento.requestFocus();
            return false;
        }

        if (cbxProvincia.getValue() == null) {
            Mensaje.alerta(null, "Error", "Debe seleccionar una provincia.");
            cbxProvincia.requestFocus();
            return false;
        }

        if (cbxDistrito.getValue() == null) {
            Mensaje.alerta(null, "Error", "Debe seleccionar un distrito.");
            cbxDistrito.requestFocus();
            return false;
        }

        return true;
    }



    // === CARGA DE UBIGEO ===
    private void cargarDepartamentos(String noCia) {
        ArccdpDao dao = new ArccdpDao();
        List<Arccdp> lista = dao.listarDepartamentos(noCia);

        if (lista != null) {
            cbxDepartamento.getItems().setAll(lista);
        }

        cbxDepartamento.setConverter(new StringConverter<>() {
            @Override
            public String toString(Arccdp item) {
                return item == null ? "" : item.getDesDepa();
            }

            @Override
            public Arccdp fromString(String string) {
                return cbxDepartamento.getItems().stream()
                        .filter(d -> d.getDesDepa().equals(string))
                        .findFirst().orElse(null);
            }
        });

        cbxDepartamento.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Arccdp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDesDepa());
            }
        });
    }

    @FXML
    void buscarProvincias(ActionEvent event) {
        Arccdp departamento = cbxDepartamento.getValue();
        cbxProvincia.getItems().clear();
        cbxDistrito.getItems().clear();

        if (departamento == null) return;

        ArccprDao dao = new ArccprDao();
        List<Arccpr> lista = dao.listaProvincias(NO_CIA, departamento.getCodDepa());

        if (lista != null) {
            cbxProvincia.getItems().setAll(lista);
        }

        cbxProvincia.setConverter(new StringConverter<>() {
            @Override
            public String toString(Arccpr item) {
                return item == null ? "" : item.getDescProv();
            }

            @Override
            public Arccpr fromString(String string) {
                return cbxProvincia.getItems().stream()
                        .filter(p -> p.getDescProv().equals(string))
                        .findFirst().orElse(null);
            }
        });

        cbxProvincia.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Arccpr item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDescProv());
            }
        });

        // No seleccionar automáticamente si estamos en modo edición
        if (!modoEdicion && !cbxProvincia.getItems().isEmpty()) {
            cbxProvincia.getSelectionModel().selectFirst();
            buscarDistrito(null);
        }
    }

    @FXML
    void buscarDistrito(ActionEvent event) {
        Arccdp departamento = cbxDepartamento.getValue();
        Arccpr provincia = cbxProvincia.getValue();
        cbxDistrito.getItems().clear();

        if (departamento == null || provincia == null) return;

        ArccdiDao dao = new ArccdiDao();
        List<Arccdi> lista = dao.listaDistrito(NO_CIA, departamento.getCodDepa(), provincia.getCodiProv());

        if (lista != null) {
            cbxDistrito.getItems().setAll(lista);
        }

        cbxDistrito.setConverter(new StringConverter<>() {
            @Override
            public String toString(Arccdi item) {
                return item == null ? "" : item.getDescDist();
            }

            @Override
            public Arccdi fromString(String string) {
                return cbxDistrito.getItems().stream()
                        .filter(d -> d.getDescDist().equals(string))
                        .findFirst().orElse(null);
            }
        });

        cbxDistrito.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Arccdi item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDescDist());
            }
        });

        // No seleccionar automáticamente si estamos en modo edición
        if (!modoEdicion && !cbxDistrito.getItems().isEmpty()) {
            cbxDistrito.getSelectionModel().selectFirst();
        }
    }

    // === MÉTODOS DE VALIDACIÓN VISUAL ===

    private void mostrarErrorCampo(TextField campo, String mensaje) {
        campo.getStyleClass().add(ESTILO_ERROR);
        Mensaje.alerta(null, "Validación", mensaje);
        Platform.runLater(() -> {
            campo.requestFocus();
            campo.selectAll();
        });
    }

    private void validarCampoEnTiempoReal(TextField campo, boolean esValido) {
        campo.getStyleClass().removeAll(ESTILO_ERROR, ESTILO_EXITO);
        if (esValido) {
            campo.getStyleClass().add(ESTILO_EXITO);
        }
    }

    private void limpiarEstilosValidacion(TextField campo) {
        campo.getStyleClass().removeAll(ESTILO_ERROR, ESTILO_EXITO);
    }

    private boolean validarLongitudDocumento(String valor) {
        String tipoDoc = cbxTipDoc.getValue();
        if (tipoDoc == null) return false;

        return switch (tipoDoc) {
            case "RUC" -> valor.length() == 11;
            case "DNI" -> valor.length() == 8;
            case "CE" -> valor.length() >= 9 && valor.length() <= 12;
            default -> false;
        };
    }

    // === INDICADOR DE CARGA ===

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
                progress.setStyle("-fx-progress-color: #3b82f6;");
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

    // === MÉTODOS PÚBLICOS ===

    /**
     * Limpia todos los campos del formulario
     */
    public void limpiarFormulario() {
        txtNumDoc.clear();
        txtRazSocial.clear();

        txtDirec.clear();
        if (txtTelefono != null) txtTelefono.clear();
        if (txtEmail != null) txtEmail.clear();
        if (chkActivo != null) chkActivo.setSelected(true);

        cbxDepartamento.getSelectionModel().clearSelection();
        cbxProvincia.getItems().clear();
        cbxDistrito.getItems().clear();

        cbxTipDoc.setValue("RUC");
        Metodos.configuracionNumeroDocumento(txtNumDoc, "RUC");

        limpiarEstilosValidacion(txtNumDoc);
        limpiarEstilosValidacion(txtRazSocial);

        limpiarEstilosValidacion(txtDirec);

        Platform.runLater(() -> txtNumDoc.requestFocus());
    }

    /**
     * Carga un cliente para edición
     *
     * @param cliente Cliente a editar
     */
    public void cargarClienteParaEditar(Cliente cliente) {
        if (cliente == null) return;

        this.modoEdicion = true;
        this.clienteActual = cliente;

        // Actualizar títulos
        if (lblTitulo != null) {
            lblTitulo.setText("Editar Cliente");
        }
        if (lblSubtitulo != null) {
            lblSubtitulo.setText("Modifique los datos del cliente");
        }
        if (btnRegistrar != null) {
            btnRegistrar.setText("Actualizar");
        }

        // Cargar número de documento
        txtNumDoc.setText(cliente.getNoCliente());
        txtNumDoc.setDisable(true); // No permitir editar el número de documento

        // Detectar y establecer tipo de documento
        String tipoDoc = cliente.getTipoDocumento();

        System.out.println("Tipo de Documento: " + tipoDoc); // Debugging line
        cbxTipDoc.setValue(tipoDoc);

        txtRazSocial.setText(cliente.getNombre());
        // Cargar dirección y contacto
        txtDirec.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");
        if (txtTelefono != null) {
            txtTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");
        }
        if (txtEmail != null) {
            txtEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
        }

        // Cargar estado activo
        if (chkActivo != null) {
            chkActivo.setSelected("S".equals(cliente.getActivo()));
        }

        // Cargar nacionalidad
        if ("S".equals(cliente.getExtranjero())) {
            rbnExtranjero.setSelected(true);
        } else {
            rbnNacional.setSelected(true);
        }

        // Cargar tipo persona
        if ("J".equals(cliente.getTipoPersona())) {
            rbnJuridico.setSelected(true);
        } else {
            rbnNatural.setSelected(true);
        }

        // Cargar ubigeo
        cargarUbigeoCliente(cliente);
    }


    /**
     * Carga el ubigeo del cliente
     */
    private void cargarUbigeoCliente(Cliente cliente) {
        if (cliente.getCodiDepa() == null) return;

        // Buscar y seleccionar departamento
        Platform.runLater(() -> {
            // Seleccionar departamento
            for (Arccdp dep : cbxDepartamento.getItems()) {
                if (dep.getCodDepa().equals(cliente.getCodiDepa())) {
                    cbxDepartamento.setValue(dep);

                    // Cargar provincias
                    ArccprDao provDao = new ArccprDao();
                    List<Arccpr> provincias = provDao.listaProvincias(NO_CIA, cliente.getCodiDepa());
                    if (provincias != null) {
                        cbxProvincia.getItems().setAll(provincias);

                        // Configurar converter
                        cbxProvincia.setConverter(new StringConverter<>() {
                            @Override
                            public String toString(Arccpr item) {
                                return item == null ? "" : item.getDescProv();
                            }
                            @Override
                            public Arccpr fromString(String string) {
                                return null;
                            }
                        });

                        // Seleccionar provincia
                        for (Arccpr prov : cbxProvincia.getItems()) {
                            if (prov.getCodiProv().equals(cliente.getCodiProv())) {
                                cbxProvincia.setValue(prov);

                                // Cargar distritos
                                ArccdiDao distDao = new ArccdiDao();
                                List<Arccdi> distritos = distDao.listaDistrito(NO_CIA, cliente.getCodiDepa(), cliente.getCodiProv());
                                if (distritos != null) {
                                    cbxDistrito.getItems().setAll(distritos);

                                    // Configurar converter
                                    cbxDistrito.setConverter(new StringConverter<>() {
                                        @Override
                                        public String toString(Arccdi item) {
                                            return item == null ? "" : item.getDescDist();
                                        }
                                        @Override
                                        public Arccdi fromString(String string) {
                                            return null;
                                        }
                                    });

                                    // Seleccionar distrito
                                    for (Arccdi dist : cbxDistrito.getItems()) {
                                        if (dist.getCodiDist().equals(cliente.getCodiDist())) {
                                            cbxDistrito.setValue(dist);
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        });
    }

    /**
     * Establece el modo de edición
     */
    public void setModoEdicion(boolean edicion) {
        this.modoEdicion = edicion;
        if (txtNumDoc != null) {
            txtNumDoc.setDisable(edicion);
        }
        if (btnRegistrar != null) {
            btnRegistrar.setText(edicion ? "Actualizar" : "Registrar");
        }
    }
}