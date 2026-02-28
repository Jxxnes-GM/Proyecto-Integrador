package Proyecto.Modelo;


//clase principal de persona que se va a heredar a las clases usuario y cliente
public class Persona {

    //creamos los atrivitos de la clase persona que se van a heredar a las clases usuario y cliente
    public String nombre;
    public String apellido;
    public String correo;
 
    public Persona(String nombre, String apellido, String correo) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }  

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    
}
