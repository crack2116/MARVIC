package com.proyecto.marvic.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.PictureAsPdf
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
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.ReportsViewModel
import com.proyecto.marvic.utils.DateUtils
import com.proyecto.marvic.utils.PdfExporter
import com.proyecto.marvic.data.Movement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(onGoToExecutive: () -> Unit, vm: ReportsViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) { vm.load() }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        TopAppBar(
            title = { Text("Reportes de Movimientos", fontWeight = FontWeight.SemiBold) },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color(0xFF1A1A1A),
                titleContentColor = Color.White
            ),
            actions = {
                IconButton(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            try {
                                val pdfExporter = PdfExporter(context)
                                val movements = vm.items.map { item ->
                                    Movement(
                                        id = "",
                                        materialId = item.materialId,
                                        delta = item.delta,
                                        timestamp = item.timestamp,
                                        userId = "Usuario"
                                    )
                                }
                                val result = withContext(Dispatchers.IO) {
                                    pdfExporter.exportMovementsToPdf(movements)
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
                    enabled = !isExporting && vm.items.isNotEmpty()
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.PictureAsPdf, "Exportar PDF", tint = Color.White)
                    }
                }
                Button(
                    onClick = onGoToExecutive,
                    colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange),
                    modifier = Modifier.height(32.dp).padding(end = 8.dp)
                ) {
                    Text("Reportes Ejecutivos", color = Color.White, fontSize = 12.sp)
                }
            }
        )
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Cards de estadísticas
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    title = "Entradas",
                    value = vm.items.count { it.delta > 0 }.toString(),
                    color = MarvicGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Salidas",
                    value = vm.items.count { it.delta < 0 }.toString(),
                    color = Color(0xFFE74C3C),
                    modifier = Modifier.weight(1f)
                )
            }

            Text("Movimientos Recientes", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            if (vm.items.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(vm.items) { m ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MarvicCard)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (m.delta > 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                    contentDescription = null,
                                    tint = if (m.delta > 0) MarvicGreen else Color(0xFFE74C3C),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(Modifier.size(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(m.materialId, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    Text(DateUtils.formatDate(m.timestamp), color = Color(0xFF9E9E9E), fontSize = 12.sp)
                                }
                                Text(
                                    "${if (m.delta > 0) "+" else ""}${m.delta}",
                                    color = if (m.delta > 0) MarvicGreen else Color(0xFFE74C3C),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 32.sp)
            Text(title, color = color.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}