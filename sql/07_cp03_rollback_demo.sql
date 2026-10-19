-- StockFlow - CP03: demostración de ROLLBACK
-- La transacción descuenta el origen, simula una falla lógica y hace ROLLBACK.
-- Al finalizar, los inventarios deben conservar 48 (Norte) y 8 (Centro).

USE stockflow_db;

SET @usuario_compras := (
    SELECT id_usuario FROM usuario WHERE usuario = 'compras'
);

SET @transferencia_cp03 := (
    SELECT t.id_transferencia
    FROM transferencia t
    JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
    JOIN inventario io ON io.id_inventario = r.id_inventario_origen
    JOIN inventario id ON id.id_inventario = r.id_inventario_destino
    JOIN producto p ON p.id_producto = io.id_producto
    JOIN sucursal so ON so.id_sucursal = io.id_sucursal
    JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
    WHERE p.codigo = 'TAL-001'
      AND so.nombre = 'Norte'
      AND sd.nombre = 'Centro'
      AND t.estado = 'EN_TRANSITO'
    LIMIT 1
);

SET @inventario_origen := (
    SELECT r.id_inventario_origen
    FROM transferencia t
    JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
    WHERE t.id_transferencia = @transferencia_cp03
);

SET @inventario_destino := (
    SELECT r.id_inventario_destino
    FROM transferencia t
    JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
    WHERE t.id_transferencia = @transferencia_cp03
);

START TRANSACTION;

UPDATE inventario
SET stock_actual = stock_actual - 7
WHERE id_inventario = @inventario_origen;

-- Punto donde CP03 simula que ocurre una falla antes de actualizar destino.
-- Para demostrar atomicidad, se revierte toda la transacción:
ROLLBACK;

SELECT
    s.nombre AS sucursal,
    i.stock_actual
FROM inventario i
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE i.id_inventario IN (@inventario_origen, @inventario_destino)
ORDER BY s.nombre;

SELECT id_transferencia, estado
FROM transferencia
WHERE id_transferencia = @transferencia_cp03;
