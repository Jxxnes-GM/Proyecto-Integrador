package Proyecto.Modelo;

public class Proveedor extends Persona {
    // Atributos
    private String direccion;
    private Boolean estado; // activo, inactivo, etc.

    // Constructor
    public Proveedor() {
    }

    // Constructor con parámetros
    public Proveedor(int id, String nombre, String apellido, String correo, String direccion, Boolean estado) {
        super.nombre = nombre;
        super.apellido = apellido;
        super.correo = correo;
        super.id = id;
        this.direccion = direccion;
        this.estado = estado;
    }

    // Getters y Setters
    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

}
