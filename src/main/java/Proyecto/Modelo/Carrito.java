package Proyecto.Modelo;

import java.util.ArrayList;
import java.util.List;

public class Carrito {

    // Atributos
    private Cliente cliente;
    private List<ItemCarrito> items;
    private double total;

    // Constructor
    public Carrito(Cliente cliente) {
        this.cliente = cliente;
        this.items = new ArrayList<>();
        this.total = 0.0;
    }

    // Métodos
    public void agregarProducto(Producto producto, int cantidad) {
        if (cantidad <= 0)
            return;

        for (ItemCarrito item : items) {
            if (item.getProducto().getIdProducto() == producto.getIdProducto()) {
                item.actualizarCantidad(item.getCantidad() + cantidad);
                recalcularTotal();
                return;
            }
        }

        items.add(new ItemCarrito(producto, cantidad));
        recalcularTotal();
    }

    public void eliminarProducto(int idProducto) {
        items.removeIf(item -> item.getProducto().getIdProducto() == idProducto);
        recalcularTotal();
    }

    public boolean estaVacio() {
        return items.isEmpty();
    }

    public void vaciar() {
        items.clear();
        total = 0.0;
    }

    private void recalcularTotal() {
        total = 0.0;
        for (ItemCarrito item : items) {
            total += item.getSubtotal();
        }
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public double getTotal() {
        return total;
    }

    public Cliente getCliente() {
        return cliente;
    }
}