package Proyecto.View.Admin;

import Proyecto.services.PersonaServices;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Formulario modal para crear o editar un Empleado — TechZone (Admin).
 *
 * Uso:
 * 
 * <pre>
 * // Nuevo empleado
 * new GestionEmpleadosView(ownerStage, null, () -> refrescarTabla());
 *
 * // Editar empleado existente (row = String[] con datos actuales)
 * new GestionEmpleadosView(ownerStage, row, () -> refrescarTabla());
 * </pre>
 */
public class GestionEmpleadosView {

    private final PersonaServices personaServices;
    private final Runnable onGuardado;
    private final String[] datosActuales; // null = nuevo, non-null = editar

    private TextField txtNombres;
    private TextField txtApellidos;
    private TextField txtEmail;
    private TextField txtTelefono;
    private TextField txtDocumento;
    private ComboBox<String> cbCargo;
    private PasswordField txtContrasena;

    private Label lblMensaje;
    private Stage stage;

    // ── Constructor ───────────────────────────────────────────────────────────
    public GestionEmpleadosView(Stage owner, String[] datosActuales, Runnable onGuardado) {
        this.personaServices = new PersonaServices();
        this.datosActuales = datosActuales;
        this.onGuardado = onGuardado;
        build(owner);
    }

    // ── Construcción ─────────────────────────────────────────────────────────
    private void build(Stage owner) {
        stage = new Stage();
        stage.setTitle(datosActuales == null ? "Nuevo Empleado" : "Editar Empleado");
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        if (owner != null)
            stage.initOwner(owner);

        GridPane grid = new GridPane();
        grid.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(25));

