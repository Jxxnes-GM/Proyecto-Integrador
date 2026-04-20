package Proyecto.Modelo;

public abstract class Persona {

    // Atributos protegidos - accesibles en clases hijas
    protected int id;
    protected String nombre;
    protected String apellido;
    protected String correo;
    protected String telefono;
    protected Boolean activo;

    // Constructor vacio
    public Persona() {
    }

    // Constructor con parametros
    public Persona(int id, String nombre, String apellido, String correo, String telefono) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.telefono = telefono;
    }

    // Getters y Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        if (id < 0) {
            throw new IllegalArgumentException("El ID no puede ser negativo");
        }
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public String getApellido() {
        return apellido;
    }

   public void setApellido(String apellido) {
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido no puede estar vacío");
        }
         this.apellido = apellido.trim();
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty()) {
            throw new IllegalArgumentException("El correo no puede estar vacío");
        }
        // Validación básica de formato de email
        if (!correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Formato de correo inválido");
        }
        this.correo = correo.trim().toLowerCase();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        if (telefono != null && !telefono.trim().isEmpty()) {
            // Validación básica - solo números, espacios, guiones, paréntesis y +
            if (!telefono.matches("^[\\d\\s\\-\\(\\)\\+]+$")) {
                throw new IllegalArgumentException("Formato de teléfono inválido");
            }
        }
        this.telefono = telefono != null ? telefono.trim() : null;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Nombre: %s, Apellido: %s, Correo: %s", id, nombre, apellido, correo);
    }

    // Metodos
    //

}
