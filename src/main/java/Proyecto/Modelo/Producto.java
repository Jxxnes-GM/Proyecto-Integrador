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
    private double precio;

  
    // Constructor
    public Producto(int idProducto, String nombre, String categoria, String descripcion, double precio){ 
        this.idProducto = idProducto; 
        this.nombre=nombre; 
        this.categoria=categoria; 
        this.descripcion=descripcion; 
        this.precio=precio; 
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

    public double getPrecio() { return precio; } 
    public void setPrecio(double precio) { this.precio = precio; }

   
    public String toString(){ 
        return "Producto{id="+idProducto+", nombre='"+nombre+"', categoria='"+categoria+"', precio="+precio+"}"; }

}
