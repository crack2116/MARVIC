package com.proyecto.marvic.utils

import java.util.concurrent.ConcurrentHashMap

/**
 * Rate Limiter para prevenir abuso de operaciones
 */
object RateLimiter {
    
    // Mapa de operaciones por usuario con timestamps
    private val operationMap = ConcurrentHashMap<String, MutableList<Long>>()
    
    // Configuración de límites (operaciones permitidas por minuto)
    private val rateLimits = mapOf(
        "login" to 5,              // 5 intentos de login por minuto
        "create_material" to 20,   // 20 materiales por minuto
        "create_movement" to 30,   // 30 movimientos por minuto
        "create_transfer" to 10,   // 10 transferencias por minuto
        "upload_photo" to 10,      // 10 fotos por minuto
        "export_pdf" to 5,         // 5 PDFs por minuto
        "delete" to 10,            // 10 eliminaciones por minuto
        "search" to 100            // 100 búsquedas por minuto
    )
    
    // Ventana de tiempo en milisegundos (1 minuto)
    private const val TIME_WINDOW = 60_000L
    
    /**
     * Verifica si una operación está permitida
     */
    fun isAllowed(userId: String, operation: String): Boolean {
        val key = "$userId:$operation"
        val now = System.currentTimeMillis()
        
        val timestamps = operationMap.getOrPut(key) { mutableListOf() }
        
        // Limpiar timestamps antiguos (fuera de la ventana de tiempo)
        timestamps.removeAll { it < now - TIME_WINDOW }
        
        // Obtener límite para esta operación
        val limit = rateLimits[operation] ?: 50 // Default: 50 ops/min
        
        // Verificar si se excedió el límite
        return if (timestamps.size < limit) {
            timestamps.add(now)
            true
        } else {
            println("[RATE LIMIT] Límite excedido para $userId en operación $operation")
            false
        }
    }
    
    /**
     * Obtiene las operaciones restantes permitidas
     */
    fun getRemainingOperations(userId: String, operation: String): Int {
        val key = "$userId:$operation"
        val now = System.currentTimeMillis()
        
        val timestamps = operationMap.getOrPut(key) { mutableListOf() }
        timestamps.removeAll { it < now - TIME_WINDOW }
        
        val limit = rateLimits[operation] ?: 50
        return maxOf(0, limit - timestamps.size)
    }
    
    /**
     * Reinicia el contador para una operación específica
     */
    fun reset(userId: String, operation: String) {
        val key = "$userId:$operation"
        operationMap.remove(key)
    }
    
    /**
     * Reinicia todos los contadores de un usuario
     */
    fun resetUser(userId: String) {
        operationMap.keys.removeAll { it.startsWith("$userId:") }
    }
    
    /**
     * Limpia entradas antiguas (llamar periódicamente)
     */
    fun cleanup() {
        val now = System.currentTimeMillis()
        operationMap.forEach { (_, timestamps) ->
            timestamps.removeAll { it < now - TIME_WINDOW }
        }
        // Remover entradas vacías
        operationMap.entries.removeAll { it.value.isEmpty() }
    }
    
    /**
     * Obtiene estadísticas de uso
     */
    fun getStats(userId: String): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        operationMap.entries
            .filter { it.key.startsWith("$userId:") }
            .forEach { (key, timestamps) ->
                val operation = key.substringAfter(":")
                stats[operation] = timestamps.size
            }
        return stats
    }
}





