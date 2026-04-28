package Proyecto.Model;

public abstract class Persona {

    // Atributos protegidos - accesibles en clases hijas
    protected int id;
    protected String nombre;
    protected String apellido;
    protected String documento;
    protected String telefono;
    protected String email;
    protected String direccion;
    protected Boolean activo;

    // Constructor vacio
    public Persona() {
    }

    // Constructor con parametros
    public Persona(int id, String nombre, String apellido, String documento, String telefono, String email,
            String direccion) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.documento = documento;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.activo = true;
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

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento no puede estar vacío");
        }
        this.documento = documento.trim();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        // Validación básica de formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Formato de email inválido");
        }
        this.email = email.trim().toLowerCase();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección no puede estar vacía");
        }
        this.direccion = direccion.trim();
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo != null ? activo : true;
    }

    @Override
    public String toString() {
        return String.format("ID: %d, Nombre: %s, Apellido: %s, Documento: %s, Email: %s, Activo: %s",
                id, nombre, apellido, documento, email, activo);
    }
}
