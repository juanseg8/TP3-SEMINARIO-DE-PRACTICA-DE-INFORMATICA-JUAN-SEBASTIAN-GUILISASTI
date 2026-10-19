-- StockFlow - Preparación del escenario CP03
-- Convierte la recomendación del Taladro Norte -> Centro en APROBADA,
-- registra una transferencia PENDIENTE y luego la pasa a EN_TRANSITO.
-- Ejecutar una sola vez sobre el escenario inicial.

USE stockflow_db;

SET @usuario_compras := (
    SELECT id_usuario
    FROM usuario
    WHERE usuario = 'compras'
);

SET @rec_taladro := (
    SELECT r.id_recomendacion
    FROM recomendacion r
    JOIN inventario io ON io.id_inventario = r.id_inventario_origen
    JOIN inventario id ON id.id_inventario = r.id_inventario_destino
    JOIN producto p ON p.id_producto = io.id_producto
    JOIN sucursal so ON so.id_sucursal = io.id_sucursal
    JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
    WHERE p.codigo = 'TAL-001'
      AND so.nombre = 'Norte'
      AND sd.nombre = 'Centro'
      AND r.estado = 'PENDIENTE'
    LIMIT 1
);

UPDATE recomendacion
SET estado = 'APROBADA',
    cantidad_aprobada = 7,
    id_usuario_decision = @usuario_compras,
    fecha_decision = NOW(),
    motivo_decision = 'Aprobada para escenario CP03'
WHERE id_recomendacion = @rec_taladro;

INSERT INTO transferencia (
    id_recomendacion,
    id_usuario_responsable,
    cantidad,
    estado,
    observacion
)
VALUES (
    @rec_taladro,
    @usuario_compras,
    7,
    'PENDIENTE',
    'Transferencia preparada para CP03'
);

SET @transferencia_cp03 := LAST_INSERT_ID();

INSERT INTO historial_transferencia (
    id_transferencia, id_usuario, estado, observacion
)
VALUES (
    @transferencia_cp03,
    @usuario_compras,
    'PENDIENTE',
    'Transferencia registrada.'
);

UPDATE transferencia
SET estado = 'EN_TRANSITO'
WHERE id_transferencia = @transferencia_cp03;

INSERT INTO historial_transferencia (
    id_transferencia, id_usuario, estado, observacion
)
VALUES (
    @transferencia_cp03,
    @usuario_compras,
    'EN_TRANSITO',
    'Traslado iniciado.'
);

SELECT
    t.id_transferencia,
    t.estado,
    t.cantidad,
    r.estado AS estado_recomendacion,
    r.cantidad_aprobada
FROM transferencia t
JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
WHERE t.id_transferencia = @transferencia_cp03;
