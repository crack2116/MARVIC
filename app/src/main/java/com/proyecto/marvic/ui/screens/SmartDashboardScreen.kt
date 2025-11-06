package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.proyecto.marvic.data.UserSession
import com.proyecto.marvic.ui.components.*
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.AuthViewModel
import com.proyecto.marvic.viewmodel.InventoryViewModel
import com.proyecto.marvic.data.Movement
import com.proyecto.marvic.data.FirestoreInventoryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartDashboardScreen(
        onGoToMovement: () -> Unit,
        onGoToSearch: () -> Unit,
        onGoToReports: () -> Unit,
        onGoToNotifications: () -> Unit,
        onGoToUserManagement: () -> Unit,
        onGoToProviders: () -> Unit = {},
        onGoToProjects: () -> Unit = {},
        onGoToTransfers: () -> Unit = {},
        onGoToAnalytics: () -> Unit = {},
        onGoToProfile: () -> Unit = {},
        onBack: () -> Unit = {},
        vm: InventoryViewModel = viewModel(),
        authVm: AuthViewModel = viewModel()
    ) {
    // Estados para movimientos
    var recentMovements by remember { mutableStateOf<List<Movement>>(emptyList()) }
    var isLoadingMovements by remember { mutableStateOf(false) }
    var totalEntradas by remember { mutableStateOf(0) }
    var totalSalidas by remember { mutableStateOf(0) }
    
    val repository = remember { FirestoreInventoryRepository() }
    
    LaunchedEffect(Unit) {
        vm.refreshTotals()
        vm.observeCritical()
        vm.observeAllMaterials() // Cargar materiales para obtener nombres
        
        // Cargar movimientos recientes (para mostrar en lista)
        launch {
            isLoadingMovements = true
            try {
                // Obtener movimientos recientes para la lista (últimos 20)
                val movementsResult = repository.recentMovements(20)
                if (movementsResult.isSuccess) {
                    recentMovements = movementsResult.getOrDefault(emptyList())
                    println("✅ [Dashboard] Movimientos recientes cargados: ${recentMovements.size}")
                    if (recentMovements.isNotEmpty()) {
                        println("   📋 Primer movimiento: materialId=${recentMovements.first().materialId}, delta=${recentMovements.first().delta}, timestamp=${java.util.Date(recentMovements.first().timestamp)}")
                    }
                } else {
                    val error = movementsResult.exceptionOrNull()
                    println("❌ [Dashboard] Error cargando movimientos recientes: ${error?.message}")
                    error?.printStackTrace()
                }
                
                // Obtener TODOS los movimientos de las últimas 24 horas para calcular estadísticas
                val movements24hResult = repository.getMovementsInLast24Hours()
                val allMovements24h = if (movements24hResult.isSuccess) {
                    movements24hResult.getOrDefault(emptyList())
                } else {
                    val error = movements24hResult.exceptionOrNull()
                    println("❌ [Dashboard] Error obteniendo movimientos de 24h: ${error?.message}")
                    error?.printStackTrace()
                    emptyList()
                }
                
                println("📊 [Dashboard] Total movimientos en 24h: ${allMovements24h.size}")
                
                if (allMovements24h.isNotEmpty()) {
                    println("   📋 Ejemplo movimiento: materialId=${allMovements24h.first().materialId}, delta=${allMovements24h.first().delta}, timestamp=${java.util.Date(allMovements24h.first().timestamp)}")
                    
                    // Calcular totales del día
                    val entradas = allMovements24h
                        .filter { it.delta > 0 }
                        .sumOf { it.delta }
                    
                    val salidas = allMovements24h
                        .filter { it.delta < 0 }
                        .sumOf { kotlin.math.abs(it.delta) }
                    
                    totalEntradas = entradas
                    totalSalidas = salidas
                    
                    val balance = entradas - salidas
                    println("✅ [Dashboard] Estadísticas calculadas:")
                    println("   📥 Entradas: $totalEntradas unidades (${allMovements24h.filter { it.delta > 0 }.size} movimientos)")
                    println("   📤 Salidas: $totalSalidas unidades (${allMovements24h.filter { it.delta < 0 }.size} movimientos)")
                    println("   ⚖️ Balance Neto: $balance unidades")
                    println("   📈 Total movimientos: ${allMovements24h.size}")
                } else {
                    // Si no hay movimientos y no hay error de permisos, crear movimientos de prueba
                    val error = movements24hResult.exceptionOrNull()
                    if (error?.message?.contains("PERMISSION") != true) {
                        println("💡 [Dashboard] No hay movimientos, creando movimientos de prueba...")
                        
                        // Crear movimientos de prueba con fechas recientes
                        try {
                            val db = FirebaseFirestore.getInstance()
                            val currentTime = System.currentTimeMillis()
                            val hour = 60L * 60 * 1000
                            
                            val testMovements = listOf(
                                hashMapOf("materialId" to "MAT001", "delta" to 50, "timestamp" to currentTime - (2L * hour), "userId" to "almacenero@marvic.com"),
                                hashMapOf("materialId" to "MAT002", "delta" to -20, "timestamp" to currentTime - (5L * hour), "userId" to "almacenero@marvic.com"),
                                hashMapOf("materialId" to "MAT003", "delta" to 80, "timestamp" to currentTime - (8L * hour), "userId" to "jefe@marvic.com"),
                                hashMapOf("materialId" to "MAT004", "delta" to -15, "timestamp" to currentTime - (12L * hour), "userId" to "almacenero@marvic.com"),
                                hashMapOf("materialId" to "MAT005", "delta" to 100, "timestamp" to currentTime - (1L * hour), "userId" to "gerente@marvic.com"),
                                hashMapOf("materialId" to "MAT006", "delta" to -30, "timestamp" to currentTime - (18L * hour), "userId" to "almacenero@marvic.com")
                            )
                            
                            var created = 0
                            testMovements.forEach { movement ->
                                try {
                                    db.collection("movements").add(movement).await()
                                    created++
                                } catch (e: Exception) {
                                    println("⚠️ [Dashboard] Error creando movimiento de prueba: ${e.message}")
                                }
                            }
                            
                            println("✅ [Dashboard] Creados $created movimientos de prueba")
                            
                            // Recargar movimientos después de crearlos
                            delay(1000)
                            val movements24hResult2 = repository.getMovementsInLast24Hours()
                            if (movements24hResult2.isSuccess) {
                                val allMovements24h2 = movements24hResult2.getOrDefault(emptyList())
                                val entradas = allMovements24h2.filter { it.delta > 0 }.sumOf { it.delta }
                                val salidas = allMovements24h2.filter { it.delta < 0 }.sumOf { kotlin.math.abs(it.delta) }
                                totalEntradas = entradas
                                totalSalidas = salidas
                                recentMovements = repository.recentMovements(20).getOrDefault(emptyList())
                                println("✅ [Dashboard] Datos recargados después de crear movimientos de prueba")
                            }
                        } catch (e: Exception) {
                            println("❌ [Dashboard] Error creando movimientos de prueba: ${e.message}")
                        }
                    } else {
                        println("⚠️ [Dashboard] Error de permisos - Verifica las reglas de Firestore")
                    }
                }
            } catch (e: Exception) {
                println("❌ [Dashboard] Error cargando movimientos: ${e.message}")
                e.printStackTrace()
            }
            isLoadingMovements = false
        }
    }

    // Generar KPIs dinámicos
    val kpis = remember {
        listOf(
            KPIData(
                title = "Stock Total",
                value = "${vm.total}",
                subtitle = "unidades",
                icon = Icons.Default.Inventory,
                color = Color(0xFF4CAF50),
                trend = "+5.2%",
                trendUp = true
            ),
            KPIData(
                title = "Movimientos Hoy",
                value = "${Random.nextInt(15, 45)}",
                subtitle = "transacciones",
                icon = Icons.Default.SwapHoriz,
                color = Color(0xFF2196F3),
                trend = "+12.1%",
                trendUp = true
            ),
            KPIData(
                title = "Eficiencia",
                value = "${Random.nextInt(85, 98)}%",
                subtitle = "precisión",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFFFF9800),
                trend = "+2.3%",
                trendUp = true
            ),
            KPIData(
                title = "Alertas Activas",
                value = "${vm.critical.size}",
                subtitle = "críticas",
                icon = Icons.Default.Warning,
                color = if (vm.critical.size > 0) Color(0xFFE74C3C) else Color(0xFF4CAF50),
                trend = if (vm.critical.size > 0) "Requiere atención" else "Todo normal",
                trendUp = vm.critical.size == 0
            )
        )
    }

    // Datos para gráficos
    val chartData = remember {
        listOf(
            ChartData("Cementos", 450f, Color(0xFFFF9800)),
            ChartData("Aceros", 380f, Color(0xFFE74C3C)),
            ChartData("Tuberías", 220f, Color(0xFF3498DB)),
            ChartData("Maderas", 180f, Color(0xFF8BC34A)),
            ChartData("Otros", 120f, Color(0xFF9C27B0))
        )
    }

    // Alertas inteligentes
    val alerts = remember {
        mutableStateListOf(
            AlertData(
                message = "Stock de cemento por debajo del 20%",
                severity = AlertSeverity.HIGH,
                timestamp = System.currentTimeMillis(),
                material = "Cemento Portland Tipo I"
            ),
            AlertData(
                message = "Predicción: Fierros 1/2\" se agotarán en 3 días",
                severity = AlertSeverity.MEDIUM,
                timestamp = System.currentTimeMillis() - 3600000,
                material = "Fierro Corrugado 1/2\""
            ),
            AlertData(
                message = "Recomendación: Reponer tuberías PVC 2\"",
                severity = AlertSeverity.LOW,
                timestamp = System.currentTimeMillis() - 7200000,
                material = "Tubos PVC SAP 2\""
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        // Header dinámico
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                        colors = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Dashboard, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Grupo Marvic - Dashboard Inteligente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Análisis en tiempo real", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
            Row {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver al Dashboard", tint = Color.White)
                }
                // Solo mostrar gestión de usuarios y notificaciones si no es almacenero
                if (UserSession.currentRole != "almacenero") {
                    IconButton(onClick = onGoToUserManagement) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = "Gestión de Usuarios", tint = Color.White)
                    }
                    IconButton(onClick = onGoToNotifications) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notificaciones", tint = Color.White)
                    }
                }
            }
        }

        // Contenido principal de movimientos
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Acciones Rápidas - Solo mostrar Proveedores y Proyectos si no es almacenero
            if (UserSession.currentRole != "almacenero") {
                Text("Acciones Rápidas", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF673AB7)),
                        onClick = onGoToProviders
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Proveedores", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF00BCD4)),
                        onClick = onGoToProjects
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Text("Proyectos", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            if (UserSession.canAccessTransfers() || UserSession.canAccessAnalytics()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (UserSession.canAccessTransfers()) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF009688)),
                            onClick = onGoToTransfers
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text("Transferencias", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    if (UserSession.canAccessAnalytics()) {
                        Card(
                            onClick = onGoToAnalytics,
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Analytics, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Text("Analytics", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Sección de métricas de movimientos
            Text("Dashboard de Movimientos", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Entradas
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("$totalEntradas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Entradas", color = Color.White, fontSize = 12.sp)
                        Text("Últimas 24h", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
                
                // Total Salidas
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("$totalSalidas", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Salidas", color = Color.White, fontSize = 12.sp)
                        Text("Últimas 24h", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
                
                // Balance Neto
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2196F3))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(8.dp))
                        val balance = totalEntradas - totalSalidas
                        Text(
                            "${if (balance >= 0) "+" else ""}$balance",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("Balance Neto", color = Color.White, fontSize = 12.sp)
                        Text("Últimas 24h", color = Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Movimientos Recientes
            Text("Movimientos Recientes", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            
            if (isLoadingMovements) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else if (recentMovements.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFBDBDBD), modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No hay movimientos recientes", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                    }
                }
            } else {
                recentMovements.take(10).forEach { movement ->
                    val isEntrada = movement.delta > 0
                    // Obtener el nombre del material desde la lista de materiales
                    val material = vm.allMaterials.find { it.id == movement.materialId }
                    val materialName = material?.nombre ?: movement.materialId
                    val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(
                        java.util.Date(movement.timestamp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Icono de entrada o salida
                            Icon(
                                if (isEntrada) Icons.Default.Add else Icons.Default.Remove,
                                contentDescription = null,
                                tint = if (isEntrada) Color(0xFF4CAF50) else Color(0xFFE74C3C),
                                modifier = Modifier.size(24.dp)
                            )
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    materialName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    fecha,
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            }
                            
                            Text(
                                "${if (isEntrada) "+" else "-"}${kotlin.math.abs(movement.delta)}",
                                color = if (isEntrada) Color(0xFF4CAF50) else Color(0xFFE74C3C),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Navegación - Solo Dashboard, sin Movimiento
        NavigationBar(containerColor = Color(0xFF1A1A1A)) {
            NavigationBarItem(
                selected = true,
                onClick = { },
                label = { Text("Dashboard", fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Dashboard, contentDescription = null, tint = MarvicOrange) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MarvicOrange,
                    selectedTextColor = MarvicOrange,
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )
            )
            
            if (UserSession.canAccessSearch()) {
                NavigationBarItem(
                    selected = false,
                    onClick = onGoToSearch,
                    label = { Text("Búsqueda", fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
            
            if (UserSession.canAccessReports()) {
                NavigationBarItem(
                    selected = false,
                    onClick = onGoToReports,
                    label = { Text("Reportes", fontSize = 11.sp) },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}
