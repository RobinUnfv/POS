package com.robin.pos.controller;

import com.robin.pos.dao.*;
import com.robin.pos.model.Arccdi;
import com.robin.pos.model.Arccdp;
import com.robin.pos.model.Arccpr;
import com.robin.pos.model.EntidadTributaria;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador mejorado para el formulario de Cliente
 * Incluye validación en tiempo real, atajos de teclado y mejor UX
 *
 * @author Robin POS
 * @version 2.0
 */
public class ClienteController implements Initializable {

    // === COMPONENTES FXML ===
    @FXML private VBox vbxPrincipal;
    @FXML private VBox vbxCuerpo;
    @FXML private HBox hbxPie;

    // Secciones del formulario
    @FXML private GridPane gpDocumento;
    @FXML private GridPane gpDos;
    @FXML private VBox gpTres;      // Cambiado a VBox para el nuevo diseño
    @FXML private VBox gpCuatro;    // Cambiado a VBox para el nuevo diseño
    @FXML private VBox gpCinco;
    @FXML private VBox gpSeis;

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
    @FXML private TextField txtApePat;
    @FXML private TextField txtApeMat;
    @FXML private TextField txtPriNom;
    @FXML private TextField txtSegNom;
    @FXML private TextField txtRazSocial;
    @FXML private TextField txtDirec;

    // RadioButton
    @FXML private RadioButton rbnJuridico;
    @FXML private RadioButton rbnNatural;
    @FXML private RadioButton rbnNacional;
    @FXML private RadioButton rbnExtranjero;

    // Botones
    @FXML private Button btnRegistrar;
    @FXML private Button btnSalir;

    // === GRUPOS DE TOGGLE ===
    private ToggleGroup tipoPersonaGroup;
    private ToggleGroup nacionalidadGroup;

    // === CONSTANTES ===
    private static final String NO_CIA = "01";
    private static final String ESTILO_ERROR = "validation-error";
    private static final String ESTILO_EXITO = "validation-success";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTipoDocumento();
        configurarRadioButtons();
        configurarListeners();
        configurarAtajosTeclado();
        cargarDepartamentos(NO_CIA);

