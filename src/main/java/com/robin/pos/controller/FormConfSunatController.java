package com.robin.pos.controller;

import com.robin.pos.dao.ConfigSunatDao;
import com.robin.pos.model.ConfigSunat;
import com.robin.pos.util.Mensaje;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controlador para el formulario de configuración SUNAT
 *
 * @author Robin POS
 * @version 1.0
 */
public class FormConfSunatController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FormConfSunatController.class.getName());
    private static final String NO_CIA = "01";

    @FXML private Label lblTitulo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtValor;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cbxGrupo;
    @FXML private ComboBox<String> cbxTipoDato;
    @FXML private CheckBox chkActivo;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private final ConfigSunatDao configSunatDao = new ConfigSunatDao();
    private ConfigSunat configuracionOriginal;
    private boolean modoEdicion = false;
    private Runnable onGuardado;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando FormConfSunatController");

        configurarComboBoxes();
        configurarValidaciones();
        //configurarAtajos();

        LOGGER.info("FormConfSunatController inicializado");
    }

    /**
     * Configura los ComboBoxes con valores predefinidos
     */
    private void configurarComboBoxes() {
        // Grupos
        cbxGrupo.getItems().addAll(
                "RUTAS",
                "CERTIFICADO",
                "SUNAT_AMBIENTE",
                "SUNAT_CREDENCIALES_BETA",
                "SUNAT_CREDENCIALES_PROD",
                "LIMITES",
                "IMPUESTOS"
        );

        // Tipos de dato
        cbxTipoDato.getItems().addAll(
                "STRING",
                "NUMBER",
                "BOOLEAN",
                "PASSWORD"
        );

        // Listener para cambio de tipo dato a PASSWORD
        cbxTipoDato.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("PASSWORD".equals(newVal)) {
                // Convertir a PasswordField (simulado con estilo)
                txtValor.setPromptText("••••••••");
            } else {
                txtValor.setPromptText("Valor del parámetro");
            }
        });
    }

    /**
     * Configura las validaciones de campos
     */
    private void configurarValidaciones() {
        // Límite de caracteres en Código
        txtCodigo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 50) {
                txtCodigo.setText(oldVal);
            }
        });

        // Límite de caracteres en Valor
        txtValor.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 500) {
                txtValor.setText(oldVal);
            }
        });

        // Límite de caracteres en Descripción
        txtDescripcion.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 200) {
                txtDescripcion.setText(oldVal);
            }
        });
    }

    /**
     * Configura atajos de teclado
     */
    /*
    private void configurarAtajos() {
        txtCodigo.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                cerrar(null);
                event.consume();
            } else if (event.isControlDown() && event.getCode() == KeyCode.S) {
                guardar(null);
                event.consume();
            }
        });
    }
    */
    /**
     * Establece la configuración a editar
     */
    public void setConfiguracion(ConfigSunat config) {
        this.configuracionOriginal = config;
        this.modoEdicion = true;

        lblTitulo.setText("Editar Configuración");

        // Cargar datos
        txtCodigo.setText(config.getCodigo());
        txtCodigo.setEditable(false);
        txtCodigo.setStyle("-fx-background-color: #f1f5f9;");

        txtValor.setText(config.getValor());
        txtDescripcion.setText(config.getDescripcion());
        cbxGrupo.setValue(config.getGrupo());
        cbxTipoDato.setValue(config.getTipoDato());
        chkActivo.setSelected("S".equals(config.getActivo()));
    }

    /**
     * Establece el callback al guardar
     */
    public void setOnGuardado(Runnable callback) {
        this.onGuardado = callback;
    }

    /**
     * Valida los datos del formulario
     */
    private boolean validarDatos() {
        // Código obligatorio
        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Validación", "El código es obligatorio.");
            txtCodigo.requestFocus();
            return false;
        }

        // Valor obligatorio
        if (txtValor.getText() == null || txtValor.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Validación", "El valor es obligatorio.");
            txtValor.requestFocus();
            return false;
        }

        // Grupo obligatorio
        if (cbxGrupo.getValue() == null) {
            Mensaje.alerta(null, "Validación", "El grupo es obligatorio.");
            cbxGrupo.requestFocus();
            return false;
        }

        // Tipo Dato obligatorio
        if (cbxTipoDato.getValue() == null) {
            Mensaje.alerta(null, "Validación", "El tipo de dato es obligatorio.");
            cbxTipoDato.requestFocus();
            return false;
        }

        // Verificar duplicados solo en modo nuevo
        if (!modoEdicion) {
            String codigo = txtCodigo.getText().trim().toUpperCase();
            if (configSunatDao.existeCodigo(NO_CIA, codigo)) {
                Mensaje.error(null, "Código Duplicado",
                        "Ya existe una configuración con el código: " + codigo);
                txtCodigo.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Guarda la configuración
     */
    @FXML
    private void guardar(ActionEvent event) {
        if (!validarDatos()) {
            return;
        }

        ConfigSunat config = new ConfigSunat();
        config.setNoCia(NO_CIA);
        config.setCodigo(txtCodigo.getText().trim().toUpperCase());
        config.setValor(txtValor.getText().trim());
        config.setDescripcion(txtDescripcion.getText() != null ? txtDescripcion.getText().trim() : "");
        config.setGrupo(cbxGrupo.getValue());
        config.setTipoDato(cbxTipoDato.getValue());
        config.setActivo(chkActivo.isSelected() ? "S" : "N");

        boolean exito;
        if (modoEdicion) {
            exito = configSunatDao.actualizar(config);
        } else {
            exito = configSunatDao.insertar(config);
        }

        if (exito) {
            if (onGuardado != null) {
                onGuardado.run();
            }
            cerrar(null);
        }
    }

    /**
     * Cierra el formulario
     */
    @FXML
    private void cerrar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
