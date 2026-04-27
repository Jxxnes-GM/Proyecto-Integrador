package Proyecto.dao;

import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InventarioDAO {

    // Registrar movimiento de inventario
    public boolean registrarMovimiento(int idDocumento, int idProducto, int idEmpleado, int cantidad, double subtotal) {
        String sql = "INSERT INTO movimiento_inventario (id_documento, id_producto, id_empleado, cantidad, subtotal_linea, fecha_movimiento) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idDocumento);
            pstmt.setInt(2, idProducto);
            pstmt.setInt(3, idEmpleado);
            pstmt.setInt(4, cantidad);
            pstmt.setDouble(5, subtotal);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar movimiento: " + e.getMessage());
        }
        return false;
    }

    // Obtener movimientos por documento
    public List<Map<String, Object>> obtenerMovimientosPorDocumento(int idDocumento) {
        String sql = "SELECT * FROM movimiento_inventario WHERE id_documento = ?";
        List<Map<String, Object>> movimientos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idDocumento);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> movimiento = new HashMap<>();
                movimiento.put("idMovimiento", rs.getInt("id_movimiento"));
                movimiento.put("idProducto", rs.getInt("id_producto"));
                movimiento.put("cantidad", rs.getInt("cantidad"));
                movimiento.put("subtotal", rs.getDouble("subtotal_linea"));
                movimiento.put("fecha", rs.getTimestamp("fecha_movimiento"));
                movimientos.add(movimiento);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
        }
        return movimientos;
    }

    // Obtener movimientos por producto
    public List<Map<String, Object>> obtenerMovimientosPorProducto(int idProducto) {
        String sql = "SELECT * FROM movimiento_inventario WHERE id_producto = ? ORDER BY fecha_movimiento DESC LIMIT 50";
        List<Map<String, Object>> movimientos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> movimiento = new HashMap<>();
                movimiento.put("idMovimiento", rs.getInt("id_movimiento"));
                movimiento.put("cantidad", rs.getInt("cantidad"));
                movimiento.put("subtotal", rs.getDouble("subtotal_linea"));
                movimiento.put("fecha", rs.getTimestamp("fecha_movimiento"));
                movimientos.add(movimiento);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por producto: " + e.getMessage());
        }
        return movimientos;
    }

    // Obtener stock actual de un producto
    public int obtenerStockActual(int idProducto) {
        String sql = "SELECT stock_actual FROM producto WHERE id_producto = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("stock_actual");
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener stock: " + e.getMessage());
        }
        return 0;
    }

    // Actualizar stock del producto
    public boolean actualizarStock(int idProducto, int cantidad) {
        String sql = "UPDATE producto SET stock_actual = stock_actual + ? WHERE id_producto = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, idProducto);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar stock: " + e.getMessage());
        }
        return false;
    }

    // Obtener productos con stock bajo
    public List<Map<String, Object>> obtenerProductosConStockBajo() {
        String sql = "SELECT id_producto, nombre, stock_actual, stock_minimo FROM producto " +
                     "WHERE stock_actual <= stock_minimo AND activo = true";
        List<Map<String, Object>> productos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> producto = new HashMap<>();
                producto.put("idProducto", rs.getInt("id_producto"));
                producto.put("nombre", rs.getString("nombre"));
                producto.put("stockActual", rs.getInt("stock_actual"));
                producto.put("stockMinimo", rs.getInt("stock_minimo"));
                productos.add(producto);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }
        return productos;
    }
}
