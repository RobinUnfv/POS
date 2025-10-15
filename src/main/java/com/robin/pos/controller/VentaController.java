package com.robin.pos.controller;

import com.robin.pos.dao.Arinda1Dao;
import com.robin.pos.dao.ClienteDao;
import com.robin.pos.model.Arinda1;
import com.robin.pos.model.Cliente;
import com.robin.pos.model.DetalleVenta;
import com.robin.pos.util.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Predicate;

public class VentaController implements Initializable {

    @FXML
    private Button agregarProductoButton;

    @FXML
    private Button btnBoleta;

    @FXML
    private Button btnBuscarCliente;

    @FXML
    private Button btnFactura;

    @FXML
    private Button btnNuevoCliente;

    @FXML
    private ComboBox<String> cbxDocIdentidad;

    @FXML
    private ComboBox<?> cbxFormaPago;

    @FXML
    private ComboBox<?> cbxMoneda;

    @FXML
    private ComboBox<?> cbxMotConting;

    @FXML
    private TextField lblIgv;

    @FXML
    private TextField lblSubTotal;

    @FXML
    private TextField lblTotGravada;

    @FXML
    private Label lblTotal;

    @FXML
    private TableView<DetalleVenta> tVenta;

    @FXML
    private TableColumn<DetalleVenta, Integer> colItem;

    @FXML
    private TableColumn<DetalleVenta, String> colDescripcion;

    @FXML
    private TableColumn<DetalleVenta, Double> colCantidad;

    @FXML
    private TableColumn<DetalleVenta, Double> colPrecio;

    @FXML
    private TableColumn<DetalleVenta, Double> colTotal;

    @FXML
    private TextArea txtDireccion;

    @FXML
    private DatePicker txtFechaVenta;

    @FXML
    private TextField txtNumDoc;

    @FXML
    private TextField txtPago;

    @FXML
    private TextField txtRazSocNom;

    @FXML
    private TextField txtVuelto;


    @FXML
    private TextField txtDesArinda1;

    @FXML
    private TableView<Arinda1> tArinda1;

    @FXML
    private Label lblProducto;

    @FXML
    private TableColumn<Arinda1, String> colProducto;

    @FXML
    private Button btnAgregarProducto;

    @FXML
    private Button btnEliminarProducto;

    FilteredList<Arinda1> filtro;

//    @FXML
//    private AutoCompleteTextField txtListaProd;

    private Task<List<Arinda1>> busquedaTask;

    private ClienteDao clienteDao;
    private Arinda1Dao arinda1Dao;

    ObservableList<DetalleVenta> listaDetalleVentas = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txtFechaVenta.setValue(LocalDate.now());

        // Configurar la tabla de ventas
        configurarTablaVenta();

        // MOSTRAR PRODUCTOS
        this.cargarProductosMostrar();

