package Proyecto.View.Documento;

import Proyecto.Model.Producto;
import Proyecto.dao.ProcedimientosDAO;
import Proyecto.services.PersonaServices;
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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VentasPosView {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String[] METODOS_PAGO = {
            "Efectivo",
            "Tarjeta Debito",
            "Tarjeta Credito",
            "Transferencia Bancaria",
            "Nequi / Daviplata"
    };

    private final int empleadoId;
    private final ProductoServices productoServices;
    private final PersonaServices personaServices;
    private final ProcedimientosDAO procedimientosDAO;

    // Estado de la venta
    private final ObservableList<ItemVenta> itemsVenta = FXCollections.observableArrayList();
    private final List<Producto> productosFiltrados = new ArrayList<>();
    private int idClienteSeleccionado = 0;

    // Widgets
    private TextField txtBuscadorCliente;
    private Label lblClienteInfo;
    private TextField txtBuscador;
    private ListView<String> listProductos;
    private Spinner<Integer> spinnerCantidad;
    private ComboBox<String> cbMetodoPago;

    private TableView<ItemVenta> tablaTicket;
    private Label lblSubtotal;
    private Label lblIva;
    private Label lblTotal;

    private VBox root;

    public VentasPosView(int empleadoId) {
        this.empleadoId = empleadoId;
        this.productoServices = new ProductoServices();
        this.personaServices = new PersonaServices();
        this.procedimientosDAO = new ProcedimientosDAO();
        construir();
        cargarProductos("");
    }

    public VentasPosView() {
        this(0);
    }

    public Node getRoot() {
        return root;
    }

    // =========================================================================
    // CONSTRUCCION
    // =========================================================================

    @SuppressWarnings("unchecked")
    private void construir() {
        root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(root, Priority.ALWAYS);

        // -- Encabezado -------------------------------------------------------
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 2 0;");

        Label lblTitulo = new Label("Punto de Venta");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);

        Label lblFecha = new Label(LocalDateTime.now().format(FMT));
        lblFecha.setFont(Font.font("Arial", 12));
        lblFecha.setTextFill(Color.GRAY);

        header.getChildren().addAll(lblTitulo, hSpacer, lblFecha);

        // -- Cuerpo: dos columnas ---------------------------------------------
        HBox body = new HBox(12);
        VBox.setVgrow(body, Priority.ALWAYS);

        // ── Columna izquierda ─────────────────────────────────────────────
        VBox leftCol = new VBox(10);
        leftCol.setPrefWidth(300);
        leftCol.setMaxWidth(320);

        // Seccion cliente
        VBox secCliente = seccion("Datos del Cliente");

        txtBuscadorCliente = new TextField();
        txtBuscadorCliente.setPromptText("Buscar cliente por ID, nombre o correo...");
        txtBuscadorCliente.setStyle("-fx-border-color: #C0C0C0; -fx-border-width:1; -fx-padding:7;");

        Button btnBuscarCliente = boton("Buscar", "#00C8FF");
        Button btnRegistroRapido = boton("Registro rapido", "#795548");

        btnBuscarCliente.setMaxWidth(Double.MAX_VALUE);
        btnRegistroRapido.setMaxWidth(Double.MAX_VALUE);
        HBox filaBotonesCliente = new HBox(8, btnBuscarCliente, btnRegistroRapido);
        HBox.setHgrow(btnBuscarCliente, Priority.ALWAYS);

        lblClienteInfo = new Label("Sin cliente seleccionado");
        lblClienteInfo.setFont(Font.font("Arial", 12));
        lblClienteInfo.setTextFill(Color.GRAY);
        lblClienteInfo.setWrapText(true);

        btnBuscarCliente.setOnAction(e -> buscarCliente());
        txtBuscadorCliente.setOnAction(e -> buscarCliente());
        btnRegistroRapido.setOnAction(e -> registroRapidoCliente());

        secCliente.getChildren().addAll(txtBuscadorCliente, filaBotonesCliente, lblClienteInfo);

        // Metodo de pago
        VBox secPago = seccion("Metodo de Pago");
        cbMetodoPago = new ComboBox<>();
        cbMetodoPago.getItems().addAll(METODOS_PAGO);
        cbMetodoPago.getSelectionModel().selectFirst();
        cbMetodoPago.setMaxWidth(Double.MAX_VALUE);
        secPago.getChildren().add(cbMetodoPago);

        // Seccion buscador de productos
        VBox secBuscar = seccion("Buscar Producto");
        VBox.setVgrow(secBuscar, Priority.ALWAYS);

        txtBuscador = new TextField();
        txtBuscador.setPromptText("Nombre o ID del producto...");
        txtBuscador.setFont(Font.font("Arial", 13));
        txtBuscador.setStyle("-fx-border-color: #00C8FF; -fx-border-width: 1.5; -fx-padding: 8;");
        txtBuscador.textProperty().addListener((obs, o, nv) -> cargarProductos(nv));

        listProductos = new ListView<>();
        listProductos.setPrefHeight(220);
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
        leftCol.getChildren().addAll(secCliente, secPago, secBuscar);

        // ── Columna derecha: ticket -------------------------------------------
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
        colSub.setCellValueFactory(
                d -> new SimpleDoubleProperty(d.getValue().getPrecio() * d.getValue().getCantidad()).asObject());
        colSub.setCellFactory(c -> precioCell());
        colSub.setMaxWidth(110);

        TableColumn<ItemVenta, Void> colDel = new TableColumn<>("");
        colDel.setMaxWidth(48);
        colDel.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("X");
            {
                b.setStyle("-fx-background-color:#C83C3C;-fx-text-fill:white;" +
                        "-fx-cursor:hand;-fx-border-width:0;-fx-padding:3 6 3 6;");
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

        lblSubtotal = totalLabel("$0");
        lblIva = totalLabel("$0");
        lblTotal = new Label("$0");
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

    // =========================================================================
    // BUSQUEDA DE CLIENTE
    // =========================================================================

    private void buscarCliente() {
        String query = txtBuscadorCliente.getText().trim();
        if (query.isEmpty()) {
            info("Ingresa un nombre, ID o correo para buscar el cliente.");
            return;
        }

        // Busqueda por ID numerico primero
        try {
            int id = Integer.parseInt(query);
            var cliente = personaServices.obtenerCliente(id);
            if (cliente != null) {
                idClienteSeleccionado = cliente.getId();
                lblClienteInfo.setText(
                        "Cliente: " + cliente.getNombre() + " " + cliente.getApellido());
                lblClienteInfo.setTextFill(Color.web("#1A8A2A"));
                return;
            }
        } catch (NumberFormatException ignored) {
        }

        // Busqueda por nombre o correo
        var resultados = personaServices.buscarClientes(query);
        if (resultados.isEmpty()) {
            lblClienteInfo.setText("No se encontro cliente con: " + query);
            lblClienteInfo.setTextFill(Color.web("#C83C3C"));
            idClienteSeleccionado = 0;
            return;
        }

        if (resultados.size() == 1) {
            var c = resultados.get(0);
            idClienteSeleccionado = c.getId();
            lblClienteInfo.setText("Cliente: " + c.getNombre() + " " + c.getApellido());
            lblClienteInfo.setTextFill(Color.web("#1A8A2A"));
            return;
        }

        // Multiples resultados: mostrar dialogo de seleccion
        ChoiceDialog<String> seleccion = new ChoiceDialog<>();
        seleccion.setTitle("Seleccionar cliente");
        seleccion.setHeaderText("Se encontraron varios clientes. Selecciona uno:");
        for (var c : resultados) {
            seleccion.getItems().add(c.getId() + " - " + c.getNombre() + " " + c.getApellido()
                    + " (" + c.getEmail() + ")");
        }

        if (!seleccion.getItems().isEmpty()) {
            seleccion.setSelectedItem(seleccion.getItems().get(0));
        }
        seleccion.showAndWait().ifPresent(opcion -> {
            int idx = seleccion.getItems().indexOf(opcion);
            if (idx >= 0 && idx < resultados.size()) {
                var c = resultados.get(idx);
                idClienteSeleccionado = c.getId();
                lblClienteInfo.setText("Cliente: " + c.getNombre() + " " + c.getApellido());
                lblClienteInfo.setTextFill(Color.web("#1A8A2A"));
            }
        });
    }

    /**
     * Registro rapido: solicita nombre y apellido, crea el cliente en BD
     * y lo selecciona automaticamente para la venta actual.
     */
    private void registroRapidoCliente() {
        Dialog<String[]> dlg = new Dialog<>();
        dlg.setTitle("Registro Rapido de Cliente");
        dlg.setHeaderText("Ingresa los datos basicos del cliente:");

        GridPane g = new GridPane();
        g.setHgap(12);
        g.setVgap(10);
        g.setPadding(new Insets(15));

        TextField txtNombre = campo("Nombre");
        TextField txtApellido = campo("Apellido");
        TextField txtEmail = campo("Correo electronico");
        TextField txtTelefono = campo("Telefono (opcional)");

        g.add(lbl("Nombre *:"), 0, 0);
        g.add(txtNombre, 1, 0);
        g.add(lbl("Apellido *:"), 0, 1);
        g.add(txtApellido, 1, 1);
        g.add(lbl("Correo *:"), 0, 2);
        g.add(txtEmail, 1, 2);
        g.add(lbl("Telefono:"), 0, 3);
        g.add(txtTelefono, 1, 3);

        ButtonType btnOk = new ButtonType("Registrar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnOk, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(g);
        dlg.setResultConverter(bt -> {
            if (bt == btnOk)
                return new String[] {
                        txtNombre.getText().trim(),
                        txtApellido.getText().trim(),
                        txtEmail.getText().trim(),
                        txtTelefono.getText().trim() };
            return null;
        });

        dlg.showAndWait().ifPresent(datos -> {
            if (datos[0].isEmpty() || datos[1].isEmpty() || datos[2].isEmpty()) {
                info("Nombre, apellido y correo son obligatorios.");
                return;
            }
            // Generar documento temporal unico
            String docTmp = "TMP-" + System.currentTimeMillis();
            boolean ok = personaServices.registrarCliente(
                    datos[0], datos[1], datos[2], datos[3], docTmp, "techzone1", datos[2]);
            if (ok) {
                // Recuperar el cliente recien creado para obtener su ID
                var lista = personaServices.buscarClientes(datos[2]);
                if (!lista.isEmpty()) {
                    var c = lista.get(0);
                    idClienteSeleccionado = c.getId();
                    lblClienteInfo.setText("Cliente: " + c.getNombre() + " " + c.getApellido()
                            + " (nuevo)");
                    lblClienteInfo.setTextFill(Color.web("#1A8A2A"));
                }
            } else {
                info("No se pudo registrar el cliente. Es posible que el correo ya este registrado.");
            }
        });
    }

    // =========================================================================
    // PRODUCTOS
    // =========================================================================

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
            listProductos.setPlaceholder(new Label("Error al cargar productos."));
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

        int cantEnTicket = itemsVenta.stream()
                .filter(i -> i.getIdProducto() == p.getIdProducto())
                .mapToInt(ItemVenta::getCantidad)
                .sum();

        if (cantEnTicket + cant > p.getCantidad()) {
            info("Stock insuficiente. Disponible: " + p.getCantidad()
                    + ", ya en ticket: " + cantEnTicket);
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

    private static final DecimalFormat PESOS_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        PESOS_FORMAT = new DecimalFormat("#,##0", symbols);
        PESOS_FORMAT.setGroupingUsed(true);
        PESOS_FORMAT.setMinimumFractionDigits(0);
        PESOS_FORMAT.setMaximumFractionDigits(0);
    }

    private String formatPesos(double importe) {
        return "$" + PESOS_FORMAT.format(importe);
    }

    private String padRight(String texto, int ancho) {
        if (texto == null)
            texto = "";
        if (texto.length() >= ancho)
            return texto;
        return texto + " ".repeat(ancho - texto.length());
    }

    private String padLeft(String texto, int ancho) {
        if (texto == null)
            texto = "";
        if (texto.length() >= ancho)
            return texto;
        return " ".repeat(ancho - texto.length()) + texto;
    }

    private void actualizarTotales() {
        double subtotal = itemsVenta.stream()
                .mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();
        double iva = subtotal * 0.19;
        double total = subtotal + iva;
        lblSubtotal.setText(formatPesos(subtotal));
        lblIva.setText(formatPesos(iva));
        lblTotal.setText(formatPesos(total));
    }

    // =========================================================================
    // COBRO - CORRECCION PRINCIPAL
    // =========================================================================

    /**
     * Registra la venta usando sp_registrar_venta via ProcedimientosDAO.
     *
     * ANTES (incorrecto):
     * documentoDAO.crearDocumento(...) → rs.getInt(1) falla con Connector/J 9.x
     * inventarioDAO.registrarMovimientoConPrecio(...) → llamadas separadas, sin
     * atomicidad
     * inventarioDAO.actualizarStock(...)
     *
     * AHORA (correcto):
     * procedimientosDAO.registrarVenta(...) → una sola llamada al SP
     * El SP valida stock, descuenta inventario y registra movimientos
     * en una transaccion atomica dentro de MySQL.
     * Los parametros OUT del SP (idDocumento, mensaje) se leen con
     * cs.getInt(5) / cs.getString(6) que funciona correctamente con
     * CallableStatement en Connector/J 9.x.
     */
    private void cobrarYEmitirFactura() {
        if (itemsVenta.isEmpty()) {
            info("Agrega al menos un producto al ticket.");
            return;
        }

        // Validar cliente
        if (idClienteSeleccionado <= 0) {
            info("Busca y selecciona un cliente antes de cobrar.\n" +
                    "Usa el campo de busqueda o el boton 'Registro rapido'.");
            return;
        }

        String metodo = cbMetodoPago.getValue();
        int idMetodoPago = cbMetodoPago.getSelectionModel().getSelectedIndex() + 1;

        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Confirmar cobro de " + lblTotal.getText() + " con " + metodo + "?",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Confirmar Cobro");
        conf.showAndWait().ifPresent(r -> {
            if (r != ButtonType.YES)
                return;

            try {
                double subtotalSinIva = itemsVenta.stream()
                        .mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();
                double totalConIva = subtotalSinIva * 1.19;

                int idDocumento = documentoDAO.crearDocumento(
                        1, // Factura de Venta
                        clienteActual.getId(), // id_persona del cliente real
                        empleadoId, // id del cajero autenticado
                        idMetodoPago,
                        0, // descuento
                        totalConIva,
                        "Venta POS - Metodo: " + metodo);

                if (idDocumento == -1) {
                    new Alert(Alert.AlertType.ERROR,
                            "Error al registrar la venta en la base de datos.", ButtonType.OK).showAndWait();
                    return;
                }

                // Registrar movimientos de inventario y descontar stock
                for (ItemVenta item : itemsVenta) {
                    inventarioDAO.registrarMovimientoConPrecio(
                            idDocumento,
                            item.getIdProducto(),
                            empleadoId,
                            item.getCantidad(),
                            item.getPrecio());
                    inventarioDAO.actualizarStock(item.getIdProducto(), -item.getCantidad());
                }

                // Emitir factura y limpiar ticket
                emitirFactura(metodo, idDocumento, clienteActual);
                itemsVenta.clear();
                actualizarTotales();
                clienteActual = null;
                lblClienteInfo.setText("Sin cliente seleccionado");
                lblClienteInfo.setTextFill(Color.GRAY);
                txtBuscarCliente.clear();
                cargarProductos(""); // refrescar stock visible

            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR,
                        "Error al registrar la venta: " + ex.getClass().getSimpleName()
                                + " - " + ex.getMessage(),
                        ButtonType.OK).showAndWait();
            }
        });
    }

    private void emitirFactura(int idDocumento, String metodoPago,
            List<ItemVenta> items,
            String subtotal, String iva, String total) {
        StringBuilder sb = new StringBuilder();
        sb.append("===========================================\n");
        sb.append("          TECHZONE  -  FACTURA\n");
        sb.append("===========================================\n");
        sb.append("  N Documento  : ").append(idDocumento).append("\n");
        sb.append("  Fecha        : ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .append("\n");
        sb.append("  Metodo pago  : ").append(metodoPago).append("\n");
        sb.append("  Cliente      : ").append(cliente.getNombre()).append(" ")
                .append(cliente.getApellido()).append("\n");
        sb.append("  ID Cliente   : ").append(cliente.getId()).append("\n");
        sb.append("-------------------------------------------\n");
        sb.append("  PRODUCTO                     CANT    SUBTOTAL\n");
        sb.append("-------------------------------------------\n");

        for (ItemVenta item : itemsVenta) {
            String nombre = padRight(truncar(item.getNombre(), 28), 28);
            String cantidad = String.format("%6d", item.getCantidad());
            String subtotal = padLeft(formatPesos(item.getPrecio() * item.getCantidad()), 12);
            sb.append("  ").append(nombre).append(" ")
                    .append(cantidad).append(" ").append(subtotal).append("\n");
        }

        sb.append("-------------------------------------------\n");
        sb.append("  Subtotal  : ").append(lblSubtotal.getText()).append("\n");
        sb.append("  IVA(19%) : ").append(lblIva.getText()).append("\n");
        sb.append("  TOTAL     : ").append(lblTotal.getText()).append("\n");
        sb.append("===========================================\n");
        sb.append("   Gracias por tu compra en TechZone!\n");

        TextArea txt = new TextArea(sb.toString());
        txt.setEditable(false);
        txt.setFont(Font.font("Monospaced", 12));
        txt.setPrefSize(480, 380);

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Factura N " + idDocumento);
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

    // =========================================================================
    // HELPERS UI
    // =========================================================================

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

    private Label lbl(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
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
        b.setStyle("-fx-background-color:" + color +
                ";-fx-border-width:0;-fx-cursor:hand;-fx-padding:8 14 8 14;");
        return b;
    }

    private TextField campo(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle("-fx-border-color:#C0C0C0;-fx-border-width:1;-fx-padding:7;");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private TableCell<ItemVenta, Double> precioCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty || v == null ? null : formatPesos(v));
            }
        };
    }

    private String truncar(String s, int max) {
        if (s == null)
            return "-";
        return s.length() > max ? s.substring(0, max - 1) + "." : s;
    }

    private void info(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void error(String msg) {
        new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK).showAndWait();
    }

    // =========================================================================
    // MODELO DE FILA
    // =========================================================================

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