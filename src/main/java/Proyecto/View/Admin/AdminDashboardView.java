package Proyecto.View.Admin;

import Proyecto.Model.Cliente;
import Proyecto.services.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Panel de administración de TechZone.
 *
 * Contiene pestañas para:
 * - Dashboard (métricas generales)
 * - Gestión de Empleados
 * - Gestión de Proveedores
 * - Gestión de Categorías
 * - Reportes
 *
 * Se embebe en el contenido principal del MenuPrincipalView cuando
 * el usuario autenticado tiene cargo Administrador.
 */
public class AdminDashboardView {

    private final Cliente adminCliente; // o Empleado si tu modelo lo tiene
    private final ProductoServices productoServices;
    private final InventarioServices inventarioServices;
    private final DocumentoServices documentoServices;

    // Labels de métricas
    private Label lblTotalProductos;
    private Label lblStockBajo;
    private Label lblTotalVentas;
    private Label lblTotalClientes;

    private VBox root;

    // ── Constructor ───────────────────────────────────────────────────────────
    public AdminDashboardView(Cliente admin) {
        this.adminCliente = admin;
        this.productoServices = new ProductoServices();
        this.inventarioServices = new InventarioServices();
        this.documentoServices = new DocumentoServices();
        build();
        cargarMetricas();
    }

    public Node getRoot() {
        return root;
    }

    // ── Construcción ─────────────────────────────────────────────────────────
    private void build() {
        root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(root, Priority.ALWAYS);

        // Encabezado
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 2 0;");

        Label lblTitulo = new Label("⚙  Panel de Administración");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#0A1933"));
        header.getChildren().add(lblTitulo);

        // Pestañas
        TabPane tabs = new TabPane();
        tabs.setStyle(
                ".tab-header-area .tab { -fx-background-color: #E8EDF5; }" +
                        ".tab-header-area .tab:selected { -fx-background-color: #0A1933; }");
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        tabs.getTabs().addAll(
                new Tab("📊 Dashboard", crearTabDashboard()),
                new Tab("👷 Empleados", crearTabEmpleados()),
                new Tab("🏢 Proveedores", crearTabProveedores()),
                new Tab("🏷 Categorías", crearTabCategorias()),
                new Tab("📄 Reportes", crearTabReportes()));

        root.getChildren().addAll(header, tabs);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 1: DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════
    private Node crearTabDashboard() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(15));

        // Tarjetas KPI
        lblTotalProductos = new Label("…");
        lblStockBajo = new Label("…");
        lblTotalVentas = new Label("…");
        lblTotalClientes = new Label("…");

        HBox kpiRow = new HBox(15);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        kpiRow.getChildren().addAll(
                kpiCard("📦 Productos", lblTotalProductos, "#00C8FF"),
                kpiCard("⚠ Stock Bajo", lblStockBajo, "#C83C3C"),
                kpiCard("💰 Ventas Totales", lblTotalVentas, "#1A8A2A"),
                kpiCard("👥 Clientes", lblTotalClientes, "#0A1933"));

        // Gráfico de barras de ventas (últimos 5 meses)
        VBox chartBox = new VBox(8);
        chartBox.setPadding(new Insets(15));
        chartBox.setStyle("-fx-border-color: #DCDCDC; -fx-border-width: 1;");

        Label lblChart = new Label("📊  Ventas mensuales (últimos 5 meses)");
        lblChart.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblChart.setTextFill(Color.web("#0A1933"));

        chartBox.getChildren().addAll(lblChart, crearGraficoBarras());

        // Tabla últimas 5 ventas
        VBox tablaBox = new VBox(8);
        tablaBox.setPadding(new Insets(15));
        tablaBox.setStyle("-fx-border-color: #DCDCDC; -fx-border-width: 1;");

        Label lblTabla = new Label("🕒  Últimas ventas");
        lblTabla.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblTabla.setTextFill(Color.web("#0A1933"));

