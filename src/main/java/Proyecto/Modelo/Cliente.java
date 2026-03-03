package Proyecto.Modelo;
import java.util.ArrayList;
import java.util.List;

public class Cliente extends Persona {
    // Atributos
    private String tipoDocumento;
    private String direccion;
    private String password;
    private Boolean estado; // activo, inactivo, etc.
    private List<Venta> historialCompras;


    // Constructor
    public Cliente() {
        super();
        this.estado = true;
        this.historialCompras = new ArrayList<>();
         // Llamada al constructor de la clase padre Persona
    }

    // Constructor con parámetros
    public Cliente(int id, String nombre, String apellido, String correo, String telefono, String tipoDocumento, String password, String direccion,
            Boolean estado) {
        super(id, nombre, apellido, correo, telefono); // Llamada al constructor de la clase padre Persona
        this.tipoDocumento = tipoDocumento;
        this.password = password;
        this.direccion = direccion;
        this.estado = estado != null ? estado : true;
        this.historialCompras = new ArrayList<>();
    }

    // Getters y Setters

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        if (tipoDocumento != null && !tipoDocumento.matches("CC|NIT|Pasaporte|CE|TI")) {
            throw new IllegalArgumentException("Tipo de documento inválido");
        }
        this.tipoDocumento = tipoDocumento;
    }

    public String getPassword() {
        return password;
    }

      public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        }
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

     public List<Venta> getHistorialCompras() {
        return historialCompras;
    }

    public void setHistorialCompras(List<Venta> historialCompras) {
        this.historialCompras = historialCompras != null ? historialCompras : new ArrayList<>();
    }


    /**
     * Registra una compra en el historial
     * @param venta La venta realizada
     */
    public void agregarCompra(Venta venta) {
        if (venta != null) {
            this.historialCompras.add(venta);
        }
    }

}