        ColumnConstraints col0 = new ColumnConstraints(150);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col0, col1);

        int fila = 0;

        // Título
        Label lblTitulo = new Label(datosActuales == null ? "NUEVO EMPLEADO" : "EDITAR EMPLEADO");
        lblTitulo.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        lblTitulo.setTextFill(Color.web("#0A1933"));
        GridPane.setColumnSpan(lblTitulo, 2);
        GridPane.setHalignment(lblTitulo, HPos.CENTER);
        grid.add(lblTitulo, 0, fila++);

        Separator sep = new Separator();
        GridPane.setColumnSpan(sep, 2);
        grid.add(sep, 0, fila++);

        // Campos
        txtNombres = campo("Nombres completos");
        txtApellidos = campo("Apellidos completos");
        txtEmail = campo("correo@techzone.co");
        txtTelefono = campo("Ej: 3001234567");
        txtDocumento = campo("Cédula / DNI");
        txtContrasena = new PasswordField();
        txtContrasena.setPromptText(datosActuales == null ? "Contraseña inicial" : "Dejar en blanco para no cambiar");
        estilo(txtContrasena);

        cbCargo = new ComboBox<>();
        cbCargo.getItems().addAll("Administrador", "Comprador", "Vendedor", "Cajero", "Bodeguero");
        cbCargo.getSelectionModel().select("Vendedor");
        cbCargo.setMaxWidth(Double.MAX_VALUE);

        Object[][] rows = {
                { "Nombres *", txtNombres },
                { "Apellidos *", txtApellidos },
                { "Email *", txtEmail },
                { "Teléfono", txtTelefono },
                { "Documento *", txtDocumento },
                { "Cargo *", cbCargo },
                { "Contraseña", txtContrasena },
        };

        for (Object[] r : rows) {
            grid.add(etiqueta((String) r[0]), 0, fila);
            grid.add((Control) r[1], 1, fila++);
        }

        // Pre-llenar si estamos editando
        if (datosActuales != null) {
            txtNombres.setText(safe(datosActuales, 1));
            txtApellidos.setText(safe(datosActuales, 2));
            txtEmail.setText(safe(datosActuales, 3));
            String cargo = safe(datosActuales, 4);
            if (cbCargo.getItems().contains(cargo))
                cbCargo.getSelectionModel().select(cargo);
        }

        // Mensaje
        lblMensaje = new Label("");
        lblMensaje.setFont(Font.font("Arial", 12));
        lblMensaje.setWrapText(true);
        lblMensaje.setMaxWidth(350);
        GridPane.setColumnSpan(lblMensaje, 2);
        GridPane.setHalignment(lblMensaje, HPos.CENTER);
        grid.add(lblMensaje, 0, fila++);

        // Botones
        Button btnGuardar = boton(datosActuales == null ? "CREAR" : "ACTUALIZAR", "#00C8FF");
        Button btnCancelar = boton("CANCELAR", "#646464");
        btnGuardar.setOnAction(e -> guardar());
        btnCancelar.setOnAction(e -> stage.close());

        HBox btnBox = new HBox(15, btnGuardar, btnCancelar);
        btnBox.setAlignment(Pos.CENTER);
        GridPane.setColumnSpan(btnBox, 2);
        grid.add(btnBox, 0, fila);

        Scene scene = new Scene(grid, 480, 560);
        stage.setScene(scene);
        stage.showAndWait();
    }

    // ── Guardar ───────────────────────────────────────────────────────────────
    private void guardar() {
        limpiar();
        if (txtNombres.getText().trim().isEmpty()) {
            error("Nombres es obligatorio.", txtNombres);
            return;
        }
        if (txtApellidos.getText().trim().isEmpty()) {
            error("Apellidos es obligatorio.", txtApellidos);
            return;
        }
        if (txtEmail.getText().trim().isEmpty()) {
            error("Email es obligatorio.", txtEmail);
            return;
        }
        if (txtDocumento.getText().trim().isEmpty()) {
            error("Documento es obligatorio.", txtDocumento);
            return;
        }
        if (datosActuales == null && txtContrasena.getText().length() < 6) {
            error("La contraseña debe tener al menos 6 caracteres.", txtContrasena);
            return;
        }

        try {
            // TODO: conectar personaServices.crearEmpleado(...) / actualizarEmpleado(...)
            // personaServices.crearEmpleado(
            // txtNombres.getText().trim(), txtApellidos.getText().trim(),
            // txtEmail.getText().trim(), txtTelefono.getText().trim(),
            // txtDocumento.getText().trim(), cbCargo.getValue(),
            // txtContrasena.getText());

            new Alert(Alert.AlertType.INFORMATION,
                    "Empleado " + (datosActuales == null ? "creado" : "actualizado") + " correctamente.\n" +
                            "(Conecta personaServices para persistencia en BD)",
                    ButtonType.OK).showAndWait();

            if (onGuardado != null)
                onGuardado.run();
            stage.close();
        } catch (Exception ex) {
            error("Error al guardar: " + ex.getMessage(), null);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private TextField campo(String prompt) {
        TextField tf = new TextField();
        estilo(tf);
        tf.setPromptText(prompt);
        return tf;
    }

    private void estilo(Control c) {
        c.setStyle("-fx-border-color: #C0C0C0; -fx-border-width: 1; -fx-padding: 8; -fx-font-size: 12px;");
    }

    private Label etiqueta(String texto) {
        Label l = new Label(texto);
        l.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        return l;
    }

    private Button boton(String texto, String color) {
        Button b = new Button(texto);
        b.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        b.setTextFill(Color.WHITE);
        b.setPrefSize(130, 38);
        b.setStyle("-fx-background-color:" + color + ";-fx-border-width:0;-fx-cursor:hand;");
        return b;
    }

    private void error(String msg, Control foco) {
        lblMensaje.setTextFill(Color.web("#C83C3C"));
        lblMensaje.setText("⚠ " + msg);
        if (foco != null)
            foco.requestFocus();
    }

    private void limpiar() {
        lblMensaje.setText("");
    }

    private String safe(String[] arr, int i) {
        return (arr != null && arr.length > i && arr[i] != null) ? arr[i] : "";
    }
}