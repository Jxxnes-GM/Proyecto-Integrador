package Proyecto.Modelo;

public class Cliente extends Persona {
    // Atributos
    private int idCliente;
    private String contrasena;
    private String estado; // activo, inactivo, etc.

    // Constructor
    public Cliente() {
        super(); // Llamada al constructor de la clase padre Persona
    }

    public Cliente(int idCliente, String nombre, String apellido, String correo, String contrasena, String estado) {
        super.nombre = nombre;
        super.apellido = apellido;
        super.correo = correo;
        this.idCliente = idCliente;
        this.contrasena = contrasena;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

}