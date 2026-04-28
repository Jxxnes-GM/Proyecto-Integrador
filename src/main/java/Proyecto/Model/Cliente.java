package Proyecto.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Cliente extends Persona {
    // Atributos
    private String passwordHash;
    private LocalDateTime fechaRegistro;
    private List<Documento> historialCompras;

    // Constructor sin parámetros
    public Cliente() {
        super();
        this.fechaRegistro = LocalDateTime.now();
        this.historialCompras = new ArrayList<>();
    }

    // Constructor con parámetros
    public Cliente(int id, String nombre, String apellido, String documento, String telefono,
            String email, String direccion, String passwordHash) {
        super(id, nombre, apellido, documento, telefono, email, direccion);
        this.passwordHash = passwordHash;
        this.fechaRegistro = LocalDateTime.now();
        this.historialCompras = new ArrayList<>();
    }

    // Getters y Setters

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        this.passwordHash = passwordHash.trim();
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("La fecha de registro no puede ser nula");
        }
        this.fechaRegistro = fechaRegistro;
    }

    public List<Documento> getHistorialCompras() {
        return historialCompras;
    }

    public void setHistorialCompras(List<Documento> historialCompras) {
        this.historialCompras = historialCompras != null ? historialCompras : new ArrayList<>();
    }

    /**
     * Registra una compra en el historial
     * 
     * @param documento La venta realizada
     */
    public void agregarCompra(Documento documento) {
        if (documento != null) {
            this.historialCompras.add(documento);
        }
    }

}