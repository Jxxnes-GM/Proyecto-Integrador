package Proyecto.View.Usuario;

import Proyecto.Model.Cliente;
import Proyecto.View.Admin.AdminDashboardView;
import Proyecto.View.Carrito.CarritoView;
import Proyecto.View.Documento.CotizacionView;
import Proyecto.View.Documento.RegistroCompraView;
import Proyecto.View.Documento.RegistroVentasView;
import Proyecto.View.Documento.VentasPosView;
import Proyecto.View.Inventario.MovimientosView;
import Proyecto.View.Producto.ProductoFormView;
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

import java.util.List;

/**
 *
 * Soporta tres modos según el rol del usuario:
 * - CLIENTE → Dashboard, Productos, Carrito, Mis Compras, Perfil
 * - EMPLEADO → Dashboard, Productos (CRUD), Cotizaciones, Punto de Venta,
 * Inventario, Registro de Ventas
 * - ADMIN → Todo lo anterior + Panel de Administración completo
 *
 * El rol se determina por el campo {@code cargo} del {@code Cliente} pasado al
 * constructor. Si {@code cliente} es {@code null} o no tiene cargo asignado se
 * asume rol "CLIENTE".
 */
public class MenuPrincipalView {

    // ── Estado ────────────────────────────────────────────────────────────────
    private final Cliente cliente;
    private final String rol; // "CLIENTE" | "VENDEDOR" | "CAJERO" | "COMPRADOR" | "BODEGUERO" |
                              // "ADMINISTRADOR"

    private BorderPane mainLayout;
    private StackPane contentPanel;
    private Stage stage;
    private Button btnActivo;

    // Servicios
    private final ProductoServices productoServices;
    private final InventarioServices inventarioServices;
    private final DocumentoServices documentoServices;

    // ── Labels del dashboard (reutilizables) ─────────────────────────────────
    private Label lblDashProductos;
    private Label lblDashStockBajo;
    private Label lblDashCompras;
    private Label lblDashGastado;

    // ── Constructores ─────────────────────────────────────────────────────────
    public MenuPrincipalView(Cliente cliente, Stage stage) {
        this.cliente = cliente;
        this.stage = stage;
        this.productoServices = new ProductoServices();
        this.inventarioServices = new InventarioServices();
        this.documentoServices = new DocumentoServices();
        this.rol = resolverRol(cliente);
        initComponents();
        mostrarDashboard();
        cargarDatosDashboard();
    }

    public MenuPrincipalView(Stage stage) {
        this(null, stage);
    }

    /** Determina el rol del usuario a partir del modelo de cliente. */
    private String resolverRol(Cliente c) {
        if (c == null)
            return "CLIENTE";
        // Si el modelo de Cliente expone un cargo/rol, usarlo aquí.
        // Ejemplo: if (c instanceof Empleado emp) return
        // emp.getCargo().getNombre().toUpperCase();
        return "CLIENTE";
    }

