package com.proyecto.marvic.utils

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.marvic.data.UserSession
import kotlinx.coroutines.tasks.await

/**
 * Sistema de logs de auditoría para rastrear operaciones críticas
 */
object AuditLogger {
    
    private val db = FirebaseFirestore.getInstance()
    private val logsCollection = db.collection("audit_logs")
    
    // Tipos de eventos
    object EventType {
        const val LOGIN = "LOGIN"
        const val LOGOUT = "LOGOUT"
        const val CREATE = "CREATE"
        const val UPDATE = "UPDATE"
        const val DELETE = "DELETE"
        const val VIEW = "VIEW"
        const val EXPORT = "EXPORT"
        const val TRANSFER = "TRANSFER"
        const val UPLOAD = "UPLOAD"
        const val PERMISSION_DENIED = "PERMISSION_DENIED"
        const val ERROR = "ERROR"
    }
    
    // Módulos del sistema
    object Module {
        const val INVENTORY = "INVENTORY"
        const val MOVEMENTS = "MOVEMENTS"
        const val PROVIDERS = "PROVIDERS"
        const val PROJECTS = "PROJECTS"
        const val TRANSFERS = "TRANSFERS"
        const val USERS = "USERS"
        const val REPORTS = "REPORTS"
        const val AUTH = "AUTH"
        const val PHOTOS = "PHOTOS"
    }
    
    // Niveles de severidad
    object Severity {
        const val INFO = "INFO"
        const val WARNING = "WARNING"
        const val ERROR = "ERROR"
        const val CRITICAL = "CRITICAL"
    }
    
    /**
     * Registra un evento en el log de auditoría
     */
    suspend fun log(
        eventType: String,
        module: String,
        description: String,
        severity: String = Severity.INFO,
        metadata: Map<String, Any> = emptyMap()
    ) {
        try {
            val logEntry = hashMapOf(
                "eventType" to eventType,
                "module" to module,
                "description" to description,
                "severity" to severity,
                "userId" to UserSession.userId,
                "userEmail" to UserSession.userEmail,
                "userRole" to UserSession.currentRole,
                "timestamp" to Timestamp.now(),
                "metadata" to metadata,
                "deviceInfo" to mapOf(
                    "platform" to "Android",
                    "appVersion" to "2.1"
                )
            )
            
            logsCollection.add(logEntry).await()
            
            // También imprimir en consola para debugging
            println("[AUDIT] $severity | $module | $eventType | $description")
            
        } catch (e: Exception) {
            // Si falla el log, al menos imprimirlo en consola
            println("[AUDIT ERROR] Failed to log: ${e.message}")
            println("[AUDIT] $severity | $module | $eventType | $description")
        }
    }
    
    /**
     * Log de login
     */
    suspend fun logLogin(email: String, success: Boolean) {
        log(
            eventType = EventType.LOGIN,
            module = Module.AUTH,
            description = if (success) "Usuario inició sesión: $email" else "Intento fallido de login: $email",
            severity = if (success) Severity.INFO else Severity.WARNING,
            metadata = mapOf(
                "email" to email,
                "success" to success
            )
        )
    }
    
    /**
     * Log de creación de entidades
     */
    suspend fun logCreate(module: String, entityType: String, entityId: String, details: String = "") {
        log(
            eventType = EventType.CREATE,
            module = module,
            description = "Creado $entityType: $entityId${if (details.isNotEmpty()) " - $details" else ""}",
            severity = Severity.INFO,
            metadata = mapOf(
                "entityType" to entityType,
                "entityId" to entityId
            )
        )
    }
    
    /**
     * Log de actualización
     */
    suspend fun logUpdate(module: String, entityType: String, entityId: String, changes: String = "") {
        log(
            eventType = EventType.UPDATE,
            module = module,
            description = "Actualizado $entityType: $entityId${if (changes.isNotEmpty()) " - $changes" else ""}",
            severity = Severity.INFO,
            metadata = mapOf(
                "entityType" to entityType,
                "entityId" to entityId,
                "changes" to changes
            )
        )
    }
    
    /**
     * Log de eliminación
     */
    suspend fun logDelete(module: String, entityType: String, entityId: String, reason: String = "") {
        log(
            eventType = EventType.DELETE,
            module = module,
            description = "Eliminado $entityType: $entityId${if (reason.isNotEmpty()) " - Razón: $reason" else ""}",
            severity = Severity.WARNING,
            metadata = mapOf(
                "entityType" to entityType,
                "entityId" to entityId,
                "reason" to reason
            )
        )
    }
    
