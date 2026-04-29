package Proyecto.services;

import Proyecto.dao.InventarioDAO;
import Proyecto.dao.ProductoDAO;
import Proyecto.Model.MovimientoInventario;
import java.util.List;
import java.util.Map;

public class InventarioServices {

    private InventarioDAO inventarioDAO;
    private ProductoDAO productoDAO;

    public InventarioServices() {
        this.inventarioDAO = new InventarioDAO();
        this.productoDAO = new ProductoDAO();
    }

    // Registrar movimiento de inventario
    public boolean registrarMovimiento(int idDocumento, int idProducto, int idEmpleado,
            int cantidad, double subtotal) {

        // Validar cantidad
        if (cantidad == 0) {
            System.out.println("Error: La cantidad no puede ser cero");
            return false;
        }

        // Validar que el producto existe
        if (productoDAO.obtenerProductoporId(idProducto) == null) {
            System.out.println("Error: Producto no encontrado");
            return false;
        }

        boolean registrado = inventarioDAO.registrarMovimiento(
                idDocumento,
                idProducto,
                idEmpleado,
                cantidad,
                subtotal);

        if (registrado) {
            System.out.println("Movimiento de inventario registrado exitosamente");
        }

        return registrado;
    }

    // Obtener movimientos por documento
    public List<Map<String, Object>> obtenerMovimientosPorDocumento(int idDocumento) {
        return inventarioDAO.obtenerMovimientosPorDocumento(idDocumento);
    }

    // Obtener movimientos por producto
    public List<Map<String, Object>> obtenerMovimientosPorProducto(int idProducto) {
        return inventarioDAO.obtenerMovimientosPorProducto(idProducto);
    }

    // Obtener stock actual de un producto
    public int obtenerStock(int idProducto) {
        return inventarioDAO.obtenerStockActual(idProducto);
    }

    // Actualizar stock de un producto
    public boolean actualizarStock(int idProducto, int cantidad) {
        // Validar que el producto existe
        if (productoDAO.obtenerProductoporId(idProducto) == null) {
            System.out.println("Error: Producto no encontrado");
            return false;
        }

        // Validar stock disponible en caso de disminución
        if (cantidad < 0) {
            int stockActual = obtenerStock(idProducto);
            if (stockActual + cantidad < 0) {
                System.out.println("Error: Stock insuficiente. Disponible: " + stockActual);
                return false;
            }
        }

        boolean actualizado = inventarioDAO.actualizarStock(idProducto, cantidad);
        if (actualizado) {
            String operacion = cantidad > 0 ? "Entrada" : "Salida";
            System.out.println(operacion + " de inventario registrada para producto ID: " + idProducto);
        }

        return actualizado;
    }

    // Obtener productos con stock bajo
    public List<Map<String, Object>> obtenerProductosConStockBajo() {
        return inventarioDAO.obtenerProductosConStockBajo();
    }

    // Generar alerta de inventario
    public String generarAlertaInventario() {
        List<Map<String, Object>> productosStockBajo = obtenerProductosConStockBajo();

        if (productosStockBajo.isEmpty()) {
            return "No hay alertas de inventario";
        }

        StringBuilder alerta = new StringBuilder();
        alerta.append("╔════════════════════════════════════════╗\n");
        alerta.append("║   ALERTA DE INVENTARIO - STOCK BAJO   ║\n");
        alerta.append("╚════════════════════════════════════════╝\n\n");

        for (Map<String, Object> producto : productosStockBajo) {
            alerta.append("📦 ").append(producto.get("nombre")).append("\n")
                    .append("   Stock actual: ").append(producto.get("stockActual")).append("\n")
                    .append("   Stock mínimo: ").append(producto.get("stockMinimo")).append("\n\n");
        }

        return alerta.toString();
    }

    // Generar reporte de inventario
    public String generarReporteInventario() {
        List<Map<String, Object>> productosStockBajo = obtenerProductosConStockBajo();

        StringBuilder reporte = new StringBuilder();
        reporte.append("╔════════════════════════════════════════╗\n");
        reporte.append("║      REPORTE DE INVENTARIO            ║\n");
        reporte.append("╚════════════════════════════════════════╝\n\n");

        reporte.append("Productos con stock bajo: ").append(productosStockBajo.size()).append("\n\n");

        for (Map<String, Object> producto : productosStockBajo) {
            reporte.append("─────────────────────────────────\n")
                    .append("Producto: ").append(producto.get("nombre")).append("\n")
                    .append("  Stock Actual: ").append(producto.get("stockActual")).append(" unidades\n")
                    .append("  Stock Mínimo: ").append(producto.get("stockMinimo")).append(" unidades\n\n");
        }

        reporte.append("─────────────────────────────────\n");
        reporte.append("TOTAL PRODUCTOS CON ALERTA: ").append(productosStockBajo.size()).append("\n");

        return reporte.toString();
    }

    // Validar si el stock es suficiente
    public boolean validarStockSuficiente(int idProducto, int cantidadRequerida) {
        int stockDisponible = obtenerStock(idProducto);
        return stockDisponible >= cantidadRequerida;
    }

    // Calcular valor total en inventario
    public double calcularValorTotalInventario() {
        // Implementar lógica para calcular el valor total
        // Esto requeriría acceso a todos los productos
        return 0.0;
    }

    // ── Alias de métodos para compatibilidad con vistas ────────────────────
    public List<MovimientoInventario> obtenerMovimientos() {
        // Obtener todos los movimientos desde la DAO y convertirlos
        // Para simplificar, devolvemos una lista vacía (necesitaría acceso a todos los
        // movimientos)
        return List.of();
    }

    public boolean registrarMovimiento(int idProducto, String tipo, int cantidad, String observaciones) {
        // Versión sobrecargada para compatibilidad con MovimientosView
        // Registra un movimiento simple de inventario
        if (cantidad == 0) {
            System.out.println("Error: La cantidad no puede ser cero");
            return false;
        }

        // Validar que el producto existe
        if (productoDAO.obtenerProductoporId(idProducto) == null) {
            System.out.println("Error: Producto no encontrado");
            return false;
        }

        // Actualizar stock según el tipo
        int cambioStock = tipo.equalsIgnoreCase("Entrada") ? cantidad : -cantidad;
        return actualizarStock(idProducto, cambioStock);
    }
}
