# 🔥 Colecciones de Firebase Firestore

Este documento lista todas las colecciones de Firebase que se crean y utilizan en la aplicación MARVIC.

## 📦 Colecciones Principales

### 1. **materials** (Materiales)
- **Descripción**: Inventario de materiales de construcción
- **Inicialización**: 38 materiales de ejemplo (MAT001-MAT038)
- **Campos principales**:
  - `id`: ID único del material
  - `nombre`: Nombre del material
  - `cantidad`: Cantidad en stock
  - `ubicacion`: Ubicación física
  - `categoria`: Categoría del material
  - `fechaCreacion`: Fecha de creación
  - `fechaActualizacion`: Última actualización

### 2. **movements** (Movimientos)
- **Descripción**: Registro de entradas y salidas de inventario
- **Inicialización**: 6 movimientos de ejemplo
- **Campos principales**:
  - `materialId`: ID del material
  - `delta`: Cantidad (positivo = entrada, negativo = salida)
  - `timestamp`: Fecha y hora del movimiento
  - `userId`: Usuario que realizó el movimiento

### 3. **users** (Usuarios)
- **Descripción**: Usuarios del sistema con sus roles y permisos
- **Inicialización**: 5 usuarios de ejemplo
- **Campos principales**:
  - `email`: Email del usuario
  - `nombre`: Nombre del usuario
  - `apellido`: Apellido del usuario
  - `rol`: Rol asignado (almacenero, jefe_logistica, gerente)
  - `permisos`: Lista de permisos
  - `activo`: Estado del usuario
  - `fechaCreacion`: Fecha de creación
  - `ultimoAcceso`: Último acceso

### 4. **roles** (Roles)
- **Descripción**: Configuración de roles del sistema
- **Inicialización**: 3 roles por defecto
- **Roles**:
  - `almacenero` (Nivel 1)
  - `jefe_logistica` (Nivel 2)
  - `gerente` (Nivel 3)
- **Campos principales**:
  - `nombre`: Nombre técnico del rol
  - `displayName`: Nombre para mostrar
  - `nivel`: Nivel de acceso (1-3)
  - `descripcion`: Descripción del rol
  - `permisos`: Lista de permisos del rol

### 5. **providers** (Proveedores)
- **Descripción**: Proveedores de materiales
- **Inicialización**: 4 proveedores de ejemplo
- **Campos principales**:
  - `nombre`: Nombre del proveedor
  - `razonSocial`: Razón social
  - `ruc`: RUC del proveedor
  - `direccion`: Dirección
  - `telefono`: Teléfono de contacto
  - `email`: Email de contacto
  - `contactoPrincipal`: Nombre del contacto
  - `categorias`: Categorías de productos
  - `calificacion`: Calificación (0.0-5.0)
  - `activo`: Estado del proveedor
  - `totalCompras`: Total de compras realizadas
  - `numeroCompras`: Número de compras

### 6. **projects** (Proyectos)
- **Descripción**: Proyectos de construcción
- **Inicialización**: 3 proyectos de ejemplo
- **Campos principales**:
  - `codigo`: Código del proyecto
  - `nombre`: Nombre del proyecto
  - `descripcion`: Descripción
  - `cliente`: Cliente del proyecto
  - `ubicacion`: Ubicación del proyecto
  - `responsable`: Responsable del proyecto
  - `estado`: Estado (PLANIFICACION, EN_CURSO, PAUSADO, FINALIZADO, CANCELADO)
  - `presupuesto`: Presupuesto total
  - `gastoReal`: Gasto real acumulado
  - `porcentajeAvance`: Porcentaje de avance (0-100)
  - `prioridad`: Prioridad (BAJA, MEDIA, ALTA, URGENTE)

### 7. **project_materials** (Materiales de Proyectos)
- **Descripción**: Materiales asignados a proyectos
- **Campos principales**:
  - `projectId`: ID del proyecto
  - `materialId`: ID del material
  - `materialNombre`: Nombre del material
  - `cantidadPlanificada`: Cantidad planificada
  - `cantidadUsada`: Cantidad usada
  - `precioUnitarioEstimado`: Precio estimado
  - `costoTotal`: Costo total

### 8. **project_activities** (Actividades de Proyectos)
- **Descripción**: Historial de actividades de proyectos
- **Inicialización**: 1 actividad por proyecto creado
- **Campos principales**:
  - `projectId`: ID del proyecto
  - `tipo`: Tipo de actividad (INICIO, ASIGNACION_MATERIAL, USO_MATERIAL, NOTA, CAMBIO_ESTADO)
  - `descripcion`: Descripción de la actividad
  - `userId`: ID del usuario
  - `userName`: Nombre del usuario
  - `timestamp`: Fecha y hora

