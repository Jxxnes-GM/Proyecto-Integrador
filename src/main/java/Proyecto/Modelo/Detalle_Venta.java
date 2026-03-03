package Proyecto.Modelo;

public class Detalle_Venta {

    // Atributos
    public int id_detalle;
    public Venta venta;
    public Producto producto;
    public int cantidad;
    public double precioUnitario;
    public double subtotal;

    // Constructor vacio
    public Detalle_Venta() {

    }

  // Constructor con parametros
    public Detalle_Venta(int id_detalle, Venta venta, Producto producto, int cantidad, double precioUnitario, double subtotal) {
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

    public Producto getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(Producto idProducto) {
        this.idProducto = idProducto;

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

    public void setPrecio_unitario(double precio_unitario) {
        this.precio_unitario = precio_unitario;

        calcularSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    // Metodos
    // Metodo para calcular el subtotal
    private void calcularSubtotal() {

        this.subtotal = cantidad * precioUnitario;

        this.subtotal = cantidad * precio_unitario;

    }

}
