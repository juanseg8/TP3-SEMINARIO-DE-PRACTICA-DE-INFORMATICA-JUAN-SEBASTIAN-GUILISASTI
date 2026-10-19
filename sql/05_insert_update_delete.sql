-- StockFlow - Ejemplos de INSERT, UPDATE y DELETE
-- Diseñados para obtener capturas sin afectar el escenario principal.

USE stockflow_db;

-- INSERT de un registro temporal
INSERT INTO categoria (nombre, descripcion)
VALUES ('Temporal TP2', 'Categoria creada para demostrar una insercion.');

SELECT * FROM categoria
WHERE nombre = 'Temporal TP2';

-- UPDATE del registro temporal
UPDATE categoria
SET descripcion = 'Categoria temporal actualizada mediante UPDATE.'
WHERE nombre = 'Temporal TP2';

SELECT * FROM categoria
WHERE nombre = 'Temporal TP2';

-- DELETE del registro temporal
DELETE FROM categoria
WHERE nombre = 'Temporal TP2';

SELECT * FROM categoria
WHERE nombre = 'Temporal TP2';
-- Resultado esperado: 0 filas.