        // Configurar el AutoCompleteTextField
        // configurarAutoComplete();
        txtPago.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d{0,2})?") ? change : null));

    }

    private void configurarTablaVenta() {
        tVenta.setEditable(true);
        tVenta.getSelectionModel().setCellSelectionEnabled(true);
        tVenta.setItems(listaDetalleVentas);
        tVenta.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
        colItem.setStyle("-fx-alignment: CENTER;");
        colItem.setEditable(false);

        colDescripcion.setCellValueFactory(cellData -> cellData.getValue().getArinda1().descripcionProperty());
        colDescripcion.setCellFactory(TextFieldTableCell.forTableColumn());
        colDescripcion.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DetalleVenta, String>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<DetalleVenta, String> e) {
                        if (!Objects.equals(e.getNewValue(), e.getOldValue())) {
                            ((DetalleVenta) e.getTableView().getItems().get(e.getTablePosition().getRow())).getArinda1().setDescripcion(e.getNewValue().toUpperCase().trim());
//                            Metodos.changeSizeOnColumn(colDescripcion, tVenta, -1);
                            TablePosition<DetalleVenta, ?> pos = e.getTablePosition();
                            if (pos.getColumn() + 1 < tVenta.getColumns().size()) {
                                tVenta.getSelectionModel().clearAndSelect(pos.getRow(), tVenta.getColumns().get(pos.getColumn() + 1));
                                tVenta.edit(pos.getRow(), tVenta.getColumns().get(pos.getColumn() + 1));
                            }
                        }
                    }
                }
        );
        colDescripcion.setEditable(true);

        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setStyle("-fx-alignment: CENTER;");
        colCantidad.setCellFactory(tc -> new DoubleCell<>());
        colCantidad.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DetalleVenta, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<DetalleVenta, Double> e) {
                if (!Objects.equals(e.getNewValue(), e.getOldValue())) {
                    ((DetalleVenta) e.getTableView().getItems().get(e.getTablePosition().getRow())).setCantidad(e.getNewValue());
                      calcularTotales();
//                      Metodos.changeSizeOnColumn(colTotal, tVenta, -1);
                    // como pasar al siguiente campo
                    TablePosition<DetalleVenta, ?> pos = e.getTablePosition();
                    if (pos.getColumn() + 1 < tVenta.getColumns().size()) {
                        tVenta.getSelectionModel().clearAndSelect(pos.getRow(), tVenta.getColumns().get(pos.getColumn() + 1));
                        tVenta.edit(pos.getRow(), tVenta.getColumns().get(pos.getColumn() + 1));
                    }
                }
            }
        });


        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setCellFactory(tc -> new CurrencyCell<>());
        colPrecio.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DetalleVenta, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<DetalleVenta, Double> e) {
                if (!Objects.equals(e.getNewValue(), e.getOldValue())) {
                    ((DetalleVenta) e.getTableView().getItems().get(e.getTablePosition().getRow())).setPrecio(e.getNewValue());
                    calcularTotales();
//                    Metodos.changeSizeOnColumn(colTotal, tVenta, -1);
                }
            }
        });

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(tc -> new CurrencyCell<>());
        colTotal.setEditable(false);
    }

    @FXML
    void buscarCliente(ActionEvent event) {

       if (this.txtNumDoc.getText().isEmpty()) {
           Mensaje.alerta(null,"Número Documento","Ingrese el Número de documento");
           return;
       }

       clienteDao = new ClienteDao();
       Cliente cliente = clienteDao.buscarPorNumId("01",txtNumDoc.getText());

       if (cliente ==  null) {
            Mensaje.error(null,"Consulta cliente","El número de documento "+this.txtNumDoc.getText()+" no valido.");
            this.limpiarCliente();
            return;
       }

       this.mostrarCliente(cliente);
    }

    @FXML
    void buscarClienteNumId(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            if (this.txtNumDoc.getText().isEmpty()) {
                this.limpiarCliente();
                return;
            }

            clienteDao = new ClienteDao();
            Cliente cliente = clienteDao.buscarPorNumId("01",txtNumDoc.getText());

            if (cliente ==  null) {
                Mensaje.error(null,"Consulta cliente","El número de documento "+this.txtNumDoc.getText()+" no valido.");
                this.limpiarCliente();
                return;
            }

            this.mostrarCliente(cliente);
        }
    }

    void mostrarCliente(Cliente cliente) {
        txtNumDoc.setText(cliente.getNoCliente());
        txtRazSocNom.setText(cliente.getNombre());
        txtDireccion.setText(cliente.getDireccion());
    }

    void limpiarCliente(){
        txtRazSocNom.setText("");
        txtDireccion.setText("");
    }

    @FXML
    void escogerTipoDocumento(ActionEvent event) {
        this.limpiarCliente();
        this.txtNumDoc.setText("");

        if (this.cbxDocIdentidad.getSelectionModel().getSelectedItem().equals("DNI")) {
            this.txtNumDoc.setPromptText("DNI");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 8 ? change : null));
        } else if (this.cbxDocIdentidad.getSelectionModel().getSelectedItem().equals("RUC")) {
            this.txtNumDoc.setPromptText("RUC");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 11 ? change : null));

        } else if (this.cbxDocIdentidad.getSelectionModel().getSelectedItem().equals("OTR")) {
            this.txtNumDoc.setPromptText("OTROS");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 11 ? change : null));
            this.txtNumDoc.setText("99999999998");

        } else {
            this.txtNumDoc.setPromptText("Número Documento");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 15 ? change : null));
        }

        this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*") ? change : null));

        this.txtNumDoc.requestFocus();
    }

