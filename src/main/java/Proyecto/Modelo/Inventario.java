package Proyecto.Modelo;

import java.time.LocalDateTime;

public class Inventario {
    private int idInventario;
    private Producto producto;
    private int cantidad;
    private String tipoMovimiento; // si es entrada o salida
    private LocalDateTime fechaMovimiento;
    private String observacion;

    public Inventario() {
    }

    public Inventario(int idInventario, Producto producto, int cantidad, String tipoMovimiento,
            LocalDateTime fechaMovimiento, String observacion) {
        this.idInventario = idInventario;
        this.producto = producto;
        this.cantidad = cantidad;
        this.tipoMovimiento = tipoMovimiento;
        this.fechaMovimiento = fechaMovimiento;
        this.observacion = observacion;
    }

    // Getters y Setters
    public int getId() {
        return idInventario;
    }

    public void setId(int idInventario) {
        this.idInventario = idInventario;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getIdProducto() {
        return producto.getIdProducto();
    }

    public void setIdProducto(int idProducto) {
        this.producto.setIdProducto(idProducto);
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public LocalDateTime getFechaMovimiento() {
        return fechaMovimiento;
    }

    public void setFechaMovimiento(LocalDateTime fechaMovimiento) {
        this.fechaMovimiento = fechaMovimiento;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
