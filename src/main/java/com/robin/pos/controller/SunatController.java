package com.robin.pos.controller;

import com.robin.pos.dao.ArccmcDao;
import com.robin.pos.dao.ArcctdaDao;
import com.robin.pos.model.EntidadTributaria;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import com.robin.pos.util.ProgressDialog;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

public class SunatController implements Initializable {

    @FXML
    private ComboBox<String> cbxTipoDocumento;
    @FXML
    private TextField txtNumeroDocumento;
    @FXML
    private TextField txtNumeroRUC;
    @FXML
    private TextField txtEstado;
    @FXML
    private TextField txtCondicion;
    @FXML
    private TextField txtRazonSocial;
    @FXML
    private TextField txtDireccion;
    @FXML
    private TextField txtDepartamento;
    @FXML
    private TextField txtProvincia;
    @FXML
    private TextField txtDistrito;
    @FXML
    private TextField txtUbigeo;
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnRegistrar;
    @FXML
    private Button btnSalir;

    private EntidadTributaria entidadTributaria;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial
        this.txtNumeroDocumento.requestFocus();
        this.cbxTipoDocumento.getItems().addAll("RUC", "DNI");
        this.cbxTipoDocumento.setValue("RUC");
        configuracionNumeroDocumento(this.txtNumeroDocumento,"RUC");

    }

    void configuracionNumeroDocumento(TextField textField, String tipoDocumento) {
        UnaryOperator<TextFormatter.Change> filter;
        if (tipoDocumento == "RUC") {
             filter = change -> {
                String newText = change.getControlNewText();

                // Permite solo dígitos y máximo 11 caracteres
                if (newText.matches("\\d{0,11}")) {
//                    txtNumeroDocumento.setStyle("-fx-border-color: green; -fx-border-width: 1px; -fx-background-radius: 5; -fx-border-radius: 5;");
                    return change;
                }

                return null; // Rechaza el cambio si no cumple
            };
        } else {
            filter = change -> {
                String newText = change.getControlNewText();

                // Permite solo dígitos y máximo 8 caracteres
                if (newText.matches("\\d{0,8}")) {
                    return change;
                }
//                txtNumeroDocumento.setStyle("-fx-border-color: red; -fx-border-width: 1px;");
                return null;
            };
        }
        textField.setTextFormatter(new TextFormatter<>(filter));
    }

    @FXML
    void salir(ActionEvent event) {
        // logica para cerrar el modal
          btnSalir.getScene().getWindow().hide();
    }

    @FXML
    void buscar(ActionEvent event) {
       String numeroDocumento = this.txtNumeroDocumento.getText();
       String tipoDocumento = this.cbxTipoDocumento.getValue();

       if (numeroDocumento != null && !numeroDocumento.trim().isEmpty()) {
            if (tipoDocumento == "RUC") {
                if (numeroDocumento.trim().length() == 11) {
                    // Lógica para buscar RUC
                    consultarNumeroDocumento();
                } else {
                    // Mostrar mensaje de error: RUC debe tener 11 dígitos
                    Mensaje.alerta(null,"RUC", "RUC debe tener 11 dígitos");
                }
            } else if (tipoDocumento == "DNI") {
                if (numeroDocumento.trim().length() == 8) {
                    // Lógica para buscar DNI
                    consultarNumeroDocumento();
                } else {
                    // Mostrar mensaje de error: DNI debe tener 8 dígitos
                    Mensaje.alerta(null,"DNI", "DNI debe tener 8 dígitos");
                }
            }
       }
    }

    @FXML
    private void buscarDocumento(KeyEvent evt) {
        if (evt.getCode() == KeyCode.ENTER) {
            buscar(new ActionEvent());
        }
    }

    @FXML
    void cambiarTipoDocumento(ActionEvent event) {
      String tipoDocumento = this.cbxTipoDocumento.getValue();
      this.txtNumeroDocumento.setText(null);
      configuracionNumeroDocumento(this.txtNumeroDocumento, tipoDocumento);
      this.txtNumeroDocumento.requestFocus();
    }

    private void consultarNumeroDocumento() {
        String numeroDocumento = this.txtNumeroDocumento.getText().trim();
        String tipoDocumento = this.cbxTipoDocumento.getValue();
        String links = "https://api.apis.net.pe/v1/"+tipoDocumento.toLowerCase()+"?numero="+numeroDocumento;
        // Mostrar progreso
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Consultando "+tipoDocumento);
        progressDialog.setMessage("Por favor, espere mientras se consulta el "+tipoDocumento+"...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        Task<EntidadTributaria> task = new Task<EntidadTributaria>() {

            @Override
            protected EntidadTributaria call() throws Exception {
                EntidadTributaria entidadTributaria = null;
                try {
                    URL url = new URL(links);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                    if (conn.getResponseCode() != 200) {
                          Mensaje.error(null,"Error", "N° " + numeroDocumento + " de " + tipoDocumento + " inválido.");
                    } else {
                        InputStreamReader in = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader br = new BufferedReader(in);
                        entidadTributaria = Metodos.convertirJson(br.readLine());
                        br.close();
                        in.close();
                        conn.disconnect();
                    }
                }catch (Exception e) {
                    Mensaje.alerta("","","Error en la consulta: " + e.getMessage());
                    throw e;
                }
                return entidadTributaria;
            }
        };

        // Manejar cuando la tarea termina exitosamente
        task.setOnSucceeded(event -> {
            progressDialog.close();
            entidadTributaria = task.getValue();
            if (entidadTributaria != null) {
//                 System.out.println(entidadTributaria.toString());
                 this.txtNumeroRUC.setText(entidadTributaria.getNumeroDocumento());
                 this.txtEstado.setText(entidadTributaria.getEstado());
                 this.txtCondicion.setText(entidadTributaria.getCondicion());
                 this.txtRazonSocial.setText(entidadTributaria.getNombre());
                 this.txtDireccion.setText(entidadTributaria.getDireccion());
                 this.txtDepartamento.setText(entidadTributaria.getDepartamento());
                 this.txtProvincia.setText(entidadTributaria.getProvincia());
                 this.txtDistrito.setText(entidadTributaria.getDistrito());
                 this.txtUbigeo.setText(entidadTributaria.getUbigeo());
            }
        });

        // Manejar cuando falla la tarea
        task.setOnFailed(event -> {
            progressDialog.close();
            Mensaje.error(null, "Error", "Ocurrió un error al consultar el documento." );
        });

        new Thread(task).start();

    }

    @FXML
    void registrarEntidad(ActionEvent event) {
        if (Mensaje.confirmacion(null,"Confirmar","¿Está seguro de registrar?").get() != ButtonType.CANCEL) {
            ArccmcDao.registrar(entidadTributaria);
            ArcctdaDao.registrar(entidadTributaria);
        }
    }

}
