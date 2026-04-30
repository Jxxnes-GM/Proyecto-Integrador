package Proyecto.View.Inventario;

import Proyecto.services.InventarioServices;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Vista de Movimientos de Inventario en JavaFX.
 * Muestra entradas, salidas y ajustes de stock, con alertas de stock bajo.
 */
public class MovimientosView {

    private final InventarioServices inventarioServices;

    private TableView<FilaMovimiento> tablaMovimientos;
    private TableView<FilaAlerta> tablaAlertas;
    private ObservableList<FilaMovimiento> movimientos;
    private ObservableList<FilaAlerta> alertas;

    private ComboBox<String> cbTipoFiltro;
    private Label lblTotalEntradas;
    private Label lblTotalSalidas;
    private Label lblTotalAlertas;
    private VBox root;

    public MovimientosView() {
        this.inventarioServices = new InventarioServices();
        this.movimientos = FXCollections.observableArrayList();
        this.alertas = FXCollections.observableArrayList();
        build();
        cargarDatos();
    }

    public Node getRoot() {
        return root;
    }

    // ── Construcción ─────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private void build() {
        root = new VBox(15);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(root, Priority.ALWAYS);

        // Encabezado
        Label lblTitulo = new Label(" Movimientos de Inventario");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        // ── Tarjetas de resumen ───────────────────────────────────────────
        HBox statsRow = new HBox(15);
        statsRow.setAlignment(Pos.CENTER_LEFT);

        lblTotalEntradas = new Label("0");
        lblTotalSalidas = new Label("0");
        lblTotalAlertas = new Label("0");

        statsRow.getChildren().addAll(
                tarjeta(" Entradas", lblTotalEntradas, "#1A8A2A"),
                tarjeta(" Salidas", lblTotalSalidas, "#C8820A"),
                tarjeta(" Stock Bajo", lblTotalAlertas, "#C83C3C"));

        // ── Filtros y acciones ────────────────────────────────────────────
        HBox filtros = new HBox(12);
        filtros.setAlignment(Pos.CENTER_LEFT);

        cbTipoFiltro = new ComboBox<>(FXCollections.observableArrayList(
                "Todos", "Entrada", "Salida", "Ajuste"));
        cbTipoFiltro.getSelectionModel().selectFirst();
        cbTipoFiltro.setStyle("-fx-font-size: 12px;");

        Button btnFiltrar = boton(" Filtrar", "#00C8FF");
        Button btnAlertas = boton(" Ver Alertas", "#C83C3C");
        Button btnRegistrar = boton(" Registrar Movimiento", "#0A1933");

        btnFiltrar.setOnAction(e -> filtrarMovimientos());
        btnAlertas.setOnAction(e -> mostrarPanelAlertas());
        btnRegistrar.setOnAction(e -> abrirRegistroMovimiento());

        filtros.getChildren().addAll(
                new Label("Tipo:") {
                    {
                        setFont(Font.font("Arial", FontWeight.BOLD, 12));
                    }
                },
                cbTipoFiltro, btnFiltrar,
                new Region() {
                    {
                        HBox.setHgrow(this, Priority.ALWAYS);
                    }
                },
                btnAlertas, btnRegistrar);

