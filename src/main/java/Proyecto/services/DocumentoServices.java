package Proyecto.services;


import Proyecto.dao.DocumentoDAO;
import Proyecto.dao.InventarioDAO;
import Proyecto.dao.CarritoDAO;
import Proyecto.dao.ItemCarritoDAO;
import Proyecto.Model.Carrito;
import Proyecto.Model.ItemCarrito;
import java.util.List;
import java.util.Map;

public class DocumentoServices {

    private DocumentoDAO documentoDAO;
    private InventarioDAO inventarioDAO;
    private CarritoDAO carritoDAO;
    private ItemCarritoDAO itemCarritoDAO;

    public DocumentoServices() {
        this.documentoDAO = new DocumentoDAO();
        this.inventarioDAO = new InventarioDAO();
        this.carritoDAO = new CarritoDAO();
        this.itemCarritoDAO = new ItemCarritoDAO();
    }

    // Crear documento de venta
    public int crearDocumentoVenta(int idCliente, int idEmpleado, double descuento, String observaciones) {
        // Obtener carrito del cliente
        Carrito carrito = carritoDAO.obtenerCarritoActivoDelCliente(idCliente);
        if (carrito == null || carrito.estaVacio()) {
            System.out.println("Error: Carrito vacío");
            return -1;
        }

        // Calcular total
        double total = carrito.getTotal() - descuento;
        if (total < 0) total = 0;

        // Crear documento (tipo 1 = Factura de venta)
        int idDocumento = documentoDAO.crearDocumento(
            1,
            idCliente,
            idEmpleado,
            descuento,
            total,
            observaciones
        );

        if (idDocumento != -1) {
            // Registrar movimientos de inventario y actualizar stock
            List<ItemCarrito> items = itemCarritoDAO.obtenerItemsDelCarrito(carrito.getCliente().getId());
            for (ItemCarrito item : items) {
                // Registrar movimiento
                inventarioDAO.registrarMovimiento(
                    idDocumento,
                    item.getProducto().getIdProducto(),
                    idEmpleado,
                    item.getCantidad(),
                    item.getSubtotal()
                );
                // Disminuir stock
                inventarioDAO.actualizarStock(item.getProducto().getIdProducto(), -item.getCantidad());
            }

            // Limpiar carrito
            itemCarritoDAO.limpiarCarrito(carrito.getCliente().getId());
            System.out.println("Documento de venta creado exitosamente. ID: " + idDocumento);
        }

        return idDocumento;
    }

    // Obtener documento por ID
    public Map<String, Object> obtenerDocumento(int idDocumento) {
        return documentoDAO.obtenerDocumentoPorId(idDocumento);
    }

    // Obtener documentos del cliente
    public List<Map<String, Object>> obtenerDocumentosDelCliente(int idCliente) {
        return documentoDAO.obtenerDocumentosPorCliente(idCliente);
    }

    // Obtener documentos por tipo
    public List<Map<String, Object>> obtenerDocumentosPorTipo(int idTipoDocumento) {
        return documentoDAO.obtenerDocumentosPorTipo(idTipoDocumento);
    }

    // Obtener todos los documentos
    public List<Map<String, Object>> obtenerTodosLosDocumentos() {
        return documentoDAO.obtenerTodosLosDocumentos();
    }

    // Obtener detalles del documento
    public List<Map<String, Object>> obtenerDetallesDocumento(int idDocumento) {
        return inventarioDAO.obtenerMovimientosPorDocumento(idDocumento);
    }

