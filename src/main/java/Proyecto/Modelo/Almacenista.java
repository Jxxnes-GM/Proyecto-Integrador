package Proyecto.Modelo;

public class Almacenista extends Persona {
    private int idAlmacenista;

    public Almacenista() {
        super("", "", ""); // Llamada al constructor de la clase padre con valores vacíos
    }

    public Almacenista(int idAlmacenista, String nombre, String apellido, String correo) {
        super(nombre, apellido, correo); // Llamada al constructor de la clase padre que es Persona
        this.idAlmacenista = idAlmacenista;
    }

    public int getIdAlmacenista() {
        return idAlmacenista;
    }

    public void setIdAlmacenista(int idAlmacenista) {
        this.idAlmacenista = idAlmacenista;
    }

}
