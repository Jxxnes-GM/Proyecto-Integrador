package Proyecto.Modelo;

public class Cliente extends Persona {
    private int idCliente;

    public Cliente() {
        super("", "", ""); // Llamada al constructor de la clase padre con valores vacíos
    }

    public Cliente(int idCliente, String nombre, String apellido, String correo) {
        super(nombre, apellido, correo); // Llamada al constructor de la clase padre que es Persona
        this.idCliente = idCliente;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }


    
}