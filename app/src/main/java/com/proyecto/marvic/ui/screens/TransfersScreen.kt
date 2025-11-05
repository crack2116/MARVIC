package com.proyecto.marvic.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.proyecto.marvic.data.Transfer
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.TransferViewModel
import com.proyecto.marvic.viewmodel.InventoryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransfersScreen(
    onBack: () -> Unit,
    vm: TransferViewModel = viewModel(),
    inventoryVm: InventoryViewModel = viewModel()
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTransfer by remember { mutableStateOf<Transfer?>(null) }
    
    LaunchedEffect(Unit) {
        vm.loadTransfers()
        inventoryVm.refreshTotals()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transferencias", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Nueva Transferencia", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MarvicOrange
            ) {
                Icon(Icons.Default.SwapHoriz, "Nueva Transferencia", tint = Color.White)
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TransferStatCard(
                    title = "Total",
                    value = vm.transfers.size.toString(),
                    icon = Icons.Default.SwapHoriz,
                    color = MarvicOrange,
                    modifier = Modifier.weight(1f)
                )
                TransferStatCard(
                    title = "Completadas",
                    value = vm.transfers.count { it.estado == "COMPLETADA" }.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MarvicGreen,
                    modifier = Modifier.weight(1f)
                )
                TransferStatCard(
                    title = "Pendientes",
                    value = vm.transfers.count { it.estado == "PENDIENTE" }.toString(),
                    icon = Icons.Default.HourglassEmpty,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Lista de transferencias
            if (vm.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else if (vm.transfers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay transferencias registradas",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
                        ) {
                            Text("Crear Primera Transferencia")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vm.transfers) { transfer ->
                        TransferCard(
                            transfer = transfer,
                            onClick = { selectedTransfer = transfer }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddDialog) {
        AddTransferDialog(
            materials = inventoryVm.allMaterials,
            onDismiss = { showAddDialog = false },
            onConfirm = { materialId, materialNombre, cantidad, origen, destino, responsable, motivo, notas ->
                vm.createTransfer(
                    materialId = materialId,
                    materialNombre = materialNombre,
                    cantidad = cantidad,
                    origen = origen,
                    destino = destino,
                    responsable = responsable,
                    motivo = motivo,
                    notas = notas
                ) { success, message ->
                    if (success) {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        showAddDialog = false
                    } else {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
    
    selectedTransfer?.let { transfer ->
        TransferDetailDialog(
            transfer = transfer,
            onDismiss = { selectedTransfer = null }
        )
    }
}

@Composable
fun TransferCard(
    transfer: Transfer,
    onClick: () -> Unit
) {
    val statusColor = when (transfer.estado) {
        "COMPLETADA" -> MarvicGreen
        "EN_TRANSITO" -> Color(0xFF2196F3)
        "PENDIENTE" -> Color(0xFFFF9800)
        "CANCELADA" -> Color.Red
        else -> Color.Gray
    }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transfer.materialNombre,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Cantidad: ${transfer.cantidad}",
                        fontSize = 14.sp,
                        color = MarvicOrange,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        transfer.estado.replace("_", " "),
                        color = statusColor,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Ruta de transferencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "ORIGEN",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        transfer.origenAlmacen,
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MarvicOrange,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(
                        "DESTINO",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        transfer.destinoAlmacen,
                        fontSize = 13.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Divider(color = Color(0xFF424242))
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        transfer.responsable,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                Text(
                    dateFormat.format(Date(transfer.fechaSolicitud)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun TransferStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                value,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                title,
                color = Color.Gray,
                fontSize = 11.sp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransferDialog(
    materials: List<com.proyecto.marvic.data.MaterialItem>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int, String, String, String, String, String) -> Unit
) {
    var selectedMaterialIndex by remember { mutableStateOf(0) }
    var cantidad by remember { mutableStateOf("") }
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var notas by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    val almacenes = listOf("Almacén 1", "Almacén 2", "Almacén 3", "Almacén 4", "Patio Exterior")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva Transferencia") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // Selector de material
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = if (materials.isNotEmpty()) materials[selectedMaterialIndex].nombre else "Sin materiales",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Material") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            colors = OutlinedTextFieldDefaults.colors()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            materials.forEachIndexed { index, material ->
                                DropdownMenuItem(
                                    text = { Text("${material.nombre} (Stock: ${material.cantidad})") },
                                    onClick = {
                                        selectedMaterialIndex = index
                                        expanded = false
                                        origen = material.ubicacion
                                    }
                                )
                            }
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = cantidad,
                        onValueChange = { cantidad = it },
                        label = { Text("Cantidad") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = origen,
                        onValueChange = { origen = it },
                        label = { Text("Almacén Origen") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = destino,
                        onValueChange = { destino = it },
                        label = { Text("Almacén Destino") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = responsable,
                        onValueChange = { responsable = it },
                        label = { Text("Responsable") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = notas,
                        onValueChange = { notas = it },
                        label = { Text("Notas (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (materials.isNotEmpty()) {
                        val material = materials[selectedMaterialIndex]
                        onConfirm(
                            material.id,
                            material.nombre,
                            cantidad.toIntOrNull() ?: 0,
                            origen,
                            destino,
                            responsable,
                            motivo,
                            notas
                        )
                    }
                },
                enabled = cantidad.toIntOrNull() != null && cantidad.toInt() > 0 && 
                         origen.isNotEmpty() && destino.isNotEmpty() && responsable.isNotEmpty()
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun TransferDetailDialog(
    transfer: Transfer,
    onDismiss: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text("Detalles de Transferencia") 
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    TransferDetailRow("Material", transfer.materialNombre)
                    TransferDetailRow("Cantidad", transfer.cantidad.toString())
                    TransferDetailRow("Estado", transfer.estado.replace("_", " "))
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Ruta", fontWeight = FontWeight.Bold)
                    TransferDetailRow("Origen", transfer.origenAlmacen)
                    TransferDetailRow("Destino", transfer.destinoAlmacen)
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Información", fontWeight = FontWeight.Bold)
                    TransferDetailRow("Responsable", transfer.responsable)
                    TransferDetailRow("Motivo", transfer.motivo)
                    if (transfer.notas.isNotEmpty()) {
                        TransferDetailRow("Notas", transfer.notas)
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Fechas", fontWeight = FontWeight.Bold)
                    TransferDetailRow("Solicitud", dateFormat.format(Date(transfer.fechaSolicitud)))
                    transfer.fechaRecepcion?.let {
                        TransferDetailRow("Recepción", dateFormat.format(Date(it)))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
private fun TransferDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(value, fontSize = 14.sp)
    }
}



