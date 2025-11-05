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
import kotlinx.coroutines.launch

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
                
                // MODO SIMPLE: Crear usuario temporal sin Firestore
                if (!AppConfig.REQUIRE_FIRESTORE_USER) {
                    println("✅ Modo simple: Perfil sin Firestore")
                    currentUser = User(
                        id = UserSession.userId,
                        email = email,
                        nombre = email.split("@").firstOrNull()?.replaceFirstChar { it.uppercase() } ?: "Usuario",
                        apellido = "MARVIC",
                        rol = UserSession.currentRole,
                        activo = true,
                        fechaCreacion = System.currentTimeMillis(),
                        ultimoAcceso = System.currentTimeMillis(),
                        permisos = when(UserSession.currentRole) {
                            "gerente" -> listOf("Acceso completo al sistema")
                            "jefe_logistica" -> listOf("Gestión de inventario y reportes")
                            else -> listOf("Operaciones básicas de almacén")
                        }
                    )
                    isLoading = false
                    return@launch
                }
                
                // MODO COMPLETO: Cargar desde Firestore
                val result = userRepo.getUserByEmail(email)
                
                if (result.isSuccess) {
                    currentUser = result.getOrNull()
                    if (currentUser == null) {
                        errorMessage = "Usuario no encontrado"
                    }
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al cargar perfil"
                }
                
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

