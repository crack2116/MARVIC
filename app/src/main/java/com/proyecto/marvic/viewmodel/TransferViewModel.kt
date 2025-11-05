package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.Transfer
import com.proyecto.marvic.data.TransferRepository
import com.proyecto.marvic.data.FirestoreTransferRepository
import com.proyecto.marvic.utils.InputValidator
import com.proyecto.marvic.utils.AuditLogger
import com.proyecto.marvic.utils.RateLimiter
import com.proyecto.marvic.data.UserSession
import kotlinx.coroutines.launch

class TransferViewModel(
    private val repository: TransferRepository = FirestoreTransferRepository()
) : ViewModel() {
    
    var transfers by mutableStateOf<List<Transfer>>(emptyList())
        private set
    
    var pendingTransfers by mutableStateOf<List<Transfer>>(emptyList())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    var selectedTransfer by mutableStateOf<Transfer?>(null)
        private set
    
    fun loadTransfers() {
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            val result = repository.getTransfers()
            isLoading = false
            
            result.onSuccess { list ->
                transfers = list
            }.onFailure { error ->
                errorMessage = error.message
            }
        }
    }
    
    fun loadPendingTransfers() {
        viewModelScope.launch {
            val result = repository.getPendingTransfers()
            result.onSuccess { list ->
                pendingTransfers = list
            }
        }
    }
    
    fun createTransfer(
        materialId: String,
        materialNombre: String,
        cantidad: Int,
        origen: String,
        destino: String,
        responsable: String,
        motivo: String,
        notas: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        // Validar inputs
        val validation = InputValidator.validateTransfer(
            materialId = materialId,
            cantidad = cantidad.toString(),
            origen = InputValidator.sanitize(origen),
            destino = InputValidator.sanitize(destino),
            responsable = InputValidator.sanitizeName(responsable)
        )
        
        if (!validation.isValid) {
            onResult(false, validation.errorMessage ?: "Datos inválidos")
            return
        }
        
        // Rate limiting
        val userId = UserSession.userId
        if (!RateLimiter.isAllowed(userId, "create_transfer")) {
            onResult(false, "Has excedido el límite de transferencias por minuto. Intenta más tarde.")
            viewModelScope.launch {
                AuditLogger.logPermissionDenied(
                    AuditLogger.Module.TRANSFERS,
                    "create_transfer",
                    "Rate limit exceeded"
                )
            }
            return
        }
        
        viewModelScope.launch {
            val transfer = Transfer(
                materialId = materialId,
                materialNombre = InputValidator.sanitizeName(materialNombre),
                cantidad = cantidad,
                origenAlmacen = InputValidator.sanitize(origen),
                destinoAlmacen = InputValidator.sanitize(destino),
                responsable = InputValidator.sanitizeName(responsable),
                motivo = InputValidator.sanitize(motivo),
                notas = InputValidator.sanitizeNotes(notas),
                estado = "COMPLETADA" // Transferencia directa
            )
            
            val result = repository.createTransfer(transfer)
            
            result.onSuccess { transferId ->
                // Audit log
                AuditLogger.logTransfer(
                    materialId = materialId,
                    cantidad = cantidad,
                    origen = origen,
                    destino = destino
                )
                
                onResult(true, "Transferencia creada exitosamente")
                loadTransfers()
                loadPendingTransfers()
            }.onFailure { error ->
                // Log error
                AuditLogger.logError(
                    AuditLogger.Module.TRANSFERS,
                    error.message ?: "Error desconocido"
                )
                
                onResult(false, error.message ?: "Error al crear transferencia")
            }
        }
    }
    
    fun updateTransferStatus(transferId: String, newStatus: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = repository.updateTransferStatus(transferId, newStatus)
            
            result.onSuccess {
                onResult(true)
                loadTransfers()
                loadPendingTransfers()
            }.onFailure {
                onResult(false)
            }
        }
    }
    
    fun selectTransfer(transfer: Transfer) {
        selectedTransfer = transfer
    }
    
    fun clearSelection() {
        selectedTransfer = null
    }
    
    fun getTransfersByMaterial(materialId: String) {
        viewModelScope.launch {
            val result = repository.getTransfersByMaterial(materialId)
            result.onSuccess { list ->
                transfers = list
            }
        }
    }
}

