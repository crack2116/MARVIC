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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proyecto.marvic.notifications.SmartNotificationManager
import com.proyecto.marvic.notifications.StockMonitor
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(onBack: () -> Unit) {
    val criticalThreshold = remember { mutableStateOf(20) }
    val warningThreshold = remember { mutableStateOf(50) }
    val reminderThreshold = remember { mutableStateOf(100) }
    val notificationsEnabled = remember { mutableStateOf(true) }
    val vibrationEnabled = remember { mutableStateOf(true) }
    val soundEnabled = remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración de Notificaciones", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A1A1A))
            )
        },
        containerColor = Color(0xFF121212)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Configuración general
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
                        Column {
                            Text(
                                text = "Notificaciones",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Recibir alertas de stock",
                                color = Color(0xFFBDBDBD),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = notificationsEnabled.value,
                            onCheckedChange = { notificationsEnabled.value = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MarvicOrange,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFF424242)
                            )
                        )
                    }
                }
            }
            
            if (notificationsEnabled.value) {
                // Configuración de vibración
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
                            Column {
                                Text(
                                    text = "Vibración",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Vibrar en notificaciones críticas",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = vibrationEnabled.value,
                                onCheckedChange = { vibrationEnabled.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MarvicOrange,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFF424242)
                                )
                            )
                        }
                    }
                }
                
                // Configuración de sonido
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
                            Column {
                                Text(
                                    text = "Sonido",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Reproducir sonido en notificaciones",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            }
                            Switch(
                                checked = soundEnabled.value,
                                onCheckedChange = { soundEnabled.value = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = MarvicOrange,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFF424242)
                                )
                            )
                        }
                    }
                }
                
                // Umbrales de stock
                Text(
                    text = "Umbrales de Stock",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Umbral crítico
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C).copy(alpha = 0.1f))
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
                                    text = "Stock Crítico",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Notificación urgente cuando el stock está muy bajo",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            }
                            OutlinedTextField(
                                value = criticalThreshold.value.toString(),
                                onValueChange = { 
                                    criticalThreshold.value = it.toIntOrNull() ?: 20
                                },
                                modifier = Modifier.width(80.dp),
                                // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                            )
                        }
                    }
                }
                
                // Umbral de advertencia
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
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
                                    text = "Stock Bajo",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Advertencia cuando el stock está bajo",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            }
                            OutlinedTextField(
                                value = warningThreshold.value.toString(),
                                onValueChange = { 
                                    warningThreshold.value = it.toIntOrNull() ?: 50
                                },
                                modifier = Modifier.width(80.dp),
                                // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                            )
                        }
                    }
                }
                
                // Umbral de recordatorio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
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
                                    text = "Recordatorio",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Recordatorio para programar reposición",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 12.sp
                                )
                            }
                            OutlinedTextField(
                                value = reminderThreshold.value.toString(),
                                onValueChange = { 
                                    reminderThreshold.value = it.toIntOrNull() ?: 100
                                },
                                modifier = Modifier.width(80.dp),
                                // colors = androidx.compose.material3.TextFieldDefaults.colors(...) // No disponible en versiones antiguas
                            )
                        }
                    }
                }
                
                // Botón de prueba
                Button(
                    onClick = { /* TODO: Implementar prueba de notificación */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Probar Notificación", color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                // Estado del sistema
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MarvicCard)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Estado del Sistema",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val status = StockMonitor.getNotificationStatus()
                        status.forEach { (key, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = key.toString().replaceFirstChar { it.uppercase() },
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = value.toString(),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

