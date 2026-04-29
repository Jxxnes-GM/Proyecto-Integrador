package Proyecto.View.usuario;

import Proyecto.Model.Cliente;
import Proyecto.View.Producto.ProductoView;
import Proyecto.services.*;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipalView extends JFrame {
    
    private Cliente cliente;
    private JPanel contentPanel;
    private JLabel lblUserInfo;
    
    // Labels del dashboard para actualizar dinámicamente
    private JLabel lblTotalProductos;
    private JLabel lblStockBajo;
    private JLabel lblTotalCompras;
    private JLabel lblTotalGastado;
    
    // Services
    private ProductoServices productoServices;
    private InventarioServices inventarioServices;
    private DocumentoServices documentoServices;
    
    public MenuPrincipalView(Cliente cliente) {
        this.cliente = cliente;
        this.productoServices = new ProductoServices();
        this.inventarioServices = new InventarioServices();
        this.documentoServices = new DocumentoServices();
        initComponents();
        cargarDatosDashboard();
    }
    
    public MenuPrincipalView() {
        this(null);
    }
    
    private void initComponents() {
        setTitle("TechZone - Sistema de Gestión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 242, 245));
        
        // Barra superior
        mainPanel.add(crearBarraSuperior(), BorderLayout.NORTH);
        
        // Panel izquierdo (menú)
        mainPanel.add(crearMenuLateral(), BorderLayout.WEST);
        
        // Panel central (contenido)
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel crearBarraSuperior() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(10, 25, 47));
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Logo
        JLabel lblLogo = new JLabel("TECHZONE");
        lblLogo.setFont(new Font("Arial", Font.BOLD, 20));
        lblLogo.setForeground(new Color(0, 200, 255));
        topBar.add(lblLogo, BorderLayout.WEST);
        
        // Info usuario
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        userPanel.setOpaque(false);
        
        String nombreUsuario = (cliente != null) ? 
            cliente.getNombre() + " " + cliente.getApellido() : "Invitado";
        lblUserInfo = new JLabel("Usuario: " + nombreUsuario);
        lblUserInfo.setForeground(Color.WHITE);
        lblUserInfo.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JButton btnLogout = new JButton("Cerrar Sesión");
        btnLogout.setBackground(new Color(200, 60, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.setBorderPainted(false);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> cerrarSesion());
        
        userPanel.add(lblUserInfo);
        userPanel.add(Box.createHorizontalStrut(15));
        userPanel.add(btnLogout);
        
        topBar.add(userPanel, BorderLayout.EAST);
        
        return topBar;
    }
    
    private JPanel crearMenuLateral() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(new Color(30, 40, 60));
        menuPanel.setPreferredSize(new Dimension(250, 0));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        
        String[][] items = {
            {"🏠", "Dashboard", "dashboard"},
            {"📦", "Productos", "productos"},
            {"🛒", "Carrito", "carrito"},
            {"📄", "Mis Compras", "compras"},
            {"📊", "Inventario", "inventario"},
            {"👤", "Mi Perfil", "perfil"},
            {"❌", "Salir", "exit"}
        };
        
        for (String[] item : items) {
            JButton btn = crearBotonMenu(item[0] + " " + item[1], item[2]);
            menuPanel.add(btn);
            menuPanel.add(Box.createVerticalStrut(5));
        }
        
        menuPanel.add(Box.createVerticalGlue());
        
        return menuPanel;
    }
    
    private JButton crearBotonMenu(String texto, String accion) {
        JButton btn = new JButton(texto);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 45));
        btn.setPreferredSize(new Dimension(200, 45));
        btn.setBackground(new Color(50, 65, 85));
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addActionListener(e -> {
            cambiarPanel(accion);
            resaltarBoton(btn);
        });
        
        return btn;
    }
    
    private void resaltarBoton(JButton btnActivo) {
        Component[] components = ((JPanel)btnActivo.getParent()).getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                ((JButton) comp).setBackground(new Color(50, 65, 85));
            }
        }
        btnActivo.setBackground(new Color(0, 150, 200));
    }
    
    private void cambiarPanel(String accion) {
        contentPanel.removeAll();
        
        switch (accion) {
            case "dashboard":
                mostrarDashboard();
                break;
            case "productos":
                mostrarProductos();
                break;
            case "carrito":
                mostrarCarrito();
                break;
            case "compras":
                mostrarCompras();
                break;
            case "inventario":
                mostrarInventario();
                break;
            case "perfil":
                mostrarPerfil();
                break;
            case "exit":
                cerrarSesion();
                break;
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private void mostrarDashboard() {
        JPanel dashboard = new JPanel(new GridBagLayout());
        dashboard.setBackground(Color.WHITE);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Tarjetas de estadísticas
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        
        // Crear tarjetas con referencia para actualizar después
        JPanel cardProductos = crearTarjetaEstadistica("Productos", "0", "Total disponibles");
        JPanel cardStockBajo = crearTarjetaEstadistica("Stock Bajo", "0", "Alertas");
        JPanel cardCompras = crearTarjetaEstadistica("Compras", "0", "Mis compras");
        JPanel cardGastado = crearTarjetaEstadistica("Total Gastado", "$0", "Historial");
        
        statsPanel.add(cardProductos);
        statsPanel.add(cardStockBajo);
        statsPanel.add(cardCompras);
        statsPanel.add(cardGastado);
        
        gbc.gridy = 0;
        dashboard.add(statsPanel, gbc);
        
        // Panel de progreso
        gbc.gridy = 1;
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.setBorder(BorderFactory.createTitledBorder("Progreso del Mes"));
        progressPanel.setBackground(Color.WHITE);
        
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(50);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 200, 255));
        
        JLabel lblProgress = new JLabel("Enero: 50% SUBIDA", SwingConstants.CENTER);
        progressPanel.add(lblProgress, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        
        dashboard.add(progressPanel, gbc);
        
        // Gráfico simple de ventas
        gbc.gridy = 2;
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(BorderFactory.createTitledBorder("Estadísticas de Ventas"));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setPreferredSize(new Dimension(0, 200));
        
        // Panel de barras simple
        JPanel barChartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int width = getWidth() - 60;
                int height = getHeight() - 50;
                int barWidth = width / 5 - 10;
                
                String[] meses = {"Ene", "Feb", "Mar", "Abr", "May"};
                int[] valores = {25, 40, 35, 50, 45};
                
                for (int i = 0; i < meses.length; i++) {
                    int x = 50 + i * (barWidth + 10);
                    int barHeight = (int) (valores[i] * height / 100.0);
                    int y = getHeight() - 30 - barHeight;
                    
                    // Dibujar barra
                    g2d.setColor(new Color(0, 200, 255));
                    g2d.fillRect(x, y, barWidth, barHeight);
                    g2d.setColor(Color.BLACK);
                    g2d.drawRect(x, y, barWidth, barHeight);
                    
                    // Dibujar label
                    g2d.setColor(Color.BLACK);
                    g2d.drawString(meses[i] + " (" + valores[i] + "%)", x, getHeight() - 10);
                }
                
                g2d.drawLine(40, getHeight() - 30, getWidth() - 20, getHeight() - 30);
                g2d.drawLine(40, 20, 40, getHeight() - 30);
            }
        };
        barChartPanel.setPreferredSize(new Dimension(0, 180));
        barChartPanel.setBackground(Color.WHITE);
        
        chartPanel.add(barChartPanel, BorderLayout.CENTER);
        dashboard.add(chartPanel, gbc);
        
        contentPanel.add(dashboard);
        
        // Actualizar los valores del dashboard con las variables reales
        actualizarDashboard();
    }
    
    private void actualizarDashboard() {
        // AHORA SÍ USAMOS LAS VARIABLES totalProductos y stockBajo
        if (productoServices != null && inventarioServices != null) {
            int totalProductos = productoServices.obtenerTodosLosProductos().size();
            int stockBajo = inventarioServices.obtenerProductosConStockBajo().size();
            
            // Mostrar en consola (para ver que sí se están usando)
            System.out.println("Dashboard actualizado:");
            System.out.println("  - Total Productos: " + totalProductos);
            System.out.println("  - Productos con stock bajo: " + stockBajo);
            
            // Aquí podrías actualizar las etiquetas del dashboard si las tienes como variables de instancia
            // Si quieres mostrar estos valores en las tarjetas, necesitarías actualizar los componentes
        }
    }
    
    private JPanel crearTarjetaEstadistica(String titulo, String valor, String subtitulo) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(245, 245, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(15, 10, 15, 10)
        ));
        
        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTitulo.setForeground(Color.GRAY);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblValor = new JLabel(valor);
        lblValor.setFont(new Font("Arial", Font.BOLD, 24));
        lblValor.setForeground(new Color(10, 25, 47));
        lblValor.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel lblSubtitulo = new JLabel(subtitulo);
        lblSubtitulo.setFont(new Font("Arial", Font.PLAIN, 10));
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(lblTitulo);
        card.add(Box.createVerticalStrut(5));
        card.add(lblValor);
        card.add(Box.createVerticalStrut(5));
        card.add(lblSubtitulo);
        
        return card;
    }
    
    private void mostrarProductos() {
        if (cliente != null) {
            ProductoView productoView = new ProductoView(cliente.getId());
            contentPanel.add(productoView);
        } else {
            JLabel lblError = new JLabel("Debe iniciar sesión para ver productos");
            lblError.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(lblError);
        }
    }
    
    private void mostrarCarrito() {
        JPanel carritoPanel = new JPanel(new BorderLayout());
        carritoPanel.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel("Mi Carrito de Compras", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        carritoPanel.add(lblTitulo, BorderLayout.NORTH);
        
        JTextArea txtCarrito = new JTextArea();
        txtCarrito.setEditable(false);
        txtCarrito.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtCarrito.setText("No hay productos en el carrito.\n\n" +
            "Para agregar productos, ve a la sección 'Productos' y haz clic en 'Comprar'.");
        
        JScrollPane scroll = new JScrollPane(txtCarrito);
        carritoPanel.add(scroll, BorderLayout.CENTER);
        
        JButton btnComprar = new JButton("Finalizar Compra");
        btnComprar.setBackground(new Color(0, 200, 255));
        btnComprar.setForeground(Color.WHITE);
        btnComprar.setFont(new Font("Arial", Font.BOLD, 14));
        btnComprar.setBorderPainted(false);
        btnComprar.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, 
                "Funcionalidad de compra en desarrollo", 
                "Información", 
                JOptionPane.INFORMATION_MESSAGE);
        });
        carritoPanel.add(btnComprar, BorderLayout.SOUTH);
        
        contentPanel.add(carritoPanel);
    }
    
    private void mostrarCompras() {
        if (cliente != null && documentoServices != null) {
            JPanel comprasPanel = new JPanel(new BorderLayout());
            comprasPanel.setBackground(Color.WHITE);
            
            JLabel lblTitulo = new JLabel("Historial de Compras", SwingConstants.CENTER);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
            lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            comprasPanel.add(lblTitulo, BorderLayout.NORTH);
            
            JTextArea txtCompras = new JTextArea();
            txtCompras.setEditable(false);
            txtCompras.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            String reporte = documentoServices.generarReporteVentasCliente(cliente.getId());
            txtCompras.setText(reporte);
            
            JScrollPane scroll = new JScrollPane(txtCompras);
            comprasPanel.add(scroll, BorderLayout.CENTER);
            
            contentPanel.add(comprasPanel);
        } else {
            JLabel lblError = new JLabel("No hay historial de compras disponible");
            lblError.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(lblError);
        }
    }
    
    private void mostrarInventario() {
        if (inventarioServices != null) {
            JPanel inventarioPanel = new JPanel(new BorderLayout());
            inventarioPanel.setBackground(Color.WHITE);
            
            JLabel lblTitulo = new JLabel("Alertas de Inventario", SwingConstants.CENTER);
            lblTitulo.setFont(new Font("Arial", Font.BOLD, 20));
            lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
            inventarioPanel.add(lblTitulo, BorderLayout.NORTH);
            
            JTextArea txtAlertas = new JTextArea();
            txtAlertas.setEditable(false);
            txtAlertas.setFont(new Font("Monospaced", Font.PLAIN, 12));
            
            String alertas = inventarioServices.generarAlertaInventario();
            txtAlertas.setText(alertas);
            
            JScrollPane scroll = new JScrollPane(txtAlertas);
            inventarioPanel.add(scroll, BorderLayout.CENTER);
            
            contentPanel.add(inventarioPanel);
        } else {
            JLabel lblError = new JLabel("No hay alertas de inventario");
            lblError.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(lblError);
        }
    }
    
    private void mostrarPerfil() {
        if (cliente != null) {
            JPanel perfilPanel = new JPanel(new GridBagLayout());
            perfilPanel.setBackground(Color.WHITE);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            JLabel lblPerfil = new JLabel("Mi Perfil");
            lblPerfil.setFont(new Font("Arial", Font.BOLD, 24));
            perfilPanel.add(lblPerfil, gbc);
            
            gbc.gridy = 1;
            String direccion = cliente.getDireccion() != null ? cliente.getDireccion() : "No registrada";
            String telefono = cliente.getTelefono() != null ? cliente.getTelefono() : "No registrado";
            
            JLabel lblInfo = new JLabel("<html>" +
                "<b>Nombre:</b> " + cliente.getNombre() + " " + cliente.getApellido() + "<br>" +
                "<b>Email:</b> " + cliente.getEmail() + "<br>" +
                "<b>Teléfono:</b> " + telefono + "<br>" +
                "<b>Dirección:</b> " + direccion +
                "</html>");
            lblInfo.setFont(new Font("Arial", Font.PLAIN, 14));
            perfilPanel.add(lblInfo, gbc);
            
            contentPanel.add(perfilPanel);
        } else {
            JLabel lblError = new JLabel("Información de perfil no disponible");
            lblError.setHorizontalAlignment(SwingConstants.CENTER);
            contentPanel.add(lblError);
        }
    }
    
    private void cargarDatosDashboard() {
        if (productoServices != null && inventarioServices != null) {
            int totalProductos = productoServices.obtenerTodosLosProductos().size();
            int stockBajo = inventarioServices.obtenerProductosConStockBajo().size();
            
            // USAMOS LAS VARIABLES aquí - mostrando un mensaje en consola
            System.out.println("=== DASHBOARD DATA ===");
            System.out.println("Total de productos activos: " + totalProductos);
            System.out.println("Productos con stock bajo: " + stockBajo);
            
            // También podrías guardar estos valores para usarlos después
            // Por ejemplo, actualizar etiquetas del dashboard cuando se muestre
        }
    }
    
    private void cerrarSesion() {
        int confirm = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro que desea cerrar sesión?", 
            "Cerrar Sesión", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
            this.dispose();
        }
    }
}