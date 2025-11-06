# 🔥 Instrucciones para Reinicializar Datos en Firebase

## ⚠️ Problema Identificado

Si las colecciones ya existen en Firebase (incluso con datos antiguos), el código **NO los sobrescribirá automáticamente** por seguridad. Esto significa que:

- Si ya tienes `materials` con datos, no se crearán los nuevos
- Si ya tienes `users`, `providers`, `projects`, etc., no se actualizarán

## ✅ Solución: Dos Opciones

### **Opción 1: Limpiar Firebase Manualmente (Recomendado)**

1. **Abre Firebase Console**: https://console.firebase.google.com
2. **Selecciona tu proyecto**
3. **Ve a Firestore Database**
4. **Elimina las colecciones que quieres reinicializar**:
   - `users` (eliminar todos los documentos)
   - `providers` (eliminar todos los documentos)
   - `projects` (eliminar todos los documentos)
   - `movements` (eliminar todos los documentos)
   - `transfers` (eliminar todos los documentos)
   - `roles` (eliminar todos los documentos)
   - `materials` (opcional, si quieres reinicializar)

5. **Ejecuta la app nuevamente** - Los datos se crearán automáticamente

### **Opción 2: Forzar Reinicialización en el Código (Solo Desarrollo)**

1. **Abre** `app/src/main/java/com/proyecto/marvic/MainActivity.kt`
2. **Busca la línea 88**:
   ```kotlin
   val forceReload = false // ⚠️ Cambiar a true para forzar reinicialización completa
   ```
3. **Cambia a**:
   ```kotlin
   val forceReload = true // ⚠️ SOLO PARA DESARROLLO - Esto SOBRESCRIBE todos los datos
   ```
4. **Compila y ejecuta la app**
5. **Vuelve a cambiar a** `false` después de la reinicialización

⚠️ **ADVERTENCIA**: `forceReload = true` **SOBRESCRIBE** todos los datos existentes. Solo úsalo durante desarrollo.

---

## 📊 Datos que se Crearán

Después de reinicializar, tendrás:

- ✅ **3 usuarios** (almacenero, jefe, gerente)
- ✅ **3 roles** (almacenero, jefe_logistica, gerente)
- ✅ **4 proveedores** (con nombres mejorados)
- ✅ **12 proyectos** (con diferentes estados)
- ✅ **25 movimientos** (entradas y salidas variadas)
- ✅ **25 transferencias** (con diferentes estados)
- ✅ **38 materiales** (MAT001-MAT038)

---

## 🔍 Verificar en Firebase Console

1. Ve a **Firestore Database** en Firebase Console
2. Verifica que aparezcan estas colecciones:
   - `users` (3 documentos)
   - `roles` (3 documentos)
   - `providers` (4 documentos)
   - `projects` (12 documentos)
   - `movements` (25 documentos)
   - `transfers` (25 documentos)
   - `materials` (38 documentos)

---

## 📱 Nota sobre la Pantalla "Mi Perfil"

La pantalla **"Mi Perfil"** solo muestra los datos del **usuario actualmente logueado** (almacenero@marvic.com). 

Para ver:
- **Todos los usuarios**: Ve a la pantalla de gestión de usuarios (solo Gerente)
- **Proveedores**: Ve a la pantalla de Proveedores
- **Proyectos**: Ve a la pantalla de Proyectos
- **Movimientos**: Ve a la pantalla de Movimientos
- **Transferencias**: Ve a la pantalla de Transferencias

---

## 🐛 Si Aún No Funciona

1. **Verifica los logs de Android Studio**:
   - Busca mensajes como: `🔄 Inicializando...`
   - Busca errores: `❌ Error...`

2. **Verifica la conexión a Firebase**:
   - Asegúrate de que `google-services.json` esté en `app/`
   - Verifica que Firebase esté configurado correctamente

3. **Limpia el proyecto**:
   ```bash
   ./gradlew clean
   ./gradlew assembleDebug
   ```

4. **Reinstala la app** completamente en el dispositivo/emulador

---

**Última actualización**: Noviembre 2025

