package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.viewmodel.AnalyticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    vm: AnalyticsViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        vm.loadAnalytics()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics & Métricas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { vm.loadAnalytics() }) {
                        Icon(Icons.Default.Refresh, "Refrescar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (vm.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1a1a2e))
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Título
                item {
                    Text(
                        "Métricas del Sistema",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // KPIs principales
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Materiales",
                            value = vm.analytics.totalMaterials.toString(),
                            icon = Icons.Default.Inventory,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF4CAF50)
                        )
                        MetricCard(
                            title = "Stock Total",
                            value = vm.analytics.totalStock.toString(),
                            icon = Icons.Default.Storage,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF2196F3)
                        )
                    }
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricCard(
                            title = "Bajo Stock",
                            value = vm.analytics.lowStockCount.toString(),
                            icon = Icons.Default.Warning,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFFF9800)
                        )
                        MetricCard(
                            title = "Movimientos",
                            value = vm.analytics.totalMovements.toString(),
                            icon = Icons.Default.SwapHoriz,
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
                
                // Valor total
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF16213e)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "Valor Total del Inventario",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    "S/ ${String.format("%.2f", vm.analytics.totalValue)}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "Precio promedio: S/ ${String.format("%.2f", vm.analytics.avgPrice)}",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Icon(
                                Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                
                // Top Categorías
                item {
                    Text(
                        "Top Categorías",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                
                items(vm.analytics.topCategories) { (category, count) ->
                    CategoryItem(category, count)
                }
                
                // Estadísticas de Caché
                item {
                    Text(
                        "Estadísticas de Caché",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF16213e)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            vm.analytics.cacheStats.forEach { (type, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(type, color = Color.Gray)
                                    Text(
                                        "$count items",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(Modifier.height(12.dp))
                            
                            Button(
                                onClick = { vm.clearCache() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5722)
                                )
                            ) {
                                Icon(Icons.Default.DeleteSweep, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Limpiar Caché")
                            }
                        }
                    }
                }
                
                // Estadísticas de Rendimiento
                item {
                    Text(
                        "Top 5 Operaciones Lentas",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                items(vm.analytics.performanceStats) { (operation, duration) ->
                    PerformanceItem(operation, duration)
                }
                
                // Botón de limpiar métricas
                item {
                    OutlinedButton(
                        onClick = { vm.clearPerformanceMetrics() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Speed, null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Limpiar Métricas de Rendimiento", color = Color.White)
                    }
                }
                
                // Actividad Reciente
                item {
                    Text(
                        "Actividad Reciente",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                items(vm.analytics.recentActivity) { activity ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF16213e)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (activity.startsWith("Entrada")) 
                                    Icons.Default.ArrowUpward 
                                else 
                                    Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (activity.startsWith("Entrada")) 
                                    Color(0xFF4CAF50) 
                                else 
                                    Color(0xFFF44336)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(activity, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: Color
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213e)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun CategoryItem(category: String, count: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213e)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(category, color = Color.White, fontWeight = FontWeight.Medium)
            }
            Text(
                "$count items",
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PerformanceItem(operation: String, duration: Long) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16213e)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                operation.replace("_", " ").capitalize(),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${duration}ms",
                color = when {
                    duration < 100 -> Color(0xFF4CAF50)
                    duration < 500 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

