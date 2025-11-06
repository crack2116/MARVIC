package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.AuthViewModel
import com.proyecto.marvic.viewmodel.InventoryViewModel
import com.proyecto.marvic.data.UserSession
import com.proyecto.marvic.data.MaterialItem

@Composable
fun DashboardScreen(
    onGoToMovement: () -> Unit, 
    onGoToSearch: () -> Unit, 
    onGoToReports: () -> Unit, 
    onGoToSmartDashboard: () -> Unit,
    onGoToProfile: () -> Unit = {},
    onGoToAnalytics: () -> Unit = {},
    vm: InventoryViewModel = viewModel(), 
    authVm: AuthViewModel = viewModel()
) {
    var showMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        vm.refreshTotals()
        vm.observeCritical()
        vm.seedFirebaseData() // Agregar datos de prueba si no existen
        vm.simulateInventoryActivity() // Simular actividad para entrenar IA
        vm.observeAllMaterials()
    }
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1A1A1A)).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Grupo Marvic - Inventario", color = Color.White, fontWeight = FontWeight.SemiBold)
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Menú de usuario",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color(0xFF1A1A1A))
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Dashboard, contentDescription = null, tint = MarvicOrange, modifier = Modifier.size(20.dp))
                                    Text("Dashboard", color = Color.White)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onGoToSmartDashboard()
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = MarvicOrange, modifier = Modifier.size(20.dp))
                                    Text("Mi Perfil", color = Color.White)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onGoToProfile()
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Analytics, contentDescription = null, tint = MarvicOrange, modifier = Modifier.size(20.dp))
                                    Text("Analytics", color = Color.White)
                                }
                            },
                            onClick = {
                                showMenu = false
                                onGoToAnalytics()
                            }
                        )
                    }
                }
            }
        Column(modifier = Modifier.weight(1f).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().background(MarvicCard).padding(16.dp)) {
                    Text("STOCK TOTAL:", color = Color(0xFFBDBDBD))
                    Text("${vm.total} unidades", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    
                    // Lista de productos principales
                    Text("PRODUCTOS PRINCIPALES", color = Color(0xFFBDBDBD), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(vm.allMaterials.take(8)) { material ->
                            MaterialItemCard(material)
                        }
                    }
                }
            }
            if (UserSession.canAccessSearch()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("MATERIAL", color = Color(0xFFBDBDBD))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip("CEMENTO", MarvicOrange)
                        FilterChip("ACERO", Color(0xFFE74C3C))
                        FilterChip("MÉTODO", Color(0xFF3498DB))
                        FilterChip("TUBERÍAS", MarvicGreen)
                    }
                }
            }
            Text("MATERIALES REGISTRADOS", color = Color(0xFFBDBDBD))
            LazyColumn(
                modifier = Modifier.height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(vm.allMaterials) { material ->
                    MaterialPriorityCard(material)
                }
                
                if (vm.allMaterials.isEmpty()) {
                    item {
                        PriorityCard(
                            text = "📦 Cargando materiales de Firebase...", 
                            color = Color(0xFF3498DB)
                        )
                    }
                }
            }
        }
        NavigationBar(containerColor = Color(0xFF1A1A1A)) {
            NavigationBarItem(
                selected = true,
                onClick = { },
                label = { Text("Inicio", fontSize = 12.sp) },
                icon = { androidx.compose.material3.Icon(Icons.Default.Home, contentDescription = null, tint = MarvicOrange) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MarvicOrange,
                    selectedTextColor = MarvicOrange,
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White,
                    indicatorColor = Color.Transparent
                )
            )
            if (UserSession.canAccessMovement()) {
                NavigationBarItem(
                    selected = false,
                    onClick = onGoToMovement,
                    label = { Text("Movimiento", fontSize = 11.sp) },
                    icon = { androidx.compose.material3.Icon(Icons.Default.SwapVert, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        unselectedIconColor = Color.White,
                        unselectedTextColor = Color.White,
                        indicatorColor = Color.Transparent
                    )
                )
            }
            if (UserSession.canAccessSearch()) {
                NavigationBarItem(
                    selected = false,
                    onClick = onGoToSearch,
                    label = { Text("Búsqueda", fontSize = 11.sp) },
                    icon = { androidx.compose.material3.Icon(Icons.Default.Search, contentDescription = null) },
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
                    icon = { androidx.compose.material3.Icon(Icons.Default.Assessment, contentDescription = null) },
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

@Composable
private fun FilterChip(text: String, color: Color) {
    OutlinedButton(onClick = { }, modifier = Modifier.clip(RoundedCornerShape(20.dp))) {
        Box(modifier = Modifier.background(color.copy(alpha = 0.15f)).padding(horizontal = 12.dp, vertical = 6.dp)) {
            Text(text, color = color)
        }
    }
}

@Composable
private fun PriorityCard(text: String, color: Color) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.fillMaxWidth().background(color).padding(16.dp)) {
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

// Función para determinar el estado del stock
private fun getStockStatus(stock: Int): String {
    return when {
        stock <= 50 -> "Crítico"
        stock <= 100 -> "Bajo"
        else -> "Normal"
    }
}

// Función para determinar la categoría basada en el nombre
private fun getCategoryFromName(name: String): String {
    return when {
        name.contains("cemento", ignoreCase = true) || 
        name.contains("arena", ignoreCase = true) || 
        name.contains("ladrillo", ignoreCase = true) -> "Construcción"
        name.contains("varilla", ignoreCase = true) || 
        name.contains("malla", ignoreCase = true) || 
        name.contains("acero", ignoreCase = true) -> "Acero"
        name.contains("tubo", ignoreCase = true) || 
        name.contains("pvc", ignoreCase = true) -> "Tuberías"
        name.contains("cable", ignoreCase = true) || 
        name.contains("eléctrico", ignoreCase = true) -> "Eléctrico"
        name.contains("pintura", ignoreCase = true) -> "Pinturas"
        else -> "General"
    }
}

// Componente para mostrar cada material de Firebase
@Composable
private fun MaterialItemCard(material: MaterialItem) {
    val status = getStockStatus(material.cantidad)
    val category = getCategoryFromName(material.nombre)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = when (status) {
                "Crítico" -> MarvicOrange.copy(alpha = 0.1f)
                "Bajo" -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFF2A2A2A)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Inventory,
                contentDescription = null,
                tint = when (status) {
                    "Crítico" -> MarvicOrange
                    "Bajo" -> Color(0xFFFF9800)
                    else -> MarvicGreen
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = material.nombre,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${material.cantidad} unidades - ${material.ubicacion}",
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = category,
                    color = Color(0xFFBDBDBD),
                    fontSize = 11.sp
                )
                Text(
                    text = status,
                    color = when (status) {
                        "Crítico" -> MarvicOrange
                        "Bajo" -> Color(0xFFFF9800)
                        else -> MarvicGreen
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Componente para mostrar materiales en la sección de prioridades
@Composable
private fun MaterialPriorityCard(material: MaterialItem) {
    val status = getStockStatus(material.cantidad)
    val statusText = when (status) {
        "Crítico" -> "¡STOCK CRÍTICO!"
        "Bajo" -> "Stock Bajo"
        else -> "Stock Normal"
    }
    val statusColor = when (status) {
        "Crítico" -> MarvicOrange
        "Bajo" -> Color(0xFFFF9800)
        else -> MarvicGreen
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = statusColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${material.nombre}: $statusText",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${material.cantidad} unidades - ${material.ubicacion}",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}


