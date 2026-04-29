package Proyecto.services;


import Proyecto.Model.Cliente;
import Proyecto.dao.PersonaDAO;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PersonaServices {

    private PersonaDAO personaDAO;

    public PersonaServices() {
        this.personaDAO = new PersonaDAO();
    }

    // Registrar nuevo cliente con validaciones completas
    public boolean registrarCliente(String nombre, String apellido, String email,
            String telefono, String tipoDocumento,
            String password, String direccion) {

        // Validar que no exista un cliente con el mismo email
        if (personaDAO.emailExiste(email)) {
            System.out.println("Error: El email ya está registrado en el sistema");
            return false;
        }

        // Validar campos requeridos
        if (!validarDatos(nombre, apellido, email, password)) {
            return false;
        }

        // Crear cliente con datos validados
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido.trim());
        cliente.setEmail(email.toLowerCase().trim());
        cliente.setTelefono(telefono);
        cliente.setPasswordHash(encriptarPassword(password));
        cliente.setDireccion(direccion);

        return personaDAO.crearCliente(cliente);
    }

    // Autenticar cliente
    public Cliente autenticarCliente(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Error: Email y contraseña son requeridos");
            return null;
        }

        Cliente cliente = personaDAO.obtenerClientePorEmail(email.toLowerCase());

        if (cliente == null) {
            System.out.println("Error: Cliente no encontrado");
            return null;
        }

        if (!verificarPassword(password, cliente.getPasswordHash())) {
            System.out.println("Error: Contraseña incorrecta");
            return null;
        }

        System.out.println("Autenticación exitosa para: " + email);
        return cliente;
    }

    // Obtener cliente por ID
    public Cliente obtenerCliente(int idCliente) {
        return personaDAO.obtenerClientePorId(idCliente);
    }

    // Obtener todos los clientes
    public List<Cliente> obtenerTodosLosClientes() {
        return personaDAO.obtenerTodosLosClientes();
    }

    // Actualizar información del cliente
    public boolean actualizarCliente(int idCliente, String nombre, String apellido,
            String telefono, String direccion) {
        Cliente cliente = personaDAO.obtenerClientePorId(idCliente);

        if (cliente == null) {
            System.out.println("Error: Cliente no encontrado");
            return false;
        }

        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido.trim());
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);

        return personaDAO.actualizarCliente(cliente);
    }

    // Cambiar contraseña con validación de contraseña actual
    public boolean cambiarPassword(int idCliente, String passwordActual, String passwordNueva) {
        Cliente cliente = personaDAO.obtenerClientePorId(idCliente);

        if (cliente == null) {
            System.out.println("Error: Cliente no encontrado");
            return false;
        }

        // Verificar contraseña actual
        if (!verificarPassword(passwordActual, cliente.getPasswordHash())) {
            System.out.println("Error: La contraseña actual es incorrecta");
            return false;
        }

        // Validar nueva contraseña
        if (passwordNueva == null || passwordNueva.length() < 6) {
            System.out.println("Error: La nueva contraseña debe tener al menos 6 caracteres");
            return false;
        }

        String passwordEncriptada = encriptarPassword(passwordNueva);
        return personaDAO.actualizarPassword(idCliente, passwordEncriptada);
    }

    // Desactivar cuenta de cliente
    public boolean desactivarCliente(int idCliente) {
        return personaDAO.eliminarCliente(idCliente);
    }

    // Validar datos del cliente
    private boolean validarDatos(String nombre, String apellido, String email, String password) {
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("Error: El nombre es requerido");
            return false;
        }

        if (apellido == null || apellido.trim().isEmpty()) {
            System.out.println("Error: El apellido es requerido");
            return false;
        }

        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            System.out.println("Error: El email es requerido y debe ser válido");
            return false;
        }

        if (password == null || password.length() < 6) {
            System.out.println("Error: La contraseña debe tener al menos 6 caracteres");
            return false;
        }

        return true;
    }

    // Encriptar contraseña
    private String encriptarPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al encriptar contraseña: " + e.getMessage());
            return null;
        }
    }

    // Verificar contraseña
    private boolean verificarPassword(String password, String hashAlmacenado) {
        String hashIngresado = encriptarPassword(password);
        return hashIngresado != null && hashIngresado.equals(hashAlmacenado);
    }
}
