-- StockFlow - Consultas solicitadas y consultas representativas

USE stockflow_db;

-- 1) Inventario consolidado con clasificación calculada (RF07, RF10, RF20)
SELECT
    p.codigo,
    p.nombre AS producto,
    s.nombre AS sucursal,
    i.stock_actual,
    i.stock_minimo,
    i.stock_maximo,
    CASE
        WHEN i.stock_actual < i.stock_minimo THEN 'DEFICIT'
        WHEN i.stock_actual > i.stock_maximo THEN 'SOBRESTOCK'
        ELSE 'NORMAL'
    END AS estado_stock
FROM inventario i
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
ORDER BY p.nombre, s.nombre;

-- 2) Productos con déficit
SELECT
    p.nombre AS producto,
    s.nombre AS sucursal,
    i.stock_actual,
    i.stock_minimo,
    (i.stock_minimo - i.stock_actual) AS necesidad
FROM inventario i
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE i.stock_actual < i.stock_minimo
ORDER BY necesidad DESC;

-- 3) Productos con sobrestock
SELECT
    p.nombre AS producto,
    s.nombre AS sucursal,
    i.stock_actual,
    i.stock_maximo,
    (i.stock_actual - i.stock_maximo) AS excedente
FROM inventario i
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE i.stock_actual > i.stock_maximo
ORDER BY excedente DESC;

-- 4) Oportunidades de transferencia calculadas automáticamente (RF12/RF13)
SELECT
    p.nombre AS producto,
    so.nombre AS sucursal_origen,
    sd.nombre AS sucursal_destino,
    io.stock_actual AS stock_origen,
    io.stock_maximo,
    (io.stock_actual - io.stock_maximo) AS excedente_origen,
    id.stock_actual AS stock_destino,
    id.stock_minimo,
    (id.stock_minimo - id.stock_actual) AS necesidad_destino,
    LEAST(
        io.stock_actual - io.stock_maximo,
        id.stock_minimo - id.stock_actual
    ) AS cantidad_recomendada
FROM inventario io
JOIN inventario id
    ON id.id_producto = io.id_producto
   AND id.id_sucursal <> io.id_sucursal
JOIN producto p ON p.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
WHERE io.stock_actual > io.stock_maximo
  AND id.stock_actual < id.stock_minimo
ORDER BY p.nombre, so.nombre, sd.nombre;

-- 5) Alertas activas (RF11/RF22)
SELECT
    a.id_alerta,
    p.nombre AS producto,
    s.nombre AS sucursal,
    a.tipo,
    a.estado,
    i.stock_actual,
    i.stock_minimo,
    i.stock_maximo,
    a.fecha_generacion
FROM alerta a
JOIN inventario i ON i.id_inventario = a.id_inventario
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE a.estado = 'ACTIVA'
ORDER BY a.fecha_generacion DESC;

-- 6) Recomendaciones pendientes (RF14)
SELECT
    r.id_recomendacion,
    p.nombre AS producto,
    so.nombre AS origen,
    sd.nombre AS destino,
    r.cantidad_recomendada,
    r.estado,
    r.fecha_generacion
FROM recomendacion r
JOIN inventario io ON io.id_inventario = r.id_inventario_origen
JOIN inventario id ON id.id_inventario = r.id_inventario_destino
JOIN producto p ON p.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
WHERE r.estado = 'PENDIENTE'
ORDER BY r.fecha_generacion DESC;

-- 7) Historial de transferencias (RF18)
SELECT
    t.id_transferencia,
    p.nombre AS producto,
    so.nombre AS origen,
    sd.nombre AS destino,
    t.cantidad,
    t.estado,
    t.fecha_registro,
    t.fecha_finalizacion,
    u.nombre AS responsable
FROM transferencia t
JOIN recomendacion r ON r.id_recomendacion = t.id_recomendacion
JOIN inventario io ON io.id_inventario = r.id_inventario_origen
JOIN inventario id ON id.id_inventario = r.id_inventario_destino
JOIN producto p ON p.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
JOIN usuario u ON u.id_usuario = t.id_usuario_responsable
ORDER BY t.fecha_registro DESC;

-- 8) Trazabilidad de cambios de estado de transferencias
SELECT
    ht.id_historial,
    ht.id_transferencia,
    ht.estado,
    ht.fecha_hora,
    u.nombre AS usuario,
    ht.observacion
FROM historial_transferencia ht
JOIN usuario u ON u.id_usuario = ht.id_usuario
ORDER BY ht.id_transferencia, ht.fecha_hora;

-- 9) Movimientos de inventario
SELECT
    m.id_movimiento,
    p.nombre AS producto,
    s.nombre AS sucursal,
    m.tipo,
    m.cantidad,
    m.fecha_hora,
    u.nombre AS usuario,
    m.id_transferencia,
    m.observacion
FROM movimiento m
JOIN inventario i ON i.id_inventario = m.id_inventario
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
JOIN usuario u ON u.id_usuario = m.id_usuario
ORDER BY m.fecha_hora DESC;

-- 10) Usuarios, roles y ámbito de operación
SELECT
    u.usuario,
    u.nombre,
    r.nombre AS rol,
    s.nombre AS sucursal
FROM usuario u
JOIN rol r ON r.id_rol = u.id_rol
LEFT JOIN usuario_sucursal us ON us.id_usuario = u.id_usuario
LEFT JOIN sucursal s ON s.id_sucursal = us.id_sucursal
ORDER BY u.usuario, s.nombre;