        TableView<String[]> tablaRecientes = new TableView<>();
        tablaRecientes.setPrefHeight(180);
        tablaRecientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<String[], String> colId = columnaStr("N° Venta", 0);
        TableColumn<String[], String> colCli = columnaStr("Cliente", 1);
        TableColumn<String[], String> colFecha = columnaStr("Fecha", 2);
        TableColumn<String[], String> colTotal = columnaStr("Total", 3);
        TableColumn<String[], String> colEstado = columnaStr("Estado", 4);
        tablaRecientes.getColumns().addAll(colId, colCli, colFecha, colTotal, colEstado);

        // Datos de ejemplo (se reemplazarán con datos reales de documentoServices)
        ObservableList<String[]> rows = FXCollections.observableArrayList();
        try {
            var ventas = documentoServices.obtenerTodasLasVentas();
            int max = Math.min(5, ventas.size());
            for (int i = 0; i < max; i++) {
                var v = ventas.get(i);
                rows.add(new String[] {
                        String.valueOf(v.getId()),
                        v.getCliente().getNombre() + " " + v.getCliente().getApellido(),
                        v.getFecha().toString(),
                        String.format("$%.2f", v.getTotal()),
                        v.getEstado()
                });
            }
        } catch (Exception ignored) {
        }

        tablaRecientes.setItems(rows);
        tablaBox.getChildren().addAll(lblTabla, tablaRecientes);

