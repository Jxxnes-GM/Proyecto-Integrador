package Proyecto.View.Producto;

import Proyecto.Model.Producto;
import Proyecto.services.ProductoServices;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class ProductoView extends JPanel {
    
    private ProductoServices productoServices;
    private JTable tablaProductos;
    private DefaultTableModel modeloTabla;
    private JTextField txtBuscar;
    private int idCliente;
    
    public ProductoView(int idCliente) {
        this.idCliente = idCliente;
        this.productoServices = new ProductoServices();
        initComponents();
        cargarProductos();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Panel superior
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel lblTitulo = new JLabel("Catálogo de Productos");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(10, 25, 47));
        
        // Panel de búsqueda
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(Color.WHITE);
        
        txtBuscar = new JTextField(20);
        txtBuscar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JButton btnBuscar = new JButton("🔍 Buscar");
        btnBuscar.setBackground(new Color(0, 200, 255));
        btnBuscar.setForeground(Color.WHITE);
        btnBuscar.setBorderPainted(false);
        btnBuscar.addActionListener(e -> buscarProductos());
        
        searchPanel.add(txtBuscar);
        searchPanel.add(btnBuscar);
        
        topPanel.add(lblTitulo, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Tabla de productos
        String[] columnas = {"ID", "Producto", "Categoría", "Precio", "Stock", "Acciones"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };
        
        tablaProductos = new JTable(modeloTabla);
        tablaProductos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaProductos.getColumnModel().getColumn(1).setPreferredWidth(250);
        tablaProductos.getColumnModel().getColumn(2).setPreferredWidth(150);
        tablaProductos.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaProductos.getColumnModel().getColumn(4).setPreferredWidth(80);
        tablaProductos.getColumnModel().getColumn(5).setPreferredWidth(120);
        
        tablaProductos.setRowHeight(40);
        tablaProductos.setFont(new Font("Arial", Font.PLAIN, 12));
        tablaProductos.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        tablaProductos.getTableHeader().setBackground(new Color(10, 25, 47));
        tablaProductos.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scroll = new JScrollPane(tablaProductos);
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);
        
        // Panel inferior
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(Color.WHITE);
        
        JButton btnActualizar = new JButton("🔄 Actualizar");
        btnActualizar.setBackground(new Color(100, 100, 100));
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setBorderPainted(false);
        btnActualizar.addActionListener(e -> cargarProductos());
        
        bottomPanel.add(btnActualizar);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Configurar renderizado de botones en la tabla
        tablaProductos.getColumn("Acciones").setCellRenderer(new ButtonRenderer());
        tablaProductos.getColumn("Acciones").setCellEditor(new ButtonEditor(new JCheckBox(), tablaProductos, modeloTabla));
    }
    
    private void cargarProductos() {
        modeloTabla.setRowCount(0);
        List<Producto> productos = productoServices.obtenerTodosLosProductos();
        
        for (Producto p : productos) {
            if (p.getActivo()) {
                Object[] fila = {
                    p.getIdProducto(),
                    p.getNombre(),
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                    "$" + String.format("%.2f", p.getPrecioVenta()),
                    p.getCantidad(),
                    "Comprar"
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
    private void buscarProductos() {
        String busqueda = txtBuscar.getText().trim();
        modeloTabla.setRowCount(0);
        
        List<Producto> productos;
        if (busqueda.isEmpty()) {
            productos = productoServices.obtenerTodosLosProductos();
        } else {
            productos = productoServices.buscarProductos(busqueda);
        }
        
        for (Producto p : productos) {
            if (p.getActivo()) {
                Object[] fila = {
                    p.getIdProducto(),
                    p.getNombre(),
                    p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría",
                    "$" + String.format("%.2f", p.getPrecioVenta()),
                    p.getCantidad(),
                    "Comprar"
                };
                modeloTabla.addRow(fila);
            }
        }
    }
    
    // Renderizador de botones
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(new Color(0, 200, 255));
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 11));
            setBorderPainted(false);
            setFocusPainted(false);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Comprar" : value.toString());
            return this;
        }
    }
    
    // Editor de botones - VERSIÓN CORREGIDA (idProducto AHORA SÍ SE USA)
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private JTable table;
        private DefaultTableModel tableModel;
        private int selectedRow;
        
        public ButtonEditor(JCheckBox checkBox, JTable table, DefaultTableModel tableModel) {
            super(checkBox);
            this.table = table;
            this.tableModel = tableModel;
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(0, 200, 255));
            button.setForeground(Color.WHITE);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.selectedRow = row;
            label = (value == null) ? "Comprar" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                int row = this.selectedRow;
                
                if (row >= 0) {
                    try {
                        // Obtener datos de la fila seleccionada
                        int idProducto = (int) tableModel.getValueAt(row, 0);
                        String nombreProducto = (String) tableModel.getValueAt(row, 1);
                        String precioStr = (String) tableModel.getValueAt(row, 3);
                        int stockDisponible = (int) tableModel.getValueAt(row, 4);
                        
                        // Validar stock
                        if (stockDisponible <= 0) {
                            JOptionPane.showMessageDialog(null,
                                "No hay stock disponible de este producto",
                                "Sin Stock",
                                JOptionPane.WARNING_MESSAGE);
                            return label;
                        }
                        
                        // Solicitar cantidad
                        String cantidadStr = JOptionPane.showInputDialog(
                            null,
                            "Producto: " + nombreProducto + 
                            "\nPrecio: " + precioStr + 
                            "\nStock disponible: " + stockDisponible + 
                            "\nID Producto: " + idProducto +
                            "\n\nIngrese la cantidad:",
                            "Agregar al Carrito",
                            JOptionPane.QUESTION_MESSAGE
                        );
                        
                        if (cantidadStr != null && !cantidadStr.trim().isEmpty()) {
                            try {
                                int cantidad = Integer.parseInt(cantidadStr.trim());
                                if (cantidad > 0) {
                                    if (cantidad <= stockDisponible) {
                                        // AHORA SÍ USAMOS idProducto
                                        String mensaje = String.format(
                                            "✓ Producto agregado al carrito:\n" +
                                            "ID: %d\n" +
                                            "Producto: %s\n" +
                                            "Cantidad: %d\n" +
                                            "Precio unitario: %s\n" +
                                            "Subtotal: $%.2f",
                                            idProducto,
                                            nombreProducto,
                                            cantidad,
                                            precioStr,
                                            cantidad * Double.parseDouble(precioStr.replace("$", ""))
                                        );
                                        
                                        JOptionPane.showMessageDialog(null, 
                                            mensaje,
                                            "Éxito",
                                            JOptionPane.INFORMATION_MESSAGE);
                                            
                                        // Aquí puedes llamar al servicio para agregar al carrito
                                        // agregarAlCarrito(idCliente, idProducto, cantidad);
                                        
                                    } else {
                                        JOptionPane.showMessageDialog(null, 
                                            "Stock insuficiente. Disponible: " + stockDisponible,
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
                                    }
                                } else {
                                    JOptionPane.showMessageDialog(null, 
                                        "La cantidad debe ser mayor a 0", 
                                        "Error", 
                                        JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null, 
                                    "Por favor ingrese un número válido", 
                                    "Error", 
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, 
                            "Error al procesar la solicitud: " + ex.getMessage(), 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            isPushed = false;
            return label;
        }
        
        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
        
        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}