-- =============================================================================
-- PROYECTO INTEGRADOR - TECHZONE
-- Modelo Entidad Relación - MySQL Workbench
-- VERSIÓN CORREGIDA
--   CORRECCIÓN 1: INSERT cliente/proveedor usan subconsulta en vez de id fijo
--   CORRECCIÓN 2: sp_registrar_venta — bloque etiquetado "sp_bloque:" añadido
-- =============================================================================

DROP DATABASE IF EXISTS techzone;
CREATE DATABASE techzone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE techzone;

-- =============================================================================
-- 1. TABLAS DE SOPORTE / CATÁLOGOS
-- =============================================================================

CREATE TABLE categoria (
    id_categoria   INT          NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(80)  NOT NULL,
    descripcion    VARCHAR(255),
    activo         TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_categoria)
);

CREATE TABLE metodo_pago (
    id_metodo_pago INT         NOT NULL AUTO_INCREMENT,
    nombre         VARCHAR(60) NOT NULL,
    activo         TINYINT(1)  NOT NULL DEFAULT 1,
    PRIMARY KEY (id_metodo_pago)
);

CREATE TABLE tipo_documento (
    id_tipo_documento      INT          NOT NULL AUTO_INCREMENT,
    descripcion            VARCHAR(100) NOT NULL,
    efecto_en_inventario   TINYINT      NOT NULL COMMENT '+1 = entrada, -1 = salida',
    PRIMARY KEY (id_tipo_documento),
    CONSTRAINT chk_efecto CHECK (efecto_en_inventario IN (1, -1))
);

INSERT INTO tipo_documento (descripcion, efecto_en_inventario) VALUES
    ('Factura de Venta al Cliente',        -1),
    ('Factura de Compra a Proveedor',      +1),
    ('Devolución del Cliente',             +1),
    ('Devolución a Proveedor',             -1),
    ('Ajuste de Inventario (Entrada)',     +1),
    ('Ajuste de Inventario (Salida)',      -1),
    ('Baja por mercancía en mal estado',   -1);

-- =============================================================================
-- 2. PERSONAS: CLIENTES, EMPLEADOS, PROVEEDORES
-- =============================================================================

CREATE TABLE cargo (
    id_cargo    INT         NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    PRIMARY KEY (id_cargo)
);

INSERT INTO cargo (nombre) VALUES
    ('Administrador'), ('Comprador'), ('Vendedor'), ('Cajero'), ('Bodeguero');

CREATE TABLE persona (
    id_persona   INT          NOT NULL AUTO_INCREMENT,
    tipo         ENUM('CLIENTE','EMPLEADO','PROVEEDOR') NOT NULL,
    nombres      VARCHAR(100) NOT NULL,
    apellidos    VARCHAR(100) NOT NULL,
    documento    VARCHAR(20)  NOT NULL UNIQUE,
    telefono     VARCHAR(20),
    email        VARCHAR(120) UNIQUE,
    direccion    VARCHAR(255),
    activo       TINYINT(1)   NOT NULL DEFAULT 1,
    PRIMARY KEY (id_persona)
);

CREATE TABLE cliente (
    id_persona       INT         NOT NULL,
    fecha_registro   DATE        NOT NULL DEFAULT (CURRENT_DATE),
    contrasena_hash  VARCHAR(255),
    PRIMARY KEY (id_persona),
    CONSTRAINT fk_cliente_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

CREATE TABLE empleado (
    id_persona       INT          NOT NULL,
    id_cargo         INT          NOT NULL,
    fecha_ingreso    DATE         NOT NULL,
    contrasena_hash  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id_persona),
    CONSTRAINT fk_empleado_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona),
    CONSTRAINT fk_empleado_cargo   FOREIGN KEY (id_cargo)   REFERENCES cargo(id_cargo)
);

CREATE TABLE proveedor (
    id_persona         INT          NOT NULL,
    nombre_empresa     VARCHAR(150) NOT NULL,
    nit                VARCHAR(30)  UNIQUE,
    PRIMARY KEY (id_persona),
    CONSTRAINT fk_proveedor_persona FOREIGN KEY (id_persona) REFERENCES persona(id_persona)
);

-- =============================================================================
-- 3. CATÁLOGO DE PRODUCTOS
-- =============================================================================

