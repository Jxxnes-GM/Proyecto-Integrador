# Proyecto Integrador - Programación II

## Descripción

Sistema de gestión de ventas e inventario desarrollado en Java con patrón MVC (Modelo-Vista-Controlador).

## Diagrama UML

```mermaid
classDiagram
    class Persona {
        +id: int
        +nombre: String
        +apellido: String
        +correo: String
        +telefono: String
    }

    class Cliente {
        +direccion: String
        +password: String
        +estado: Boolean
    }

    class Proveedor {
        +direccion: String
        +estado: Boolean
    }

    class Empleado {
        +codigoEmpleado: String
        +salario: Double
        +estado: Boolean
        +password: String
    }

    class Cargo {
        +nombreCargo: String
        +descripcion: String
    }

    class Categoria {
        +idCategoria: int
        +nombre: String
        +descripcion: String
    }

    class Producto {
        +idProducto: int
        +nombre: String
        +categoria: Categoria
        +descripcion: String
        +precioCompra: double
        +precioVenta: double
        +cantidad: int
    }

    class Inventario {
        +idInventario: int
        +producto: Producto
        +cantidad: int
        +tipoMovimiento: String
        +fechaMovimiento: LocalDateTime
        +observacion: String
    }

    class MetodoPago {
        +idMetodoPago: int
        +tipo: String
    }

    class Carrito {
        +cliente: Cliente
        +items: List~ItemCarrito~
        +total: double
        +agregarProducto(producto, cantidad): void
        +eliminarProducto(idProducto): void
        +estaVacio(): boolean
        +vaciar(): void
    }

    class ItemCarrito {
        +producto: Producto
        +cantidad: int
        +precioUnitario: double
        +subtotal: double
    }

    class Venta {
        +idVenta: int
        +numeroFactura: String
        +fecha: LocalDateTime
        +tipoVenta: String
        +estado: String
        +cliente: Cliente
        +empleado: Empleado
        +metodoPago: MetodoPago
        +detalles: List~Detalle_Venta~
        +total: double
        +calcularTotal(): void
    }

    class Detalle_Venta {
        +id_detalle: int
        +venta: Venta
        +producto: Producto
        +cantidad: int
        +precioUnitario: double
        +subtotal: double
        +calcularSubtotal(): void
    }

    Persona <|-- Cliente
    Persona <|-- Proveedor
    Persona <|-- Empleado
    Empleado --> Cargo
    Producto --> Categoria
    Inventario --> Producto
    Carrito --> Cliente
    Carrito --> ItemCarrito
    ItemCarrito --> Producto
    Venta --> Cliente
    Venta --> Empleado
    Venta --> MetodoPago
    Venta --> Detalle_Venta
    Detalle_Venta --> Producto
    Detalle_Venta --> Venta
```

## Estructura del Proyecto

```
src/
├── main/
│   └── java/
│       └── Proyecto/
│           ├── Controlador/
│           │   └── Controlador.java
│           ├── Modelo/
│           │   ├── Almacenista.java
│           │   ├── Carrito.java
│           │   ├── Cliente.java
│           │   ├── Detalle_Venta.java
│           │   ├── Inventario.java
│           │   ├── Persona.java
│           │   ├── Producto.java
│           │   ├── Reporte.java
│           │   ├── Usuario.java
│           │   └── Venta.java
│           ├── Vista/
│           │   └── Vista.java
│           └── Main.java
├── pom.xml
└── README.md
```

## Tecnologías Utilizadas

- Java
- Maven
- Patrón MVC

## Autor

Juanes
