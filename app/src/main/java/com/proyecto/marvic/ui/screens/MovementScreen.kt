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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.navigation.NavController
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import kotlinx.coroutines.launch
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovementScreen(
    onBack: () -> Unit, 
    onGoToScanner: () -> Unit, 
    navController: NavController,
    vm: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    val material = remember { mutableStateOf("") }
    val cantidad = remember { mutableStateOf("1") }
    val ubicacion = remember { mutableStateOf("") }
    val proyecto = remember { mutableStateOf("") }
    val tipoMovimiento = remember { mutableStateOf("Entrada") }
    var showScanSuccess by remember { mutableStateOf(false) }
    
    // Leer el código escaneado del savedStateHandle
    LaunchedEffect(Unit) {
        println("📲 MovementScreen: Iniciando observación del savedStateHandle")
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<String>("scannedCode")?.observeForever { code ->
            println("📲 MovementScreen: Código recibido del savedStateHandle: $code")
            if (code != null && code.isNotEmpty()) {
                println("📲 MovementScreen: Aplicando código al campo material: $code")
                material.value = code
                showScanSuccess = true
                // Limpiar el savedStateHandle después de leerlo
                navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedCode")
                
                // Ocultar el mensaje después de 3 segundos
                kotlinx.coroutines.GlobalScope.launch {
                    kotlinx.coroutines.delay(3000)
                    showScanSuccess = false
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF121212))) {
        TopAppBar(
            title = { Text("Movimiento de Inventario", fontWeight = FontWeight.SemiBold) },
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
        
        Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Botón de escanear código
            OutlinedButton(
                onClick = { 
                    onGoToScanner()
                },
                modifier = Modifier.fillMaxWidth().height(80.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = MarvicOrange.copy(alpha = 0.9f))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("ESCANEAR CÓDIGO QR / CÓDIGO DE BARRAS", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            
            // Mensaje de éxito del escaneo
            if (showScanSuccess) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MarvicGreen.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MarvicGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                "✓ Código escaneado exitosamente",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "Material: ${material.value}",
                                color = Color(0xFFBDBDBD),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Tipo de movimiento
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { tipoMovimiento.value = "Entrada" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tipoMovimiento.value == "Entrada") MarvicGreen else Color(0xFF1E1E1E)
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Text("ENTRADA", color = Color.White)
                }
                OutlinedButton(
                    onClick = { tipoMovimiento.value = "Salida" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (tipoMovimiento.value == "Salida") Color(0xFFE74C3C) else Color(0xFF1E1E1E)
                    )
                ) {
                    Icon(Icons.Default.Remove, contentDescription = null, tint = Color.White)
                    Text("SALIDA", color = Color.White)
                }
            }

            Text("Detalles del Movimiento", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = material.value,
                onValueChange = { material.value = it },
                label = { Text("Material", color = Color(0xFFBDBDBD)) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = cantidad.value,
                onValueChange = { cantidad.value = it },
                label = { Text("Cantidad", color = Color(0xFFBDBDBD)) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = ubicacion.value,
                onValueChange = { ubicacion.value = it },
                label = { Text("Ubicación", color = Color(0xFFBDBDBD)) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = proyecto.value,
                onValueChange = { proyecto.value = it },
                label = { Text("Proyecto / Destino", color = Color(0xFFBDBDBD)) },
                textStyle = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.weight(1f))

            if (vm.isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            }

            Button(
                onClick = {
                    if (material.value.isEmpty()) {
                        // Mostrar error si no hay material
                        return@Button
                    }
                    
                    val delta = if (tipoMovimiento.value == "Entrada") 
                        cantidad.value.toIntOrNull() ?: 0 
                    else 
                        -(cantidad.value.toIntOrNull() ?: 0)
                    
                    // Usar el código escaneado o el nombre del material
                    vm.move(material.value, delta, { success ->
                        if (success) {
                            material.value = ""
                            cantidad.value = "1"
                            ubicacion.value = ""
                            proyecto.value = ""
                            onBack()
                        }
                    }, context)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = material.value.isNotEmpty() && cantidad.value.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MarvicGreen,
                    disabledContainerColor = Color(0xFF424242)
                )
            ) {
                Text("CONFIRMAR TRANSACCIÓN", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}