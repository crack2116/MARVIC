package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.RoleConfig
import com.proyecto.marvic.data.RoleRepository
import com.proyecto.marvic.data.FirestoreRoleRepository
import kotlinx.coroutines.launch

class RoleViewModel(
    private val repo: RoleRepository = FirestoreRoleRepository()
) : ViewModel() {
    
    var roles by mutableStateOf<List<RoleConfig>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    init {
        loadRoles()
    }
    
    fun loadRoles() {
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                // Inicializar roles por defecto si no existen
                repo.initializeDefaultRoles()
                
                // Cargar roles
                val result = repo.getAllRoles()
                
                if (result.isSuccess) {
                    roles = result.getOrNull() ?: emptyList()
                } else {
                    errorMessage = result.exceptionOrNull()?.message ?: "Error al cargar roles"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
    
    fun getRoleDisplayNames(): List<String> {
        return roles.map { it.displayName }
    }
    
    fun getRoleIdByDisplayName(displayName: String): String? {
        return roles.find { it.displayName == displayName }?.nombre
    }
}





