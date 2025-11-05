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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
fun SearchScreen(
    onBack: () -> Unit,
    onGoToGallery: (String) -> Unit = {},
    vm: InventoryViewModel = viewModel()
) {
    val query = remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        TopAppBar(
            title = { Text("Buscar Material", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
            },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Barra de búsqueda
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = query.value,
                    onValueChange = { query.value = it },
                    label = { Text("Buscar por nombre o código") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Button(
                    onClick = { vm.search(query.value) },
                    modifier = Modifier.height(56.dp)
                ) {
                    Text("Buscar")
                }
            }

            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            }

            // Resultados
            if (vm.items.isNotEmpty()) {
                Text("Resultados encontrados (${vm.items.size})", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MarvicCard)
                        ) {
                            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = MarvicOrange, modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.size(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.nombre, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF9E9E9E), modifier = Modifier.size(16.dp))
                                            Text(item.ubicacion, color = Color(0xFF9E9E9E), fontSize = 12.sp)
                                        }
                                        if (item.fechaCreacion > 0L) {
                                            Text("Creado: ${DateUtils.formatDateShort(item.fechaCreacion)}", color = Color(0xFF757575), fontSize = 10.sp)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("${item.cantidad}", color = if (item.cantidad < 50) Color(0xFFE74C3C) else MarvicGreen, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                                        Text("unidades", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onGoToGallery(item.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.size(8.dp))
                                    Text("Ver Galería")
                                }
                            }
                        }
                    }
                }
            } else if (!vm.isLoading && query.value.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("No se encontraron resultados", color = Color(0xFF9E9E9E))
                }
            }
        }
    }
}