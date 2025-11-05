# 🏗️ MARVIC - Sistema de Gestión de Inventario

**Aplicación Android profesional para gestión de inventario empresarial**

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen)]()
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.0-blue)]()
[![Firebase](https://img.shields.io/badge/firebase-integrated-orange)]()

---

## 📱 Características Principales

- ✅ **Autenticación segura** con Firebase Auth
- ✅ **Sistema de roles** (Almacenero, Jefe de Logística, Gerente)
- ✅ **Escáner QR/Barcode** con ML Kit
- ✅ **17 pantallas** completamente funcionales
- ✅ **Exportación a PDF** de reportes
- ✅ **Galería de imágenes** con Firebase Storage
- ✅ **Analytics e IA** para predicciones
- ✅ **Tests unitarios** (76 tests)

---

## 🚀 Inicio Rápido

### **1. Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/marvic-inventory.git
cd marvic-inventory
```

### **2. Configurar Firebase**
- Descarga `google-services.json` desde Firebase Console
- Coloca el archivo en `app/`

### **3. Compilar y ejecutar**
```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

### **4. Usuarios de prueba**
```
Almacenero:
  - Email: almacenero@marvic.com
  - Password: marvic123

Jefe de Logística:
  - Email: jefe@marvic.com
  - Password: marvic123

Gerente:
  - Email: gerente@marvic.com
  - Password: marvic123
```

---

## 🏗️ Arquitectura

```
MVVM + Repository Pattern
├── UI Layer (Jetpack Compose)
├── ViewModel Layer (Business Logic)
├── Repository Layer (Data Access)
└── Data Layer (Firebase + Local)
```

---

## 🔥 Tecnologías

- **Lenguaje:** Kotlin 100%
- **UI:** Jetpack Compose (Material Design 3)
- **Backend:** Firebase (Auth, Firestore, Storage, Messaging)
- **Cámara:** CameraX + ML Kit Barcode Scanning
- **Navegación:** Navigation Compose
- **Testing:** JUnit + Mockk
- **PDF:** Android PdfDocument API

---

## 📊 Estructura del Proyecto

```
app/src/main/java/com/proyecto/marvic/
├── ui/
│   ├── screens/ (17 pantallas)
│   ├── components/ (5 componentes)
│   └── theme/ (Material Design 3)
├── viewmodel/ (8 ViewModels)
├── data/ (Repositories + Models)
├── utils/ (9 utilidades)
├── camera/ (Escáner QR)
├── ai/ (Motor de IA)
└── notifications/ (Push notifications)
```

---

## 🔒 Sistema de Permisos

| Rol | Nivel | Acceso |
|-----|-------|--------|
| **Almacenero** | 1 | Operaciones básicas de inventario |
| **Jefe de Logística** | 2 | + Gestión de proveedores, proyectos y transferencias |
| **Gerente** | 3 | Acceso completo + Analytics + Gestión de usuarios |

---

## ⚙️ Configuración

**Modo de autenticación** en `AppConfig.kt`:

```kotlin
// Modo Simple (solo Firebase Auth)
REQUIRE_FIRESTORE_USER = false

// Modo Completo (Auth + Firestore validation)
REQUIRE_FIRESTORE_USER = true
```

---

## 🧪 Testing

```bash
# Ejecutar tests unitarios
gradlew.bat test

# 76 tests implementados
✅ InputValidator (31 tests)
✅ CacheManager (12 tests)
✅ PerformanceMonitor (12 tests)
✅ RateLimiter (10 tests)
✅ LazyListOptimizer (11 tests)
```

---

## 📱 Pantallas Principales

1. **Login** - Autenticación con roles
2. **Dashboard** - Vista general con KPIs
3. **Escáner QR** - Lectura de códigos de barras
4. **Inventario** - Gestión de materiales
5. **Movimientos** - Registro de entradas/salidas
6. **Proveedores** - CRUD de proveedores
7. **Proyectos** - Gestión de proyectos
8. **Transferencias** - Transferencias entre almacenes
9. **Reportes** - Exportación a PDF
10. **Analytics** - Estadísticas y predicciones IA
11. **Perfil** - Información del usuario
12. **Galería** - Imágenes de materiales

---

## 📦 Dependencias Principales

```gradle
// Firebase
implementation("com.google.firebase:firebase-auth")
implementation("com.google.firebase:firebase-firestore")
implementation("com.google.firebase:firebase-storage")

// CameraX + ML Kit
implementation("androidx.camera:camera-camera2:1.2.3")
implementation("com.google.mlkit:barcode-scanning:17.1.0")

// Jetpack Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
```

---

## 🎓 Para Tesis/Portfolio

**Puntos destacados:**

1. ✅ Arquitectura MVVM profesional
2. ✅ Integración completa de Firebase (4 servicios)
3. ✅ Sistema de roles multinivel con validación
4. ✅ Escáner QR con Machine Learning (ML Kit)
5. ✅ Exportación de reportes profesionales a PDF
6. ✅ Sistema de seguridad multicapa
7. ✅ 76 tests unitarios automatizados
8. ✅ Optimizaciones de performance (cache, lazy loading)

---

## 📝 Licencia

Proyecto académico - MARVIC © 2025

---

## 📧 Contacto

Para más información sobre el proyecto, contactar a través de la universidad.

---

**Estado del Proyecto:** ✅ Completado y funcional (95%)  
**Última actualización:** Octubre 2025

---

## 🔥 Firebase Firestore - Datos Completos

**Todos los datos de la aplicación se guardan automáticamente en Firebase:**

### ✅ Colecciones Inicializadas Automáticamente:
- **materials** (38 materiales de ejemplo)
- **users** (5 usuarios con diferentes roles)
- **roles** (3 roles: Almacenero, Jefe de Logística, Gerente)
- **providers** (4 proveedores de ejemplo)
- **projects** (3 proyectos de ejemplo)
- **movements** (6 movimientos de ejemplo)
- **transfers** (2 transferencias de ejemplo)
- **project_activities** (actividades de proyectos)

### 📊 Datos Creados desde la App:
Todos los datos creados desde la interfaz se guardan automáticamente en Firebase:
- ✅ Nuevos materiales → `materials`
- ✅ Movimientos de inventario → `movements`
- ✅ Proveedores → `providers`
- ✅ Proyectos → `projects`
- ✅ Transferencias → `transfers`
- ✅ Compras → `purchases`
- ✅ Actividades → `project_activities` y `user_activities`
- ✅ Logs de auditoría → `audit_logs`

**Ver documentación completa:** `FIRESTORE_COLLECTIONS.md`



