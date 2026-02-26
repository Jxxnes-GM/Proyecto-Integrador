package Proyecto.Modelo;
import java.sql.*;
import java.time.LocalDateTime;

public class Inventario {
    private int idInventario;
    private int idProducto;
    private int cantidad;
    private String tipoMovimiento;
    private LocalDateTime fechaMovimiento;
    private String observacion;

    public Inventario() {}

    public Inventario(int idInventario, int idProducto, int cantidad, String tipoMovimiento, LocalDateTime fechaMovimiento, String observacion) {
        this.idInventario = idInventario; 
        this.idProducto = idProducto; 
        this.cantidad = cantidad; 
        this.tipoMovimiento = tipoMovimiento; 
        this.fechaMovimiento = fechaMovimiento; 
        this.observacion = observacion;
    }

    // Getters y Setters
    public int getId() { return idInventario; }
    public void setId(int idInventario) { this.idInventario = idInventario; }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }

    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }

    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
