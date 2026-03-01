package Proyecto.Modelo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Producto {
    
    // Atributos
    private int idProducto;
    private String nombre;
    private String categoria;
    private String descripcion;
    private double precioCompra;
    private double precioVenta;
    private int cantidad;
    private String estado;
    private String fecha_Creacion;
    private String fecha_Modificacion;
    private String creado_Por_usuario;
    private String modificado_Por_usuario;




  
    // Constructor
    public Producto(int idProducto, String nombre, String categoria, String descripcion, double precioCompra, double precioVenta, int cantidad){ 
        this.idProducto = idProducto; 
        this.nombre=nombre; 
        this.categoria=categoria; 
        this.descripcion=descripcion; 
        this.precioCompra=precioCompra; 
        this.precioVenta=precioVenta; 
        this.cantidad=cantidad;
        this.fecha_Creacion = "2026-01-01"; // Valor por defecto
        this.fecha_Modificacion = "2026-01-01"; // Valor por defecto
        this.creado_Por_usuario = "admin"; // Valor por defecto
        this.modificado_Por_usuario = "admin"; // Valor por defecto
    }

    
    // Getters y Setters
    public int getId() { return idProducto; } 
    public void setId(int idProducto) { this.idProducto = idProducto; }

    public String getNombre() { return nombre; } 
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCategoria() { return categoria; } 
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion; } 
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVenta() { return precioVenta; } 
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }  
    
    public int getCantidad() { return cantidad; } 
    public void setCantidad(int cantidad) { this.cantidad = cantidad; } 
    
    public String getEstado() { return estado; } 
    public void setEstado(String estado) { this.estado = estado; }
    
    public String toString(){ 
        return "Producto{id="+idProducto+", nombre='"+nombre+"', categoria='"+categoria+"', precioCompra="+precioCompra+", precioVenta="+precioVenta+", cantidad="+cantidad+", estado='"+estado+"'}"; }
        public String getFecha_Creacion() { return fecha_Creacion; }

    public void setFecha_Creacion(String fecha_Creacion) { this.fecha_Creacion = fecha_Creacion; }

    public String getFecha_Modificacion() { return fecha_Modificacion; }
    public void setFecha_Modificacion(String fecha_Modificacion) { this.fecha_Modificacion = fecha_Modificacion; }

    public String getCreado_Por_usuario() { return creado_Por_usuario; }
    public void setCreado_Por_usuario(String creado_Por_usuario) { this.creado_Por_usuario = creado_Por_usuario; }

    public String getModificado_Por_usuario() { return modificado_Por_usuario; }
    public void setModificado_Por_usuario(String modificado_Por_usuario) { this.modificado_Por_usuario = modificado_Por_usuario; }
   
}
