package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.FirestoreProviderRepository
import com.proyecto.marvic.data.Provider
import com.proyecto.marvic.data.ProviderPurchase
import com.proyecto.marvic.data.ProviderRepository
import com.proyecto.marvic.utils.InputValidator
import com.proyecto.marvic.utils.AuditLogger
import com.proyecto.marvic.utils.RateLimiter
import com.proyecto.marvic.data.UserSession
import kotlinx.coroutines.launch

class ProviderViewModel(
    private val repo: ProviderRepository = FirestoreProviderRepository()
) : ViewModel() {
    
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    val providers = mutableStateListOf<Provider>()
    val purchases = mutableStateListOf<ProviderPurchase>()
    var selectedProvider by mutableStateOf<Provider?>(null)
        private set
    
    fun loadProviders() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.getAllProviders()
            isLoading = false
            if (result.isSuccess) {
                providers.clear()
                providers.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun loadActiveProviders() {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.getActiveProviders()
            isLoading = false
            if (result.isSuccess) {
                providers.clear()
                providers.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun searchProviders(query: String) {
        if (query.isEmpty()) {
            loadProviders()
            return
        }
        
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.searchProviders(query)
            isLoading = false
            if (result.isSuccess) {
                providers.clear()
                providers.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun createProvider(provider: Provider, onComplete: (Boolean) -> Unit) {
        // Validar inputs
        val validation = InputValidator.validateProvider(
            nombre = provider.nombre,
            ruc = provider.ruc,
            email = provider.email,
            telefono = provider.contactoPrincipal
        )
        
        if (!validation.isValid) {
            errorMessage = validation.errorMessage
            onComplete(false)
            return
        }
        
        // Rate limiting
        val userId = UserSession.userId
        if (!RateLimiter.isAllowed(userId, "create_material")) {
            errorMessage = "Has excedido el límite de creaciones por minuto"
            viewModelScope.launch {
                AuditLogger.logPermissionDenied(
                    AuditLogger.Module.PROVIDERS,
                    "create_provider",
                    "Rate limit exceeded"
                )
            }
            onComplete(false)
            return
        }
        
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            // Sanitizar datos
            val sanitizedProvider = provider.copy(
                nombre = InputValidator.sanitizeName(provider.nombre),
                ruc = provider.ruc.trim(),
                direccion = InputValidator.sanitizeAddress(provider.direccion),
                email = provider.email.trim().lowercase(),
                contactoPrincipal = InputValidator.sanitizeName(provider.contactoPrincipal),
                notas = InputValidator.sanitizeNotes(provider.notas)
            )
            
            val result = repo.createProvider(sanitizedProvider)
            isLoading = false
            if (result.isSuccess) {
                val createdProvider = result.getOrNull()
                // Audit log
                AuditLogger.logCreate(
                    AuditLogger.Module.PROVIDERS,
                    "Provider",
                    createdProvider?.id ?: "",
                    sanitizedProvider.nombre
                )
                
                loadProviders()
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                AuditLogger.logError(
                    AuditLogger.Module.PROVIDERS,
                    errorMessage ?: "Error al crear proveedor"
                )
                onComplete(false)
            }
        }
    }
    
    fun updateProvider(provider: Provider, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.updateProvider(provider)
            isLoading = false
            if (result.isSuccess) {
                loadProviders()
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun deleteProvider(providerId: String, onComplete: (Boolean) -> Unit) {
        // Rate limiting
        val userId = UserSession.userId
        if (!RateLimiter.isAllowed(userId, "delete")) {
            errorMessage = "Has excedido el límite de eliminaciones por minuto"
            onComplete(false)
            return
        }
        
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.deleteProvider(providerId)
            isLoading = false
            if (result.isSuccess) {
                // Audit log
                AuditLogger.logDelete(
                    AuditLogger.Module.PROVIDERS,
                    "Provider",
                    providerId
                )
                
                providers.removeIf { it.id == providerId }
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                AuditLogger.logError(
                    AuditLogger.Module.PROVIDERS,
                    errorMessage ?: "Error al eliminar proveedor"
                )
                onComplete(false)
            }
        }
    }
    
    fun selectProvider(provider: Provider) {
        selectedProvider = provider
        loadProviderPurchases(provider.id)
    }
    
    fun loadProviderPurchases(providerId: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.getPurchasesByProvider(providerId)
            isLoading = false
            if (result.isSuccess) {
                purchases.clear()
                purchases.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.message
            }
        }
    }
    
    fun createPurchase(purchase: ProviderPurchase, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.createPurchase(purchase)
            isLoading = false
            if (result.isSuccess) {
                selectedProvider?.let { loadProviderPurchases(it.id) }
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun receivePurchase(purchaseId: String, receivedBy: String, onComplete: (Boolean) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = repo.receivePurchase(purchaseId, receivedBy)
            isLoading = false
            if (result.isSuccess) {
                selectedProvider?.let { loadProviderPurchases(it.id) }
                onComplete(true)
            } else {
                errorMessage = result.exceptionOrNull()?.message
                onComplete(false)
            }
        }
    }
    
    fun clearError() {
        errorMessage = null
    }
}