CREATE TABLE producto (
    id_producto      INT            NOT NULL AUTO_INCREMENT,
    id_categoria     INT            NOT NULL,
    nombre           VARCHAR(150)   NOT NULL,
    descripcion      TEXT,
    precio_compra    DECIMAL(12,2)  NOT NULL,
    precio_venta     DECIMAL(12,2)  NOT NULL,
    stock_actual     INT            NOT NULL DEFAULT 0,
    stock_minimo     INT            NOT NULL DEFAULT 0,
    activo           TINYINT(1)     NOT NULL DEFAULT 1,
    PRIMARY KEY (id_producto),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

-- =============================================================================
-- 4. DOCUMENTO
-- =============================================================================

CREATE TABLE documento (
    id_documento       INT            NOT NULL AUTO_INCREMENT,
    id_tipo_documento  INT            NOT NULL,
    id_persona         INT            NOT NULL  COMMENT 'Cliente o Proveedor según tipo',
    id_empleado        INT            COMMENT 'Empleado que registra el documento',
    id_metodo_pago     INT,
    fecha_documento    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_doc_externo VARCHAR(60)    COMMENT 'Nro. factura del proveedor o serie interna',
    subtotal           DECIMAL(14,2)  NOT NULL DEFAULT 0,
    descuento          DECIMAL(14,2)  NOT NULL DEFAULT 0,
    total              DECIMAL(14,2)  NOT NULL DEFAULT 0,
    observaciones      TEXT,
    PRIMARY KEY (id_documento),
    CONSTRAINT fk_doc_tipo      FOREIGN KEY (id_tipo_documento) REFERENCES tipo_documento(id_tipo_documento),
    CONSTRAINT fk_doc_persona   FOREIGN KEY (id_persona)        REFERENCES persona(id_persona),
    CONSTRAINT fk_doc_empleado  FOREIGN KEY (id_empleado)       REFERENCES empleado(id_persona),
    CONSTRAINT fk_doc_metpago   FOREIGN KEY (id_metodo_pago)    REFERENCES metodo_pago(id_metodo_pago)
);

-- =============================================================================
-- 5. MOVIMIENTO_INVENTARIO
-- =============================================================================

CREATE TABLE movimiento_inventario (
    id_movimiento      INT            NOT NULL AUTO_INCREMENT,
    id_documento       INT            NOT NULL  COMMENT 'FK a documento (venta o compra)',
    id_producto        INT            NOT NULL,
    id_empleado        INT            NOT NULL  COMMENT 'Bodeguero que registra',
    cantidad           INT            NOT NULL,
    precio_unitario    DECIMAL(12,2)  NOT NULL,
    subtotal_linea     DECIMAL(14,2)  GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    fecha_movimiento   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_movimiento),
    CONSTRAINT fk_mov_documento  FOREIGN KEY (id_documento) REFERENCES documento(id_documento),
    CONSTRAINT fk_mov_producto   FOREIGN KEY (id_producto)  REFERENCES producto(id_producto),
    CONSTRAINT fk_mov_empleado   FOREIGN KEY (id_empleado)  REFERENCES empleado(id_persona)
);

-- =============================================================================
-- 6. CARRITO DE COMPRAS
-- =============================================================================

CREATE TABLE carrito (
    id_carrito   INT       NOT NULL AUTO_INCREMENT,
    id_cliente   INT       NOT NULL,
    creado_en    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_carrito),
    CONSTRAINT fk_carrito_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona)
);

CREATE TABLE item_carrito (
    id_item      INT           NOT NULL AUTO_INCREMENT,
    id_carrito   INT           NOT NULL,
    id_producto  INT           NOT NULL,
    cantidad     INT           NOT NULL DEFAULT 1,
    PRIMARY KEY (id_item),
    CONSTRAINT fk_item_carrito  FOREIGN KEY (id_carrito)  REFERENCES carrito(id_carrito),
    CONSTRAINT fk_item_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    UNIQUE (id_carrito, id_producto)
);

-- =============================================================================
-- 7. PROCEDIMIENTOS ALMACENADOS
-- =============================================================================

DELIMITER $$

