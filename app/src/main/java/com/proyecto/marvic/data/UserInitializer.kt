package com.proyecto.marvic.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UserInitializer {
    
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    
    suspend fun initializeDefaultUsers() {
        try {
            println("🔄 Verificando usuarios en Firestore...")
            
            // Crear usuarios por defecto con nombres correctos
            val defaultUsers = listOf(
                // Almacenero - José Martínez
                hashMapOf(
                    "nombre" to "José",
                    "apellido" to "Martínez",
                    "email" to "almacenero@marvic.com",
                    "rol" to "almacenero",
                    "activo" to true,
                    "permisos" to listOf(
                        "registrar_movimientos",
                        "consultar_inventario",
                        "escanear_qr"
                    ),
                    "fechaCreacion" to System.currentTimeMillis(),
                    "ultimoAcceso" to System.currentTimeMillis()
                ),
                // Jefe de Logística - María González
                hashMapOf(
                    "nombre" to "María",
                    "apellido" to "González",
                    "email" to "jefe@marvic.com",
                    "rol" to "jefe_logistica",
                    "activo" to true,
                    "permisos" to listOf(
                        "registrar_movimientos",
                        "consultar_inventario",
                        "escanear_qr",
                        "ver_reportes",
                        "busqueda_avanzada",
                        "gestionar_proveedores",
                        "gestionar_proyectos"
                    ),
                    "fechaCreacion" to System.currentTimeMillis(),
                    "ultimoAcceso" to System.currentTimeMillis()
                ),
                // Gerente - Carlos Rodríguez
                hashMapOf(
                    "nombre" to "Carlos",
                    "apellido" to "Rodríguez",
                    "email" to "gerente@marvic.com",
                    "rol" to "gerente",
                    "activo" to true,
                    "permisos" to listOf(
                        "registrar_movimientos",
                        "consultar_inventario",
                        "escanear_qr",
                        "ver_reportes",
                        "busqueda_avanzada",
                        "gestionar_proveedores",
                        "gestionar_proyectos",
                        "gestionar_usuarios",
                        "ver_analytics",
                        "exportar_pdf",
                        "configurar_sistema"
                    ),
                    "fechaCreacion" to System.currentTimeMillis(),
                    "ultimoAcceso" to System.currentTimeMillis()
                )
            )
            
            // Verificar si ya existen usuarios
            val existing = usersCollection.limit(1).get().await()
            if (!existing.isEmpty) {
                println("✅ Usuarios ya existen en Firestore - Verificando si necesitan actualización...")
                // Verificar y crear/actualizar usuarios específicos
                for (userData in defaultUsers) {
                    val email = userData["email"] as String
                    val userId = email.replace("@", "_").replace(".", "_")
                    val doc = usersCollection.document(userId).get().await()
                    
                    if (!doc.exists()) {
                        usersCollection.document(userId).set(userData).await()
                        println("✅ Usuario creado/actualizado: $email - ${userData["nombre"]} ${userData["apellido"]}")
                    } else {
                        // Actualizar datos si existen pero no coinciden
                        val existingNombre = doc.getString("nombre") ?: ""
                        val existingApellido = doc.getString("apellido") ?: ""
                        val expectedNombre = userData["nombre"] as String
                        val expectedApellido = userData["apellido"] as String
                        
                        if (existingNombre != expectedNombre || existingApellido != expectedApellido) {
                            usersCollection.document(userId).set(userData).await()
                            println("✅ Usuario actualizado: $email - $expectedNombre $expectedApellido")
                        } else {
                            println("ℹ️ Usuario ya existe correcto: $email")
                        }
                    }
                }
                return
            }
            
            println("🔄 Inicializando usuarios en Firestore...")
            
            // Insertar usuarios en Firestore
            for (userData in defaultUsers) {
                val email = userData["email"] as String
                // Usar un ID único basado en el email (sin @ y .)
                val userId = email.replace("@", "_").replace(".", "_")
                
                usersCollection.document(userId).set(userData).await()
                println("✅ Usuario creado: $email - ${userData["nombre"]} ${userData["apellido"]}")
            }
            
            println("✅ Todos los usuarios inicializados correctamente (${defaultUsers.size} usuarios)")
            
        } catch (e: Exception) {
            println("❌ Error inicializando usuarios: ${e.message}")
            e.printStackTrace()
        }
    }
    
    suspend fun createUserIfNotExists(email: String, nombre: String, apellido: String, rol: String) {
        try {
            val userId = email.replace("@", "_").replace(".", "_")
            val doc = usersCollection.document(userId).get().await()
            
            if (!doc.exists()) {
                val userData = hashMapOf(
                    "nombre" to nombre,
                    "apellido" to apellido,
                    "email" to email,
                    "rol" to rol,
                    "activo" to true,
                    "permisos" to getPermissionsByRole(rol),
                    "fechaCreacion" to System.currentTimeMillis(),
                    "ultimoAcceso" to System.currentTimeMillis()
                )
                
                usersCollection.document(userId).set(userData).await()
                println("✅ Usuario creado en Firestore: $email")
            } else {
                println("ℹ️ Usuario ya existe en Firestore: $email")
            }
        } catch (e: Exception) {
            println("❌ Error creando usuario en Firestore: ${e.message}")
        }
    }
    
    private fun getPermissionsByRole(rol: String): List<String> {
        return when (rol) {
            "almacenero" -> listOf(
                "registrar_movimientos",
                "consultar_inventario",
                "escanear_qr"
            )
            "jefe_logistica" -> listOf(
                "registrar_movimientos",
                "consultar_inventario",
                "escanear_qr",
                "ver_reportes",
                "busqueda_avanzada",
                "gestionar_proveedores",
                "gestionar_proyectos"
            )
            "gerente" -> listOf(
                "registrar_movimientos",
                "consultar_inventario",
                "escanear_qr",
                "ver_reportes",
                "busqueda_avanzada",
                "gestionar_proveedores",
                "gestionar_proyectos",
                "gestionar_usuarios",
                "ver_analytics",
                "exportar_pdf",
                "configurar_sistema"
            )
            else -> emptyList()
        }
    }
}



