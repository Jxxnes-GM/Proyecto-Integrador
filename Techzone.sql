CREATE DATABASE techzone CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE techzone;

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

CREATE TABLE documento (
    id_documento       INT            NOT NULL AUTO_INCREMENT,
    id_tipo_documento  INT            NOT NULL,
    id_persona         INT            NOT NULL,
    id_empleado        INT,
    id_metodo_pago     INT,
    fecha_documento    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    numero_doc_externo VARCHAR(60),
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

CREATE TABLE movimiento_inventario (
    id_movimiento      INT            NOT NULL AUTO_INCREMENT,
    id_documento       INT            NOT NULL,
    id_producto        INT            NOT NULL,
    id_empleado        INT            NOT NULL,
    cantidad           INT            NOT NULL,
    precio_unitario    DECIMAL(12,2)  NOT NULL,
    subtotal_linea     DECIMAL(14,2)  GENERATED ALWAYS AS (cantidad * precio_unitario) STORED,
    fecha_movimiento   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_movimiento),
    CONSTRAINT fk_mov_documento  FOREIGN KEY (id_documento) REFERENCES documento(id_documento),
    CONSTRAINT fk_mov_producto   FOREIGN KEY (id_producto)  REFERENCES producto(id_producto),
    CONSTRAINT fk_mov_empleado   FOREIGN KEY (id_empleado)  REFERENCES empleado(id_persona)
);

CREATE TABLE carrito (
    id_carrito   INT       NOT NULL AUTO_INCREMENT,
    id_cliente   INT       NOT NULL,
    creado_en    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id_carrito),
    CONSTRAINT fk_carrito_cliente FOREIGN KEY (id_cliente) REFERENCES cliente(id_persona)
);

CREATE TABLE item_carrito (
    id_item      INT  NOT NULL AUTO_INCREMENT,
    id_carrito   INT  NOT NULL,
    id_producto  INT  NOT NULL,
    cantidad     INT  NOT NULL DEFAULT 1,
    PRIMARY KEY (id_item),
    CONSTRAINT fk_item_carrito  FOREIGN KEY (id_carrito)  REFERENCES carrito(id_carrito),
    CONSTRAINT fk_item_producto FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    UNIQUE (id_carrito, id_producto)
);

-- =============================================================================
-- PROCEDIMIENTOS ALMACENADOS
-- =============================================================================

DELIMITER $$

-- SP 1: Registrar Venta
-- Crea el documento de venta, verifica stock de cada producto recibido en JSON
-- y registra los movimientos de inventario descontando el stock. Usa transacción
-- atómica: si falta stock en algún producto, se hace ROLLBACK completo.
-- IN:  p_id_cliente, p_id_empleado, p_id_metodo_pago, p_productos_json
--      JSON esperado: [{"id":1,"qty":2,"precio":2800000}, ...]
-- OUT: p_id_documento (-1 si falla), p_mensaje

CREATE PROCEDURE sp_registrar_venta (
    IN  p_id_cliente      INT,
    IN  p_id_empleado     INT,
    IN  p_id_metodo_pago  INT,
    IN  p_productos_json  JSON,
    OUT p_id_documento    INT,
    OUT p_mensaje         VARCHAR(255)
)
sp_bloque: BEGIN
    DECLARE v_total   DECIMAL(14,2) DEFAULT 0;
    DECLARE v_idx     INT DEFAULT 0;
    DECLARE v_count   INT;
    DECLARE v_id_prod INT;
    DECLARE v_qty     INT;
    DECLARE v_precio  DECIMAL(12,2);
    DECLARE v_stock   INT;
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
            LEAVE sp_bloque;
        END IF;

        INSERT INTO movimiento_inventario (id_documento, id_producto, id_empleado, cantidad, precio_unitario, fecha_movimiento)
        VALUES (p_id_documento, v_id_prod, p_id_empleado, v_qty, v_precio, NOW());

        UPDATE producto SET stock_actual = stock_actual - v_qty WHERE id_producto = v_id_prod;

        SET v_total = v_total + (v_qty * v_precio);
        SET v_idx   = v_idx + 1;
    END WHILE;

    UPDATE documento SET total = v_total, subtotal = v_total WHERE id_documento = p_id_documento;

    COMMIT;
    SET p_mensaje = 'Venta registrada exitosamente.';
END$$


