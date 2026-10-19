-- StockFlow - Datos de prueba
-- Escenario consistente con el prototipo JavaFX y los casos CP01/CP03.

USE stockflow_db;

INSERT INTO rol (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Gestiona usuarios, roles, sucursales, productos y configuraciones.'),
('ENCARGADO_SUCURSAL', 'Opera y consulta información de una o más sucursales asignadas.'),
('RESPONSABLE_INVENTARIO_COMPRAS', 'Consulta información consolidada y decide sobre recomendaciones.');

INSERT INTO sucursal (nombre, direccion) VALUES
('Centro', 'Av. Central 100'),
('Norte', 'Av. Norte 250'),
('Sur', 'Av. Sur 300'),
('Oeste', 'Av. Oeste 450'),
('Deposito Central', 'Ruta Provincial 5 Km 10');

INSERT INTO usuario (id_rol, nombre, usuario, clave_hash, email) VALUES
((SELECT id_rol FROM rol WHERE nombre = 'ADMINISTRADOR'),
 'Administrador StockFlow', 'admin', '210000$h54Z9EgQjIqYf07UOOJw4Q==$EdYhK/odTnCz0b6y/i+ekYUmy4ArQHfsnWXO7qJxnks=', 'admin@delta.local'),
((SELECT id_rol FROM rol WHERE nombre = 'ENCARGADO_SUCURSAL'),
 'Encargado Centro', 'encargado.centro', '210000$5540WKGL6NX0tGbTkidolw==$4RdICyIR3S+l/nx3wk/t20dNexOeI/Ptx0tmjW7ciAY=', 'centro@delta.local'),
((SELECT id_rol FROM rol WHERE nombre = 'RESPONSABLE_INVENTARIO_COMPRAS'),
 'Responsable de Inventario y Compras', 'compras', '210000$cutMYvIahXxee+JbyLJapg==$AKWDc1RuPBrj6tr3rxeUkMitHjd5co6uv1Q6+yvZnSc=', 'compras@delta.local');

INSERT INTO usuario_sucursal (id_usuario, id_sucursal)
SELECT u.id_usuario, s.id_sucursal
FROM usuario u
JOIN sucursal s ON s.nombre IN ('Centro', 'Norte', 'Sur', 'Oeste')
WHERE u.usuario = 'compras';

INSERT INTO usuario_sucursal (id_usuario, id_sucursal)
SELECT u.id_usuario, s.id_sucursal
FROM usuario u
JOIN sucursal s ON s.nombre = 'Centro'
WHERE u.usuario = 'encargado.centro';

INSERT INTO categoria (nombre, descripcion) VALUES
('Herramientas electricas', 'Herramientas accionadas electricamente.'),
('Herramientas manuales', 'Herramientas de uso manual.'),
('Medicion', 'Instrumentos de medicion.');

INSERT INTO producto (id_categoria, codigo, nombre, descripcion) VALUES
((SELECT id_categoria FROM categoria WHERE nombre = 'Herramientas electricas'),
 'TAL-001', 'Taladro Percutor', 'Taladro percutor de uso general.'),
((SELECT id_categoria FROM categoria WHERE nombre = 'Herramientas manuales'),
 'MAR-001', 'Martillo Galponero', 'Martillo para trabajos generales.'),
((SELECT id_categoria FROM categoria WHERE nombre = 'Herramientas electricas'),
 'SIE-001', 'Sierra Circular', 'Sierra circular electrica.'),
((SELECT id_categoria FROM categoria WHERE nombre = 'Medicion'),
 'CIN-001', 'Cinta Metrica 5m', 'Cinta metrica de cinco metros.');

-- Inventario de Taladro Percutor
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 8, 15, 30
FROM producto p JOIN sucursal s ON s.nombre = 'Centro'
WHERE p.codigo = 'TAL-001';

INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 48, 15, 30
FROM producto p JOIN sucursal s ON s.nombre = 'Norte'
WHERE p.codigo = 'TAL-001';

INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 18, 10, 30
FROM producto p JOIN sucursal s ON s.nombre = 'Sur'
WHERE p.codigo = 'TAL-001';

-- Inventario de Martillo Galponero
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 40, 10, 20
FROM producto p JOIN sucursal s ON s.nombre = 'Centro'
WHERE p.codigo = 'MAR-001';

INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 5, 10, 20
FROM producto p JOIN sucursal s ON s.nombre = 'Sur'
WHERE p.codigo = 'MAR-001';

-- Inventario de Sierra Circular
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 22, 5, 15
FROM producto p JOIN sucursal s ON s.nombre = 'Oeste'
WHERE p.codigo = 'SIE-001';

INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 2, 5, 15
FROM producto p JOIN sucursal s ON s.nombre = 'Norte'
WHERE p.codigo = 'SIE-001';

-- Inventario de Cinta Metrica 5m
INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 110, 30, 80
FROM producto p JOIN sucursal s ON s.nombre = 'Sur'
WHERE p.codigo = 'CIN-001';

INSERT INTO inventario (id_producto, id_sucursal, stock_actual, stock_minimo, stock_maximo)
SELECT p.id_producto, s.id_sucursal, 12, 30, 80
FROM producto p JOIN sucursal s ON s.nombre = 'Centro'
WHERE p.codigo = 'CIN-001';

-- Alertas activas derivadas del escenario de inventario
INSERT INTO alerta (id_inventario, tipo, estado)
SELECT i.id_inventario, 'DEFICIT', 'ACTIVA'
FROM inventario i
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE (p.codigo = 'TAL-001' AND s.nombre = 'Centro')
   OR (p.codigo = 'MAR-001' AND s.nombre = 'Sur')
   OR (p.codigo = 'SIE-001' AND s.nombre = 'Norte')
   OR (p.codigo = 'CIN-001' AND s.nombre = 'Centro');

INSERT INTO alerta (id_inventario, tipo, estado)
SELECT i.id_inventario, 'SOBRESTOCK', 'ACTIVA'
FROM inventario i
JOIN producto p ON p.id_producto = i.id_producto
JOIN sucursal s ON s.id_sucursal = i.id_sucursal
WHERE (p.codigo = 'TAL-001' AND s.nombre = 'Norte')
   OR (p.codigo = 'MAR-001' AND s.nombre = 'Centro')
   OR (p.codigo = 'SIE-001' AND s.nombre = 'Oeste')
   OR (p.codigo = 'CIN-001' AND s.nombre = 'Sur');

-- Recomendaciones del prototipo
INSERT INTO recomendacion (
    id_inventario_origen, id_inventario_destino,
    cantidad_recomendada, estado
)
SELECT io.id_inventario, id.id_inventario, 7, 'PENDIENTE'
FROM inventario io
JOIN producto po ON po.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN inventario id ON id.id_producto = io.id_producto
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
WHERE po.codigo = 'TAL-001'
  AND so.nombre = 'Norte'
  AND sd.nombre = 'Centro';

INSERT INTO recomendacion (
    id_inventario_origen, id_inventario_destino,
    cantidad_recomendada, estado
)
SELECT io.id_inventario, id.id_inventario, 5, 'PENDIENTE'
FROM inventario io
JOIN producto po ON po.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN inventario id ON id.id_producto = io.id_producto
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
WHERE po.codigo = 'MAR-001'
  AND so.nombre = 'Centro'
  AND sd.nombre = 'Sur';

INSERT INTO recomendacion (
    id_inventario_origen, id_inventario_destino,
    cantidad_recomendada, estado
)
SELECT io.id_inventario, id.id_inventario, 3, 'PENDIENTE'
FROM inventario io
JOIN producto po ON po.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN inventario id ON id.id_producto = io.id_producto
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
WHERE po.codigo = 'SIE-001'
  AND so.nombre = 'Oeste'
  AND sd.nombre = 'Norte';

INSERT INTO recomendacion (
    id_inventario_origen, id_inventario_destino,
    cantidad_recomendada, estado
)
SELECT io.id_inventario, id.id_inventario, 18, 'PENDIENTE'
FROM inventario io
JOIN producto po ON po.id_producto = io.id_producto
JOIN sucursal so ON so.id_sucursal = io.id_sucursal
JOIN inventario id ON id.id_producto = io.id_producto
JOIN sucursal sd ON sd.id_sucursal = id.id_sucursal
WHERE po.codigo = 'CIN-001'
  AND so.nombre = 'Sur'
  AND sd.nombre = 'Centro';
