package Proyecto.dao;

import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para operaciones sobre la tabla documento.
 */
public class DocumentoDAO {

    /**
     * Crea un documento en la BD.
     * Si idEmpleado <= 0 se inserta NULL (compra online sin cajero asignado).
     */
    public int crearDocumento(int idTipoDocumento, int idPersona, int idEmpleado,
            double descuento, double total, String observaciones) {

        String sql = "INSERT INTO documento " +
                "(id_tipo_documento, id_persona, id_empleado, descuento, total, observaciones, fecha_documento, estado) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), 'COMPLETADA')";

        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, idTipoDocumento);
            pstmt.setInt(2, idPersona);

            if (idEmpleado > 0) {
                pstmt.setInt(3, idEmpleado);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setDouble(4, descuento);
            pstmt.setDouble(5, total);
            pstmt.setString(6, observaciones);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next())
                return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("DocumentoDAO.crearDocumento: " + e.getMessage());
        }
        return -1;
    }

    public Map<String, Object> obtenerDocumentoPorId(int idDocumento) {
        String sql = "SELECT * FROM documento WHERE id_documento = ?";
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idDocumento);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return mapearDocumento(rs);
        } catch (SQLException e) {
            System.err.println("DocumentoDAO.obtenerDocumentoPorId: " + e.getMessage());
        }
        return null;
    }

    public List<Map<String, Object>> obtenerDocumentosPorCliente(int idCliente) {
        String sql = "SELECT * FROM documento WHERE id_persona = ? ORDER BY fecha_documento DESC";
        List<Map<String, Object>> documentos = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idCliente);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                documentos.add(mapearDocumento(rs));
        } catch (SQLException e) {
            System.err.println("DocumentoDAO.obtenerDocumentosPorCliente: " + e.getMessage());
        }
        return documentos;
    }

    public List<Map<String, Object>> obtenerDocumentosPorTipo(int idTipoDocumento) {
        String sql = "SELECT * FROM documento WHERE id_tipo_documento = ? ORDER BY fecha_documento DESC";
        List<Map<String, Object>> documentos = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, idTipoDocumento);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                documentos.add(mapearDocumento(rs));
        } catch (SQLException e) {
            System.err.println("DocumentoDAO.obtenerDocumentosPorTipo: " + e.getMessage());
        }
        return documentos;
    }

    public List<Map<String, Object>> obtenerTodosLosDocumentos() {
        String sql = "SELECT * FROM documento WHERE id_tipo_documento = 1 " +
                "ORDER BY fecha_documento DESC LIMIT 200";
        List<Map<String, Object>> documentos = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                Statement stmt = conexion.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next())
                documentos.add(mapearDocumento(rs));
        } catch (SQLException e) {
            System.err.println("DocumentoDAO.obtenerTodosLosDocumentos: " + e.getMessage());
        }
        return documentos;
    }

    /**
     * Retorna las N ventas mas recientes con datos del cliente para el dashboard.
     */
    public List<Map<String, Object>> obtenerVentasRecientes(int limite) {
        String sql = "SELECT d.id_documento, d.fecha_documento, d.total, d.estado, " +
                "CONCAT(p.nombres, ' ', p.apellidos) AS nombre_cliente, " +
                "mp.nombre AS metodo_pago " +
                "FROM documento d " +
                "JOIN persona p ON d.id_persona = p.id_persona " +
                "LEFT JOIN metodo_pago mp ON d.id_metodo_pago = mp.id_metodo_pago " +
                "WHERE d.id_tipo_documento = 1 " +
                "ORDER BY d.fecha_documento DESC " +
                "LIMIT ?";

        List<Map<String, Object>> resultado = new ArrayList<>();
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setInt(1, limite);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                fila.put("idDocumento", rs.getInt("id_documento"));
                fila.put("fechaDocumento", rs.getTimestamp("fecha_documento"));
                fila.put("total", rs.getDouble("total"));
                fila.put("estado", rs.getString("estado"));
                fila.put("nombreCliente", rs.getString("nombre_cliente"));
                fila.put("metodoPago", rs.getString("metodo_pago"));
                resultado.add(fila);
            }
        } catch (SQLException e) {
            System.err.println("DocumentoDAO.obtenerVentasRecientes: " + e.getMessage());
        }
        return resultado;
    }

    public boolean actualizarDocumento(int idDocumento, double descuento, double total, String observaciones) {
        String sql = "UPDATE documento SET descuento=?, total=?, observaciones=? WHERE id_documento=?";
        try (Connection conexion = conexionBD.obtenerConexion();
                PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            pstmt.setDouble(1, descuento);
            pstmt.setDouble(2, total);
            pstmt.setString(3, observaciones);
            pstmt.setInt(4, idDocumento);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("DocumentoDAO.actualizarDocumento: " + e.getMessage());
        }
        return false;
    }

    private Map<String, Object> mapearDocumento(ResultSet rs) throws SQLException {
        Map<String, Object> doc = new HashMap<>();
        doc.put("idDocumento", rs.getInt("id_documento"));
        doc.put("idTipoDocumento", rs.getInt("id_tipo_documento"));
        doc.put("idPersona", rs.getInt("id_persona"));
        doc.put("idEmpleado", rs.getObject("id_empleado"));
        doc.put("descuento", rs.getDouble("descuento"));
        doc.put("total", rs.getDouble("total"));
        doc.put("observaciones", rs.getString("observaciones"));
        doc.put("fecha", rs.getTimestamp("fecha_documento"));
        // CORRECCION: mapear el campo estado real de la BD
        doc.put("estado", rs.getString("estado"));
        return doc;
    }
}