-- SP 2: Registrar Compra a Proveedor
-- Crea el documento de compra con el número de factura externo del proveedor,
-- registra los movimientos de inventario y aumenta el stock de cada producto.
-- IN:  p_id_proveedor, p_id_empleado, p_nro_factura_ext, p_productos_json
--      JSON esperado: [{"id":1,"qty":10,"precio":2200000}, ...]
-- OUT: p_id_documento (-1 si falla), p_mensaje

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
        SET v_idx   = v_idx + 1;
    END WHILE;

    UPDATE documento SET total = v_total, subtotal = v_total WHERE id_documento = p_id_documento;

    COMMIT;
    SET p_mensaje = 'Compra registrada exitosamente.';
END$$


-- SP 3: Ajuste de Inventario
-- Registra una entrada o salida manual de inventario. El efecto (+1 o -1)
-- lo determina el tipo de documento. Si es salida y no hay stock suficiente,
-- se aborta la operación sin modificar nada.
-- IN:  p_id_tipo_doc (5=Entrada,6=Salida,7=Baja), p_id_empleado,
--      p_id_producto, p_cantidad, p_observacion
-- OUT: p_id_documento (-1 si falla), p_mensaje

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
    DECLARE v_efecto TINYINT;
    DECLARE v_stock  INT;
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

        UPDATE producto SET stock_actual = stock_actual + (v_efecto * p_cantidad) WHERE id_producto = p_id_producto;

        COMMIT;
        SET p_mensaje = 'Ajuste registrado correctamente.';
    END IF;
END$$


-- SP 4: Consultar Stock
-- Retorna el estado de inventario de todos los productos activos.
-- Con p_solo_alertas = 1 filtra solo los que tienen stock <= stock_minimo,
-- ordenados por diferencia ASC (los más críticos primero).
-- IN:  p_solo_alertas (1 = solo alertas, 0 = todos)
-- Columnas retornadas: id_producto, nombre, categoria, stock_actual, stock_minimo, diferencia

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


-- SP 5: Reporte de Movimientos
-- Retorna el detalle de movimientos de inventario con información completa
-- de documento, producto, persona y empleado. Filtra por producto (opcional)
-- y por rango de fechas (obligatorio).
-- IN:  p_id_producto (NULL = todos), p_fecha_desde, p_fecha_hasta (DATE 'YYYY-MM-DD')
-- Columnas: id_movimiento, fecha_movimiento, tipo_documento, efecto_en_inventario,
--           numero_doc_externo, producto, cantidad, precio_unitario,
--           subtotal_linea, persona_doc, empleado_reg

CREATE PROCEDURE sp_reporte_movimientos (
    IN p_id_producto INT,
    IN p_fecha_desde DATE,
    IN p_fecha_hasta DATE
)
BEGIN
    SELECT
        m.id_movimiento,
        m.fecha_movimiento,
        td.descripcion                          AS tipo_documento,
        td.efecto_en_inventario,
        d.numero_doc_externo,
        p.nombre                                AS producto,
        m.cantidad,
        m.precio_unitario,
        m.subtotal_linea,
        CONCAT(pe.nombres,  ' ', pe.apellidos)  AS persona_doc,
        CONCAT(emp.nombres, ' ', emp.apellidos) AS empleado_reg
    FROM movimiento_inventario m
    JOIN documento             d   ON m.id_documento      = d.id_documento
    JOIN tipo_documento        td  ON d.id_tipo_documento  = td.id_tipo_documento
    JOIN producto              p   ON m.id_producto        = p.id_producto
    JOIN persona               pe  ON d.id_persona         = pe.id_persona
    JOIN empleado              e   ON m.id_empleado        = e.id_persona
    JOIN persona               emp ON e.id_persona         = emp.id_persona
    WHERE (p_id_producto IS NULL OR m.id_producto = p_id_producto)
      AND DATE(m.fecha_movimiento) BETWEEN p_fecha_desde AND p_fecha_hasta
    ORDER BY m.fecha_movimiento DESC;
END$$


-- SP 6: Historial de un Cliente
-- Retorna todos los documentos asociados a un cliente, agrupados por documento,
-- con el total, método de pago y cantidad de productos involucrados.
-- IN:  p_id_cliente
-- Columnas: id_documento, fecha_documento, tipo, total, metodo_pago, cant_productos

CREATE PROCEDURE sp_historial_cliente (
    IN p_id_cliente INT
)
BEGIN
    SELECT
        d.id_documento,
        d.fecha_documento,
        td.descripcion              AS tipo,
        d.total,
        mp.nombre                   AS metodo_pago,
        COUNT(m.id_movimiento)      AS cant_productos
    FROM documento d
    JOIN  tipo_documento  td  ON d.id_tipo_documento = td.id_tipo_documento
    LEFT JOIN metodo_pago mp  ON d.id_metodo_pago    = mp.id_metodo_pago
    LEFT JOIN movimiento_inventario m ON d.id_documento = m.id_documento
    WHERE d.id_persona = p_id_cliente
    GROUP BY d.id_documento, d.fecha_documento, td.descripcion, d.total, mp.nombre
    ORDER BY d.fecha_documento DESC;
