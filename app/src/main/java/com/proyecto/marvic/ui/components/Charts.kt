package com.proyecto.marvic.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import kotlin.math.*

data class ChartData(
    val label: String,
    val value: Float,
    val color: Color
)

@Composable
fun PieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    
    Box(
        modifier = modifier
            .size(200.dp)
            .background(Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(150.dp)
                .padding(8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2f
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
            
            var startAngle = -90f
            
            data.forEach { chartData ->
                val sweepAngle = (chartData.value / total) * 360f
                
                drawArc(
                    color = chartData.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
            
            // Dibujar círculo central blanco
            drawCircle(
                color = Color(0xFF121212),
                radius = radius * 0.4f,
                center = center
            )
        }
        
        // Leyenda
        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total: ${total.toInt()}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 1f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Consumo por Categoría",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val barWidth = canvasWidth / data.size * 0.8f
                val spacing = canvasWidth / data.size * 0.2f
                
                data.forEachIndexed { index, chartData ->
                    val barHeight = (chartData.value / maxValue) * (canvasHeight - 40f)
                    val x = index * (barWidth + spacing) + spacing / 2
                    val y = canvasHeight - barHeight - 20f
                    
                    // Dibujar barra
                    drawRect(
                        color = chartData.color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                    
                    // Dibujar valor
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = 24f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(
                            chartData.value.toInt().toString(),
                            x + barWidth / 2f,
                            y - 10f,
                            paint
                        )
                    }
                }
            }
            
            // Leyenda
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { chartData ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(chartData.color, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = chartData.label,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LineChart(
    data: List<Pair<String, Float>>,
    modifier: Modifier = Modifier
) {
    val maxValue = data.maxOfOrNull { it.second } ?: 1f
    val minValue = data.minOfOrNull { it.second } ?: 0f
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tendencia de Stock",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val padding = 40f
                
                if (data.isNotEmpty()) {
                    val stepX = (canvasWidth - 2 * padding) / (data.size - 1)
                    
                    // Dibujar líneas de la grilla
                    for (i in 0..4) {
                        val y = padding + (canvasHeight - 2 * padding) * i / 4
                        drawLine(
                            color = Color(0xFF424242),
                            start = Offset(padding, y),
                            end = Offset(canvasWidth - padding, y)
                        )
                    }
                    
                    // Dibujar línea de datos
                    val points = data.mapIndexed { index, pair ->
                        val x = padding + index * stepX
                        val y = canvasHeight - padding - ((pair.second - minValue) / (maxValue - minValue)) * (canvasHeight - 2 * padding)
                        Offset(x, y)
                    }
                    
                    // Línea principal
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = MarvicOrange,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 3.dp.toPx()
                        )
                    }
                    
                    // Puntos
                    points.forEach { point ->
                        drawCircle(
                            color = MarvicOrange,
                            radius = 4.dp.toPx(),
                            center = point
                        )
                    }
                }
            }
        }
    }
}

