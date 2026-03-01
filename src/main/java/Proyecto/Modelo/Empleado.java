package Proyecto.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Empleado extends Persona {
    // Atributos
    private int idUsuarioEmpleado;
    private String contrasena;
    private String rol; // Vendedor En Linea o Vendedor En Tienda

    // Constructor
    public Empleado(int idUsuarioEmpleado, String nombre, String apellido, String correo, String contrasena,
            String rol) {
        super(nombre, apellido, correo); // Llamada al constructor de la clase padre
        this.idUsuarioEmpleado = idUsuarioEmpleado;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters y Setters
    public int getIdUsuarioEmpleado() {
        return idUsuarioEmpleado;
    }

    public void setIdUsuarioEmpleado(int idUsuarioEmpleado) {
        this.idUsuarioEmpleado = idUsuarioEmpleado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String toString() {
        return "Empleado{id=" + idUsuarioEmpleado + ", nombre='" + nombre + "', apellido='" + apellido + "', correo='"
                + correo + "', rol='" + rol + "'}";
    }

}