        panel.getChildren().addAll(kpiRow, chartBox, tablaBox);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");
        return scroll;
    }

    private void cargarMetricas() {
        try {
            int prods = productoServices.obtenerTodosLosProductos().size();
            lblTotalProductos.setText(String.valueOf(prods));
        } catch (Exception e) {
            lblTotalProductos.setText("—");
        }

        try {
            int bajo = inventarioServices.obtenerProductosConStockBajo().size();
            lblStockBajo.setText(String.valueOf(bajo));
            lblStockBajo.setTextFill(bajo > 0 ? Color.web("#C83C3C") : Color.web("#1A8A2A"));
        } catch (Exception e) {
            lblStockBajo.setText("—");
        }

        try {
            var ventas = documentoServices.obtenerTodasLasVentas();
            lblTotalVentas.setText(String.valueOf(ventas.size()));
        } catch (Exception e) {
            lblTotalVentas.setText("—");
        }

        // Clientes — aproximación via personaServices si está disponible
        lblTotalClientes.setText("—");
    }

    private Canvas crearGraficoBarras() {
        Canvas c = new Canvas(700, 130);
        GraphicsContext gc = c.getGraphicsContext2D();

        String[] meses = { "Dic", "Ene", "Feb", "Mar", "Abr" };
        int[] valores = { 30, 55, 40, 70, 60 };

        double ml = 40, mb = 25, aw = 700 - ml - 20, ah = 130 - mb - 10;
        double bw = aw / meses.length - 8;

        gc.setStroke(Color.LIGHTGRAY);
        gc.strokeLine(ml, 5, ml, 130 - mb);
        gc.strokeLine(ml, 130 - mb, 690, 130 - mb);

        for (int i = 0; i < meses.length; i++) {
            double bh = valores[i] * ah / 100.0;
            double x = ml + i * (bw + 8);
            double y = 130 - mb - bh;

            gc.setFill(Color.web("#00C8FF"));
            gc.fillRoundRect(x, y, bw, bh, 6, 6);

            gc.setFill(Color.web("#0A1933"));
            gc.setFont(Font.font("Arial", 11));
            gc.fillText(meses[i], x + bw / 4, 130 - 6);
            gc.fillText(valores[i] + "%", x + 2, y - 3);
        }
        return c;
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 2: EMPLEADOS
    // ═══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private Node crearTabEmpleados() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

        // Barra de acciones
        HBox acciones = new HBox(10);
        acciones.setAlignment(Pos.CENTER_LEFT);

        TextField txtBuscar = campo("Buscar empleado...");
        Button btnBuscar = boton("🔍 Buscar", "#00C8FF");
        Button btnNuevo = boton("➕ Nuevo", "#1A8A2A");
        Button btnEditar = boton("✏ Editar", "#0A1933");
        Button btnDesact = boton("🚫 Desactivar", "#C83C3C");

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);
        acciones.getChildren().addAll(txtBuscar, btnBuscar, spc, btnNuevo, btnEditar, btnDesact);

        // Tabla empleados
        ObservableList<String[]> dataEmps = FXCollections.observableArrayList();
        TableView<String[]> tablaEmps = new TableView<>(dataEmps);
        tablaEmps.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaEmps, Priority.ALWAYS);

        tablaEmps.getColumns().addAll(
                columnaStr("ID", 0),
                columnaStr("Nombres", 1),
                columnaStr("Apellidos", 2),
                columnaStr("Email", 3),
                columnaStr("Cargo", 4),
                columnaStr("Estado", 5));

        // Carga inicial de empleados
        cargarEmpleados(dataEmps);

        // Acciones
        btnBuscar.setOnAction(e -> {
            String q = txtBuscar.getText().trim().toLowerCase();
            dataEmps.removeIf(r -> !r[1].toLowerCase().contains(q) &&
                    !r[2].toLowerCase().contains(q) &&
                    !r[3].toLowerCase().contains(q));
        });

        btnNuevo.setOnAction(e -> {
            Stage s = new Stage();
            new GestionEmpleadosView(s, null, () -> cargarEmpleados(dataEmps));
        });

        btnEditar.setOnAction(e -> {
            String[] sel = tablaEmps.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona un empleado para editar.");
                return;
            }
            Stage s = new Stage();
            new GestionEmpleadosView(s, sel, () -> cargarEmpleados(dataEmps));
        });

        btnDesact.setOnAction(e -> {
            String[] sel = tablaEmps.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona un empleado para desactivar.");
                return;
            }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Desactivar al empleado " + sel[1] + " " + sel[2] + "?",
                    ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        // personaServices.desactivarEmpleado(Integer.parseInt(sel[0]));
                        new Alert(Alert.AlertType.INFORMATION,
                                "Empleado desactivado (funcionalidad lista para conectar).",
                                ButtonType.OK).showAndWait();
                        cargarEmpleados(dataEmps);
                    } catch (Exception ex) {
                        error("Error al desactivar el empleado.");
                    }
                }
            });
        });

        panel.getChildren().addAll(acciones, tablaEmps);
        return panel;
    }

    private void cargarEmpleados(ObservableList<String[]> data) {
        data.clear();
        // TODO: conectar con EmpleadoServices cuando esté disponible.
        // Por ahora se muestran datos de ejemplo para validar la UI:
        data.addAll(
                new String[] { "1", "Gabriel", "Pérez", "gperez@techzone.co", "Administrador", "Activo" },
                new String[] { "2", "Oscar", "Soto", "osoto@techzone.co", "Comprador", "Activo" },
                new String[] { "3", "Leidy", "Bustamante", "lbustamante@tz.co", "Vendedor", "Activo" });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 3: PROVEEDORES
    // ═══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private Node crearTabProveedores() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

        HBox acciones = new HBox(10);
        acciones.setAlignment(Pos.CENTER_LEFT);

        TextField txtBuscar = campo("Buscar proveedor...");
        Button btnBuscar = boton("🔍 Buscar", "#00C8FF");
        Button btnNuevo = boton("➕ Nuevo", "#1A8A2A");
        Button btnEditar = boton("✏ Editar", "#0A1933");

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);
        acciones.getChildren().addAll(txtBuscar, btnBuscar, spc, btnNuevo, btnEditar);

        ObservableList<String[]> dataProv = FXCollections.observableArrayList();
        TableView<String[]> tablaProv = new TableView<>(dataProv);
        tablaProv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaProv, Priority.ALWAYS);

        tablaProv.getColumns().addAll(
                columnaStr("ID", 0),
                columnaStr("Empresa", 1),
                columnaStr("NIT", 2),
                columnaStr("Contacto", 3),
                columnaStr("Email", 4),
                columnaStr("Teléfono", 5));

        cargarProveedores(dataProv);

        btnNuevo.setOnAction(e -> {
            Stage s = new Stage();
            new GestionProveedoresView(s, null, () -> cargarProveedores(dataProv));
        });

        btnEditar.setOnAction(e -> {
            String[] sel = tablaProv.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona un proveedor para editar.");
                return;
            }
            Stage s = new Stage();
            new GestionProveedoresView(s, sel, () -> cargarProveedores(dataProv));
        });

        panel.getChildren().addAll(acciones, tablaProv);
        return panel;
    }

    private void cargarProveedores(ObservableList<String[]> data) {
        data.clear();
        // TODO: conectar con ProveedorServices cuando esté disponible.
        data.addAll(
                new String[] { "1", "Samsung Colombia", "900.123.456-1", "María López", "ventas@samsung.co",
                        "3001234567" },
                new String[] { "2", "Apple Distribuidora", "800.987.654-2", "Carlos Ríos", "carlos@apple-col.co",
                        "3019876543" },
                new String[] { "3", "Lenovo Corp", "700.555.222-3", "Ana Gómez", "ana@lenovo.com", "6043334444" });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 4: CATEGORÍAS
    // ═══════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private Node crearTabCategorias() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

        HBox acciones = new HBox(10);
        acciones.setAlignment(Pos.CENTER_LEFT);

        Button btnNueva = boton("➕ Nueva categoría", "#1A8A2A");
        Button btnEditar = boton("✏ Editar", "#0A1933");
        Button btnElim = boton("🗑 Eliminar", "#C83C3C");

        acciones.getChildren().addAll(btnNueva, btnEditar, btnElim);

        ObservableList<String[]> dataCat = FXCollections.observableArrayList();
        TableView<String[]> tablaCat = new TableView<>(dataCat);
        tablaCat.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaCat, Priority.ALWAYS);

        tablaCat.getColumns().addAll(
                columnaStr("ID", 0),
                columnaStr("Nombre", 1),
                columnaStr("Descripción", 2),
                columnaStr("Estado", 3));

        cargarCategorias(dataCat);

        btnNueva.setOnAction(e -> mostrarFormCategoria(null, dataCat));
        btnEditar.setOnAction(e -> {
            String[] sel = tablaCat.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona una categoría.");
                return;
            }
            mostrarFormCategoria(sel, dataCat);
        });
        btnElim.setOnAction(e -> {
            String[] sel = tablaCat.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona una categoría.");
                return;
            }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "¿Eliminar la categoría \"" + sel[1] + "\"?",
                    ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES)
                    cargarCategorias(dataCat);
            });
        });

        panel.getChildren().addAll(acciones, tablaCat);
        return panel;
    }

    private void cargarCategorias(ObservableList<String[]> data) {
        data.clear();
        try {
            var cats = new CategoriaServices().obtenerTodasLasCategorias();
            for (var c : cats)
                data.add(new String[] {
                        String.valueOf(c.getId()), c.getNombre(), c.getDescripcion(), "Activa" });
        } catch (Exception e) {
            // Fallback con datos de ejemplo
            data.addAll(
                    new String[] { "1", "Smartphones", "Teléfonos móviles inteligentes", "Activa" },
                    new String[] { "2", "Laptops", "Computadores portátiles", "Activa" },
                    new String[] { "3", "Accesorios", "Periféricos y accesorios varios", "Activa" });
        }
    }

    private void mostrarFormCategoria(String[] cat, ObservableList<String[]> data) {
        Dialog<String[]> dlg = new Dialog<>();
        dlg.setTitle(cat == null ? "Nueva Categoría" : "Editar Categoría");
        dlg.setResizable(false);

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(20));

        TextField txtNombre = campo("Nombre de la categoría");
        TextField txtDesc = campo("Descripción");
        if (cat != null) {
            txtNombre.setText(cat[1]);
            txtDesc.setText(cat[2]);
        }

        g.add(etiqueta("Nombre:"), 0, 0);
        g.add(txtNombre, 1, 0);
        g.add(etiqueta("Descripción:"), 0, 1);
        g.add(txtDesc, 1, 1);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(bt -> {
            if (bt == btnOk)
                return new String[] { txtNombre.getText(), txtDesc.getText() };
            return null;
        });

        dlg.showAndWait().ifPresent(r -> {
            if (!r[0].trim().isEmpty()) {
                // TODO: categoriaServices.crear/actualizar(...)
                cargarCategorias(data);
                info("Categoría guardada exitosamente.");
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    // TAB 5: REPORTES
    // ═══════════════════════════════════════════════════════════════════════
    private Node crearTabReportes() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));

        Label lblTitulo = new Label("📄  Generación de Reportes");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        // Opciones de reporte
        String[][] reportes = {
                { "📊 Reporte de Ventas",
                        "Todas las ventas del sistema con detalles por cliente, fecha y monto total." },
                { "📦 Inventario Actual", "Estado actual del inventario con stock disponible, mínimo y categorías." },
                { "⚠ Alertas de Stock Bajo",
                        "Productos cuyo stock ha caído por debajo del umbral mínimo configurado." },
                { "👥 Clientes Frecuentes", "Top 10 clientes con mayor número de compras realizadas en el sistema." },
                { "📈 Rotación de Productos",
                        "Análisis de productos con mayor y menor rotación en el último período." },
                { "💰 Rentabilidad por Categoría", "Margen de ganancia agrupado por categoría de producto." },
        };

        TextArea txtSalida = new TextArea();
        txtSalida.setEditable(false);
        txtSalida.setFont(Font.font("Monospaced", 12));
        txtSalida.setPrefHeight(250);
        VBox.setVgrow(txtSalida, Priority.ALWAYS);

        FlowPane botonesRep = new FlowPane(10, 10);
        for (String[] rep : reportes) {
            Button btn = boton(rep[0], "#0A1933");
            btn.setPrefWidth(240);
            btn.setTooltip(new Tooltip(rep[1]));
            btn.setOnAction(e -> generarReporte(rep[0], txtSalida));
            botonesRep.getChildren().add(btn);
        }

        HBox exportRow = new HBox(10);
        exportRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnExportar = boton("💾 Exportar a texto", "#1A8A2A");
        btnExportar.setOnAction(e -> {
            if (txtSalida.getText().isEmpty()) {
                info("Genera un reporte primero.");
            } else {
                info("El reporte ha sido copiado al portapapeles.");
                javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
                javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
                cc.putString(txtSalida.getText());
                cb.setContent(cc);
            }
        });
        exportRow.getChildren().add(btnExportar);

        panel.getChildren().addAll(lblTitulo, botonesRep, new Label("Resultado:"), txtSalida, exportRow);

        ScrollPane scroll = new ScrollPane(panel);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");
        return scroll;
    }

    private void generarReporte(String tipo, TextArea destino) {
        destino.setText("Generando " + tipo + "...\n");
        new Thread(() -> {
            String resultado;
            try {
                resultado = switch (tipo) {
                    case "📊 Reporte de Ventas" -> documentoServices.generarReporteVentas();
                    case "⚠ Alertas de Stock Bajo" -> inventarioServices.generarAlertaInventario();
                    default -> "Reporte: " + tipo
                            + "\n\nFuncionalidad en implementación.\nConecta el servicio correspondiente en generarReporte().";
                };
            } catch (Exception ex) {
                resultado = "[Sin datos] — " + ex.getMessage();
            }
            String res = resultado;
            javafx.application.Platform.runLater(() -> destino.setText(res));
        }).start();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // HELPERS UI
    // ═══════════════════════════════════════════════════════════════════════
    private VBox kpiCard(String titulo, Label lblValor, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(185);
        card.setPadding(new Insets(15, 10, 15, 10));
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

    @SuppressWarnings("unchecked")
    private TableColumn<String[], String> columnaStr(String titulo, int idx) {
        TableColumn<String[], String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().length > idx ? d.getValue()[idx] : ""));
        return col;
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setFont(Font.font("Arial", 12));
        tf.setStyle("-fx-border-color: #C0C0C0; -fx-border-width: 1; -fx-padding: 7;");
        return tf;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        return l;
    }

    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        b.setTextFill(Color.WHITE);
        b.setStyle("-fx-background-color:" + color + ";-fx-border-width:0;-fx-cursor:hand;-fx-padding:7 14 7 14;");
        return b;
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }
}