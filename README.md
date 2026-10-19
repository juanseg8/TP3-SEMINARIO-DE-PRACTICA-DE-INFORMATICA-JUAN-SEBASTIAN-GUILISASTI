# StockFlow

Aplicación de escritorio desarrollada en **Java 17 y JavaFX 17** para consultar inventario multisucursal, detectar déficit y sobrestock, generar recomendaciones y administrar transferencias internas persistidas en **MySQL 8 mediante JDBC**.

Este proyecto corresponde al Trabajo Práctico 3. Incluye los conceptos de Java pedidos en la consigna y un circuito completo conectado a MySQL.

## 1. Funcionalidades

- Inicio de sesión contra usuarios activos de MySQL.
- Contraseñas protegidas con PBKDF2; no se almacenan en texto plano.
- Inicio con usuario, rol y métricas reales del sistema.
- Menú principal: **Inicio | Inventario | Recomendaciones | Transferencias | Salir**.
- Inventario consolidado con filtros por producto, sucursal y estado.
- Clasificación automática: `Déficit`, `Sobrestock` o `Normal`.
- Generación de recomendaciones sin duplicar pendientes existentes.
- Aprobación o rechazo de recomendaciones con trazabilidad del usuario.
- Creación automática de transferencias `PENDIENTE` al aprobar.
- Ciclo de transferencia: `PENDIENTE → EN_TRANSITO → COMPLETADA`.
- Cancelación con motivo desde `PENDIENTE` o `EN_TRANSITO`.
- Actualización atómica de ambos stocks, movimientos e historial.
- `COMMIT` ante éxito y `ROLLBACK` completo ante una falla.

## 2. Requisitos para ejecutar

| Componente | Versión utilizada |
|---|---|
| Java JDK | 17 |
| JavaFX | 17.0.2, descargado por Maven |
| MySQL Server | 8.x |
| Maven | Incluido para Windows mediante `mvnw.cmd` |
| Sistema probado | Windows 10 |

Comprobar Java:

```bat
java -version
```

La salida debe indicar Java 17. Si existen varias versiones instaladas, definir `JAVA_HOME` antes de ejecutar Maven.

## 3. Preparar MySQL

Iniciar MySQL y ejecutar, en este orden:

1. `sql/01_create_database.sql`
2. `sql/02_create_tables.sql`
3. `sql/03_seed_data.sql`

Puede hacerse desde MySQL Workbench abriendo cada archivo y ejecutándolo completo, o desde consola:

```bat
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p < sql\01_create_database.sql
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p < sql\02_create_tables.sql
"C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" -u root -p < sql\03_seed_data.sql
```

El escenario inicial esperado contiene:

| Dato | Cantidad |
|---|---:|
| Inventarios | 9 |
| Recomendaciones `PENDIENTE` | 4 |
| Transferencias | 0 |
| Movimientos | 0 |

> Los scripts `06` a `08` modifican datos para demostrar `ROLLBACK` y `COMMIT`. No deben ejecutarse para preparar el escenario inicial.

## 4. Configurar la conexión

StockFlow no contiene contraseñas de MySQL en el código. La conexión se configura con estas variables:

| Variable | Ejemplo |
|---|---|
| `STOCKFLOW_DB_URL` | `jdbc:mysql://localhost:3306/stockflow_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `STOCKFLOW_DB_USER` | Usuario MySQL con acceso a `stockflow_db` |
| `STOCKFLOW_DB_PASSWORD` | Contraseña de ese usuario |

Se puede usar un usuario existente, incluido `root` para una prueba local. Si se prefiere crear el usuario que la aplicación usa por defecto, ejecutar en MySQL y reemplazar `clave_local` por una contraseña propia:

```sql
CREATE USER IF NOT EXISTS 'stockflow_app'@'localhost' IDENTIFIED BY 'clave_local';
GRANT SELECT, INSERT, UPDATE, DELETE ON stockflow_db.* TO 'stockflow_app'@'localhost';
FLUSH PRIVILEGES;
```

En ese caso, configurar `STOCKFLOW_DB_USER=stockflow_app` y usar la misma contraseña en `STOCKFLOW_DB_PASSWORD`.

Configuración temporal en CMD:

```bat
set STOCKFLOW_DB_URL=jdbc:mysql://localhost:3306/stockflow_db?useSSL=false^&serverTimezone=UTC^&allowPublicKeyRetrieval=true
set STOCKFLOW_DB_USER=tu_usuario
set STOCKFLOW_DB_PASSWORD=tu_contrasena
```

Configuración persistente para futuras terminales:

```bat
setx STOCKFLOW_DB_URL "jdbc:mysql://localhost:3306/stockflow_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
setx STOCKFLOW_DB_USER "tu_usuario"
setx STOCKFLOW_DB_PASSWORD "tu_contrasena"
```

Después de usar `setx`, cerrar y abrir la terminal. No subir credenciales reales al proyecto.

## 5. Compilar y ejecutar

Abrir CMD o PowerShell en la carpeta raíz del proyecto.

Compilación completa:

```bat
mvnw.cmd clean verify
```

Resultado esperado:

```text
BUILD SUCCESS
```

Ejecución:

```bat
mvnw.cmd javafx:run
```

La advertencia de Maven sobre `filename-based automodules` corresponde a MySQL Connector/J y no impide compilar ni ejecutar la aplicación.

En Unix se necesita Maven instalado:

```bash
mvn clean verify
mvn javafx:run
```

## 6. Credenciales de evaluación

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `Admin123!` | Administrador |
| `encargado.centro` | `Centro123!` | Encargado de sucursal |
| `compras` | `Compras123!` | Responsable de inventario y compras |

Para demostrar todo el circuito se recomienda utilizar `compras`.

## 7. Recorrido sugerido para la presentación

1. Iniciar sesión con `compras / Compras123!`.
2. Mostrar Inicio, las métricas consultadas desde MySQL y el menú principal.
3. Abrir Inventario y aplicar filtros por producto, sucursal y estado.
4. Presionar **Generar recomendaciones** y comprobar que no se duplican las pendientes.
5. Seleccionar una recomendación y aprobarla.
6. Abrir Transferencias y comprobar que se creó en estado `PENDIENTE`.
7. Iniciar el traslado para pasar a `EN_TRANSITO`.
8. Completarlo y verificar que cambia a `COMPLETADA`.
9. Volver a Inventario y comprobar la actualización de origen y destino.
10. Cerrar sesión con **Salir**.

También pueden demostrarse el rechazo y la cancelación, que solicitan un motivo.

## 8. Arquitectura

```text
JavaFX / FXML
      ↓
