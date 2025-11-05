package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.AuthRepository
import com.proyecto.marvic.data.FirebaseAuthRepository
import com.proyecto.marvic.data.UserRepository
import com.proyecto.marvic.data.FirestoreUserRepository
import com.proyecto.marvic.data.User
import com.proyecto.marvic.data.UserSession
import com.proyecto.marvic.data.AppConfig
import com.proyecto.marvic.data.demo.DemoConfig
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repo: AuthRepository = FirebaseAuthRepository(),
    private val userRepo: UserRepository = FirestoreUserRepository()
) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var role by mutableStateOf<String?>(null)

    fun signIn(email: String, password: String, selectedRole: String, onDone: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        
        // Validar que se haya seleccionado un rol
        if (selectedRole == "Seleccione su rol" || selectedRole.isBlank()) {
            errorMessage = "Por favor, seleccione su rol"
            isLoading = false
            onDone(false)
            return
        }
        
        viewModelScope.launch {
            try {
                // 1. Autenticar con Firebase Auth
                val authResult = if (DemoConfig.useDemo) {
                    Result.success(Unit)
                } else {
                    repo.signIn(email, password)
                }

                if (authResult.isFailure) {
                    isLoading = false
                    errorMessage = authResult.exceptionOrNull()?.localizedMessage ?: "Error de autenticación"
                    onDone(false)
                    return@launch
                }

                // MODO SIMPLE: Solo Firebase Auth (sin validación de Firestore)
                if (!AppConfig.REQUIRE_FIRESTORE_USER) {
                    println("✅ Modo simple: Solo Firebase Auth")
                    // Normalizar rol seleccionado
                    val normalizedRole = when (selectedRole) {
                        "Almacenero" -> "almacenero"
                        "Jefe de Logística" -> "jefe_logistica"
                        "Gerente" -> "gerente"
                        else -> "almacenero"
                    }
                    
                    role = normalizedRole
                    UserSession.setRole(normalizedRole)
                    
                    isLoading = false
                    onDone(true)
                    return@launch
                }

                // MODO COMPLETO: Validar contra Firestore
                // 2. Obtener el rol REAL del usuario desde Firestore
                var userResult = userRepo.getUserByEmail(email)
                var user = userResult.getOrNull()

                // Si el usuario NO existe en Firestore, crearlo automáticamente
                if (user == null) {
                    println("⚠️ Usuario no existe en Firestore, creando automáticamente...")
                    
                    // Usar el rol que el usuario SELECCIONÓ (ya normalizado)
                    val normalizedSelectedRole = when (selectedRole) {
                        "Almacenero" -> "almacenero"
                        "Jefe de Logística" -> "jefe_logistica"
                        "Gerente" -> "gerente"
                        else -> "almacenero"
                    }
                    
                    val autoRol = normalizedSelectedRole
                    
                    val newUser = User(
                        email = email,
                        nombre = email.split("@").first().replaceFirstChar { it.uppercase() },
                        apellido = "Usuario",
                        rol = autoRol,
                        activo = true,
                        fechaCreacion = System.currentTimeMillis(),
                        ultimoAcceso = System.currentTimeMillis(),
                        permisos = when(autoRol) {
                            "gerente" -> listOf("registrar_movimientos", "consultar_inventario", "escanear_qr", "ver_reportes", "busqueda_avanzada", "gestionar_proveedores", "gestionar_proyectos", "gestionar_usuarios", "ver_analytics", "exportar_pdf", "configurar_sistema")
                            "jefe_logistica" -> listOf("registrar_movimientos", "consultar_inventario", "escanear_qr", "ver_reportes", "busqueda_avanzada", "gestionar_proveedores", "gestionar_proyectos")
                            else -> listOf("registrar_movimientos", "consultar_inventario", "escanear_qr")
                        }
                    )
                    
                    val createResult = userRepo.createUser(newUser)
                    if (createResult.isSuccess) {
                        user = createResult.getOrNull()
                        println("✅ Usuario creado en Firestore: ${user?.email}")
                    } else {
                        isLoading = false
                        errorMessage = "Error al crear usuario en el sistema."
                        repo.signOut()
                        onDone(false)
                        return@launch
                    }
                }

                if (user == null) {
                    isLoading = false
                    errorMessage = "Error inesperado al procesar usuario."
                    repo.signOut()
                    onDone(false)
                    return@launch
                }

                // 3. Verificar que el usuario esté activo
                if (!user.activo) {
                    isLoading = false
                    errorMessage = "Usuario desactivado. Contacte al administrador."
                    repo.signOut()
                    onDone(false)
                    return@launch
                }

                // 4. VALIDAR que el rol seleccionado coincida con el rol real
                val normalizedSelectedRole = when (selectedRole) {
                    "Almacenero" -> "almacenero"
                    "Jefe de Logística" -> "jefe_logistica"
                    "Gerente" -> "gerente"
                    else -> selectedRole.lowercase()
                }

                if (user.rol != normalizedSelectedRole) {
                    isLoading = false
                    errorMessage = "Rol incorrecto. Usted no tiene permisos como '$selectedRole'."
                    repo.signOut()
                    onDone(false)
                    return@launch
                }

                // 5. Establecer el rol validado
                role = user.rol
                UserSession.setRole(user.rol)

                isLoading = false
                onDone(true)

            } catch (e: Exception) {
                isLoading = false
                errorMessage = "Error: ${e.message}"
                onDone(false)
            }
        }
    }

    fun loadRole() {
        viewModelScope.launch { role = if (DemoConfig.useDemo) "admin" else repo.getRole() }
    }
}


