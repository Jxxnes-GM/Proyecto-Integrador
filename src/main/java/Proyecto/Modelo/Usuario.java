package Proyecto.Modelo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Usuario extends Persona {
    // Atributos
    private int idUsuario; 
    private String contrasena;
    private String rol; // Administrador o Cliente


    // Constructor
    public Usuario(int idUsuario, String nombre, String apellido, String correo, String contrasena, String rol) {
        super(nombre, apellido, correo); // Llamada al constructor de la clase padre
        this.idUsuario = idUsuario;
        this.contrasena = contrasena; 
        this.rol = rol;
    }

    

    // Getters y Setters
    public int getIdUsuario() { return idUsuario; } 
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; } 
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; } 
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; } 
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasena() { return contrasena; } 
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getRol() { return rol; } 
    public void setRol(String rol) { this.rol = rol; }

    
    public String toString() {
        return "Usuario{id="+idUsuario+", nombre='"+nombre+"', apellido='"+apellido+"', correo='"+correo+"', rol='"+rol+"'}";
    }

}
