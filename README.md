# Proyecto Integrador - Programación II

## Descripción

Sistema de gestión de ventas e inventario desarrollado en Java con patrón MVC (Modelo-Vista-Controlador).

## Diagrama UML

```mermaid
classDiagram
    class Persona {
        #nombre: String
        #apellido: String
        #correo: String
        +Persona(nombre, apellido, correo)
        +getNombre(): String
        +setNombre(nombre): void
        +getApellido(): String
        +setApellido(apellido): void
        +getCorreo(): String
        +setCorreo(correo): void
    }

    class Usuario {
        -idUsuario: int
        -contrasena: String
        -rol: String
        +Usuario(idUsuario, nombre, apellido, correo, contrasena, rol)
        +getIdUsuario(): int
        +setIdUsuario(idUsuario): void
        +getContrasena(): String
        +setContrasena(contrasena): void
        +getRol(): String
        +setRol(rol): void
        +toString(): String
    }

    class Cliente {
        -idCliente: int
        +Cliente()
        +Cliente(idCliente, nombre, apellido, correo)
        +getIdCliente(): int
        +setIdCliente(idCliente): void
    }

    class Almacenista {
        -idAlmacenista: int
        +Almacenista()
        +Almacenista(idAlmacenista, nombre, apellido, correo)
        +getIdAlmacenista(): int
        +setIdAlmacenista(idAlmacenista): void
    }

    class Producto {
        -idProducto: int
        -nombre: String
        -categoria: String
        -descripcion: String
        -precio: double
        +Producto(idProducto, nombre, categoria, descripcion, precio)
        +getId(): int
        +setId(idProducto): void
        +getNombre(): String
        +setNombre(nombre): void
        +getCategoria(): String
        +setCategoria(categoria): void
        +getDescripcion(): String
        +setDescripcion(descripcion): void
        +getPrecio(): double
        +setPrecio(precio): void
        +toString(): String
    }

    class Venta {
        -idVenta: int
        -fecha: LocalDateTime
        -metodoPago: String
        -total: double
        -idUsuario: int
        +Venta()
        +Venta(idVenta, fecha, metodoPago, total, idUsuario)
        +getId(): int
        +setId(idVenta): void
        +getFecha(): LocalDateTime
        +setFecha(fecha): void
        +getMetodoPago(): String
        +setMetodoPago(metodoPago): void
        +getTotal(): double
        +setTotal(total): void
        +getIdUsuario(): int
        +setIdUsuario(idUsuario): void
    }

    class Detalle_Venta {
        -id_detalle: int
        -venta: Venta
        -idProducto: Producto
        -cantidad: int
        -precio_unitario: double
        -subtotal: double
        +Detalle_Venta()
        +Detalle_Venta(id_detalle, venta, idProducto, cantidad, precio_unitario, subtotal)
        +getId_detalle(): int
        +setId_detalle(id_detalle): void
        +getVenta(): Venta
        +setVenta(venta): void
        +getidProducto(): Producto
        +setProducto(idProducto): void
        +getCantidad(): int
        +setCantidad(cantidad): void
        +getPrecio_unitario(): double
        +setPrecio_unitario(precio_unitario): void
        +getSubtotal(): double
        +calcularSubtotal(): void
        +calcularDescuento(porcentaje): double
    }

    class Carrito {
        -items: Map~Integer,Integer~
        +Carrito()
        +agregar(idProducto, cantidad): void
        +eliminar(idProducto): void
        +getItems(): Map
        +estaVacio(): boolean
        +vaciar(): void
    }

    class Inventario {
        -idInventario: int
        -idProducto: int
        -cantidad: int
        -tipoMovimiento: String
        -fechaMovimiento: LocalDateTime
        -observacion: String
        +Inventario()
        +Inventario(idInventario, idProducto, cantidad, tipoMovimiento, fechaMovimiento, observacion)
        +getId(): int
        +setId(idInventario): void
        +getIdProducto(): int
        +setIdProducto(idProducto): void
        +getCantidad(): int
        +setCantidad(cantidad): void
        +getTipoMovimiento(): String
        +setTipoMovimiento(tipoMovimiento): void
        +getFechaMovimiento(): LocalDateTime
        +setFechaMovimiento(fechaMovimiento): void
        +getObservacion(): String
        +setObservacion(observacion): void
    }

    class Reporte {
    }

    Persona <|-- Usuario
    Persona <|-- Cliente
    Persona <|-- Almacenista
    Venta --> Usuario : "usa"
    Detalle_Venta --> Venta : "contiene"
    Detalle_Venta --> Producto : "incluye"
    Inventario --> Producto : "controla"
    Carrito --> Producto : "contiene"
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
