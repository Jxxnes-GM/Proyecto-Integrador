package Proyecto.Modelo;


public class Detalle_Venta { //se hereda de la clase de venta

    // Atributos
    private int id_detalle;
    private Venta venta;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    // Constructor vacío
    public Detalle_Venta() {
    }

    // Constructor con parámetros
    public Detalle_Venta(int id_detalle, Venta venta, Producto producto, int cantidad, double precioUnitario,
            double subtotal) {
        this.id_detalle = id_detalle;
        this.venta = venta;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    // Getters y Setters
    public int getId_detalle() {
        return id_detalle;
    }

    public void setId_detalle(int id_detalle) {
        this.id_detalle = id_detalle;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
        calcularSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    // Métodos
    // Método para calcular el subtotal
    private void calcularSubtotal() {
        this.subtotal = cantidad * precioUnitario;
    }
}
