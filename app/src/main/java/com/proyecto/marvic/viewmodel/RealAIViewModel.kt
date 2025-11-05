package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.ai.RealAIEngine
import com.proyecto.marvic.ai.AIAnalysis
import com.proyecto.marvic.ai.SmartRecommendation
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class RealAIViewModel : ViewModel() {
    
    private val aiEngine = RealAIEngine()
    
    var isLoading by mutableStateOf(false)
        private set
    
    var errorMessage by mutableStateOf<String?>(null)
        private set
    
    val aiAnalyses = mutableStateListOf<AIAnalysis>()
    val smartRecommendations = mutableStateListOf<SmartRecommendation>()
    
    var kpis by mutableStateOf<Map<String, Any>>(emptyMap())
        private set
    
    init {
        loadAIData()
    }
    
    fun loadAIData() {
        isLoading = true
        errorMessage = null
        
        viewModelScope.launch {
            try {
                // Cargar análisis de IA en paralelo
                val analysesJob = launch { loadMaterialAnalyses() }
                val recommendationsJob = launch { loadSmartRecommendations() }
                val kpisJob = launch { calculateRealKPIs() }
                
                // Esperar a que terminen todos
                analysesJob.join()
                recommendationsJob.join()
                kpisJob.join()
                
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message
                isLoading = false
            }
        }
    }
    
    private suspend fun loadMaterialAnalyses() {
        try {
            // Analizar los materiales más importantes
            val materialIds = listOf(
                "MAT001", "MAT002", "MAT008", "MAT011", "MAT015", "MAT016", "MAT018"
            )
            
            val analyses = materialIds.mapNotNull { materialId ->
                aiEngine.analyzeDemand(materialId)
            }
            
            aiAnalyses.clear()
            aiAnalyses.addAll(analyses)
        } catch (e: Exception) {
            errorMessage = "Error cargando análisis: ${e.message}"
        }
    }
    
    private suspend fun loadSmartRecommendations() {
        try {
            val recommendations = aiEngine.generateSmartRecommendations()
            smartRecommendations.clear()
            smartRecommendations.addAll(recommendations)
        } catch (e: Exception) {
            errorMessage = "Error cargando recomendaciones: ${e.message}"
        }
    }
    
    private suspend fun calculateRealKPIs() {
        try {
            val kpiData = mutableMapOf<String, Any>()
            
            // KPI 1: Eficiencia de inventario real
            val totalMaterials = aiAnalyses.size
            val optimizedMaterials = aiAnalyses.count { 
                it.recommendedAction in listOf("MAINTAIN_CURRENT", "MONITOR_CLOSELY")
            }
            val inventoryEfficiency = if (totalMaterials > 0) {
                (optimizedMaterials.toDouble() / totalMaterials * 100).roundToInt()
            } else 0
            kpiData["inventoryEfficiency"] = inventoryEfficiency
            
            // KPI 2: Precisión de predicciones
            val avgConfidence = if (aiAnalyses.isNotEmpty()) {
                aiAnalyses.map { it.confidence }.average().roundToInt()
            } else 0
            kpiData["predictionAccuracy"] = avgConfidence
            
            // KPI 3: Riesgo de stockout
            val criticalMaterials = aiAnalyses.count { 
                it.daysUntilDepletion <= 14 && it.daysUntilDepletion > 0
            }
            val stockoutRisk = if (totalMaterials > 0) {
                (criticalMaterials.toDouble() / totalMaterials * 100).roundToInt()
            } else 0
            kpiData["stockoutRisk"] = stockoutRisk
            
            // KPI 4: Optimización de costos
            val costOptimization = calculateCostOptimization()
            kpiData["costOptimization"] = costOptimization
            
            // KPI 5: Tendencias de demanda
            val increasingTrend = aiAnalyses.count { it.trendDirection == "increasing" }
            val demandTrend = if (totalMaterials > 0) {
                (increasingTrend.toDouble() / totalMaterials * 100).roundToInt()
            } else 0
            kpiData["demandTrend"] = demandTrend
            
            kpis = kpiData
        } catch (e: Exception) {
            errorMessage = "Error calculando KPIs: ${e.message}"
        }
    }
    
    private fun calculateCostOptimization(): Int {
        // Calcular optimización de costos basada en análisis
        val overstockMaterials = aiAnalyses.count { 
            it.recommendedAction == "REDUCE_STOCK"
        }
        val understockMaterials = aiAnalyses.count { 
            it.recommendedAction in listOf("REORDER_URGENT", "REORDER_SOON")
        }
        
        // Porcentaje de optimización (menos problemas = más optimización)
        val totalIssues = overstockMaterials + understockMaterials
        val totalMaterials = aiAnalyses.size
        
        return if (totalMaterials > 0) {
            ((totalMaterials - totalIssues).toDouble() / totalMaterials * 100).roundToInt()
        } else 0
    }
    
    fun getMaterialAnalysis(materialId: String): AIAnalysis? {
        return aiAnalyses.find { it.materialId == materialId }
    }
    
    fun getCriticalMaterials(): List<AIAnalysis> {
        return aiAnalyses.filter { 
            it.daysUntilDepletion <= 14 && it.daysUntilDepletion > 0
        }.sortedBy { it.daysUntilDepletion }
    }
    
    fun getHighConfidencePredictions(): List<AIAnalysis> {
        return aiAnalyses.filter { it.confidence >= 70 }
            .sortedByDescending { it.confidence }
    }
    
    fun getTrendingMaterials(): List<AIAnalysis> {
        return aiAnalyses.filter { it.trendDirection == "increasing" }
            .sortedByDescending { it.predictedDemand }
    }
    
    fun getOptimizationRecommendations(): List<SmartRecommendation> {
        return smartRecommendations.filter { 
            it.actionType.name in listOf("OPTIMIZE", "RELOCATE")
        }
    }
    
    fun getCriticalRecommendations(): List<SmartRecommendation> {
        return smartRecommendations.filter { it.priority.name == "CRITICAL" }
    }
    
    fun refreshAnalysis(materialId: String) {
        viewModelScope.launch {
            try {
                val analysis = aiEngine.analyzeDemand(materialId)
                if (analysis != null) {
                    val index = aiAnalyses.indexOfFirst { it.materialId == materialId }
                    if (index >= 0) {
                        aiAnalyses[index] = analysis
                    } else {
                        aiAnalyses.add(analysis)
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error actualizando análisis: ${e.message}"
            }
        }
    }
    
    fun getDashboardSummary(): Map<String, String> {
        return mapOf(
            "totalMaterials" to aiAnalyses.size.toString(),
            "criticalMaterials" to getCriticalMaterials().size.toString(),
            "highConfidencePredictions" to getHighConfidencePredictions().size.toString(),
            "optimizationOpportunities" to getOptimizationRecommendations().size.toString(),
            "inventoryEfficiency" to "${kpis["inventoryEfficiency"]}%",
            "predictionAccuracy" to "${kpis["predictionAccuracy"]}%",
            "stockoutRisk" to "${kpis["stockoutRisk"]}%",
            "costOptimization" to "${kpis["costOptimization"]}%"
        )
    }
    
    fun generateMaterialInsight(materialId: String): String {
        val analysis = getMaterialAnalysis(materialId) ?: return "Sin datos disponibles"
        
        return buildString {
            appendLine("📊 Análisis Inteligente de ${analysis.materialName}")
            appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            appendLine("📈 Demanda Predicha: ${analysis.predictedDemand.roundToInt()} unidades/día")
            appendLine("🎯 Confianza: ${analysis.confidence.roundToInt()}%")
            appendLine("📅 Días hasta agotamiento: ${analysis.daysUntilDepletion}")
            appendLine("🔄 Tendencia: ${getTrendEmoji(analysis.trendDirection)} ${getTrendText(analysis.trendDirection)}")
            appendLine("📦 Punto de reorden: ${analysis.optimalReorderPoint} unidades")
            appendLine("🌡️ Factor estacional: ${(analysis.seasonalFactor * 100).roundToInt()}%")
            appendLine()
            appendLine("💡 Recomendación: ${getActionDescription(analysis.recommendedAction)}")
        }
    }
    
    private fun getTrendEmoji(direction: String): String {
        return when (direction) {
            "increasing" -> "📈"
            "decreasing" -> "📉"
            else -> "➡️"
        }
    }
    
    private fun getTrendText(direction: String): String {
        return when (direction) {
            "increasing" -> "En aumento"
            "decreasing" -> "En descenso"
            else -> "Estable"
        }
    }
    
    private fun getActionDescription(action: String): String {
        return when (action) {
            "REORDER_URGENT" -> "🚨 REORDENAR URGENTE - Stock crítico"
            "REORDER_SOON" -> "⚠️ REORDENAR PRONTO - Stock bajo"
            "MONITOR_CLOSELY" -> "👁️ MONITOREAR DE CERCA - Stock adecuado"
            "REDUCE_STOCK" -> "📉 REDUCIR STOCK - Exceso de inventario"
            "MAINTAIN_CURRENT" -> "✅ MANTENER ACTUAL - Stock óptimo"
            else -> "❓ Revisar análisis"
        }
    }
    
    // Funciones para calcular métricas reales de IA
    suspend fun calculateEfficiency(): Int {
        return try {
            aiEngine.calculateAIEfficiency().roundToInt()
        } catch (e: Exception) {
            0
        }
    }
    
    suspend fun calculatePrecision(): Int {
        return try {
            aiEngine.calculateMLPrecision().roundToInt()
        } catch (e: Exception) {
            0
        }
    }
    
    suspend fun calculateRisk(materials: List<com.proyecto.marvic.data.MaterialItem>): Int {
        return try {
            aiEngine.calculateStockRisk(materials).roundToInt()
        } catch (e: Exception) {
            0
        }
    }
}
