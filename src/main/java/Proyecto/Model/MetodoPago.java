package Proyecto.Modelo;

public class MetodoPago {
    private int idMetodoPago;
    private String tipo; // Efectivo, Tarjeta, Transferencia.

    public MetodoPago() {
    }

    public MetodoPago(int idMetodoPago, String tipo) {
        this.idMetodoPago = idMetodoPago;
        this.tipo = tipo;
    }

    // getters/setters
    public int getId() {
        return idMetodoPago;
    }

    public void setId(int idMetodoPago) {
        this.idMetodoPago = idMetodoPago;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

}