Controladores
      ↓
Servicios de aplicación
      ↓
Interfaces Repository
      ↓
Implementaciones JDBC
      ↓
MySQL 8
```

- `controller`: eventos, navegación y actualización de vistas.
- `service`: validaciones y coordinación de casos de uso.
- `repository`: contratos de persistencia y abstracción.
- `repository.jdbc`: SQL, transacciones y mapeo de resultados.
- `model`: entidades y reglas propias del dominio.
- `session`: usuario autenticado durante la navegación.
- `config`: variables de entorno y apertura de conexiones JDBC.

MySQL es la única fuente de verdad. Al navegar se crean controladores nuevos y cada pantalla vuelve a consultar los datos persistidos.

## 9. Requisitos de la consigna

| Requisito | Evidencia en el proyecto |
|---|---|
| Sintaxis y tipos de datos | Primitivos, `String`, `LocalDateTime`, arreglos, colecciones genéricas, records y estados de dominio controlados. |
| Estructuras condicionales | Clasificación de inventario, validación de estados, habilitación de botones y reglas transaccionales. |
| Estructuras repetitivas | Carga de filtros, recorrido de resultados JDBC y procesamiento uniforme de movimientos. |
| Objetos y constructores | Modelos `Inventario`, `Producto`, `Sucursal`, `Recomendacion`, `Transferencia`, `Usuario` y movimientos. |
| Encapsulamiento | Campos privados, mayormente inmutables, accedidos mediante getters y métodos de negocio. |
| Abstracción | Interfaces `Repository`, servicios desacoplados y clase abstracta `MovimientoInventario`. |
| Herencia | `MovimientoEntrada` y `MovimientoSalida` extienden `MovimientoInventario`. |
| Polimorfismo | Ambos movimientos se procesan como `MovimientoInventario` invocando `aplicarSobre`. |
| Excepciones | Jerarquía `StockFlowException`, traducción de `SQLException`, mensajes de interfaz y rollback. |
| Menú de selección | Barra uniforme con Inicio, Inventario, Recomendaciones, Transferencias y Salir. |
| Búsqueda | Filtros parametrizados por producto, sucursal y estado mediante `PreparedStatement`. |
| Ordenación | Consultas con `ORDER BY` y columnas ordenables de `TableView`. |
| Persistencia | JDBC, MySQL, consultas parametrizadas y cierre de recursos con try-with-resources. |
| Integridad | Aprobación y finalización transaccionales con `commit`, `rollback` y `SELECT ... FOR UPDATE`. |

La búsqueda se realiza con filtros SQL parametrizados. La ordenación se aplica con `ORDER BY` y también desde las columnas de las tablas JavaFX.

## 10. Verificación funcional

Se probaron los siguientes casos:

- Compilación limpia mediante `mvnw.cmd clean verify`.
- Inicio de la aplicación mediante `mvnw.cmd javafx:run`.
- Carga real de Login, Inicio, Inventario, Recomendaciones y Transferencias.
- Autenticación válida e inválida.
- Métricas y filtros contra MySQL.
- Prevención de recomendaciones pendientes duplicadas.
- Aprobación, rechazo, inicio, cancelación y finalización.
- Actualización de stock y registro de dos movimientos al completar.
- `ROLLBACK` sin movimientos parciales ante stock insuficiente.
- Restauración final mediante los scripts `01` a `03`.

## 11. Solución de problemas

### No se pudo conectar con MySQL

- Confirmar que MySQL esté iniciado.
- Verificar las tres variables `STOCKFLOW_DB_*`.
- Confirmar que exista `stockflow_db` y que el usuario tenga permisos.

### `JAVA_HOME` no está definido correctamente

Apuntar `JAVA_HOME` al JDK 17, no a la carpeta `bin`:

```bat
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17
```

### El login no acepta las credenciales

Restaurar `sql/01_create_database.sql`, `02_create_tables.sql` y `03_seed_data.sql`. Las credenciales de demostración dependen de los hashes incluidos en el seed.

### La ventana se cerró pero Maven muestra `BUILD SUCCESS`

Es el comportamiento normal: `javafx:run` finaliza cuando se cierra la ventana principal.
