package Proyecto.View.Producto;

import Proyecto.Model.Categoria;
import Proyecto.services.CategoriaServices;
import Proyecto.services.ProductoServices;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ProductoFormView extends JDialog {
    
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JComboBox<Categoria> cbCategoria;
    private JTextField txtPrecioCompra;
    private JTextField txtPrecioVenta;
    private JTextField txtStock;
    private JButton btnGuardar;
    private JButton btnCancelar;
    
    private ProductoServices productoServices;
    private CategoriaServices categoriaServices;
    private boolean editando;
    private Integer idProducto;
    private boolean guardadoExitoso;
    
    // Constructor para nuevo producto
    public ProductoFormView(JFrame parent) {
        super(parent, "Nuevo Producto", true);
        this.productoServices = new ProductoServices();
        this.categoriaServices = new CategoriaServices();
        this.editando = false;
        this.guardadoExitoso = false;
        initComponents();
        cargarCategorias();
    }
    
    // Constructor para editar producto
    public ProductoFormView(JFrame parent, int idProducto) {
        super(parent, "Editar Producto", true);
        this.productoServices = new ProductoServices();
        this.categoriaServices = new CategoriaServices();
        this.editando = true;
        this.idProducto = idProducto;
        this.guardadoExitoso = false;
        initComponents();
        cargarCategorias();
        cargarDatosProducto();
    }
    
    private void initComponents() {
        setSize(500, 600);
        setLocationRelativeTo(getParent());
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel(editando ? "EDITAR PRODUCTO" : "NUEVO PRODUCTO");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(10, 25, 47));
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(lblTitulo, gbc);
        
        // Línea separadora
        gbc.gridy = 1;
        JSeparator separador = new JSeparator();
        mainPanel.add(separador, gbc);
        
        // Nombre
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        JLabel lblNombre = new JLabel("Nombre del Producto:");
        lblNombre.setFont(new Font("Arial", Font.BOLD, 12));
        lblNombre.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblNombre, gbc);
        
        gbc.gridx = 1;
        txtNombre = new JTextField(20);
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        txtNombre.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(txtNombre, gbc);
        
        // Categoría
        gbc.gridy = 3;
        gbc.gridx = 0;
        JLabel lblCategoria = new JLabel("Categoría:");
        lblCategoria.setFont(new Font("Arial", Font.BOLD, 12));
        lblCategoria.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblCategoria, gbc);
        
        gbc.gridx = 1;
        cbCategoria = new JComboBox<>();
        cbCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        cbCategoria.setBackground(Color.WHITE);
        mainPanel.add(cbCategoria, gbc);
        
        // Descripción
        gbc.gridy = 4;
        gbc.gridx = 0;
        JLabel lblDescripcion = new JLabel("Descripción:");
        lblDescripcion.setFont(new Font("Arial", Font.BOLD, 12));
        lblDescripcion.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblDescripcion, gbc);
        
        gbc.gridx = 1;
        txtDescripcion = new JTextArea(4, 20);
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        txtDescripcion.setFont(new Font("Arial", Font.PLAIN, 12));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescripcion);
        scrollDesc.setPreferredSize(new Dimension(250, 80));
        mainPanel.add(scrollDesc, gbc);
        
        // Precio Compra
        gbc.gridy = 5;
        gbc.gridx = 0;
        JLabel lblPrecioCompra = new JLabel("Precio de Compra:");
        lblPrecioCompra.setFont(new Font("Arial", Font.BOLD, 12));
        lblPrecioCompra.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblPrecioCompra, gbc);
        
        gbc.gridx = 1;
        txtPrecioCompra = new JTextField(20);
        txtPrecioCompra.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        mainPanel.add(txtPrecioCompra, gbc);
        
        // Precio Venta
        gbc.gridy = 6;
        gbc.gridx = 0;
        JLabel lblPrecioVenta = new JLabel("Precio de Venta:");
        lblPrecioVenta.setFont(new Font("Arial", Font.BOLD, 12));
        lblPrecioVenta.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblPrecioVenta, gbc);
        
        gbc.gridx = 1;
        txtPrecioVenta = new JTextField(20);
        txtPrecioVenta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        mainPanel.add(txtPrecioVenta, gbc);
        
        // Stock
        gbc.gridy = 7;
        gbc.gridx = 0;
        JLabel lblStock = new JLabel("Stock Inicial:");
        lblStock.setFont(new Font("Arial", Font.BOLD, 12));
        lblStock.setForeground(new Color(50, 50, 50));
        mainPanel.add(lblStock, gbc);
        
        gbc.gridx = 1;
        txtStock = new JTextField(20);
        txtStock.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        mainPanel.add(txtStock, gbc);
        
        // Panel de botones
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        btnGuardar = new JButton(editando ? "ACTUALIZAR" : "GUARDAR");
        btnGuardar.setBackground(new Color(0, 200, 255));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Arial", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(130, 40));
        btnGuardar.setBorderPainted(false);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarProducto());
        
        btnCancelar = new JButton("CANCELAR");
        btnCancelar.setBackground(new Color(100, 100, 100));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFont(new Font("Arial", Font.BOLD, 14));
        btnCancelar.setPreferredSize(new Dimension(130, 40));
        btnCancelar.setBorderPainted(false);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());
        
        buttonPanel.add(btnGuardar);
        buttonPanel.add(btnCancelar);
        mainPanel.add(buttonPanel, gbc);
        
        add(mainPanel);
    }
    
    private void cargarCategorias() {
        List<Categoria> categorias = categoriaServices.obtenerTodasLasCategorias();
        cbCategoria.removeAllItems();
        
        for (Categoria cat : categorias) {
            cbCategoria.addItem(cat);
        }
        
        if (cbCategoria.getItemCount() == 0) {
            // Categoría por defecto si no hay ninguna
            Categoria defaultCat = new Categoria(1, "General", "Categoría general");
            cbCategoria.addItem(defaultCat);
        }
    }
    
    private void cargarDatosProducto() {
        if (idProducto != null) {
            Proyecto.Model.Producto producto = productoServices.obtenerProducto(idProducto);
            if (producto != null) {
                txtNombre.setText(producto.getNombre());
                txtDescripcion.setText(producto.getDescripcion());
                txtPrecioCompra.setText(String.valueOf(producto.getPrecioCompra()));
                txtPrecioVenta.setText(String.valueOf(producto.getPrecioVenta()));
                txtStock.setText(String.valueOf(producto.getCantidad()));
                
                // Seleccionar la categoría correcta
                if (producto.getCategoria() != null) {
                    for (int i = 0; i < cbCategoria.getItemCount(); i++) {
                        Categoria cat = cbCategoria.getItemAt(i);
                        if (cat.getId() == producto.getCategoria().getId()) {
                            cbCategoria.setSelectedIndex(i);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    private void guardarProducto() {
        // Validar campos
        if (!validarCampos()) {
            return;
        }
        
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        Categoria categoria = (Categoria) cbCategoria.getSelectedItem();
        double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());
        double precioVenta = Double.parseDouble(txtPrecioVenta.getText().trim());
        int stock = Integer.parseInt(txtStock.getText().trim());
        
        boolean resultado;
        
        if (editando) {
            resultado = productoServices.actualizarProducto(
                idProducto, nombre, descripcion, precioCompra, precioVenta, stock
            );
        } else {
            resultado = productoServices.crearProducto(
                categoria.getId(), nombre, descripcion, precioCompra, precioVenta, stock
            );
        }
        
        if (resultado) {
            guardadoExitoso = true;
            JOptionPane.showMessageDialog(this,
                editando ? "Producto actualizado exitosamente" : "Producto creado exitosamente",
                "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al " + (editando ? "actualizar" : "crear") + " el producto",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validarCampos() {
        // Validar nombre
        if (txtNombre.getText().trim().isEmpty()) {
            mostrarError("El nombre del producto es requerido");
            txtNombre.requestFocus();
            return false;
        }
        
        // Validar precio compra
        try {
            double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());
            if (precioCompra <= 0) {
                mostrarError("El precio de compra debe ser mayor a 0");
                txtPrecioCompra.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un precio de compra válido");
            txtPrecioCompra.requestFocus();
            return false;
        }
        
        // Validar precio venta
        try {
            double precioVenta = Double.parseDouble(txtPrecioVenta.getText().trim());
            if (precioVenta <= 0) {
                mostrarError("El precio de venta debe ser mayor a 0");
                txtPrecioVenta.requestFocus();
                return false;
            }
            
            double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());
            if (precioVenta < precioCompra) {
                mostrarError("El precio de venta no puede ser menor al precio de compra");
                txtPrecioVenta.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un precio de venta válido");
            txtPrecioVenta.requestFocus();
            return false;
        }
        
        // Validar stock
        try {
            int stock = Integer.parseInt(txtStock.getText().trim());
            if (stock < 0) {
                mostrarError("El stock no puede ser negativo");
                txtStock.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("Ingrese un stock válido");
            txtStock.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
    
    public boolean isGuardadoExitoso() {
        return guardadoExitoso;
    }
}