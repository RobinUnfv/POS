package com.robin.pos.controller;

import com.robin.pos.dao.ArfaccDao;
import com.robin.pos.dao.ArfadocDao;
import com.robin.pos.model.Arfacc;
import com.robin.pos.model.Arfadoc;
import com.robin.pos.util.Mensaje;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Logger;


public class FormSerieDocumentoController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(FormSerieDocumentoController.class.getName());

    // ==================== CAMPOS FXML ====================

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;

    @FXML private TextField txtCentro;
    @FXML private ComboBox<Arfadoc> cbxTipoDoc;
    @FXML private TextField txtSerie;
    @FXML private TextField txtConsDesde;
    @FXML private TextField txtLineas;
    @FXML private ComboBox<String> cbxControlAuto;
    @FXML private ComboBox<String> cbxActivo;
    @FXML private TextField txtNoCaba;

    // ==================== VARIABLES DE INSTANCIA ====================

    private String noCia;
    private Arfacc serieEditar;
    private boolean modoEdicion = false;

    private final ArfaccDao arfaccDao = new ArfaccDao();

    private Runnable onGuardado;
    private Runnable onCancelado;

    private static final String NO_CIA = "01";

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
        //cbxTipoDoc.getItems().addAll("F", "B", "N", "NC", "ND");
        cargarTipoDocumento();

        // Control automático
        cbxControlAuto.getItems().addAll("S", "N");
        cbxControlAuto.setValue("S");

        // Estado activo
        cbxActivo.getItems().addAll("S", "N");
        cbxActivo.setValue("S");
    }

    private void cargarTipoDocumento() {
        ArfadocDao arfadocDao = new ArfadocDao();
        //List<Arfadoc> arfadocs = arfadocDao.listarDocumentos(NO_CIA);
        cbxTipoDoc.getItems().setAll(arfadocDao.listarDocumentos(NO_CIA));

        cbxTipoDoc.setConverter(new StringConverter<Arfadoc>() {
            @Override
            public String toString(Arfadoc arfadoc) {
                return arfadoc != null ? arfadoc.getDescripcion() : "";
            }

            @Override
            public Arfadoc fromString(String s) {
                return cbxTipoDoc.getItems().stream()
                        .filter(d -> d.getDescripcion().equals(s))
                        .findFirst().orElse(null);
            }
        });

        cbxTipoDoc.setCellFactory(lv -> new ListCell<Arfadoc>() {
            @Override
            protected void updateItem(Arfadoc item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDescripcion());
            }
        });
    }

    /**
     * Configura validaciones de campos
     */
    private void configurarValidaciones() {
        // Solo números en campos numéricos
        txtConsDesde.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtConsDesde.setText(oldVal);
            }
        });

        txtLineas.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtLineas.setText(oldVal);
            }
        });

        // Limitar longitud de campos
        txtCentro.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                txtCentro.setText(oldVal);
            }
        });

        txtSerie.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                txtSerie.setText(oldVal);
            }
        });

        txtNoCaba.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                txtNoCaba.setText(oldVal);
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
     * Carga los datos de una serie para editar
     */
    public void cargarDatos(Arfacc serie) {
        this.serieEditar = serie;
        this.modoEdicion = true;

        // Cambiar título
        lblTitulo.setText("Editar Serie de Documento");
        lblSubtitulo.setText("Modifique los datos de la serie");

        // Cargar datos en los campos
        txtCentro.setText(serie.getCentro());
        txtCentro.setEditable(false); // No se puede editar en modo edición
        txtCentro.setStyle("-fx-background-color: #f1f5f9;");
        ArfadocDao arfadocDao = new ArfadocDao();
        cbxTipoDoc.setValue( arfadocDao.buscarPorCodigo(noCia, serie.getTipoDoc() ) );
        cbxTipoDoc.setDisable(true); // No se puede editar en modo edición

        txtSerie.setText(serie.getSerie());
        txtSerie.setEditable(false); // No se puede editar en modo edición
        txtSerie.setStyle("-fx-background-color: #f1f5f9;");

        txtConsDesde.setText(String.valueOf(serie.getConsDesde()));
        txtLineas.setText(String.valueOf(serie.getLineas()));
        cbxControlAuto.setValue(serie.getIndControlAuto());
        cbxActivo.setValue(serie.getActivo());
        txtNoCaba.setText(serie.getNoCaba());
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida que los campos obligatorios estén llenos
     */
    private boolean validarCampos() {
        // Centro
        if (txtCentro.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe ingresar el centro.");
            txtCentro.requestFocus();
            return false;
        }

        // Tipo documento
        if (cbxTipoDoc.getValue() == null) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe seleccionar el tipo de documento.");
            cbxTipoDoc.requestFocus();
            return false;
        }

        // Serie
        if (txtSerie.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe ingresar la serie.");
            txtSerie.requestFocus();
            return false;
        }

        // Consecutivo desde
        if (txtConsDesde.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe ingresar el consecutivo desde.");
            txtConsDesde.requestFocus();
            return false;
        }

        try {
            int consDesde = Integer.parseInt(txtConsDesde.getText().trim());
            if (consDesde < 0) {
                Mensaje.alerta(null, "Valor Inválido",
                        "El consecutivo desde debe ser mayor o igual a 0.");
                txtConsDesde.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Mensaje.alerta(null, "Valor Inválido",
                    "El consecutivo desde debe ser un número válido.");
            txtConsDesde.requestFocus();
            return false;
        }

        // Líneas
        if (txtLineas.getText().trim().isEmpty()) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe ingresar el número de líneas.");
            txtLineas.requestFocus();
            return false;
        }

        try {
            int lineas = Integer.parseInt(txtLineas.getText().trim());
            if (lineas <= 0) {
                Mensaje.alerta(null, "Valor Inválido",
                        "El número de líneas debe ser mayor a 0.");
                txtLineas.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            Mensaje.alerta(null, "Valor Inválido",
                    "El número de líneas debe ser un número válido.");
            txtLineas.requestFocus();
            return false;
        }

        // Control automático
        if (cbxControlAuto.getValue() == null) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe seleccionar si tiene control automático.");
            cbxControlAuto.requestFocus();
            return false;
        }

        // Estado
        if (cbxActivo.getValue() == null) {
            Mensaje.alerta(null, "Campo Obligatorio",
                    "Debe seleccionar el estado.");
            cbxActivo.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Valida que no exista una serie duplicada
     */
    private boolean validarDuplicado() {
        if (!modoEdicion) {
            String centro = txtCentro.getText().trim();
            String tipoDoc = cbxTipoDoc.getValue().getCodDoc();
            String serie = txtSerie.getText().trim();

            boolean existe = arfaccDao.existe(noCia, centro, tipoDoc, serie);

            if (existe) {
                Mensaje.alerta(null, "Serie Duplicada",
                        "Ya existe una serie con estos datos:\n" +
                                "Centro: " + centro + "\n" +
                                "Tipo Doc: " + tipoDoc + "\n" +
                                "Serie: " + serie + "\n\n" +
                                "Por favor, ingrese datos diferentes.");
                return false;
            }
        }
        return true;
    }

    // ==================== ACCIONES ====================

    /**
     * Guarda la serie (nueva o editada)
     */
    @FXML
    private void guardar(ActionEvent event) {
        LOGGER.info("Guardando serie");

        // Validar campos
        if (!validarCampos()) {
            return;
        }

        // Validar duplicado (solo en modo nuevo)
        if (!validarDuplicado()) {
            return;
        }

        // Crear o actualizar serie
        Arfacc serie = new Arfacc();
        serie.setNoCia(noCia);
        serie.setCentro(txtCentro.getText().trim().toUpperCase());
        serie.setTipoDoc(cbxTipoDoc.getValue().getCodDoc());
        serie.setSerie(txtSerie.getText().trim().toUpperCase());
        serie.setConsDesde(Integer.parseInt(txtConsDesde.getText().trim()));
        serie.setLineas(Integer.parseInt(txtLineas.getText().trim()));
        serie.setIndControlAuto(cbxControlAuto.getValue());
        serie.setActivo(cbxActivo.getValue());
        serie.setNoCaba(txtNoCaba.getText().trim());

        boolean resultado;

        if (modoEdicion) {
            // Actualizar
            LOGGER.info("Actualizando serie: " + serie.getSerie());
            resultado = arfaccDao.actualizar(serie);
        } else {
            // Insertar nuevo
            LOGGER.info("Insertando nueva serie: " + serie.getSerie());
            resultado = arfaccDao.insertar(serie);
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
