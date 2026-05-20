package Proyecto.View.Documento;

import Proyecto.Model.Producto;
import Proyecto.dao.DocumentoDAO;
import Proyecto.dao.InventarioDAO;
import Proyecto.services.ProductoServices;
import javafx.beans.property.SimpleDoubleProperty;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VentasPosView {

    private final int empleadoId;
    private final ProductoServices productoServices;
    private final DocumentoDAO documentoDAO;
    private final InventarioDAO inventarioDAO;

    private ObservableList<ItemVenta> itemsVenta;
    private List<Producto> productosFiltrados;

    // Widgets
    private TextField txtBuscador;
    private ListView<String> listProductos;
    private Spinner<Integer> spinnerCantidad;
    private ComboBox<String> cbMetodoPago;
    private TextField txtIdCliente;

    private TableView<ItemVenta> tablaTicket;
    private Label lblSubtotal;
    private Label lblIva;
    private Label lblTotal;

    private VBox root;

    // Mapa metodo de pago nombre -> id_metodo_pago
    private static final String[] METODOS_PAGO = {
            "Efectivo", "Tarjeta Debito", "Tarjeta Credito",
            "Transferencia Bancaria", "Nequi / Daviplata"
    };

    public VentasPosView(int empleadoId) {
        this.empleadoId = empleadoId;
        this.productoServices = new ProductoServices();
        this.documentoDAO = new DocumentoDAO();
        this.inventarioDAO = new InventarioDAO();
        this.itemsVenta = FXCollections.observableArrayList();
        this.productosFiltrados = new ArrayList<>();
        build();
        cargarProductos("");
    }

    public VentasPosView() {
        this(0);
    }

    public Node getRoot() {
        return root;
    }

    @SuppressWarnings("unchecked")
    private void build() {
        root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(root, Priority.ALWAYS);

        // Encabezado
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 2 0;");

        Label lblTitulo = new Label("Punto de Venta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Label lblFecha = new Label(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        lblFecha.setFont(Font.font("Arial", 12));
        lblFecha.setTextFill(Color.GRAY);

        header.getChildren().addAll(lblTitulo, hSpacer, lblFecha);

        // Cuerpo: dos columnas
        HBox body = new HBox(12);
        VBox.setVgrow(body, Priority.ALWAYS);

        // Columna izquierda: datos del cliente + buscador de productos
        VBox leftCol = new VBox(10);
        leftCol.setPrefWidth(300);
        leftCol.setMaxWidth(320);

        // Seccion cliente
        VBox secCliente = seccion("Datos de la Venta");

        Label lblClienteHint = new Label("ID del Cliente (opcional):");
        lblClienteHint.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        txtIdCliente = new TextField();
        txtIdCliente.setPromptText("ID del cliente o dejar vacio");
        txtIdCliente.setStyle("-fx-border-color: #C0C0C0; -fx-border-width: 1; -fx-padding: 7;");

        Label lblMetodo = new Label("Metodo de Pago:");
        lblMetodo.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        cbMetodoPago = new ComboBox<>();
        cbMetodoPago.getItems().addAll(METODOS_PAGO);
        cbMetodoPago.getSelectionModel().selectFirst();
        cbMetodoPago.setMaxWidth(Double.MAX_VALUE);

        secCliente.getChildren().addAll(lblClienteHint, txtIdCliente, lblMetodo, cbMetodoPago);

        // Seccion buscador de productos
        VBox secBuscar = seccion("Buscar Producto");
        VBox.setVgrow(secBuscar, Priority.ALWAYS);

        txtBuscador = new TextField();
        txtBuscador.setPromptText("Nombre o ID del producto...");
        txtBuscador.setFont(Font.font("Arial", 13));
        txtBuscador.setStyle("-fx-border-color: #00C8FF; -fx-border-width: 1.5; -fx-padding: 8;");
        txtBuscador.textProperty().addListener((obs, o, nv) -> cargarProductos(nv));

        listProductos = new ListView<>();
        listProductos.setPrefHeight(260);
        VBox.setVgrow(listProductos, Priority.ALWAYS);
        listProductos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
                agregarProducto();
        });

        Label lblCant = new Label("Cantidad:");
        lblCant.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        spinnerCantidad = new Spinner<>(1, 9999, 1);
        spinnerCantidad.setEditable(true);
        spinnerCantidad.setPrefWidth(90);

        Button btnAgregar = boton("Agregar al ticket", "#1A8A2A");
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> agregarProducto());

        HBox cantRow = new HBox(8, lblCant, spinnerCantidad);
        cantRow.setAlignment(Pos.CENTER_LEFT);

        secBuscar.getChildren().addAll(txtBuscador, listProductos, cantRow, btnAgregar);
        leftCol.getChildren().addAll(secCliente, secBuscar);

        // Columna derecha: ticket de venta
        VBox rightCol = new VBox(10);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox secTicket = seccion("Ticket de Venta");
        VBox.setVgrow(secTicket, Priority.ALWAYS);

        tablaTicket = new TableView<>(itemsVenta);
        tablaTicket.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaTicket, Priority.ALWAYS);
        tablaTicket.setPlaceholder(new Label("Agrega productos desde el panel izquierdo."));

        TableColumn<ItemVenta, String> colProd = new TableColumn<>("Producto");
        colProd.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));

        TableColumn<ItemVenta, Integer> colCant = new TableColumn<>("Cant.");
        colCant.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getCantidad()).asObject());
        colCant.setMaxWidth(70);

        TableColumn<ItemVenta, Double> colPU = new TableColumn<>("P. Unit.");
        colPU.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecio()).asObject());
        colPU.setCellFactory(c -> precioCell());
        colPU.setMaxWidth(100);

        TableColumn<ItemVenta, Double> colSub = new TableColumn<>("Subtotal");
        colSub.setCellValueFactory(d -> new SimpleDoubleProperty(
                d.getValue().getPrecio() * d.getValue().getCantidad()).asObject());
        colSub.setCellFactory(c -> precioCell());
        colSub.setMaxWidth(110);

        TableColumn<ItemVenta, Void> colDel = new TableColumn<>("");
        colDel.setMaxWidth(48);
        colDel.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("X");
            {
                b.setStyle("-fx-background-color:#C83C3C;-fx-text-fill:white;-fx-cursor:hand;" +
                        "-fx-border-width:0;-fx-padding:3 6 3 6;");
                b.setOnAction(e -> {
                    itemsVenta.remove(getTableView().getItems().get(getIndex()));
                    actualizarTotales();
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : b);
            }
        });

        tablaTicket.getColumns().addAll(colProd, colCant, colPU, colSub, colDel);

        // Totales
        GridPane gridTotales = new GridPane();
        gridTotales.setHgap(20);
        gridTotales.setVgap(5);
        gridTotales.setPadding(new Insets(10, 0, 0, 0));
        gridTotales.setAlignment(Pos.CENTER_RIGHT);

        lblSubtotal = totalLabel("$0.00");
        lblIva = totalLabel("$0.00");
        lblTotal = new Label("$0.00");
        lblTotal.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTotal.setTextFill(Color.web("#0A1933"));

        gridTotales.add(etiqueta("Subtotal:"), 0, 0);
        gridTotales.add(lblSubtotal, 1, 0);
        gridTotales.add(etiqueta("IVA (19%):"), 0, 1);
        gridTotales.add(lblIva, 1, 1);
        gridTotales.add(etiqueta("TOTAL:"), 0, 2);
        gridTotales.add(lblTotal, 1, 2);

        secTicket.getChildren().addAll(tablaTicket, gridTotales);

        // Botones de cobro
        HBox btnCobro = new HBox(10);
        btnCobro.setAlignment(Pos.CENTER_RIGHT);

        Button btnCancelar = boton("Cancelar venta", "#646464");
        Button btnCobrar = boton("Cobrar y emitir factura", "#1A8A2A");
        btnCobrar.setPrefHeight(42);
        btnCobrar.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        btnCancelar.setOnAction(e -> cancelarVenta());
        btnCobrar.setOnAction(e -> cobrarYEmitirFactura());

        btnCobro.getChildren().addAll(btnCancelar, btnCobrar);

        rightCol.getChildren().addAll(secTicket, btnCobro);
        body.getChildren().addAll(leftCol, rightCol);
        root.getChildren().addAll(header, body);
    }

    // ── Logica ───────────────────────────────────────────────────────────────

    private void cargarProductos(String query) {
        productosFiltrados.clear();
        listProductos.getItems().clear();
        try {
            List<Producto> todos = query.isEmpty()
                    ? productoServices.obtenerTodosLosProductos()
                    : productoServices.buscarProductos(query);

            for (Producto p : todos) {
                if (Boolean.TRUE.equals(p.getActivo()) && p.getCantidad() > 0) {
                    productosFiltrados.add(p);
                    listProductos.getItems().add(String.format(
                            "[%d] %-30s  $%,.0f  (stock: %d)",
                            p.getIdProducto(),
                            truncar(p.getNombre(), 28),
                            p.getPrecioVenta(),
                            p.getCantidad()));
                }
            }
        } catch (Exception e) {
            listProductos.setPlaceholder(new Label("Error al cargar productos: " + e.getMessage()));
        }
    }

    private void agregarProducto() {
        int idx = listProductos.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= productosFiltrados.size()) {
            info("Selecciona un producto de la lista.");
            return;
        }
        Producto p = productosFiltrados.get(idx);
        int cant = spinnerCantidad.getValue();

        // Calcular cantidad ya en el ticket para validar contra stock real
        int cantEnTicket = itemsVenta.stream()
                .filter(i -> i.getIdProducto() == p.getIdProducto())
                .mapToInt(ItemVenta::getCantidad)
                .sum();

        if (cantEnTicket + cant > p.getCantidad()) {
            info("Stock insuficiente. Disponible: " + p.getCantidad() +
                    ", ya en ticket: " + cantEnTicket);
            return;
        }

        for (ItemVenta item : itemsVenta) {
            if (item.getIdProducto() == p.getIdProducto()) {
                item.setCantidad(item.getCantidad() + cant);
                tablaTicket.refresh();
                actualizarTotales();
                return;
            }
        }

        itemsVenta.add(new ItemVenta(p.getIdProducto(), p.getNombre(), p.getPrecioVenta(), cant));
        actualizarTotales();
    }

    private void actualizarTotales() {
        double subtotal = itemsVenta.stream()
                .mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();
        double iva = subtotal * 0.19;
        double total = subtotal + iva;
        lblSubtotal.setText(String.format("$%,.0f", subtotal));
        lblIva.setText(String.format("$%,.0f", iva));
        lblTotal.setText(String.format("$%,.0f", total));
    }

    private void cobrarYEmitirFactura() {
        if (itemsVenta.isEmpty()) {
            info("Agrega al menos un producto al ticket.");
            return;
        }

        String metodo = cbMetodoPago.getValue();
        int idMetodoPago = cbMetodoPago.getSelectionModel().getSelectedIndex() + 1;

        // Determinar id_persona del cliente
        int idPersona = 0;
        String txtCliente = txtIdCliente.getText().trim();
        if (!txtCliente.isEmpty()) {
            try {
                idPersona = Integer.parseInt(txtCliente);
            } catch (NumberFormatException ex) {
                info("El ID de cliente debe ser un numero entero.");
                return;
            }
        }

        // Si no se especifica cliente, usar el cliente anonimo (id_persona del
        // bodeguero como fallback)
        // En produccion se debe tener un cliente anonimo registrado en la BD
        if (idPersona <= 0) {
            info("Ingresa el ID del cliente para registrar la venta.\n" +
                    "Puedes consultarlo en el modulo de Administracion.");
            return;
        }

        final int idPersonaFinal = idPersona;

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmar cobro de " + lblTotal.getText() + " con " + metodo + "?",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmar Cobro");
        conf.showAndWait().ifPresent(r -> {
            if (r != ButtonType.YES)
                return;

            try {
                // Calcular subtotal sin IVA para guardar en BD
                double subtotalSinIva = itemsVenta.stream()
                        .mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();
                double totalConIva = subtotalSinIva * 1.19;

                // 1. Crear documento de venta
                int idDocumento = documentoDAO.crearDocumento(
                        1, // id_tipo_documento: Factura de Venta
                        idPersonaFinal,
                        empleadoId, // 0 si no hay cajero autenticado
                        0, // descuento
                        totalConIva,
                        "Venta POS - Metodo: " + metodo);

                if (idDocumento == -1) {
                    new Alert(Alert.AlertType.ERROR,
                            "Error al registrar la venta en la base de datos.",
                            ButtonType.OK).showAndWait();
                    return;
                }

                // 2. Registrar movimientos y descontar stock
                for (ItemVenta item : itemsVenta) {
                    inventarioDAO.registrarMovimientoConPrecio(
                            idDocumento,
                            item.getIdProducto(),
                            empleadoId,
                            item.getCantidad(),
                            item.getPrecio());

                    inventarioDAO.actualizarStock(item.getIdProducto(), -item.getCantidad());
                }

                // 3. Emitir ticket y limpiar
                emitirFactura(metodo, idDocumento);
                itemsVenta.clear();
                actualizarTotales();
                cargarProductos(""); // refrescar stock visible

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR,
                        "Error al registrar la venta: " + ex.getMessage(),
                        ButtonType.OK).showAndWait();
            }
        });
    }

    private void emitirFactura(String metodoPago, int idDocumento) {
        StringBuilder sb = new StringBuilder();
        sb.append("===========================================\n");
        sb.append("          TECHZONE  —  FACTURA\n");
        sb.append("===========================================\n");
        sb.append(String.format("  N° Documento : %d%n", idDocumento));
        sb.append(String.format("  Fecha        : %s%n",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        sb.append(String.format("  Metodo pago  : %s%n", metodoPago));
        sb.append("-------------------------------------------\n");
        sb.append(String.format("  %-28s %6s %12s%n", "PRODUCTO", "CANT", "SUBTOTAL"));
        sb.append("-------------------------------------------\n");

        for (ItemVenta item : itemsVenta) {
            sb.append(String.format("  %-28s %6d %12,.0f%n",
                    truncar(item.getNombre(), 28),
                    item.getCantidad(),
                    item.getPrecio() * item.getCantidad()));
        }

        sb.append("-------------------------------------------\n");
        sb.append(String.format("  Subtotal  : %s%n", lblSubtotal.getText()));
        sb.append(String.format("  IVA(19%%) : %s%n", lblIva.getText()));
        sb.append(String.format("  TOTAL     : %s%n", lblTotal.getText()));
        sb.append("===========================================\n");
        sb.append("   Gracias por tu compra en TechZone!\n");

        TextArea txt = new TextArea(sb.toString());
        txt.setEditable(false);
        txt.setFont(Font.font("Monospaced", 12));
        txt.setPrefSize(480, 380);

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Factura N° " + idDocumento);
        dlg.getDialogPane().setContent(new ScrollPane(txt));
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    private void cancelarVenta() {
        if (itemsVenta.isEmpty())
            return;
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancelar la venta y limpiar el ticket?",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Cancelar venta");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES) {
                itemsVenta.clear();
                actualizarTotales();
            }
        });
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────

    private VBox seccion(String titulo) {
        VBox sec = new VBox(10);
        sec.setPadding(new Insets(12));
        sec.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 1;");
        Label lbl = new Label(titulo);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lbl.setTextFill(Color.web("#0A1933"));
        sec.getChildren().add(lbl);
        return sec;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        l.setTextFill(Color.web("#555555"));
        return l;
    }

    private Label totalLabel(String v) {
        Label l = new Label(v);
        l.setFont(Font.font("Arial", 14));
        l.setTextFill(Color.web("#0A1933"));
        return l;
    }

    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        b.setTextFill(Color.WHITE);
        b.setStyle("-fx-background-color:" + color + ";-fx-border-width:0;" +
                "-fx-cursor:hand;-fx-padding:8 14 8 14;");
        return b;
    }

    private TableCell<ItemVenta, Double> precioCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : String.format("$%,.0f", v));
            }
        };
    }

    private String truncar(String s, int max) {
        return s != null && s.length() > max ? s.substring(0, max - 1) + "..." : s;
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    // ── Modelo de fila ────────────────────────────────────────────────────────

    public static class ItemVenta {
        private final int idProducto;
        private final String nombre;
        private final double precio;
        private int cantidad;

        public ItemVenta(int idProducto, String nombre, double precio, int cantidad) {
            this.idProducto = idProducto;
            this.nombre = nombre;
            this.precio = precio;
            this.cantidad = cantidad;
        }

        public int getIdProducto() {
            return idProducto;
        }

        public String getNombre() {
            return nombre;
        }

        public double getPrecio() {
            return precio;
        }

        public int getCantidad() {
            return cantidad;
        }

        public void setCantidad(int c) {
            this.cantidad = c;
        }
    }
}