        // Listener para cambio de tipo de documento
        cbxTipDoc.valueProperty().addListener((obs, oldVal, newVal) -> {
            txtNumDoc.setText("");
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
        rbnExtranjero.setUserData("E");
        rbnJuridico.setUserData("J");
        rbnNatural.setUserData("P");

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
        configurarMayusculas(txtApePat);
        configurarMayusculas(txtApeMat);
        configurarMayusculas(txtPriNom);
        configurarMayusculas(txtSegNom);
        configurarMayusculas(txtRazSocial);
        configurarMayusculas(txtDirec);
    }

    /**
     * Configura un campo para convertir texto a mayúsculas
     */
    private void configurarMayusculas(TextField campo) {
        campo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(newVal.toUpperCase())) {
                campo.setText(newVal.toUpperCase());
            }
        });
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
        lblNacionalidad.setVisible(isDNI || isCE);
        lblNacionalidad.setManaged(isDNI || isCE);

        // Buscar el contenedor padre de los radio buttons de nacionalidad
        if (lblNacionalidad.getParent() != null && lblNacionalidad.getParent() instanceof VBox) {
            VBox contenedorNacionalidad = (VBox) lblNacionalidad.getParent();
            contenedorNacionalidad.setVisible(isDNI || isCE);
            contenedorNacionalidad.setManaged(isDNI || isCE);
        }

        // Datos personales (DNI/CE) vs Razón Social (RUC)
        gpTres.setVisible(!isRUC);
        gpTres.setManaged(!isRUC);
        gpCuatro.setVisible(isRUC);
        gpCuatro.setManaged(isRUC);

        // Caso especial para CE
        if (isCE) {
            gpTres.setVisible(true);
            gpTres.setManaged(true);
            gpCuatro.setVisible(false);
            gpCuatro.setManaged(false);
        }

        // Actualizar etiquetas y selecciones
        if (isRUC) {
            lblTipNum.setText("Número de RUC");
            rbnJuridico.setSelected(true);
            rbnNacional.setSelected(true);
            if (lblSubtitulo != null) {
                lblSubtitulo.setText("Complete los datos de la empresa");
            }
        } else if (isDNI) {
            lblTipNum.setText("Número de DNI");
            rbnNatural.setSelected(true);
            rbnNacional.setSelected(true);
            if (lblSubtitulo != null) {
                lblSubtitulo.setText("Complete los datos personales del cliente");
            }
        } else if (isCE) {
            lblTipNum.setText("Número de CE");
            rbnNatural.setSelected(true);
            rbnExtranjero.setSelected(true);
            if (lblSubtitulo != null) {
                lblSubtitulo.setText("Complete los datos del cliente extranjero");
            }
        }
    }

    // === ACCIONES DE FORMULARIO ===

    @FXML
    void cerrarModal(ActionEvent event) {
        btnSalir.getScene().getWindow().hide();
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

        // Confirmar registro
        if (Mensaje.confirmacion(null, "Confirmar Registro",
                "¿Está seguro de registrar este cliente?").get() == ButtonType.OK) {

            EntidadTributaria entidad = obtenerDatosCliente();
            int registro = ArccmcDao.registrar(entidad);

            if (registro > 0) {
                registro = ArcctdaDao.registrar(entidad);
                Mensaje.alerta(null, "Registro Exitoso",
                        "El cliente se registró correctamente.");
                btnSalir.getScene().getWindow().hide();
            } else {
                Mensaje.error(null, "Error de Registro",
                        "No se pudo registrar el cliente. Intente nuevamente.");
            }
        }
    }

    /**
     * Valida el formulario según el tipo de documento seleccionado
     */
    private boolean validarFormularioSegunTipo(String tipoDoc) {
        switch (tipoDoc) {
            case "RUC":
                return validarCamposRuc();
            case "DNI":
                return validarCamposDni();
            case "CE":
                return validarCamposCE();
            default:
                return false;
        }
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

        if (txtApePat.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtApePat, "Debe ingresar el apellido paterno.");
            return false;
        }

        if (txtPriNom.getText().trim().isEmpty()) {
            mostrarErrorCampo(txtPriNom, "Debe ingresar el primer nombre.");
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

    /**
     * Obtiene los datos del cliente del formulario
     */
    private EntidadTributaria obtenerDatosCliente() {
        EntidadTributaria entidad = new EntidadTributaria();
        String tipoDoc = cbxTipDoc.getValue();

        entidad.setNumeroDocumento(txtNumDoc.getText().trim());

        switch (tipoDoc) {
            case "RUC":
                entidad.setTipoDocumento("6");
                entidad.setNombre(txtRazSocial.getText().trim());
                break;
            case "DNI":
                entidad.setTipoDocumento("1");
                entidad.setNombre(construirNombreCompleto());
                break;
            case "CE":
                entidad.setTipoDocumento("7");
                entidad.setNombre(construirNombreCompleto());
                break;
        }

        entidad.setDireccion(txtDirec.getText().trim());
        entidad.setUbigeo(construirUbigeo());

        return entidad;
    }

    private String construirNombreCompleto() {
        StringBuilder nombre = new StringBuilder();
        nombre.append(txtApePat.getText().trim());

        String apeMat = txtApeMat.getText().trim();
        if (!apeMat.isEmpty()) {
            nombre.append(" ").append(apeMat);
        }

        nombre.append(" ").append(txtPriNom.getText().trim());

        String segNom = txtSegNom.getText().trim();
        if (!segNom.isEmpty()) {
            nombre.append(" ").append(segNom);
        }

        return nombre.toString().trim();
    }

    private String construirUbigeo() {
        Arccdp dep = cbxDepartamento.getValue();
        Arccpr prov = cbxProvincia.getValue();
        Arccdi dist = cbxDistrito.getValue();

        if (dep != null && prov != null && dist != null) {
            return dep.getCodDepa() + prov.getCodiProv() + dist.getCodiDist();
        }
        return "";
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
        List<Arccpr> lista = dao.listaProvincias(NO_CIA, departamento.getCodDepa()); // listarProvincias(NO_CIA, departamento.getCodDepa());

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

        if (!cbxProvincia.getItems().isEmpty()) {
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

        if (!cbxDistrito.getItems().isEmpty()) {
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

    /**
     * Limpia todos los campos del formulario
     */
    public void limpiarFormulario() {
        txtNumDoc.clear();
        txtRazSocial.clear();
        txtApePat.clear();
        txtApeMat.clear();
        txtPriNom.clear();
        txtSegNom.clear();
        txtDirec.clear();

        cbxDepartamento.getSelectionModel().clearSelection();
        cbxProvincia.getItems().clear();
        cbxDistrito.getItems().clear();

        cbxTipDoc.setValue("RUC");
        Metodos.configuracionNumeroDocumento(txtNumDoc, "RUC");

        // Limpiar estilos de validación
        limpiarEstilosValidacion(txtNumDoc);
        limpiarEstilosValidacion(txtRazSocial);
        limpiarEstilosValidacion(txtApePat);
        limpiarEstilosValidacion(txtApeMat);
        limpiarEstilosValidacion(txtPriNom);
        limpiarEstilosValidacion(txtSegNom);
        limpiarEstilosValidacion(txtDirec);

        Platform.runLater(() -> txtNumDoc.requestFocus());
    }

    /**
     * Establece el modo de edición con datos existentes
     */
    public void setClienteParaEditar(EntidadTributaria cliente) {
        if (cliente == null) return;

        if (lblTitulo != null) {
            lblTitulo.setText("Editar Cliente");
        }
        if (lblSubtitulo != null) {
            lblSubtitulo.setText("Modifique los datos del cliente");
        }
        if (btnRegistrar != null) {
            btnRegistrar.setText("Actualizar");
        }

        // Cargar datos del cliente en el formulario
        txtNumDoc.setText(cliente.getNumeroDocumento());
        txtDirec.setText(cliente.getDireccion());

        // Detectar tipo de documento y cargar campos correspondientes
        String tipoDoc = cliente.getTipoDocumento();
        switch (tipoDoc) {
            case "6" -> {
                cbxTipDoc.setValue("RUC");
                txtRazSocial.setText(cliente.getNombre());
            }
            case "1" -> {
                cbxTipDoc.setValue("DNI");
                // Aquí se deberían parsear los nombres si están disponibles
            }
            case "7" -> {
                cbxTipDoc.setValue("CE");
                // Aquí se deberían parsear los nombres si están disponibles
            }
        }
    }
}