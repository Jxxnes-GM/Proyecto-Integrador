# Proyecto Integrador - Programación II

## Descripción

Sistema de gestión de ventas e inventario desarrollado en Java con patrón MVC (Modelo-Vista-Controlador).

## Diagrama UML

```mermaid
classDiagram
    %% Clase abstracta Persona (Base)
    class Persona {
        #id: int
        #nombre: String
        #apellido: String
        #correo: String
        #telefono: String
        #activo: Boolean
        +getId()* int
        +setId(int)* void
        +getNombre()* String
        +setNombre(String)* void
        +getApellido()* String
        +setApellido(String)* void
        +getCorreo()* String
        +setCorreo(String)* void
        +getTelefono()* String
        +setTelefono(String)* void
    }

    %% Clases que heredan de Persona
    class Cliente {
        -tipoDocumento: String
        -direccion: String
        -password: String
        -estado: Boolean
        -historialCompras: List~Venta~
        +create(Cliente) boolean
        +read(id) Cliente
        +update(Cliente) boolean
        +delete(id) boolean
        +getTipoDocumento() String
        +setTipoDocumento(String) void
        +getDireccion() String
        +setDireccion(String) void
        +getPassword() String
        +setPassword(String) void
        +getEstado() Boolean
        +setEstado(Boolean) void
    }

    class Empleado {
        -codigoEmpleado: String
        -cargo: Cargo
        -salario: Double
        -estado: Boolean
        -password: String
        +create(Empleado) boolean
        +read(id) Empleado
        +update(Empleado) boolean
        +delete(id) boolean
       
    }

    class Proveedor {
        -direccion: String
        -estado: Boolean
        +create(Proveedor) boolean
        +read(id) Proveedor
        +update(Proveedor) boolean
        +delete(id) boolean
        
    }

    %% Clase Cargo (Entidad por sí sola)
    class Cargo {
        -nombreCargo: String
        -descripcion: String
        +create(Cargo) boolean
        +read(nombre) Cargo
        +update(Cargo) boolean
        +delete(nombre) boolean
        
    }

    %% Clase Categoria (Entidad por sí sola)
    class Categoria {
        -idCategoria: int
        -nombre: String
        -descripcion: String
        +create(Categoria) boolean
        +read(idCategoria) Categoria
        +update(Categoria) boolean
        +delete(idCategoria) boolean
        
    }

    %% Clase Producto
    class Producto {
        -idProducto: int
        -nombre: String
        -categoria: Categoria
        -descripcion: String
        -precioCompra: double
        -precioVenta: double
        -cantidad: int
        +create(Producto) boolean
        +read(idProducto) Producto
        +update(Producto) boolean
        +delete(idProducto) boolean
        
    }

    %% Clase MetodoPago
    class MetodoPago {
        -idMetodoPago: int
        -tipo: String
        +create(MetodoPago) boolean
        +read(idMetodoPago) MetodoPago
        +delete(idMetodoPago) boolean
        
    }

    %% Clase Inventario
    class Inventario {
        -idInventario: int
        -producto: Producto
        -cantidad: int
        -tipoMovimiento: String
        -fechaMovimiento: LocalDateTime
        -observacion: String
        +create(Inventario) boolean
        +read(idInventario) Inventario
        +update(Inventario) boolean
        +delete(idInventario) boolean
        
    }

    %% Clase Venta
    class Venta {
        -idVenta: int
        -numeroFactura: String
        -fecha: LocalDateTime
        -tipoVenta: String
        -estado: String
        -cliente: Cliente
        -empleado: Empleado
        -metodoPago: MetodoPago
        -detalles: List~Detalle_Venta~
        -total: double
        +create(Venta) boolean
        +read(idVenta) Venta
        +update(Venta) boolean
        +delete(idVenta) boolean
        +calcularTotal() void
    }

    %% Clase Detalle_Venta
    class Detalle_Venta {
        -id_detalle: int
        -venta: Venta
        -producto: Producto
        -cantidad: int
        -precioUnitario: double
        -subtotal: double
        +create(Detalle_Venta) boolean
        +read(id_detalle) Detalle_Venta
        +update(Detalle_Venta) boolean
        +delete(id_detalle) boolean
        +calcularSubtotal() void
    }

    %% Clase Carrito
    class Carrito {
        -cliente: Cliente
        -items: List~ItemCarrito~
        -total: double
        +agregarProducto(Producto, int) void
        +eliminarProducto(int) void
        +estaVacio() boolean
        +vaciar() void
        +recalcularTotal() void

    %% Clase ItemCarrito
    class ItemCarrito {
        -producto: Producto
        -cantidad: int
        -precioUnitario: double
        -subtotal: double
        +actualizarCantidad(int) void
    }

    %% Relaciones de Herencia
    Persona <|-- Cliente
    Persona <|-- Empleado
    Persona <|-- Proveedor

    %% Relaciones de Asociación con Cardinalidades
    Empleado "1" -- "1" Cargo : tiene >
    Producto "1" -- "1" Categoria : pertenece a >
    Inventario "1" -- "1" Producto : registra >

    Venta "1" -- "1" Cliente : realiza >
    Venta "1" -- "1" Empleado : procesa >
    Venta "1" -- "1" MetodoPago : utiliza >
    Venta "1" -- "1..*" Detalle_Venta : contiene >

    Detalle_Venta "1..*" -- "1" Producto : incluye >

    Carrito "1" -- "1" Cliente : pertenece a >
    Carrito "1" -- "0..*" ItemCarrito : contiene >
    ItemCarrito "1..*" -- "1" Producto : referencia >

    Cliente "1" -- "0..*" Venta : compra >
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