-- ---------------------------------------------------------------------------
-- SP 1: Registrar una Factura de Venta con sus líneas de movimiento
-- CORRECCIÓN: Se añade la etiqueta "sp_bloque:" al BEGIN para que el
--             LEAVE sp_bloque funcione correctamente
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_registrar_venta (
    IN  p_id_cliente      INT,
    IN  p_id_empleado     INT,
    IN  p_id_metodo_pago  INT,
    IN  p_productos_json  JSON,
    OUT p_id_documento    INT,
    OUT p_mensaje         VARCHAR(255)
)
sp_bloque: BEGIN                          -- ✅ CORRECCIÓN: etiqueta añadida
    DECLARE v_total        DECIMAL(14,2) DEFAULT 0;
    DECLARE v_idx          INT DEFAULT 0;
    DECLARE v_count        INT;
    DECLARE v_id_prod      INT;
    DECLARE v_qty          INT;
    DECLARE v_precio       DECIMAL(12,2);
    DECLARE v_stock        INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_mensaje = 'Error al registrar la venta. Transacción revertida.';
        SET p_id_documento = -1;
    END;

    START TRANSACTION;

    INSERT INTO documento (id_tipo_documento, id_persona, id_empleado, id_metodo_pago, total)
    VALUES (1, p_id_cliente, p_id_empleado, p_id_metodo_pago, 0);
    SET p_id_documento = LAST_INSERT_ID();

    SET v_count = JSON_LENGTH(p_productos_json);
    WHILE v_idx < v_count DO
        SET v_id_prod = JSON_UNQUOTE(JSON_EXTRACT(p_productos_json, CONCAT('$[', v_idx, '].id')));
        SET v_qty     = JSON_UNQUOTE(JSON_EXTRACT(p_productos_json, CONCAT('$[', v_idx, '].qty')));
        SET v_precio  = JSON_UNQUOTE(JSON_EXTRACT(p_productos_json, CONCAT('$[', v_idx, '].precio')));

        SELECT stock_actual INTO v_stock FROM producto WHERE id_producto = v_id_prod FOR UPDATE;
        IF v_stock < v_qty THEN
            SET p_mensaje = CONCAT('Stock insuficiente para producto id=', v_id_prod);
            ROLLBACK;
            SET p_id_documento = -1;
            LEAVE sp_bloque;              -- ✅ Ahora funciona porque el bloque tiene etiqueta
        END IF;

        INSERT INTO movimiento_inventario (id_documento, id_producto, id_empleado, cantidad, precio_unitario, fecha_movimiento)
        VALUES (p_id_documento, v_id_prod, p_id_empleado, v_qty, v_precio, NOW());

        UPDATE producto SET stock_actual = stock_actual - v_qty WHERE id_producto = v_id_prod;

        SET v_total = v_total + (v_qty * v_precio);
        SET v_idx = v_idx + 1;
    END WHILE;

    UPDATE documento SET total = v_total, subtotal = v_total WHERE id_documento = p_id_documento;

    COMMIT;
    SET p_mensaje = 'Venta registrada exitosamente.';
END$$

-- ---------------------------------------------------------------------------
-- SP 2: Registrar una Compra a Proveedor
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_registrar_compra (
    IN  p_id_proveedor    INT,
    IN  p_id_empleado     INT,
    IN  p_nro_factura_ext VARCHAR(60),
    IN  p_productos_json  JSON,
    OUT p_id_documento    INT,
    OUT p_mensaje         VARCHAR(255)
)
BEGIN
    DECLARE v_total   DECIMAL(14,2) DEFAULT 0;
    DECLARE v_idx     INT DEFAULT 0;
    DECLARE v_count   INT;
    DECLARE v_id_prod INT;
    DECLARE v_qty     INT;
    DECLARE v_precio  DECIMAL(12,2);
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_mensaje = 'Error al registrar la compra. Transacción revertida.';
        SET p_id_documento = -1;
    END;

    START TRANSACTION;

    INSERT INTO documento (id_tipo_documento, id_persona, id_empleado, numero_doc_externo, total)
    VALUES (2, p_id_proveedor, p_id_empleado, p_nro_factura_ext, 0);
    SET p_id_documento = LAST_INSERT_ID();

    SET v_count = JSON_LENGTH(p_productos_json);
    WHILE v_idx < v_count DO
        SET v_id_prod = JSON_UNQUOTE(JSON_EXTRACT(p_productos_json, CONCAT('$[', v_idx, '].id')));
        SET v_qty     = JSON_UNQUOTE(JSON_EXTRACT(p_productos_json, CONCAT('$[', v_idx, '].qty')));
        SET v_precio  = JSON_UNQUOTE(JSON_EXTRACT(p_productos_json, CONCAT('$[', v_idx, '].precio')));

        INSERT INTO movimiento_inventario (id_documento, id_producto, id_empleado, cantidad, precio_unitario, fecha_movimiento)
        VALUES (p_id_documento, v_id_prod, p_id_empleado, v_qty, v_precio, NOW());

        UPDATE producto SET stock_actual = stock_actual + v_qty WHERE id_producto = v_id_prod;

        SET v_total = v_total + (v_qty * v_precio);
        SET v_idx = v_idx + 1;
    END WHILE;

    UPDATE documento SET total = v_total, subtotal = v_total WHERE id_documento = p_id_documento;

    COMMIT;
    SET p_mensaje = 'Compra registrada exitosamente.';
