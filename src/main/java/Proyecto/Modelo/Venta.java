package Proyecto.Modelo;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venta {
    private int idVenta;
    private LocalDateTime fecha;
    private String metodoPago;
    private double total;
    private int idUsuario;

    public Venta() {}

    public Venta(int idVenta, LocalDateTime fecha, String metodoPago, double total, int idUsuario) {
        this.idVenta = idVenta; 
        this.fecha = fecha; 
        this.metodoPago = metodoPago; 
        this.total = total; 
        this.idUsuario = idUsuario;
    }

    // getters/setters
    public int getId() { return idVenta; } 
    public void setId(int idVenta) { this.idVenta = idVenta; }

    public LocalDateTime getFecha() { return fecha; } 
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getMetodoPago() { return metodoPago; } 
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }

    public double getTotal() { return total; } 
    public void setTotal(double total) { this.total = total; }

    public int getIdUsuario() { return idUsuario; } 
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

}
