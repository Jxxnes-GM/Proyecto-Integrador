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

    // ── NUEVO: Buscar clientes por nombre o email ──────────────────────────────
    /**
     * Busca clientes cuyo nombre, apellido o email contengan el texto indicado.
     * Usado en CotizacionView para localizar al cliente de la cotización.
     */
    public List<Cliente> buscarClientes(String query) {
        if (query == null || query.trim().isEmpty()) {
            return personaDAO.obtenerTodosLosClientes();
        }
        return personaDAO.buscarClientes(query.trim());
    }

    // ── Registrar nuevo cliente ────────────────────────────────────────────────
    public boolean registrarCliente(String nombre, String apellido, String email,
            String telefono, String tipoDocumento,
            String password, String direccion) {

        if (personaDAO.emailExiste(email)) {
            System.out.println("Error: El email ya esta registrado");
            return false;
        }
        if (!validarDatos(nombre, apellido, email, password)) return false;

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido.trim());
        cliente.setEmail(email.toLowerCase().trim());
        cliente.setTelefono(telefono);
        cliente.setPasswordHash(encriptarPassword(password));
        cliente.setDireccion(direccion);

        return personaDAO.crearCliente(cliente);
    }

    // ── Autenticación unificada ────────────────────────────────────────────────
    public Cliente autenticarCliente(String email, String password) {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            System.out.println("Error: Email y contraseña son requeridos");
            return null;
        }

        String emailLower = email.toLowerCase().trim();
        String hash       = encriptarPassword(password);

        // 1. Intentar como CLIENTE
        Cliente cliente = personaDAO.obtenerClientePorEmail(emailLower);
        if (cliente != null && hash != null && hash.equals(cliente.getPasswordHash())) {
            cliente.setRol("CLIENTE");
            System.out.println("Login exitoso como CLIENTE: " + emailLower);
            return cliente;
        }

        // 2. Intentar como EMPLEADO
        Cliente empleado = personaDAO.obtenerEmpleadoPorEmail(emailLower);
        if (empleado != null && hash != null && hash.equals(empleado.getPasswordHash())) {
            System.out.println("Login exitoso como EMPLEADO (" + empleado.getRol() + "): " + emailLower);
            return empleado;
        }

        System.out.println("Error: Credenciales incorrectas para " + emailLower);
        return null;
    }

    // ── Obtener cliente por ID ─────────────────────────────────────────────────
    public Cliente obtenerCliente(int idCliente) {
        return personaDAO.obtenerClientePorId(idCliente);
    }

    // ── Obtener todos los clientes ─────────────────────────────────────────────
    public List<Cliente> obtenerTodosLosClientes() {
        return personaDAO.obtenerTodosLosClientes();
    }

    // ── Actualizar cliente ────────────────────────────────────────────────────
    public boolean actualizarCliente(int idCliente, String nombre, String apellido,
            String telefono, String direccion) {
        Cliente cliente = personaDAO.obtenerClientePorId(idCliente);
        if (cliente == null) { System.out.println("Error: Cliente no encontrado"); return false; }
        cliente.setNombre(nombre.trim());
        cliente.setApellido(apellido.trim());
        cliente.setTelefono(telefono);
        cliente.setDireccion(direccion);
        return personaDAO.actualizarCliente(cliente);
    }

    // ── Cambiar contraseña ────────────────────────────────────────────────────
    public boolean cambiarPassword(int idCliente, String passwordActual, String passwordNueva) {
        Cliente cliente = personaDAO.obtenerClientePorId(idCliente);
        if (cliente == null) { System.out.println("Error: Cliente no encontrado"); return false; }
        if (!verificarPassword(passwordActual, cliente.getPasswordHash())) {
            System.out.println("Error: Contraseña actual incorrecta");
            return false;
        }
        if (passwordNueva == null || passwordNueva.length() < 6) {
            System.out.println("Error: La nueva contraseña debe tener al menos 6 caracteres");
            return false;
        }
        return personaDAO.actualizarPassword(idCliente, encriptarPassword(passwordNueva));
    }

    // ── Desactivar cliente ────────────────────────────────────────────────────
    public boolean desactivarCliente(int idCliente) {
        return personaDAO.eliminarCliente(idCliente);
    }

    // ── Helpers privados ──────────────────────────────────────────────────────
    private boolean validarDatos(String nombre, String apellido, String email, String password) {
        if (nombre == null || nombre.trim().isEmpty())     { System.out.println("Error: nombre requerido"); return false; }
        if (apellido == null || apellido.trim().isEmpty()) { System.out.println("Error: apellido requerido"); return false; }
        if (email == null || !email.contains("@"))         { System.out.println("Error: email invalido"); return false; }
        if (password == null || password.length() < 6)    { System.out.println("Error: contraseña minimo 6 caracteres"); return false; }
        return true;
    }

    private String encriptarPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error al encriptar: " + e.getMessage());
            return null;
        }
    }

    private boolean verificarPassword(String password, String hashAlmacenado) {
        String hashIngresado = encriptarPassword(password);
        return hashIngresado != null && hashIngresado.equals(hashAlmacenado);
    }
}
