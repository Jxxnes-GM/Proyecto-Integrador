-- ============================================================
--  TechZone - Base de datos migrada de MySQL a PostgreSQL
--  Compatible con Neon (PostgreSQL)
-- ============================================================

-- Crear y seleccionar la base de datos
-- En Neon ya tienes una base de datos creada, este script
-- crea todo dentro del schema público (public) por defecto.

-- ============================================================
-- TABLA: Usuario
-- ============================================================
-- 1. Crear el schema
CREATE SCHEMA IF NOT EXISTS techzone;

-- 2. Establecerlo como schema activo para esta sesión
SET search_path TO techzone;

CREATE TABLE Usuario (
    id_usuario   SERIAL PRIMARY KEY,
    nombreUsuario    VARCHAR(100),
    apellidoUsuario  VARCHAR(100),
    correoUsuario    VARCHAR(150) UNIQUE,
    contrasenaUsuario VARCHAR(150) NOT NULL,
    rol          VARCHAR(20) DEFAULT 'Cliente' CHECK (rol IN ('Administrador', 'Cliente'))
);

-- ============================================================
-- TABLA: Producto
-- ============================================================
CREATE TABLE Producto (
    id_producto      SERIAL PRIMARY KEY,
    nombreProducto   VARCHAR(150) NOT NULL,
    categoria        VARCHAR(100),
    descripcion      TEXT,
    precio           NUMERIC(10, 2) NOT NULL
);

-- ============================================================
-- TABLA: Inventario
-- ============================================================
CREATE TABLE Inventario (
    id_inventario    SERIAL PRIMARY KEY,
    id_producto      INT NOT NULL,
    cantidad         INT NOT NULL,
    tipo_movimiento  VARCHAR(10) NOT NULL CHECK (tipo_movimiento IN ('entrada', 'salida', 'ajuste')),
    fecha_movimiento TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacion      TEXT,
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- TABLA: Venta
-- ============================================================
CREATE TABLE Venta (
    id_venta     SERIAL PRIMARY KEY,
    fecha        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metodo_pago  VARCHAR(50),
    total        NUMERIC(10, 2) NOT NULL,
    id_usuario   INT,
    id_producto  INT,
    FOREIGN KEY (id_usuario)  REFERENCES Usuario(id_usuario),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- TABLA: Detalle_Venta
-- (subtotal como columna generada - soportada en PostgreSQL 12+)
-- ============================================================
CREATE TABLE Detalle_Venta (
    id_detalle       SERIAL PRIMARY KEY,
    id_venta         INT NOT NULL,
    id_producto      INT NOT NULL,
    cantidad         INT NOT NULL,
    precio_unitario  NUMERIC(10, 2) NOT NULL,
    subtotal         NUMERIC(12, 2) GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    FOREIGN KEY (id_venta)    REFERENCES Venta(id_venta),
    FOREIGN KEY (id_producto) REFERENCES Producto(id_producto)
);

-- ============================================================
-- DATOS INICIALES: Usuario
-- ============================================================
INSERT INTO Usuario (nombreUsuario, apellidoUsuario, correoUsuario, contrasenaUsuario, rol)
VALUES
    ('Juan Esteban', 'Gaviria',  'admin@techzone.com',   'admin123',  'Administrador'),
    ('Maria Jose',   'Becerra',  'mjbb102000@gmail.com', 'lunita58*', 'Cliente');
