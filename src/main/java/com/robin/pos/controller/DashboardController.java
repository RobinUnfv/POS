package com.robin.pos.controller;

import java.io.IOException;

import com.robin.pos.MainApp;
import com.robin.pos.util.Mensaje;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class DashboardController {

    @FXML private VBox sidebar;
    @FXML private HBox headerBox;
    @FXML private VBox menuContainer;
    @FXML private Button btnToggleMenu;
    @FXML private ImageView imgLogoCia;
    
    // Botones del menú principal
    @FXML private Button btnDashboard;
    @FXML private Button btnVentas;
    @FXML private Button btnCliente;
    @FXML private Button btnProductos;
    @FXML private Button btnReportes;
    @FXML private Button btnSalir;
    
    // Botones del sub-menú
    @FXML private Button btnFactura;
    @FXML private Button btnProforma;
    @FXML private Button btnNuevoCliente;
    @FXML private Button btnListaClientes;
    @FXML private Button btnNuevoProducto;
    @FXML private Button btnCatalogo;
    
    // Sub-menús
    @FXML private VBox ventasSubmenu;
    @FXML private VBox clientesSubmenu;
    @FXML private VBox productosSubmenu;
    
    // Flechas de sub-menús
    @FXML private Label lblVentasArrow;
    @FXML private Label lblClientesArrow;
    @FXML private Label lblProductosArrow;
    
    // Iconos del menú
    @FXML private Label iconDashboard;
    @FXML private Label iconVentas;
    @FXML private Label iconClientes;
    @FXML private Label iconProductos;
    @FXML private Label iconReportes;
    
    // Otros componentes
    @FXML private TabPane tabPane;
    @FXML private TextField txtBuscar;
    @FXML private Label lblUsuario;
    
    // Variables de estado
    private boolean isSidebarExpanded = true;
    private static final double EXPANDED_WIDTH = 220.0;
    private static final double COLLAPSED_WIDTH = 70.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    
    private Tab tabVenta;
    private Tab tabCliente;
    private Tab tabArticulo;

    @FXML
    public void initialize() {
        // Configurar el estado inicial
        setupInitialState();
        
        // Configurar eventos de hover para efectos visuales
        setupHoverEffects();
        
        System.out.println("Dashboard Controller inicializado correctamente");
    }

    /**
     * Configurar el estado inicial del sidebar
     */
    private void setupInitialState() {
        // Asegurar que todos los sub-menús estén cerrados al inicio
        closeAllSubmenus();
        
        // Configurar el ancho inicial del sidebar
        sidebar.setPrefWidth(EXPANDED_WIDTH);
        sidebar.setMinWidth(EXPANDED_WIDTH);
        sidebar.setMaxWidth(EXPANDED_WIDTH);
    }

    /**
     * Configurar efectos de hover para los botones
     */
    private void setupHoverEffects() {
        // Aquí puedes agregar efectos adicionales de hover si lo deseas
    }

    /**
     * Alternar entre sidebar expandido y colapsado
     */
    @FXML
    public void toggleSidebar() {
        isSidebarExpanded = !isSidebarExpanded;
        
        // Cerrar todos los sub-menús al colapsar
        if (!isSidebarExpanded) {
            closeAllSubmenus();
        }
        
        // Animación de ancho del sidebar
        animateSidebarWidth();
        
        // Animar visibilidad de textos y logo
        animateContentVisibility();
        
        // Cambiar el símbolo del botón toggle
        animateToggleButton();
    }

    /**
     * Animar el ancho del sidebar
     */
    private void animateSidebarWidth() {
        double targetWidth = isSidebarExpanded ? EXPANDED_WIDTH : COLLAPSED_WIDTH;
        
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(sidebar.prefWidthProperty(), sidebar.getPrefWidth()),
                new KeyValue(sidebar.minWidthProperty(), sidebar.getMinWidth()),
                new KeyValue(sidebar.maxWidthProperty(), sidebar.getMaxWidth())
            ),
            new KeyFrame(ANIMATION_DURATION,
                new KeyValue(sidebar.prefWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                new KeyValue(sidebar.minWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
                new KeyValue(sidebar.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH)
            )
        );
        timeline.play();
    }

    /**
     * Animar la visibilidad del contenido (textos)
     */
    private void animateContentVisibility() {
        // Animar opacidad del logo
        FadeTransition logoFade = new FadeTransition(ANIMATION_DURATION, imgLogoCia);
        logoFade.setToValue(isSidebarExpanded ? 1.0 : 0.0);
        
        // Animar visibilidad de los textos de los botones
        animateButtonTexts(btnDashboard);
        animateButtonTexts(btnVentas);
        animateButtonTexts(btnCliente);
        animateButtonTexts(btnProductos);
        animateButtonTexts(btnReportes);
        animateButtonTexts(btnSalir);
        
        // Ocultar/mostrar flechas de sub-menú
        animateArrows();
        
        logoFade.play();
    }

    /**
     * Animar el texto de un botón
     */
    private void animateButtonTexts(Button button) {
        if (isSidebarExpanded) {
            // Mostrar texto
            PauseTransition pause = new PauseTransition(Duration.millis(150));
            pause.setOnFinished(e -> button.setText(getButtonOriginalText(button)));
            pause.play();
        } else {
            // Ocultar texto inmediatamente
            button.setText("");
        }
    }

    /**
     * Obtener el texto original de un botón
     */
    private String getButtonOriginalText(Button button) {
        if (button == btnDashboard) return "Dashboard";
        if (button == btnVentas) return "Ventas";
        if (button == btnCliente) return "Clientes";
        if (button == btnProductos) return "Productos";
        if (button == btnReportes) return "Reportes";
        if (button == btnSalir) return "SALIR";
        return "";
    }

    /**
     * Animar las flechas de los sub-menús
     */
    private void animateArrows() {
        animateLabelOpacity(lblVentasArrow);
        animateLabelOpacity(lblClientesArrow);
        animateLabelOpacity(lblProductosArrow);
    }

    /**
     * Animar opacidad de un label
     */
    private void animateLabelOpacity(Label label) {
        FadeTransition fade = new FadeTransition(ANIMATION_DURATION, label);
        fade.setToValue(isSidebarExpanded ? 1.0 : 0.0);
        fade.play();
    }

    /**
     * Animar el botón toggle
     */
    private void animateToggleButton() {
        // Rotación sutil del icono
        RotateTransition rotate = new RotateTransition(ANIMATION_DURATION, btnToggleMenu);
        rotate.setByAngle(isSidebarExpanded ? 0 : 180);
        rotate.play();
        
        // Cambiar el símbolo si lo deseas
        // btnToggleMenu.setText(isSidebarExpanded ? "☰" : "→");
    }

    /**
     * Cerrar todos los sub-menús
     */
    private void closeAllSubmenus() {
        closeSubmenu(ventasSubmenu, lblVentasArrow);
        closeSubmenu(clientesSubmenu, lblClientesArrow);
        closeSubmenu(productosSubmenu, lblProductosArrow);
    }

    /**
     * Cerrar un sub-menú específico
     */
    private void closeSubmenu(VBox submenu, Label arrow) {
        if (submenu.isVisible()) {
            animateSubmenu(submenu, arrow, false);
        }
    }

    /**
     * Alternar sub-menú de Ventas
     */
    @FXML
    public void toggleVentasSubmenu() {
        if (!isSidebarExpanded) return; // No permitir abrir sub-menús si está colapsado
        
        boolean isCurrentlyVisible = ventasSubmenu.isVisible();
        
        // Cerrar otros sub-menús
        closeSubmenu(clientesSubmenu, lblClientesArrow);
        closeSubmenu(productosSubmenu, lblProductosArrow);
        
        // Toggle del sub-menú actual
        animateSubmenu(ventasSubmenu, lblVentasArrow, !isCurrentlyVisible);
    }

    /**
     * Alternar sub-menú de Clientes
     */
    @FXML
    public void toggleClientesSubmenu() {
        if (!isSidebarExpanded) return;
        
        boolean isCurrentlyVisible = clientesSubmenu.isVisible();
        
        // Cerrar otros sub-menús
        closeSubmenu(ventasSubmenu, lblVentasArrow);
        closeSubmenu(productosSubmenu, lblProductosArrow);
        
        // Toggle del sub-menú actual
        animateSubmenu(clientesSubmenu, lblClientesArrow, !isCurrentlyVisible);
    }

    /**
     * Alternar sub-menú de Productos
     */
    @FXML
    public void toggleProductosSubmenu() {
        if (!isSidebarExpanded) return;
        
        boolean isCurrentlyVisible = productosSubmenu.isVisible();
        
        // Cerrar otros sub-menús
        closeSubmenu(ventasSubmenu, lblVentasArrow);
        closeSubmenu(clientesSubmenu, lblClientesArrow);
        
        // Toggle del sub-menú actual
        animateSubmenu(productosSubmenu, lblProductosArrow, !isCurrentlyVisible);
    }

    /**
     * Animar apertura/cierre de un sub-menú
     */
    private void animateSubmenu(VBox submenu, Label arrow, boolean show) {
        if (show) {
            // Mostrar sub-menú
            submenu.setVisible(true);
            submenu.setManaged(true);
            
            // Animación de altura
            submenu.setMaxHeight(0);
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(submenu.maxHeightProperty(), 0)),
                new KeyFrame(ANIMATION_DURATION, new KeyValue(submenu.maxHeightProperty(), 200, Interpolator.EASE_BOTH))
            );
            
            // Animación de opacidad
            FadeTransition fade = new FadeTransition(ANIMATION_DURATION, submenu);
            fade.setFromValue(0);
            fade.setToValue(1);
            
            // Rotar flecha
            RotateTransition rotate = new RotateTransition(ANIMATION_DURATION, arrow);
            rotate.setToAngle(90);
            
            ParallelTransition parallel = new ParallelTransition(timeline, fade, rotate);
            parallel.play();
            
        } else {
            // Ocultar sub-menú
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(submenu.maxHeightProperty(), submenu.getHeight())),
                new KeyFrame(ANIMATION_DURATION, new KeyValue(submenu.maxHeightProperty(), 0, Interpolator.EASE_BOTH))
            );
            
            FadeTransition fade = new FadeTransition(ANIMATION_DURATION, submenu);
            fade.setFromValue(1);
            fade.setToValue(0);
            
            // Rotar flecha
            RotateTransition rotate = new RotateTransition(ANIMATION_DURATION, arrow);
            rotate.setToAngle(0);
            
            ParallelTransition parallel = new ParallelTransition(timeline, fade, rotate);
            parallel.setOnFinished(e -> {
                submenu.setVisible(false);
                submenu.setManaged(false);
            });
            parallel.play();
        }
    }

    // ===== MÉTODOS DE NAVEGACIÓN =====
    
    @FXML
    public void ingresarDashboard() {
        setActiveButton(btnDashboard);
        System.out.println("Navegando a Dashboard");
        // Aquí cargarías la vista correspondiente en el TabPane
    }

    @FXML
    public void ingresarFactura() {
        System.out.println("Navegando a Factura");
        // Cargar vista de factura
    }

    @FXML
    public void ingresarProforma() {
        System.out.println("Navegando a Proforma");
        // Cargar vista de proforma
    }

    @FXML
    public void ingresarNuevoCliente() {
        System.out.println("Navegando a Nuevo Cliente");
        // Cargar vista de nuevo cliente
    }

    @FXML
    public void ingresarListaClientes() {
        System.out.println("Navegando a Lista de Clientes");
        // Cargar vista de lista de clientes
    }

    @FXML
    public void ingresarNuevoProducto() throws IOException {
    	if (this.tabArticulo == null) {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/Arinda1.fxml"));
            Parent  ap = loader.load();

            ImageView icono = new ImageView(getClass().getResource("/com/robin/pos/imagenes/producto.png").toString());
            icono.setFitWidth(16);
            icono.setFitHeight(16);
            tabArticulo = new Tab("Articulo", ap);
            tabArticulo.setGraphic(icono);
            tabArticulo.setClosable(true);
            tabArticulo.setOnClosed(e -> tabArticulo = null);

            this.tabPane.getTabs().add(tabArticulo);
            this.tabPane.getSelectionModel().select(tabArticulo);
        } else {
            // Si el tab ya existe, solo selecciónalo
            this.tabPane.getSelectionModel().select(tabArticulo);
        }
    }

    @FXML
    public void ingresarCatalogo() {
        System.out.println("Navegando a Catálogo");
        // Cargar vista de catálogo
    }

    @FXML
    public void ingresarReporte() {
        setActiveButton(btnReportes);
        System.out.println("Navegando a Reportes");
        // Cargar vista de reportes
    }

    @FXML
    public void salirSistema() {
    	if (Mensaje.confirmacion(null,"Confirmar","¿Está seguro de salir del sistema?").get() != ButtonType.CANCEL) {
    		this.btnSalir.getScene().getWindow().hide();
    	}
    }

    /**
     * Establecer un botón como activo
     */
    private void setActiveButton(Button activeButton) {
        // Remover clase activa de todos los botones
        btnDashboard.getStyleClass().remove("menu-button-active");
        btnVentas.getStyleClass().remove("menu-button-active");
        btnCliente.getStyleClass().remove("menu-button-active");
        btnProductos.getStyleClass().remove("menu-button-active");
        btnReportes.getStyleClass().remove("menu-button-active");
        
        // Agregar clase activa al botón seleccionado
        if (!activeButton.getStyleClass().contains("menu-button-active")) {
            activeButton.getStyleClass().add("menu-button-active");
        }
    }
}