package Proyecto.Modelo;

public class Cliente extends Persona {
    // Atributos
    private String direccion;
    private String password;
    private Boolean estado; // activo, inactivo, etc.

    // Constructor
    public Cliente() {
        super(); // Llamada al constructor de la clase padre Persona
    }

    // Constructor con parámetros
    public Cliente(int id, String nombre, String apellido, String correo, String password, String direccion,
            Boolean estado) {
        super.nombre = nombre;
        super.apellido = apellido;
        super.correo = correo;
        super.id = id;
        this.password = password;
        this.direccion = direccion;
        this.estado = estado;
    }

    // Getters y Setters
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

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