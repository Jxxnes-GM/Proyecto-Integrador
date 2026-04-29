package Proyecto.Model;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Modelo de datos para representar una compra (documento de venta)
 */
public class Compra {
    private int id;
    private LocalDate fecha;
    private List<DetalleCompra> detalles;
    private double total;
    private String estado;

    public Compra(int id, LocalDate fecha, List<DetalleCompra> detalles, double total, String estado) {
        this.id = id;
        this.fecha = fecha;
        this.detalles = detalles;
        this.total = total;
        this.estado = estado;
    }

    // Getters
    public int getId() {
        return id;
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
     * Crea una Compra desde un Map devuelto por la DAO
     */
    public static Compra fromMap(Map<String, Object> data) {
        int id = ((Number) data.get("idDocumento")).intValue();
        String fechaStr = (String) data.get("fecha");
        LocalDate fecha = LocalDate.parse(fechaStr);
        double total = ((Number) data.get("total")).doubleValue();
        String estado = (String) data.get("estado");

        return new Compra(id, fecha, List.of(), total, estado);
    }

    @Override
    public String toString() {
        return "Compra{" +
                "id=" + id +
                ", fecha=" + fecha +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                '}';
    }
}
