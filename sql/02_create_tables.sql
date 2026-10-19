-- StockFlow - Modelo relacional
-- Ejecutar después de 01_create_database.sql

USE stockflow_db;

CREATE TABLE rol (
    id_rol INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE sucursal (
    id_sucursal INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_sucursal_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    id_rol INT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    usuario VARCHAR(60) NOT NULL,
    clave_hash VARCHAR(255) NOT NULL,
    email VARCHAR(120),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_usuario_usuario UNIQUE (usuario),
    CONSTRAINT uq_usuario_email UNIQUE (email),
    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE usuario_sucursal (
    id_usuario INT NOT NULL,
    id_sucursal INT NOT NULL,
    PRIMARY KEY (id_usuario, id_sucursal),
    CONSTRAINT fk_usuario_sucursal_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_usuario_sucursal_sucursal
        FOREIGN KEY (id_sucursal) REFERENCES sucursal(id_sucursal)
) ENGINE=InnoDB;

CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

CREATE TABLE producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    id_categoria INT NOT NULL,
    codigo VARCHAR(40) NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_producto_codigo UNIQUE (codigo),
    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE inventario (
    id_inventario INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    id_sucursal INT NOT NULL,
    stock_actual INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    stock_maximo INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_inventario_producto_sucursal
        UNIQUE (id_producto, id_sucursal),
    CONSTRAINT chk_inventario_stock_actual
        CHECK (stock_actual >= 0),
    CONSTRAINT chk_inventario_stock_minimo
        CHECK (stock_minimo >= 0),
    CONSTRAINT chk_inventario_stock_maximo
        CHECK (stock_maximo >= stock_minimo),
    CONSTRAINT fk_inventario_producto
        FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CONSTRAINT fk_inventario_sucursal
        FOREIGN KEY (id_sucursal) REFERENCES sucursal(id_sucursal)
) ENGINE=InnoDB;

CREATE TABLE alerta (
    id_alerta INT AUTO_INCREMENT PRIMARY KEY,
    id_inventario INT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_cierre DATETIME NULL,
    CONSTRAINT chk_alerta_tipo
        CHECK (tipo IN ('DEFICIT', 'SOBRESTOCK')),
    CONSTRAINT chk_alerta_estado
        CHECK (estado IN ('ACTIVA', 'RESUELTA')),
    CONSTRAINT chk_alerta_fechas
        CHECK (fecha_cierre IS NULL OR fecha_cierre >= fecha_generacion),
    CONSTRAINT fk_alerta_inventario
        FOREIGN KEY (id_inventario) REFERENCES inventario(id_inventario)
) ENGINE=InnoDB;

CREATE TABLE recomendacion (
    id_recomendacion INT AUTO_INCREMENT PRIMARY KEY,
    id_inventario_origen INT NOT NULL,
    id_inventario_destino INT NOT NULL,
    cantidad_recomendada INT NOT NULL,
    cantidad_aprobada INT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_generacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_decision INT NULL,
    fecha_decision DATETIME NULL,
    motivo_decision VARCHAR(255) NULL,
    CONSTRAINT chk_recomendacion_inventarios
        CHECK (id_inventario_origen <> id_inventario_destino),
    CONSTRAINT chk_recomendacion_cantidad_recomendada
        CHECK (cantidad_recomendada > 0),
    CONSTRAINT chk_recomendacion_cantidad_aprobada
        CHECK (
            cantidad_aprobada IS NULL
            OR (cantidad_aprobada > 0 AND cantidad_aprobada <= cantidad_recomendada)
        ),
    CONSTRAINT chk_recomendacion_estado
        CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    CONSTRAINT chk_recomendacion_aprobada
        CHECK (
            (estado = 'APROBADA' AND cantidad_aprobada IS NOT NULL AND id_usuario_decision IS NOT NULL AND fecha_decision IS NOT NULL)
            OR estado <> 'APROBADA'
        ),
    CONSTRAINT chk_recomendacion_rechazada
        CHECK (
            (estado = 'RECHAZADA' AND id_usuario_decision IS NOT NULL AND fecha_decision IS NOT NULL)
            OR estado <> 'RECHAZADA'
        ),
    CONSTRAINT fk_recomendacion_origen
        FOREIGN KEY (id_inventario_origen) REFERENCES inventario(id_inventario),
    CONSTRAINT fk_recomendacion_destino
        FOREIGN KEY (id_inventario_destino) REFERENCES inventario(id_inventario),
    CONSTRAINT fk_recomendacion_usuario_decision
        FOREIGN KEY (id_usuario_decision) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE transferencia (
    id_transferencia INT AUTO_INCREMENT PRIMARY KEY,
    id_recomendacion INT NOT NULL,
    id_usuario_responsable INT NOT NULL,
    cantidad INT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_finalizacion DATETIME NULL,
    observacion VARCHAR(255) NULL,
    CONSTRAINT uq_transferencia_recomendacion UNIQUE (id_recomendacion),
    CONSTRAINT chk_transferencia_cantidad
        CHECK (cantidad > 0),
    CONSTRAINT chk_transferencia_estado
        CHECK (estado IN ('PENDIENTE', 'EN_TRANSITO', 'COMPLETADA', 'CANCELADA')),
    CONSTRAINT chk_transferencia_fecha_finalizacion
        CHECK (
            (estado = 'COMPLETADA' AND fecha_finalizacion IS NOT NULL)
            OR estado <> 'COMPLETADA'
        ),
    CONSTRAINT fk_transferencia_recomendacion
        FOREIGN KEY (id_recomendacion) REFERENCES recomendacion(id_recomendacion),
    CONSTRAINT fk_transferencia_usuario
        FOREIGN KEY (id_usuario_responsable) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE historial_transferencia (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_transferencia INT NOT NULL,
    id_usuario INT NOT NULL,
    estado VARCHAR(20) NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255) NULL,
    CONSTRAINT chk_historial_transferencia_estado
        CHECK (estado IN ('PENDIENTE', 'EN_TRANSITO', 'COMPLETADA', 'CANCELADA')),
    CONSTRAINT fk_historial_transferencia
        FOREIGN KEY (id_transferencia) REFERENCES transferencia(id_transferencia),
    CONSTRAINT fk_historial_transferencia_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE movimiento (
    id_movimiento INT AUTO_INCREMENT PRIMARY KEY,
    id_inventario INT NOT NULL,
    id_usuario INT NOT NULL,
    id_transferencia INT NULL,
    tipo VARCHAR(20) NOT NULL,
    cantidad INT NOT NULL,
    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacion VARCHAR(255) NULL,
    CONSTRAINT chk_movimiento_tipo
        CHECK (tipo IN ('ENTRADA', 'SALIDA')),
    CONSTRAINT chk_movimiento_cantidad
        CHECK (cantidad > 0),
    CONSTRAINT fk_movimiento_inventario
        FOREIGN KEY (id_inventario) REFERENCES inventario(id_inventario),
    CONSTRAINT fk_movimiento_usuario
        FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_movimiento_transferencia
        FOREIGN KEY (id_transferencia) REFERENCES transferencia(id_transferencia)
) ENGINE=InnoDB;

CREATE INDEX idx_inventario_producto ON inventario(id_producto);
CREATE INDEX idx_inventario_sucursal ON inventario(id_sucursal);
CREATE INDEX idx_alerta_estado ON alerta(estado, tipo);
CREATE INDEX idx_recomendacion_estado ON recomendacion(estado);
CREATE INDEX idx_transferencia_estado ON transferencia(estado);
CREATE INDEX idx_movimiento_fecha ON movimiento(fecha_hora);
CREATE INDEX idx_historial_fecha ON historial_transferencia(fecha_hora);
