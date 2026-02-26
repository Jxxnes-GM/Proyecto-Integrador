package Proyecto.Modelo;

public class Detalle_Venta {

    // Atributos
    public int id_detalle;
    public Venta venta;
    public Producto idProducto;
    public int cantidad;
    public double precio_unitario;
    public double subtotal;

    // Constructor vacio
    public Detalle_Venta (){

    }

    public Detalle_Venta(int id_detalle, Venta venta, Producto idProducto, int cantidad, double precio_unitario, double subtotal) {
        this.id_detalle = id_detalle;
        this.venta = venta;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
        this.precio_unitario = precio_unitario;
        this.subtotal = subtotal;
    }

    // Getters y Setters
    public int getId_detalle() {return id_detalle;}
    public void setId_detalle(int id_detalle) {this.id_detalle = id_detalle;}

    public Venta getVenta() {return venta;}
    public void setVenta(Venta venta) {this.venta = venta;}

    public Producto getidProducto() {return idProducto;}
    public void setProducto(Producto idProducto) {
        this.idProducto = idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }
    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
        calcularSubtotal();
    }

    public double getPrecio_unitario() {
        return precio_unitario;
    }
    public void setPrecio_unitario(double precio_unitario) {
        this.precio_unitario = precio_unitario;
        calcularSubtotal();
    }

    public double getSubtotal() {
        return subtotal;
    }

    
}