        // ── Tabla de movimientos ──────────────────────────────────────────
        tablaMovimientos = new TableView<>(movimientos);
        //  Correcto para JavaFX 21
        tablaMovimientos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaMovimientos, Priority.ALWAYS);

        TableColumn<FilaMovimiento, Integer> colId = new TableColumn<>("N°");
        colId.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()).asObject());
        colId.setMaxWidth(60);

        TableColumn<FilaMovimiento, String> colProducto = new TableColumn<>("Producto");
        colProducto.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto()));

        TableColumn<FilaMovimiento, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTipo.setMaxWidth(90);
        colTipo.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(v);
                    String color = switch (v.toLowerCase()) {
                        case "entrada" -> "#1A8A2A";
                        case "salida" -> "#C8820A";
                        default -> "#00C8FF";
                    };
                    setStyle("-fx-text-fill:" + color + ";-fx-font-weight:bold;");
                }
            }
        });

        TableColumn<FilaMovimiento, Integer> colCantidad = new TableColumn<>("Cantidad");
        colCantidad.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()).asObject());
        colCantidad.setMaxWidth(90);

        TableColumn<FilaMovimiento, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFecha()));

        TableColumn<FilaMovimiento, String> colObs = new TableColumn<>("Observación");
        colObs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservacion()));

        tablaMovimientos.getColumns().addAll(colId, colProducto, colTipo, colCantidad, colFecha, colObs);
        tablaMovimientos.setRowFactory(tv -> {
            TableRow<FilaMovimiento> row = new TableRow<>();
            row.setStyle("-fx-cell-size: 38px;");
            return row;
        });

        // ── Panel de alertas (oculto por defecto) ─────────────────────────
        tablaAlertas = new TableView<>(alertas);
        //  Correcto para JavaFX 21
        tablaAlertas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        tablaAlertas.setPrefHeight(150);
        tablaAlertas.setVisible(false);
        tablaAlertas.setManaged(false);

        TableColumn<FilaAlerta, String> colAlProd = new TableColumn<>("Producto");
        colAlProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getProducto()));
        TableColumn<FilaAlerta, Integer> colAlStock = new TableColumn<>("Stock Actual");
        colAlStock.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStock()).asObject());
        colAlStock.setMaxWidth(110);
        TableColumn<FilaAlerta, Integer> colAlMin = new TableColumn<>("Stock Mínimo");
        colAlMin.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getStockMinimo()).asObject());
        colAlMin.setMaxWidth(110);
        TableColumn<FilaAlerta, String> colAlNivel = new TableColumn<>("Nivel");
        colAlNivel.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNivel()));
        colAlNivel.setMaxWidth(90);
        colAlNivel.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                if (empty || v == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(v);
                    setStyle(v.equalsIgnoreCase("Crítico")
                            ? "-fx-text-fill:#C83C3C;-fx-font-weight:bold;"
                            : "-fx-text-fill:#C8820A;-fx-font-weight:bold;");
                }
            }
        });

        tablaAlertas.getColumns().addAll(colAlProd, colAlStock, colAlMin, colAlNivel);

        Label lblAlertas = new Label(" Productos con Stock Bajo:");
        lblAlertas.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblAlertas.setTextFill(Color.web("#C83C3C"));
        lblAlertas.setVisible(false);
        lblAlertas.setManaged(false);

        // Sincronizar visibilidad
        tablaAlertas.visibleProperty().addListener((obs, o, nv) -> {
            lblAlertas.setVisible(nv);
            lblAlertas.setManaged(nv);
        });
        tablaAlertas.managedProperty().bind(tablaAlertas.visibleProperty());

        root.getChildren().addAll(lblTitulo, statsRow, filtros, tablaMovimientos, lblAlertas, tablaAlertas);
    }

    private VBox tarjeta(String titulo, Label lblValor, String colorBorde) {
        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #F5F5FA; -fx-border-color:" + colorBorde + "; -fx-border-width: 0 0 3 0;");
        Label lblT = new Label(titulo);
        lblT.setTextFill(Color.GRAY);
        lblT.setFont(Font.font("Arial", 12));
        lblValor.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        lblValor.setTextFill(Color.web("#0A1933"));
        card.getChildren().addAll(lblT, lblValor);
        return card;
    }

    // ── Datos ────────────────────────────────────────────────────────────────
    private void cargarDatos() {
        movimientos.clear();
        alertas.clear();
        int entradas = 0, salidas = 0;

        try {
            var lista = inventarioServices.obtenerMovimientos();
            for (var m : lista) {
                movimientos.add(new FilaMovimiento(
                        m.getId(),
                        m.getProducto().getNombre(),
                        m.getTipo(),
                        m.getCantidad(),
                        m.getFecha().toString(),
                        m.getObservacion() != null ? m.getObservacion() : ""));
                if (m.getTipo().equalsIgnoreCase("Entrada"))
                    entradas += m.getCantidad();
                else if (m.getTipo().equalsIgnoreCase("Salida"))
                    salidas += m.getCantidad();
            }
        } catch (Exception ex) {
            /* servicio no implementado aún */ }

        // Alertas stock bajo
        try {
            var stockBajo = inventarioServices.obtenerProductosConStockBajo();
            for (var p : stockBajo) {
                String nombre = (String) p.get("nombre");
                int cantidad = ((Number) p.get("stockActual")).intValue();
                int stockMinimo = p.containsKey("stockMinimo") ? ((Number) p.get("stockMinimo")).intValue() : 5;
                String nivel = cantidad == 0 ? "Crítico" : "Bajo";
                alertas.add(new FilaAlerta(nombre, cantidad, stockMinimo, nivel));
            }
            lblTotalAlertas.setText(String.valueOf(alertas.size()));
            lblTotalAlertas.setTextFill(alertas.isEmpty() ? Color.web("#1A8A2A") : Color.web("#C83C3C"));
        } catch (Exception ex) {
            lblTotalAlertas.setText("0");
        }

        lblTotalEntradas.setText(String.valueOf(entradas));
        lblTotalSalidas.setText(String.valueOf(salidas));
    }

    private void filtrarMovimientos() {
        String tipo = cbTipoFiltro.getSelectionModel().getSelectedItem();
        if (tipo == null || tipo.equals("Todos")) {
            cargarDatos();
            return;
        }
        movimientos.removeIf(m -> !m.getTipo().equalsIgnoreCase(tipo));
    }

    private void mostrarPanelAlertas() {
        boolean visible = tablaAlertas.isVisible();
        tablaAlertas.setVisible(!visible);
        tablaAlertas.setManaged(!visible);
    }

    private void abrirRegistroMovimiento() {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Registrar Movimiento de Inventario");
        dlg.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField txtProductoId = campoTexto("ID del Producto");
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Entrada", "Salida", "Ajuste"));
        cbTipo.getSelectionModel().selectFirst();
        TextField txtCantidad = campoTexto("Cantidad");
        TextField txtObs = campoTexto("Observación (opcional)");

        grid.add(new Label("ID Producto:"), 0, 0);
        grid.add(txtProductoId, 1, 0);
        grid.add(new Label("Tipo:"), 0, 1);
        grid.add(cbTipo, 1, 1);
        grid.add(new Label("Cantidad:"), 0, 2);
        grid.add(txtCantidad, 1, 2);
        grid.add(new Label("Observación:"), 0, 3);
        grid.add(txtObs, 1, 3);

        for (Node n : grid.getChildren())
            if (n instanceof Label l)
                l.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        ButtonType btnGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(bt -> {
            if (bt == btnGuardar) {
                try {
                    int idProd = Integer.parseInt(txtProductoId.getText().trim());
                    String tipo = cbTipo.getSelectionModel().getSelectedItem();
                    int cantidad = Integer.parseInt(txtCantidad.getText().trim());
                    String obs = txtObs.getText().trim();

                    if (cantidad <= 0)
                        throw new NumberFormatException("cantidad");

                    boolean ok = inventarioServices.registrarMovimiento(idProd, tipo, cantidad, obs);
                    if (ok) {
                        new Alert(Alert.AlertType.INFORMATION, "Movimiento registrado correctamente.", ButtonType.OK)
                                .showAndWait();
                        cargarDatos();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Error al registrar el movimiento.", ButtonType.OK)
                                .showAndWait();
                    }
                } catch (NumberFormatException e) {
                    new Alert(Alert.AlertType.ERROR, "Ingrese valores numéricos válidos.", ButtonType.OK).showAndWait();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.INFORMATION, "Funcionalidad en desarrollo.", ButtonType.OK).showAndWait();
                }
            }
            return null;
        });

        dlg.showAndWait();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        b.setTextFill(Color.WHITE);
        b.setStyle("-fx-background-color:" + color + ";-fx-border-width:0;-fx-cursor:hand;-fx-padding:7 14 7 14;");
        return b;
    }

    private TextField campoTexto(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(220);
        tf.setStyle("-fx-border-color:#B4B4B4;-fx-border-width:1;-fx-padding:7;");
        return tf;
    }

    // ── Modelos de fila ──────────────────────────────────────────────────────
    public static class FilaMovimiento {
        private final int id;
        private final String producto;
        private final String tipo;
        private final int cantidad;
        private final String fecha;
        private final String observacion;

        public FilaMovimiento(int id, String producto, String tipo, int cantidad, String fecha, String obs) {
            this.id = id;
            this.producto = producto;
            this.tipo = tipo;
            this.cantidad = cantidad;
            this.fecha = fecha;
            this.observacion = obs;
        }

        public int getId() {
            return id;
        }

        public String getProducto() {
            return producto;
        }

        public String getTipo() {
            return tipo;
        }

        public int getCantidad() {
            return cantidad;
        }

        public String getFecha() {
            return fecha;
        }

        public String getObservacion() {
            return observacion;
        }
    }

    public static class FilaAlerta {
        private final String producto;
        private final int stock;
        private final int stockMinimo;
        private final String nivel;

        public FilaAlerta(String producto, int stock, int stockMinimo, String nivel) {
            this.producto = producto;
            this.stock = stock;
            this.stockMinimo = stockMinimo;
            this.nivel = nivel;
        }

        public String getProducto() {
            return producto;
        }

        public int getStock() {
            return stock;
        }

        public int getStockMinimo() {
            return stockMinimo;
        }

        public String getNivel() {
            return nivel;
        }
    }
}