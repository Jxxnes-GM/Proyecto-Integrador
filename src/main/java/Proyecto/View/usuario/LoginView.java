package Proyecto.View.usuario;


import Proyecto.Model.Cliente;
import Proyecto.services.PersonaServices;
import java.util.Scanner;

public class LoginView {

    private PersonaServices personaServices;
    private Scanner scanner;

    public LoginView() {
        this.personaServices = new PersonaServices();
        this.scanner = new Scanner(System.in);
    }

    // Mostrar menú de login
    public Cliente mostrarMenuLogin() {
        limpiarPantalla();
        imprimirEncabezado();

        while (true) {
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║           MENÚ DE AUTENTICACIÓN        ║");
            System.out.println("╚════════════════════════════════════════╝\n");

            System.out.println("1. Iniciar sesión");
            System.out.println("2. Registrar nueva cuenta");
            System.out.println("3. Salir\n");

            System.out.print("Seleccione una opción: ");
            String opcion = scanner.nextLine();

            switch (opcion) {
                case "1":
                    Cliente cliente = realizarLogin();
                    if (cliente != null) {
                        return cliente;
                    }
                    break;
                case "2":
                    registrarNuevaCuenta();
                    break;
                case "3":
                    System.out.println("\n¡Gracias por usar TechZone! Hasta luego.");
                    System.exit(0);
                    break;
                default:
                    System.out.println(" Opción inválida. Intente de nuevo.\n");
            }
        }
    }

    // Realizar login
    private Cliente realizarLogin() {
        limpiarPantalla();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║           INICIAR SESIÓN               ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Contraseña: ");
        String password = scanner.nextLine();

        System.out.println("\n Validando credenciales...");
        Cliente cliente = personaServices.autenticarCliente(email, password);

        if (cliente != null) {
            limpiarPantalla();
            System.out.println("╔════════════════════════════════════════╗");
            System.out.println("║    BIENVENIDO AL SISTEMA TECHZONE      ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            System.out.println(" Hola, " + cliente.getNombre() + " " + cliente.getApellido());
            System.out.println("ID Cliente: " + cliente.getId());
            pausa(2);
            return cliente;
        } else {
            System.out.println("\n Credenciales inválidas. Intente de nuevo.\n");
            pausa(2);
            return null;
        }
    }

    // Registrar nueva cuenta
    private void registrarNuevaCuenta() {
        limpiarPantalla();
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║        CREAR NUEVA CUENTA              ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Apellido: ");
        String apellido = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Teléfono: ");
        String telefono = scanner.nextLine();

        System.out.print("Tipo de documento (CC/TI/Pasaporte): ");
        String tipoDocumento = scanner.nextLine();

        System.out.print("Contraseña (mín. 6 caracteres): ");
        String password = scanner.nextLine();

        System.out.print("Confirmar contraseña: ");
        String confirmar = scanner.nextLine();

        if (!password.equals(confirmar)) {
            System.out.println(" Las contraseñas no coinciden.\n");
            pausa(2);
            return;
        }

        System.out.print("Dirección: ");
        String direccion = scanner.nextLine();

        System.out.println("\n Registrando cuenta...");

        boolean registrado = personaServices.registrarCliente(
                nombre,
                apellido,
                email,
                telefono,
                tipoDocumento,
                password,
                direccion);

        if (registrado) {
            System.out.println(" Cuenta creada exitosamente!");
            System.out.println("Ahora puede iniciar sesión con sus credenciales.\n");
        } else {
            System.out.println(" Error al crear la cuenta. Intente de nuevo.\n");
        }

        pausa(3);
    }

    // Utilidades
    private void limpiarPantalla() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private void imprimirEncabezado() {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║         BIENVENIDO A TECHZONE          ║");
        System.out.println("║      Sistema de Gestión de Ventas      ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private void pausa(int segundos) {
        try {
            Thread.sleep(segundos * 1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