    // Generar reporte de ventas del cliente
    public String generarReporteVentasCliente(int idCliente) {
        List<Map<String, Object>> documentos = documentoDAO.obtenerDocumentosPorCliente(idCliente);

        if (documentos.isEmpty()) {
            return "No hay compras registradas para este cliente";
        }

        StringBuilder reporte = new StringBuilder();
        reporte.append("╔════════════════════════════════════════╗\n");
        reporte.append("║    REPORTE DE COMPRAS DEL CLIENTE     ║\n");
        reporte.append("╚════════════════════════════════════════╝\n\n");

        double totalGastado = 0;
        int cantidadCompras = 0;

        for (Map<String, Object> doc : documentos) {
            double total = (double) doc.get("total");
            totalGastado += total;
            cantidadCompras++;

            reporte.append("─────────────────────────────────\n")
                   .append("Documento: ").append(doc.get("idDocumento")).append("\n")
                   .append("  Fecha: ").append(doc.get("fecha")).append("\n")
                   .append("  Total: $").append(String.format("%.2f", total)).append("\n")
                   .append("  Observaciones: ").append(doc.get("observaciones")).append("\n\n");
        }

        reporte.append("─────────────────────────────────\n");
        reporte.append("RESUMEN:\n");
        reporte.append("  Cantidad de compras: ").append(cantidadCompras).append("\n");
        reporte.append("  Total gastado: $").append(String.format("%.2f", totalGastado)).append("\n");
        reporte.append("  Promedio por compra: $").append(String.format("%.2f", totalGastado / cantidadCompras)).append("\n");

        return reporte.toString();
    }

    // Generar reporte de ventas totales
    public String generarReporteVentasTotales() {
        List<Map<String, Object>> documentos = documentoDAO.obtenerTodosLosDocumentos();

        StringBuilder reporte = new StringBuilder();
        reporte.append("╔════════════════════════════════════════╗\n");
        reporte.append("║         REPORTE DE VENTAS TOTALES     ║\n");
        reporte.append("╚════════════════════════════════════════╝\n\n");

        double totalVentas = 0;
        double totalDescuentos = 0;
        int cantidadDocumentos = 0;

        for (Map<String, Object> doc : documentos) {
            double total = (double) doc.get("total");
            double descuento = (double) doc.get("descuento");
            totalVentas += total;
            totalDescuentos += descuento;
            cantidadDocumentos++;
        }

        reporte.append("Total de documentos: ").append(cantidadDocumentos).append("\n");
        reporte.append("Total de ventas: $").append(String.format("%.2f", totalVentas)).append("\n");
        reporte.append("Total de descuentos: $").append(String.format("%.2f", totalDescuentos)).append("\n");
        reporte.append("Venta promedio: $").append(String.format("%.2f", totalVentas / cantidadDocumentos)).append("\n");

        return reporte.toString();
    }

    // Generar factura
    public String generarFactura(int idDocumento) {
        Map<String, Object> documento = obtenerDocumento(idDocumento);

        if (documento == null) {
            return "Documento no encontrado";
        }

        StringBuilder factura = new StringBuilder();
        factura.append("╔════════════════════════════════════════╗\n");
        factura.append("║              FACTURA                 ║\n");
        factura.append("╚════════════════════════════════════════╝\n\n");

        factura.append("Número de Documento: ").append(documento.get("idDocumento")).append("\n");
        factura.append("Fecha: ").append(documento.get("fecha")).append("\n");
        factura.append("Cliente ID: ").append(documento.get("idPersona")).append("\n");
        factura.append("Empleado ID: ").append(documento.get("idEmpleado")).append("\n\n");

        // Obtener detalles
        List<Map<String, Object>> detalles = obtenerDetallesDocumento(idDocumento);
        factura.append("DETALLES:\n");
        factura.append("─────────────────────────────────\n");

        for (Map<String, Object> detalle : detalles) {
            factura.append("  Cantidad: ").append(detalle.get("cantidad")).append("\n")
                   .append("  Subtotal: $").append(String.format("%.2f", detalle.get("subtotal"))).append("\n\n");
        }

        factura.append("─────────────────────────────────\n");
        factura.append("Descuento: $").append(String.format("%.2f", documento.get("descuento"))).append("\n");
        factura.append("TOTAL: $").append(String.format("%.2f", documento.get("total"))).append("\n\n");
        factura.append("Observaciones: ").append(documento.get("observaciones")).append("\n");

        return factura.toString();
    }
}