END$$

-- ---------------------------------------------------------------------------
-- SP 3: Registrar Ajuste de Inventario
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_ajuste_inventario (
    IN  p_id_tipo_doc  INT,
    IN  p_id_empleado  INT,
    IN  p_id_producto  INT,
    IN  p_cantidad     INT,
    IN  p_observacion  TEXT,
    OUT p_id_documento INT,
    OUT p_mensaje      VARCHAR(255)
)
BEGIN
    DECLARE v_efecto   TINYINT;
    DECLARE v_stock    INT;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_mensaje = 'Error en ajuste de inventario.';
        SET p_id_documento = -1;
    END;

    START TRANSACTION;

    SELECT efecto_en_inventario INTO v_efecto FROM tipo_documento WHERE id_tipo_documento = p_id_tipo_doc;
    SELECT stock_actual INTO v_stock FROM producto WHERE id_producto = p_id_producto FOR UPDATE;

    IF v_efecto = -1 AND v_stock < p_cantidad THEN
        SET p_mensaje = 'Stock insuficiente para registrar la salida.';
        ROLLBACK;
        SET p_id_documento = -1;
    ELSE
        INSERT INTO documento (id_tipo_documento, id_persona, id_empleado, observaciones, total)
        VALUES (p_id_tipo_doc, p_id_empleado, p_id_empleado, p_observacion, 0);
        SET p_id_documento = LAST_INSERT_ID();

        INSERT INTO movimiento_inventario (id_documento, id_producto, id_empleado, cantidad, precio_unitario)
        VALUES (p_id_documento, p_id_producto, p_id_empleado, p_cantidad, 0);

        UPDATE producto
        SET stock_actual = stock_actual + (v_efecto * p_cantidad)
        WHERE id_producto = p_id_producto;

        COMMIT;
        SET p_mensaje = 'Ajuste registrado correctamente.';
    END IF;
END$$

-- ---------------------------------------------------------------------------
-- SP 4: Consultar stock actual de todos los productos con alerta
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_consultar_stock (
    IN p_solo_alertas TINYINT(1)
)
BEGIN
    IF p_solo_alertas = 1 THEN
        SELECT p.id_producto, p.nombre, c.nombre AS categoria,
               p.stock_actual, p.stock_minimo,
               (p.stock_actual - p.stock_minimo) AS diferencia
        FROM producto p
        JOIN categoria c ON p.id_categoria = c.id_categoria
        WHERE p.activo = 1 AND p.stock_actual <= p.stock_minimo
        ORDER BY diferencia ASC;
    ELSE
        SELECT p.id_producto, p.nombre, c.nombre AS categoria,
               p.stock_actual, p.stock_minimo,
               (p.stock_actual - p.stock_minimo) AS diferencia
        FROM producto p
        JOIN categoria c ON p.id_categoria = c.id_categoria
        WHERE p.activo = 1
        ORDER BY p.nombre;
    END IF;
END$$

