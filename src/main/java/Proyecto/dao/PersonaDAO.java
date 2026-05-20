package Proyecto.dao;

import Proyecto.Model.Cliente;
import Proyecto.Model.Empleado;
import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    // ── NUEVO: Buscar clientes por nombre o email ──────────────────────────────
    /**
     * Busca clientes cuyo nombre, apellido o email contengan el texto indicado.
     * Se usa en CotizacionView para localizar el cliente sin necesidad de
     * conocer su contraseña.
     */
    public List<Cliente> buscarClientes(String query) {
        String sql =
            "SELECT p.id_persona, " +
            "p.nombres AS nombre, p.apellidos AS apellido, " +
            "p.documento, p.telefono, p.email, p.direccion, " +
            "c.contrasena_hash " +
            "FROM persona p " +
            "JOIN cliente c ON p.id_persona = c.id_persona " +
            "WHERE p.activo = 1 " +
            "  AND p.tipo = 'CLIENTE' " +
            "  AND (p.nombres   LIKE ? " +
            "    OR p.apellidos LIKE ? " +
            "    OR p.email     LIKE ?) " +
            "ORDER BY p.nombres ASC " +
            "LIMIT 10";

        List<Cliente> resultados = new ArrayList<>();
        String patron = "%" + query.trim() + "%";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultados.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar clientes: " + e.getMessage());
        }
        return resultados;
    }

    // ── NUEVO: Autenticar empleado ─────────────────────────────────────────────
    /**
     * Verifica credenciales de un empleado usando SHA2 directamente en MySQL.
     * Retorna un Cliente con el rol del cargo, o null si falla.
     */
    public Cliente obtenerEmpleadoPorEmail(String email) {
        String sql =
            "SELECT p.id_persona, " +
            "p.nombres AS nombre, p.apellidos AS apellido, " +
            "p.documento, p.telefono, p.email, p.direccion, " +
            "e.contrasena_hash, " +
            "UPPER(c.nombre) AS nombre_cargo " +
            "FROM persona p " +
            "JOIN empleado e ON p.id_persona = e.id_persona " +
            "JOIN cargo    c ON e.id_cargo   = c.id_cargo " +
            "WHERE p.email = ? AND p.tipo = 'EMPLEADO' AND p.activo = 1";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cliente empleado = mapearCliente(rs);
                String cargo = rs.getString("nombre_cargo");
                empleado.setRol(cargo != null ? cargo : "EMPLEADO");
                return empleado;
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener empleado por email: " + e.getMessage());
        }
        return null;
    }

    // ── Crear cliente ──────────────────────────────────────────────────────────
    public boolean crearCliente(Cliente cliente) {
        String sqlPersona =
            "INSERT INTO persona " +
            "(tipo, nombres, apellidos, documento, telefono, email, direccion, activo) " +
            "VALUES ('CLIENTE', ?, ?, ?, ?, ?, ?, 1)";
        String sqlCliente = "INSERT INTO cliente (id_persona, contrasena_hash) VALUES (?, ?)";

        Connection conexion = null;
        try {
            conexion = conexionBD.obtenerConexion();
            conexion.setAutoCommit(false);

            int idPersona;
            try (PreparedStatement ps = conexion.prepareStatement(
                    sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getApellido());
                String doc = (cliente.getDocumento() != null && !cliente.getDocumento().isEmpty())
                        ? cliente.getDocumento()
                        : "CLI-" + System.currentTimeMillis();
                ps.setString(3, doc);
                ps.setString(4, cliente.getTelefono());
                ps.setString(5, cliente.getEmail());
                ps.setString(6, cliente.getDireccion());
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (!rs.next()) { conexion.rollback(); return false; }
                idPersona = rs.getInt(1);
            }

            try (PreparedStatement ps2 = conexion.prepareStatement(sqlCliente)) {
                ps2.setInt(1, idPersona);
                ps2.setString(2, cliente.getPasswordHash());
                ps2.executeUpdate();
            }

            conexion.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
            try { if (conexion != null) conexion.rollback(); } catch (SQLException ex) { /* ignore */ }
        } finally {
            try { if (conexion != null) { conexion.setAutoCommit(true); conexion.close(); } }
            catch (SQLException ex) { /* ignore */ }
        }
        return false;
    }

    // ── Obtener cliente por ID ─────────────────────────────────────────────────
    public Cliente obtenerClientePorId(int idPersona) {
        String sql =
            "SELECT p.id_persona, " +
            "p.nombres AS nombre, p.apellidos AS apellido, " +
            "p.documento, p.telefono, p.email, p.direccion, " +
            "c.contrasena_hash " +
            "FROM persona p " +
            "JOIN cliente c ON p.id_persona = c.id_persona " +
            "WHERE p.id_persona = ? AND p.tipo = 'CLIENTE'";

        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearCliente(rs);
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }

    // ── Obtener cliente por email ──────────────────────────────────────────────
    public Cliente obtenerClientePorEmail(String email) {
        String sql = "SELECT * FROM vista_persona_cliente WHERE email = ?";
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearCliente(rs);
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por email: " + e.getMessage());
        }
        return null;
    }

    // ── Verificar si email existe ──────────────────────────────────────────────
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM persona WHERE email = ?";
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }

    // ── Obtener todos los clientes ─────────────────────────────────────────────
    public List<Cliente> obtenerTodosLosClientes() {
        String sql = "SELECT * FROM vista_persona_cliente";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection conn = conexionBD.obtenerConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) clientes.add(mapearCliente(rs));
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
        }
        return clientes;
    }

    // ── Actualizar cliente ─────────────────────────────────────────────────────
    public boolean actualizarCliente(Cliente cliente) {
        String sql =
            "UPDATE persona SET nombres = ?, apellidos = ?, documento = ?, " +
            "telefono = ?, email = ?, direccion = ? WHERE id_persona = ?";
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cliente.getNombre());
            ps.setString(2, cliente.getApellido());
            ps.setString(3, cliente.getDocumento());
            ps.setString(4, cliente.getTelefono());
            ps.setString(5, cliente.getEmail());
            ps.setString(6, cliente.getDireccion());
            ps.setInt(7, cliente.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
        }
        return false;
    }

    // ── Actualizar contraseña ──────────────────────────────────────────────────
    public boolean actualizarPassword(int idPersona, String passwordHash) {
        String sql = "UPDATE cliente SET contrasena_hash = ? WHERE id_persona = ?";
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, idPersona);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
        }
        return false;
    }

    // ── Eliminar cliente (soft delete) ─────────────────────────────────────────
    public boolean eliminarCliente(int idPersona) {
        String sql = "UPDATE persona SET activo = false WHERE id_persona = ?";
        try (Connection conn = conexionBD.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPersona);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
        }
        return false;
    }

    // ── Mapear ResultSet a Cliente ─────────────────────────────────────────────
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_persona"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("documento"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("direccion"),
                rs.getString("contrasena_hash"));
    }
}
