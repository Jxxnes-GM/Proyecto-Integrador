package Proyecto.Modelo;

public class Empleado extends Persona {
    // Atributos
    private String codigoEmpleado;
    private Cargo cargo;
    private Double salario;
    private Boolean estado; // activo, inactivo, etc.
    private String password;

    // Constructor
    public Empleado() {
        super(); // Llamada al constructor de la clase padre Persona
    }

    // Constructor con parámetros
    public Empleado(int id, String nombre, String apellido, String correo, String telefono, String codigoEmpleado,
            Cargo cargo, Double salario, Boolean estado, String password) {
        super.nombre = nombre;
        super.apellido = apellido;
        super.correo = correo;
        super.id = id;
        super.telefono = telefono;
        this.codigoEmpleado = codigoEmpleado;
        this.cargo = cargo;
        this.salario = salario;
        this.estado = estado;
        this.password = password;
    }

    // Getters y Setters
    public String getCodigoEmpleado() {
        return codigoEmpleado;
    }

    public void setCodigoEmpleado(String codigoEmpleado) {
        this.codigoEmpleado = codigoEmpleado;
    }

    public Cargo getCargo() {
        return cargo;
    }

    public void setCargo(Cargo cargo) {
        this.cargo = cargo;
    }

    public Double getSalario() {
        return salario;
    }

    public void setSalario(Double salario) {
        this.salario = salario;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
