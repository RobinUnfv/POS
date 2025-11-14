package com.robin.pos.controller;

import com.robin.pos.util.Metodos;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class ClienteController implements Initializable {

    @FXML
    private Button btnRegistrar;

    @FXML
    private Button btnSalir;

    @FXML
    private ComboBox<?> cbxDepartamento;

    @FXML
    private ComboBox<?> cbxDistrito;

    @FXML
    private ComboBox<?> cbxProvincia;

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
        this.cbxTipDoc.getItems().addAll("RUC", "DNI","CE");
        this.cbxTipDoc.setValue("RUC");
        Metodos.configuracionNumeroDocumento(this.txtNumDoc ,"RUC");
        this.txtNumDoc.requestFocus();

        ToggleGroup tipoDocGroup = new ToggleGroup();
        ToggleGroup tipoPersonaGroup = new ToggleGroup();

        rbnJuridico.setToggleGroup(tipoPersonaGroup);
        rbnNatural.setToggleGroup(tipoPersonaGroup);
        rbnNacional.setToggleGroup(tipoDocGroup);
        rbnExtranjero.setToggleGroup(tipoDocGroup);

        // Seleccionar uno por defecto
        rbnJuridico.setSelected(true);
        rbnNacional.setSelected(true);

    }
}
