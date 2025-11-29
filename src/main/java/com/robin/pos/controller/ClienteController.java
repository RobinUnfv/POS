package com.robin.pos.controller;

import com.robin.pos.dao.ArccdpDao;
import com.robin.pos.dao.ArccprDao;
import com.robin.pos.model.Arccdp;
import com.robin.pos.model.Arccpr;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ClienteController implements Initializable {

    @FXML
    private Button btnRegistrar;

    @FXML
    private Button btnSalir;

    @FXML
    private ComboBox<Arccdp> cbxDepartamento;

    @FXML
    private ComboBox<?> cbxDistrito;

    @FXML
    private ComboBox<Arccpr> cbxProvincia;

    @FXML
    private ComboBox<String> cbxTipDoc;

    @FXML
    private GridPane gpCinco;

    @FXML
    private GridPane gpCuatro;

    @FXML
    private GridPane gpDocumento;

    @FXML
    private GridPane gpDos;

    @FXML
    private GridPane gpSeis;

    @FXML
    private GridPane gpTres;

    @FXML
    private HBox hbxPie;

    @FXML
    private Label lblNacionalidad;

    @FXML
    private Text lblTipDoc;

    @FXML
    private Text lblTipNum;

    @FXML
    private Label lblTipPersona;

    @FXML
    private RadioButton rbnExtranjero;

    @FXML
    private RadioButton rbnJuridico;

    @FXML
    private RadioButton rbnNacional;

    @FXML
    private RadioButton rbnNatural;

    @FXML
    private TextField txtApeMat;

    @FXML
    private TextField txtApePat;

    @FXML
    private TextField txtDirec;

    @FXML
    private TextField txtNumDoc;

    @FXML
    private TextField txtPriNom;

    @FXML
    private TextField txtRazSocial;

    @FXML
    private TextField txtSegNom;

    @FXML
    private VBox vbxCuerpo;

    @FXML
    private VBox vbxPrincipal;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configurarTipoDocumento();
        configurarRadioButon();

        this.cbxTipDoc.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibilityByTipoDoc(newVal);
            // si además necesita reconfigurar el formato del número:
            Metodos.configuracionNumeroDocumento(this.txtNumDoc, newVal != null ? newVal : "RUC");
        });

        updateVisibilityByTipoDoc("RUC");
        cargarDepartamentos("01");
    }

    private void configurarTipoDocumento() {
        this.cbxTipDoc.getItems().addAll("RUC", "DNI","CE");
        this.cbxTipDoc.setValue("RUC");
        Metodos.configuracionNumeroDocumento(this.txtNumDoc ,"RUC");
        this.txtNumDoc.requestFocus();
    }

    private void configurarRadioButon() {
        ToggleGroup tipoDocGroup = new ToggleGroup();
        ToggleGroup tipoPersonaGroup = new ToggleGroup();

        rbnJuridico.setToggleGroup(tipoPersonaGroup);
        rbnNatural.setToggleGroup(tipoPersonaGroup);
        rbnNacional.setToggleGroup(tipoDocGroup);
        rbnExtranjero.setToggleGroup(tipoDocGroup);

        rbnNacional.setUserData("N");
        rbnExtranjero.setUserData("E");
        rbnJuridico.setUserData("J");
        rbnNatural.setUserData("P");

        // Seleccionar uno por defecto
        rbnJuridico.setSelected(true);
        rbnNacional.setSelected(true);
    }

    private void updateVisibilityByTipoDoc(String tipoDoc) {
        boolean isRUC = "RUC".equals(tipoDoc);
        boolean isDNI = "DNI".equals(tipoDoc);
        boolean isCE = "CE".equals(tipoDoc);

        // Mostrar/ocultar campos según el tipo de documento
        lblNacionalidad.setVisible(isDNI || isCE);
        /*
        rbnNacional.setVisible(isDNI || isCE);
        rbnExtranjero.setVisible(isDNI || isCE);
        */
        // Si es RUC ocultar campos de persona natural (gpTres) y mostrar Razon Social (gpCuatro)
        gpTres.setVisible(!isRUC);
        gpTres.setManaged(!isRUC);

        gpCuatro.setVisible(isRUC);
        gpCuatro.setManaged(isRUC);

        // Si se quiere un comportamiento especial para CE u otros, se puede añadir aquí.
        // Por ejemplo, si CE debe comportarse como DNI:
        if (tipoDoc.equalsIgnoreCase("CE")) {
            gpTres.setVisible(true);
            gpTres.setManaged(true);
            gpCuatro.setVisible(false);
            gpCuatro.setManaged(false);
        }

        // Ajustar etiquetas
        if (isRUC) {
            lblTipNum.setText("Número RUC:");
            rbnJuridico.setSelected(true);
            rbnNacional.setSelected(true);
        } else if (isDNI) {
            lblTipNum.setText("Número DNI:");
            rbnNatural.setSelected(true);
            rbnNacional.setSelected(true);
        } else if (isCE) {
            lblTipNum.setText("Número CE:");
            rbnNatural.setSelected(true);
            rbnExtranjero.setSelected(true);
        }
    }

    @FXML
    void cerrarModal(ActionEvent event) {
       this.btnSalir.getScene().getWindow().hide();
    }

    private void cargarDepartamentos(String noCia) {
        ArccdpDao dao = new ArccdpDao();
        List<Arccdp> lstDepartamentos = dao.listarDepartamentos(noCia);
        if (lstDepartamentos != null ) {
            this.cbxDepartamento.getItems().setAll(lstDepartamentos);
        }

        this.cbxDepartamento.setConverter( new StringConverter<Arccdp>() {

            @Override
            public String toString(Arccdp arccdp) {
                return arccdp == null ? "" : arccdp.getDesDepa();
            }

            @Override
            public Arccdp fromString(String string) {
                return cbxDepartamento.getItems().stream()
                        .filter(d -> d.getDesDepa().equals(string))
                        .findFirst().orElse(null);
            }

        } );

        this.cbxDepartamento.setCellFactory( lst -> new ListCell<Arccdp>() {
            @Override
            protected void updateItem(Arccdp item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDesDepa());
            }
        });

    }

    @FXML
    void buscarProvincias(ActionEvent event) {
        Arccdp departamento = this.cbxDepartamento.getValue();
//        System.out.println("DEPARTAMENTO SELECCIONADO: " + departamento.getDesDepa());
        if (departamento == null) {
            Mensaje.error(null, "Error Departamento","Debe seleccionar un departamento.");
            return;
        }
        String codDepa = departamento.getCodDepa();
        ArccprDao arccprDao = new ArccprDao();
        List<Arccpr> lstProvincias = arccprDao.listaProvincias("01", codDepa);

        if (lstProvincias != null) {
            this.cbxProvincia.getItems().setAll(lstProvincias);
        } else {
            this.cbxProvincia.getItems().clear();
        }

        this.cbxProvincia.setConverter(new StringConverter<Arccpr>() {
            @Override
            public String toString(Arccpr p) {
                return p == null ? "" : p.getDescProv();
            }

            @Override
            public Arccpr fromString(String string) {
                return cbxProvincia.getItems().stream()
                        .filter(p -> p.getDescProv().equals(string))
                        .findFirst().orElse(null);
            }
        });

        this.cbxProvincia.setCellFactory(list -> new ListCell<Arccpr>() {
            @Override
            protected void updateItem(Arccpr item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getDescProv()); // ajustar getter si hace falta
            }
        });

        if (!this.cbxProvincia.getItems().isEmpty()) {
            this.cbxProvincia.getSelectionModel().selectFirst();
        }


    }


}
