package Proyecto.View.Admin;

import Proyecto.Model.Cliente;
import Proyecto.services.CategoriaServices;
import Proyecto.services.DocumentoServices;
import Proyecto.services.InventarioServices;
import Proyecto.services.PersonaServices;
import Proyecto.services.ProductoServices;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class AdminDashboardView {

    private final Cliente adminCliente;
    private final ProductoServices productoServices;
    private final InventarioServices inventarioServices;
    private final DocumentoServices documentoServices;
    private final PersonaServices personaServices;

    private Label lblTotalProductos;
    private Label lblStockBajo;
    private Label lblTotalVentas;
    private Label lblTotalClientes;

    private VBox root;

    public AdminDashboardView(Cliente admin) {
        this.adminCliente = admin;
        this.productoServices = new ProductoServices();
        this.inventarioServices = new InventarioServices();
        this.documentoServices = new DocumentoServices();
        this.personaServices = new PersonaServices();
        build();
        cargarMetricas();
    }

    public Node getRoot() {
        return root;
    }

    private void build() {
        root = new VBox(15);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(root, Priority.ALWAYS);

        VBox header = new VBox(4);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 2 0;");

        Label lblTitulo = new Label("Panel de Administracion");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        Label lblSubtitulo = new Label("Bienvenido, " + adminCliente.getNombre() + " " + adminCliente.getApellido());
        lblSubtitulo.setFont(Font.font("Arial", 14));
        lblSubtitulo.setTextFill(Color.web("#4B5876"));

        header.getChildren().addAll(lblTitulo, lblSubtitulo);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        VBox.setVgrow(tabs, Priority.ALWAYS);

        tabs.getTabs().addAll(
                new Tab("Dashboard", crearTabDashboard()),
                new Tab("Empleados", crearTabEmpleados()),
                new Tab("Proveedores", crearTabProveedores()),
                new Tab("Categorias", crearTabCategorias()),
                new Tab("Reportes", crearTabReportes()));

        root.getChildren().addAll(header, tabs);
    }

    // =========================================================================
    // TAB 1: DASHBOARD
    // =========================================================================
    private Node crearTabDashboard() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(15));

        // Boton de refresco del dashboard
        Button btnRefrescar = boton("Actualizar dashboard", "#0A1933");
        btnRefrescar.setOnAction(e -> cargarMetricas());
        HBox headerDash = new HBox(btnRefrescar);
        headerDash.setAlignment(Pos.CENTER_RIGHT);

        lblTotalProductos = new Label("...");
        lblStockBajo = new Label("...");
        lblTotalVentas = new Label("...");
        lblTotalClientes = new Label("...");

        HBox kpiRow = new HBox(15);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        kpiRow.getChildren().addAll(
                kpiCard("Productos", lblTotalProductos, "#00C8FF"),
                kpiCard("Stock Bajo", lblStockBajo, "#C83C3C"),
                kpiCard("Ventas", lblTotalVentas, "#1A8A2A"),
                kpiCard("Clientes", lblTotalClientes, "#0A1933"));

        VBox chartBox = new VBox(8);
        chartBox.setPadding(new Insets(15));
        chartBox.setStyle("-fx-border-color: #DCDCDC; -fx-border-width: 1;");
        Label lblChart = new Label("Ventas mensuales (ultimos 5 meses)");
        lblChart.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblChart.setTextFill(Color.web("#0A1933"));
        chartBox.getChildren().addAll(lblChart, crearGraficoBarras());

        VBox tablaBox = new VBox(8);
        tablaBox.setPadding(new Insets(15));
        tablaBox.setStyle("-fx-border-color: #DCDCDC; -fx-border-width: 1;");
        Label lblTabla = new Label("Ultimas ventas");
        lblTabla.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblTabla.setTextFill(Color.web("#0A1933"));

        TableView<String[]> tablaRecientes = new TableView<>();
        tablaRecientes.setPrefHeight(180);
        tablaRecientes.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaRecientes.getColumns().addAll(
                columnaStr("N Venta", 0),
                columnaStr("Cliente", 1),
                columnaStr("Fecha", 2),
                columnaStr("Total", 3),
                columnaStr("Estado", 4));

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

        panel.getChildren().addAll(headerDash, kpiRow, chartBox, tablaBox);
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
            lblTotalProductos.setText("-");
        }
        try {
            int bajo = inventarioServices.obtenerProductosConStockBajo().size();
            lblStockBajo.setText(String.valueOf(bajo));
            lblStockBajo.setTextFill(bajo > 0 ? Color.web("#C83C3C") : Color.web("#1A8A2A"));
        } catch (Exception e) {
            lblStockBajo.setText("-");
        }
        try {
            var ventas = documentoServices.obtenerTodasLasVentas();
            lblTotalVentas.setText(String.valueOf(ventas.size()));
        } catch (Exception e) {
            lblTotalVentas.setText("-");
        }
        try {
            var clientes = personaServices.obtenerTodosLosClientes();
            lblTotalClientes.setText(String.valueOf(clientes != null ? clientes.size() : 0));
        } catch (Exception e) {
            lblTotalClientes.setText("-");
        }
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

    // =========================================================================
    // TAB 2: EMPLEADOS — con carga real de BD y boton de refresco
    // =========================================================================
    @SuppressWarnings("unchecked")
    private Node crearTabEmpleados() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

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
                columnaStr("Salario", 6),
                columnaStr("Estado", 5));

        // Barra de acciones
        TextField txtBuscar = campo("Buscar empleado...");
        Button btnBuscar = boton("Buscar", "#00C8FF");
        Button btnRefrescar = boton("Actualizar", "#0A1933");
        Button btnNuevo = boton("Nuevo", "#1A8A2A");
        Button btnEditar = boton("Editar", "#795548");
        Button btnDesact = boton("Desactivar", "#C83C3C");

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);
        HBox acciones = new HBox(10, txtBuscar, btnBuscar, spc, btnRefrescar, btnNuevo, btnEditar, btnDesact);
        acciones.setAlignment(Pos.CENTER_LEFT);

        cargarEmpleados(dataEmps);

        btnBuscar.setOnAction(e -> {
            String q = txtBuscar.getText().trim().toLowerCase();
            if (q.isEmpty()) {
                cargarEmpleados(dataEmps);
                return;
            }
            ObservableList<String[]> filtrado = FXCollections.observableArrayList();
            for (String[] r : dataEmps) {
                if (r[1].toLowerCase().contains(q) ||
                        r[2].toLowerCase().contains(q) ||
                        r[3].toLowerCase().contains(q))
                    filtrado.add(r);
            }
            tablaEmps.setItems(filtrado);
        });

        btnRefrescar.setOnAction(e -> {
            txtBuscar.clear();
            cargarEmpleados(dataEmps);
            tablaEmps.setItems(dataEmps);
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
                    "Desactivar al empleado " + sel[1] + " " + sel[2] + "?",
                    ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        boolean ok = personaServices.actualizarEmpleado(
                                Integer.parseInt(sel[0]),
                                sel[1], sel[2],
                                sel.length > 7 ? sel[7] : "",
                                sel[4], 0, false);
                        if (ok)
                            cargarEmpleados(dataEmps);
                        else
                            error("No se pudo desactivar el empleado.");
                    } catch (Exception ex) {
                        error("Error: " + ex.getMessage());
                    }
                }
            });
        });

        panel.getChildren().addAll(acciones, tablaEmps);
        return panel;
    }

    private void cargarEmpleados(ObservableList<String[]> data) {
        data.clear();
        String sql = "SELECT p.id_persona, p.nombres, p.apellidos, p.email, " +
                "c.nombre AS cargo, p.activo, p.telefono, e.salario " +
                "FROM persona p " +
                "JOIN empleado e ON p.id_persona = e.id_persona " +
                "JOIN cargo    c ON e.id_cargo   = c.id_cargo " +
                "ORDER BY p.nombres ASC";

        try (java.sql.Connection conn = Proyecto.util.conexionBD.obtenerConexion();
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.add(new String[] {
                        String.valueOf(rs.getInt("id_persona")),
                        rs.getString("nombres"),
                        rs.getString("apellidos"),
                        rs.getString("email"),
                        rs.getString("cargo"),
                        rs.getBoolean("activo") ? "Activo" : "Inactivo",
                        String.format("$%,.0f", rs.getDouble("salario")),
                        rs.getString("telefono") != null ? rs.getString("telefono") : ""
                });
            }
        } catch (java.sql.SQLException e) {
            System.err.println("AdminDashboardView.cargarEmpleados: " + e.getMessage());
        }
    }

    // =========================================================================
    // TAB 3: PROVEEDORES
    // =========================================================================
    @SuppressWarnings("unchecked")
    private Node crearTabProveedores() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

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
                columnaStr("Telefono", 5));

        Button btnRefrescar = boton("Actualizar", "#0A1933");
        Button btnNuevo = boton("Nuevo", "#1A8A2A");
        Button btnEditar = boton("Editar", "#795548");

        Region spc = new Region();
        HBox.setHgrow(spc, Priority.ALWAYS);
        HBox acciones = new HBox(10, spc, btnRefrescar, btnNuevo, btnEditar);
        acciones.setAlignment(Pos.CENTER_LEFT);

        cargarProveedores(dataProv);

        btnRefrescar.setOnAction(e -> cargarProveedores(dataProv));

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
        String sql = "SELECT p.id_persona, pr.nombre_empresa, pr.nit, " +
                "p.nombres, p.email, p.telefono " +
                "FROM persona p " +
                "JOIN proveedor pr ON p.id_persona = pr.id_persona " +
                "WHERE p.activo = 1 " +
                "ORDER BY pr.nombre_empresa ASC";

        try (java.sql.Connection conn = Proyecto.util.conexionBD.obtenerConexion();
                java.sql.Statement stmt = conn.createStatement();
                java.sql.ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                data.add(new String[] {
                        String.valueOf(rs.getInt("id_persona")),
                        rs.getString("nombre_empresa"),
                        rs.getString("nit"),
                        rs.getString("nombres"),
                        rs.getString("email"),
                        rs.getString("telefono") != null ? rs.getString("telefono") : ""
                });
            }
        } catch (java.sql.SQLException e) {
            System.err.println("AdminDashboardView.cargarProveedores: " + e.getMessage());
        }
    }

    // =========================================================================
    // TAB 4: CATEGORIAS — con refresco real
    // =========================================================================
    @SuppressWarnings("unchecked")
    private Node crearTabCategorias() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(15));

        ObservableList<String[]> dataCat = FXCollections.observableArrayList();
        TableView<String[]> tablaCat = new TableView<>(dataCat);
        tablaCat.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaCat, Priority.ALWAYS);
        tablaCat.getColumns().addAll(
                columnaStr("ID", 0),
                columnaStr("Nombre", 1),
                columnaStr("Descripcion", 2),
                columnaStr("Estado", 3));

        Button btnRefrescar = boton("Actualizar", "#0A1933");
        Button btnNueva = boton("Nueva categoria", "#1A8A2A");
        Button btnEditar = boton("Editar", "#795548");
        Button btnElim = boton("Eliminar", "#C83C3C");

        HBox acciones = new HBox(10, btnRefrescar, btnNueva, btnEditar, btnElim);
        acciones.setAlignment(Pos.CENTER_LEFT);

        cargarCategorias(dataCat);

        btnRefrescar.setOnAction(e -> cargarCategorias(dataCat));

        btnNueva.setOnAction(e -> mostrarFormCategoria(null, dataCat));
        btnEditar.setOnAction(e -> {
            String[] sel = tablaCat.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona una categoria.");
                return;
            }
            mostrarFormCategoria(sel, dataCat);
        });
        btnElim.setOnAction(e -> {
            String[] sel = tablaCat.getSelectionModel().getSelectedItem();
            if (sel == null) {
                info("Selecciona una categoria.");
                return;
            }
            Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                    "Eliminar la categoria \"" + sel[1] + "\"?",
                    ButtonType.YES, ButtonType.NO);
            conf.showAndWait().ifPresent(r -> {
                if (r == ButtonType.YES) {
                    try {
                        new CategoriaServices().eliminarCategoria(Integer.parseInt(sel[0]));
                        cargarCategorias(dataCat);
                    } catch (Exception ex) {
                        error("No se pudo eliminar la categoria.");
                    }
                }
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
                        String.valueOf(c.getId()),
                        c.getNombre(),
                        c.getDescripcion() != null ? c.getDescripcion() : "",
                        "Activa"
                });
        } catch (Exception e) {
            System.err.println("AdminDashboardView.cargarCategorias: " + e.getMessage());
        }
    }

    private void mostrarFormCategoria(String[] cat, ObservableList<String[]> data) {
        Dialog<String[]> dlg = new Dialog<>();
        dlg.setTitle(cat == null ? "Nueva Categoria" : "Editar Categoria");
        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(12);
        g.setPadding(new Insets(20));

        TextField txtNombre = campo("Nombre de la categoria");
        TextField txtDesc = campo("Descripcion");
        if (cat != null) {
            txtNombre.setText(cat[1]);
            txtDesc.setText(cat[2]);
        }

        g.add(etiqueta("Nombre:"), 0, 0);
        g.add(txtNombre, 1, 0);
        g.add(etiqueta("Descripcion:"), 0, 1);
        g.add(txtDesc, 1, 1);

        ButtonType btnOk = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(bt -> bt == btnOk
                ? new String[] { txtNombre.getText(), txtDesc.getText() }
                : null);

        dlg.showAndWait().ifPresent(r -> {
            if (r[0].trim().isEmpty())
                return;
            try {
                CategoriaServices cs = new CategoriaServices();
                if (cat == null) {
                    cs.crearCategoria(r[0].trim(), r[1].trim());
                } else {
                    cs.actualizarCategoria(Integer.parseInt(cat[0]), r[0].trim(), r[1].trim());
                }
                cargarCategorias(data);
            } catch (Exception ex) {
                error("No se pudo guardar la categoria.");
            }
        });
    }

    // =========================================================================
    // TAB 5: REPORTES
    // =========================================================================
    private Node crearTabReportes() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(15));

        Label lblTitulo = new Label("Generacion de Reportes");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        String[][] reportes = {
                { "Reporte de Ventas", "Todas las ventas con detalle por cliente, fecha y monto." },
                { "Inventario Actual", "Estado del inventario con stock disponible y categorias." },
                { "Alertas de Stock Bajo", "Productos bajo el umbral minimo configurado." },
                { "Clientes Frecuentes", "Top 10 clientes con mayor numero de compras." },
                { "Rotacion de Productos", "Productos con mayor y menor rotacion." },
                { "Rentabilidad por Categoria", "Margen de ganancia agrupado por categoria." },
        };

        TextArea txtSalida = new TextArea();
        txtSalida.setEditable(false);
        txtSalida.setFont(Font.font("Monospaced", 12));
        txtSalida.setPrefHeight(260);
        VBox.setVgrow(txtSalida, Priority.ALWAYS);

        FlowPane botonesRep = new FlowPane(10, 10);
        for (String[] rep : reportes) {
            Button btn = boton(rep[0], "#0A1933");
            btn.setPrefWidth(250);
            btn.setTooltip(new Tooltip(rep[1]));
            btn.setOnAction(e -> generarReporte(rep[0], txtSalida));
            botonesRep.getChildren().add(btn);
        }

        HBox exportRow = new HBox(10);
        exportRow.setAlignment(Pos.CENTER_RIGHT);
        Button btnExportar = boton("Copiar al portapapeles", "#1A8A2A");
        btnExportar.setOnAction(e -> {
            if (txtSalida.getText().isEmpty()) {
                info("Genera un reporte primero.");
                return;
            }
            javafx.scene.input.Clipboard cb = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent cc = new javafx.scene.input.ClipboardContent();
            cc.putString(txtSalida.getText());
            cb.setContent(cc);
            info("Reporte copiado al portapapeles.");
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
                    case "Reporte de Ventas" -> documentoServices.generarReporteVentas();
                    case "Alertas de Stock Bajo" -> inventarioServices.generarAlertaInventario();
                    default -> "Reporte: " + tipo + "\n\nConecta el servicio correspondiente en generarReporte().";
                };
            } catch (Exception ex) {
                resultado = "[Sin datos] — " + ex.getMessage();
            }
            String res = resultado;
            javafx.application.Platform.runLater(() -> destino.setText(res));
        }).start();
    }

    // =========================================================================
    // HELPERS UI
    // =========================================================================
    private VBox kpiCard(String titulo, Label lblValor, String color) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(185);
        card.setPadding(new Insets(15, 10, 15, 10));
        card.setStyle("-fx-background-color: #F5F5FA; -fx-border-color: " + color + "; -fx-border-width: 0 0 4 0;");
        Label lbl = new Label(titulo);
        lbl.setTextFill(Color.GRAY);
        lbl.setFont(Font.font("Arial", 12));
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblValor.setTextFill(Color.web("#0A1933"));
        card.getChildren().addAll(lbl, lblValor);
        return card;
    }

    private TableColumn<String[], String> columnaStr(String titulo, int idx) {
        TableColumn<String[], String> col = new TableColumn<>(titulo);
        col.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().length > idx ? d.getValue()[idx] : ""));
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