    // ── Inicialización ────────────────────────────────────────────────────────
    private void initComponents() {
        stage.setTitle("TechZone — Sistema de Gestión");
        stage.setWidth(1280);
        stage.setHeight(780);
        stage.setMinWidth(900);
        stage.setMinHeight(600);

        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #F0F2F5;");

        mainLayout.setTop(crearBarraSuperior());
        mainLayout.setLeft(crearMenuLateral());

        contentPanel = new StackPane();
        contentPanel.setStyle("-fx-background-color: white;");
        contentPanel.setPadding(new Insets(20));
        mainLayout.setCenter(contentPanel);

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.show();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BARRA SUPERIOR
    // ═════════════════════════════════════════════════════════════════════════
    private HBox crearBarraSuperior() {
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPrefHeight(62);
        topBar.setPadding(new Insets(0, 20, 0, 20));
        topBar.setStyle("-fx-background-color: #0A1933;");

        // Logo + nombre
        Label lblLogo = new Label("TECHZONE");
        lblLogo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblLogo.setTextFill(Color.web("#00C8FF"));

        Label lblGadgets = new Label(" GADGETS & HOBBIES");
        lblGadgets.setFont(Font.font("Arial", 12));
        lblGadgets.setTextFill(Color.web("#5588AA"));

        // Espaciador
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Info usuario
        String nombreUsuario = (cliente != null)
                ? cliente.getNombre() + " " + cliente.getApellido()
                : "Invitado";

        Label lblRolBadge = new Label(rol);
        lblRolBadge.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        lblRolBadge.setTextFill(Color.web("#0A1933"));
        lblRolBadge.setStyle("-fx-background-color: #00C8FF; -fx-padding: 2 6 2 6; -fx-border-radius: 3;");

        Label lblUser = new Label("  " + nombreUsuario);
        lblUser.setTextFill(Color.WHITE);
        lblUser.setFont(Font.font("Arial", 13));

        Button btnLogout = new Button("Cerrar sesión");
        btnLogout.setStyle(
                "-fx-background-color: #C83C3C; -fx-text-fill: white; " +
                        "-fx-font-weight: bold; -fx-border-width: 0; -fx-cursor: hand; " +
                        "-fx-padding: 6 14 6 14;");
        btnLogout.setOnAction(e -> cerrarSesion());

        HBox userBox = new HBox(10, lblRolBadge, lblUser, btnLogout);
        userBox.setAlignment(Pos.CENTER_RIGHT);

        topBar.getChildren().addAll(lblLogo, lblGadgets, spacer, userBox);
        return topBar;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MENÚ LATERAL (dinámico por rol)
    // ═════════════════════════════════════════════════════════════════════════
    private VBox crearMenuLateral() {
        VBox menuPanel = new VBox(4);
        menuPanel.setPrefWidth(240);
        menuPanel.setPadding(new Insets(18, 10, 18, 10));
        menuPanel.setStyle("-fx-background-color: #1E2840;");

        // Sección: común para todos
        agregarSeccion(menuPanel, "PRINCIPAL", List.of("🏠  Dashboard", "📦  Catálogo"),
                List.of("dashboard", "productos"));

        if (esCliente()) {
            agregarSeccion(menuPanel, "MI CUENTA", List.of("🛒  Mi Carrito", "📄  Mis Compras", "👤  Mi Perfil"),
                    List.of("carrito", "compras", "perfil"));
        }

        if (esEmpleadoOAdmin()) {
            agregarSeccion(menuPanel, "VENTAS",
                    List.of("📝  Cotizaciones", "🖥  Punto de Venta", "📊  Registro Ventas"),
                    List.of("cotizaciones", "pos", "ventas"));
        }

        if (esBodegueroOAdmin()) {
            agregarSeccion(menuPanel, "INVENTARIO", List.of("🔄  Movimientos"), List.of("inventario"));
        }

        if (esCompradorOAdmin()) {
            agregarSeccion(menuPanel, "CATÁLOGO", List.of("➕  Nuevo Producto"), List.of("nuevo_producto"));
        }

        if (esAdmin()) {
            agregarSeccion(menuPanel, "ADMINISTRACIÓN", List.of("⚙  Panel Admin"), List.of("admin"));
        }

        // Salir al fondo
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        menuPanel.getChildren().add(spacer);
        menuPanel.getChildren().add(btnMenu("❌  Salir", "exit"));

        return menuPanel;
    }

    private void agregarSeccion(VBox menuPanel, String titulo, List<String> textos, List<String> acciones) {
        menuPanel.getChildren().add(sectionLabel(titulo));
        for (int i = 0; i < textos.size(); i++) {
            menuPanel.getChildren().add(btnMenu(textos.get(i), acciones.get(i)));
        }
    }

    private Label sectionLabel(String texto) {
        Label lbl = new Label(texto);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        lbl.setTextFill(Color.web("#5588AA"));
        lbl.setPadding(new Insets(12, 0, 4, 8));
        return lbl;
    }

    private Button btnMenu(String texto, String accion) {
        Button btn = new Button(texto);
        btn.setPrefSize(210, 42);
        btn.setMaxWidth(210);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setPadding(new Insets(0, 0, 0, 14));
        estiloBtnNormal(btn);

        btn.setOnMouseEntered(e -> {
            if (btn != btnActivo)
                btn.setStyle(ESTILO_HOVER);
        });
        btn.setOnMouseExited(e -> {
            if (btn != btnActivo)
                estiloBtnNormal(btn);
        });

        btn.setOnAction(e -> {
            resaltarBoton(btn);
            cambiarPanel(accion);
        });

        return btn;
    }

    private static final String ESTILO_NORMAL = "-fx-background-color: transparent; -fx-border-width: 0; -fx-cursor: hand;";
    private static final String ESTILO_HOVER = "-fx-background-color: #3A4F6A;   -fx-border-width: 0; -fx-cursor: hand;";
    private static final String ESTILO_ACTIVO = "-fx-background-color: #0096C8;   -fx-border-width: 0; -fx-cursor: hand;";

    private void estiloBtnNormal(Button b) {
        b.setStyle(ESTILO_NORMAL);
    }

    private void resaltarBoton(Button sel) {
        if (btnActivo != null)
            estiloBtnNormal(btnActivo);
        btnActivo = sel;
        btnActivo.setStyle(ESTILO_ACTIVO);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // NAVEGACIÓN
    // ═════════════════════════════════════════════════════════════════════════
    private void cambiarPanel(String accion) {
        contentPanel.getChildren().clear();
        switch (accion) {
            case "dashboard" -> mostrarDashboard();
            case "productos" -> mostrarProductos();
            case "carrito" -> mostrarCarrito();
            case "compras" -> mostrarCompras();
            case "perfil" -> mostrarPerfil();
            case "cotizaciones" -> mostrarCotizaciones();
            case "pos" -> mostrarPuntoDeVenta();
            case "ventas" -> mostrarRegistroVentas();
            case "inventario" -> mostrarInventario();
            case "nuevo_producto" -> abrirFormularioProducto();
            case "admin" -> mostrarAdminPanel();
            case "exit" -> cerrarSesion();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VISTAS — COMÚN
    // ═════════════════════════════════════════════════════════════════════════
    private void mostrarDashboard() {
        VBox dashboard = new VBox(18);
        dashboard.setFillWidth(true);

        // Bienvenida
        Label lblBienvenida = new Label(
                "Bienvenido, " + (cliente != null ? cliente.getNombre() : "Usuario") + " 👋");
        lblBienvenida.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblBienvenida.setTextFill(Color.web("#0A1933"));

        // KPIs
        lblDashProductos = kpiValor("…");
        lblDashStockBajo = kpiValor("…");
        lblDashCompras = kpiValor("…");
        lblDashGastado = kpiValor("…");

        HBox kpiRow = new HBox(15);
        kpiRow.setAlignment(Pos.CENTER_LEFT);

        if (esCliente()) {
            kpiRow.getChildren().addAll(
                    kpiCard("🛒 En el carrito", lblDashProductos, "#00C8FF"),
                    kpiCard("📄 Mis compras", lblDashCompras, "#1A8A2A"),
                    kpiCard("💰 Total gastado", lblDashGastado, "#0A1933"));
        } else {
            kpiRow.getChildren().addAll(
                    kpiCard("📦 Productos", lblDashProductos, "#00C8FF"),
                    kpiCard("⚠ Stock bajo", lblDashStockBajo, "#C83C3C"),
                    kpiCard("💰 Ventas", lblDashCompras, "#1A8A2A"),
                    kpiCard("📈 Monto total", lblDashGastado, "#0A1933"));
        }

        // Gráfico
        VBox chartBox = new VBox(8);
        chartBox.setPadding(new Insets(15));
        chartBox.setStyle("-fx-border-color: #DCDCDC; -fx-border-width: 1;");

        Label lblChart = new Label("📊  Estadísticas de Ventas — últimos 5 meses");
        lblChart.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        chartBox.getChildren().addAll(lblChart, crearGraficoBarras(750, 170));

        // Barra de progreso del mes
        VBox progressBox = new VBox(8);
        progressBox.setPadding(new Insets(12));
        progressBox.setStyle("-fx-border-color: #DCDCDC; -fx-border-width: 1;");

        Label lblProg = new Label("Meta mensual — Mayo 2025");
        lblProg.setFont(Font.font("Arial", FontWeight.BOLD, 13));

        ProgressBar pb = new ProgressBar(0.64);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(18);
        pb.setStyle("-fx-accent: #00C8FF;");

        Label lblPct = new Label("64% completado");
        lblPct.setFont(Font.font("Arial", 12));
        lblPct.setTextFill(Color.GRAY);

        progressBox.getChildren().addAll(lblProg, pb, lblPct);

        dashboard.getChildren().addAll(lblBienvenida, kpiRow, chartBox, progressBox);
        contentPanel.getChildren().add(dashboard);

        cargarDatosDashboard();
    }

    private void cargarDatosDashboard() {
        try {
            int prods = productoServices.obtenerTodosLosProductos().size();
            if (lblDashProductos != null)
                lblDashProductos.setText(String.valueOf(prods));
        } catch (Exception e) {
            if (lblDashProductos != null)
                lblDashProductos.setText("—");
        }
        try {
            int bajo = inventarioServices.obtenerProductosConStockBajo().size();
            if (lblDashStockBajo != null) {
                lblDashStockBajo.setText(String.valueOf(bajo));
                if (bajo > 0)
                    lblDashStockBajo.setTextFill(Color.web("#C83C3C"));
            }
        } catch (Exception e) {
            if (lblDashStockBajo != null)
                lblDashStockBajo.setText("—");
        }
        try {
            if (esCliente() && cliente != null) {
                String rep = documentoServices.generarReporteVentasCliente(cliente.getId());
                // Contar líneas de compra en el reporte como aproximación
                long nCompras = rep.lines().filter(l -> l.contains("Compra #")).count();
                if (lblDashCompras != null)
                    lblDashCompras.setText(String.valueOf(nCompras));
            } else {
                int nVentas = documentoServices.obtenerTodasLasVentas().size();
                if (lblDashCompras != null)
                    lblDashCompras.setText(String.valueOf(nVentas));
                double monto = documentoServices.obtenerTodasLasVentas()
                        .stream().mapToDouble(v -> v.getTotal()).sum();
                if (lblDashGastado != null)
                    lblDashGastado.setText(String.format("$%.0f", monto));
            }
        } catch (Exception e) {
            if (lblDashCompras != null)
                lblDashCompras.setText("—");
            if (lblDashGastado != null)
                lblDashGastado.setText("—");
        }
    }

    // ── PRODUCTOS ─────────────────────────────────────────────────────────────
    private void mostrarProductos() {
        try {
            ProductoView view = new ProductoView();
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("📦 Catálogo de Productos", "Sin conexión a la base de datos."));
        }
    }

    private void abrirFormularioProducto() {
        try {
            ProductoFormView form = new ProductoFormView(stage);
            if (form.isGuardadoExitoso())
                mostrarProductos();
        } catch (Exception e) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Formulario de producto en desarrollo.", ButtonType.OK).showAndWait();
        }
    }

    // ── CARRITO (solo clientes) ───────────────────────────────────────────────
    private void mostrarCarrito() {
        if (cliente == null) {
            contentPanel.getChildren().add(placeholder("🛒 Carrito", "Inicia sesión para ver tu carrito."));
            return;
        }
        try {
            CarritoView view = new CarritoView(cliente);
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("🛒 Carrito", "Sin conexión a la base de datos."));
        }
    }

    // ── MIS COMPRAS (solo clientes) ───────────────────────────────────────────
    private void mostrarCompras() {
        if (cliente == null) {
            contentPanel.getChildren().add(placeholder("📄 Mis Compras", "Inicia sesión para ver tu historial."));
            return;
        }
        try {
            RegistroCompraView view = new RegistroCompraView(cliente);
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("📄 Mis Compras", "Sin conexión a la base de datos."));
        }
    }

    // ── PERFIL ────────────────────────────────────────────────────────────────
    private void mostrarPerfil() {
        if (cliente == null) {
            contentPanel.getChildren().add(placeholder("👤 Perfil", "Sin información de usuario."));
            return;
        }
        try {
            PerfilView view = new PerfilView(cliente);
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("👤 Perfil", "Error al cargar el perfil."));
        }
    }

