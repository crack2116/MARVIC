package com.proyecto.marvic.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Gestor de caché en memoria para optimizar consultas frecuentes
 */
object CacheManager {
    
    // Mapas de caché con mutex para thread-safety
    private val mutex = Mutex()
    
    // Caché de datos con timestamp
    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private val caches = mutableMapOf<String, CacheEntry<*>>()
    
    // Configuración de TTL (Time To Live) en milisegundos
    private val cacheTTL = mapOf(
        "inventory" to 30_000L,      // 30 segundos
        "providers" to 60_000L,      // 1 minuto
        "projects" to 60_000L,       // 1 minuto
        "transfers" to 30_000L,      // 30 segundos
        "movements" to 20_000L,      // 20 segundos
        "totals" to 15_000L,         // 15 segundos
        "stats" to 30_000L           // 30 segundos
    )
    
    /**
     * Guarda datos en caché
     */
    suspend fun <T> put(key: String, data: T) {
        mutex.withLock {
            caches[key] = CacheEntry(data)
        }
    }
    
    /**
     * Obtiene datos de caché si no han expirado
     */
    suspend fun <T> get(key: String): T? {
        return mutex.withLock {
            @Suppress("UNCHECKED_CAST")
            val entry = caches[key] as? CacheEntry<T> ?: return@withLock null
            
            // Obtener el tipo de caché desde la clave (formato: tipo_id)
            val cacheType = key.substringBefore("_")
            val ttl = cacheTTL[cacheType] ?: 30_000L
            
            // Verificar si ha expirado
            if (System.currentTimeMillis() - entry.timestamp > ttl) {
                caches.remove(key)
                return@withLock null
            }
            
            entry.data
        }
    }
    
    /**
     * Invalida una entrada específica del caché
     */
    suspend fun invalidate(key: String) {
        mutex.withLock {
            caches.remove(key)
        }
    }
    
    /**
     * Invalida todas las entradas de un tipo
     */
    suspend fun invalidateType(type: String) {
        mutex.withLock {
            caches.keys.removeAll { it.startsWith("${type}_") || it == type }
        }
    }
    
    /**
     * Limpia todo el caché
     */
    suspend fun clear() {
        mutex.withLock {
            caches.clear()
        }
    }
    
    /**
     * Limpia entradas expiradas
     */
    suspend fun cleanup() {
        mutex.withLock {
            val now = System.currentTimeMillis()
            val toRemove = mutableListOf<String>()
            
            caches.forEach { (key, entry) ->
                val cacheType = key.substringBefore("_")
                val ttl = cacheTTL[cacheType] ?: 30_000L
                
                if (now - entry.timestamp > ttl) {
                    toRemove.add(key)
                }
            }
            
            toRemove.forEach { caches.remove(it) }
        }
    }
    
    /**
     * Obtiene estadísticas del caché
     */
    suspend fun getStats(): Map<String, Int> {
        return mutex.withLock {
            val stats = mutableMapOf<String, Int>()
            
            caches.keys.forEach { key ->
                val type = key.substringBefore("_")
                stats[type] = (stats[type] ?: 0) + 1
            }
            
            stats["total"] = caches.size
            stats
        }
    }
    
    /**
     * Ejecuta una función con caché automático
     */
    suspend fun <T> cached(
        key: String,
        fetchData: suspend () -> T
    ): T {
        // Intentar obtener de caché
        val cached = get<T>(key)
        if (cached != null) {
            println("[CACHE HIT] $key")
            return cached
        }
        
        // Si no está en caché, fetchear y guardar
        println("[CACHE MISS] $key")
        val data = fetchData()
        put(key, data)
        return data
    }
    
    /**
     * Verifica si una clave existe en caché y no ha expirado
     */
    suspend fun has(key: String): Boolean {
        return get<Any>(key) != null
    }
    
    /**
     * Obtiene el tamaño del caché
     */
    suspend fun size(): Int {
        return mutex.withLock {
            caches.size
        }
    }
}





