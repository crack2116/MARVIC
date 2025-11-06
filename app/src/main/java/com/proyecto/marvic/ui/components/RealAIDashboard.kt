package com.proyecto.marvic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.marvic.ai.AIAnalysis
import com.proyecto.marvic.ai.SmartRecommendation
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import kotlin.math.roundToInt

@Composable
fun RealAIDashboardContent(
    analyses: List<AIAnalysis>,
    recommendations: List<SmartRecommendation>,
    kpis: Map<String, Any>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPIs de IA Real
        item {
            Text(
                text = "🤖 Análisis con IA Real",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // KPI 1: Eficiencia de Inventario
                item {
                    RealKPICard(
                        title = "Eficiencia IA",
                        value = "${kpis["inventoryEfficiency"]}%",
                        icon = Icons.Default.TrendingUp,
                        color = MarvicGreen,
                        subtitle = "Optimización automática"
                    )
                }
                
                // KPI 2: Precisión de Predicciones
                item {
                    RealKPICard(
                        title = "Precisión ML",
                        value = "${kpis["predictionAccuracy"]}%",
                        icon = Icons.Default.Analytics,
                        color = Color(0xFF2196F3),
                        subtitle = "Confianza en predicciones"
                    )
                }
                
                // KPI 3: Riesgo de Stockout
                item {
                    RealKPICard(
                        title = "Riesgo Stockout",
                        value = "${kpis["stockoutRisk"]}%",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFE74C3C),
                        subtitle = "Materiales en riesgo"
                    )
                }
            }
        }
        
        // Predicciones de Demanda Real
        item {
            Text(
                text = "📈 Predicciones de Demanda (IA Real)",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (analyses.isEmpty() && !isLoading) {
                Text(
                    text = "Analizando datos históricos...",
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
            } else {
                analyses.take(5).forEach { analysis ->
                    RealDemandPredictionCard(analysis = analysis)
                }
            }
        }
        
        // Recomendaciones Inteligentes
        item {
            Text(
                text = "💡 Recomendaciones Inteligentes",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (recommendations.isEmpty() && !isLoading) {
                Text(
                    text = "Generando recomendaciones...",
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
            } else {
                recommendations.take(3).forEach { recommendation ->
                    SmartRecommendationCard(recommendation = recommendation)
                }
            }
        }
        
        // Análisis Detallado
        item {
            Text(
                text = "🔍 Análisis Detallado por Material",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            if (analyses.isEmpty() && !isLoading) {
                Text(
                    text = "Procesando análisis de patrones...",
                    color = Color(0xFFBDBDBD),
                    fontSize = 14.sp
                )
            } else {
                analyses.take(3).forEach { analysis ->
                    DetailedAnalysisCard(analysis = analysis)
                }
            }
        }
    }
}

@Composable
fun RealKPICard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    subtitle: String
) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                color = Color(0xFFBDBDBD),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
fun RealDemandPredictionCard(
    analysis: AIAnalysis
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = analysis.materialName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Demanda: ${analysis.predictedDemand.roundToInt()} unidades/día",
                        color = Color(0xFFBDBDBD),
                        fontSize = 14.sp
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${analysis.confidence.roundToInt()}%",
                        color = getConfidenceColor(analysis.confidence),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Confianza",
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    label = "Stock Actual",
                    value = "${analysis.currentStock}",
                    color = Color.White
                )
                InfoItem(
                    label = "Días Restantes",
                    value = if (analysis.daysUntilDepletion > 0) "${analysis.daysUntilDepletion}" else "N/A",
                    color = if (analysis.daysUntilDepletion <= 14) Color(0xFFE74C3C) else Color.White
                )
                InfoItem(
                    label = "Tendencia",
                    value = getTrendEmoji(analysis.trendDirection),
                    color = getTrendColor(analysis.trendDirection)
                )
            }
        }
    }
}

@Composable
fun SmartRecommendationCard(
    recommendation: SmartRecommendation
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (recommendation.priority.name) {
                "CRITICAL" -> Color(0xFFE74C3C).copy(alpha = 0.1f)
                "HIGH" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                "MEDIUM" -> Color(0xFF2196F3).copy(alpha = 0.1f)
                else -> MarvicCard
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    getRecommendationIcon(recommendation.actionType.name),
                    contentDescription = null,
                    tint = getPriorityColor(recommendation.priority.name),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recommendation.title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = getPriorityText(recommendation.priority.name),
                        color = getPriorityColor(recommendation.priority.name),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recommendation.description,
                color = Color.White,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "💰 Impacto: ${recommendation.estimatedImpact}",
                color = Color(0xFFBDBDBD),
                fontSize = 12.sp
            )
            
            if (recommendation.materials.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📦 Materiales: ${recommendation.materials.take(3).joinToString(", ")}${if (recommendation.materials.size > 3) "..." else ""}",
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun DetailedAnalysisCard(
    analysis: AIAnalysis
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = analysis.materialName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Métricas detalladas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(
                    label = "Punto Reorden",
                    value = "${analysis.optimalReorderPoint}",
                    color = MarvicOrange
                )
                MetricItem(
                    label = "Factor Estacional",
                    value = "${(analysis.seasonalFactor * 100).roundToInt()}%",
                    color = Color(0xFF2196F3)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recomendación específica
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (analysis.recommendedAction) {
                        "REORDER_URGENT" -> Color(0xFFE74C3C).copy(alpha = 0.1f)
                        "REORDER_SOON" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                        "MAINTAIN_CURRENT" -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                        else -> Color(0xFF2196F3).copy(alpha = 0.1f)
                    }
                )
            ) {
                Text(
                    text = getActionDescription(analysis.recommendedAction),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color(0xFFBDBDBD),
            fontSize = 12.sp
        )
    }
}

@Composable
fun MetricItem(
    label: String,
    value: String,
    color: Color
) {
    Column {
        Text(
            text = label,
            color = Color(0xFFBDBDBD),
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Funciones auxiliares
private fun getConfidenceColor(confidence: Double): Color {
    return when {
        confidence >= 80 -> Color(0xFF4CAF50)
        confidence >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFE74C3C)
    }
}

private fun getTrendEmoji(direction: String): String {
    return when (direction) {
        "increasing" -> "📈"
        "decreasing" -> "📉"
        else -> "➡️"
    }
}

private fun getTrendColor(direction: String): Color {
    return when (direction) {
        "increasing" -> Color(0xFF4CAF50)
        "decreasing" -> Color(0xFFE74C3C)
        else -> Color(0xFFFF9800)
    }
}

private fun getRecommendationIcon(actionType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (actionType) {
        "REORDER" -> Icons.Default.ShoppingCart
        "OPTIMIZE" -> Icons.Default.Tune
        "RELOCATE" -> Icons.Default.LocationOn
        "DISCOUNT" -> Icons.Default.LocalOffer
        "MAINTENANCE" -> Icons.Default.Build
        else -> Icons.Default.Info
    }
}

private fun getPriorityColor(priority: String): Color {
    return when (priority) {
        "CRITICAL" -> Color(0xFFE74C3C)
        "HIGH" -> Color(0xFFFF9800)
        "MEDIUM" -> Color(0xFF2196F3)
        else -> Color(0xFF4CAF50)
    }
}

private fun getPriorityText(priority: String): String {
    return when (priority) {
        "CRITICAL" -> "CRÍTICO"
        "HIGH" -> "ALTA"
        "MEDIUM" -> "MEDIA"
        else -> "BAJA"
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










