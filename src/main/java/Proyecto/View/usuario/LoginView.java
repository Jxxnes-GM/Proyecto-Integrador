package Proyecto.View.usuario;

import Proyecto.Model.Cliente;
import Proyecto.services.PersonaServices;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class LoginView extends Application {

    private TextField txtEmail;
    private PasswordField txtPassword;
    private Button btnLogin;
    private Button btnRegistro;
    private Button btnSalir;

    private PersonaServices personaServices;
    private Stage stage;

    // Constructor por defecto para JavaFX Application
    public LoginView() {
        this.personaServices = new PersonaServices();
    }

    @Override
    public void start(Stage primaryStage) {
        this.stage = primaryStage;
        initComponents(primaryStage);
    }

    // Constructor alternativo para uso sin Application.launch()
    public LoginView(Stage stage) {
        this.personaServices = new PersonaServices();
        this.stage = stage;
        initComponents(stage);
    }

    private void initComponents(Stage stage) {
        stage.setTitle("TechZone - Iniciar Sesión");
        stage.setResizable(false);

        // ── Panel izquierdo (logo) ───────────────────────────────────────────
        VBox leftPanel = new VBox();
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setPrefWidth(450);
        leftPanel.setStyle("-fx-background-color: #0A1933;");

        Label lblLogo = new Label("TECHZONE");
        lblLogo.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        lblLogo.setTextFill(Color.web("#00C8FF"));

        Label lblSub = new Label("GADGETS & HOBBIES");
        lblSub.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        lblSub.setTextFill(Color.web("#00C8FF"));

        leftPanel.getChildren().addAll(lblLogo, lblSub);

        // ── Panel derecho (formulario) ───────────────────────────────────────
        GridPane rightPanel = new GridPane();
        rightPanel.setAlignment(Pos.CENTER);
        rightPanel.setHgap(10);
        rightPanel.setVgap(15);
        rightPanel.setPadding(new Insets(50));
        rightPanel.setStyle("-fx-background-color: #0F1E37;");

        // Título
        Label lblTitulo = new Label("INICIAR SESIÓN");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        lblTitulo.setTextFill(Color.WHITE);
        lblTitulo.setTextAlignment(TextAlignment.CENTER);
        GridPane.setColumnSpan(lblTitulo, 2);
        GridPane.setHalignment(lblTitulo, javafx.geometry.HPos.CENTER);
        rightPanel.add(lblTitulo, 0, 0);

        // Email
        Label lblEmail = new Label("Correo Electrónico:");
        lblEmail.setTextFill(Color.WHITE);
        lblEmail.setFont(Font.font("Arial", 14));
        rightPanel.add(lblEmail, 0, 1);

        txtEmail = new TextField();
        txtEmail.setPrefWidth(250);
        txtEmail.setFont(Font.font("Arial", 14));
        txtEmail.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #00C8FF;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 8;");
        rightPanel.add(txtEmail, 1, 1);

        // Contraseña
        Label lblPassword = new Label("Contraseña:");
        lblPassword.setTextFill(Color.WHITE);
        lblPassword.setFont(Font.font("Arial", 14));
        rightPanel.add(lblPassword, 0, 2);

        txtPassword = new PasswordField();
        txtPassword.setPrefWidth(250);
        txtPassword.setFont(Font.font("Arial", 14));
        txtPassword.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #00C8FF;" +
                        "-fx-border-width: 1.5;" +
                        "-fx-padding: 8;");
        rightPanel.add(txtPassword, 1, 2);

        // Botones
        btnLogin = crearBoton("INGRESAR", "#00C8FF");
        btnRegistro = crearBoton("REGISTRARSE", "#323246");
        btnSalir = crearBoton("SALIR", "#C83C3C");

        HBox buttonBox = new HBox(15, btnLogin, btnRegistro, btnSalir);
        buttonBox.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(buttonBox, 2);
        rightPanel.add(buttonBox, 0, 3);

        // Acción salir por defecto
        btnSalir.setOnAction(e -> stage.close());

        // ── Layout principal ─────────────────────────────────────────────────
        HBox mainPanel = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(rightPanel, Priority.ALWAYS);

        Scene scene = new Scene(mainPanel, 900, 600);
        stage.setScene(scene);
        stage.show();
    }

    private Button crearBoton(String texto, String colorHex) {
        Button btn = new Button(texto);
        btn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        btn.setTextFill(Color.WHITE);
        btn.setPrefSize(130, 40);
        btn.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;");

        // Efecto hover
        String hoverColor = colorHex.equals("#00C8FF") ? "#009DBF"
                : colorHex.equals("#C83C3C") ? "#A03030"
                        : "#222232";
        btn.setOnMouseEntered(e -> btn.setStyle(
                "-fx-background-color: " + hoverColor + ";" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                        "-fx-border-width: 0;" +
                        "-fx-cursor: hand;"));
        return btn;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getEmail() {
        return txtEmail.getText().trim();
    }

    public String getPassword() {
        return txtPassword.getText();
    }

    public Button getBtnLogin() {
        return btnLogin;
    }

    public Button getBtnRegistro() {
        return btnRegistro;
    }

    public Button getBtnSalir() {
        return btnSalir;
    }

    // ── Métodos utilitarios ──────────────────────────────────────────────────
    public void mostrarMensaje(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.showAndWait();
    }

    public void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        alert.setTitle("Error");
        alert.showAndWait();
    }

    public void limpiarCampos() {
        txtEmail.clear();
        txtPassword.clear();
    }

    public void abrirMenuPrincipal(Cliente cliente) {
        Stage menuStage = new Stage();
        new MenuPrincipalView(cliente, menuStage);
        stage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}