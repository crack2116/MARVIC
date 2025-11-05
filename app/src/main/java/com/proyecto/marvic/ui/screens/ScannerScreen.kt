package com.proyecto.marvic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Close
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
import com.proyecto.marvic.camera.RealBarcodeScanner
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.InventoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScannerScreen(
    onBack: () -> Unit,
    onScanResult: (String) -> Unit,
    vm: InventoryViewModel = viewModel()
) {
    var scannedCode by remember { mutableStateOf("") }
    var showManualEntry by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isScanning by remember { mutableStateOf(true) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escáner de Materiales", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color(0xFF1A1A1A)),
                actions = {
                    IconButton(onClick = { showManualEntry = !showManualEntry }) {
                        Icon(
                            if (showManualEntry) Icons.Default.QrCodeScanner else Icons.Default.Edit,
                            contentDescription = if (showManualEntry) "Escáner" else "Manual",
                            tint = Color.White
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
        ) {
            if (!showManualEntry) {
                // Escáner real con cámara
                RealBarcodeScanner(
                    onBarcodeScanned = { barcode ->
                        scannedCode = barcode
                        onScanResult(barcode)
                    },
                    onError = { error ->
                        errorMessage = "Error del escáner: $error"
                        isScanning = false
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Entrada manual
                ManualCodeEntry(
                    onCodeEntered = { code ->
                        scannedCode = code
                        onScanResult(code)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Resultado del escaneo/entrada
            if (scannedCode.isNotEmpty()) {
                ScanResultCard(
                    _code = scannedCode,
                    onScanAnother = {
                        scannedCode = ""
                        showManualEntry = false
                    },
                    onContinue = onBack
                )
            }
            
            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        IconButton(
                            onClick = { errorMessage = "" }
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ManualCodeEntry(
    onCodeEntered: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var manualCode by remember { mutableStateOf("") }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MarvicOrange.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MarvicOrange,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Entrada Manual de Código",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ingresa manualmente el código del material",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }
        }
        
        // Formulario
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Código del Material",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextField(
                    value = manualCode,
                    onValueChange = { manualCode = it.uppercase() },
                    label = { Text("Ej: MAT001, MAT002...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Códigos de ejemplo
                Text(
                    text = "Códigos disponibles:",
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(listOf("MAT001", "MAT002", "MAT003", "MAT004", "MAT005")) { code ->
                        Card(
                            onClick = { manualCode = code },
                            modifier = Modifier.padding(4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF424242))
                        ) {
                            Text(
                                text = code,
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
                
                Button(
                    onClick = { 
                        if (manualCode.isNotEmpty()) {
                            onCodeEntered(manualCode.trim())
                            manualCode = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = manualCode.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MarvicOrange,
                        disabledContainerColor = Color(0xFF424242)
                    )
                ) {
                    Text("Buscar Material", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "💡 Tip",
                    color = MarvicOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Los códigos de materiales siguen el formato MAT### donde ### es un número de 3 dígitos.",
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
                Text(
                    text = "Ejemplos: MAT001 (Cemento), MAT002 (Acero), MAT003 (Arena)...",
                    color = Color(0xFFBDBDBD),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun ScanResultCard(
    _code: String, // Renombrado para evitar warning
    onScanAnother: () -> Unit,
    onContinue: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicGreen.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MarvicGreen,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "¡Código Detectado!",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Código escaneado:",
                        color = Color(0xFFBDBDBD),
                        fontSize = 12.sp
                    )
                    Text(
                        text = _code,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onScanAnother,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF424242))
                ) {
                    Text("Escanear Otro")
                }
                
                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MarvicGreen)
                ) {
                    Text("Continuar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE74C3C).copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
                tint = Color(0xFFE74C3C),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }
    }
}