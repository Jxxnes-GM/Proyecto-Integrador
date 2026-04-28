package Proyecto.View.usuario;

import Proyecto.Model.Cliente;
import Proyecto.services.PersonaServices;
import javax.swing.*;
import java.awt.*;

public class LoginView extends JFrame {
    
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JButton btnRegistro;
    private JButton btnSalir;
    
    private PersonaServices personaServices;
    
    public LoginView() {
        this.personaServices = new PersonaServices();
        initComponents();
    }
    
    private void initComponents() {
        setTitle("TechZone - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel principal con color de fondo
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(10, 25, 47));
        
        // Panel izquierdo (logo)
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(10, 25, 47));
        leftPanel.setPreferredSize(new Dimension(450, 0));
        
        JLabel lblLogo = new JLabel();
        lblLogo.setText("TECHZONE\nGADGETS & HOBBIES");
        lblLogo.setFont(new Font("Arial", Font.BOLD, 28));
        lblLogo.setForeground(new Color(0, 200, 255));
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        leftPanel.add(lblLogo);
        
        // Panel derecho (formulario)
        JPanel rightPanel = new JPanel(new GridBagLayout());
        rightPanel.setBackground(new Color(15, 30, 55));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Título
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        JLabel lblTitulo = new JLabel("INICIAR SESIÓN");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 24));
        lblTitulo.setForeground(Color.WHITE);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        rightPanel.add(lblTitulo, gbc);
        
        // Email
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        JLabel lblEmail = new JLabel("Correo Electrónico:");
        lblEmail.setForeground(Color.WHITE);
        lblEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        rightPanel.add(lblEmail, gbc);
        
        gbc.gridx = 1;
        txtEmail = new JTextField(20);
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        txtEmail.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        rightPanel.add(txtEmail, gbc);
        
        // Contraseña
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lblPassword = new JLabel("Contraseña:");
        lblPassword.setForeground(Color.WHITE);
        lblPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        rightPanel.add(lblPassword, gbc);
        
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 200, 255)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        rightPanel.add(txtPassword, gbc);
        
        // Botones
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(new Color(15, 30, 55));
        
        btnLogin = new JButton("INGRESAR");
        btnLogin.setBackground(new Color(0, 200, 255));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(130, 40));
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        
        btnRegistro = new JButton("REGISTRARSE");
        btnRegistro.setBackground(new Color(50, 50, 70));
        btnRegistro.setForeground(Color.WHITE);
        btnRegistro.setFont(new Font("Arial", Font.BOLD, 14));
        btnRegistro.setPreferredSize(new Dimension(130, 40));
        btnRegistro.setBorderPainted(false);
        btnRegistro.setFocusPainted(false);
        
        btnSalir = new JButton("SALIR");
        btnSalir.setBackground(new Color(200, 60, 60));
        btnSalir.setForeground(Color.WHITE);
        btnSalir.setFont(new Font("Arial", Font.BOLD, 14));
        btnSalir.setPreferredSize(new Dimension(130, 40));
        btnSalir.setBorderPainted(false);
        btnSalir.setFocusPainted(false);
        
        buttonPanel.add(btnLogin);
        buttonPanel.add(btnRegistro);
        buttonPanel.add(btnSalir);
        rightPanel.add(buttonPanel, gbc);
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    // Getters
    public String getEmail() {
        return txtEmail.getText().trim();
    }
    
    public String getPassword() {
        return new String(txtPassword.getPassword());
    }
    
    public JButton getBtnLogin() {
        return btnLogin;
    }
    
    public JButton getBtnRegistro() {
        return btnRegistro;
    }
    
    public JButton getBtnSalir() {
        return btnSalir;
    }
    
    // Métodos utilitarios
    public void mostrarMensaje(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje);
    }
    
    public void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    public void limpiarCampos() {
        txtEmail.setText("");
        txtPassword.setText("");
    }
    
    public void abrirMenuPrincipal(Cliente cliente) {
        MenuPrincipalView menuView = new MenuPrincipalView(cliente);
        menuView.setVisible(true);
        this.dispose();
    }
}