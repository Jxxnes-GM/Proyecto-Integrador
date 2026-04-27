package Proyecto.dao;

import Proyecto.Model.Cliente;
import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonaDAO {

    // Crear cliente
    public boolean crearCliente(Cliente cliente) {
        String sql = "INSERT INTO persona (tipo_enum, nombre, apellido, documento, telefono, email, direccion, activo) " +
                     "VALUES ('Cliente', ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellido());
            pstmt.setString(3, cliente.getTipoDocumento());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getCorreo());
            pstmt.setString(6, cliente.getDireccion());
            pstmt.setBoolean(7, true);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al crear cliente: " + e.getMessage());
        }
        return false;
    }

    // Obtener cliente por ID
    public Cliente obtenerClientePorId(int idPersona) {
        String sql = "SELECT * FROM persona WHERE id_persona = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idPersona);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCliente(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente: " + e.getMessage());
        }
        return null;
    }

    // Obtener cliente por email (para login)
    public Cliente obtenerClientePorEmail(String email) {
        String sql = "SELECT * FROM persona WHERE email = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearCliente(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener cliente por email: " + e.getMessage());
        }
        return null;
    }

    // Verificar si el email existe
    public boolean emailExiste(String email) {
        String sql = "SELECT COUNT(*) FROM persona WHERE email = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar email: " + e.getMessage());
        }
        return false;
    }

    // Obtener todos los clientes
    public List<Cliente> obtenerTodosLosClientes() {
        String sql = "SELECT * FROM persona WHERE activo = true";
        List<Cliente> clientes = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clientes.add(mapearCliente(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener clientes: " + e.getMessage());
        }
        return clientes;
    }

    // Actualizar cliente
    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE persona SET nombre = ?, apellido = ?, documento = ?, " +
                     "telefono = ?, email = ?, direccion = ? WHERE id_persona = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getApellido());
            pstmt.setString(3, cliente.getTipoDocumento());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getCorreo());
            pstmt.setString(6, cliente.getDireccion());
            pstmt.setInt(7, cliente.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cliente: " + e.getMessage());
        }
        return false;
    }

    // Actualizar contraseña
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

    // Eliminar cliente (soft delete)
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

    // Mapear ResultSet a Cliente
    private Cliente mapearCliente(ResultSet rs) throws SQLException {
        return new Cliente(
            rs.getInt("id_persona"),
            rs.getString("nombre"),
            rs.getString("apellido"),
            rs.getString("email"),
            rs.getString("telefono"),
            rs.getString("documento"),
            rs.getString("contrasena_hash"),
            rs.getString("direccion"),
            rs.getBoolean("activo")
        );
    }
}
