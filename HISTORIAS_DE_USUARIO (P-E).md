# Historias de Usuario - Sistema de Gestión de Pedidos y Envíos

Especificaciones funcionales completas del sistema CRUD de Pedidos y Envíos
## Tabla de Contenidos

- [Épica 1: Gestión de Pedidos](#épica-1-gestión-de-pedidos)
- [Épica 2: Gestión de Envíos](#épica-2-gestión-de-envios)
- [Épica 3: Operaciones Asociadas](#épica-3-operaciones-asociadas)
- [Reglas de Negocio](#reglas-de-negocio)
- [Modelo de Datos](#modelo-de-datos)
- [Queries Principales](#queries-principales)
- [Flujos Técnicos Críticos](#flujos-tecnicos-criticos)
- [Resumen de Operaciones del Menú](#resumen-de-operaciones-del-menu)

---

## Épica 1: Gestión de Pedidos

### HU-001: rear Pedido

**Como** usuario del sistema
**Quiero** crear un registro de pedido con sus datos básicos
**Para** almacenar pedidos en la base de datos; opcionalmente asociar un envío

#### Criterios de Aceptación

```gherkin
Escenario: Crear pedido sin envío
  Dado que el usuario selecciona "Crear pedido"
  Cuando ingresa nroPedido "P-0001", descripcion "Remera", cantidad "2", precioUnitario "1500.00"
  Y responde "n" a agregar envío
  Entonces el sistema crea el pedido con ID autogenerado
  Y muestra "Pedido creado exitosamente con ID: X"

Escenario: Crear pedido con envío
  Dado que el usuario selecciona "Crear pedido"
  Cuando ingresa nroPedido "P-0002", descripcion "Libro", cantidad "1", precioUnitario "3500.00"
  Y responde "s" a agregar envío
  Y ingresa direccion "Av. Libertador 1000", fechaEnvio "2025-11-01"
  Entonces el sistema crea el envío primero
  Y luego crea el pedido con referencia al envío
  Y muestra "Pedido creado exitosamente con ID: X"

Escenario: Intento de crear pedido con nroPedido duplicado
  Dado que existe un pedido con nroPedido "P-0001"
  Cuando el usuario intenta crear otro pedido con el mismo nroPedido
  Entonces el sistema muestra "Ya existe un pedido con el número: P-0001"
  Y no crea el registro

Escenario: Intento de crear pedido con campos vacíos
  Dado que el usuario selecciona "Crear pedido"
  Cuando deja la descripción vacía (solo espacios o enter)
  Entonces el sistema muestra "La descripción no puede estar vacía"
  Y no crea el registro
```

#### Reglas de Negocio Aplicables

- **RN-001**: nro_pedido y descripcion son obligatorios.
- **RN-002**: nro_pedido debe ser único.
- **RN-003**: Espacios iniciales y finales se eliminan automáticamente
- **RN-004**: El nro_pedido se genera automáticamente
- **RN-005**: El envío es opcional en la creación de pedido.

#### Implementación Técnica

- **Clase**: `MenuHandler.crearPedido`
- **Servicio**: `PedidoServiceImpl.insertar()`
- **Validación**: `PedidoServiceImpl.validatePedido()` + `validateNroPedidoUnique()`
- **Flujo**:
  1. Captura entrada con `.trim()`
  2. Crea objeto Pedido
  3. Si el usuario agrega envío y envio.id == 0 → EnvioServiceImpl.insertar()
  4. Inserta pedido con envio_id (nullable)
  5. Genera ID automático con `Statement.RETURN_GENERATED_KEYS`

---

### HU-002: Listar Todos los Pedidos

**Como** usuario del sistema
**Quiero** ver un listado de todos los pedidos registrados
**Para** consultar los pedidos y, si tienen envío, ver los datos del envío

#### Criterios de Aceptación

```gherkin
Escenario: Listar pedidos con envío
  Dado que existen pedidos con envíos asociados
  Cuando el usuario selecciona "Listar pedidos"
  Entonces el sistema muestra todos los pedidos con eliminado = FALSE
  Y para cada pedido con envío muestra "Envío: [direccion] [fecha_envio]"

Escenario: Listar pedidos sin envío
  Dado que existen pedidos sin envío
  Cuando el usuario lista todos los pedidos
  Entonces el sistema muestra los pedidos y para los que no tienen envío no se muestra línea de envío

Escenario: No hay pedidos en el sistema
  Dado que no existen pedidos activos
  Cuando el usuario lista pedidos
  Entonces el sistema muestra "No se encontraron pedidos."
```

#### Reglas de Negocio Aplicables

- **RN-006**: Solo se listan pedidos con eliminado = FALSE.
- **RN-007**: El envío se obtiene mediante LEFT JOIN.
- **RN-008**: Si envio_id es NULL, no se muestra envío.

#### Implementación Técnica

- **Clase**: `MenuHandler.listarPedidos()`
- **Servicio**: `PedidoServiceImpl.getAll()`
- **DAO**: `PedidoDAO.getAll()` con `SELECT y LEFT JOIN a envios.`

---

### HU-003: Buscar Pedidos por NroPedido o Descripción

**Como** usuario del sistema
**Quiero** buscar pedidos por número o descripción
**Para** encontrar rápidamente un pedido específico

#### Criterios de Aceptación

```gherkin
Escenario: Buscar por nroPedido exacto
  Dado que existe pedido "P-0001"
  Cuando el usuario busca por "P-0001"
  Entonces el sistema muestra el pedido correspondiente

Escenario: Buscar por descripción con coincidencia parcial
  Dado que existen pedidos "Remera blanca" y "Remera negra"
  Cuando el usuario busca por "rem"
  Entonces el sistema muestra ambos pedidos

Escenario: Búsqueda sin resultados
  Dado que no existen pedidos con "Teléfono"
  Cuando el usuario busca por "Teléfono"
  Entonces el sistema muestra "No se encontraron pedidos."
```

#### Reglas de Negocio Aplicables

- **RN-009**: La búsqueda es case-insensitive
- **RN-010**: Se busca con LIKE %texto% en descripcion
- **RN-010**: Se busca con exact match en nro_pedido.
- **RN-012**: Espacios se eliminan automáticamente
- **RN-013**: No se permiten búsquedas vacías

#### Implementación Técnica &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& REFACTORIZAR

- **Clase**: `MenuHandler.listarPedidos() (líneas 49-82, subopción 2, línea 59 con trim())`
- **Servicio**: `PedidoServiceImpl.buscarPorNroPedidoDescripcion()`
- **DAO**: `PedidoDAO.buscarPorNroPedidoDescripcion()` con `SEARCH_BY_NRO_DESC_SQL`
- **Query**:
  ```sql
  SELECT p.id, p.nro_pedido, p.descripcion, p.cantidad, p.precio_unitario, p.estado, p.envio_id,
       e.id AS envio_id, e.direccion, e.fecha_envio, e.transportista
  FROM pedidos p
  LEFT JOIN envios e ON p.envio_id = e.id
  WHERE p.eliminado = FALSE
    AND (
          p.nro_pedido = ? 
      OR LOWER(p.descripcion) LIKE ?
    );
  ```
- **Parámetros**: 
  nroPedidoParam = filtro.trim() — usado para match exacto de nro_pedido.

  descripcionParam = "%"+filtro.trim().toLowerCase()+"%" — usado en LOWER(p.descripcion) LIKE ?.

---

### HU-004: Actualizar Pedido

**Como** usuario del sistema
**Quiero** modificar los datos de un pedido existente
**Para** mantener la información actualizada

#### Criterios de Aceptación

```gherkin
Escenario: Actualizar solo descripcion
  Dado que existe pedido con ID 1, descripcion "Remera"
  Cuando el usuario actualiza pedido ID 1
  Y presiona Enter en nroPedido y cantidad
  Y escribe "Remera estampada" en descripcion
  Entonces el sistema actualiza solo la descripcion
  Y mantiene los demás campos sin cambios

Escenario: Actualizar nroPedido a uno duplicado
  Dado que existen pedidos con nroPedido "P-001" y "P-002"
  Cuando el usuario intenta cambiar P-002 a P-001
  Entonces el sistema muestra "Ya existe un pedido con el número: P-001"
  Y no actualiza

Escenario: Agregar envío a pedido sin envío
  Dado que pedido ID 1 no tiene envío
  Cuando el usuario actualiza el pedido y responde "s" a agregar envío
  Y escribe direccion y fechaEnvio
  Entonces el sistema crea el envío y lo asocia al pedido
```

#### Reglas de Negocio Aplicables

- **RN-014**: Validación de nro_pedido único excepto para el mismo pedido cuando se actualiza.
- **RN-015**: Campos vacíos (Enter) mantienen valor original
- **RN-016**: Se requiere ID > 0 para actualizar
- **RN-017**: Se puede agregar/actualizar envío durante la actualización.
- **RN-018**: Trim se aplica antes de validar si el campo está vacío

#### Implementación Técnica

- **Clase**: `MenuHandler.actualizarPedido()` (líneas 84-119)
- **Servicio**: `PedidoServiceImpl.actualizar()`
- **Validación**:  `validateNumeroPedidoUnique(numeroPedido, pedidoId)` permite mantener el mismo número de pedido al actualizar el propio registro.
- **Pattern**:
  ```java
  System.out.print("Nueva descripción (dejar vacío para mantener): ");
  String descripcion = scanner.nextLine().trim();
  if (!descripcion.isEmpty()) {
      pedido.setDescripcion(descripcion);
  }
  ```

---

### HU-005: Eliminar Pedido

**Como** usuario del sistema
**Quiero** eliminar un pedido del sistema
**Para** mantener sólo pedidos activos

#### Criterios de Aceptación

```gherkin
Escenario: Eliminar pedido existente
  Dado que existe pedido con ID 1
  Cuando el usuario elimina el pedido ID 1
  Entonces el sistema marca eliminado = TRUE
  Y muestra "Pedido eliminado exitosamente."

Escenario: Eliminar pedido inexistente
  Dado que no existe pedido ID 999
  Cuando intenta eliminarlo
  Entonces el sistema muestra "No se encontró pedido con ID: 999"

Escenario: Pedido eliminado no aparece en listados
  Dado que se eliminó pedido ID 1
  Cuando se lista todos los pedidos
  Entonces el pedido ID 1 no aparece
```

#### Reglas de Negocio Aplicables

- **RN-019**: Eliminación es lógica, no física
- **RN-020**: Se ejecuta `UPDATE pedidos SET eliminado = TRUE`
- **RN-021**: El envio asociado NO se elimina automáticamente
- **RN-022**: Se verifica `rowsAffected` para confirmar eliminación

#### Implementación Técnica

- **Clase**: `MenuHandler.eliminarPedido()` (líneas 121-130)
- **Servicio**: `PedidoServiceImpl.eliminar()`
- **DAO**: `PedidoDAO.eliminar()` con `DELETE_SQL`
- **Query**: `UPDATE pedidos SET eliminado = TRUE WHERE id = ?`
`

---

## Épica 2: Gestión de Envíos

### HU-006: Crear Envío Independiente

**Como** usuario del sistema
**Quiero** crear un envío sin asociarlo a ningún pedido
**Para** tener envíos disponibles para asignación posterior

#### Criterios de Aceptación

```gherkin
Escenario: Crear envío válido
  Dado que el usuario selecciona "Crear envío"
  Cuando ingresa direccion "Córdoba 200", fechaEnvio "2025-11-02", transportista "CorreoX"
  Entonces el sistema crea el envío con ID autogenerado
  Y muestra "Envío creado exitosamente con ID: X"

Escenario: Crear envío con campos vacíos
  Dado que el usuario selecciona "Crear envío"
  Cuando deja direccion vacía
  Entonces el sistema muestra "La dirección no puede estar vacía"
  Y no crea el envío
```

#### Reglas de Negocio Aplicables

- **RN-023**: direccion y fecha_envio son obligatorios
- **RN-024**: Se eliminan espacios iniciales y finales
- **RN-025**: ID se genera automáticamente

#### Implementación Técnica

- **Clase**: `MenuHandler.crearEnvioIndependiente()`
- **Servicio**: `EnvioServiceImpl.insertar()`
- **DAO**: `EnvioDAO.insertar()`

---

### HU-007: Listar Envíos

**Como** usuario del sistema
**Quiero** ver todos los envíos registrados
**Para** consultar envíos disponibles o asociados

#### Criterios de Aceptación

```gherkin
Escenario: Listar envíos existentes
  Dado que existen envíos en el sistema
  Cuando el usuario selecciona "Listar envíos"
  Entonces el sistema muestra ID, direccion, fechaEnvio, transportista
  Y solo muestra envíos con eliminado = FALSE

Escenario: No hay envíos
  Dado que no existen envíos activos
  Cuando el usuario lista envíos
  Entonces el sistema muestra "No se encontraron envíos."
```

#### Reglas de Negocio Aplicables

- **RN-026**: Solo se listan envíos con `eliminado = FALSE`
- **RN-027**: Mostrar envíos independientemente de si están asociados a pedidos o no.

#### Implementación Técnica

- **Clase**: `MenuHandler.listarDomicilios()` (líneas 142-155)
- **Servicio**: `DomicilioServiceImpl.getAll()`
- **DAO**: `DomicilioDAO.getAll()` con `SELECT_ALL_SQL`
- **Query**: `SELECT * FROM domicilios WHERE eliminado = FALSE`

---

### HU-008: Eliminar Envío por ID (Operación Peligrosa)

**Como** usuario del sistema
**Quiero** eliminar un envío directamente por su ID
**Para** remover envíos no utilizados

⚠️ **ADVERTENCIA**: Esta operación puede dejar referencias huérfanas si el envío está asociado a un pedido.

#### Criterios de Aceptación

```gherkin
Escenario: Eliminar envío no asociado
  Dado que existe envio ID 5 sin pedidos asociados
  Cuando el usuario elimina envio ID 5
  Entonces el sistema marca eliminado = TRUE
  Y muestra "Envío eliminado exitosamente."

Escenario: Eliminar envío asociado (problema)
  Dado que envio ID 1 está asociado a pedido ID 10
  Cuando el usuario elimina envio ID 1 por esta opción
  Entonces el sistema marca el envio como eliminado
  Pero el pedido ID 10 mantiene envio_id = 1
  Y queda una referencia huérfana en la base de datos
```

#### Reglas de Negocio Aplicables

- **RN-028**: Eliminación es lógica
- **RN-029**: ⚠️ NO verifica si está asociado a pedidos
- **RN-030**: Puede causar referencias huérfanas
- **RN-031**: Usar HU-010 como alternativa segura

#### Implementación Técnica 

- **Clase**: `MenuHandler.eliminarEnvioPorId()` (líneas 187-196)
- **Servicio**: `EnvioServiceImpl.eliminar()`
- **DAO**: `EnvioDAO.eliminar()`
- **Limitación**: No verifica si existen `pedidos.envio_id` asociados antes de eliminar el registro.

---

### HU-009: Actualizar Envío por ID

**Como** usuario del sistema
**Quiero** actualizar un envío usando su ID
**Para** corregir datos de envío

#### Criterios de Aceptación

```gherkin
Escenario: Actualizar direccion de envío
  Dado que existe envio ID 1 con direccion "San Martín"
  Cuando el usuario actualiza envio ID 1
  Y escribe "Belgrano" en direccion
  Y presiona Enter en transportista
  Entonces el sistema actualiza solo la direccion
  Y mantiene transportista sin cambios

Escenario: Actualizar envío inexistente
  Dado que no existe envio ID 999
  Cuando el usuario intenta actualizarlo
  Entonces el sistema muestra "Envío no encontrado."
```

#### Reglas de Negocio Aplicables

- **RN-032**: Se permite actualizar cualquier envío por ID.
- **RN-033**: Campos vacíos mantienen valor original.
- **RN-034**: La actualización afecta a todos los pedidos que compartan el envio_id.

#### Implementación Técnica

- **Clase**: `MenuHandler.actualizarEnvioPorId()` (líneas 157-185)
- **Servicio**: `EnvioServiceImpl.actualizar()`
- **DAO**: `EnvioDAO.actualizar()` con `UPDATE_SQL`
- **Pattern**: Usa `.trim()` y verifica `isEmpty()`

---

## Épica 3: Operaciones Asociadas

### HU-010: Eliminar Envío por Pedido (Operación Segura)

**Como** usuario del sistema
**Quiero** eliminar el envío asociado a un pedido específico
**Para** remover la referencia sin dejar pedidos con envio_id huérfano

✅ **RECOMENDADO**: Esta es la forma segura de eliminar un envío asociado

#### Criterios de Aceptación

```gherkin
Escenario: Eliminar envío de pedido correctamente
  Dado que pedido ID 1 tiene envio ID 5
  Cuando el usuario elimina envio por pedido ID 1
  Entonces el sistema primero actualiza pedido.envio_id = NULL
  Y luego marca envio ID 5 como eliminado = TRUE
  Y muestra "Envío eliminado exitosamente y referencia actualizada."

Escenario: Pedido sin envío
  Dado que pedido ID 1 no tiene envio
  Cuando el usuario intenta eliminar su envio
  Entonces el sistema muestra "El pedido no tiene envío asociado."
  Y no ejecuta ninguna operación

Escenario: Validación de pertenencia
  Dado que pedido ID 1 tiene envio ID 5
  Cuando el servicio intenta eliminar envio ID 7 de pedido ID 1
  Entonces el sistema muestra "El envío no pertenece a este pedido"
  Y no elimina nada
```

#### Reglas de Negocio Aplicables

- **RN-035**: Se actualiza la FK (pedido.envio_id) antes de marcar el envío como eliminado.
- **RN-036**: Se valida que el envío pertenezca al pedido.
- **RN-037**: Operación en dos pasos: UPDATE pedidos → UPDATE envios.
- **RN-038**: Sin transacción explícita (si no se usa), el orden correcto previene huérfanos.

#### Implementación Técnica

- **Clase**: `MenuHandler.eliminarEnvioPorPedido()`
- **Servicio**: `PersonaServiceImpl.eliminarEnvioDePedido()`
- **Flujo**:    &&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& REFACTORIZAR
  1. Valida IDs > 0
  2. Obtiene pedido por ID
  3. Valida que tenga envio
  4. Valida que el envio_id coincida
  5. `pedido.setEnvio(null)`
  6. `pedidoDAO.actualizar(envio)` → envio_id = NULL
  7. `envioServiceImpl.eliminar(envio)` → eliminado = TRUE

#### Comparación HU-008 vs HU-010

| Aspecto | HU-008 (Por ID) | HU-010 (Por Persona) |
|---------|-----------------|----------------------|
| **Validación** | No verifica asociación | Verifica pertenencia |
| **Referencias** | Puede dejar huérfanas | Actualiza FK primero |
| **Seguridad** | ⚠️ Peligroso | ✅ Seguro |
| **Uso recomendado** | Solo para domicilios independientes | Para domicilios asociados |

---

### HU-011: Actualizar Envío por Pedido

**Como** usuario del sistema
**Quiero** actualizar el envío de un pedido específico
**Para** corregir la información del envío sin afectar otros envíos

#### Criterios de Aceptación

```gherkin
Escenario: Actualizar envío de pedido
  Dado que pedido ID 1 tiene envio con direccion "San Martín"
  Cuando el usuario actualiza envio por pedido ID 1
  Y escribe "Belgrano" en direccion
  Entonces el sistema actualiza el envío de ese pedido
  Y muestra "Envío actualizado exitosamente."

Escenario: Pedido sin envío
  Dado que pedido ID 1 no tiene envio
  Cuando el usuario intenta actualizar su envio
  Entonces el sistema muestra "El pedido no tiene envío asociado."
```

#### Reglas de Negocio Aplicables

- **RN-039**: Solo se actualiza el envío asociado al pedido especificado.
- **RN-040**: Si varias pedidos comparten el mismo envio_id, todas se afectan.
- **RN-041**: Se requiere que el pedido tenga envío asociado.

#### Implementación Técnica   

- **Clase**: `MenuHandler.actualizarEnvioPorPedido()` (líneas 198-232)
- **Servicio**: `EnvioServiceImpl.actualizar()`
- **Flujo**:
  1. Obtiene pedido por ID
  2. Valida que tenga envio (`p.getEnvio() != null`)
  3. Captura nuevos valores con trim()
  4. Actualiza objeto envio
  5. Llama a `envioService.actualizar()`

---

## Reglas de Negocio 

### Validación de Datos 

| Código | Regla                                                               |
| ------ | ------------------------------------------------------------------- |
| RN-001 | `nro_pedido`, `descripcion` obligatorios                            |
| RN-002 | `nro_pedido` único                                                  |
| RN-003 | `.trim()` automático en entradas                                    |
| RN-004 | IDs autogenerados                                                   |
| RN-005 | `envio` opcional en `Pedido`                                        |
| RN-006 | Solo listar pedidos `eliminado = FALSE`                             |
| RN-007 | Búsqueda case-insensitive / LIKE para descripción                   |
| RN-008 | No búsquedas vacías                                                 |
| RN-009 | `direccion` y `fecha_envio` obligatorios para envíos independientes |
| RN-010 | Solo listar envíos `eliminado = FALSE`                              |
| RN-011 | Actualización de envío por ID permitida                             |
| RN-P12 | Validación de `nro_pedido` único en update                          |


### Operaciones de Base de Datos 

| Código | Regla                                                          |
| ------ | -------------------------------------------------------------- |
| RN-014 | Eliminación lógica de pedidos (`eliminado = TRUE`)             |
| RN-015 | Eliminación lógica de envíos                                   |
| RN-016 | Actualizar FK antes de eliminar envío asociado                 |
| RN-017 | Validar pertenencia del envío al pedido al eliminar por pedido |
| RN-018 | Operación en dos pasos: `UPDATE pedidos` → `UPDATE envios`     |
| RN-019 | Coordinar `PedidoService` con `EnvioService`                   |
| RN-020 | Usar `PreparedStatement` y `try-with-resources`                |
| RN-021 | Verificar `rowsAffected` en updates/deletes lógicos            |


### Integridad Referencial (RN-028 a RN-041) 

| Código | Regla | Implementación |
|--------|-------|----------------|
| RN-028 | HU-008 no verifica referencias | `EnvioDAO.eliminar()` sin validación |
| RN-029 | Puede causar referencias huérfanas | FK apunta a envío  eliminado |
| RN-030 | HU-010 es alternativa segura | `PedidoServiceImpl.eliminarEnvioDePedido()` |
| RN-035 | Actualizar FK antes de eliminar | Orden: `pedido.setEnvio(null)` → actualizar → eliminar |
| RN-036 | Validar pertenencia | Verifica `pedido.getEnvio().getId() == envioId` |
| RN-037 | Operación en dos pasos | UPDATE pedidos → UPDATE envios |
| RN-040 | Compartir domicilio afecta a todos | Un envío puede estar en varios pedidos |

### Transacciones y Coordinación (RN-042 a RN-051) 

| Código | Regla | Implementación |
|--------|-------|----------------|
| RN-042 | PedidoService coordina con EnvioService| `PedidoServiceImpl` usa `EnvioServiceImpl` |
| RN-043 | Insertar envío antes que pedido | `PedidoServiceImpl.insertar()` |
| RN-044 | Try-with-resources para recursos | Todas las conexiones, statements, resultsets |
| RN-045 | PreparedStatements para prevenir SQL injection | 100% de queries |
| RN-046 | LEFT JOIN para relación opcional | Todas las queries de PedidoDAO |
| RN-047 | NULL seguro en FK | `setEnvioId()` usa `stmt.setNull(Types.INTEGER)` |
| RN-048 | TransactionManager soporta rollback | AutoCloseable con rollback en close() |
| RN-049 | Equals/HashCode de Pedido  basado en nro_pedido | nro_pedido es único |
| RN-050 | equals/hashCode de Envío basado en direccion + fecha_envio | Comparación semántica |
| RN-051 | Scanner se cierra al salir | `AppMenu.run()` |

---

## Modelo de Datos

### Diagrama Entidad-Relación

```
┌─────────────────────────────────────────┐
│                pedidos                  │
├─────────────────────────────────────────┤
│ id: INT PK AUTO_INCREMENT               │
│ nro_pedido: VARCHAR(30) NOT NULL UNIQUE │
│ descripcion: VARCHAR(200) NOT NULL      │
│ cantidad: INT NOT NULL                  │
│ precio_unitario: DECIMAL(12,2) NOT NULL │
│ estado: VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' │
│ envio_id: INT FK NULL                   │
│ eliminado: BOOLEAN DEFAULT FALSE        │
└───────────────┬─────────────────────────┘
                │ 0..1
                │
                │ FK
                │
                ▼
┌───────────────────────────────────────┐
│                envios                 │
├───────────────────────────────────────┤
│ id: INT PK AUTO_INCREMENT             │
│ direccion: VARCHAR(200) NOT NULL      │
│ fecha_envio: DATE NOT NULL            │
│ transportista: VARCHAR(100) NULL      │
│ eliminado: BOOLEAN DEFAULT FALSE      │
└───────────────────────────────────────┘

```

### Constraints y Validaciones

```sql
ALTER TABLE pedidos ADD CONSTRAINT uk_nro_pedido UNIQUE (nro_pedido);
ALTER TABLE pedidos ADD CONSTRAINT fk_envio FOREIGN KEY (envio_id) REFERENCES envios(id);

CREATE INDEX idx_pedido_nro ON pedidos(nro_pedido);
CREATE INDEX idx_pedido_descripcion ON pedidos(descripcion);
CREATE INDEX idx_pedido_eliminado ON pedidos(eliminado);
CREATE INDEX idx_envio_eliminado ON envios(eliminado);
```

### Queries Principales

#### SELECT con JOIN
```sql
SELECT p.id, p.nro_pedido, p.descripcion, p.cantidad, p.precio_unitario, p.estado, p.envio_id,
       e.id AS envio_id, e.direccion, e.fecha_envio, e.transportista
FROM pedidos p
LEFT JOIN envios e ON p.envio_id = e.id
WHERE p.eliminado = FALSE;
```

#### Búsqueda por nombre/apellido
```sql
-- búsqueda por nro_pedido exacto
SELECT ... FROM pedidos p LEFT JOIN envios e ON p.envio_id = e.id
WHERE p.eliminado = FALSE AND p.nro_pedido = ?;

-- búsqueda por descripción parcial (case-insensitive)
SELECT ... FROM pedidos p LEFT JOIN envios e ON p.envio_id = e.id
WHERE p.eliminado = FALSE AND p.descripcion LIKE ?;
-- param: '%' || filtro || '%'
```

#### Búsqueda por DNI
```sql
SELECT p.*, e.id AS envio_id, e.direccion, e.fecha_envio
FROM pedidos p LEFT JOIN envios e ON p.envio_id = e.id
WHERE p.id = ? AND p.eliminado = FALSE;
```

#### Soft delete pedido
```sql
UPDATE pedidos SET eliminado = TRUE WHERE id = ?;
```

#### Soft delete envío
```sql
UPDATE envios SET eliminado = TRUE WHERE id = ?;
```

---

## Flujos Técnicos Críticos

### Flujo 1: Crear Pedido con Envío

```
Usuario (MenuHandler.crearPedido)
    ↓ captura datos con .trim()
PedidoServiceImpl.insertar()
    ↓ validatePedido()
    ↓ validateNroPedidoUnique(nroPedido, null)
    ↓ if envio != null && envio.id == 0:
         EnvioServiceImpl.insertar()
            ↓ validateEnvio()
            ↓ EnvioDAO.insertar() -> obtiene ID
            ↓ envio.setId(generatedId)
    ↓ PedidoDAO.insertar(pedido) (con envio_id si existe)
        ↓ INSERT pedidos
        ↓ obtiene ID autogenerado
        ↓ pedido.setId(generatedId)
    ↓ return
Usuario recibe: "Pedido creado exitosamente con ID: X"
```

### Flujo 2: Eliminar Envío Seguro (por Pedido)

```
Usuario (MenuHandler.eliminarEnvioPorPedido)
    ↓ ingresa pedidoId
PedidoServiceImpl.eliminarEnvioDePedido(pedidoId, envioId)
    ↓ valida ids > 0
    ↓ pedido = pedidoDAO.getById(pedidoId)
    ↓ si pedido == null -> throw "Pedido no encontrado"
    ↓ if pedido.getEnvio() == null -> throw "Sin envío"
    ↓ if pedido.getEnvio().getId() != envioId -> throw "Envío no pertenece a este pedido"
    ↓ pedido.setEnvio(null)
    ↓ pedidoDAO.actualizar(pedido) -> UPDATE pedidos SET envio_id = NULL WHERE id = pedidoId
    ↓ envioServiceImpl.eliminar(envioId) -> UPDATE envios SET eliminado = TRUE WHERE id = envioId
    ↓ return
Usuario recibe: "Envío eliminado exitosamente y referencia actualizada."

```

### Flujo 3: Validación nro_pedido único en Update

```
Usuario actualiza pedido
    ↓ PedidoServiceImpl.actualizar(pedido)
        ↓ validatePedido(pedido)
        ↓ validateNroPedidoUnique(pedido.getNroPedido(), pedido.getId())
            ↓ existente = pedidoDAO.buscarPorNroPedido(nro)
            ↓ if existente != null:
                ↓ if pedidoId == null || existente.getId() != pedidoId:
                    ✗ throw "Ya existe un pedido con el número: X"
                ↓ else:
                    ✓ return (es el mismo pedido, OK)
            ↓ else:
                ✓ return (nro no existe, OK)
        ↓ pedidoDAO.actualizar(pedido)
    ↓ return
```

---

## Resumen de Operaciones del Menú

| Opción | Operación                   | Handler                      | HU             |
| ------ | --------------------------- | ---------------------------- | -------------- |
| 1      | Crear pedido                | `crearPedido()`              | HU-001         |
| 2      | Listar pedidos              | `listarPedidos()`            | HU-002, HU-003 |
| 3      | Actualizar pedido           | `actualizarPedido()`         | HU-004         |
| 4      | Eliminar pedido             | `eliminarPedido()`           | HU-005         |
| 5      | Crear envío                 | `crearEnvioIndependiente()`  | HU-006         |
| 6      | Listar envíos               | `listarEnvios()`             | HU-007         |
| 7      | Actualizar envío por ID     | `actualizarEnvioPorId()`     | HU-009         |
| 8      | Eliminar envío por ID       | `eliminarEnvioPorId()` ⚠️    | HU-008         |
| 9      | Actualizar envío por pedido | `actualizarEnvioPorPedido()` | HU-011         |
| 10     | Eliminar envío por pedido   | `eliminarEnvioPorPedido()` ✅ | HU-010         |
| 0      | Salir                       | sets `running = false`       | -              |


---

## Documentación Relacionada

- **README.md**: Guía de instalación, configuración y uso
- **CLAUDE.md**: Documentación técnica para desarrollo, arquitectura detallada, patrones de código

---

**Versión**: 1.0
**Total Historias de Usuario**: 11
**Total Reglas de Negocio**: 51
**Arquitectura**: 4 capas (Main → Service → DAO → Models)