    // ── COTIZACIONES (empleados / admin) ──────────────────────────────────────
    private void mostrarCotizaciones() {
        try {
            CotizacionView view = new CotizacionView();
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("📝 Cotizaciones", "Sin conexión a la base de datos."));
        }
    }

    // ── PUNTO DE VENTA (cajero / admin) ──────────────────────────────────────
    private void mostrarPuntoDeVenta() {
        try {
            VentasPosView view = new VentasPosView();
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("🖥 Punto de Venta", "Sin conexión a la base de datos."));
        }
    }

    // ── REGISTRO DE VENTAS (empleados / admin) ────────────────────────────────
    private void mostrarRegistroVentas() {
        try {
            RegistroVentasView view = new RegistroVentasView();
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("📊 Ventas", "Sin conexión a la base de datos."));
        }
    }

    // ── INVENTARIO (bodeguero / admin) ────────────────────────────────────────
    private void mostrarInventario() {
        try {
            MovimientosView view = new MovimientosView();
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("🔄 Inventario", "Sin conexión a la base de datos."));
        }
    }

    // ── ADMIN PANEL ───────────────────────────────────────────────────────────
    private void mostrarAdminPanel() {
        try {
            AdminDashboardView view = new AdminDashboardView(cliente);
            contentPanel.getChildren().add(view.getRoot());
        } catch (Exception e) {
            contentPanel.getChildren().add(placeholder("⚙ Admin", "Error al cargar el panel de administración."));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CERRAR SESIÓN
    // ═════════════════════════════════════════════════════════════════════════
    private void cerrarSesion() {
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Cerrar sesión?", ButtonType.YES, ButtonType.NO);
        conf.setTitle("Cerrar Sesión");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                Stage loginStage = new Stage();
                new LoginView(loginStage);
                stage.close();
            }
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═════════════════════════════════════════════════════════════════════════
    private VBox kpiCard(String titulo, Label lblValor, String color) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(190);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setStyle(
                "-fx-background-color: #F5F5FA;" +
                        "-fx-border-color: " + color + ";" +
                        "-fx-border-width: 0 0 4 0;");
        Label lbl = new Label(titulo);
        lbl.setTextFill(Color.GRAY);
        lbl.setFont(Font.font("Arial", 12));
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblValor.setTextFill(Color.web("#0A1933"));
        card.getChildren().addAll(lbl, lblValor);
        return card;
    }

    private Label kpiValor(String val) {
        Label l = new Label(val);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        l.setTextFill(Color.web("#0A1933"));
        return l;
    }

    private Canvas crearGraficoBarras(double ancho, double alto) {
        Canvas c = new Canvas(ancho, alto);
        GraphicsContext gc = c.getGraphicsContext2D();
        String[] meses = { "Ene", "Feb", "Mar", "Abr", "May" };
        int[] valores = { 25, 40, 35, 55, 48 };
        double ml = 40, mb = 28, aw = ancho - ml - 20, ah = alto - mb - 10, bw = aw / meses.length - 10;

        gc.setStroke(Color.web("#E0E0E0"));
        // Grid horizontal
        for (int i = 0; i <= 4; i++) {
            double y = alto - mb - (i * ah / 4);
            gc.strokeLine(ml, y, ancho - 10, y);
        }

        gc.setStroke(Color.web("#AAAAAA"));
        gc.strokeLine(ml, alto - mb, ancho - 10, alto - mb);

        for (int i = 0; i < meses.length; i++) {
            double bh = valores[i] * ah / 100.0;
            double x = ml + i * (bw + 10);
            double y = alto - mb - bh;

            gc.setFill(Color.web("#00C8FF"));
            gc.fillRoundRect(x, y, bw, bh, 6, 6);

            gc.setFill(Color.web("#0A1933"));
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            gc.fillText(meses[i], x + bw / 4, alto - 8);
            gc.fillText(valores[i] + "%", x + bw / 5, y - 4);
        }
        return c;
    }

    /** Panel de marcador de posición cuando un servicio no está disponible. */
    private VBox placeholder(String titulo, String msg) {
        VBox box = new VBox(12);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(40));

        Label lblT = new Label(titulo);
        lblT.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblT.setTextFill(Color.web("#0A1933"));

        Label lblM = new Label(msg);
        lblM.setFont(Font.font("Arial", 14));
        lblM.setTextFill(Color.GRAY);

        box.getChildren().addAll(lblT, lblM);
        return box;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // HELPERS DE ROL
    // ═════════════════════════════════════════════════════════════════════════
    private boolean esCliente() {
        return "CLIENTE".equals(rol);
    }

    private boolean esAdmin() {
        return "ADMINISTRADOR".equals(rol);
    }

    private boolean esEmpleadoOAdmin() {
        return !esCliente();
    }

    private boolean esCompradorOAdmin() {
        return "COMPRADOR".equals(rol) || esAdmin();
    }

    private boolean esBodegueroOAdmin() {
        return "BODEGUERO".equals(rol) || esAdmin();
    }
}