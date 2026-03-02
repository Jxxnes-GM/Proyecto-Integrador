package Proyecto.Modelo;

import java.util.List;
import java.time.LocalDateTime;

public class Venta {
    private int idVenta;
    private String numeroFactura;
    private LocalDateTime fecha;
    private String tipoVenta; // Tienda, Online.
    private String estado; // Pendiente, Completada, Cancelada.

    private Cliente cliente;
    private Empleado empleado;
    private MetodoPago metodoPago;

    private List<Detalle_Venta> detalles;
    private double total;

    // Constructor
    public Venta() {

    }

    // Constructor con parámetros
    public Venta(int idVenta, String numeroFactura, LocalDateTime fecha, String tipoVenta, String estado,
            Cliente cliente, Empleado empleado, MetodoPago metodoPago, List<Detalle_Venta> detalles) {
        this.idVenta = idVenta;
        this.numeroFactura = numeroFactura;
        this.fecha = fecha;
        this.tipoVenta = tipoVenta;
        this.estado = estado;
        this.cliente = cliente;
        this.empleado = empleado;
        this.metodoPago = metodoPago;
        this.detalles = detalles;
        calcularTotal();
    }

    // Getters y Setters
    public int getId() {
        return idVenta;
    }

    public void setId(int idVenta) {
        this.idVenta = idVenta;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipoVenta() {
        return tipoVenta;
    }

    public void setTipoVenta(String tipoVenta) {
        this.tipoVenta = tipoVenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Empleado getEmpleado() {
        return empleado;
    }

    public void setEmpleado(Empleado empleado) {
        this.empleado = empleado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public List<Detalle_Venta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<Detalle_Venta> detalles) {
        this.detalles = detalles;
        calcularTotal();
    }

    public double getTotal() {
        return total;
    }

    // Metodo para calcular el total de la venta sumando los subtotales de cada
    // detalle
    private void calcularTotal() {
        this.total = 0;
        for (Detalle_Venta detalle : detalles) {
            this.total += detalle.getSubtotal();
        }
    }

}
