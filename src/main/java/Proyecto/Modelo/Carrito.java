package Proyecto.Modelo;
import java.util.HashMap;
import java.util.Map;

public class Carrito {
     private Map<Integer, Integer> items; 

    public Carrito() { items = new HashMap<>(); }

    public void agregar(int idProducto, int cantidad) {
        if (cantidad <= 0) return;
        items.put(idProducto, items.getOrDefault(idProducto, 0) + cantidad);
    }

    public void eliminar(int idProducto) {
        items.remove(idProducto);
    }

    public Map<Integer,Integer> getItems() { return items; }

    public boolean estaVacio() { return items.isEmpty(); }

    public void vaciar() { items.clear(); }
}
