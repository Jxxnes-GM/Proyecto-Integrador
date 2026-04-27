package Proyecto.dao;

import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class DocumentoDAO {

    // Crear documento
    public int crearDocumento(int idTipoDocumento, int idPersona, int idEmpleado, double descuento, double total, String observaciones) {
        String sql = "INSERT INTO documento (id_tipo_documento, id_persona, id_empleado, descuento, total, observaciones, fecha_documento) " +
                     "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idTipoDocumento);
            pstmt.setInt(2, idPersona);
            pstmt.setInt(3, idEmpleado);
            pstmt.setDouble(4, descuento);
            pstmt.setDouble(5, total);
            pstmt.setString(6, observaciones);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al crear documento: " + e.getMessage());
        }
        return -1;
    }

    // Obtener documento por ID
    public Map<String, Object> obtenerDocumentoPorId(int idDocumento) {
        String sql = "SELECT * FROM documento WHERE id_documento = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idDocumento);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapearDocumento(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener documento: " + e.getMessage());
        }
        return null;
    }

    // Obtener documentos por cliente
    public List<Map<String, Object>> obtenerDocumentosPorCliente(int idCliente) {
        String sql = "SELECT * FROM documento WHERE id_persona = ? ORDER BY fecha_documento DESC";
        List<Map<String, Object>> documentos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documentos.add(mapearDocumento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener documentos del cliente: " + e.getMessage());
        }
        return documentos;
    }

    // Obtener documentos por tipo
    public List<Map<String, Object>> obtenerDocumentosPorTipo(int idTipoDocumento) {
        String sql = "SELECT * FROM documento WHERE id_tipo_documento = ? ORDER BY fecha_documento DESC";
        List<Map<String, Object>> documentos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idTipoDocumento);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                documentos.add(mapearDocumento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener documentos por tipo: " + e.getMessage());
        }
        return documentos;
    }

    // Obtener todos los documentos
    public List<Map<String, Object>> obtenerTodosLosDocumentos() {
        String sql = "SELECT * FROM documento ORDER BY fecha_documento DESC LIMIT 100";
        List<Map<String, Object>> documentos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                documentos.add(mapearDocumento(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener documentos: " + e.getMessage());
        }
        return documentos;
    }

    // Actualizar documento
    public boolean actualizarDocumento(int idDocumento, double descuento, double total, String observaciones) {
        String sql = "UPDATE documento SET descuento = ?, total = ?, observaciones = ? WHERE id_documento = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setDouble(1, descuento);
            pstmt.setDouble(2, total);
            pstmt.setString(3, observaciones);
            pstmt.setInt(4, idDocumento);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar documento: " + e.getMessage());
        }
        return false;
    }

    private Map<String, Object> mapearDocumento(ResultSet rs) throws SQLException {
        Map<String, Object> documento = new HashMap<>();
        documento.put("idDocumento", rs.getInt("id_documento"));
        documento.put("idTipoDocumento", rs.getInt("id_tipo_documento"));
        documento.put("idPersona", rs.getInt("id_persona"));
        documento.put("idEmpleado", rs.getInt("id_empleado"));
        documento.put("descuento", rs.getDouble("descuento"));
        documento.put("total", rs.getDouble("total"));
        documento.put("observaciones", rs.getString("observaciones"));
        documento.put("fecha", rs.getTimestamp("fecha_documento"));
        return documento;
    }
}
