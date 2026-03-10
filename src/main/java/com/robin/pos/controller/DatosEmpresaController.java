package com.robin.pos.controller;

import com.robin.pos.dao.*;
import com.robin.pos.model.*;
import com.robin.pos.util.Mensaje;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controlador unificado para datos de empresa y sucursales
 * Gestiona FACTU.ARFAMC y FACTU.SUCURSAL_PTOVTA
 * 
 * @author Robin POS
 * @version 1.0
 */
public class DatosEmpresaController implements Initializable {
    
    private static final Logger LOGGER = Logger.getLogger(DatosEmpresaController.class.getName());
    private static final String NO_CIA = "01"; // Código de compañía
    
    // ==================== CAMPOS FXML - COMPAÑÍA ====================
    
    @FXML private TextField txtRuc;
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtNombre;
    @FXML private TextField txtBanco;
    @FXML private TextField txtCuentaSol;
    @FXML private TextField txtCci;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtIgv;
    @FXML private TextField txtIsc;
    
    // ==================== CAMPOS FXML - SUCURSALES ====================
    
    @FXML private ComboBox<SucursalPtovta> cbxSucursal;
    @FXML private Label lblSucursalInfo;
    
    @FXML private TextField txtCodSucursal;
    @FXML private TextField txtCodPtoVta;
    @FXML private ComboBox<String> cbxEstadoSuc;
    @FXML private TextField txtNombreSucursal;
    @FXML private TextField txtNomComercial;

    @FXML private ComboBox<Arccdp> cbxDepartamento;
    @FXML private ComboBox<Arccpr> cbxProvincia;
    @FXML private ComboBox<Arccdi> cbxDistrito;

    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono1;
    @FXML private TextField txtTelefono2;
    @FXML private TextField txtCorreo;
    
    // ==================== VARIABLES DE INSTANCIA ====================
    
    private final ArfamcDao arfamcDao = new ArfamcDao();
    private final SucursalPtovtaDao sucursalDao = new SucursalPtovtaDao();
    
    private Arfamc datosCompania;
    private ObservableList<SucursalPtovta> listaSucursales = FXCollections.observableArrayList();
    private SucursalPtovta sucursalSeleccionada;
    
    // ==================== INITIALIZE ====================
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("Inicializando DatosEmpresaController");
        
        // Configurar ComboBoxes
        configurarComboBoxes();

        cargarDepartamentos();
        
        // Cargar datos iniciales
        cargarDatosCompania();
        cargarSucursales();
        
        // Configurar atajos de teclado
        configurarAtajos();
        
