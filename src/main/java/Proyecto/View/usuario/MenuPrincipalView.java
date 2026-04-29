package Proyecto.View.Usuario;

import Proyecto.Model.Cliente;
import Proyecto.View.Producto.ProductoView;
import Proyecto.services.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class MenuPrincipalView {

    private Cliente cliente;
    private BorderPane mainLayout;
    private StackPane contentPanel;
    private Stage stage;

    // Services
    private ProductoServices productoServices;
    private InventarioServices inventarioServices;
    private DocumentoServices documentoServices;

    // Botón activo actual
    private Button btnActivo;

    public MenuPrincipalView(Cliente cliente, Stage stage) {
        this.cliente = cliente;
        this.stage = stage;
        this.productoServices = new ProductoServices();
        this.inventarioServices = new InventarioServices();
        this.documentoServices = new DocumentoServices();
        initComponents();
        mostrarDashboard();
        cargarDatosDashboard();
    }

    public MenuPrincipalView(Stage stage) {
        this(null, stage);
    }

    private void initComponents() {
        stage.setTitle("TechZone - Sistema de Gestión");
        stage.setWidth(1200);
        stage.setHeight(750);

        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F0F2F5;");

        // Barra superior
        mainLayout.setTop(crearBarraSuperior());

        // Menú lateral
        mainLayout.setLeft(crearMenuLateral());

        // Panel central
        contentPanel = new StackPane();
        contentPanel.setStyle("-fx-background-color: white;");
        contentPanel.setPadding(new Insets(20));
        mainLayout.setCenter(contentPanel);

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.show();
    }

    // ── Barra superior ───────────────────────────────────────────────────────
    private HBox crearBarraSuperior() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(60);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setStyle("-fx-background-color: #0A1933;");

        Label lblLogo = new Label("TECHZONE");
        lblLogo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblLogo.setTextFill(Color.web("#00C8FF"));

        // Espaciador
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String nombreUsuario = (cliente != null)
                ? cliente.getNombre() + " " + cliente.getApellido()
                : "Invitado";

        Label lblUser = new Label("Usuario: " + nombreUsuario);
        lblUser.setTextFill(Color.WHITE);
        lblUser.setFont(Font.font("Arial", 12));

        Button btnLogout = new Button("Cerrar Sesión");
        btnLogout.setStyle(
                "-fx-background-color: #C83C3C;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;");
        btnLogout.setOnAction(e -> cerrarSesion());

        HBox userPanel = new HBox(15, lblUser, btnLogout);
        userPanel.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().addAll(lblLogo, spacer, userPanel);
        return topBar;
    }

    // ── Menú lateral ─────────────────────────────────────────────────────────
    private VBox crearMenuLateral() {
        VBox menuPanel = new VBox(5);
        menuPanel.setPrefWidth(250);
        menuPanel.setPadding(new Insets(20, 10, 20, 10));
        menuPanel.setStyle("-fx-background-color: #1E2840;");

        String[][] items = {
                { "🏠 Dashboard", "dashboard" },
                { "📦 Productos", "productos" },
                { "🛒 Carrito", "carrito" },
                { "📄 Mis Compras", "compras" },
                { "📊 Inventario", "inventario" },
                { "👤 Mi Perfil", "perfil" },
                { "❌ Salir", "exit" }
        };

        for (String[] item : items) {
            Button btn = crearBotonMenu(item[0], item[1]);
            menuPanel.getChildren().add(btn);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        menuPanel.getChildren().add(spacer);

        return menuPanel;
    }

    private Button crearBotonMenu(String texto, String accion) {
        Button btn = new Button(texto);
        btn.setPrefSize(200, 45);
        btn.setMaxWidth(200);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(10, 15, 10, 15));
        btn.setStyle(
                "-fx-background-color: #32415585;" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;");

        btn.setOnMouseEntered(e -> {
            if (btn != btnActivo)
                btn.setStyle("-fx-background-color: #3A4F6A; -fx-border-width: 0; -fx-cursor: hand;");
        });
        btn.setOnMouseExited(e -> {
            if (btn != btnActivo)
                btn.setStyle("-fx-background-color: #32415585; -fx-border-width: 0; -fx-cursor: hand;");
        });

        btn.setOnAction(e -> {
            resaltarBoton(btn);
            cambiarPanel(accion);
        });

        return btn;
    }

    private void resaltarBoton(Button btnSeleccionado) {
        if (btnActivo != null)
            btnActivo.setStyle("-fx-background-color: #32415585; -fx-border-width: 0; -fx-cursor: hand;");
        btnActivo = btnSeleccionado;
        btnActivo.setStyle("-fx-background-color: #0096C8; -fx-border-width: 0; -fx-cursor: hand;");
    }

    // ── Navegación entre paneles ─────────────────────────────────────────────
    private void cambiarPanel(String accion) {
        contentPanel.getChildren().clear();
        switch (accion) {
            case "dashboard" -> mostrarDashboard();
            case "productos" -> mostrarProductos();
            case "carrito" -> mostrarCarrito();
            case "compras" -> mostrarCompras();
            case "inventario" -> mostrarInventario();
            case "perfil" -> mostrarPerfil();
            case "exit" -> cerrarSesion();
        }
    }

    // ── Dashboard ────────────────────────────────────────────────────────────
    private void mostrarDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setAlignment(Pos.TOP_CENTER);
        dashboard.setFillWidth(true);

        // Tarjetas estadísticas
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER);

        statsRow.getChildren().addAll(
                crearTarjeta("Productos", "0", "Total disponibles"),
                crearTarjeta("Stock Bajo", "0", "Alertas"),
                crearTarjeta("Compras", "0", "Mis compras"),
                crearTarjeta("Total Gastado", "$0", "Historial"));

        // Barra de progreso
        VBox progressBox = new VBox(8);
        progressBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 15;");
        progressBox.setMaxWidth(Double.MAX_VALUE);

        Label lblProgress = new Label("Enero: 50% SUBIDA");
        lblProgress.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ProgressBar progressBar = new ProgressBar(0.50);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #00C8FF;");

        progressBox.getChildren().addAll(new Label("Progreso del Mes"), lblProgress, progressBar);

        // Gráfico de barras con Canvas
        VBox chartBox = new VBox(8);
        chartBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-padding: 15;");
        chartBox.setMaxWidth(Double.MAX_VALUE);

        Label lblChart = new Label("Estadísticas de Ventas");
        lblChart.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        Canvas canvas = crearGraficoBarras(700, 180);
        chartBox.getChildren().addAll(lblChart, canvas);

        dashboard.getChildren().addAll(statsRow, progressBox, chartBox);
        contentPanel.getChildren().add(dashboard);

        actualizarDashboard();
    }

    private Canvas crearGraficoBarras(double ancho, double alto) {
        Canvas canvas = new Canvas(ancho, alto);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        String[] meses = { "Ene", "Feb", "Mar", "Abr", "May" };
        int[] valores = { 25, 40, 35, 50, 45 };

        double margenIzq = 40;
        double margenInf = 30;
        double areaAncho = ancho - margenIzq - 20;
        double areaAlto = alto - margenInf - 10;
        double barW = areaAncho / meses.length - 10;

        // Ejes
        gc.setStroke(Color.BLACK);
        gc.strokeLine(margenIzq, 10, margenIzq, alto - margenInf);
        gc.strokeLine(margenIzq, alto - margenInf, ancho - 10, alto - margenInf);

        for (int i = 0; i < meses.length; i++) {
            double barH = valores[i] * areaAlto / 100.0;
            double x = margenIzq + i * (barW + 10);
            double y = alto - margenInf - barH;

            gc.setFill(Color.web("#00C8FF"));
            gc.fillRect(x, y, barW, barH);
            gc.setStroke(Color.web("#007FA0"));
            gc.strokeRect(x, y, barW, barH);

            gc.setFill(Color.BLACK);
            gc.fillText(meses[i] + " (" + valores[i] + "%)", x, alto - 8);
        }
        return canvas;
    }

    private void actualizarDashboard() {
        if (productoServices != null && inventarioServices != null) {
            int totalProductos = productoServices.obtenerTodosLosProductos().size();
            int stockBajo = inventarioServices.obtenerProductosConStockBajo().size();
            System.out.println("Dashboard actualizado:");
            System.out.println("  - Total Productos: " + totalProductos);
            System.out.println("  - Productos con stock bajo: " + stockBajo);
        }
    }

    private VBox crearTarjeta(String titulo, String valor, String subtitulo) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPadding(new Insets(15, 10, 15, 10));
        card.setStyle(
                "-fx-background-color: #F5F5FA;" +
                        "-fx-border-color: #C8C8C8;" +
                        "-fx-border-width: 1;");

        Label lblT = new Label(titulo);
        lblT.setTextFill(Color.GRAY);
        lblT.setFont(Font.font("Arial", 12));

        Label lblV = new Label(valor);
        lblV.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblV.setTextFill(Color.web("#0A1933"));

        Label lblS = new Label(subtitulo);
        lblS.setTextFill(Color.GRAY);
        lblS.setFont(Font.font("Arial", 10));

        card.getChildren().addAll(lblT, lblV, lblS);
        return card;
    }

    // ── Productos ────────────────────────────────────────────────────────────
    private void mostrarProductos() {
        if (cliente != null) {
            ProductoView productoView = new ProductoView(cliente.getId());
            contentPanel.getChildren().add(productoView.getRoot());
        } else {
            contentPanel.getChildren().add(
                    centeredLabel("Debe iniciar sesión para ver productos"));
        }
    }

    // ── Carrito ──────────────────────────────────────────────────────────────
    private void mostrarCarrito() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(10));

        Label lblTitulo = new Label("Mi Carrito de Compras");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        TextArea txtCarrito = new TextArea(
                "No hay productos en el carrito.\n\n" +
                        "Para agregar productos, ve a la sección 'Productos' y haz clic en 'Comprar'.");
        txtCarrito.setEditable(false);
        txtCarrito.setFont(Font.font("Monospaced", 12));
        VBox.setVgrow(txtCarrito, Priority.ALWAYS);

        Button btnComprar = crearBotonAccion("Finalizar Compra", "#00C8FF");
        btnComprar.setOnAction(e -> new Alert(Alert.AlertType.INFORMATION,
                "Funcionalidad de compra en desarrollo").showAndWait());

        panel.getChildren().addAll(lblTitulo, txtCarrito, btnComprar);
        contentPanel.getChildren().add(panel);
    }

    // ── Compras ──────────────────────────────────────────────────────────────
    private void mostrarCompras() {
        if (cliente != null && documentoServices != null) {
            VBox panel = new VBox(15);
            panel.setPadding(new Insets(10));

            Label lblTitulo = new Label("Historial de Compras");
            lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            TextArea txtCompras = new TextArea(
                    documentoServices.generarReporteVentasCliente(cliente.getId()));
            txtCompras.setEditable(false);
            txtCompras.setFont(Font.font("Monospaced", 12));
            VBox.setVgrow(txtCompras, Priority.ALWAYS);

            panel.getChildren().addAll(lblTitulo, new ScrollPane(txtCompras));
            contentPanel.getChildren().add(panel);
        } else {
            contentPanel.getChildren().add(
                    centeredLabel("No hay historial de compras disponible"));
        }
    }

    // ── Inventario ───────────────────────────────────────────────────────────
    private void mostrarInventario() {
        if (inventarioServices != null) {
            VBox panel = new VBox(15);
            panel.setPadding(new Insets(10));

            Label lblTitulo = new Label("Alertas de Inventario");
            lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));

            TextArea txtAlertas = new TextArea(
                    inventarioServices.generarAlertaInventario());
            txtAlertas.setEditable(false);
            txtAlertas.setFont(Font.font("Monospaced", 12));
            VBox.setVgrow(txtAlertas, Priority.ALWAYS);

            panel.getChildren().addAll(lblTitulo, new ScrollPane(txtAlertas));
            contentPanel.getChildren().add(panel);
        } else {
            contentPanel.getChildren().add(
                    centeredLabel("No hay alertas de inventario"));
        }
    }

    // ── Perfil ───────────────────────────────────────────────────────────────
    private void mostrarPerfil() {
        if (cliente != null) {
            VBox panel = new VBox(15);
            panel.setAlignment(Pos.CENTER);
            panel.setPadding(new Insets(30));

            Label lblTitulo = new Label("Mi Perfil");
            lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));

            String direccion = cliente.getDireccion() != null ? cliente.getDireccion() : "No registrada";
            String telefono = cliente.getTelefono() != null ? cliente.getTelefono() : "No registrado";

            GridPane grid = new GridPane();
            grid.setHgap(15);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));

            agregarFila(grid, 0, "Nombre:", cliente.getNombre() + " " + cliente.getApellido());
            agregarFila(grid, 1, "Email:", cliente.getEmail());
            agregarFila(grid, 2, "Teléfono:", telefono);
            agregarFila(grid, 3, "Dirección:", direccion);

            panel.getChildren().addAll(lblTitulo, grid);
            contentPanel.getChildren().add(panel);
        } else {
            contentPanel.getChildren().add(
                    centeredLabel("Información de perfil no disponible"));
        }
    }

    private void agregarFila(GridPane grid, int fila, String etiqueta, String valor) {
        Label lbl = new Label(etiqueta);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        Label val = new Label(valor);
        val.setFont(Font.font("Arial", 14));
        grid.add(lbl, 0, fila);
        grid.add(val, 1, fila);
    }

    // ── Cerrar sesión ────────────────────────────────────────────────────────
    private void cerrarSesion() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Está seguro que desea cerrar sesión?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Cerrar Sesión");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                Stage loginStage = new Stage();
                new LoginView(loginStage);
                stage.close();
            }
        });
    }

    private void cargarDatosDashboard() {
        if (productoServices != null && inventarioServices != null) {
            int totalProductos = productoServices.obtenerTodosLosProductos().size();
            int stockBajo = inventarioServices.obtenerProductosConStockBajo().size();
            System.out.println("=== DASHBOARD DATA ===");
            System.out.println("Total de productos activos: " + totalProductos);
            System.out.println("Productos con stock bajo: " + stockBajo);
        }
    }

    // ── Utilidades ───────────────────────────────────────────────────────────
    private Label centeredLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", 14));
        StackPane.setAlignment(lbl, Pos.CENTER);
        return lbl;
    }

    private Button crearBotonAccion(String texto, String color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-border-width: 0; -fx-cursor: hand;");
        return btn;
    }
}