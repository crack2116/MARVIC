# 🔧 SOLUCIÓN: Datos no aparecen en Firebase

## 🎯 Problema

Si las colecciones ya existen en Firebase (aunque estén vacías), el código **NO las reinicializa** por seguridad.

## ✅ SOLUCIÓN RÁPIDA

### Paso 1: Limpiar Firebase

1. Abre **Firebase Console**: https://console.firebase.google.com
2. Selecciona tu proyecto
3. Ve a **Firestore Database**
4. **Elimina estas colecciones** (haz clic derecho → Delete collection):
   - `users`
   - `providers`
   - `projects`
   - `movements`
   - `transfers`
   - `roles` (opcional)
   - `materials` (opcional, solo si quieres reinicializar)

### Paso 2: Ejecutar la App

1. **Ejecuta la app** en tu dispositivo/emulador
2. **Espera unos segundos** (la inicialización es en segundo plano)
3. **Verifica en Firebase Console** que aparezcan los datos

---

## 🔄 SOLUCIÓN ALTERNATIVA: Forzar Reinicialización

Si no quieres eliminar manualmente, puedes forzar la reinicialización:

1. Abre `MainActivity.kt` línea 93
2. Cambia:
   ```kotlin
   val forceReload = false
   ```
   a:
   ```kotlin
   val forceReload = true  // ⚠️ SOLO PARA DESARROLLO
   ```
3. **Ejecuta la app**
4. **Vuelve a cambiar** a `false` después

⚠️ **ADVERTENCIA**: Esto SOBRESCRIBE todos los datos existentes.

---

## 📊 Datos que se Crearán

- ✅ **3 usuarios** (almacenero, jefe, gerente)
- ✅ **4 proveedores** (con nombres mejorados)
- ✅ **12 proyectos** (con diferentes estados)
- ✅ **25 movimientos** (entradas y salidas)
- ✅ **25 transferencias** (con diferentes estados)

---

## 📱 ¿Dónde Ver los Datos en la App?

- **Usuarios**: Pantalla de Gestión de Usuarios (solo Gerente)
- **Proveedores**: Pantalla de Proveedores
- **Proyectos**: Pantalla de Proyectos
- **Movimientos**: Pantalla de Movimientos
- **Transferencias**: Pantalla de Transferencias

**"Mi Perfil"** solo muestra el usuario actual, NO todos los datos.

