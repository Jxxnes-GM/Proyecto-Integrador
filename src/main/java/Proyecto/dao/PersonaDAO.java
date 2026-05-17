package Proyecto.dao;

import Proyecto.Model.Cliente;
import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PersonaDAO — CORRECCIONES APLICADAS:
 *
 * BUG 1 — crearCliente():
 *   El original solo insertaba en tabla 'persona' pero NUNCA insertaba
 *   en tabla 'cliente' donde vive la contrasena_hash.
 *   Resultado: usuario creado pero login siempre fallaba.
 *   FIX: se usa una transacción que inserta en ambas tablas.
 *
 * BUG 2 — obtenerClientePorId():
 *   Hacía SELECT * FROM persona → esa tabla NO tiene contrasena_hash.
 *   FIX: JOIN explícito con tabla cliente + alias nombre/apellido.
 *
 * BUG 3 — mapearCliente():
 *   Usaba rs.getString("nombre") y rs.getString("apellido") pero la tabla
 *   'persona' tiene columnas 'nombres' y 'apellidos' (en plural).
 *   FIX: las queries ahora exponen alias nombre/apellido en el SELECT,
 *   así mapearCliente() funciona igual sin cambios.
 *
 * BUG 4 — tipo_enum en crearCliente():
 *   Insertaba tipo_enum='Cliente' pero el ENUM de la BD es 'CLIENTE'.
 *   FIX: se usa 'CLIENTE' en mayúsculas.
 */
public class PersonaDAO {

    // ── Crear cliente ──────────────────────────────────────────────────────────
    public boolean crearCliente(Cliente cliente) {
        String sqlPersona = "INSERT INTO persona "
                + "(tipo, nombres, apellidos, documento, telefono, email, direccion, activo) "
                + "VALUES ('CLIENTE', ?, ?, ?, ?, ?, ?, 1)";

        String sqlCliente = "INSERT INTO cliente (id_persona, contrasena_hash) VALUES (?, ?)";

        Connection conexion = null;
        try {
            conexion = conexionBD.obtenerConexion();
            conexion.setAutoCommit(false); // transacción: persona + cliente juntos

            // 1. Insertar en persona y recuperar el id generado
            int idPersona;
            try (PreparedStatement ps = conexion.prepareStatement(sqlPersona,
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, cliente.getNombre());
                ps.setString(2, cliente.getApellido());
                // documento puede ser nulo en registros de cliente web
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

            // 2. Insertar en cliente con la contraseña hasheada
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
        // JOIN con cliente para obtener contrasena_hash + alias nombre/apellido
        String sql = "SELECT p.id_persona, "
                + "p.nombres AS nombre, p.apellidos AS apellido, "
                + "p.documento, p.telefono, p.email, p.direccion, "
                + "c.contrasena_hash "
                + "FROM persona p "
                + "JOIN cliente c ON p.id_persona = c.id_persona "
                + "WHERE p.id_persona = ? AND p.tipo = 'CLIENTE'";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idPersona);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapearCliente(rs);

        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }

    // ── Obtener cliente por email (login) ──────────────────────────────────────
    // La vista ya expone los alias nombre/apellido/contrasena_hash correctos
    public Cliente obtenerClientePorEmail(String email) {
        String sql = "SELECT * FROM vista_persona_cliente WHERE email = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapearCliente(rs);

        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por email: " + e.getMessage());
        }
        return null;
    }

    // ── Verificar si el email existe ───────────────────────────────────────────
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM persona WHERE email = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;

        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }

    // ── Obtener todos los clientes ─────────────────────────────────────────────
    public List<Cliente> obtenerTodosLosClientes() {
        // Usa la vista para obtener los alias correctos
        String sql = "SELECT * FROM vista_persona_cliente";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
                Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) clientes.add(mapearCliente(rs));

        } catch (SQLException e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
        }
        return clientes;
    }

    // ── Actualizar cliente ─────────────────────────────────────────────────────
    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE persona SET nombres = ?, apellidos = ?, documento = ?, "
                + "telefono = ?, email = ?, direccion = ? WHERE id_persona = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellido());
            pstmt.setString(3, cliente.getDocumento());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getEmail());
            pstmt.setString(6, cliente.getDireccion());
            pstmt.setInt(7, cliente.getId());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
        }
        return false;
    }

    // ── Actualizar contraseña ──────────────────────────────────────────────────
    public boolean actualizarPassword(int idPersona, String passwordHash) {
        String sql = "UPDATE cliente SET contrasena_hash = ? WHERE id_persona = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, passwordHash);
            pstmt.setInt(2, idPersona);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar contraseña: " + e.getMessage());
        }
        return false;
    }

    // ── Eliminar cliente (soft delete) ─────────────────────────────────────────
    public boolean eliminarCliente(int idPersona) {
        String sql = "UPDATE persona SET activo = false WHERE id_persona = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idPersona);
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cliente: " + e.getMessage());
        }
        return false;
    }

    // ── Mapear ResultSet → Cliente ─────────────────────────────────────────────
    // Las queries exponen los alias: nombre, apellido, contrasena_hash
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
                rs.getInt("id_persona"),
                rs.getString("nombre"),        // alias correcto
                rs.getString("apellido"),       // alias correcto
                rs.getString("documento"),
                rs.getString("telefono"),
                rs.getString("email"),
                rs.getString("direccion"),
                rs.getString("contrasena_hash"));
    }
}