END$$

DELIMITER ;

-- =============================================================================
-- DATOS DE PRUEBA
-- =============================================================================

INSERT INTO categoria (nombre) VALUES ('Computadores'), ('Celulares'), ('Accesorios'), ('Componentes');
INSERT INTO metodo_pago (nombre) VALUES ('Efectivo'), ('Tarjeta Débito'), ('Tarjeta Crédito'), ('Transferencia');

INSERT INTO persona (tipo, nombres, apellidos, documento, email) VALUES
    ('EMPLEADO', 'Carlos', 'Martínez', '10001', 'carlos@techzone.co');
INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 5, '2024-01-10', SHA2('admin123', 256) FROM persona WHERE documento = '10001';

INSERT INTO persona (tipo, nombres, apellidos, documento, email) VALUES
    ('CLIENTE', 'Laura', 'Gómez', '20001', 'laura@gmail.com');
INSERT INTO cliente (id_persona)
SELECT id_persona FROM persona WHERE documento = '20001';

INSERT INTO persona (tipo, nombres, apellidos, documento, email) VALUES
    ('PROVEEDOR', 'Samsung', 'Colombia', '30001', 'ventas@samsung.co');
INSERT INTO proveedor (id_persona, nombre_empresa, nit)
SELECT id_persona, 'Samsung Electronics Colombia', '900123456-1' FROM persona WHERE documento = '30001';

INSERT INTO producto (id_categoria, nombre, precio_compra, precio_venta, stock_actual, stock_minimo) VALUES
    (1, 'Laptop ASUS VivoBook 15',    2200000, 2800000, 10, 3),
    (2, 'Samsung Galaxy A55',         1100000, 1400000, 15, 5),
    (3, 'Mouse Logitech MX Master 3',  180000,  250000, 30, 8);

UPDATE cliente SET contrasena_hash = SHA2('laura123', 256)
WHERE id_persona = (SELECT id_persona FROM persona WHERE email = 'laura@gmail.com');

INSERT IGNORE INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('CLIENTE', 'Admin', 'TechZone', '99999', 'admin@techzone.co', 1);
INSERT IGNORE INTO cliente (id_persona, contrasena_hash)
SELECT id_persona, SHA2('techzone', 256) FROM persona WHERE documento = '99999';

CREATE OR REPLACE VIEW vista_persona_cliente AS
SELECT
    p.id_persona,
    p.nombres     AS nombre,
    p.apellidos   AS apellido,
    p.documento,
    p.telefono,
    p.email,
    p.direccion,
    p.activo,
    c.contrasena_hash
FROM persona p
JOIN cliente c ON p.id_persona = c.id_persona
WHERE p.tipo = 'CLIENTE' AND p.activo = 1;


-- ============================================================
-- TECHZONE — Usuarios con roles asignados
-- ============================================================
USE techzone;

-- Limpia datos de prueba anteriores para evitar duplicados
DELETE FROM empleado WHERE id_persona IN (
    SELECT id_persona FROM persona WHERE documento IN ('10001','10002','10003','10004','10005')
);
DELETE FROM cliente WHERE id_persona IN (
    SELECT id_persona FROM persona WHERE documento IN ('20001','99999')
);
DELETE FROM persona WHERE documento IN ('10001','10002','10003','10004','10005','20001','99999');

-- ── ADMINISTRADOR (cargo 1) ──────────────────────────────────
INSERT INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('EMPLEADO', 'Admin', 'TechZone', '10001', 'admin@techzone.co', 1);

INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 1, CURDATE(), SHA2('admin123', 256)
FROM persona WHERE documento = '10001';

-- ── COMPRADOR (cargo 2) ──────────────────────────────────────
INSERT INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('EMPLEADO', 'Oscar', 'Soto', '10002', 'osoto@techzone.co', 1);

INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 2, CURDATE(), SHA2('comprador123', 256)
FROM persona WHERE documento = '10002';

-- ── VENDEDOR (cargo 3) ───────────────────────────────────────
INSERT INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('EMPLEADO', 'Leidy', 'Bustamante', '10003', 'lbustamante@techzone.co', 1);

INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 3, CURDATE(), SHA2('vendedor123', 256)
FROM persona WHERE documento = '10003';

-- ── CAJERO (cargo 4) ─────────────────────────────────────────
INSERT INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('EMPLEADO', 'Carlos', 'Martínez', '10004', 'cmartinez@techzone.co', 1);

INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 4, CURDATE(), SHA2('cajero123', 256)
FROM persona WHERE documento = '10004';

-- ── BODEGUERO (cargo 5) ──────────────────────────────────────
INSERT INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('EMPLEADO', 'Juan', 'Gaviria', '10005', 'jgaviria@techzone.co', 1);

INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 5, CURDATE(), SHA2('bodeguero123', 256)
FROM persona WHERE documento = '10005';

-- ── CLIENTE de prueba ────────────────────────────────────────
INSERT INTO persona (tipo, nombres, apellidos, documento, email, activo)
VALUES ('CLIENTE', 'Laura', 'Gómez', '20001', 'laura@gmail.com', 1);

INSERT INTO cliente (id_persona, contrasena_hash)
SELECT id_persona, SHA2('laura123', 256)
FROM persona WHERE documento = '20001';

-- ── VERIFICAR QUE QUEDÓ BIEN ─────────────────────────────────
SELECT
    p.documento,
    CONCAT(p.nombres, ' ', p.apellidos) AS nombre_completo,
    p.tipo,
    p.email,
    c.nombre AS cargo
FROM persona p
LEFT JOIN empleado e ON p.id_persona = e.id_persona
LEFT JOIN cargo c    ON e.id_cargo = c.id_cargo
ORDER BY p.tipo, c.id_cargo;

/*
  ┌───────────────────────────────────────────────────────────────┐
  │  CREDENCIALES                                                 │
  ├──────────────────────┬────────────────────────┬──────────────┤
  │  ROL / CARGO         │  EMAIL                  │  CONTRASEÑA  │
  ├──────────────────────┼────────────────────────┼──────────────┤
  │  Administrador       │  admin@techzone.co      │  admin123    │
  │  Comprador           │  osoto@techzone.co      │  comprador123│
  │  Vendedor            │  lbustamante@techzone.co│  vendedor123 │
  │  Cajero              │  cmartinez@techzone.co  │  cajero123   │
  │  Bodeguero           │  jgaviria@techzone.co   │  bodeguero123│
  │  Cliente             │  laura@gmail.com        │  laura123    │
  └──────────────────────┴────────────────────────┴──────────────┘
*/


-- =============================================================================
-- TECHZONE — Corrección de usuarios mal asignados
-- Ejecutar en MySQL Workbench sobre la base de datos techzone
-- =============================================================================
 
USE techzone;
 
-- -----------------------------------------------------------------------------
-- PASO 1: Eliminar al admin de la tabla cliente (estaba mal insertado)
-- -----------------------------------------------------------------------------
DELETE FROM cliente
WHERE id_persona = (
    SELECT id_persona FROM persona WHERE email = 'admin@techzone.co'
);
 
-- -----------------------------------------------------------------------------
-- PASO 2: Cambiar el tipo de CLIENTE a EMPLEADO en la tabla persona
-- -----------------------------------------------------------------------------
UPDATE persona
SET tipo = 'EMPLEADO'
WHERE email = 'admin@techzone.co';
 
-- -----------------------------------------------------------------------------
-- PASO 3: Insertar en la tabla empleado con cargo 1 (Administrador)
-- La contraseña 'techzone' queda hasheada igual que los demás empleados
-- -----------------------------------------------------------------------------
INSERT INTO empleado (id_persona, id_cargo, fecha_ingreso, contrasena_hash)
SELECT id_persona, 1, CURDATE(), SHA2('techzone', 256)
FROM persona
WHERE email = 'admin@techzone.co';
 
-- -----------------------------------------------------------------------------
-- PASO 4: Verificar que quedó bien
-- Deberías ver al admin con cargo = Administrador y tipo = EMPLEADO
-- -----------------------------------------------------------------------------
SELECT
    p.documento,
    CONCAT(p.nombres, ' ', p.apellidos) AS nombre_completo,
    p.tipo,
    p.email,
    c.nombre AS cargo
FROM persona p
LEFT JOIN empleado e ON p.id_persona = e.id_persona
LEFT JOIN cargo    c ON e.id_cargo   = c.id_cargo
WHERE p.email = 'admin@techzone.co';
 
-- -----------------------------------------------------------------------------
-- PASO 5: Verificar todos los usuarios del sistema para confirmar integridad
-- -----------------------------------------------------------------------------
SELECT
    p.documento,
    CONCAT(p.nombres, ' ', p.apellidos) AS nombre_completo,
    p.tipo,
    p.email,
    c.nombre AS cargo
FROM persona p
LEFT JOIN empleado e ON p.id_persona = e.id_persona
LEFT JOIN cargo    c ON e.id_cargo   = c.id_cargo
ORDER BY p.tipo, c.id_cargo;







 