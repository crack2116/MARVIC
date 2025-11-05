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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.ui.theme.MarvicCard
import kotlin.math.roundToInt

data class KPIData(
    val title: String,
    val value: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val trend: String = "",
    val trendUp: Boolean = true
)

data class AlertData(
    val message: String,
    val severity: AlertSeverity,
    val timestamp: Long,
    val material: String = ""
)

enum class AlertSeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

@Composable
fun KPICard(
    kpi: KPIData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
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
                        text = kpi.title,
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = kpi.value,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = kpi.subtitle,
                        color = Color(0xFF9E9E9E),
                        fontSize = 10.sp
                    )
                }
                
                Icon(
                    imageVector = kpi.icon,
                    contentDescription = null,
                    tint = kpi.color,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            if (kpi.trend.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (kpi.trendUp) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (kpi.trendUp) MarvicGreen else Color(0xFFE74C3C),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = kpi.trend,
                        color = if (kpi.trendUp) MarvicGreen else Color(0xFFE74C3C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: AlertData,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (alert.severity) {
        AlertSeverity.LOW -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        AlertSeverity.MEDIUM -> Color(0xFFFF9800).copy(alpha = 0.1f)
        AlertSeverity.HIGH -> Color(0xFFFF5722).copy(alpha = 0.1f)
        AlertSeverity.CRITICAL -> Color(0xFFF44336).copy(alpha = 0.1f)
    }
    
    val borderColor = when (alert.severity) {
        AlertSeverity.LOW -> MarvicGreen
        AlertSeverity.MEDIUM -> Color(0xFFFF9800)
        AlertSeverity.HIGH -> Color(0xFFFF5722)
        AlertSeverity.CRITICAL -> Color(0xFFF44336)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (alert.severity) {
                    AlertSeverity.LOW -> Icons.Default.Info
                    AlertSeverity.MEDIUM -> Icons.Default.Warning
                    AlertSeverity.HIGH -> Icons.Default.Error
                    AlertSeverity.CRITICAL -> Icons.Default.ErrorOutline
                },
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.message,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                if (alert.material.isNotEmpty()) {
                    Text(
                        text = "Material: ${alert.material}",
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SmartDashboardContent(
    kpis: List<KPIData>,
    alerts: List<AlertData>,
    chartData: List<ChartData>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // KPIs Row
        item {
            Text(
                text = "Indicadores Clave (KPIs)",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(kpis) { kpi ->
                    KPICard(
                        kpi = kpi,
                        modifier = Modifier.width(180.dp)
                    )
                }
            }
        }
        
        // Charts
        item {
            Text(
                text = "Análisis de Consumo",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PieChart(
                    data = chartData,
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chartData.forEach { data ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(data.color, RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = data.label,
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "${data.value.roundToInt()}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Bar Chart
        item {
            BarChart(data = chartData)
        }
        
        // Alerts
        if (alerts.isNotEmpty()) {
            item {
                Text(
                    text = "Alertas Inteligentes",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(alerts) { alert ->
                AlertCard(alert = alert)
            }
        }
    }
}

@Composable
fun PredictionCard(
    materialName: String,
    currentStock: Int,
    predictedDays: Int,
    recommendedOrder: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "Predicción IA",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = MarvicOrange,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = materialName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Stock actual",
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$currentStock unidades",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Días restantes",
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$predictedDays días",
                        color = if (predictedDays < 7) Color(0xFFE74C3C) else MarvicGreen,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Column {
                    Text(
                        text = "Recomendado",
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                    Text(
                        text = "$recommendedOrder unidades",
                        color = MarvicOrange,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

