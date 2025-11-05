package com.proyecto.marvic.notifications

import android.content.Context
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.firebase.firestore.FirebaseFirestore
import com.proyecto.marvic.data.MaterialItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object StockMonitor {
    private val db = FirebaseFirestore.getInstance()
    private val materialsCollection = db.collection("materials")
    
    // Configuración de umbrales
    private const val CRITICAL_THRESHOLD = 20
    private const val WARNING_THRESHOLD = 50
    private const val REMINDER_THRESHOLD = 100
    
    // Estado de notificaciones enviadas
    private val sentNotifications: SnapshotStateMap<String, Long> = mutableStateMapOf()
    private val lastStockLevels: MutableMap<String, Int> = mutableMapOf()
    
    fun startMonitoring(context: Context) {
        SmartNotificationManager.createNotificationChannels(context)
        
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                try {
                    checkStockLevels(context)
                    delay(300000) // Verificar cada 5 minutos
                } catch (e: Exception) {
                    println("Error en monitoreo de stock: ${e.message}")
                    delay(60000) // Esperar 1 minuto en caso de error
                }
            }
        }
    }
    
    private suspend fun checkStockLevels(context: Context) {
        try {
            val snapshot = materialsCollection.get().await()
            val materials = snapshot.documents.mapNotNull { doc ->
                try {
                    MaterialItem(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        cantidad = (doc.getLong("cantidad") ?: 0L).toInt(),
                        ubicacion = doc.getString("ubicacion") ?: "",
                        fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()?.time ?: 0L,
                        fechaActualizacion = doc.getTimestamp("fechaActualizacion")?.toDate()?.time ?: 0L
                    )
                } catch (e: Exception) {
                    println("Error al parsear material ${doc.id}: ${e.message}")
                    null
                }
            }
            
            materials.forEach { material ->
                checkMaterialStock(context, material)
            }
        } catch (e: Exception) {
            println("Error al obtener materiales: ${e.message}")
        }
    }
    
    private fun checkMaterialStock(context: Context, material: MaterialItem) {
        val materialKey = "${material.id}_${material.nombre}"
        val currentStock = material.cantidad
        val previousStock = lastStockLevels[materialKey] ?: currentStock
        
        // Actualizar nivel de stock anterior
        lastStockLevels[materialKey] = currentStock
        
        // Verificar si ya se envió una notificación reciente para este material
        val lastNotification = sentNotifications[materialKey] ?: 0
        val timeSinceLastNotification = System.currentTimeMillis() - lastNotification
        
        // No enviar notificaciones muy frecuentes (mínimo 1 hora entre notificaciones)
        if (timeSinceLastNotification < 3600000) {
            return
        }
        
        when {
            currentStock <= CRITICAL_THRESHOLD -> {
                if (currentStock != previousStock || timeSinceLastNotification > 7200000) { // 2 horas para crítico
                    SmartNotificationManager.showCriticalStockAlert(
                        context,
                        material.nombre,
                        currentStock,
                        material.ubicacion
                    )
                    sentNotifications[materialKey] = System.currentTimeMillis()
                }
            }
            
            currentStock <= WARNING_THRESHOLD -> {
                if (currentStock != previousStock || timeSinceLastNotification > 14400000) { // 4 horas para advertencia
                    SmartNotificationManager.showLowStockWarning(
                        context,
                        material.nombre,
                        currentStock,
                        material.ubicacion
                    )
                    sentNotifications[materialKey] = System.currentTimeMillis()
                }
            }
            
            currentStock <= REMINDER_THRESHOLD && currentStock > WARNING_THRESHOLD -> {
                // Solo enviar recordatorios una vez al día
                if (timeSinceLastNotification > 86400000) { // 24 horas
                    val estimatedDaysLeft = calculateDaysLeft(currentStock, material.nombre)
                    if (estimatedDaysLeft <= 7) {
                        SmartNotificationManager.showReplenishmentReminder(
                            context,
                            material.nombre,
                            estimatedDaysLeft
                        )
                        sentNotifications[materialKey] = System.currentTimeMillis()
                    }
                }
            }
        }
    }
    
    private fun calculateDaysLeft(currentStock: Int, materialName: String): Int {
        // Simulación simple basada en el nombre del material
        // En una implementación real, esto se basaría en datos históricos de consumo
        return when {
            materialName.contains("cemento", ignoreCase = true) -> currentStock / 10
            materialName.contains("fierro", ignoreCase = true) -> currentStock / 8
            materialName.contains("tubería", ignoreCase = true) -> currentStock / 5
            materialName.contains("madera", ignoreCase = true) -> currentStock / 6
            else -> currentStock / 7
        }.coerceAtLeast(1)
    }
    
    fun onMovementRegistered(context: Context, materialName: String, movementType: String, quantity: Int) {
        SmartNotificationManager.showMovementNotification(
            context,
            materialName,
            movementType,
            quantity
        )
    }
    
    fun clearNotificationHistory() {
        sentNotifications.clear()
        lastStockLevels.clear()
    }
    
    fun getNotificationStatus(): Map<String, Any> {
        return mapOf(
            "totalNotifications" to sentNotifications.size,
            "monitoredMaterials" to lastStockLevels.size,
            "criticalThreshold" to CRITICAL_THRESHOLD,
            "warningThreshold" to WARNING_THRESHOLD,
            "reminderThreshold" to REMINDER_THRESHOLD
        )
    }
}


