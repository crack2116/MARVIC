package com.proyecto.marvic.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.proyecto.marvic.data.User
import java.util.Date

object FirestoreInitializer {
    
    suspend fun initializeIfEmpty(forceReload: Boolean = false) {
        val db = FirebaseFirestore.getInstance()
        
        println("🚀 Iniciando inicialización de colecciones (forceReload=$forceReload)...")
        
        // Inicializar cada colección independientemente
        // Cada una maneja sus propios errores para no bloquear a las demás
        initializeMaterials(db, forceReload)
        initializeUsers(db, forceReload)
        initializeProviders(db, forceReload)
        initializeProjects(db, forceReload)
        initializeMovements(db, forceReload)
        initializeTransfers(db, forceReload)
        
        println("✅ Proceso de inicialización completado")
    }
    
    private suspend fun initializeMaterials(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            val snapshot = db.collection("materials").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando materiales...")
                loadSampleMaterials(db)
            } else {
                println("✅ Materiales ya existen")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando materiales: ${e.message}")
        }
    }
    
    private suspend fun initializeUsers(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            val snapshot = db.collection("users").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando usuarios desde FirestoreInitializer...")
                // NOTA: UserInitializer se ejecuta primero en MainActivity, 
                // así que esta función solo se ejecuta si no hay usuarios
                loadSampleUsers(db)
            } else {
                println("✅ Usuarios ya existen (inicializados por UserInitializer)")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando usuarios: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeProviders(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            println("🔍 Verificando colección 'providers'...")
            val snapshot = db.collection("providers").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando proveedores...")
                loadSampleProviders(db)
                println("✅ Proveedores inicializados correctamente")
            } else {
                println("✅ Proveedores ya existen (${snapshot.size()} documentos encontrados)")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando proveedores: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeProjects(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            println("🔍 Verificando colección 'projects'...")
            val snapshot = db.collection("projects").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando proyectos...")
                loadSampleProjects(db)
                println("✅ Proyectos inicializados correctamente")
            } else {
                println("✅ Proyectos ya existen (${snapshot.size()} documentos encontrados)")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando proyectos: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeMovements(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            println("🔍 Verificando colección 'movements'...")
            val snapshot = db.collection("movements").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando movimientos...")
                loadSampleMovements(db)
                println("✅ Movimientos inicializados correctamente")
            } else {
                println("✅ Movimientos ya existen (${snapshot.size()} documentos encontrados)")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando movimientos: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun initializeTransfers(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            println("🔍 Verificando colección 'transfers'...")
            val snapshot = db.collection("transfers").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando transferencias...")
                loadSampleTransfers(db)
                println("✅ Transferencias inicializadas correctamente")
            } else {
                println("✅ Transferencias ya existen (${snapshot.size()} documentos encontrados)")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando transferencias: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private suspend fun loadSampleMaterials(db: FirebaseFirestore) {
        val currentTimestamp = Timestamp.now()
        
        // Crear materiales con IDs simples numerados
        val materials = listOf(
            mapOf("id" to "MAT001", "nombre" to "Cemento Portland Tipo I", "cantidad" to 250, "ubicacion" to "Almacén 1", "categoria" to "Cementos"),
            mapOf("id" to "MAT002", "nombre" to "Cemento Portland Tipo V", "cantidad" to 180, "ubicacion" to "Almacén 1", "categoria" to "Cementos"),
            mapOf("id" to "MAT003", "nombre" to "Arena Gruesa m³", "cantidad" to 45, "ubicacion" to "Patio Exterior", "categoria" to "Agregados"),
            mapOf("id" to "MAT004", "nombre" to "Arena Fina m³", "cantidad" to 38, "ubicacion" to "Patio Exterior", "categoria" to "Agregados"),
            mapOf("id" to "MAT005", "nombre" to "Piedra Chancada 1/2", "cantidad" to 55, "ubicacion" to "Patio Exterior", "categoria" to "Agregados"),
            mapOf("id" to "MAT006", "nombre" to "Piedra Chancada 3/4", "cantidad" to 60, "ubicacion" to "Patio Exterior", "categoria" to "Agregados"),
            mapOf("id" to "MAT007", "nombre" to "Hormigón m³", "cantidad" to 30, "ubicacion" to "Patio Exterior", "categoria" to "Agregados"),
            
            mapOf("id" to "MAT008", "nombre" to "Fierro Corrugado 6mm", "cantidad" to 150, "ubicacion" to "Almacén 2", "categoria" to "Fierros"),
            mapOf("id" to "MAT009", "nombre" to "Fierro Corrugado 8mm", "cantidad" to 200, "ubicacion" to "Almacén 2", "categoria" to "Fierros"),
            mapOf("id" to "MAT010", "nombre" to "Fierro Corrugado 3/8", "cantidad" to 180, "ubicacion" to "Almacén 2", "categoria" to "Fierros"),
            mapOf("id" to "MAT011", "nombre" to "Fierro Corrugado 1/2", "cantidad" to 220, "ubicacion" to "Almacén 2", "categoria" to "Fierros"),
            mapOf("id" to "MAT012", "nombre" to "Alambre Negro #8", "cantidad" to 25, "ubicacion" to "Almacén 2", "categoria" to "Fierros"),
            mapOf("id" to "MAT013", "nombre" to "Clavos de 2 pulgadas", "cantidad" to 150, "ubicacion" to "Almacén 3", "categoria" to "Ferretería"),
            mapOf("id" to "MAT014", "nombre" to "Clavos de 3 pulgadas", "cantidad" to 140, "ubicacion" to "Almacén 3", "categoria" to "Ferretería"),
            
            mapOf("id" to "MAT015", "nombre" to "Ladrillo King Kong", "cantidad" to 5000, "ubicacion" to "Patio Techado", "categoria" to "Ladrillos"),
            mapOf("id" to "MAT016", "nombre" to "Ladrillo Pandereta", "cantidad" to 3500, "ubicacion" to "Patio Techado", "categoria" to "Ladrillos"),
            mapOf("id" to "MAT017", "nombre" to "Bloques de Concreto 15cm", "cantidad" to 800, "ubicacion" to "Patio Techado", "categoria" to "Ladrillos"),
            
            mapOf("id" to "MAT018", "nombre" to "Tubo PVC 2 pulgadas", "cantidad" to 180, "ubicacion" to "Almacén 4", "categoria" to "Tuberías"),
            mapOf("id" to "MAT019", "nombre" to "Tubo PVC 3 pulgadas", "cantidad" to 150, "ubicacion" to "Almacén 4", "categoria" to "Tuberías"),
            mapOf("id" to "MAT020", "nombre" to "Tubo PVC 4 pulgadas", "cantidad" to 120, "ubicacion" to "Almacén 4", "categoria" to "Tuberías"),
            mapOf("id" to "MAT021", "nombre" to "Codos PVC 2 pulgadas", "cantidad" to 200, "ubicacion" to "Almacén 4", "categoria" to "Accesorios"),
            mapOf("id" to "MAT022", "nombre" to "Pegamento PVC", "cantidad" to 45, "ubicacion" to "Almacén 4", "categoria" to "Accesorios"),
            
            mapOf("id" to "MAT023", "nombre" to "Madera Tornillo 2x3", "cantidad" to 85, "ubicacion" to "Almacén 5", "categoria" to "Maderas"),
            mapOf("id" to "MAT024", "nombre" to "Triplay 6mm", "cantidad" to 35, "ubicacion" to "Almacén 5", "categoria" to "Maderas"),
            
            mapOf("id" to "MAT025", "nombre" to "Pintura Látex Blanco", "cantidad" to 48, "ubicacion" to "Almacén 6", "categoria" to "Pinturas"),
            mapOf("id" to "MAT026", "nombre" to "Barniz Marino", "cantidad" to 22, "ubicacion" to "Almacén 6", "categoria" to "Pinturas"),
            mapOf("id" to "MAT027", "nombre" to "Thinner Acrílico", "cantidad" to 40, "ubicacion" to "Almacén 6", "categoria" to "Pinturas"),
            
            mapOf("id" to "MAT028", "nombre" to "Yeso en Bolsas 25kg", "cantidad" to 120, "ubicacion" to "Almacén 1", "categoria" to "Acabados"),
            mapOf("id" to "MAT029", "nombre" to "Porcelanato 60x60", "cantidad" to 95, "ubicacion" to "Almacén 6", "categoria" to "Acabados"),
            mapOf("id" to "MAT030", "nombre" to "Cerámico 40x40", "cantidad" to 110, "ubicacion" to "Almacén 6", "categoria" to "Acabados"),
            
            mapOf("id" to "MAT031", "nombre" to "Cable Eléctrico 12 AWG", "cantidad" to 25, "ubicacion" to "Almacén 3", "categoria" to "Eléctricos"),
            mapOf("id" to "MAT032", "nombre" to "Interruptores Simples", "cantidad" to 150, "ubicacion" to "Almacén 3", "categoria" to "Eléctricos"),
            mapOf("id" to "MAT033", "nombre" to "Tomacorrientes", "cantidad" to 140, "ubicacion" to "Almacén 3", "categoria" to "Eléctricos"),
            
            mapOf("id" to "MAT034", "nombre" to "Inodoro Tanque Bajo", "cantidad" to 15, "ubicacion" to "Almacén 4", "categoria" to "Sanitarios"),
            mapOf("id" to "MAT035", "nombre" to "Lavatorio", "cantidad" to 12, "ubicacion" to "Almacén 4", "categoria" to "Sanitarios"),
            
            mapOf("id" to "MAT036", "nombre" to "Pico", "cantidad" to 22, "ubicacion" to "Almacén 3", "categoria" to "Herramientas"),
            mapOf("id" to "MAT037", "nombre" to "Pala", "cantidad" to 25, "ubicacion" to "Almacén 3", "categoria" to "Herramientas"),
            mapOf("id" to "MAT038", "nombre" to "Carretilla", "cantidad" to 8, "ubicacion" to "Patio Exterior", "categoria" to "Herramientas")
        )
        
        materials.forEach { material ->
            val docId = material["id"] as String
            val materialData = mapOf(
                "nombre" to material["nombre"],
                "cantidad" to material["cantidad"],
                "ubicacion" to material["ubicacion"],
                "categoria" to material["categoria"],
                "fechaCreacion" to currentTimestamp,
                "fechaActualizacion" to currentTimestamp
            )
            
            try {
                db.collection("materials").document(docId).set(materialData).await()
            } catch (e: Exception) {
                println("❌ Error al crear $docId: ${e.message}")
            }
        }
        
        println("✅ ${materials.size} materiales cargados con IDs limpios (MAT001-MAT038)")
    }
    
    private suspend fun loadSampleUsers(db: FirebaseFirestore) {
        val currentTimestamp = Timestamp.now()
        
        // Solo 3 usuarios: almacenero, jefe, gerente
        val sampleUsers = listOf(
            User(
                email = "almacenero@marvic.com",
                nombre = "José",
                apellido = "Martínez", 
                rol = "almacenero",
                permisos = listOf("movement_create", "movement_view", "inventory_search")
            ),
            User(
                email = "jefe@marvic.com", 
                nombre = "María",
                apellido = "González",
                rol = "jefe_logistica",
                permisos = listOf("movement_create", "movement_view", "inventory_search", "reports_view", "reports_export", "notifications_manage")
            ),
            User(
                email = "gerente@marvic.com",
                nombre = "Carlos",
                apellido = "Rodríguez",
                rol = "gerente",
                permisos = listOf("movement_create", "movement_view", "inventory_search", "reports_view", "reports_export", "users_manage", "settings_configure", "notifications_manage")
            )
        )
        
        sampleUsers.forEach { user ->
            try {
                val userData = user.copy(
                    fechaCreacion = currentTimestamp.toDate().time,
                    ultimoAcceso = currentTimestamp.toDate().time
                )
                db.collection("users").add(userData).await()
            } catch (e: Exception) {
                println("❌ Error al crear usuario ${user.email}: ${e.message}")
            }
        }
        println("✅ ${sampleUsers.size} usuarios de ejemplo creados exitosamente")
    }
    
    private suspend fun loadSampleProviders(db: FirebaseFirestore) {
        try {
            println("🔄 Creando proveedores de ejemplo...")
            val providers = listOf(
                hashMapOf(
                    "nombre" to "Cementos Unidos del Perú S.A.C.",
                    "razonSocial" to "Cementos Unidos del Perú S.A.C.",
                    "ruc" to "20123456789",
                    "direccion" to "Av. Javier Prado Este 4200, San Borja, Lima",
                    "telefono" to "01-6178000",
                    "email" to "ventas@cementosunidos.pe",
                    "contactoPrincipal" to "Roberto Mendoza",
                    "categorias" to listOf("Cementos", "Agregados", "Fierros"),
                    "calificacion" to 4.7,
                    "activo" to true,
                    "notas" to "Proveedor líder en materiales de construcción, entrega rápida y productos de calidad",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 285000.0,
                    "numeroCompras" to 28
                ),
                hashMapOf(
                    "nombre" to "Ferretería Industrial San Martín",
                    "razonSocial" to "Ferretería Industrial San Martín E.I.R.L.",
                    "ruc" to "20456789012",
                    "direccion" to "Av. Argentina 3456, Callao, Lima",
                    "telefono" to "01-4296789",
                    "email" to "contacto@ferreteriasanmartin.pe",
                    "contactoPrincipal" to "Carmen Villanueva",
                    "categorias" to listOf("Ferretería", "Herramientas", "Eléctricos", "Accesorios"),
                    "calificacion" to 4.5,
                    "activo" to true,
                    "notas" to "Especialistas en herramientas industriales y ferretería, amplio catálogo",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 165000.0,
                    "numeroCompras" to 35
                ),
                hashMapOf(
                    "nombre" to "Sanitarios y Cerámicos Premium S.A.",
                    "razonSocial" to "Sanitarios y Cerámicos Premium S.A.",
                    "ruc" to "20345678901",
                    "direccion" to "Av. La Marina 2890, San Miguel, Lima",
                    "telefono" to "01-5678901",
                    "email" to "info@sanitariospremium.pe",
                    "contactoPrincipal" to "Luis Ramírez",
                    "categorias" to listOf("Sanitarios", "Acabados", "Tuberías", "Cerámicos"),
                    "calificacion" to 4.9,
                    "activo" to true,
                    "notas" to "Proveedor premium de sanitarios y acabados de alta calidad, productos importados",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 198000.0,
                    "numeroCompras" to 18
                ),
                hashMapOf(
                    "nombre" to "Maderas del Pacífico S.R.L.",
                    "razonSocial" to "Maderas del Pacífico S.R.L.",
                    "ruc" to "20567890123",
                    "direccion" to "Km 22.5 Carretera Panamericana Norte, Puente Piedra, Lima",
                    "telefono" to "01-5432109",
                    "email" to "ventas@maderaspacifico.pe",
                    "contactoPrincipal" to "Patricia Torres",
                    "categorias" to listOf("Maderas", "Triplay", "Tableros"),
                    "calificacion" to 4.3,
                    "activo" to true,
                    "notas" to "Distribuidor mayorista de maderas, triplay y tableros, precios competitivos",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 125000.0,
                    "numeroCompras" to 15
                )
            )
            
            var createdCount = 0
            providers.forEach { provider ->
                try {
                    db.collection("providers").add(provider).await()
                    createdCount++
                    println("  ✅ Proveedor creado: ${provider["nombre"]}")
                } catch (e: Exception) {
                    println("❌ Error al crear proveedor ${provider["nombre"]}: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            println("✅ $createdCount/${providers.size} proveedores creados exitosamente")
        } catch (e: Exception) {
            println("❌ Error creando proveedores: ${e.message}")
        }
    }
    
    private suspend fun loadSampleProjects(db: FirebaseFirestore) {
        try {
            println("🔄 Creando proyectos de ejemplo...")
            val currentTime = System.currentTimeMillis()
            val day = 24L * 60 * 60 * 1000
            
            val projects = listOf(
                hashMapOf(
                    "codigo" to "PROJ001",
                    "nombre" to "Edificio Residencial San Miguel",
                    "descripcion" to "Construcción de edificio residencial de 5 pisos con 20 departamentos",
                    "cliente" to "Inmobiliaria San Miguel S.A.",
                    "ubicacion" to "Av. Brasil 1500, San Miguel, Lima",
                    "responsable" to "María González",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (45L * day),
                    "fechaFinPrevista" to currentTime + (135L * day),
                    "presupuesto" to 3200000.0,
                    "gastoReal" to 1250000.0,
                    "porcentajeAvance" to 38,
                    "prioridad" to "ALTA",
                    "notas" to "Proyecto en ejecución, estructura en progreso, materiales según plan",
                    "fechaCreacion" to currentTime - (45L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ002",
                    "nombre" to "Centro Comercial Plaza Norte - Ampliación",
                    "descripcion" to "Ampliación del centro comercial con 15 locales adicionales",
                    "cliente" to "Grupo Retail Perú",
                    "ubicacion" to "Av. Túpac Amaru 5797, Independencia, Lima",
                    "responsable" to "Carlos Rodríguez",
                    "estado" to "PLANIFICACION",
                    "fechaInicio" to currentTime + (20L * day),
                    "fechaFinPrevista" to currentTime + (380L * day),
                    "presupuesto" to 6500000.0,
                    "gastoReal" to 0.0,
                    "porcentajeAvance" to 0,
                    "prioridad" to "MEDIA",
                    "notas" to "Proyecto en fase de planificación y permisos",
                    "fechaCreacion" to currentTime - (15L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ003",
                    "nombre" to "Casa Familiar La Molina",
                    "descripcion" to "Construcción de casa unifamiliar de 2 pisos, 4 habitaciones",
                    "cliente" to "Familia Pérez García",
                    "ubicacion" to "Calle Los Pinos 245, La Molina, Lima",
                    "responsable" to "José Martínez",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (75L * day),
                    "fechaFinPrevista" to currentTime + (75L * day),
                    "presupuesto" to 580000.0,
                    "gastoReal" to 365000.0,
                    "porcentajeAvance" to 63,
                    "prioridad" to "ALTA",
                    "notas" to "Excelente avance, acabados en progreso, materiales de calidad",
                    "fechaCreacion" to currentTime - (75L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ004",
                    "nombre" to "Oficinas Corporativas Miraflores",
                    "descripcion" to "Edificio de oficinas de 8 pisos en zona empresarial",
                    "cliente" to "Corporación Inversiones del Sur",
                    "ubicacion" to "Av. Larco 1200, Miraflores, Lima",
                    "responsable" to "María González",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (120L * day),
                    "fechaFinPrevista" to currentTime + (180L * day),
                    "presupuesto" to 8500000.0,
                    "gastoReal" to 4200000.0,
                    "porcentajeAvance" to 49,
                    "prioridad" to "URGENTE",
                    "notas" to "Proyecto prioritario, cumplimiento de cronograma ajustado",
                    "fechaCreacion" to currentTime - (120L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ005",
                    "nombre" to "Condominio Residencial Los Olivos",
                    "descripcion" to "Complejo de 12 casas en condominio cerrado",
                    "cliente" to "Inmobiliaria Norte S.A.C.",
                    "ubicacion" to "Av. Universitaria 7800, Los Olivos, Lima",
                    "responsable" to "Carlos Rodríguez",
                    "estado" to "FINALIZADO",
                    "fechaInicio" to currentTime - (450L * day),
                    "fechaFinPrevista" to currentTime - (30L * day),
                    "fechaFinReal" to currentTime - (25L * day),
                    "presupuesto" to 4200000.0,
                    "gastoReal" to 3980000.0,
                    "porcentajeAvance" to 100,
                    "prioridad" to "MEDIA",
                    "notas" to "Proyecto finalizado exitosamente, dentro del presupuesto",
                    "fechaCreacion" to currentTime - (450L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ006",
                    "nombre" to "Restaurante La Casona Barranco",
                    "descripcion" to "Remodelación completa de restaurante histórico",
                    "cliente" to "Restaurantes del Perú S.A.",
                    "ubicacion" to "Jr. San Martín 340, Barranco, Lima",
                    "responsable" to "José Martínez",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (25L * day),
                    "fechaFinPrevista" to currentTime + (65L * day),
                    "presupuesto" to 320000.0,
                    "gastoReal" to 125000.0,
                    "porcentajeAvance" to 39,
                    "prioridad" to "ALTA",
                    "notas" to "Remodelación en curso, respetando arquitectura original",
                    "fechaCreacion" to currentTime - (25L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ007",
                    "nombre" to "Hospital Clínica San Juan",
                    "descripcion" to "Construcción de pabellón de emergencias y consultorios",
                    "cliente" to "Clínica San Juan S.A.",
                    "ubicacion" to "Av. Javier Prado Este 4200, San Borja, Lima",
                    "responsable" to "María González",
                    "estado" to "PLANIFICACION",
                    "fechaInicio" to currentTime + (60L * day),
                    "fechaFinPrevista" to currentTime + (450L * day),
                    "presupuesto" to 12500000.0,
                    "gastoReal" to 0.0,
                    "porcentajeAvance" to 0,
                    "prioridad" to "URGENTE",
                    "notas" to "Proyecto en fase de licitación y aprobación de planos",
                    "fechaCreacion" to currentTime - (30L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ008",
                    "nombre" to "Colegio Privado San Patricio",
                    "descripcion" to "Ampliación con 10 aulas nuevas y laboratorio",
                    "cliente" to "Colegio San Patricio",
                    "ubicacion" to "Av. Del Ejército 1800, San Isidro, Lima",
                    "responsable" to "Carlos Rodríguez",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (90L * day),
                    "fechaFinPrevista" to currentTime + (90L * day),
                    "presupuesto" to 1800000.0,
                    "gastoReal" to 950000.0,
                    "porcentajeAvance" to 53,
                    "prioridad" to "ALTA",
                    "notas" to "Construcción en proceso, aulas casi terminadas",
                    "fechaCreacion" to currentTime - (90L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ009",
                    "nombre" to "Casa de Playa Punta Hermosa",
                    "descripcion" to "Construcción de casa de playa de 3 pisos con vista al mar",
                    "cliente" to "Familia Ramírez",
                    "ubicacion" to "Calle Las Palmeras 120, Punta Hermosa, Lima",
                    "responsable" to "José Martínez",
                    "estado" to "PAUSADO",
                    "fechaInicio" to currentTime - (180L * day),
                    "fechaFinPrevista" to currentTime + (60L * day),
                    "presupuesto" to 750000.0,
                    "gastoReal" to 320000.0,
                    "porcentajeAvance" to 42,
                    "prioridad" to "BAJA",
                    "notas" to "Proyecto pausado por solicitud del cliente, reanudación pendiente",
                    "fechaCreacion" to currentTime - (180L * day),
                    "fechaActualizacion" to currentTime - (30L * day)
                ),
                hashMapOf(
                    "codigo" to "PROJ010",
                    "nombre" to "Almacén Industrial Lurín",
                    "descripcion" to "Construcción de almacén de 2000 m² para distribución",
                    "cliente" to "Logística del Sur S.A.C.",
                    "ubicacion" to "Km 28 Carretera Panamericana Sur, Lurín, Lima",
                    "responsable" to "María González",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (60L * day),
                    "fechaFinPrevista" to currentTime + (120L * day),
                    "presupuesto" to 2800000.0,
                    "gastoReal" to 1150000.0,
                    "porcentajeAvance" to 41,
                    "prioridad" to "ALTA",
                    "notas" to "Estructura metálica en montaje, techado en progreso",
                    "fechaCreacion" to currentTime - (60L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ011",
                    "nombre" to "Hotel Boutique San Isidro",
                    "descripcion" to "Remodelación completa de hotel de 4 estrellas, 45 habitaciones",
                    "cliente" to "Hoteles Premium Perú S.A.",
                    "ubicacion" to "Av. Las Begonias 475, San Isidro, Lima",
                    "responsable" to "Carlos Rodríguez",
                    "estado" to "PLANIFICACION",
                    "fechaInicio" to currentTime + (45L * day),
                    "fechaFinPrevista" to currentTime + (300L * day),
                    "presupuesto" to 5200000.0,
                    "gastoReal" to 0.0,
                    "porcentajeAvance" to 0,
                    "prioridad" to "MEDIA",
                    "notas" to "Proyecto en diseño y aprobación de planos arquitectónicos",
                    "fechaCreacion" to currentTime - (20L * day),
                    "fechaActualizacion" to currentTime
                ),
                hashMapOf(
                    "codigo" to "PROJ012",
                    "nombre" to "Edificio de Departamentos Surco",
                    "descripcion" to "Edificio de 6 pisos con 18 departamentos, 2 por piso",
                    "cliente" to "Inmobiliaria Surco S.A.",
                    "ubicacion" to "Av. Angamos Este 1450, Surco, Lima",
                    "responsable" to "José Martínez",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to currentTime - (150L * day),
                    "fechaFinPrevista" to currentTime + (90L * day),
                    "presupuesto" to 4800000.0,
                    "gastoReal" to 3100000.0,
                    "porcentajeAvance" to 65,
                    "prioridad" to "ALTA",
                    "notas" to "Obra en avanzado estado, instalaciones en progreso",
                    "fechaCreacion" to currentTime - (150L * day),
                    "fechaActualizacion" to currentTime
                )
            )
            
            var createdCount = 0
            projects.forEach { project ->
                try {
                    val docRef = db.collection("projects").document()
                    docRef.set(project).await()
                    createdCount++
                    println("  ✅ Proyecto creado: ${project["nombre"]}")
                    
                    // Crear actividad inicial
                    db.collection("project_activities").add(
                        hashMapOf(
                            "projectId" to docRef.id,
                            "tipo" to "INICIO",
                            "descripcion" to "Proyecto creado",
                            "userId" to "SYSTEM",
                            "userName" to "Sistema",
                            "timestamp" to System.currentTimeMillis()
                        )
                    ).await()
                } catch (e: Exception) {
                    println("❌ Error al crear proyecto ${project["nombre"]}: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            println("✅ $createdCount/${projects.size} proyectos creados exitosamente")
        } catch (e: Exception) {
            println("❌ Error creando proyectos: ${e.message}")
        }
    }
    
    private suspend fun loadSampleMovements(db: FirebaseFirestore) {
        try {
            println("🔄 Creando movimientos de ejemplo...")
            val currentTime = System.currentTimeMillis()
            val day = 24L * 60 * 60 * 1000
            val hour = 60L * 60 * 1000
            
            // Crear 25 movimientos variados (entradas y salidas)
            val movements = listOf(
                hashMapOf("materialId" to "MAT001", "delta" to 50, "timestamp" to currentTime - (5L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT001", "delta" to -20, "timestamp" to currentTime - (3L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT002", "delta" to 80, "timestamp" to currentTime - (7L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT003", "delta" to -15, "timestamp" to currentTime - (4L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT004", "delta" to 25, "timestamp" to currentTime - (6L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT005", "delta" to -10, "timestamp" to currentTime - (2L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT006", "delta" to 60, "timestamp" to currentTime - (8L * day), "userId" to "gerente@marvic.com"),
                hashMapOf("materialId" to "MAT007", "delta" to -8, "timestamp" to currentTime - (1L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT008", "delta" to 100, "timestamp" to currentTime - (10L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT008", "delta" to -30, "timestamp" to currentTime - (3L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT009", "delta" to 120, "timestamp" to currentTime - (12L * day), "userId" to "gerente@marvic.com"),
                hashMapOf("materialId" to "MAT010", "delta" to -25, "timestamp" to currentTime - (5L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT011", "delta" to 90, "timestamp" to currentTime - (9L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT012", "delta" to -12, "timestamp" to currentTime - (2L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT013", "delta" to 40, "timestamp" to currentTime - (6L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT014", "delta" to -18, "timestamp" to currentTime - (4L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT015", "delta" to -500, "timestamp" to currentTime - (2L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT015", "delta" to 800, "timestamp" to currentTime - (15L * day), "userId" to "gerente@marvic.com"),
                hashMapOf("materialId" to "MAT016", "delta" to -300, "timestamp" to currentTime - (3L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT017", "delta" to 200, "timestamp" to currentTime - (11L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT018", "delta" to -45, "timestamp" to currentTime - (1L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT019", "delta" to 70, "timestamp" to currentTime - (8L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT020", "delta" to -35, "timestamp" to currentTime - (4L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT025", "delta" to 30, "timestamp" to currentTime - (1L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT025", "delta" to -10, "timestamp" to currentTime - (12L * hour), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT026", "delta" to 20, "timestamp" to currentTime - (5L * day), "userId" to "jefe@marvic.com"),
                hashMapOf("materialId" to "MAT027", "delta" to -15, "timestamp" to currentTime - (2L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT028", "delta" to 55, "timestamp" to currentTime - (7L * day), "userId" to "gerente@marvic.com"),
                hashMapOf("materialId" to "MAT029", "delta" to -22, "timestamp" to currentTime - (3L * day), "userId" to "almacenero@marvic.com"),
                hashMapOf("materialId" to "MAT030", "delta" to 35, "timestamp" to currentTime - (6L * day), "userId" to "jefe@marvic.com")
            )
            
            var createdCount = 0
            movements.forEach { movement ->
                try {
                    db.collection("movements").add(movement).await()
                    createdCount++
                } catch (e: Exception) {
                    println("❌ Error al crear movimiento: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            println("✅ $createdCount/${movements.size} movimientos creados exitosamente")
        } catch (e: Exception) {
            println("❌ Error creando movimientos: ${e.message}")
        }
    }
    
    private suspend fun loadSampleTransfers(db: FirebaseFirestore) {
        try {
            println("🔄 Creando transferencias de ejemplo...")
            val currentTime = Timestamp.now().toDate().time
            val day = 24L * 60 * 60 * 1000
            val almacenes = listOf("Almacén 1", "Almacén 2", "Almacén 3", "Almacén 4", "Almacén 5", "Almacén 6", "Patio Exterior", "Patio Techado")
            val estados = listOf("COMPLETADA", "EN_TRANSITO", "PENDIENTE")
            val responsables = listOf("María González", "José Martínez", "Carlos Rodríguez")
            val autorizadores = listOf("Carlos Rodríguez", "María González", "")
            
            val materials = listOf(
                "MAT001" to "Cemento Portland Tipo I",
                "MAT002" to "Cemento Portland Tipo V",
                "MAT003" to "Arena Gruesa m³",
                "MAT004" to "Arena Fina m³",
                "MAT008" to "Fierro Corrugado 6mm",
                "MAT009" to "Fierro Corrugado 8mm",
                "MAT015" to "Ladrillo King Kong",
                "MAT016" to "Ladrillo Pandereta",
                "MAT018" to "Tubo PVC 2 pulgadas",
                "MAT019" to "Tubo PVC 3 pulgadas",
                "MAT025" to "Pintura Látex Blanco",
                "MAT028" to "Yeso en Bolsas 25kg",
                "MAT031" to "Cable Eléctrico 12 AWG",
                "MAT032" to "Interruptores Simples"
            )
            
            // Crear 25 transferencias variadas
            val transfers = mutableListOf<HashMap<String, Any>>()
            for (i in 0 until 25) {
                val material = materials[i % materials.size]
                val estado = estados[i % estados.size]
                val responsable = responsables[i % responsables.size]
                val autorizadoPor = if (estado == "COMPLETADA") autorizadores[i % autorizadores.size] else ""
                val origen = almacenes[i % almacenes.size]
                val destino = almacenes[(i + 3) % almacenes.size]
                
                val fechaSolicitud = currentTime - ((25 - i).toLong() * day)
                val transfer = hashMapOf<String, Any>(
                    "materialId" to material.first,
                    "materialNombre" to material.second,
                    "cantidad" to (20 + (i * 5) % 200),
                    "origenAlmacen" to origen,
                    "destinoAlmacen" to destino,
                    "responsable" to responsable,
                    "motivo" to when (i % 5) {
                        0 -> "Distribución de stock"
                        1 -> "Reubicación por obras"
                        2 -> "Abastecimiento de almacén"
                        3 -> "Optimización de espacio"
                        else -> "Transferencia programada"
                    },
                    "estado" to estado,
                    "fechaSolicitud" to fechaSolicitud,
                    "notas" to when (estado) {
                        "COMPLETADA" -> "Transferencia completada exitosamente"
                        "EN_TRANSITO" -> "Material en tránsito, llegada programada"
                        else -> "Esperando autorización"
                    }
                )
                
                if (estado == "COMPLETADA") {
                    transfer["fechaTransferencia"] = fechaSolicitud + (1L * day)
                    transfer["fechaRecepcion"] = fechaSolicitud + (2L * day)
                    transfer["autorizadoPor"] = autorizadoPor
                } else if (estado == "EN_TRANSITO") {
                    transfer["fechaTransferencia"] = fechaSolicitud + (1L * day)
                    transfer["autorizadoPor"] = autorizadoPor
                } else {
                    transfer["autorizadoPor"] = ""
                }
                
                transfers.add(transfer)
            }
            
            var createdCount = 0
            transfers.forEach { transfer ->
                try {
                    db.collection("transfers").add(transfer).await()
                    createdCount++
                    println("  ✅ Transferencia creada: ${transfer["materialNombre"]} - ${transfer["estado"]}")
                } catch (e: Exception) {
                    println("❌ Error al crear transferencia: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            println("✅ $createdCount/${transfers.size} transferencias creadas exitosamente")
        } catch (e: Exception) {
            println("❌ Error creando transferencias: ${e.message}")
        }
    }
}