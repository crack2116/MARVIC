package com.proyecto.marvic.utils

import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source

/**
 * Optimizaciones para consultas de Firestore
 */
object FirestoreOptimizer {
    
    /**
     * Estrategias de caché para consultas
     */
    enum class CacheStrategy {
        CACHE_FIRST,    // Intenta caché primero, luego servidor
        SERVER_FIRST,   // Intenta servidor primero, luego caché
        CACHE_ONLY,     // Solo caché
        SERVER_ONLY     // Solo servidor
    }
    
    /**
     * Obtiene la fuente de datos según la estrategia
     */
    fun getSource(strategy: CacheStrategy): Source {
        return when (strategy) {
            CacheStrategy.CACHE_FIRST, CacheStrategy.CACHE_ONLY -> Source.CACHE
            CacheStrategy.SERVER_FIRST, CacheStrategy.SERVER_ONLY -> Source.SERVER
        }
    }
    
    /**
     * Aplica límites de consulta para paginación
     */
    fun Query.withPagination(pageSize: Int = 20): Query {
        return this.limit(pageSize.toLong())
    }
    
    /**
     * Optimiza una consulta con índices compuestos
     */
    fun Query.optimizeWithIndexes(
        orderByField: String,
        direction: Query.Direction = Query.Direction.DESCENDING
    ): Query {
        return this.orderBy(orderByField, direction)
    }
    
    /**
     * Crea una consulta optimizada con límites y orden
     */
    fun Query.optimized(
        orderField: String = "timestamp",
        pageSize: Int = 20,
        direction: Query.Direction = Query.Direction.DESCENDING
    ): Query {
        return this
            .orderBy(orderField, direction)
            .limit(pageSize.toLong())
    }
    
    /**
     * Batch para operaciones en lote
     */
    data class BatchConfig(
        val batchSize: Int = 500,  // Límite de Firestore
        val delayBetweenBatches: Long = 100 // ms
    )
    
    /**
     * Divide una lista grande en batches para operaciones en lote
     */
    fun <T> chunkForBatch(items: List<T>, batchSize: Int = 500): List<List<T>> {
        return items.chunked(batchSize)
    }
}

/**
 * Extension para realizar consultas con estrategia de caché
 */
suspend inline fun <T> cachedQuery(
    cacheKey: String,
    crossinline fetchFromFirestore: suspend () -> T
): T {
    return CacheManager.cached(cacheKey) {
        PerformanceMonitor.measure("firestore_query_$cacheKey") {
            fetchFromFirestore()
        }
    }
}





