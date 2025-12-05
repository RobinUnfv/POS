package com.robin.pos.controller;

import com.robin.pos.dao.ArccmcDao;
import com.robin.pos.dao.ArcctdaDao;
import com.robin.pos.model.EntidadTributaria;
import com.robin.pos.util.Mensaje;
import com.robin.pos.util.Metodos;
import com.robin.pos.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import java.util.function.Consumer;
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

    private Consumer<String> onRegistro;

    public void setOnRegistro(Consumer<String> onRegistro) {
        this.onRegistro = onRegistro;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configuración inicial
        this.cbxTipoDocumento.getItems().addAll("RUC", "DNI");
        this.cbxTipoDocumento.setValue("RUC");
        Metodos.configuracionNumeroDocumento(this.txtNumeroDocumento,"RUC");
        this.txtNumeroDocumento.requestFocus();
        this.btnRegistrar.disableProperty().bind(Bindings.isEmpty(txtNumeroRUC.textProperty()));

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
            if (tipoDocumento.equals("RUC")) {
                if (numeroDocumento.trim().length() == 11) {
                    // Lógica para buscar RUC
                    consultarNumeroDocumento();
                } else {
                    // Mostrar mensaje de error: RUC debe tener 11 dígitos
                    Mensaje.alerta(null,"RUC", "RUC debe tener 11 dígitos");
                }
            } else if (tipoDocumento.equals("DNI")) {
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
      Metodos.configuracionNumeroDocumento(this.txtNumeroDocumento, tipoDocumento);
        Platform.runLater(() -> {
            this.txtNumeroDocumento.requestFocus();
            //this.txtNumDoc.selectAll();
        });

    }

    private void consultarNumeroDocumento() {
        String numeroDocumento = this.txtNumeroDocumento.getText().trim();
        String tipoDocumento = this.cbxTipoDocumento.getValue();
        if (ArccmcDao.contadorRegistros(numeroDocumento) == 0) {
            realizarConsultaSunatDniRuc(tipoDocumento, numeroDocumento);
        } else {
            Mensaje.alerta(null, "Registro existente", "El " + tipoDocumento + " N° " + numeroDocumento + " ya se encuentra registrado.");
        }

    }

    private void realizarConsultaSunatDniRuc(String tipoDocumento, String numeroDocumento) {
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
        String numeroDocumento = this.txtNumeroDocumento.getText().trim();
        String tipoDocumento = this.cbxTipoDocumento.getValue();
        if (ArccmcDao.contadorRegistros(numeroDocumento) == 0) {
            if (Mensaje.confirmacion(null,"Confirmar","¿Está seguro de registrar?").get() != ButtonType.CANCEL) {
                int registro = ArccmcDao.registrar(entidadTributaria);
                if (registro > 0) {
                    registro = ArcctdaDao.registrar(entidadTributaria);
                    if (registro > 0) {
                        Mensaje.alerta (null, "Registro exitoso", "El cliente se registró correctamente.");
                        // Asegurar ejecución en UI thread
                        Platform.runLater(() -> onRegistro.accept(entidadTributaria.getNumeroDocumento()));
                        salir(new ActionEvent());
                    }
                }

                if (registro == 0) {
                    Mensaje.error(null, "Error de registro", "No se pudo registrar la entidad tributaria.");
                }
            }
        } else {
            Mensaje.alerta(null, "Registro existente", "El " + tipoDocumento + " N° " + numeroDocumento + " ya se encuentra registrado.");
        }
    }

}
