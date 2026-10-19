-- StockFlow - CP03: finalización correcta con COMMIT
-- Resultado esperado:
-- Norte: 48 -> 41
-- Centro: 8 -> 15
-- Dos movimientos registrados
-- Transferencia: COMPLETADA

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

SET @cantidad := (
    SELECT cantidad
    FROM transferencia
    WHERE id_transferencia = @transferencia_cp03
);

START TRANSACTION;

UPDATE inventario
SET stock_actual = stock_actual - @cantidad
WHERE id_inventario = @inventario_origen;

UPDATE inventario
SET stock_actual = stock_actual + @cantidad
WHERE id_inventario = @inventario_destino;

INSERT INTO movimiento (
    id_inventario, id_usuario, id_transferencia,
    tipo, cantidad, observacion
)
VALUES (
    @inventario_origen, @usuario_compras, @transferencia_cp03,
    'SALIDA', @cantidad, 'Salida por transferencia interna'
);

INSERT INTO movimiento (
    id_inventario, id_usuario, id_transferencia,
    tipo, cantidad, observacion
)
VALUES (
    @inventario_destino, @usuario_compras, @transferencia_cp03,
    'ENTRADA', @cantidad, 'Entrada por transferencia interna'
);

UPDATE transferencia
SET estado = 'COMPLETADA',
    fecha_finalizacion = NOW()
WHERE id_transferencia = @transferencia_cp03;

INSERT INTO historial_transferencia (
    id_transferencia, id_usuario, estado, observacion
)
VALUES (
    @transferencia_cp03, @usuario_compras, 'COMPLETADA',
    'Transferencia finalizada. Inventarios actualizados atomicamente.'
);

COMMIT;

SELECT
    p.nombre AS producto,
    s.nombre AS sucursal,
    i.stock_actual
FROM inventario i
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE i.id_inventario IN (@inventario_origen, @inventario_destino)
ORDER BY s.nombre;

SELECT id_transferencia, estado, cantidad, fecha_finalizacion
FROM transferencia
WHERE id_transferencia = @transferencia_cp03;

SELECT tipo, cantidad, id_inventario, id_transferencia
FROM movimiento
WHERE id_transferencia = @transferencia_cp03
ORDER BY id_movimiento;