//    private void configurarAutoComplete() {
//
//        txtListaProd.textProperty().addListener((obs, oldVal, newVal) -> {
//            if (newVal != null && newVal.length() >= 2) {
//                buscarProductosAsync(newVal);
//            }
//        });
//
//        txtListaProd.setOnProductoSeleccionado(producto -> cargarDatosProducto((Arinda1) producto));
//
//    }

    private void buscarProductosAsync(String criterio) {
        // Cancelar búsqueda anterior si existe
        if (busquedaTask != null && busquedaTask.isRunning()) {
            busquedaTask.cancel();
        }

        // Crear nueva tarea de búsqueda
        busquedaTask = new Task<List<Arinda1>>() {
            @Override
            protected List<Arinda1> call() {
                arinda1Dao = new Arinda1Dao();
                return arinda1Dao.buscarProducto("01", criterio); //dao.buscarProductos(criterio);
            }
        };

        busquedaTask.setOnSucceeded(e -> {
            List<Arinda1> productos = busquedaTask.getValue();
            // txtListaProd.mostrarSugerencias(productos);
        });

        busquedaTask.setOnFailed(e -> {
            System.err.println("Error en búsqueda: " + busquedaTask.getException().getMessage());
        });

        // Ejecutar en hilo separado
        Thread thread = new Thread(busquedaTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void cargarDatosProducto(Arinda1 producto) {
        if (producto != null) {
            System.out.println(producto.getCodigo() + " + " + producto.getDescripcion());
        }
    }

    @FXML
    private void buscarArinda1(KeyEvent evt) {
        switch (evt.getCode()) {
            case DOWN:
                tArinda1.requestFocus();
                tArinda1.getSelectionModel().select(0, colProducto);
                break;
            case ESCAPE:
//                this.txtCodBarra.requestFocus();
                break;
            default:
                txtDesArinda1.textProperty().addListener((observableValue, oldValue, newValue) -> {
                    filtro.setPredicate((Predicate<? super Arinda1>) param -> {
                        if (newValue == null || newValue.isEmpty()) {
                            return true;
                        }
                        return (param.getDescripcion().contains(newValue.toUpperCase()));
                    });
                });
                SortedList<Arinda1> sorterData = new SortedList<>(filtro);
                sorterData.comparatorProperty().bind(tArinda1.comparatorProperty());
                tArinda1.setItems(sorterData);
        }
    }

    private void cargarProductosMostrar() {
        arinda1Dao = new Arinda1Dao();
        List<Arinda1> listaArinda1 = arinda1Dao.buscarProducto("01");
        ObservableList<Arinda1> listaObservable = FXCollections.observableArrayList(listaArinda1);
        filtro = new FilteredList<>(listaObservable, e -> true);
        tArinda1.setItems(filtro);
        tArinda1.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tArinda1.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tArinda1.getSelectionModel().selectFirst();

        colProducto.setCellValueFactory( param -> param.getValue().descripcionProperty() );
        Metodos.changeSizeOnColumn(colProducto, tArinda1, -1);

    }

    @FXML
    private void tArinda1KeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE) {
            txtDesArinda1.requestFocus();
            tArinda1.getSelectionModel().clearSelection();
        } else if (event.getCode() == KeyCode.ENTER) {
            agregarDetalleVenta(tArinda1.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void tArinda1MouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
            agregarDetalleVenta(tArinda1.getSelectionModel().getSelectedItem());
        }
    }

    private void agregarDetalleVenta(Arinda1 arinda1) {
        this.listaDetalleVentas.stream().
                filter( p -> p.getArinda1().getCodigo().equals(arinda1.getCodigo()) ).
                findFirst().map( (t) -> {
                    t.setCantidad(t.getCantidad()+1);
                    return t;
                } ).orElseGet( () -> {
                    DetalleVenta dv = new DetalleVenta();
                    dv.setItem( this.listaDetalleVentas.size() + 1 );
                    dv.setArinda1(arinda1);
                    dv.setCantidad(1.0);
                    dv.setPrecio(0.0);
                    dv.setIgv(0.0);


                    this.listaDetalleVentas.add(dv);
                    // Ajustar tamaño de la columna descripción
                    Metodos.changeSizeOnColumn(colDescripcion, tArinda1, -1);
                    // Ajuntar tamaño de la columna colproducto
                    //Metodos.changeSizeOnColumn(colProducto, tArinda1, -1);
                    // Seleccionar y editar la nueva fila
                    tVenta.scrollTo(dv);
                    tVenta.getSelectionModel().select(dv);
                    tVenta.layout();
                    tVenta.edit(tVenta.getItems().size() -1, this.colCantidad);
                    return dv;
                } );
        calcularTotales();
    }

    private void calcularTotales() {
        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

        double total = listaDetalleVentas.stream().mapToDouble( p -> p.getCantidad() * p.getPrecio() ).sum();
        double igv = total * 0.18;
        double subTotal = total / 1.18;

        lblSubTotal.setText(formato.format(subTotal));
        lblIgv.setText(formato.format(igv));
        lblTotGravada.setText(formato.format(subTotal));
        lblTotal.setText(formato.format(total));
    }

    @FXML
    void tVentaKeyPressed(KeyEvent evt) {
         // como poder crear una nueva fila presionando insert
        if (evt.getCode() == KeyCode.INSERT) {
            agregarProductoVenta(new ActionEvent());
        } else  if (evt.getCode() == KeyCode.DELETE) {
            eliminarProductoVenta(new ActionEvent());

//        if (evt.getCode() == KeyCode.) {
//            final TablePosition focusedCell = tVenta.focusModelProperty().get().focusedCellProperty().get();
//            tVenta.edit(focusedCell.getRow(), focusedCell.getTableColumn());
        } else if (evt.getCode() == KeyCode.ESCAPE) {
            this.txtDesArinda1.requestFocus();
            tVenta.getSelectionModel().clearSelection();
        }
    }

    @FXML
    void calcularVuelto(KeyEvent evt) {
        if (evt.getCode() == KeyCode.ENTER) {
            NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));
            if (this.txtPago.getText().isEmpty()) {
                this.txtVuelto.setText("0.00");
                return;
            }
            double pago = Double.parseDouble(this.txtPago.getText());
            double total = listaDetalleVentas.stream().mapToDouble( p -> p.getCantidad() * p.getPrecio() ).sum();
            if (pago < total) {
                Mensaje.alerta(null,"Pago","El monto ingresado es menor al total.");
                this.txtVuelto.setText("0.00");
                this.txtPago.requestFocus();
                return;
            }
            double vuelto = pago - total;
            String vueltoFormato = formato.format(vuelto);
            this.txtVuelto.setText(vueltoFormato);
        }
    }

    @FXML
    void agregarProductoVenta(ActionEvent event) {
        DetalleVenta dv = new DetalleVenta();
        dv.setItem(this.listaDetalleVentas.size() + 1 );
        Arinda1 arinda1 = new Arinda1();
        arinda1.setCodigo(Metodos.generarTextoAleatorio(6));
        arinda1.setDescripcion("");
        dv.setArinda1(arinda1);
        dv.setCantidad(1.0);
        dv.setPrecio(0.0);
        dv.setIgv(0.0);

        this.listaDetalleVentas.add(dv);
        tVenta.scrollTo(dv);
        tVenta.getSelectionModel().select(dv);
        tVenta.layout();
        tVenta.edit(tVenta.getItems().size() -1, colDescripcion);
    }

    @FXML
    void eliminarProductoVenta(ActionEvent event) {
        if (tVenta.getSelectionModel().getSelectedItem() == null) {
            Mensaje.alerta(null,"Eliminar item","Seleccione un producto para eliminar.");
            return;
        }
        if (Mensaje.confirmacion(null,"Eliminar item","¿Está seguro de eliminar el producto seleccionado?").get() != ButtonType.CANCEL) {
            this.listaDetalleVentas.remove(tVenta.getSelectionModel().getSelectedItem());
            // Reenumerar items
            for (int i = 0; i < this.listaDetalleVentas.size(); i++) {
                this.listaDetalleVentas.get(i).setItem(i + 1);
            }
            calcularTotales();
        }
    }

}
