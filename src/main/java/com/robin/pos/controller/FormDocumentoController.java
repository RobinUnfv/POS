package com.robin.pos.controller;

import com.robin.pos.dao.ArfadocDao;
import com.robin.pos.model.Arfact;
import com.robin.pos.model.Arfadoc;
import com.robin.pos.model.DocumentoPago;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Controlador para el formulario modal de documentos
 * Permite crear y editar documentos
 *
 * @author Robin POS
 * @version 1.0
 */
public class FormDocumentoController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FormDocumentoController.class.getName());

    // ==================== CAMPOS FXML ====================

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;

    @FXML private TextField txtCodDoc;
    @FXML private TextField txtDescripcion;
    //@FXML private ComboBox<String> cbxTipo;
    @FXML private ComboBox<Arfact> cbxTipo;
    @FXML private TextField txtCodSunat;
    @FXML private ComboBox<String> cbxEstado;

    // ==================== VARIABLES DE INSTANCIA ====================

    private String noCia;
    private Arfadoc documentoEditar;
    private boolean modoEdicion = false;

    private final ArfadocDao arfadocDao = new ArfadocDao();

    private Runnable onGuardado;
    private Runnable onCancelado;

    // ==================== INITIALIZE ====================

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarComboBoxes();
        configurarValidaciones();
    }

    /**
     * Configura los ComboBoxes
     */
    private void configurarComboBoxes() {
        // Tipos de documento
        cargarTipoDocumento();

        // Estados
        cbxEstado.getItems().addAll("A", "I");
        cbxEstado.setValue("A");
    }

    private void cargarTipoDocumento() {
        //List<Arfact> tipos = Metodos.getArfacts();
        this.cbxTipo.getItems().setAll(Metodos.getArfacts());

        cbxTipo.setConverter(new StringConverter<Arfact>() {
            @Override
            public String toString(Arfact arfact) {
                return arfact != null ? arfact.getDescripcion() : "";
            }

            @Override
            public Arfact fromString(String s) {
                return cbxTipo.getItems().stream()
                        .filter(d -> d.getDescripcion().equals(s))
                        .findFirst().orElse(null);
            }
        });

        cbxTipo.setCellFactory(lv -> new ListCell<Arfact>() {
            @Override
            protected void updateItem(Arfact item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDescripcion());
            }
        });
    }

    /**
     * Configura validaciones de campos
     */
    private void configurarValidaciones() {
        // Limitar longitud de campos
        txtCodDoc.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                txtCodDoc.setText(oldVal);
            }
        });

        txtCodSunat.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                txtCodSunat.setText(oldVal);
            }
        });
    }

    // ==================== SETTERS PÚBLICOS ====================

    /**
     * Establece el código de compañía
     */
    public void setNoCia(String noCia) {
        this.noCia = noCia;
    }

    /**
     * Establece el callback para cuando se guarde
     */
    public void setOnGuardado(Runnable onGuardado) {
        this.onGuardado = onGuardado;
    }

    /**
     * Establece el callback para cuando se cancele
     */
    public void setOnCancelado(Runnable onCancelado) {
        this.onCancelado = onCancelado;
    }

    /**
     * Carga los datos de un documento para editar
     */
    public void cargarDatos(Arfadoc documento) {
        this.documentoEditar = documento;
        this.modoEdicion = true;

        // Cambiar título
        lblTitulo.setText("Editar Documento");
        lblSubtitulo.setText("Modifique los datos del documento");

        // Cargar datos en los campos
        txtCodDoc.setText(documento.getCodDoc());
        txtCodDoc.setEditable(false); // No se puede editar el código en modo edición
        txtCodDoc.setStyle("-fx-background-color: #f1f5f9;");

        txtDescripcion.setText(documento.getDescripcion());
        cbxTipo.setValue(Metodos.getTipoDocumento(documento.getTipo()));
        txtCodSunat.setText(documento.getCodSunat());
        cbxEstado.setValue(documento.getEstado());
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida que los campos obligatorios estén llenos
     */
    private boolean validarCampos() {
        // Código documento
        if (txtCodDoc.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe ingresar el código del documento.");
            txtCodDoc.requestFocus();
            return false;
        }

        // Descripción
        if (txtDescripcion.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe ingresar la descripción del documento.");
            txtDescripcion.requestFocus();
            return false;
        }

        // Tipo
        if (cbxTipo.getValue() == null) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe seleccionar el tipo de documento.");
            cbxTipo.requestFocus();
            return false;
        }

        // Estado
        if (cbxEstado.getValue() == null) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe seleccionar el estado del documento.");
            cbxEstado.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Valida que no exista un documento con el mismo código
     */
    private boolean validarDuplicado() {
        if (!modoEdicion) {
            String codDoc = txtCodDoc.getText().trim();
            boolean existe = arfadocDao.existe(noCia, codDoc);

            if (existe) {
                Mensaje.alerta(null, "Código Duplicado",
                        "Ya existe un documento con el código '" + codDoc + "'.\n" +
                                "Por favor, ingrese un código diferente.");
                txtCodDoc.requestFocus();
                return false;
            }
        }
        return true;
    }

    // ==================== ACCIONES ====================

    /**
     * Guarda el documento (nuevo o editado)
     */
    @FXML
    private void guardar(ActionEvent event) {
        LOGGER.info("Guardando documento");

        // Validar campos
        if (!validarCampos()) {
            return;
        }

        // Validar duplicado (solo en modo nuevo)
        if (!validarDuplicado()) {
            return;
        }

        // Crear o actualizar documento
        Arfadoc documento = new Arfadoc();
        documento.setNoCia(noCia);
        documento.setCodDoc(txtCodDoc.getText().trim().toUpperCase());
        documento.setDescripcion(txtDescripcion.getText().trim());
        String tipo = (cbxTipo.getValue() != null) ? cbxTipo.getValue().getTipo() : "";
        documento.setTipo(tipo);
        String codSunat = (txtCodSunat.getText() != null) ? txtCodSunat.getText().trim() : "";
        documento.setCodSunat(codSunat);
        documento.setEstado(cbxEstado.getValue());

        boolean resultado;

        if (modoEdicion) {
            // Actualizar
            LOGGER.info("Actualizando documento: " + documento.getCodDoc());
            resultado = arfadocDao.actualizar(documento);
        } else {
            // Insertar nuevo
            LOGGER.info("Insertando nuevo documento: " + documento.getCodDoc());
            resultado = arfadocDao.insertar(documento);
        }

        if (resultado) {
            // Ejecutar callback de guardado
            if (onGuardado != null) {
                onGuardado.run();
            }
        }
    }

    /**
     * Cancela la operación y cierra el formulario
     */
    @FXML
    private void cancelar(ActionEvent event) {
        LOGGER.info("Cancelando operación");

        // Ejecutar callback de cancelado
        if (onCancelado != null) {
            onCancelado.run();
        }
    }
}