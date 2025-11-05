package com.proyecto.marvic.utils

/**
 * Utilidades para optimizar listas largas con paginación
 */
object LazyListOptimizer {
    
    private const val DEFAULT_PAGE_SIZE = 20
    private const val PREFETCH_DISTANCE = 3
    
    /**
     * Calcula qué elementos mostrar según el scroll
     */
    fun <T> paginateList(
        fullList: List<T>,
        pageSize: Int = DEFAULT_PAGE_SIZE,
        currentPage: Int = 0
    ): List<T> {
        val start = currentPage * pageSize
        val end = minOf(start + pageSize, fullList.size)
        
        return if (start < fullList.size) {
            fullList.subList(start, end)
        } else {
            emptyList()
        }
    }
    
    /**
     * Determina si se debe cargar la siguiente página
     */
    fun shouldLoadNextPage(
        currentIndex: Int,
        totalLoaded: Int,
        pageSize: Int = DEFAULT_PAGE_SIZE
    ): Boolean {
        // Cargar cuando estemos cerca del final
        return currentIndex >= totalLoaded - PREFETCH_DISTANCE
    }
    
    /**
     * Calcula el número total de páginas
     */
    fun calculateTotalPages(totalItems: Int, pageSize: Int = DEFAULT_PAGE_SIZE): Int {
        return (totalItems + pageSize - 1) / pageSize
    }
    
    /**
     * Crea una clave única para el caché basada en la consulta
     */
    fun createCacheKey(prefix: String, vararg params: Any?): String {
        return buildString {
            append(prefix)
            params.forEach { param ->
                append("_")
                append(param?.toString() ?: "null")
            }
        }
    }
}

/**
 * Clase para manejar paginación en listas
 */
class PaginatedList<T>(
    private val pageSize: Int = 20,
    private val loadPage: suspend (page: Int, size: Int) -> List<T>
) {
    private val loadedItems = mutableListOf<T>()
    private var currentPage = 0
    private var isLoading = false
    private var hasMore = true
    
    suspend fun loadMore(): List<T> {
        if (isLoading || !hasMore) return loadedItems
        
        isLoading = true
        return try {
            val newItems = loadPage(currentPage, pageSize)
            
            if (newItems.isEmpty() || newItems.size < pageSize) {
                hasMore = false
            }
            
            loadedItems.addAll(newItems)
            currentPage++
            
            loadedItems
        } finally {
            isLoading = false
        }
    }
    
    fun getLoadedItems(): List<T> = loadedItems.toList()
    
    fun reset() {
        loadedItems.clear()
        currentPage = 0
        hasMore = true
    }
    
    fun canLoadMore(): Boolean = hasMore && !isLoading
}