        LOGGER.info("DatosEmpresaController inicializado correctamente");
    }

    @FXML
    void buscarDepartamento(ActionEvent event) {
        listarProvincia();
    }

    // === CARGA DE UBIGEO ===
    private void cargarDepartamentos() {
        ArccdpDao dao = new ArccdpDao();
        List<Arccdp> lista = dao.listarDepartamentos(NO_CIA);

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
        listarDistrito();
    }

    private void listarProvincia(){
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
        /*
        if (!cbxProvincia.getItems().isEmpty()) {
            cbxProvincia.getSelectionModel().selectFirst();
            buscarDistrito(null);
        }
        */
    }

    @FXML
    void buscarDistrito(ActionEvent event) {
    }

    private void listarDistrito(){
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
    }

    private void cargarValoresDepProDist(String codDepa, String codProvincia, String codDistrito) {
        //DEPARTAMENTO
        ArccdpDao arccdpDao = new ArccdpDao();
        Arccdp departamento = arccdpDao.getDepartamento(NO_CIA, codDepa );
        cbxDepartamento.setValue(departamento);

        //PROVINCIA
        listarProvincia();
        ArccprDao arccprDao = new ArccprDao();
        Arccpr provincia = arccprDao.getProvincia(NO_CIA, codDepa, codProvincia);
        cbxProvincia.setValue(provincia);

        //DISTRITO
        listarDistrito();
        ArccdiDao arccdiDao = new ArccdiDao();
        Arccdi distrito = arccdiDao.getDistrito(NO_CIA, codDepa, codProvincia, codDistrito);
        cbxDistrito.setValue(distrito);

    }
    
    /**
     * Configura los ComboBoxes
     */
    private void configurarComboBoxes() {
        // Estado de sucursal
        cbxEstadoSuc.getItems().addAll("A", "I");
        
        // Converter para el ComboBox de sucursales
        cbxSucursal.setConverter(new StringConverter<SucursalPtovta>() {
            @Override
            public String toString(SucursalPtovta sucursal) {
                if (sucursal == null) return "";
                return sucursal.getCodSucursal() + " - " + 
                       sucursal.getCodPtoVta() + " | " + 
                       sucursal.getNombreSucuPtovta();
            }
            
            @Override
            public SucursalPtovta fromString(String string) {
                return null;
            }
        });
    }
    
    /**
     * Configura los atajos de teclado
     */
    private void configurarAtajos() {
        // Este método se puede expandir según sea necesario
    }
    
    // ==================== CARGAR DATOS ====================
    
    /**
     * Carga los datos de la compañía
     */
    private void cargarDatosCompania() {
        LOGGER.info("Cargando datos de la compañía");
        
        try {
            datosCompania = arfamcDao.obtenerDatosCompania(NO_CIA);
            
            if (datosCompania != null) {
                mostrarDatosCompania();
                LOGGER.info("Datos de compañía cargados exitosamente");
            } else {
                Mensaje.alerta(null, "Sin Datos", 
                    "No se encontraron datos de la compañía.");
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar datos de compañía", e);
            Mensaje.error(null, "Error al Cargar", 
                "No se pudieron cargar los datos de la compañía.\n" + e.getMessage());
        }
    }
    
    /**
     * Muestra los datos de la compañía en los campos
     */
    private void mostrarDatosCompania() {
        if (datosCompania == null) return;
        
        txtRuc.setText(datosCompania.getRuc());
        txtRazonSocial.setText(datosCompania.getRazonSocial());
        txtNombre.setText(datosCompania.getNombre());
        txtBanco.setText(datosCompania.getBanco());
        txtCuentaSol.setText(datosCompania.getCuentaSol());
        txtCci.setText(datosCompania.getCci());
        txtDescripcion.setText(datosCompania.getDescripcion());
        txtIgv.setText(datosCompania.getPorcIgvFormateado());
        txtIsc.setText(datosCompania.getPorcIscFormateado());
    }
    
    /**
     * Carga la lista de sucursales
     */
    private void cargarSucursales() {
        
        try {
            List<SucursalPtovta> sucursales = sucursalDao.listarSucursales(NO_CIA);
            listaSucursales.clear();
            listaSucursales.addAll(sucursales);
            
            cbxSucursal.setItems(listaSucursales);
            
            // Seleccionar primera sucursal si existe
            if (!listaSucursales.isEmpty()) {
                cbxSucursal.getSelectionModel().selectFirst();
                seleccionarSucursal(null);
            }
            
            actualizarInfoSucursales();
            LOGGER.info("Se cargaron " + sucursales.size() + " sucursales");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al cargar sucursales", e);
            Mensaje.error(null, "Error al Cargar", 
                "No se pudieron cargar las sucursales.\n" + e.getMessage());
        }
    }
    
    /**
     * Actualiza la información de sucursales en el label
     */
    private void actualizarInfoSucursales() {
        int total = listaSucursales.size();
        lblSucursalInfo.setText(total + (total == 1 ? " Sucursal" : " Sucursales"));
    }
    
    // ==================== SELECCIÓN DE SUCURSAL ====================
    
    /**
     * Maneja la selección de una sucursal
     */
    @FXML
    private void seleccionarSucursal(ActionEvent event) {
        sucursalSeleccionada = cbxSucursal.getValue();
        
        if (sucursalSeleccionada != null) {
            mostrarDatosSucursal();
            LOGGER.info("Sucursal seleccionada: " + sucursalSeleccionada.getCodSucursal());
        } else {
            limpiarCamposSucursal();
        }
    }
    
    /**
     * Muestra los datos de la sucursal seleccionada
     */
    private void mostrarDatosSucursal() {
        if (sucursalSeleccionada == null) return;

        txtCodSucursal.setText(sucursalSeleccionada.getCodSucursal());
        txtCodPtoVta.setText(sucursalSeleccionada.getCodPtoVta());
        cbxEstadoSuc.setValue(sucursalSeleccionada.getEstadoSuc());
        txtNombreSucursal.setText(sucursalSeleccionada.getNombreSucuPtovta());
        txtNomComercial.setText(sucursalSeleccionada.getNomComercial());

        cargarValoresDepProDist(sucursalSeleccionada.getCodiDepa(),
                                  sucursalSeleccionada.getCodiProv(),
                                  sucursalSeleccionada.getCodiDist());

        txtDireccion.setText(sucursalSeleccionada.getDireccion());
        txtTelefono1.setText(sucursalSeleccionada.getTelef1());
        txtTelefono2.setText(sucursalSeleccionada.getTelef2());
        txtCorreo.setText(sucursalSeleccionada.getCorreoElectro());
    }
    
    /**
     * Limpia los campos de sucursal
     */
    private void limpiarCamposSucursal() {
        txtCodSucursal.clear();
        txtCodPtoVta.clear();
        cbxEstadoSuc.setValue(null);
        txtNombreSucursal.clear();
        txtNomComercial.clear();
        cbxDepartamento.setValue(null);
        cbxProvincia.setValue(null);
        cbxDistrito.setValue(null);
        txtDireccion.clear();
        txtTelefono1.clear();
        txtTelefono2.clear();
        txtCorreo.clear();
    }
    
    // ==================== GUARDAR CAMBIOS ====================
    
    /**
     * Guarda todos los cambios (compañía y sucursal)
     */
    @FXML
    private void guardarCambios(ActionEvent event) {
        LOGGER.info("Guardando cambios");
        
        boolean exitoCompania = guardarDatosCompania();
        boolean exitoSucursal = guardarDatosSucursal();
        
        if (exitoCompania && exitoSucursal) {
            Mensaje.alerta(null, "Guardado Exitoso",
                "Todos los cambios se guardaron correctamente.");
        } else if (exitoCompania) {
            Mensaje.alerta(null, "Guardado Parcial", 
                "Los datos de la compañía se guardaron, pero hubo un problema con la sucursal.");
        } else if (exitoSucursal) {
            Mensaje.alerta(null, "Guardado Parcial", 
                "Los datos de la sucursal se guardaron, pero hubo un problema con la compañía.");
        }
    }
    
    /**
     * Guarda los datos de la compañía
     */
    private boolean guardarDatosCompania() {
        if (datosCompania == null) return false;
        
        try {
            // Actualizar objeto con datos del formulario
            datosCompania.setRuc(txtRuc.getText());
            datosCompania.setRazonSocial(txtRazonSocial.getText().trim());
            datosCompania.setNombre(txtNombre.getText().trim());
            datosCompania.setBanco(txtBanco.getText().trim());
            datosCompania.setCuentaSol(txtCuentaSol.getText().trim());
            datosCompania.setCci(txtCci.getText().trim());
            datosCompania.setDescripcion(txtDescripcion.getText().trim());
            
            // Guardar en base de datos
            return arfamcDao.actualizarDatosCompania(datosCompania);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar datos de compañía", e);
            return false;
        }
    }
    
    /**
     * Guarda los datos de la sucursal seleccionada
     */
    private boolean guardarDatosSucursal() {
        if (sucursalSeleccionada == null) {
            LOGGER.info("No hay sucursal seleccionada para guardar");
            return true; // No es un error si no hay sucursal seleccionada
        }
        
        try {
            // Actualizar objeto con datos del formulario
            sucursalSeleccionada.setNombreSucuPtovta(txtNombreSucursal.getText().trim());
            sucursalSeleccionada.setNomComercial(txtNomComercial.getText().trim());

            Arccdp departamento = cbxDepartamento.getValue();
            sucursalSeleccionada.setCodiDepa(departamento.getCodDepa());
            Arccpr provincia = cbxProvincia.getValue();
            sucursalSeleccionada.setCodiProv(provincia.getCodiProv());
            Arccdi distrito = cbxDistrito.getValue();
            sucursalSeleccionada.setCodiDist(distrito.getCodiDist());

            sucursalSeleccionada.setDireccion(txtDireccion.getText());
            sucursalSeleccionada.setTelef1(txtTelefono1.getText());
            sucursalSeleccionada.setTelef2(txtTelefono2.getText());
            sucursalSeleccionada.setCorreoElectro(txtCorreo.getText());
            sucursalSeleccionada.setEstadoSuc(cbxEstadoSuc.getValue());
            
            // Guardar en base de datos
            return sucursalDao.actualizarSucursal(sucursalSeleccionada);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al guardar datos de sucursal", e);
            return false;
        }
    }
    
    // ==================== REFRESCAR DATOS ====================
    
    /**
     * Refresca todos los datos desde la base de datos
     */
    @FXML
    private void refrescarDatos(ActionEvent event) {
        LOGGER.info("Refrescando datos");
        cargarDatosCompania();
        cargarSucursales();
    }
    
    // ==================== MÉTODOS PÚBLICOS ====================
    
    /**
     * Maneja los atajos de teclado
     */
    public void manejarAtajos(KeyEvent event) {
        // Ctrl+S: Guardar
        if (event.getCode() == KeyCode.S && event.isControlDown()) {
            guardarCambios(null);
            event.consume();
        }
        // F5: Refrescar
        else if (event.getCode() == KeyCode.F5) {
            refrescarDatos(null);
            event.consume();
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
}
