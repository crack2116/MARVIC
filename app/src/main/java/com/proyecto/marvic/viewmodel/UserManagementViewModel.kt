package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.FirestoreUserRepository
import com.proyecto.marvic.data.User
import com.proyecto.marvic.data.UserActivity
import com.proyecto.marvic.data.UserRepository
import kotlinx.coroutines.launch

class UserManagementViewModel(
    private val userRepo: UserRepository = FirestoreUserRepository()
) : ViewModel() {
    
    var users by mutableStateOf<List<User>>(emptyList())
        private set
    
    var activities by mutableStateOf<List<UserActivity>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var selectedUser by mutableStateOf<User?>(null)
        private set
    
    // Roles disponibles
    val availableRoles = listOf(
        "almacenero" to "Almacenero",
        "jefe_logistica" to "Jefe de Logística", 
        "gerente" to "Gerente",
        "supervisor" to "Supervisor",
        "auditor" to "Auditor",
        "operario" to "Operario"
    )
    
    // Permisos disponibles
    val availablePermissions = listOf(
        "movement_create" to "Crear Movimientos",
        "movement_view" to "Ver Movimientos",
        "inventory_search" to "Buscar Inventario",
        "reports_view" to "Ver Reportes",
        "reports_export" to "Exportar Reportes",
        "users_manage" to "Gestionar Usuarios",
        "settings_configure" to "Configurar Sistema",
        "notifications_manage" to "Gestionar Notificaciones"
    )
    
    init {
        loadUsers()
        loadActivities()
    }
    
    fun loadUsers() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = userRepo.getAllUsers()
                if (result.isSuccess) {
                    users = result.getOrDefault(emptyList())
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }
    
    fun loadActivities() {
        viewModelScope.launch {
            try {
                val result = userRepo.getAllActivities(100)
                if (result.isSuccess) {
                    activities = result.getOrDefault(emptyList())
                }
            } catch (e: Exception) {
                // Silently handle error for activities
            }
        }
    }
    
    fun createUser(
        email: String,
        nombre: String,
        apellido: String,
        rol: String,
        permisos: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val newUser = User(
                    email = email,
                    nombre = nombre,
                    apellido = apellido,
                    rol = rol,
                    permisos = permisos
                )
                
                val result = userRepo.createUser(newUser)
                if (result.isSuccess) {
                    loadUsers()
                    onSuccess()
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    onError(errorMessage ?: "Error desconocido")
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onError(errorMessage ?: "Error desconocido")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun updateUser(
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = userRepo.updateUser(user)
                if (result.isSuccess) {
                    loadUsers()
                    onSuccess()
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    onError(errorMessage ?: "Error desconocido")
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onError(errorMessage ?: "Error desconocido")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun deleteUser(
        userId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val result = userRepo.deleteUser(userId)
                if (result.isSuccess) {
                    loadUsers()
                    onSuccess()
                } else {
                    errorMessage = result.exceptionOrNull()?.message
                    onError(errorMessage ?: "Error desconocido")
                }
            } catch (e: Exception) {
                errorMessage = e.message
                onError(errorMessage ?: "Error desconocido")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun selectUser(user: User) {
        selectedUser = user
    }
    
    fun clearSelectedUser() {
        selectedUser = null
    }
    
    fun getUserById(userId: String): User? {
        return users.find { it.id == userId }
    }
    
    fun getUserActivities(userId: String): List<UserActivity> {
        return activities.filter { it.userId == userId }
    }
    
    fun logActivity(
        userId: String,
        accion: String,
        descripcion: String,
        materialId: String = "",
        cantidad: Int = 0
    ) {
        viewModelScope.launch {
            try {
                val activity = UserActivity(
                    userId = userId,
                    accion = accion,
                    descripcion = descripcion,
                    materialId = materialId,
                    cantidad = cantidad
                )
                userRepo.logUserActivity(activity)
                loadActivities() // Refresh activities
            } catch (e: Exception) {
                // Silently handle error
            }
        }
    }
    
    fun getRolePermissions(role: String): List<String> {
        return when (role) {
            "gerente" -> availablePermissions.map { it.first }
            "jefe_logistica" -> listOf(
                "movement_create", "movement_view", "inventory_search", 
                "reports_view", "reports_export", "notifications_manage"
            )
            "almacenero" -> listOf(
                "movement_create", "movement_view", "inventory_search"
            )
            "supervisor" -> listOf(
                "movement_view", "inventory_search", "reports_view"
            )
            "auditor" -> listOf(
                "inventory_search", "reports_view", "reports_export"
            )
            "operario" -> listOf(
                "movement_view", "inventory_search"
            )
            else -> emptyList()
        }
    }
    
    fun searchUsers(query: String): List<User> {
        if (query.isBlank()) return users
        return users.filter { user ->
            user.nombre.contains(query, ignoreCase = true) ||
            user.apellido.contains(query, ignoreCase = true) ||
            user.email.contains(query, ignoreCase = true) ||
            user.rol.contains(query, ignoreCase = true)
        }
    }
    
    fun getActiveUsers(): List<User> {
        return users.filter { it.activo }
    }
    
    fun getInactiveUsers(): List<User> {
        return users.filter { !it.activo }
    }
    
    fun getUsersByRole(role: String): List<User> {
        return users.filter { it.rol == role }
    }
}


