# Proyecto Integrador - Programación II

## Descripción

Sistema de gestión de ventas e inventario desarrollado en Java con patrón MVC (Modelo-Vista-Controlador).

## Diagrama UML

```mermaid
classDiagram
    class Persona {
        <<abstract>>
        #int id
        #String nombre
        #String apellido
        #String correo
        #String telefono
        #Boolean activo
        +Persona()
        +Persona(int, String, String, String, String)
        +toString() String
    }

    class Cliente {
        -String tipoDocumento
        -String direccion
        -String password
        -Boolean estado
        -List historialCompras
        +Cliente()
        +Cliente(int, String, String, String, String, String, String, String, Boolean)
        +agregarCompra(Venta)
    }

    class Empleado {
        -String codigoEmpleado
        -Cargo cargo
        -Double salario
        -Boolean estado
        -String password
        +Empleado()
        +Empleado(int, String, String, String, String, String, Cargo, Double, Boolean, String)
    }

    class Proveedor {
        -String direccion
        -Boolean estado
        +Proveedor()
        +Proveedor(int, String, String, String, String, Boolean)
    }

    class Producto {
        -int idProducto
        -String nombre
        -String codigo
        -Categoria categoria
        -String descripcion
        -int cantidad
        -int garantiaMeses
        -LocalDateTime fechaActualizacion
        -double precioCompra
        -double precioVenta
        -double porcentajeGanancia
        +Producto()
        +Producto(int, String, Categoria, String, double, double, int)
    }

    class Venta {
        -int idVenta
        -String numeroFactura
        -LocalDateTime fecha
        -String tipoVenta
        -String estado
        -Cliente cliente
        -Empleado empleado
        -MetodoPago metodoPago
        -List detalles
        -double total
        +Venta()
        +Venta(int, String, LocalDateTime, String, String, Cliente, Empleado, MetodoPago, List)
        +calcularTotal()
    }

    class Detalle_Venta {
        -int id_detalle
        -Venta venta
        -Producto producto
        -int cantidad
        -double precioUnitario
        -double subtotal
        +Detalle_Venta()
        +Detalle_Venta(int, Venta, Producto, int, double, double)
        +calcularSubtotal()
    }

    class Carrito {
        -Cliente cliente
        -List items
        -double total
        +Carrito(Cliente)
        +agregarProducto(Producto, int)
        +eliminarProducto(int)
        +estaVacio() boolean
        +vaciar()
        +recalcularTotal()
    }

    class ItemCarrito {
        -Producto producto
        -int cantidad
        -double precioUnitario
        -double subtotal
        +ItemCarrito(Producto, int)
        +actualizarCantidad(int)
    }

    class Cargo {
        -String nombreCargo
        -String descripcion
        +Cargo()
        +Cargo(String, String)
    }

    class Categoria {
        -int idCategoria
        -String nombre
        -String descripcion
        +Categoria()
        +Categoria(int, String, String)
    }

    class MetodoPago {
        -int idMetodoPago
        -String tipo
        +MetodoPago()
        +MetodoPago(int, String)
    }

    class Inventario {
        -int idInventario
        -Producto producto
        -int cantidad
        -String tipoMovimiento
        -LocalDateTime fechaMovimiento
        -String observacion
        +Inventario()
        +Inventario(int, Producto, int, String, LocalDateTime, String)
    }

    Persona <|-- Cliente
    Persona <|-- Empleado
    Persona <|-- Proveedor
    Producto "1" --> "1" Categoria
    Empleado "1" --> "1" Cargo
    Venta "1" --> "1" Cliente
    Venta "1" --> "1" Empleado
    Venta "1" --> "1" MetodoPago
    Venta "1" --> "*" Detalle_Venta
    Detalle_Venta "1" --> "1" Producto
    Carrito "1" --> "1" Cliente
    Carrito "1" --> "*" ItemCarrito
    ItemCarrito "1" --> "1" Producto
    Inventario "1" --> "1" Producto
```

## Estructura del Proyecto

```
proyecto-integrador-pii/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── Proyecto/
│   │   │       ├── Main.java
│   │   │       ├── Controlador/
│   │   │       │   └── Controlador.java
│   │   │       ├── Modelo/
│   │   │       │   ├── Cargo.java
│   │   │       │   ├── Carrito.java
│   │   │       │   ├── Categoria.java
│   │   │       │   ├── Cliente.java
│   │   │       │   ├── Detalle_Venta.java
│   │   │       │   ├── Empleado.java
│   │   │       │   ├── Inventario.java
│   │   │       │   ├── ItemCarrito.java
│   │   │       │   ├── MetodoPago.java
│   │   │       │   ├── Persona.java
│   │   │       │   ├── Producto.java
│   │   │       │   ├── Proveedor.java
│   │   │       │   └── Venta.java
│   │   │       └── Vista/
│   │   │           └── Vista.java
│   │   └── resources/
│   └── test/
│       └── java/
├── target/
├── pom.xml
└── README.md
```

## Tecnologías Utilizadas

- Java
- Maven
- Patrón MVC

## Autor

Juanes
