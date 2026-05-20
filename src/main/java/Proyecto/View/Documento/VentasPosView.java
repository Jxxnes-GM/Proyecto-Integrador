package Proyecto.View.Documento;

import Proyecto.Model.Cliente;
import Proyecto.Model.Producto;
import Proyecto.dao.DocumentoDAO;
import Proyecto.dao.InventarioDAO;
import Proyecto.services.PersonaServices;
import Proyecto.services.ProductoServices;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
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

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final int empleadoId;
    private final ProductoServices productoServices;
    private final PersonaServices personaServices;
    private final DocumentoDAO documentoDAO;
    private final InventarioDAO inventarioDAO;

    private ObservableList<ItemVenta> itemsVenta;
    private List<Producto> productosFiltrados;

    private Cliente clienteSeleccionado = null;

    // Widgets de cliente
    private TextField txtBuscarCliente;
    private Label lblClienteInfo;

    // Widgets de productos
    private TextField txtBuscador;
    private ListView<String> listProductos;
    private Spinner<Integer> spinnerCantidad;
    private ComboBox<String> cbMetodoPago;

    // Ticket
    private TableView<ItemVenta> tablaTicket;
    private Label lblSubtotal;
    private Label lblIva;
    private Label lblTotal;

    private VBox root;

    private static final String[] METODOS_PAGO = {
            "Efectivo", "Tarjeta Debito", "Tarjeta Credito",
            "Transferencia Bancaria", "Nequi / Daviplata"
    };

    public VentasPosView(int empleadoId) {
        this.empleadoId = empleadoId;
        this.productoServices = new ProductoServices();
        this.personaServices = new PersonaServices();
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
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        header.setStyle("-fx-border-color: #E0E0E0; -fx-border-width: 0 0 2 0;");
        Label lblTitulo = new Label("Punto de Venta - Cajero");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#0A1933"));
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        Label lblFecha = new Label(LocalDateTime.now().format(FMT));
        lblFecha.setFont(Font.font("Arial", 12));
        lblFecha.setTextFill(Color.GRAY);
        header.getChildren().addAll(lblTitulo, hSpacer, lblFecha);

        HBox body = new HBox(12);
        VBox.setVgrow(body, Priority.ALWAYS);

        // ── Columna izquierda ─────────────────────────────────────────────
        VBox leftCol = new VBox(10);
        leftCol.setPrefWidth(310);
        leftCol.setMaxWidth(330);

        // Seccion cliente
        VBox secCliente = seccion("Cliente de la venta (obligatorio)");

        txtBuscarCliente = new TextField();
        txtBuscarCliente.setPromptText("Buscar por nombre, email o documento...");
        estiloCampo(txtBuscarCliente);

        Button btnBuscarCliente = boton("Buscar", "#00C8FF");
        Button btnNuevoCliente = boton("Nuevo cliente", "#1A8A2A");

        btnBuscarCliente.setOnAction(e -> buscarCliente());
        btnNuevoCliente.setOnAction(e -> abrirFormularioNuevoCliente());

        HBox buscarRow = new HBox(8, txtBuscarCliente, btnBuscarCliente);
        HBox.setHgrow(txtBuscarCliente, Priority.ALWAYS);

        lblClienteInfo = new Label("Sin cliente seleccionado");
        lblClienteInfo.setFont(Font.font("Arial", 12));
        lblClienteInfo.setTextFill(Color.GRAY);
        lblClienteInfo.setWrapText(true);

        secCliente.getChildren().addAll(buscarRow, btnNuevoCliente, lblClienteInfo);

        // Seccion metodo de pago
        VBox secPago = seccion("Metodo de pago");
        cbMetodoPago = new ComboBox<>();
        cbMetodoPago.getItems().addAll(METODOS_PAGO);
        cbMetodoPago.getSelectionModel().selectFirst();
        cbMetodoPago.setMaxWidth(Double.MAX_VALUE);
        secPago.getChildren().add(cbMetodoPago);

        // Seccion buscar productos
        VBox secBuscar = seccion("Agregar producto");
        VBox.setVgrow(secBuscar, Priority.ALWAYS);

        txtBuscador = new TextField();
        txtBuscador.setPromptText("Nombre o ID...");
        estiloCampo(txtBuscador);
        txtBuscador.textProperty().addListener((obs, o, nv) -> cargarProductos(nv));

        listProductos = new ListView<>();
        listProductos.setPrefHeight(220);
        VBox.setVgrow(listProductos, Priority.ALWAYS);
        listProductos.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2)
                agregarProducto();
        });

        spinnerCantidad = new Spinner<>(1, 9999, 1);
        spinnerCantidad.setEditable(true);
        spinnerCantidad.setPrefWidth(90);

        Button btnAgregar = boton("Agregar al ticket", "#1A8A2A");
        btnAgregar.setMaxWidth(Double.MAX_VALUE);
        btnAgregar.setOnAction(e -> agregarProducto());

        HBox cantRow = new HBox(8, new Label("Cantidad:") {
            {
                setFont(Font.font("Arial", FontWeight.BOLD, 12));
            }
        }, spinnerCantidad);
        cantRow.setAlignment(Pos.CENTER_LEFT);

        secBuscar.getChildren().addAll(txtBuscador, listProductos, cantRow, btnAgregar);
        leftCol.getChildren().addAll(secCliente, secPago, secBuscar);

        // ── Columna derecha — Ticket ───────────────────────────────────────
        VBox rightCol = new VBox(10);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox secTicket = seccion("Ticket de venta");
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
        Button btnCancelar = boton("Cancelar", "#646464");
        Button btnCobrar = boton("Cobrar y emitir factura", "#1A8A2A");
        btnCobrar.setPrefHeight(42);
        btnCobrar.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btnCancelar.setOnAction(e -> cancelarVenta());
        btnCobrar.setOnAction(e -> confirmarYCobrar());

        HBox btnCobro = new HBox(10, btnCancelar, btnCobrar);
        btnCobro.setAlignment(Pos.CENTER_RIGHT);

        rightCol.getChildren().addAll(secTicket, btnCobro);
        body.getChildren().addAll(leftCol, rightCol);
        root.getChildren().addAll(header, body);
    }

    // =========================================================================
    // BUSQUEDA Y REGISTRO DE CLIENTE
    // =========================================================================

    private void buscarCliente() {
        String query = txtBuscarCliente.getText().trim();
        if (query.isEmpty()) {
            info("Ingresa un nombre, email o documento para buscar.");
            return;
        }

        List<Cliente> encontrados = personaServices.buscarClientes(query);

        if (encontrados.isEmpty()) {
            lblClienteInfo.setText("No se encontro ningun cliente con: \"" + query
                    + "\".\nUsa el boton \"Nuevo cliente\" para registrarlo.");
            lblClienteInfo.setTextFill(Color.web("#C83C3C"));
            clienteSeleccionado = null;
            return;
        }

        if (encontrados.size() == 1) {
            seleccionarCliente(encontrados.get(0));
            return;
        }

        // Mas de un resultado: mostrar lista de seleccion
        ChoiceDialog<String> dlg = new ChoiceDialog<>();
        dlg.setTitle("Seleccionar cliente");
        dlg.setHeaderText("Se encontraron " + encontrados.size() + " clientes. Selecciona uno:");
        List<String> opciones = new ArrayList<>();
        for (Cliente c : encontrados)
            opciones.add(c.getNombre() + " " + c.getApellido() + " | " + c.getEmail());
        dlg.getItems().addAll(opciones);
        dlg.setSelectedItem(opciones.get(0));
        dlg.showAndWait().ifPresent(sel -> {
            int idx = opciones.indexOf(sel);
            if (idx >= 0)
                seleccionarCliente(encontrados.get(idx));
        });
    }

    private void seleccionarCliente(Cliente c) {
        clienteSeleccionado = c;
        lblClienteInfo.setText("Cliente: " + c.getNombre() + " " + c.getApellido() +
                "\nEmail: " + c.getEmail() +
                "\nID: " + c.getId());
        lblClienteInfo.setTextFill(Color.web("#1A8A2A"));
    }

    /**
     * Abre un formulario modal para registrar un nuevo cliente directamente
     * desde la interfaz del cajero sin salir del POS.
     */
    private void abrirFormularioNuevoCliente() {
        Dialog<Cliente> dlg = new Dialog<>();
        dlg.setTitle("Registrar nuevo cliente");
        dlg.setResizable(false);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        ColumnConstraints c0 = new ColumnConstraints(130);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(c0, c1);

        int fila = 0;

        Label lblTitulo = new Label("NUEVO CLIENTE");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        lblTitulo.setTextFill(Color.web("#0A1933"));
        GridPane.setColumnSpan(lblTitulo, 2);
        GridPane.setHalignment(lblTitulo, HPos.CENTER);
        grid.add(lblTitulo, 0, fila++);

        TextField txtNombres = campoForm("Nombres completos");
        TextField txtApellidos = campoForm("Apellidos completos");
        TextField txtEmail = campoForm("correo@ejemplo.com");
        TextField txtTelefono = campoForm("Ej: 3001234567");
        TextField txtDocumento = campoForm("Numero de cedula / DNI");
        TextField txtDireccion = campoForm("Direccion (opcional)");
        PasswordField txtPass = new PasswordField();
        txtPass.setPromptText("Contrasena temporal (min. 6 caracteres)");
        estiloCampo(txtPass);

        Object[][] rows = {
                { "Nombres *", txtNombres },
                { "Apellidos *", txtApellidos },
                { "Email *", txtEmail },
                { "Telefono", txtTelefono },
                { "Documento *", txtDocumento },
                { "Direccion", txtDireccion },
                { "Contrasena *", txtPass },
        };
        for (Object[] r : rows) {
            grid.add(etiqueta((String) r[0]), 0, fila);
            grid.add((Control) r[1], 1, fila++);
        }

        Label lblMsg = new Label("");
        lblMsg.setFont(Font.font("Arial", 11));
        lblMsg.setTextFill(Color.web("#C83C3C"));
        lblMsg.setWrapText(true);
        GridPane.setColumnSpan(lblMsg, 2);
        GridPane.setHalignment(lblMsg, HPos.CENTER);
        grid.add(lblMsg, 0, fila);

        ButtonType btnGuardar = new ButtonType("Registrar cliente", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btnGuardar, ButtonType.CANCEL);
        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().setPrefWidth(460);

        Button okBtn = (Button) dlg.getDialogPane().lookupButton(btnGuardar);
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String nombre = txtNombres.getText().trim();
            String apellido = txtApellidos.getText().trim();
            String emailVal = txtEmail.getText().trim();
            String documento = txtDocumento.getText().trim();
            String password = txtPass.getText();

            if (nombre.isEmpty()) {
                lblMsg.setText("Nombres es obligatorio.");
                event.consume();
                return;
            }
            if (apellido.isEmpty()) {
                lblMsg.setText("Apellidos es obligatorio.");
                event.consume();
                return;
            }
            if (!emailVal.contains("@")) {
                lblMsg.setText("Email invalido.");
                event.consume();
                return;
            }
            if (documento.isEmpty()) {
                lblMsg.setText("Documento es obligatorio.");
                event.consume();
                return;
            }
            if (password.length() < 6) {
                lblMsg.setText("La contrasena debe tener al menos 6 caracteres.");
                event.consume();
                return;
            }

            boolean ok = personaServices.registrarCliente(
                    nombre, apellido, emailVal,
                    txtTelefono.getText().trim(),
                    documento, password,
                    txtDireccion.getText().trim());

            if (!ok) {
                lblMsg.setText("No se pudo registrar. Verifique que email y documento no esten ya registrados.");
                event.consume();
            }
        });

        dlg.setResultConverter(bt -> {
            if (bt == btnGuardar) {
                // Recuperar el cliente recien creado
                return personaServices.buscarClientes(txtEmail.getText().trim())
                        .stream().findFirst().orElse(null);
            }
            return null;
        });

        dlg.showAndWait().ifPresent(c -> {
            if (c != null) {
                seleccionarCliente(c);
                info("Cliente registrado y seleccionado correctamente.");
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
            List<Producto> todos = query == null || query.isEmpty()
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
                .mapToInt(ItemVenta::getCantidad).sum();

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
        double subtotal = itemsVenta.stream().mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();
        double iva = subtotal * 0.19;
        double total = subtotal + iva;
        lblSubtotal.setText(String.format("$%,.0f", subtotal));
        lblIva.setText(String.format("$%,.0f", iva));
        lblTotal.setText(String.format("$%,.0f", total));
    }

    // =========================================================================
    // CONFIRMACION Y REGISTRO DE VENTA
    // =========================================================================

    private void confirmarYCobrar() {
        // Validar que el carrito no este vacio
        if (itemsVenta.isEmpty()) {
            info("Agrega al menos un producto al ticket.");
            return;
        }

        // Validar que haya un cliente seleccionado
        if (clienteSeleccionado == null) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Cliente requerido");
            alerta.setHeaderText("No hay un cliente seleccionado.");
            alerta.setContentText("Busca al cliente por nombre, email o documento.\n" +
                    "Si no esta registrado, usa el boton \"Nuevo cliente\".");
            alerta.showAndWait();
            return;
        }

        String metodo = cbMetodoPago.getValue();
        int idMetodoPago = cbMetodoPago.getSelectionModel().getSelectedIndex() + 1;

        double subtotalSinIva = itemsVenta.stream()
                .mapToDouble(i -> i.getPrecio() * i.getCantidad()).sum();
        double totalConIva = subtotalSinIva * 1.19;

        // Mostrar dialogo de confirmacion con resumen
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION);
        conf.setTitle("Confirmar venta");
        conf.setHeaderText("Resumen de la venta");
        conf.setContentText(
                "Cliente:       " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido() + "\n" +
                        "ID Cliente:    " + clienteSeleccionado.getId() + "\n" +
                        "Metodo pago:   " + metodo + "\n" +
                        "Total (c/IVA): " + String.format("$%,.0f", totalConIva) + "\n\n" +
                        "Confirmas la venta?");
        conf.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        conf.showAndWait().ifPresent(r -> {
            if (r != ButtonType.YES)
                return;
            registrarVentaEnBD(idMetodoPago, metodo, subtotalSinIva, totalConIva);
        });
    }

    private void registrarVentaEnBD(int idMetodoPago, String metodoNombre,
            double subtotalSinIva, double totalConIva) {
        try {
            // 1. Crear documento de venta
            int idDocumento = documentoDAO.crearDocumento(
                    1, // Factura de Venta
                    clienteSeleccionado.getId(),
                    empleadoId,
                    0, // descuento
                    totalConIva,
                    "Venta POS - Metodo: " + metodoNombre);

            if (idDocumento == -1) {
                new Alert(Alert.AlertType.ERROR,
                        "Error al registrar la venta en la base de datos.",
                        ButtonType.OK).showAndWait();
                return;
            }

            // 2. Registrar movimientos de inventario y descontar stock
            for (ItemVenta item : itemsVenta) {
                inventarioDAO.registrarMovimientoConPrecio(
                        idDocumento,
                        item.getIdProducto(),
                        empleadoId,
                        item.getCantidad(),
                        item.getPrecio());

                inventarioDAO.actualizarStock(item.getIdProducto(), -item.getCantidad());
            }

            // 3. Emitir factura y limpiar
            emitirFactura(metodoNombre, idDocumento, totalConIva);
            limpiarPostVenta();

        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "Error al registrar la venta: " + ex.getMessage(),
                    ButtonType.OK).showAndWait();
        }
    }

    private void emitirFactura(String metodoPago, int idDocumento, double totalConIva) {
        StringBuilder sb = new StringBuilder();
        sb.append("===========================================\n");
        sb.append("          TECHZONE  -  FACTURA\n");
        sb.append("===========================================\n");
        sb.append(String.format("  N Documento   : %d%n", idDocumento));
        sb.append(String.format("  Fecha         : %s%n", LocalDateTime.now().format(FMT)));
        sb.append(String.format("  Cliente       : %s %s%n",
                clienteSeleccionado.getNombre(), clienteSeleccionado.getApellido()));
        sb.append(String.format("  ID Cliente    : %d%n", clienteSeleccionado.getId()));
        sb.append(String.format("  Metodo pago   : %s%n", metodoPago));
        sb.append("-------------------------------------------\n");
        sb.append(String.format("  %-28s %6s %12s%n", "PRODUCTO", "CANT", "SUBTOTAL"));
        sb.append("-------------------------------------------\n");

        double subtotal = 0;
        for (ItemVenta item : itemsVenta) {
            double sub = item.getPrecio() * item.getCantidad();
            subtotal += sub;
            sb.append(String.format("  %-28s %6d %12,.0f%n",
                    truncar(item.getNombre(), 28), item.getCantidad(), sub));
        }

        sb.append("-------------------------------------------\n");
        sb.append(String.format("  Subtotal  : $%,.0f%n", subtotal));
        sb.append(String.format("  IVA (19%%) : $%,.0f%n", totalConIva - subtotal));
        sb.append(String.format("  TOTAL     : $%,.0f%n", totalConIva));
        sb.append("===========================================\n");
        sb.append("   Gracias por su compra en TechZone!\n");

        TextArea txt = new TextArea(sb.toString());
        txt.setEditable(false);
        txt.setFont(Font.font("Monospaced", 12));
        txt.setPrefSize(500, 420);

        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Factura N " + idDocumento);
        dlg.getDialogPane().setContent(new ScrollPane(txt));
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.showAndWait();
    }

    private void limpiarPostVenta() {
        itemsVenta.clear();
        clienteSeleccionado = null;
        txtBuscarCliente.clear();
        lblClienteInfo.setText("Sin cliente seleccionado");
        lblClienteInfo.setTextFill(Color.GRAY);
        actualizarTotales();
        cargarProductos(""); // Refrescar stock visible
    }

    private void cancelarVenta() {
        if (itemsVenta.isEmpty() && clienteSeleccionado == null)
            return;
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancelar la venta y limpiar el ticket?",
                ButtonType.YES, ButtonType.NO);
        conf.setTitle("Cancelar venta");
        conf.showAndWait().ifPresent(r -> {
            if (r == ButtonType.YES)
                limpiarPostVenta();
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
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#0A1933"));
        sec.getChildren().add(lbl);
        return sec;
    }

    private void estiloCampo(Control c) {
        c.setStyle("-fx-border-color: #C0C0C0; -fx-border-width: 1; -fx-padding: 7; -fx-font-size: 12px;");
    }

    private TextField campoForm(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        estiloCampo(tf);
        return tf;
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
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