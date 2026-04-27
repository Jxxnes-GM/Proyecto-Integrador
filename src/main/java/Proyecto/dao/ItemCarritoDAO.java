package Proyecto.dao;

import Proyecto.Model.ItemCarrito;
import Proyecto.Model.Producto;
import Proyecto.util.conexionBD;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemCarritoDAO {

    // Agregar item al carrito
    public boolean agregarItemAlCarrito(int idCarrito, int idProducto, int cantidad) {
        String sql = "INSERT INTO item_carrito (id_carrito, id_producto, cantidad) VALUES (?, ?, ?)";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idCarrito);
            pstmt.setInt(2, idProducto);
            pstmt.setInt(3, cantidad);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al agregar item al carrito: " + e.getMessage());
        }
        return false;
    }

    // Obtener items del carrito
    public List<ItemCarrito> obtenerItemsDelCarrito(int idCarrito) {
        String sql = "SELECT ic.id_item, ic.cantidad, p.* FROM item_carrito ic " +
                     "INNER JOIN producto p ON ic.id_producto = p.id_producto " +
                     "WHERE ic.id_carrito = ?";
        List<ItemCarrito> items = new ArrayList<>();

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idCarrito);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                items.add(mapearItemCarrito(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener items del carrito: " + e.getMessage());
        }
        return items;
    }

    // Actualizar cantidad del item
    public boolean actualizarCantidadItem(int idItem, int cantidad) {
        String sql = "UPDATE item_carrito SET cantidad = ? WHERE id_item = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, cantidad);
            pstmt.setInt(2, idItem);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar cantidad: " + e.getMessage());
        }
        return false;
    }

    // Eliminar item del carrito
    public boolean eliminarItemDelCarrito(int idItem) {
        String sql = "DELETE FROM item_carrito WHERE id_item = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idItem);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar item: " + e.getMessage());
        }
        return false;
    }

    // Limpiar carrito
    public boolean limpiarCarrito(int idCarrito) {
        String sql = "DELETE FROM item_carrito WHERE id_carrito = ?";

        try (Connection conexion = conexionBD.obtenerConexion();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {

            pstmt.setInt(1, idCarrito);
            return pstmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            System.err.println("Error al limpiar carrito: " + e.getMessage());
        }
        return false;
    }

    private ItemCarrito mapearItemCarrito(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setIdProducto(rs.getInt("id_producto"));
        producto.setNombre(rs.getString("nombre"));
        producto.setPrecioVenta(rs.getDouble("precio_venta"));

        return new ItemCarrito(producto, rs.getInt("cantidad"));
    }
}
