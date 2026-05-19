package Proyecto.dao;

import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class InventarioDAO {

    /**
     * Registra un movimiento de inventario.
     *
     * CORRECCION PRINCIPAL: subtotal_linea es una columna GENERATED ALWAYS AS
     * (cantidad * precio_unitario) STORED en MySQL. Insertar en ella directamente
     * lanza un error. Se inserta precio_unitario y MySQL calcula subtotal_linea
     * de forma automatica.
     *
     * Si idEmpleado <= 0 se inserta NULL (compra de cliente sin empleado asignado).
     */
    public boolean registrarMovimiento(int idDocumento, int idProducto,
                                       int idEmpleado, int cantidad, double subtotal) {

        double precioUnitario = (cantidad > 0) ? (subtotal / cantidad) : 0.0;

        String sql = "INSERT INTO movimiento_inventario " +
                     "(id_documento, id_producto, id_empleado, " +
                     " cantidad, precio_unitario, fecha_movimiento) " +
                     "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idDocumento);
            pstmt.setInt(2, idProducto);

            if (idEmpleado > 0) {
                pstmt.setInt(3, idEmpleado);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }

            pstmt.setInt(4, cantidad);
            pstmt.setDouble(5, precioUnitario);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar movimiento: " + e.getMessage());
        }
        return false;
    }

    public List<Map<String, Object>> obtenerMovimientosPorDocumento(int idDocumento) {
        String sql = "SELECT * FROM movimiento_inventario WHERE id_documento = ?";
        List<Map<String, Object>> movimientos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idDocumento);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("idMovimiento", rs.getInt("id_movimiento"));
                m.put("idProducto",   rs.getInt("id_producto"));
                m.put("cantidad",     rs.getInt("cantidad"));
                m.put("subtotal",     rs.getDouble("subtotal_linea"));
                m.put("fecha",        rs.getTimestamp("fecha_movimiento"));
                movimientos.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos: " + e.getMessage());
        }
        return movimientos;
    }

    public List<Map<String, Object>> obtenerMovimientosPorProducto(int idProducto) {
        String sql = "SELECT * FROM movimiento_inventario " +
                     "WHERE id_producto = ? ORDER BY fecha_movimiento DESC LIMIT 50";
        List<Map<String, Object>> movimientos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> m = new HashMap<>();
                m.put("idMovimiento", rs.getInt("id_movimiento"));
                m.put("cantidad",     rs.getInt("cantidad"));
                m.put("subtotal",     rs.getDouble("subtotal_linea"));
                m.put("fecha",        rs.getTimestamp("fecha_movimiento"));
                movimientos.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener movimientos por producto: " + e.getMessage());
        }
        return movimientos;
    }

    public int obtenerStockActual(int idProducto) {
        String sql = "SELECT stock_actual FROM producto WHERE id_producto = ?";
        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idProducto);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("stock_actual");

        } catch (SQLException e) {
            System.err.println("Error al obtener stock: " + e.getMessage());
        }
        return 0;
    }

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

    public List<Map<String, Object>> obtenerProductosConStockBajo() {
        String sql = "SELECT id_producto, nombre, stock_actual, stock_minimo " +
                     "FROM producto WHERE stock_actual <= stock_minimo AND activo = true";
        List<Map<String, Object>> productos = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             Statement stmt = conexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> p = new HashMap<>();
                p.put("idProducto",  rs.getInt("id_producto"));
                p.put("nombre",      rs.getString("nombre"));
                p.put("stockActual", rs.getInt("stock_actual"));
                p.put("stockMinimo", rs.getInt("stock_minimo"));
                productos.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener productos con stock bajo: " + e.getMessage());
        }
        return productos;
    }
}
