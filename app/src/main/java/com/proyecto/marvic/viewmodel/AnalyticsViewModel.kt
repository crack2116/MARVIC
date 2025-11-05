package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.FirestoreInventoryRepository
import com.proyecto.marvic.data.InventoryRepository
import com.proyecto.marvic.utils.CacheManager
import com.proyecto.marvic.utils.PerformanceMonitor
import kotlinx.coroutines.launch

data class AnalyticsData(
    val totalMaterials: Int = 0,
    val totalStock: Int = 0,
    val lowStockCount: Int = 0,
    val totalMovements: Int = 0,
    val avgPrice: Double = 0.0,
    val totalValue: Double = 0.0,
    val topCategories: List<Pair<String, Int>> = emptyList(),
    val recentActivity: List<String> = emptyList(),
    val cacheStats: Map<String, Int> = emptyMap(),
    val performanceStats: List<Pair<String, Long>> = emptyList()
)

class AnalyticsViewModel(
    private val repository: InventoryRepository = FirestoreInventoryRepository()
) : ViewModel() {
    
    var analytics by mutableStateOf(AnalyticsData())
        private set
    
    var isLoading by mutableStateOf(false)
        private set
    
    fun loadAnalytics() {
        isLoading = true
        viewModelScope.launch {
            try {
                val materials = repository.searchMaterials("").getOrNull() ?: emptyList()
                val movements = repository.recentMovements(100).getOrNull() ?: emptyList()
                val lowStock = repository.getLowStockMaterials().getOrNull() ?: emptyList()
                
                // Calcular métricas
                val totalStock = materials.sumOf { it.cantidad }
                val totalValue = materials.sumOf { it.cantidad * it.precioUnitario }
                val avgPrice = if (materials.isNotEmpty()) {
                    materials.map { it.precioUnitario }.average()
                } else 0.0
                
                // Top categorías
                val topCategories = materials
                    .groupBy { it.categoria }
                    .mapValues { it.value.size }
                    .toList()
                    .sortedByDescending { it.second }
                    .take(5)
                
                // Actividad reciente
                val recentActivity = movements.take(10).map { movement ->
                    val type = if (movement.delta > 0) "Entrada" else "Salida"
                    "$type: ${movement.materialId} (${movement.delta})"
                }
                
                // Estadísticas de caché
                val cacheStats = CacheManager.getStats()
                
                // Estadísticas de rendimiento
                val performanceStats = PerformanceMonitor.getSlowestOperations(5)
                
                analytics = AnalyticsData(
                    totalMaterials = materials.size,
                    totalStock = totalStock,
                    lowStockCount = lowStock.size,
                    totalMovements = movements.size,
                    avgPrice = avgPrice,
                    totalValue = totalValue,
                    topCategories = topCategories,
                    recentActivity = recentActivity,
                    cacheStats = cacheStats,
                    performanceStats = performanceStats
                )
                
            } catch (e: Exception) {
                println("Error cargando analytics: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }
    
    fun generatePerformanceReport(): String {
        return PerformanceMonitor.generateReport()
    }
    
    fun clearCache() {
        viewModelScope.launch {
            CacheManager.clear()
            loadAnalytics()
        }
    }
    
    fun clearPerformanceMetrics() {
        PerformanceMonitor.clear()
        loadAnalytics()
    }
}



