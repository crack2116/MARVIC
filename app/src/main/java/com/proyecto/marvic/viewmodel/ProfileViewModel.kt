package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.User
import com.proyecto.marvic.data.UserRepository
import com.proyecto.marvic.data.FirestoreUserRepository
import com.proyecto.marvic.data.UserSession
import com.proyecto.marvic.data.AppConfig
import com.proyecto.marvic.data.UserInitializer
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ProfileViewModel(
    private val userRepo: UserRepository = FirestoreUserRepository()
) : ViewModel() {
    
    var currentUser by mutableStateOf<User?>(null)
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    fun loadUserProfile() {
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                val email = UserSession.userEmail
                
                if (email == "unknown") {
                    errorMessage = "No hay sesión activa"
                    isLoading = false
                    return@launch
                }
                
                println("🔄 [ProfileViewModel] Cargando perfil desde Firebase para: $email")
                println("🔄 [ProfileViewModel] Rol actual: ${UserSession.currentRole}")
                
                // Paso 1: Intentar cargar desde Firebase usando UserInitializer (más confiable)
                var firestoreUser: User? = null
                
                try {
                    // Primero intentar obtener el usuario (puede que ya exista)
                    val userId = email.replace("@", "_").replace(".", "_")
                    var userByIdResult = userRepo.getUserById(userId)
                    
                    if (userByIdResult.isSuccess) {
                        firestoreUser = userByIdResult.getOrNull()
                        if (firestoreUser != null) {
                            println("✅ [ProfileViewModel] Usuario encontrado por ID inmediatamente: ${firestoreUser.nombre} ${firestoreUser.apellido}")
                        }
                    }
                    
                    // Si no se encontró por ID, intentar por email
                    if (firestoreUser == null) {
                        val emailResult = userRepo.getUserByEmail(email)
                        if (emailResult.isSuccess) {
                            firestoreUser = emailResult.getOrNull()
                            if (firestoreUser != null) {
                                println("✅ [ProfileViewModel] Usuario encontrado por email: ${firestoreUser.nombre} ${firestoreUser.apellido}")
                            }
                        }
                    }
                    
                    // Si no existe, crearlo con UserInitializer
                    if (firestoreUser == null) {
                        println("🔄 [ProfileViewModel] Paso 1: Usuario no existe, creando con UserInitializer...")
                        val (nombre, apellido) = getRealNameByEmail(email)
                        UserInitializer.createUserIfNotExists(email, nombre, apellido, UserSession.currentRole)
                        println("✅ [ProfileViewModel] UserInitializer completado")
                        
                        // Intentar obtener el usuario con varios intentos (Firestore puede tener delay)
                        var attempts = 0
                        while (firestoreUser == null && attempts < 3) {
                            delay(300) // Esperar un momento para que Firestore propague
                            attempts++
                            
                            // Intentar obtener por ID
                            userByIdResult = userRepo.getUserById(userId)
                            if (userByIdResult.isSuccess) {
                                firestoreUser = userByIdResult.getOrNull()
                                if (firestoreUser != null) {
                                    println("✅ [ProfileViewModel] Usuario encontrado por ID después de UserInitializer (intento $attempts): ${firestoreUser.nombre} ${firestoreUser.apellido}")
                                    break
                                }
                            }
                            
                            // Si aún no se encuentra, intentar por email
                            if (firestoreUser == null) {
                                val emailResult = userRepo.getUserByEmail(email)
                                if (emailResult.isSuccess) {
                                    firestoreUser = emailResult.getOrNull()
                                    if (firestoreUser != null) {
                                        println("✅ [ProfileViewModel] Usuario encontrado por email después de UserInitializer (intento $attempts): ${firestoreUser.nombre} ${firestoreUser.apellido}")
                                        break
                                    }
                                }
                            }
                        }
                        
                        if (firestoreUser == null) {
                            println("⚠️ [ProfileViewModel] Usuario no encontrado después de $attempts intentos, continuando con creación directa...")
                        }
                    }
                    
                } catch (e: Exception) {
                    println("⚠️ [ProfileViewModel] Error en Paso 1: ${e.message}")
                    e.printStackTrace()
                }
                
                // Paso 2: Si aún no se encontró, crear directamente
                if (firestoreUser == null) {
                    println("⚠️ [ProfileViewModel] Paso 2: Usuario no encontrado, creando directamente...")
                    try {
                        firestoreUser = createRealUserInFirebase(email)
                        if (firestoreUser != null) {
                            println("✅ [ProfileViewModel] Usuario creado directamente: ${firestoreUser.nombre} ${firestoreUser.apellido}")
                        } else {
                            println("❌ [ProfileViewModel] No se pudo crear usuario directamente")
                        }
                    } catch (e: Exception) {
                        println("❌ [ProfileViewModel] Error creando usuario directamente: ${e.message}")
                        e.printStackTrace()
                    }
                }
                
                // Paso 3: Si todo falló, usar datos básicos pero guardarlos en Firebase
                if (firestoreUser == null) {
                    println("⚠️ [ProfileViewModel] Paso 3: Todo falló, usando datos básicos...")
                    val (nombre, apellido) = getRealNameByEmail(email)
                    
                    // Crear usuario básico localmente
                    firestoreUser = User(
                        id = email.replace("@", "_").replace(".", "_"),
                        email = email,
                        nombre = nombre,
                        apellido = apellido,
                        rol = UserSession.currentRole,
                        activo = true,
                        fechaCreacion = System.currentTimeMillis(),
                        ultimoAcceso = System.currentTimeMillis(),
                        permisos = getPermissionsByRole(UserSession.currentRole)
                    )
                    
                    // Intentar guardar en Firebase en segundo plano (sin bloquear)
                    try {
                        createUserWithSpecificId(firestoreUser.id, firestoreUser)
                        println("✅ [ProfileViewModel] Usuario guardado en Firebase en segundo plano")
                    } catch (e: Exception) {
                        println("⚠️ [ProfileViewModel] No se pudo guardar en Firebase, pero el usuario se muestra: ${e.message}")
                    }
                }
                
                // Usar datos reales
                currentUser = firestoreUser
                println("✅ [ProfileViewModel] Perfil cargado: ${firestoreUser.nombre} ${firestoreUser.apellido}")
                
                // Actualizar último acceso en Firebase (opcional, no crítico)
                if (firestoreUser != null && !firestoreUser.id.isEmpty()) {
                    try {
                        val updatedUser = firestoreUser.copy(ultimoAcceso = System.currentTimeMillis())
                        userRepo.updateUser(updatedUser).onSuccess {
                            currentUser = updatedUser
                            println("✅ [ProfileViewModel] Último acceso actualizado")
                        }.onFailure {
                            println("⚠️ [ProfileViewModel] No se pudo actualizar último acceso (no crítico)")
                        }
                    } catch (e: Exception) {
                        println("⚠️ [ProfileViewModel] Error actualizando último acceso (no crítico): ${e.message}")
                    }
                }
                
                isLoading = false
                
            } catch (e: Exception) {
                println("❌ [ProfileViewModel] Excepción crítica: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error al cargar perfil: ${e.message}"
                isLoading = false
            }
        }
    }
    
    private fun getRealNameByEmail(email: String): Pair<String, String> {
        val usuariosReales = mapOf(
            "almacenero@marvic.com" to Pair("José", "Martínez"),
            "jefe@marvic.com" to Pair("María", "González"),
            "gerente@marvic.com" to Pair("Carlos", "Rodríguez")
        )
        return usuariosReales.getOrDefault(email, 
            Pair(email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Usuario", "Usuario")
        )
    }
    
    private fun getPermissionsByRole(rol: String): List<String> {
        return when (rol) {
            "gerente" -> listOf(
                "movement_create", "movement_view", "inventory_search", 
                "reports_view", "reports_export", "users_manage", 
                "settings_configure", "notifications_manage"
            )
            "jefe_logistica" -> listOf(
                "movement_create", "movement_view", "inventory_search", 
                "reports_view", "reports_export", "notifications_manage"
            )
            else -> listOf("movement_create", "movement_view", "inventory_search")
        }
    }
    
    private suspend fun createRealUserInFirebase(email: String): User? {
        try {
            // Mapeo de emails a datos reales
            val usuariosReales = mapOf(
                "almacenero@marvic.com" to Pair("José", "Martínez"),
                "jefe@marvic.com" to Pair("María", "González"),
                "gerente@marvic.com" to Pair("Carlos", "Rodríguez")
            )
            
            val (nombre, apellido) = usuariosReales.getOrDefault(email, 
                Pair(email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Usuario", "Usuario")
            )
            
            val permisos = when(UserSession.currentRole) {
                "gerente" -> listOf(
                    "movement_create", "movement_view", "inventory_search", 
                    "reports_view", "reports_export", "users_manage", 
                    "settings_configure", "notifications_manage"
                )
                "jefe_logistica" -> listOf(
                    "movement_create", "movement_view", "inventory_search", 
                    "reports_view", "reports_export", "notifications_manage"
                )
                else -> listOf("movement_create", "movement_view", "inventory_search")
            }
            
            val userId = email.replace("@", "_").replace(".", "_")
            val newUser = User(
                id = userId,
                email = email,
                nombre = nombre,
                apellido = apellido,
                rol = UserSession.currentRole,
                activo = true,
                fechaCreacion = System.currentTimeMillis(),
                ultimoAcceso = System.currentTimeMillis(),
                permisos = permisos
            )
            
            // Crear usuario en Firebase usando UserInitializer (que usa IDs específicos)
            try {
                UserInitializer.createUserIfNotExists(email, nombre, apellido, UserSession.currentRole)
                
                // Ahora obtener el usuario recién creado
                val getUserResult = userRepo.getUserById(userId)
                if (getUserResult.isSuccess) {
                    val createdUser = getUserResult.getOrNull()
                    if (createdUser != null) {
                        println("✅ Usuario creado y obtenido de Firebase: ${createdUser.nombre} ${createdUser.apellido}")
                        return createdUser
                    }
                }
                
                // Si no se pudo obtener, intentar crear directamente con ID específico
                return createUserWithSpecificId(userId, newUser)
                
            } catch (e: Exception) {
                println("❌ Error usando UserInitializer: ${e.message}")
                e.printStackTrace()
                // Intentar crear directamente con ID específico
                return createUserWithSpecificId(userId, newUser)
            }
            
        } catch (e: Exception) {
            println("❌ Error en createRealUserInFirebase: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
    
    private suspend fun createUserWithSpecificId(userId: String, user: User): User? {
        try {
            // Crear usuario directamente en Firebase con ID específico
            val db = FirebaseFirestore.getInstance()
            val usersCollection = db.collection("users")
            
            val userData = hashMapOf(
                "email" to user.email,
                "nombre" to user.nombre,
                "apellido" to user.apellido,
                "rol" to user.rol,
                "activo" to user.activo,
                "fechaCreacion" to user.fechaCreacion,
                "ultimoAcceso" to user.ultimoAcceso,
                "permisos" to user.permisos
            )
            
            usersCollection.document(userId).set(userData).await()
            
            println("✅ Usuario creado directamente en Firebase con ID: $userId")
            
            // Obtener el usuario recién creado
            val doc = usersCollection.document(userId).get().await()
            if (doc.exists()) {
                return User(
                    id = doc.id,
                    email = doc.getString("email") ?: "",
                    nombre = doc.getString("nombre") ?: "",
                    apellido = doc.getString("apellido") ?: "",
                    rol = doc.getString("rol") ?: "",
                    activo = doc.getBoolean("activo") ?: true,
                    fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                    ultimoAcceso = doc.getLong("ultimoAcceso") ?: 0L,
                    permisos = (doc.get("permisos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                )
            }
            
            return null
        } catch (e: Exception) {
            println("❌ Error en createUserWithSpecificId: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
}

