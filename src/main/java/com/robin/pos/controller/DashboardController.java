package com.robin.pos.controller;

import com.robin.pos.MainApp;
import com.robin.pos.util.Mensaje;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    // ===== COMPONENTES ORIGINALES =====
    @FXML
    private Button btn;

    @FXML
    private Button btnCliente;

    @FXML
    private Button btnDashboard;

    @FXML
    private Button btnProductos;

    @FXML
    private Button btnReportes;

    @FXML
    private Button btnVentas;

    @FXML
    private ImageView imgLogoCia;

    @FXML
    private TextField txtBuscar;

    @FXML
    private TabPane tabPane;
    
    @FXML
    private Button btnSalir;

    // ===== NUEVOS COMPONENTES PARA MENÚ MEJORADO =====
    @FXML
    private VBox sidebar;
    
    @FXML
    private VBox menuContainer;
    
    @FXML
    private Button btnToggleMenu;
    
    // === SUB-MENÚS ===
    @FXML
    private VBox ventasSubmenu;
    
    @FXML
    private VBox clientesSubmenu;
    
    @FXML
    private VBox productosSubmenu;
    
    // === BOTONES SUB-MENÚ VENTAS ===
    @FXML
    private Button btnFactura;
    
    @FXML
    private Button btnProforma;
    
    // === BOTONES SUB-MENÚ CLIENTES ===
    @FXML
    private Button btnNuevoCliente;
    
    @FXML
    private Button btnListaClientes;
    
    // === BOTONES SUB-MENÚ PRODUCTOS ===
    @FXML
    private Button btnNuevoProducto;
    
    @FXML
    private Button btnCatalogo;
    
    // === FLECHAS DE SUB-MENÚS ===
    @FXML
    private Label lblVentasArrow;
    
    @FXML
    private Label lblClientesArrow;
    
    @FXML
    private Label lblProductosArrow;
    
    @FXML
    private Label lblUsuario;

    // ===== TABS ORIGINALES =====
    private Tab tabVenta;
    private Tab tabCliente;
    private Tab tabProducto;
    private Tab tabReporte;
    
    // ===== NUEVOS TABS PARA SUB-MENÚS =====
    private Tab tabFactura;
    private Tab tabProforma;
    private Tab tabNuevoCliente;
    private Tab tabListaClientes;
    private Tab tabNuevoProducto;
    private Tab tabCatalogo;
    
    // ===== VARIABLES DE ESTADO PARA SIDEBAR =====
    private boolean isSidebarCollapsed = false;
    private static final double SIDEBAR_WIDTH_EXPANDED = 220.0;
    private static final double SIDEBAR_WIDTH_COLLAPSED = 60.0;
    private static final Duration ANIMATION_DURATION = Duration.millis(300);

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Dashboard inicializado correctamente");
        
        // Configurar el estado inicial de los botones usando CSS classes
        setActiveButton(btnDashboard);
        
        // Configurar nombre de usuario
        if (lblUsuario != null) {
            lblUsuario.setText("Admin");
        }
    }

    // ========================================
    // MÉTODOS ORIGINALES DE NAVEGACIÓN
    // ========================================

    @FXML
    void ingresarDashboard(ActionEvent event) {
        this.setActiveButton((Button) event.getSource());
        closeAllSubmenus();
    }

    @FXML
    void ingreasarVenta(ActionEvent event) throws IOException {
        // Si el sidebar está colapsado, solo expandimos el sub-menú
        if (!isSidebarCollapsed) {
            toggleVentasSubmenu();
        }
        // Mantener la funcionalidad original de abrir tab
        this.setActiveButton((Button) event.getSource());
    }

    @FXML
    void ingresarCliente(ActionEvent event) throws IOException {
        // Si el sidebar está colapsado, solo expandimos el sub-menú
        if (!isSidebarCollapsed) {
            toggleClientesSubmenu();
        }
        // Mantener la funcionalidad original
        this.setActiveButton((Button) event.getSource());
    }

    @FXML
    void ingresarProducto(ActionEvent event) {
        // Si el sidebar está colapsado, solo expandimos el sub-menú
        if (!isSidebarCollapsed) {
            toggleProductosSubmenu();
        }
        this.setActiveButton((Button) event.getSource());
    }

    @FXML
    void ingresarReporte(ActionEvent event) {
        this.setActiveButton((Button) event.getSource());
        closeAllSubmenus();
    }
    
    @FXML
    void salirSistema(ActionEvent event) {
        if (Mensaje.confirmacion(null, "Confirmar", "¿Está seguro de salir?").get() != ButtonType.CANCEL) {
            this.btnSalir.getScene().getWindow().hide();
        }
    }

    // ========================================
    // NUEVOS MÉTODOS PARA SUB-MENÚS
    // ========================================
    
    // === SUB-MENÚ VENTAS ===
    
    @FXML
    private void toggleVentasSubmenu() {
        if (ventasSubmenu != null && !isSidebarCollapsed) {
            toggleSubmenu(ventasSubmenu, lblVentasArrow);
            // Cerrar otros sub-menús
            if (clientesSubmenu != null) closeSubmenu(clientesSubmenu, lblClientesArrow);
            if (productosSubmenu != null) closeSubmenu(productosSubmenu, lblProductosArrow);
        }
    }
    
    @FXML
    private void ingresarFactura() throws IOException {
        System.out.println("Ingresando a Factura/Boleta");
        setActiveButton(btnFactura);
        
        if (tabFactura == null) {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/Venta.fxml"));
                BorderPane ap = loader.load();
                
                ImageView icono = new ImageView(getClass().getResource("/com/robin/pos/imagenes/carritoCompras32.png").toString());
                icono.setFitWidth(16);
                icono.setFitHeight(16);
                
                tabFactura = new Tab("Factura/Boleta", ap);
                tabFactura.setGraphic(icono);
                tabFactura.setClosable(true);
                tabFactura.setOnClosed(e -> tabFactura = null);
                
                this.tabPane.getTabs().add(tabFactura);
            } catch (Exception e) {
                System.err.println("Error al cargar Factura: " + e.getMessage());
            }
        }
        if (tabFactura != null) {
            this.tabPane.getSelectionModel().select(tabFactura);
        }
    }
    
    @FXML
    private void ingresarProforma() {
        System.out.println("Ingresando a Proforma");
        setActiveButton(btnProforma);
        
        // Aquí puedes implementar la carga de tu vista de Proforma
        // Similar a ingresarFactura()
    }
    
    // === SUB-MENÚ CLIENTES ===
    
    @FXML
    private void toggleClientesSubmenu() {
        if (clientesSubmenu != null && !isSidebarCollapsed) {
            toggleSubmenu(clientesSubmenu, lblClientesArrow);
            // Cerrar otros sub-menús
            if (ventasSubmenu != null) closeSubmenu(ventasSubmenu, lblVentasArrow);
            if (productosSubmenu != null) closeSubmenu(productosSubmenu, lblProductosArrow);
        }
    }
    
    @FXML
    private void ingresarNuevoCliente() {
        System.out.println("Ingresando a Nuevo Cliente");
        setActiveButton(btnNuevoCliente);
        
        // Aquí puedes implementar la carga de tu vista de Nuevo Cliente
    }
    
    @FXML
    private void ingresarListaClientes() throws IOException {
        System.out.println("Ingresando a Lista de Clientes");
        setActiveButton(btnListaClientes);
        
        if (tabListaClientes == null) {
            try {
                FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("/com/robin/pos/fxml/ListaCliente.fxml"));
                BorderPane ap = loader.load();
                
                ImageView icono = new ImageView(getClass().getResource("/com/robin/pos/imagenes/listaClientes.png").toString());
                icono.setFitWidth(16);
                icono.setFitHeight(16);
                
                tabListaClientes = new Tab("Lista Clientes", ap);
                tabListaClientes.setGraphic(icono);
                tabListaClientes.setClosable(true);
                tabListaClientes.setOnClosed(e -> tabListaClientes = null);
                
                this.tabPane.getTabs().add(tabListaClientes);
            } catch (Exception e) {
                System.err.println("Error al cargar Lista Clientes: " + e.getMessage());
            }
        }
        if (tabListaClientes != null) {
            this.tabPane.getSelectionModel().select(tabListaClientes);
        }
    }
    
    // === SUB-MENÚ PRODUCTOS ===
    
    @FXML
    private void toggleProductosSubmenu() {
        if (productosSubmenu != null && !isSidebarCollapsed) {
            toggleSubmenu(productosSubmenu, lblProductosArrow);
            // Cerrar otros sub-menús
            if (ventasSubmenu != null) closeSubmenu(ventasSubmenu, lblVentasArrow);
            if (clientesSubmenu != null) closeSubmenu(clientesSubmenu, lblClientesArrow);
        }
    }
    
    @FXML
    private void ingresarNuevoProducto() {
        System.out.println("Ingresando a Nuevo Producto");
        setActiveButton(btnNuevoProducto);
        
        // Aquí puedes implementar la carga de tu vista de Nuevo Producto
    }
    
    @FXML
    private void ingresarCatalogo() {
        System.out.println("Ingresando a Catálogo");
        setActiveButton(btnCatalogo);
        
        // Aquí puedes implementar la carga de tu vista de Catálogo
    }

    // ========================================
    // MÉTODOS PARA COLAPSAR/EXPANDIR SIDEBAR
    // ========================================
    
    @FXML
    private void toggleSidebar() {
        if (sidebar == null) {
            System.err.println("Error: sidebar es null. Verifica el fx:id en el FXML");
            return;
        }
        
        if (isSidebarCollapsed) {
            expandSidebar();
        } else {
            collapseSidebar();
        }
        isSidebarCollapsed = !isSidebarCollapsed;
    }
    
    private void collapseSidebar() {
        // Cerrar todos los sub-menús antes de colapsar
        closeAllSubmenus();
        
        // Animación de ancho del sidebar
        Timeline timeline = new Timeline(
            new KeyFrame(ANIMATION_DURATION,
                new KeyValue(sidebar.prefWidthProperty(), SIDEBAR_WIDTH_COLLAPSED, Interpolator.EASE_BOTH)
            )
        );
        
        // Ocultar textos y flechas con fade
        if (menuContainer != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), menuContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.3);
            
            fadeOut.setOnFinished(e -> {
                if (imgLogoCia != null) imgLogoCia.setVisible(false);
                if (lblVentasArrow != null) lblVentasArrow.setVisible(false);
                if (lblClientesArrow != null) lblClientesArrow.setVisible(false);
                if (lblProductosArrow != null) lblProductosArrow.setVisible(false);
                
                // Cambiar el texto del botón a solo icono
                if (btnToggleMenu != null) btnToggleMenu.setText("☰");
            });
            
            fadeOut.play();
        }
        
        timeline.play();
    }
    
    private void expandSidebar() {
        // Animación de ancho del sidebar
        Timeline timeline = new Timeline(
            new KeyFrame(ANIMATION_DURATION,
                new KeyValue(sidebar.prefWidthProperty(), SIDEBAR_WIDTH_EXPANDED, Interpolator.EASE_BOTH)
            )
        );
        
        timeline.setOnFinished(e -> {
            if (imgLogoCia != null) imgLogoCia.setVisible(true);
            if (lblVentasArrow != null) lblVentasArrow.setVisible(true);
            if (lblClientesArrow != null) lblClientesArrow.setVisible(true);
            if (lblProductosArrow != null) lblProductosArrow.setVisible(true);
            
            // Fade in de los textos
            if (menuContainer != null) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), menuContainer);
                fadeIn.setFromValue(0.3);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
            
            if (btnToggleMenu != null) btnToggleMenu.setText("✕");
        });
        
        timeline.play();
    }

    // ========================================
    // MÉTODOS AUXILIARES PARA SUB-MENÚS
    // ========================================
    
    private void toggleSubmenu(VBox submenu, Label arrow) {
        if (submenu.isVisible()) {
            closeSubmenu(submenu, arrow);
        } else {
            openSubmenu(submenu, arrow);
        }
    }
    
    private void openSubmenu(VBox submenu, Label arrow) {
        submenu.setVisible(true);
        submenu.setManaged(true);
        
        // Animación de altura
        submenu.setMaxHeight(0);
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(250),
                new KeyValue(submenu.maxHeightProperty(), 200, Interpolator.EASE_OUT)
            )
        );
        
        // Animación de opacidad
        FadeTransition fade = new FadeTransition(Duration.millis(250), submenu);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        
        // Rotar flecha
        if (arrow != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(250), arrow);
            rotate.setToAngle(90);
            rotate.play();
        }
        
        timeline.play();
        fade.play();
    }
    
    private void closeSubmenu(VBox submenu, Label arrow) {
        if (!submenu.isVisible()) return;
        
        // Animación de altura
        Timeline timeline = new Timeline(
            new KeyFrame(Duration.millis(200),
                new KeyValue(submenu.maxHeightProperty(), 0, Interpolator.EASE_IN)
            )
        );
        
        // Animación de opacidad
        FadeTransition fade = new FadeTransition(Duration.millis(200), submenu);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        
        // Rotar flecha de vuelta
        if (arrow != null) {
            RotateTransition rotate = new RotateTransition(Duration.millis(200), arrow);
            rotate.setToAngle(0);
            rotate.play();
        }
        
        timeline.setOnFinished(e -> {
            submenu.setVisible(false);
            submenu.setManaged(false);
        });
        
        timeline.play();
        fade.play();
    }
    
    private void closeAllSubmenus() {
        if (ventasSubmenu != null && lblVentasArrow != null) {
            closeSubmenu(ventasSubmenu, lblVentasArrow);
        }
        if (clientesSubmenu != null && lblClientesArrow != null) {
            closeSubmenu(clientesSubmenu, lblClientesArrow);
        }
        if (productosSubmenu != null && lblProductosArrow != null) {
            closeSubmenu(productosSubmenu, lblProductosArrow);
        }
    }

    // ========================================
    // MÉTODO MEJORADO PARA BOTÓN ACTIVO
    // ========================================
    
    private void setActiveButton(Button activeButton) {
        // Remover clase activa de todos los botones principales
        if (btnDashboard != null) btnDashboard.getStyleClass().remove("menu-button-active");
        if (btnVentas != null) btnVentas.getStyleClass().remove("menu-button-active");
        if (btnCliente != null) btnCliente.getStyleClass().remove("menu-button-active");
        if (btnProductos != null) btnProductos.getStyleClass().remove("menu-button-active");
        if (btnReportes != null) btnReportes.getStyleClass().remove("menu-button-active");
        
        // Remover clase activa de todos los botones de sub-menú
        if (btnFactura != null) btnFactura.getStyleClass().remove("submenu-button-active");
        if (btnProforma != null) btnProforma.getStyleClass().remove("submenu-button-active");
        if (btnNuevoCliente != null) btnNuevoCliente.getStyleClass().remove("submenu-button-active");
        if (btnListaClientes != null) btnListaClientes.getStyleClass().remove("submenu-button-active");
        if (btnNuevoProducto != null) btnNuevoProducto.getStyleClass().remove("submenu-button-active");
        if (btnCatalogo != null) btnCatalogo.getStyleClass().remove("submenu-button-active");
        
        // Agregar clase activa al botón seleccionado
        if (activeButton != null) {
            // Verificar si es un botón de sub-menú
            boolean isSubmenuButton = (activeButton == btnFactura || activeButton == btnProforma ||
                                      activeButton == btnNuevoCliente || activeButton == btnListaClientes ||
                                      activeButton == btnNuevoProducto || activeButton == btnCatalogo);
            
            String activeClass = isSubmenuButton ? "submenu-button-active" : "menu-button-active";
            
            if (!activeButton.getStyleClass().contains(activeClass)) {
                activeButton.getStyleClass().add(activeClass);
            }
        }
    }
}