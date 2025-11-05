package com.proyecto.marvic.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.proyecto.marvic.data.User
import java.util.Date

object FirestoreInitializer {
    
    suspend fun initializeIfEmpty(forceReload: Boolean = false) {
        val db = FirebaseFirestore.getInstance()
        
        try {
            // Inicializar cada colección independientemente
            initializeMaterials(db, forceReload)
            initializeUsers(db, forceReload)
            initializeProviders(db, forceReload)
            initializeProjects(db, forceReload)
            initializeMovements(db, forceReload)
            initializeTransfers(db, forceReload)
        } catch (e: Exception) {
            e.printStackTrace()
            println("❌ Error en inicialización: ${e.message}")
        }
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
                println("🔄 Inicializando usuarios...")
                loadSampleUsers(db)
            } else {
                println("✅ Usuarios ya existen")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando usuarios: ${e.message}")
        }
    }
    
    private suspend fun initializeProviders(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            val snapshot = db.collection("providers").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando proveedores...")
                loadSampleProviders(db)
            } else {
                println("✅ Proveedores ya existen")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando proveedores: ${e.message}")
        }
    }
    
    private suspend fun initializeProjects(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            val snapshot = db.collection("projects").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando proyectos...")
                loadSampleProjects(db)
            } else {
                println("✅ Proyectos ya existen")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando proyectos: ${e.message}")
        }
    }
    
    private suspend fun initializeMovements(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            val snapshot = db.collection("movements").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando movimientos...")
                loadSampleMovements(db)
            } else {
                println("✅ Movimientos ya existen")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando movimientos: ${e.message}")
        }
    }
    
    private suspend fun initializeTransfers(db: FirebaseFirestore, forceReload: Boolean) {
        try {
            val snapshot = db.collection("transfers").limit(1).get().await()
            if (snapshot.isEmpty || forceReload) {
                println("🔄 Inicializando transferencias...")
                loadSampleTransfers(db)
            } else {
                println("✅ Transferencias ya existen")
            }
        } catch (e: Exception) {
            println("❌ Error inicializando transferencias: ${e.message}")
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
        
        val sampleUsers = listOf(
            User(
                email = "gerente@marvic.com",
                nombre = "Carlos",
                apellido = "Rodríguez",
                rol = "gerente",
                permisos = listOf("movement_create", "movement_view", "inventory_search", "reports_view", "reports_export", "users_manage", "settings_configure", "notifications_manage")
            ),
            User(
                email = "jefe@marvic.com", 
                nombre = "María",
                apellido = "González",
                rol = "jefe_logistica",
                permisos = listOf("movement_create", "movement_view", "inventory_search", "reports_view", "reports_export", "notifications_manage")
            ),
            User(
                email = "almacenero@marvic.com",
                nombre = "José",
                apellido = "Martínez", 
                rol = "almacenero",
                permisos = listOf("movement_create", "movement_view", "inventory_search")
            ),
            User(
                email = "supervisor@marvic.com",
                nombre = "Ana",
                apellido = "López",
                rol = "supervisor",
                permisos = listOf("movement_view", "inventory_search", "reports_view")
            ),
            User(
                email = "auditor@marvic.com",
                nombre = "Luis",
                apellido = "Fernández",
                rol = "auditor", 
                permisos = listOf("inventory_search", "reports_view", "reports_export")
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
                    "nombre" to "Construcciones ABC S.A.C.",
                    "razonSocial" to "Construcciones ABC S.A.C.",
                    "ruc" to "20123456789",
                    "direccion" to "Av. Los Constructores 123, Lima",
                    "telefono" to "987654321",
                    "email" to "contacto@construccionesabc.com",
                    "contactoPrincipal" to "Juan Pérez",
                    "categorias" to listOf("Cementos", "Agregados", "Fierros"),
                    "calificacion" to 4.5,
                    "activo" to true,
                    "notas" to "Proveedor principal de materiales de construcción",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 125000.0,
                    "numeroCompras" to 15
                ),
                hashMapOf(
                    "nombre" to "Ferretería El Constructor",
                    "razonSocial" to "Ferretería El Constructor E.I.R.L.",
                    "ruc" to "20456789012",
                    "direccion" to "Jr. Las Herramientas 456, Lima",
                    "telefono" to "987654322",
                    "email" to "ventas@ferreteria.com",
                    "contactoPrincipal" to "María García",
                    "categorias" to listOf("Ferretería", "Herramientas", "Eléctricos"),
                    "calificacion" to 4.2,
                    "activo" to true,
                    "notas" to "Especialista en herramientas y ferretería",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 85000.0,
                    "numeroCompras" to 22
                ),
                hashMapOf(
                    "nombre" to "Sanitarios y Acabados S.A.",
                    "razonSocial" to "Sanitarios y Acabados S.A.",
                    "ruc" to "20345678901",
                    "direccion" to "Av. Los Baños 789, Lima",
                    "telefono" to "987654323",
                    "email" to "info@sanitarios.com",
                    "contactoPrincipal" to "Carlos López",
                    "categorias" to listOf("Sanitarios", "Acabados", "Tuberías"),
                    "calificacion" to 4.8,
                    "activo" to true,
                    "notas" to "Proveedor de alta calidad en sanitarios",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 95000.0,
                    "numeroCompras" to 12
                ),
                hashMapOf(
                    "nombre" to "Maderas del Norte",
                    "razonSocial" to "Maderas del Norte S.R.L.",
                    "ruc" to "20567890123",
                    "direccion" to "Km 15 Carretera Norte, Lima",
                    "telefono" to "987654324",
                    "email" to "ventas@maderasnorte.com",
                    "contactoPrincipal" to "Ana Martínez",
                    "categorias" to listOf("Maderas", "Triplay"),
                    "calificacion" to 4.0,
                    "activo" to true,
                    "notas" to "Distribuidor de maderas y triplay",
                    "fechaCreacion" to System.currentTimeMillis(),
                    "fechaActualizacion" to System.currentTimeMillis(),
                    "totalCompras" to 65000.0,
                    "numeroCompras" to 8
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
            val projects = listOf(
                hashMapOf(
                    "codigo" to "PROJ001",
                    "nombre" to "Edificio Residencial San Miguel",
                    "descripcion" to "Construcción de edificio residencial de 5 pisos",
                    "cliente" to "Inmobiliaria XYZ",
                    "ubicacion" to "San Miguel, Lima",
                    "responsable" to "María González",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000), // Hace 30 días
                    "fechaFinPrevista" to System.currentTimeMillis() + (180L * 24 * 60 * 60 * 1000), // En 180 días
                    "presupuesto" to 2500000.0,
                    "gastoReal" to 850000.0,
                    "porcentajeAvance" to 35,
                    "prioridad" to "ALTA",
                    "notas" to "Proyecto en ejecución, todo según cronograma",
                    "fechaCreacion" to System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000),
                    "fechaActualizacion" to System.currentTimeMillis()
                ),
                hashMapOf(
                    "codigo" to "PROJ002",
                    "nombre" to "Centro Comercial Plaza Norte",
                    "descripcion" to "Ampliación del centro comercial",
                    "cliente" to "Grupo Retail",
                    "ubicacion" to "Independencia, Lima",
                    "responsable" to "Carlos Rodríguez",
                    "estado" to "PLANIFICACION",
                    "fechaInicio" to System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000), // En 30 días
                    "fechaFinPrevista" to System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000), // En 1 año
                    "presupuesto" to 5000000.0,
                    "gastoReal" to 0.0,
                    "porcentajeAvance" to 0,
                    "prioridad" to "MEDIA",
                    "notas" to "Proyecto en fase de planificación",
                    "fechaCreacion" to System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000),
                    "fechaActualizacion" to System.currentTimeMillis()
                ),
                hashMapOf(
                    "codigo" to "PROJ003",
                    "nombre" to "Casa Familiar La Molina",
                    "descripcion" to "Construcción de casa unifamiliar",
                    "cliente" to "Familia Pérez",
                    "ubicacion" to "La Molina, Lima",
                    "responsable" to "José Martínez",
                    "estado" to "EN_CURSO",
                    "fechaInicio" to System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000), // Hace 60 días
                    "fechaFinPrevista" to System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000), // En 90 días
                    "presupuesto" to 450000.0,
                    "gastoReal" to 280000.0,
                    "porcentajeAvance" to 62,
                    "prioridad" to "ALTA",
                    "notas" to "Buen avance, materiales según plan",
                    "fechaCreacion" to System.currentTimeMillis() - (60L * 24 * 60 * 60 * 1000),
                    "fechaActualizacion" to System.currentTimeMillis()
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
            // Crear algunos movimientos de ejemplo (entradas y salidas)
            val movements = listOf(
                hashMapOf(
                    "materialId" to "MAT001",
                    "delta" to 50, // Entrada
                    "timestamp" to System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000), // Hace 5 días
                    "userId" to "almacenero@marvic.com"
                ),
                hashMapOf(
                    "materialId" to "MAT001",
                    "delta" to -20, // Salida
                    "timestamp" to System.currentTimeMillis() - (3L * 24 * 60 * 60 * 1000), // Hace 3 días
                    "userId" to "almacenero@marvic.com"
                ),
                hashMapOf(
                    "materialId" to "MAT008",
                    "delta" to 100, // Entrada
                    "timestamp" to System.currentTimeMillis() - (7L * 24 * 60 * 60 * 1000), // Hace 7 días
                    "userId" to "jefe@marvic.com"
                ),
                hashMapOf(
                    "materialId" to "MAT015",
                    "delta" to -500, // Salida
                    "timestamp" to System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000), // Hace 2 días
                    "userId" to "almacenero@marvic.com"
                ),
                hashMapOf(
                    "materialId" to "MAT025",
                    "delta" to 30, // Entrada
                    "timestamp" to System.currentTimeMillis() - (1L * 24 * 60 * 60 * 1000), // Ayer
                    "userId" to "jefe@marvic.com"
                ),
                hashMapOf(
                    "materialId" to "MAT025",
                    "delta" to -10, // Salida
                    "timestamp" to System.currentTimeMillis() - (12L * 60 * 60 * 1000), // Hace 12 horas
                    "userId" to "almacenero@marvic.com"
                )
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
            val transfers = listOf(
                hashMapOf(
                    "materialId" to "MAT001",
                    "materialNombre" to "Cemento Portland Tipo I",
                    "cantidad" to 30,
                    "origenAlmacen" to "Almacén 1",
                    "destinoAlmacen" to "Almacén 2",
                    "responsable" to "María González",
                    "motivo" to "Distribución de stock",
                    "estado" to "COMPLETADA",
                    "fechaSolicitud" to Timestamp.now().toDate().time - (10L * 24 * 60 * 60 * 1000),
                    "fechaTransferencia" to Timestamp.now().toDate().time - (9L * 24 * 60 * 60 * 1000),
                    "fechaRecepcion" to Timestamp.now().toDate().time - (8L * 24 * 60 * 60 * 1000),
                    "notas" to "Transferencia completada exitosamente",
                    "autorizadoPor" to "Carlos Rodríguez"
                ),
                hashMapOf(
                    "materialId" to "MAT015",
                    "materialNombre" to "Ladrillo King Kong",
                    "cantidad" to 200,
                    "origenAlmacen" to "Patio Techado",
                    "destinoAlmacen" to "Almacén 1",
                    "responsable" to "José Martínez",
                    "motivo" to "Reubicación por obras",
                    "estado" to "PENDIENTE",
                    "fechaSolicitud" to Timestamp.now().toDate().time - (2L * 24 * 60 * 60 * 1000),
                    "notas" to "Esperando autorización",
                    "autorizadoPor" to ""
                )
            )
            
            var createdCount = 0
            transfers.forEach { transfer ->
                try {
                    db.collection("transfers").add(transfer).await()
                    createdCount++
                    println("  ✅ Transferencia creada: ${transfer["materialNombre"]}")
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