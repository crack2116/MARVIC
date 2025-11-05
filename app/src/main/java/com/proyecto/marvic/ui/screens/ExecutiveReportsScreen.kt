package com.proyecto.marvic.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.ui.components.ChartData
import com.proyecto.marvic.ui.components.BarChart
import com.proyecto.marvic.ui.components.PieChart
import com.proyecto.marvic.ui.components.KPIData
import com.proyecto.marvic.ui.components.KPICard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.viewmodel.ReportsViewModel
import com.proyecto.marvic.viewmodel.InventoryViewModel
import com.proyecto.marvic.utils.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveReportsScreen(
    onBack: () -> Unit,
    vm: ReportsViewModel = viewModel(),
    inventoryVm: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        inventoryVm.refreshTotals()
    }
    LaunchedEffect(Unit) { vm.load() }
    
    val selectedPeriod = remember { mutableStateOf("Últimos 7 días") }
    val periods = listOf("Últimos 7 días", "Últimos 30 días", "Último trimestre", "Último año")
    
    // KPIs ejecutivos
    val executiveKPIs = remember {
        listOf(
            KPIData(
                title = "Rotación de Stock",
                value = "4.2x",
                subtitle = "por mes",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF4CAF50),
                trend = "+0.3x",
                trendUp = true
            ),
            KPIData(
                title = "Precisión Inventario",
                value = "97.8%",
                subtitle = "exactitud",
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF2196F3),
                trend = "+2.1%",
                trendUp = true
            ),
            KPIData(
                title = "Tiempo Promedio",
                value = "2.3 min",
                subtitle = "por transacción",
                icon = Icons.Default.Schedule,
                color = Color(0xFFFF9800),
                trend = "-0.5 min",
                trendUp = true
            ),
            KPIData(
                title = "Ahorro Estimado",
                value = "S/ 45,200",
                subtitle = "este mes",
                icon = Icons.Default.Savings,
                color = Color(0xFF9C27B0),
                trend = "+15.2%",
                trendUp = true
            )
        )
    }
    
    // Datos de consumo por categoría
    val consumptionData = remember {
        listOf(
            ChartData("Cementos", 1250f, Color(0xFFFF9800)),
            ChartData("Aceros", 980f, Color(0xFFE74C3C)),
            ChartData("Tuberías", 650f, Color(0xFF3498DB)),
            ChartData("Maderas", 420f, Color(0xFF8BC34A)),
            ChartData("Pinturas", 380f, Color(0xFF9C27B0)),
            ChartData("Otros", 290f, Color(0xFF607D8B))
        )
    }
    
    // Datos de eficiencia por almacén
    val efficiencyData = remember {
        listOf(
            ChartData("Almacén 1", 94f, MarvicGreen),
            ChartData("Almacén 2", 89f, Color(0xFF4CAF50)),
            ChartData("Almacén 3", 92f, Color(0xFF8BC34A)),
            ChartData("Patio Exterior", 87f, Color(0xFFFF9800))
        )
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes Ejecutivos", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A1A1A)),
                actions = {
                    IconButton(
                        onClick = {
                            isExporting = true
                            scope.launch {
                                try {
                                    val pdfExporter = PdfExporter(context)
                                    val result = withContext(Dispatchers.IO) {
                                        pdfExporter.exportInventoryToPdf(inventoryVm.allMaterials)
                                    }
                                    result.onSuccess { filePath ->
                                        Toast.makeText(context, "PDF guardado en:\n$filePath", Toast.LENGTH_LONG).show()
                                    }.onFailure { error ->
                                        Toast.makeText(context, "Error al exportar: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        enabled = !isExporting
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.FileDownload, contentDescription = "Exportar", tint = Color.White)
                        }
                    }
                    IconButton(onClick = { 
                        Toast.makeText(context, "Próximamente", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir", tint = Color.White)
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selector de período
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MarvicCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Período de análisis",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(periods) { period ->
                                FilterChip(
                                    selected = selectedPeriod.value == period,
                                    onClick = { selectedPeriod.value = period },
                                    label = { Text(period, color = Color.White) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MarvicOrange,
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFF2A2A2A),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // KPIs Ejecutivos
            item {
                Text(
                    text = "Indicadores Clave de Rendimiento",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(executiveKPIs) { kpi ->
                        KPICard(
                            kpi = kpi,
                            modifier = Modifier.width(180.dp)
                        )
                    }
                }
            }
            
            // Resumen financiero
            item {
                Text(
                    text = "Resumen Financiero",
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
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MarvicGreen.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MarvicGreen, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ingresos", color = Color.White, fontSize = 12.sp)
                            Text("S/ 125,400", color = MarvicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("+12.5% vs mes anterior", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                    
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C).copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Costos", color = Color.White, fontSize = 12.sp)
                            Text("S/ 78,200", color = Color(0xFFE74C3C), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("-5.2% vs mes anterior", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                }
            }
            
            // Gráficos de análisis
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
                        data = consumptionData.take(4),
                        modifier = Modifier.weight(1f)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        consumptionData.take(4).forEach { data ->
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
            
            // Gráfico de barras de eficiencia
            item {
                BarChart(data = efficiencyData)
            }
            
            // Top materiales
            item {
                Text(
                    text = "Top 5 Materiales Más Utilizados",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MarvicCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        val topMaterials = listOf(
                            "Cemento Portland Tipo I" to 1250,
                            "Fierro Corrugado 1/2\"" to 980,
                            "Tubos PVC SAP 2\"" to 650,
                            "Madera Tornillo 2x4" to 420,
                            "Pintura Látex Blanco" to 380
                        )
                        
                        topMaterials.forEachIndexed { index, (material, quantity) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "#${index + 1}",
                                        color = MarvicOrange,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = material,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Text(
                                    text = "$quantity unidades",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            if (index < topMaterials.size - 1) {
                                Divider(color = Color(0xFF424242), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
            
            // Recomendaciones estratégicas
            item {
                Text(
                    text = "Recomendaciones Estratégicas",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MarvicGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Oportunidades de Optimización",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val recommendations = listOf(
                            "• Considerar automatización en Almacén 2 para mejorar eficiencia",
                            "• Optimizar stock de cemento - reducir en 15% para liberar capital",
                            "• Implementar sistema de alertas proactivas para tuberías PVC",
                            "• Evaluar contrato con proveedores de fierros para mejor precio"
                        )
                        
                        recommendations.forEach { recommendation ->
                            Text(
                                text = recommendation,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