    /**
     * Log de exportación
     */
    suspend fun logExport(reportType: String, recordCount: Int) {
        log(
            eventType = EventType.EXPORT,
            module = Module.REPORTS,
            description = "Exportado $reportType con $recordCount registros",
            severity = Severity.INFO,
            metadata = mapOf(
                "reportType" to reportType,
                "recordCount" to recordCount
            )
        )
    }
    
    /**
     * Log de transferencia
     */
    suspend fun logTransfer(materialId: String, cantidad: Int, origen: String, destino: String) {
        log(
            eventType = EventType.TRANSFER,
            module = Module.TRANSFERS,
            description = "Transferencia de $cantidad unidades del material $materialId de $origen a $destino",
            severity = Severity.INFO,
            metadata = mapOf(
                "materialId" to materialId,
                "cantidad" to cantidad,
                "origen" to origen,
                "destino" to destino
            )
        )
    }
    
    /**
     * Log de acceso denegado
     */
    suspend fun logPermissionDenied(module: String, action: String, reason: String = "") {
        log(
            eventType = EventType.PERMISSION_DENIED,
            module = module,
            description = "Acceso denegado para $action${if (reason.isNotEmpty()) ": $reason" else ""}",
            severity = Severity.WARNING,
            metadata = mapOf(
                "action" to action,
                "reason" to reason
            )
        )
    }
    
    /**
     * Log de error
     */
    suspend fun logError(module: String, errorMessage: String, stackTrace: String = "") {
        log(
            eventType = EventType.ERROR,
            module = module,
            description = "Error: $errorMessage",
            severity = Severity.ERROR,
            metadata = mapOf(
                "errorMessage" to errorMessage,
                "stackTrace" to stackTrace.take(1000)
            )
        )
    }
    
    /**
     * Log de movimiento de inventario
     */
    suspend fun logMovement(materialId: String, tipo: String, cantidad: Int, almacen: String) {
        log(
            eventType = tipo,
            module = Module.MOVEMENTS,
            description = "$tipo de $cantidad unidades del material $materialId en $almacen",
            severity = Severity.INFO,
            metadata = mapOf(
                "materialId" to materialId,
                "tipo" to tipo,
                "cantidad" to cantidad,
                "almacen" to almacen
            )
        )
    }
    
    /**
     * Log de upload de foto
     */
    suspend fun logPhotoUpload(materialId: String, imageUrl: String) {
        log(
            eventType = EventType.UPLOAD,
            module = Module.PHOTOS,
            description = "Foto subida para material $materialId",
            severity = Severity.INFO,
            metadata = mapOf(
                "materialId" to materialId,
                "imageUrl" to imageUrl
            )
        )
    }
    
    /**
     * Obtiene logs recientes (últimas 24 horas)
     */
    suspend fun getRecentLogs(limit: Int = 100): List<Map<String, Any>> {
        return try {
            val yesterday = System.currentTimeMillis() - (24 * 60 * 60 * 1000)
            val snapshot = logsCollection
                .whereGreaterThan("timestamp", Timestamp(yesterday / 1000, 0))
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            println("[AUDIT ERROR] Failed to retrieve logs: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtiene logs por usuario
     */
    suspend fun getLogsByUser(userId: String, limit: Int = 50): List<Map<String, Any>> {
        return try {
            val snapshot = logsCollection
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            println("[AUDIT ERROR] Failed to retrieve user logs: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtiene logs por módulo
     */
    suspend fun getLogsByModule(module: String, limit: Int = 50): List<Map<String, Any>> {
        return try {
            val snapshot = logsCollection
                .whereEqualTo("module", module)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            println("[AUDIT ERROR] Failed to retrieve module logs: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Obtiene logs de eventos críticos
     */
    suspend fun getCriticalLogs(limit: Int = 50): List<Map<String, Any>> {
        return try {
            val snapshot = logsCollection
                .whereIn("severity", listOf(Severity.ERROR, Severity.CRITICAL))
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            snapshot.documents.map { it.data ?: emptyMap() }
        } catch (e: Exception) {
            println("[AUDIT ERROR] Failed to retrieve critical logs: ${e.message}")
            emptyList()
        }
    }
}