### 9. **transfers** (Transferencias)
- **Descripción**: Transferencias entre almacenes
- **Inicialización**: 2 transferencias de ejemplo
- **Campos principales**:
  - `materialId`: ID del material
  - `materialNombre`: Nombre del material
  - `cantidad`: Cantidad a transferir
  - `origenAlmacen`: Almacén origen
  - `destinoAlmacen`: Almacén destino
  - `responsable`: Responsable de la transferencia
  - `motivo`: Motivo de la transferencia
  - `estado`: Estado (PENDIENTE, EN_TRANSITO, COMPLETADA, CANCELADA)
  - `fechaSolicitud`: Fecha de solicitud
  - `fechaTransferencia`: Fecha de transferencia
  - `fechaRecepcion`: Fecha de recepción
  - `autorizadoPor`: Usuario que autorizó

### 10. **purchases** (Compras)
- **Descripción**: Órdenes de compra a proveedores
- **Campos principales**:
  - `providerId`: ID del proveedor
  - `numeroOrden`: Número de orden
  - `fecha`: Fecha de compra
  - `items`: Lista de items
  - `subtotal`: Subtotal
  - `igv`: IGV
  - `total`: Total
  - `estado`: Estado (PENDIENTE, RECIBIDO, CANCELADO)
  - `documentoReferencia`: Número de factura/guía
  - `recibidoPor`: Usuario que recibió
  - `fechaRecepcion`: Fecha de recepción

### 11. **audit_logs** (Logs de Auditoría)
- **Descripción**: Registro de auditoría del sistema
- **Campos principales**:
  - `eventType`: Tipo de evento (LOGIN, LOGOUT, CREATE, UPDATE, DELETE, etc.)
  - `module`: Módulo (INVENTORY, MOVEMENTS, PROVIDERS, PROJECTS, etc.)
  - `description`: Descripción del evento
  - `severity`: Nivel de severidad (INFO, WARNING, ERROR, CRITICAL)
  - `userId`: ID del usuario
  - `timestamp`: Fecha y hora
  - `metadata`: Metadatos adicionales

### 12. **user_activities** (Actividades de Usuarios)
- **Descripción**: Actividades realizadas por usuarios
- **Campos principales**:
  - `userId`: ID del usuario
  - `accion`: Acción realizada
  - `descripcion`: Descripción
  - `materialId`: ID del material (si aplica)
  - `cantidad`: Cantidad (si aplica)
  - `timestamp`: Fecha y hora
  - `ipAddress`: Dirección IP
  - `deviceInfo`: Información del dispositivo

---

## 🚀 Inicialización Automática

Cuando la app se inicia por primera vez, se inicializan automáticamente:

- ✅ **38 materiales** (MAT001-MAT038)
- ✅ **5 usuarios** (almacenero, jefe, gerente, supervisor, auditor)
- ✅ **3 roles** (almacenero, jefe_logistica, gerente)
- ✅ **4 proveedores** (diferentes categorías)
- ✅ **3 proyectos** (en diferentes estados)
- ✅ **6 movimientos** (entradas y salidas)
- ✅ **2 transferencias** (completada y pendiente)
- ✅ **3 actividades de proyectos** (una por proyecto)

---

## 📊 Resumen de Datos

| Colección | Documentos Iniciales | Total Campos |
|-----------|---------------------|-------------|
| materials | 38 | 7 |
| movements | 6 | 4 |
| users | 5 | 8 |
| roles | 3 | 6 |
| providers | 4 | 14 |
| projects | 3 | 13 |
| project_materials | 0* | 8 |
| project_activities | 3 | 7 |
| transfers | 2 | 11 |
| purchases | 0* | 11 |
| audit_logs | 0* | 7 |
| user_activities | 0* | 8 |

*Se crean dinámicamente cuando se usan desde la app

---

## 🔄 Sincronización

**Todos los datos creados desde la app se guardan automáticamente en Firebase:**

- ✅ Nuevos materiales → `materials`
- ✅ Movimientos de inventario → `movements`
- ✅ Nuevos proveedores → `providers`
- ✅ Nuevos proyectos → `projects`
- ✅ Transferencias → `transfers`
- ✅ Compras → `purchases`
- ✅ Actividades → `project_activities` y `user_activities`
- ✅ Logs de auditoría → `audit_logs`

---

## 📝 Notas Importantes

1. **Inicialización única**: Los datos de ejemplo solo se crean si las colecciones están vacías
2. **IDs consistentes**: Los materiales usan IDs fijos (MAT001, MAT002, etc.) para facilitar pruebas
3. **Timestamps**: Todos los documentos incluyen `fechaCreacion` y `fechaActualizacion`
4. **Transacciones**: Las operaciones críticas usan transacciones de Firestore para garantizar consistencia
5. **Validación**: Todos los repositorios validan datos antes de guardar

---

**Última actualización**: $(date)

