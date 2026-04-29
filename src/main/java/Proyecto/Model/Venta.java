package Proyecto.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Modelo de datos para representar una venta (similar a Compra pero con más
 * información del cliente)
 */
public class Venta {
    private int id;
    private Cliente cliente;
    private LocalDate fecha;
    private List<DetalleCompra> detalles;
    private double total;
    private String estado;

    public Venta(int id, Cliente cliente, LocalDate fecha, List<DetalleCompra> detalles, double total, String estado) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.detalles = detalles;
        this.total = total;
        this.estado = estado;
    }

    // Getters
    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public List<DetalleCompra> getDetalles() {
        return detalles;
    }

    public double getTotal() {
        return total;
    }

    public String getEstado() {
        return estado;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public void setDetalles(List<DetalleCompra> detalles) {
        this.detalles = detalles;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Crea una Venta desde un Map devuelto por la DAO
     */
    public static Venta fromMap(Map<String, Object> data, Cliente cliente) {
        int id = ((Number) data.get("idDocumento")).intValue();
        String fechaStr = (String) data.get("fecha");
        LocalDate fecha = LocalDate.parse(fechaStr);
        double total = ((Number) data.get("total")).doubleValue();
        String estado = (String) data.get("estado");

        return new Venta(id, cliente, fecha, List.of(), total, estado);
    }

    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", cliente=" + cliente.getNombre() +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                '}';
    }
}
