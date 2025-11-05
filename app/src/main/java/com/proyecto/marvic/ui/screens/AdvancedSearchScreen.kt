package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
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
import com.proyecto.marvic.viewmodel.InventoryViewModel
import com.proyecto.marvic.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(onBack: () -> Unit, vm: InventoryViewModel = viewModel()) {
    val query = remember { mutableStateOf("") }
    val selectedCategory = remember { mutableStateOf("Todas") }
    val searchByCode = remember { mutableStateOf(false) }
    val recentSearches = remember { mutableStateListOf("cemento", "fierro", "tubería", "madera") }

    val categories = listOf(
        "Todas", "Cementos", "Aceros", "Tuberías", "Maderas", 
        "Pinturas", "Eléctricos", "Sanitarios", "Herramientas"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Búsqueda Avanzada", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A1A1A)),
                actions = {
                    IconButton(onClick = { searchByCode.value = !searchByCode.value }) {
                        Icon(
                            Icons.Default.QrCode,
                            contentDescription = "Buscar por código",
                            tint = if (searchByCode.value) MarvicOrange else Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Barra de búsqueda mejorada
            OutlinedTextField(
                value = query.value,
                onValueChange = {
                    query.value = it
                    vm.search(it)
                },
                label = { 
                    Text(
                        if (searchByCode.value) "Buscar por código..." else "Buscar material...", 
                        color = Color(0xFFBDBDBD)
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        if (searchByCode.value) Icons.Default.QrCode else Icons.Default.Search, 
                        contentDescription = null, 
                        tint = Color.White
                    ) 
                },
                trailingIcon = {
                    if (query.value.isNotEmpty()) {
                        IconButton(onClick = { query.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = Color.White)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                    // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
            )

            // Filtros por categoría
            Text("Filtrar por categoría:", color = Color.White, fontWeight = FontWeight.SemiBold)
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory.value == category,
                        onClick = { selectedCategory.value = category },
                        label = { Text(category, color = Color.White) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MarvicOrange,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF2A2A2A),
                            labelColor = Color.White
                        )
                    )
                }
            }

            // Búsquedas recientes
            if (recentSearches.isNotEmpty()) {
                Text("Búsquedas recientes:", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentSearches) { search ->
                        SuggestionChip(
                            onClick = { 
                                query.value = search
                                vm.search(search)
                            },
                            label = { Text(search, color = Color.White) },
                            icon = {
                                Icon(Icons.Default.History, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = Color(0xFF2A2A2A),
                                labelColor = Color.White,
                                iconContentColor = Color.White
                            )
                        )
                    }
                }
            }

            // Resultados
            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else if (vm.items.isNotEmpty()) {
                Text(
                    "Resultados encontrados (${vm.items.size})", 
                    color = Color(0xFFBDBDBD), 
                    fontSize = 14.sp
                )
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.items) { item ->
                        MaterialCard(
                            item = item,
                            onClick = { /* TODO: Ver detalles */ }
                        )
                    }
                }
            } else if (!vm.isLoading && query.value.isNotBlank()) {
                Text("No se encontraron resultados para \"${query.value}\"", color = Color(0xFFBDBDBD))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialCard(
    item: com.proyecto.marvic.data.MaterialItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Inventory, 
                    contentDescription = null, 
                    tint = MarvicOrange, 
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        item.nombre, 
                        color = Color.White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(16.dp))
                        Text(item.ubicacion, color = Color(0xFF9E9E9E), fontSize = 12.sp)
                    }
                    if (item.fechaCreacion > 0L) {
                        Text(
                            "Creado: ${DateUtils.formatDateShort(item.fechaCreacion)}", 
                            color = Color(0xFF757575), 
                            fontSize = 10.sp
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${item.cantidad}", 
                        color = if (item.cantidad < 50) Color(0xFFE74C3C) else MarvicGreen, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 24.sp
                    )
                    Text("unidades", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                }
            }
            
            // Información adicional
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("ID: ${item.id}", color = Color(0xFF757575), fontSize = 10.sp)
                if (item.cantidad < 50) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE74C3C), modifier = Modifier.size(12.dp))
                        Text("Stock bajo", color = Color(0xFFE74C3C), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}
