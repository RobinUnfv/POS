package com.robin.pos.controller;

import com.robin.pos.dao.Arinda1Dao;
import com.robin.pos.dao.ClienteDao;
import com.robin.pos.dao.ComprobantePagoDao;
import com.robin.pos.model.*;
import com.robin.pos.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;

public class VentaController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private Button btnBuscarCliente;
    /*
    @FXML
    private Button btnFactura;
    */
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
    private TextArea txtRazSocNom;

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
    /*
    @FXML
    private Label lblNumDoc;
    */
    @FXML
    private Label lblRazSocNom;
    @FXML
    private Label lblGuiaRemision;

    FilteredList<Arinda1> filtro;

    @FXML
    private Button btnPagar;

    @FXML
    private ToggleButton btnBoleta;
    @FXML
    private ToggleButton btnFactura;

    @FXML
    private TextField txtGuiaRemision;

    private Task<List<Arinda1>> busquedaTask;
    private ClienteDao clienteDao;
    private Arinda1Dao arinda1Dao;
    private NumberFormat formatoMoneda;

    private String tipoComprobante;

    ObservableList<DetalleVenta> listaDetalleVentas = FXCollections.observableArrayList();

    private ComprobantePagoDao comprobantePagoDao = new ComprobantePagoDao();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Inicializar formato de moneda
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

       // this.lblNumDoc.setText("N° Doc:");
        this.lblRazSocNom.setText("Nombres:");
        this.txtNumDoc.setText("99999999998");
        this.cbxDocIdentidad.getItems().addAll("CE", "DNI", "RUC", "OTR");
        this.cbxDocIdentidad.setValue("OTR");

        txtFechaVenta.setValue(LocalDate.now());

        // Configurar la tabla de ventas
        configurarTablaVenta();

        // MOSTRAR PRODUCTOS
        this.cargarProductosMostrar();

        // Configurar el formatter para el campo de pago
        txtPago.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d{0,2})?") ? change : null));

        this.mostrarGuiaRemision(false);
        // Configurar botón de boleta como seleccionado por defecto
        tipoComprobante = "B";
        btnBoleta.setSelected(true);
        btnBoleta.fire();
        // Estilo seleccionado para BOLETA
        btnBoleta.setStyle(
                "-fx-background-color: #16BB60; " +
                        "-fx-border-color: #999999; " +
                        "-fx-border-width: 1px; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 2px; " +
                        "-fx-border-radius: 2px;"
        );
    }

    private void configurarTablaVenta() {
        tVenta.setEditable(true);
        tVenta.getSelectionModel().setCellSelectionEnabled(true);
        tVenta.setItems(listaDetalleVentas);

        // ===============================================
        // IMPORTANTE: Usar CONSTRAINED_RESIZE_POLICY
        // para que las columnas mantengan proporciones
        // ===============================================
        tVenta.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Deshabilitar reordenamiento y sorting
        tVenta.setSortPolicy(tv -> false);

        // Configurar altura de fila
        tVenta.setFixedCellSize(32);

        // ===============================================
        // COLUMNA ITEM
        // ===============================================
        colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
        colItem.setStyle("-fx-alignment: CENTER;");
        colItem.setEditable(false);
        colItem.setResizable(false);
        colItem.setReorderable(false);
        colItem.setSortable(false);
        colItem.setMinWidth(50);
        colItem.setMaxWidth(50);

        // ===============================================
        // COLUMNA DESCRIPCIÓN (LA CLAVE)
        // ===============================================
        colDescripcion.setCellValueFactory(cellData ->
                cellData.getValue().getArinda1().descripcionProperty()
        );

        // Configurar CellFactory con wrap text
        colDescripcion.setCellFactory(column -> {
            TableCell<DetalleVenta, String> cell = new TableCell<DetalleVenta, String>() {
                private Text text = new Text();

                {
                    // Configurar el Text para wrap automático
                    text.wrappingWidthProperty().bind(
                            colDescripcion.widthProperty().subtract(20)
                    );
                    text.setStyle("-fx-font-size: 13px;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                        setTooltip(null);
                    } else {
                        // Mostrar con wrap
                        text.setText(item);
                        setGraphic(text);

                        // Agregar tooltip si el texto es largo
                        if (item.length() > 30) {
                            Tooltip tooltip = new Tooltip(item);
                            tooltip.setWrapText(true);
                            tooltip.setMaxWidth(400);
                            setTooltip(tooltip);
                        }
                    }
                }

                @Override
                public void startEdit() {
                    super.startEdit();
                    if (isEmpty()) return;

                    TextField textField = new TextField(getItem());
                    textField.setOnAction(event -> {
                        commitEdit(textField.getText());
                    });
                    textField.setOnKeyPressed(event -> {
                        if (event.getCode() == KeyCode.ESCAPE) {
                            cancelEdit();
                        }
                    });
                    setGraphic(textField);
                    setText(null);
                    textField.selectAll();
                    textField.requestFocus();
                }

                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    text.setText(getItem());
                    setGraphic(text);
                    setText(null);
                }
            };

            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });

        colDescripcion.setOnEditCommit(event -> {
            if (!Objects.equals(event.getNewValue(), event.getOldValue())) {
                DetalleVenta item = event.getTableView().getItems()
                        .get(event.getTablePosition().getRow());
                item.getArinda1().setDescripcion(
                        event.getNewValue().toUpperCase().trim()
                );

                // Navegar a la siguiente columna
                TablePosition<DetalleVenta, ?> pos = event.getTablePosition();
                if (pos.getColumn() + 1 < tVenta.getColumns().size()) {
                    tVenta.getSelectionModel().clearAndSelect(
                            pos.getRow(),
                            tVenta.getColumns().get(pos.getColumn() + 1)
                    );
                    tVenta.edit(
                            pos.getRow(),
                            tVenta.getColumns().get(pos.getColumn() + 1)
                    );
                }

                // Refrescar la tabla para actualizar el wrap
                tVenta.refresh();
            }
        });

        colDescripcion.setEditable(true);
        colDescripcion.setReorderable(false);
        colDescripcion.setSortable(false);
        colDescripcion.setMinWidth(200);
        // NO establecer maxWidth para que se expanda

        // ===============================================
        // COLUMNA CANTIDAD
        // ===============================================
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCantidad.setStyle("-fx-alignment: CENTER-RIGHT;");
        colCantidad.setCellFactory(tc -> new DoubleCell<>());
        colCantidad.setOnEditCommit(event -> {
            if (!Objects.equals(event.getNewValue(), event.getOldValue())) {
                DetalleVenta item = event.getTableView().getItems()
                        .get(event.getTablePosition().getRow());
                item.setCantidad(event.getNewValue());
                calcularTotales();

                // Navegar a la siguiente columna
                TablePosition<DetalleVenta, ?> pos = event.getTablePosition();
                if (pos.getColumn() + 1 < tVenta.getColumns().size()) {
                    tVenta.getSelectionModel().clearAndSelect(
                            pos.getRow(),
                            tVenta.getColumns().get(pos.getColumn() + 1)
                    );
                    tVenta.edit(
                            pos.getRow(),
                            tVenta.getColumns().get(pos.getColumn() + 1)
                    );
                }
            }
        });
        colCantidad.setReorderable(false);
        colCantidad.setSortable(false);
        colCantidad.setMinWidth(70);
        colCantidad.setMaxWidth(100);

        // ===============================================
        // COLUMNA PRECIO
        // ===============================================
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colPrecio.setStyle("-fx-alignment: CENTER-RIGHT;");
        colPrecio.setCellFactory(tc -> new CurrencyCell<>());
        colPrecio.setOnEditCommit(event -> {
            if (!Objects.equals(event.getNewValue(), event.getOldValue())) {
                DetalleVenta item = event.getTableView().getItems()
                        .get(event.getTablePosition().getRow());
                item.setPrecio(event.getNewValue());
                calcularTotales();
                tVenta.refresh();
            }
        });
        colPrecio.setReorderable(false);
        colPrecio.setSortable(false);
        colPrecio.setMinWidth(100);
        colPrecio.setMaxWidth(120);

        // ===============================================
        // COLUMNA TOTAL
        // ===============================================
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");
        colTotal.setCellFactory(tc -> new CurrencyCell<>());
        colTotal.setEditable(false);
        colTotal.setReorderable(false);
        colTotal.setSortable(false);
        colTotal.setMinWidth(100);
        colTotal.setMaxWidth(130);
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

    private void validarTipoDocumento() {
        String tipoDoc = this.cbxDocIdentidad.getSelectionModel().getSelectedItem();

        if (tipoDoc.equals("RUC") && tipoComprobante.equals("B")) {
            Mensaje.error(null,"Validación de documento",
                    "No se puede emitir una BOLETA con tipo de documento RUC.");
            cbxDocIdentidad.setValue("OTR");
            return;
        }

        if (tipoDoc.equals("OTR") && tipoComprobante.equals("F")) {
            Mensaje.error(null,"Validación de documento",
                    "No se puede emitir una FACTURA con tipo de documento OTROS.");
            cbxDocIdentidad.setValue("RUC");
            return;
        }

        if (tipoDoc.equals("CE") && tipoComprobante.equals("F")) {
            Mensaje.error(null,"Validación de documento",
                    "No se puede emitir una FACTURA con tipo de documento CARNET DE EXTRANJERÍA.");
            cbxDocIdentidad.setValue("RUC");
            return;
        }

        if (tipoDoc.equals("DNI") && tipoComprobante.equals("F")) {
            Mensaje.error(null,"Validación de documento",
                    "No se puede emitir una FACTURA con tipo de documento DNI.");
            cbxDocIdentidad.setValue("RUC");
            return;
        }

    }

    @FXML
    void escogerTipoDocumento(ActionEvent event) {
        validarTipoDocumento();
        this.limpiarCliente();
        this.txtNumDoc.setText("");
        String tipoDoc = this.cbxDocIdentidad.getSelectionModel().getSelectedItem();

        if (tipoDoc.equals("DNI")) {
            this.txtNumDoc.setPromptText("DNI");
            Metodos.configuracionNumeroDocumento(this.txtNumDoc, "DNI");
           // this.lblNumDoc.setText("DNI:");
            this.lblRazSocNom.setText("Apellido y Nombre:");
        } else if (tipoDoc.equals("RUC")) {
            this.txtNumDoc.setPromptText("RUC");
            Metodos.configuracionNumeroDocumento(this.txtNumDoc, "RUC");
           // this.lblNumDoc.setText("RUC:");
            this.lblRazSocNom.setText("Razón Social:");
        } else if (tipoDoc.equals("OTR")) {
            this.txtNumDoc.setPromptText("OTROS");
            this.txtNumDoc.setText("99999999998");
           // this.lblNumDoc.setText("N° Doc:");
            this.lblRazSocNom.setText("Nombres:");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 15 ? change : null));
        } else {
            this.txtNumDoc.setPromptText("Número Documento");
           // this.lblNumDoc.setText("N° Doc:");
            this.lblRazSocNom.setText("Nombres:");
            this.txtNumDoc.setTextFormatter(new TextFormatter<String>(change ->
                    change.getControlNewText().length() <= 15 ? change : null));
        }

        this.txtNumDoc.requestFocus();
    }

    @FXML
    private void buscarArinda1(KeyEvent evt) {
        switch (evt.getCode()) {
            case DOWN:
                tArinda1.requestFocus();
                tArinda1.getSelectionModel().select(0, colProducto);
                break;
            case ESCAPE:
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
        tArinda1.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tArinda1.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tArinda1.getSelectionModel().selectFirst();

        // Configurar altura dinámica para las filas
        tArinda1.setFixedCellSize(Region.USE_COMPUTED_SIZE);

        // ============================================
        // CONFIGURAR COLUMNA CON WRAP TEXT
        // ============================================
        colProducto.setCellValueFactory(param -> param.getValue().descripcionProperty());

        colProducto.setCellFactory(column -> {
            TableCell<Arinda1, String> cell = new TableCell<Arinda1, String>() {
                private Text text = new Text();

                {
                    // Configurar el Text para wrap automático
                    text.wrappingWidthProperty().bind(
                            colProducto.widthProperty().subtract(20)
                    );
                    text.setStyle("-fx-font-size: 13px;");
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);

                        // Ajustar altura de la celda al contenido
                        setPrefHeight(Region.USE_COMPUTED_SIZE);
                    }
                }
            };

            cell.setAlignment(Pos.CENTER_LEFT);
            return cell;
        });

        // ELIMINAR esta línea que causa problemas:
        // Metodos.changeSizeOnColumn(colProducto, tArinda1, -1);
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
        if (arinda1 == null) {
            return;
        }

        // Buscar si el producto ya existe
        DetalleVenta[] productoExistente = {null};

        this.listaDetalleVentas.stream()
                .filter(p -> p.getArinda1().getCodigo().equals(arinda1.getCodigo()))
                .findFirst()
                .ifPresentOrElse(
                        // Si existe, incrementar cantidad
                        existente -> {
                            existente.setCantidad(existente.getCantidad() + 1);
                            productoExistente[0] = existente;

                            // Seleccionar y resaltar la fila existente
                            tVenta.getSelectionModel().select(existente);
                            tVenta.scrollTo(existente);

                            // Aplicar efecto visual temporal
                            resaltarFilaExistente(existente);

                            // Editar la celda de cantidad
                            Platform.runLater(() -> {
                                int rowIndex = tVenta.getItems().indexOf(existente);
                                tVenta.edit(rowIndex, colCantidad);
                            });
                        },
                        // Si no existe, agregar nuevo
                        () -> {
                            DetalleVenta dv = new DetalleVenta();
                            dv.setItem(this.listaDetalleVentas.size() + 1);
                            dv.setArinda1(arinda1);
                            dv.setCantidad(1.0);
                            dv.setPrecio(0.0);
                            dv.setIgv(0.0);

                            this.listaDetalleVentas.add(dv);

                            // Seleccionar y editar la nueva fila
                            tVenta.scrollTo(dv);
                            tVenta.getSelectionModel().select(dv);
                            Platform.runLater(() -> {
                                tVenta.edit(tVenta.getItems().size() - 1, this.colCantidad);
                            });
                        }
                );

        calcularTotales();
        tVenta.refresh();
    }

    /**
     * Resalta visualmente una fila cuando se agrega un producto duplicado
     */
    private void resaltarFilaExistente(DetalleVenta detalleVenta) {
        // Obtener el índice de la fila
        int rowIndex = tVenta.getItems().indexOf(detalleVenta);

        // Aplicar animación de resaltado usando Timeline
        Timeline timeline = new Timeline();

        // Crear una marca temporal para identificar la fila
        detalleVenta.setResaltado(true);

        // Después de 2 segundos, quitar el resaltado
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.seconds(2), event -> {
                    detalleVenta.setResaltado(false);
                    tVenta.refresh();
                })
        );

        timeline.play();
        tVenta.refresh();
    }

    private void calcularTotales() {
        double total = listaDetalleVentas.stream()
                .mapToDouble(p -> p.getCantidad() * p.getPrecio())
                .sum();
        double igv = total * 0.18;
        double subTotal = total / 1.18;

        lblSubTotal.setText(formatoMoneda.format(subTotal));
        lblIgv.setText(formatoMoneda.format(igv));
        lblTotGravada.setText(formatoMoneda.format(subTotal));
        lblTotal.setText(formatoMoneda.format(total));
    }

    @FXML
    void tVentaKeyPressed(KeyEvent evt) {
        if (evt.getCode() == KeyCode.INSERT) {
            agregarProductoVenta(new ActionEvent());
        } else if (evt.getCode() == KeyCode.DELETE) {
            eliminarProductoVenta(new ActionEvent());
        } else if (evt.getCode() == KeyCode.ESCAPE) {
            this.txtDesArinda1.requestFocus();
            tVenta.getSelectionModel().clearSelection();
        }
    }

    @FXML
    void calcularVuelto(KeyEvent evt) {
        if (evt.getCode() == KeyCode.ENTER) {
            if (this.txtPago.getText().isEmpty()) {
                this.txtVuelto.setText("0.00");
                return;
            }

            try {
                double pago = Double.parseDouble(this.txtPago.getText());
                double total = listaDetalleVentas.stream()
                        .mapToDouble(p -> p.getCantidad() * p.getPrecio())
                        .sum();

                if (pago < total) {
                    Mensaje.alerta(null, "Pago", "El monto ingresado es menor al total.");
                    this.txtVuelto.setText("0.00");
                    this.txtPago.requestFocus();
                    return;
                }

                double vuelto = pago - total;
                this.txtVuelto.setText(formatoMoneda.format(vuelto));
            } catch (NumberFormatException e) {
                Mensaje.error(null, "Error", "Ingrese un monto válido");
                this.txtVuelto.setText("0.00");
            }
        }
    }

    @FXML
    void agregarProductoVenta(ActionEvent event) {
        DetalleVenta dv = new DetalleVenta();
        dv.setItem(this.listaDetalleVentas.size() + 1);

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
        Platform.runLater(() -> {
            tVenta.edit(tVenta.getItems().size() - 1, colDescripcion);
        });
    }

    @FXML
    void eliminarProductoVenta(ActionEvent event) {
        DetalleVenta itemSeleccionado = tVenta.getSelectionModel().getSelectedItem();

        if (itemSeleccionado == null) {
            Mensaje.alerta(null, "Eliminar item", "Seleccione un producto para eliminar.");
            return;
        }

        if (Mensaje.confirmacion(null, "Eliminar item",
                "¿Está seguro de eliminar el producto seleccionado?").get() != ButtonType.CANCEL) {
            this.listaDetalleVentas.remove(itemSeleccionado);

            // Reenumerar items
            for (int i = 0; i < this.listaDetalleVentas.size(); i++) {
                this.listaDetalleVentas.get(i).setItem(i + 1);
            }

            calcularTotales();
            tVenta.refresh();
        }
    }

    @FXML
    void nuevoCliente(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/robin/pos/fxml/Sunat.fxml")
        );
        VBox vbox = loader.load();

        // Obtener controller y registrar callback
        SunatController sunatController = loader.getController();
        sunatController.setOnRegistro(numeroDocumento -> {
            Platform.runLater(() -> {
                txtNumDoc.setText(numeroDocumento);
                try {
                    buscarCliente(new ActionEvent());
                } catch (Exception ex) {
                    Mensaje.error(null, "Error",
                            "Ocurrió un error al buscar el cliente recién registrado: "
                                    + ex.getMessage());
                }
            });
        });

        Scene scene = new Scene(vbox);
        Stage stage = new Stage();
        stage.setTitle("Buscar Cliente - SUNAT");
        stage.setScene(scene);
        stage.initOwner(root.getScene().getWindow());
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.showAndWait();
    }

    @FXML
    void realizarPago(ActionEvent event) {
        String numeroDoc = this.txtNumDoc.getText();
        if (numeroDoc == null || numeroDoc.isEmpty()) {
            Mensaje.error (null, "Validación de cliente",
                    "Ingrese el número de documento del cliente.");
            Platform.runLater(() -> {
                this.txtNumDoc.requestFocus();
            });
            return ;
        }

        // Validar productos
        if (!validarProductosParaPago()) {
            return;
        }

        String tipoComprobanteDesc = tipoComprobante.equals("B") ? "BOLETA" : "FACTURA";
        String msjComprobante = "Se generará la " + tipoComprobanteDesc + "\n" + "¿Desea continuar?";

        if (tipoComprobanteDesc.equalsIgnoreCase("FACTURA")) {
            String guiaRemision = this.txtGuiaRemision.getText();
            if (guiaRemision == null || guiaRemision.isEmpty()) {

                if (Mensaje.confirmacion(null, "Validación de guía de remisión",
                        "¿Desea Ingrese el número de guía de remisión para emitir la FACTURA?").get() == ButtonType.OK) {

                    Platform.runLater(() -> {
                        this.txtGuiaRemision.requestFocus();
                    });
                    return ;

                }

            }
        }

        // Confirmar la operación
        if (Mensaje.confirmacion(null, "Confirmar Pago",
                msjComprobante).get() == ButtonType.OK) {
            emitirComprobantePago();
        }

    }

    /**
     * Emite el comprobante de pago llamando al procedimiento de Oracle
     * FACTU.PR_COMPROBANTE_PAGO.EMISION_COMPRO_PAGO
     *
     * Respuesta esperada del procedimiento:
     * <emisionComprobantePagoResponse>
     *   <noCia>01</noCia>
     *   <noCliente>99999999998</noCliente>
     *   <noOrden>9410000129</noOrden>
     *   <noGuia>9950001311</noGuia>
     *   <noFactu>B0010000026</noFactu>
     *   <fecha>2025-12-26</fecha>
     *   <resultado>OK</resultado>
     * </emisionComprobantePagoResponse>
     */
    private void emitirComprobantePago() {
        // Mostrar diálogo de progreso
        ProgressDialog progressDialog = new ProgressDialog();
        progressDialog.setTitle("Generando Comprobante");
        progressDialog.setMessage("Por favor espere mientras se genera el comprobante de pago...");
        progressDialog.setIndeterminate(true);
        progressDialog.show();

        // Crear tarea en segundo plano
        Task<ResultadoEmision> task = new Task<ResultadoEmision>() {
            @Override
            protected ResultadoEmision call() throws Exception {
                // Construir parámetros del comprobante
                ParametrosComprobante parametros = construirParametrosComprobante();

                // Llamar al DAO para emitir el comprobante
                return comprobantePagoDao.emitirComprobante(parametros);
            }
        };

        // Manejar resultado exitoso
        task.setOnSucceeded(event -> {
            progressDialog.close();
            ResultadoEmision resultado = task.getValue();

            if (resultado.isExito()) {
                // Mostrar mensaje de éxito con información del comprobante
                StringBuilder mensajeExito = new StringBuilder();
                mensajeExito.append("¡Comprobante emitido exitosamente!\n\n");

                if (resultado.getNoFactu() != null) {
                    mensajeExito.append("N° Comprobante: ").append(resultado.getNoFactu()).append("\n");
                }
                if (resultado.getNoOrden() != null) {
                    mensajeExito.append("N° Orden: ").append(resultado.getNoOrden()).append("\n");
                }

                if (resultado.getNoGuia() != null) {
                    mensajeExito.append("N° Guía: ").append(resultado.getNoGuia()).append("\n");
                }

                if (resultado.getFecha() != null) {
                    mensajeExito.append("Fecha: ").append(resultado.getFecha());
                }

                Mensaje.alerta(null, "Comprobante Generado", mensajeExito.toString());

                // Imprimir el comprobante
                imprimirComprobante(resultado, new ArrayList<>(listaDetalleVentas));

                // Limpiar formulario después de la venta exitosa
                limpiarFormularioVenta();

                // TODO: Aquí puedes agregar lógica para imprimir el comprobante
                // imprimirComprobante(resultado);

            } else {
                // Mostrar error
                Mensaje.error(null, "Error en Comprobante",
                        resultado.getMensaje() != null ?
                                resultado.getMensaje() :
                                "Error desconocido al generar el comprobante");

                // Log del XML de respuesta para debug
                if (resultado.getXmlRespuesta() != null) {
                    System.err.println("XML Respuesta Error: " + resultado.getXmlRespuesta());
                }
            }
        });

        // Manejar error en la tarea
        task.setOnFailed(event -> {
            progressDialog.close();
            Throwable exception = task.getException();
            Mensaje.error(null, "Error",
                    "Ocurrió un error al procesar el comprobante: " +
                            (exception != null ? exception.getMessage() : "Error desconocido"));
            if (exception != null) {
                exception.printStackTrace();
            }
        });

        // Ejecutar tarea en nuevo hilo
        new Thread(task).start();
    }

    /**
     * Limpia el formulario después de una venta exitosa
     */
    private void limpiarFormularioVenta() {
        // Limpiar lista de productos
        listaDetalleVentas.clear();

        // Resetear tipo de comprobante a Boleta
        tipoComprobante = "B";
        btnBoleta.setSelected(true);
        btnBoleta.fire();

        // Resetear cliente a valores por defecto
        cbxDocIdentidad.setValue("OTR");
        txtNumDoc.setText("99999999998");
        txtRazSocNom.setText("CLIENTES VARIOS");
        txtDireccion.setText("");

        // Estilo seleccionado para BOLETA
        btnBoleta.setStyle(
                "-fx-background-color: #16BB60; " +
                        "-fx-border-color: #999999; " +
                        "-fx-border-width: 1px; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 2px; " +
                        "-fx-border-radius: 2px;"
        );

        // Estilo normal para FACTURA
        btnFactura.setStyle(
                "-fx-background-color: white; " +
                        "-fx-border-color: #999999; " +
                        "-fx-border-width: 1px; " +
                        "-fx-text-fill: #333333; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        "-fx-background-radius: 2px; " +
                        "-fx-border-radius: 2px;"
        );

        // Resetear campos de pago
        txtPago.setText("0.00");
        txtVuelto.setText("0.00");

        // Resetear totales
        lblSubTotal.setText("S/ 0.00");
        lblIgv.setText("S/ 0.00");
        lblTotGravada.setText("S/ 0.00");
        lblTotal.setText("S/ 0.00");

        // Ocultar guía de remisión
        mostrarGuiaRemision(false);

        // Actualizar fecha
        txtFechaVenta.setValue(LocalDate.now());

        // Refrescar tabla
        tVenta.refresh();

        // Enfocar el campo de búsqueda de productos
        Platform.runLater(() -> txtDesArinda1.requestFocus());
    }

    /**
     * Construye el objeto de parámetros para el comprobante
     * basándose en los datos del formulario de venta
     */
    private ParametrosComprobante construirParametrosComprobante() {
        ParametrosComprobante params = new ParametrosComprobante();

        // Datos del cliente
        params.setNoCliente(txtNumDoc.getText().trim());
        params.setNombreCliente(txtRazSocNom.getText() != null ?
                txtRazSocNom.getText().trim() : "CLIENTE VARIOS");
        params.setDireccionComercial(txtDireccion.getText() != null ?
                txtDireccion.getText().trim() : "");

        // Tipo de documento del cliente
        String tipoDocCliente = cbxDocIdentidad.getValue();
        switch (tipoDocCliente) {
            case "RUC":
                params.setTipoDocCli("RUC");
                params.setRuc(txtNumDoc.getText().trim());
                break;
            case "DNI":
                params.setTipoDocCli("DNI");
                break;
            case "CE":
                params.setTipoDocCli("CE");
                break;
            default:
                params.setTipoDocCli("OTR");
                break;
        }

        // Tipo de comprobante (B=Boleta, F=Factura)
        params.setTipoDocumento(tipoComprobante);

        // Fecha de la venta
        params.setFecha(txtFechaVenta.getValue() != null ?
                txtFechaVenta.getValue() : LocalDate.now());

        // Guía de remisión (solo para facturas)
        if ("F".equals(tipoComprobante) && txtGuiaRemision.getText() != null
                && !txtGuiaRemision.getText().trim().isEmpty()) {
            params.setGuiaRemision(txtGuiaRemision.getText().trim());
        }

        // Calcular totales
        double totalConIgv = listaDetalleVentas.stream()
                .mapToDouble(dv -> dv.getCantidad() * dv.getPrecio())
                .sum();
        double subTotal = totalConIgv / 1.18;
        double igv = totalConIgv - subTotal;

        params.setSubTotal(subTotal);
        params.setTotalIgv(igv);
        params.setTotalPrecio(totalConIgv);
        params.setPorcentajeIgv(18);

        // Moneda (TODO: obtener del ComboBox cuando esté implementado)
        params.setMoneda("SOL");
        params.setTipoCambio(3.70); // TODO: obtener tipo de cambio actual

        // Datos del vendedor/cajero (TODO: obtener del usuario logueado)
        params.setNoVendedor("48750185"); // Código del vendedor
        params.setUsuario("YPC");          // Usuario del sistema
        params.setCajera("900002");        // Código de cajera
        params.setCodCaja("C11");          // Código de caja

        // Detalles de la venta
        params.setDetalles(new ArrayList<>(listaDetalleVentas));

        return params;
    }

    /**
     * Valida los productos antes de generar el comprobante de pago
     * @return true si la validación es exitosa
     */
    private boolean validarProductosParaPago() {
        if (listaDetalleVentas.isEmpty()) {
            Mensaje.alerta(null, "Validación de productos",
                    "Debe agregar al menos un producto para realizar la venta.");
            return false;
        }

        for (DetalleVenta dv : listaDetalleVentas) {
            if (dv.getArinda1().getDescripcion() == null ||
                    dv.getArinda1().getDescripcion().trim().isEmpty()) {
                Mensaje.error(null, "Validación de productos",
                        "La descripción del producto en el item " + dv.getItem() +
                                " no puede estar vacía.");
                return false;
            }
            if (dv.getCantidad() <= 0) {
                Mensaje.error(null, "Validación de productos",
                        "La cantidad del producto '" + dv.getArinda1().getDescripcion() +
                                "' debe ser mayor a cero.");
                return false;
            }
            if (dv.getPrecio() <= 0) {
                Mensaje.error(null, "Validación de productos",
                        "El precio del producto '" + dv.getArinda1().getDescripcion() +
                                "' debe ser mayor a cero.");
                return false;
            }
        }
        return true;
    }

    @FXML
    void onBoleta(ActionEvent event) {
        if ( this.btnBoleta.isSelected() ) {
            tipoComprobante = "B";
            cbxDocIdentidad.setValue("OTR");
            validarTipoDocumento();
            this.mostrarGuiaRemision(false);

            // Estilo seleccionado para BOLETA
            btnBoleta.setStyle(
                    "-fx-background-color: #16BB60; " +
                            "-fx-border-color: #999999; " +
                            "-fx-border-width: 1px; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 2px; " +
                            "-fx-border-radius: 2px;"
            );

            // Estilo normal para FACTURA
            btnFactura.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #999999; " +
                            "-fx-border-width: 1px; " +
                            "-fx-text-fill: #333333; " +
                            "-fx-font-size: 11px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-background-radius: 2px; " +
                            "-fx-border-radius: 2px;"
            );


        }
    }

    @FXML
    void onFactura(ActionEvent event) {
        if (btnFactura.isSelected()) {
                tipoComprobante = "F";
                cbxDocIdentidad.setValue("RUC");
                validarTipoDocumento();
                this.mostrarGuiaRemision(true);

                // Estilo seleccionado para FACTURA
                btnFactura.setStyle(
                      "-fx-background-color: #16BB60; " +
                             "-fx-border-color: #999999; " +
                             "-fx-border-width: 1px; " +
                             "-fx-text-fill: #333333; " +
                             "-fx-font-size: 11px; " +
                             "-fx-font-weight: bold; " +
                             "-fx-background-radius: 2px; " +
                             "-fx-border-radius: 2px;"
                );

                // Estilo normal para BOLETA
                btnBoleta.setStyle(
                      "-fx-background-color: white; " +
                             "-fx-border-color: #999999; " +
                             "-fx-border-width: 1px; " +
                             "-fx-text-fill: #333333; " +
                             "-fx-font-size: 11px; " +
                             "-fx-font-weight: bold; " +
                             "-fx-background-radius: 2px; " +
                             "-fx-border-radius: 2px;"
                );
            Platform.runLater(() -> {
                this.txtNumDoc.requestFocus();
            });
        }
    }

    private void mostrarGuiaRemision(Boolean mostrar) {
        // Ocultar campo de guía de remisión
        txtGuiaRemision.setVisible(mostrar);
        txtGuiaRemision.setManaged(mostrar);
        txtGuiaRemision.clear();

        lblGuiaRemision.setVisible(mostrar);
        lblGuiaRemision.setManaged(mostrar);
    }

    /**
     * Imprime el comprobante de pago usando JasperReports
     *
     * @param resultado Resultado de la emisión del comprobante
     * @param detalles Lista de detalles de la venta (copiar antes de limpiar)
     */
    private void imprimirComprobante(ResultadoEmision resultado, List<DetalleVenta> detalles) {
        // Preparar datos del cliente
        DatosCliente datosCliente = new DatosCliente();
        datosCliente.setNombre(txtRazSocNom.getText());
        datosCliente.setNumeroDocumento(txtNumDoc.getText());
        datosCliente.setTipoDocumento(cbxDocIdentidad.getValue());
        datosCliente.setDireccion(txtDireccion.getText());

        // Preparar datos de la venta
        DatosVenta datosVenta = new DatosVenta();
        datosVenta.setFechaEmision(txtFechaVenta.getValue());
        datosVenta.setMoneda("SOL"); // TODO: obtener del ComboBox
        datosVenta.setCondicionPago("VENTA CONTADO"); // TODO: obtener del ComboBox
        datosVenta.setVendedor("YPC"); // TODO: obtener del usuario logueado
        datosVenta.setPorcentajeIgv(18);

        // Mostrar el reporte
        Platform.runLater(() -> {
            ReporteComprobantePago reporteComprobantePago = new ReporteComprobantePago();
            reporteComprobantePago.generarReporte(resultado, detalles, datosCliente, datosVenta);
        });
    }


}