-- ---------------------------------------------------------------------------
-- SP 5: Reporte de movimientos por producto y rango de fechas
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_reporte_movimientos (
    IN p_id_producto  INT,
    IN p_fecha_desde  DATE,
    IN p_fecha_hasta  DATE
)
BEGIN
    SELECT
        m.id_movimiento,
        m.fecha_movimiento,
        td.descripcion         AS tipo_documento,
        td.efecto_en_inventario,
        d.numero_doc_externo,
        p.nombre               AS producto,
        m.cantidad,
        m.precio_unitario,
        m.subtotal_linea,
        CONCAT(pe.nombres, ' ', pe.apellidos) AS persona_doc,
        CONCAT(emp.nombres, ' ', emp.apellidos) AS empleado_reg
    FROM movimiento_inventario m
    JOIN documento             d   ON m.id_documento       = d.id_documento
    JOIN tipo_documento        td  ON d.id_tipo_documento   = td.id_tipo_documento
    JOIN producto              p   ON m.id_producto         = p.id_producto
    JOIN persona               pe  ON d.id_persona          = pe.id_persona
    JOIN empleado              e   ON m.id_empleado         = e.id_persona
    JOIN persona               emp ON e.id_persona          = emp.id_persona
    WHERE (p_id_producto IS NULL OR m.id_producto = p_id_producto)
      AND DATE(m.fecha_movimiento) BETWEEN p_fecha_desde AND p_fecha_hasta
    ORDER BY m.fecha_movimiento DESC;
END$$

-- ---------------------------------------------------------------------------
-- SP 6: Historial de compras de un cliente
-- ---------------------------------------------------------------------------
CREATE PROCEDURE sp_historial_cliente (
    IN p_id_cliente INT
)
BEGIN
    SELECT
        d.id_documento,
        d.fecha_documento,
        td.descripcion   AS tipo,
        d.total,
        mp.nombre        AS metodo_pago,
        COUNT(m.id_movimiento) AS cant_productos
    FROM documento d
    JOIN tipo_documento  td  ON d.id_tipo_documento = td.id_tipo_documento
    LEFT JOIN metodo_pago mp ON d.id_metodo_pago    = mp.id_metodo_pago
    LEFT JOIN movimiento_inventario m ON d.id_documento = m.id_documento
    WHERE d.id_persona = p_id_cliente
    GROUP BY d.id_documento, d.fecha_documento, td.descripcion, d.total, mp.nombre
    ORDER BY d.fecha_documento DESC;
END$$

DELIMITER ;

-- =============================================================================
-- 8. DATOS DE PRUEBA
-- CORRECCIÓN: INSERT de cliente y proveedor usan subconsulta para obtener
--             el id_persona real en vez de un número fijo (1, 2, 3)
--             que podría no coincidir con el AUTO_INCREMENT
-- =============================================================================

INSERT INTO categoria (nombre) VALUES ('Computadores'), ('Celulares'), ('Accesorios'), ('Componentes');
INSERT INTO metodo_pago (nombre) VALUES ('Efectivo'), ('Tarjeta Débito'), ('Tarjeta Crédito'), ('Transferencia');

-- Persona EMPLEADO
INSERT INTO persona (tipo, nombres, apellidos, documento, email) VALUES
    ('EMPLEADO', 'Carlos', 'Martínez', '10001', 'carlos@techzone.co');

-- ✅ CORRECCIÓN: subconsulta en vez de id fijo
INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 5, '2024-01-10', SHA2('admin123', 256)
FROM persona WHERE documento = '10001';

-- Persona CLIENTE
INSERT INTO persona (tipo, nombres, apellidos, documento, email) VALUES
    ('CLIENTE', 'Laura', 'Gómez', '20001', 'laura@gmail.com');

-- ✅ CORRECCIÓN: subconsulta en vez de id fijo
INSERT INTO cliente (id_persona)
SELECT id_persona FROM persona WHERE documento = '20001';

-- Persona PROVEEDOR
INSERT INTO persona (tipo, nombres, apellidos, documento, email) VALUES
    ('PROVEEDOR', 'Samsung', 'Colombia', '30001', 'ventas@samsung.co');

-- ✅ CORRECCIÓN: subconsulta en vez de id fijo
INSERT INTO proveedor (id_persona, nombre_empresa, nit)
SELECT id_persona, 'Samsung Electronics Colombia', '900123456-1'
FROM persona WHERE documento = '30001';

-- Productos
INSERT INTO producto (id_categoria, nombre, precio_compra, precio_venta, stock_actual, stock_minimo) VALUES
    (1, 'Laptop ASUS VivoBook 15',    2200000, 2800000, 10, 3),
    (2, 'Samsung Galaxy A55',         1100000, 1400000, 15, 5),
    (3, 'Mouse Logitech MX Master 3',  180000,  250000, 30, 8);

-- =============================================================================
-- FIN DEL SCRIPT CORREGIDO
-- =============================================================================
