package Proyecto.View.Producto;

import Proyecto.Model.Producto;
import Proyecto.services.ProductoServices;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;

/**
 * ProductoView — CORRECCIÓN APLICADA:
 *
 * BUG: procesarCompra() parseaba el precio con:
 *   Double.parseDouble(fila.getPrecio().replace("$", ""))
 *
 * En sistemas con locale español (Colombia), los números se formatean con
 * coma decimal y punto como separador de miles, por ejemplo: "$2.800.000,00"
 * Double.parseDouble() solo acepta punto decimal → lanzaba NumberFormatException
 * → Alert "Por favor ingrese un número válido" que se veía en la imagen.
 *
 * FIX: se guarda el precio como double en FilaProducto además del String
 * formateado para display. El cálculo usa el double directamente,
 * eliminando por completo el parseo del String formateado.
 */
public class ProductoView {

    private final ProductoServices productoServices;

    private TableView<FilaProducto> tablaProductos;
    private ObservableList<FilaProducto> datos;
    private TextField txtBuscar;
    private VBox root;

    public ProductoView() {
        this.productoServices = new ProductoServices();
        this.datos = FXCollections.observableArrayList();
        initComponents();
        cargarProductos();
    }

    public Node getRoot() { return root; }

    @SuppressWarnings("unchecked")
    private void initComponents() {
        root = new VBox(15);
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(root, Priority.ALWAYS);

        // ── Panel superior ─────────────────────────────────────────────────
        HBox topPanel = new HBox(10);
        topPanel.setAlignment(Pos.CENTER_LEFT);

        Label lblTitulo = new Label("Catálogo de Productos");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        lblTitulo.setTextFill(Color.web("#0A1933"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar producto...");
        txtBuscar.setPrefWidth(220);
        txtBuscar.setStyle(
                "-fx-border-color: #C8C8C8;" +
                "-fx-border-width: 1;" +
                "-fx-padding: 8;");

        Button btnBuscar = crearBoton("Buscar", "#00C8FF");
        btnBuscar.setOnAction(e -> buscarProductos());

        topPanel.getChildren().addAll(lblTitulo, spacer, txtBuscar, btnBuscar);

        // ── Tabla ──────────────────────────────────────────────────────────
        tablaProductos = new TableView<>();
        tablaProductos.setItems(datos);
        tablaProductos.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        VBox.setVgrow(tablaProductos, Priority.ALWAYS);

        TableColumn<FilaProducto, Integer> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setMaxWidth(60);

        TableColumn<FilaProducto, String> colNombre = new TableColumn<>("Producto");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));

        TableColumn<FilaProducto, String> colCategoria = new TableColumn<>("Categoría");
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));

        TableColumn<FilaProducto, String> colPrecio = new TableColumn<>("Precio");
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioDisplay"));
        colPrecio.setMaxWidth(130);

        TableColumn<FilaProducto, Integer> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStock.setMaxWidth(80);

        TableColumn<FilaProducto, Void> colAccion = new TableColumn<>("Acción");
        colAccion.setMaxWidth(130);
        colAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = crearBoton("🛒 Comprar", "#00C8FF");
            {
                btn.setOnAction(e -> {
                    FilaProducto fila = getTableView().getItems().get(getIndex());
                    procesarCompra(fila);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tablaProductos.getStylesheets().add(
                "data:text/css," +
                ".table-view .column-header-background { -fx-background-color: #0A1933; }" +
                ".table-view .column-header .label { -fx-text-fill: white; -fx-font-weight: bold; }");

        tablaProductos.getColumns().addAll(colId, colNombre, colCategoria, colPrecio, colStock, colAccion);
        tablaProductos.setRowFactory(tv -> {
            TableRow<FilaProducto> row = new TableRow<>();
            row.setStyle("-fx-cell-size: 40px;");
            return row;
        });

        // ── Panel inferior ─────────────────────────────────────────────────
        HBox bottomPanel = new HBox();
        bottomPanel.setAlignment(Pos.CENTER);
        Button btnActualizar = crearBoton("Actualizar", "#646464");
        btnActualizar.setOnAction(e -> cargarProductos());
        bottomPanel.getChildren().add(btnActualizar);

        root.getChildren().addAll(topPanel, tablaProductos, bottomPanel);
    }

    // ── Carga de datos ─────────────────────────────────────────────────────────
    private void cargarProductos() {
        datos.clear();
        List<Producto> productos = productoServices.obtenerTodosLosProductos();
        for (Producto p : productos) {
            if (p.getActivo()) {
                datos.add(new FilaProducto(
                        p.getIdProducto(),
                        p.getNombre(),
                        p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                        p.getPrecioVenta(),   // double real para cálculos
                        p.getCantidad()));
            }
        }
    }

    private void buscarProductos() {
        String busqueda = txtBuscar.getText().trim();
        datos.clear();
        List<Producto> productos = busqueda.isEmpty()
                ? productoServices.obtenerTodosLosProductos()
                : productoServices.buscarProductos(busqueda);

        for (Producto p : productos) {
            if (p.getActivo()) {
                datos.add(new FilaProducto(
                        p.getIdProducto(),
                        p.getNombre(),
                        p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                        p.getPrecioVenta(),
                        p.getCantidad()));
            }
        }
    }

    // ── Lógica de compra ───────────────────────────────────────────────────────
    private void procesarCompra(FilaProducto fila) {
        if (fila.getStock() <= 0) {
            new Alert(Alert.AlertType.WARNING,
                    "No hay stock disponible de este producto.", ButtonType.OK).showAndWait();
            return;
        }

        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("Agregar al Carrito");
        dialog.setHeaderText(
                "Producto: " + fila.getNombre() +
                "\nPrecio: " + fila.getPrecioDisplay() +
                "\nStock disponible: " + fila.getStock());
        dialog.setContentText("Ingrese la cantidad:");

        dialog.showAndWait().ifPresent(cantStr -> {
            try {
                int cantidad = Integer.parseInt(cantStr.trim());

                if (cantidad <= 0) {
                    showError("La cantidad debe ser mayor a 0.");
                    return;
                }
                if (cantidad > fila.getStock()) {
                    showError("Stock insuficiente. Disponible: " + fila.getStock());
                    return;
                }

                // CORRECCIÓN: usa fila.getPrecioDouble() directamente, sin parsear el String
                double subtotal = cantidad * fila.getPrecioDouble();

                String mensaje = String.format(
                        "✓ Producto agregado al carrito:\n" +
                        "ID: %d\n" +
                        "Producto: %s\n" +
                        "Cantidad: %d\n" +
                        "Precio unitario: %s\n" +
                        "Subtotal: $%,.2f",
                        fila.getId(),
                        fila.getNombre(),
                        cantidad,
                        fila.getPrecioDisplay(),
                        subtotal);

                new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK).showAndWait();
                // Aquí puedes llamar: carritoServices.agregarProductoAlCarrito(idCliente, fila.getId(), cantidad);

            } catch (NumberFormatException ex) {
                showError("Por favor ingrese un número entero válido.");
            }
        });
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setTitle("Error");
        a.showAndWait();
    }

    private Button crearBoton(String texto, String color) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        btn.setTextFill(Color.WHITE);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-border-width: 0;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 7 14 7 14;");
        return btn;
    }

    // ── Modelo de fila ─────────────────────────────────────────────────────────
    // CORRECCIÓN: se agrega precioDouble para usar en cálculos sin parsear el String
    public static class FilaProducto {
        private final int id;
        private final String nombre;
        private final String categoria;
        private final double precioDouble;   // valor real para cálculos
        private final String precioDisplay;  // String formateado solo para mostrar
        private final int stock;

        public FilaProducto(int id, String nombre, String categoria, double precio, int stock) {
            this.id = id;
            this.nombre = nombre;
            this.categoria = categoria;
            this.precioDouble = precio;
            // Formato colombiano con punto como separador de miles
            this.precioDisplay = String.format("$%,.2f", precio);
            this.stock = stock;
        }

        public int    getId()           { return id; }
        public String getNombre()       { return nombre; }
        public String getCategoria()    { return categoria; }
        public double getPrecioDouble() { return precioDouble; }
        public String getPrecioDisplay(){ return precioDisplay; }
        public int    getStock()        { return stock; }

        // Alias para PropertyValueFactory de la columna "precioDisplay"
        public String getPrecio()       { return precioDisplay; }
    }
}
