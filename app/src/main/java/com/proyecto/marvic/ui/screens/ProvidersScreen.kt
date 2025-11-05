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
import com.proyecto.marvic.data.Provider
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.ProviderViewModel
import com.proyecto.marvic.utils.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(
    onBack: () -> Unit,
    vm: ProviderViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<Provider?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        vm.loadProviders()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Proveedores", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                actions = {
                    IconButton(
                        onClick = {
                            isExporting = true
                            scope.launch {
                                try {
                                    val pdfExporter = PdfExporter(context)
                                    val result = withContext(Dispatchers.IO) {
                                        pdfExporter.exportProvidersToPdf(vm.providers)
                                    }
                                    result.onSuccess { filePath ->
                                        Toast.makeText(context, "PDF guardado en:\n$filePath", Toast.LENGTH_LONG).show()
                                    }.onFailure { error ->
                                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                } finally {
                                    isExporting = false
                                }
                            }
                        },
                        enabled = !isExporting && vm.providers.isNotEmpty()
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
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "Agregar", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MarvicOrange
            ) {
                Icon(Icons.Default.Add, "Agregar Proveedor", tint = Color.White)
            }
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Barra de búsqueda
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    if (it.isEmpty()) {
                        vm.loadProviders()
                    } else {
                        vm.searchProviders(it)
                    }
                },
                label = { Text("Buscar proveedor") },
                leadingIcon = {
                    Icon(Icons.Default.Search, "Buscar")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MarvicOrange,
                    unfocusedBorderColor = Color.Gray
                )
            )
            
            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatCard(
                    title = "Total",
                    value = vm.providers.size.toString(),
                    icon = Icons.Default.Business,
                    color = MarvicOrange
                )
                StatCard(
                    title = "Activos",
                    value = vm.providers.count { it.activo }.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = MarvicGreen
                )
            }
            
            // Lista de proveedores
            if (vm.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else if (vm.providers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.BusinessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay proveedores registrados",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
                        ) {
                            Text("Agregar Primer Proveedor")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vm.providers) { provider ->
                        ProviderCard(
                            provider = provider,
                            onClick = { selectedProvider = provider }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddDialog) {
        AddProviderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { provider ->
                vm.createProvider(provider) { success ->
                    if (success) {
                        showAddDialog = false
                    }
                }
            }
        )
    }
    
    selectedProvider?.let { provider ->
        ProviderDetailDialog(
            provider = provider,
            onDismiss = { selectedProvider = null },
            onDelete = {
                vm.deleteProvider(provider.id) { success ->
                    if (success) selectedProvider = null
                }
            },
            onEdit = { updatedProvider ->
                vm.updateProvider(updatedProvider) { success ->
                    if (success) selectedProvider = null
                }
            }
        )
    }
}

@Composable
fun ProviderCard(
    provider: Provider,
    onClick: () -> Unit
) {
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
                        text = provider.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "RUC: ${provider.ruc}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                if (provider.activo) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MarvicGreen.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Activo",
                            color = MarvicGreen,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Red.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "Inactivo",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MarvicOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${provider.numeroCompras} compras",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        String.format("%.1f", provider.calificacion),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
            
            if (provider.categorias.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    provider.categorias.take(3).forEach { categoria ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF424242)
                        ) {
                            Text(
                                categoria,
                                color = Color.White,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MarvicCard)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    title,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    value,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProviderDialog(
    onDismiss: () -> Unit,
    onConfirm: (Provider) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var razonSocial by remember { mutableStateOf("") }
    var ruc by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Proveedor") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = razonSocial,
                    onValueChange = { razonSocial = it },
                    label = { Text("Razón Social") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ruc,
                    onValueChange = { ruc = it },
                    label = { Text("RUC") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Teléfono") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Provider(
                            nombre = nombre,
                            razonSocial = razonSocial,
                            ruc = ruc,
                            telefono = telefono,
                            email = email,
                            activo = true
                        )
                    )
                },
                enabled = nombre.isNotEmpty() && ruc.isNotEmpty()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailDialog(
    provider: Provider,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (Provider) -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(provider.nombre) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DetailRow("Razón Social", provider.razonSocial)
                DetailRow("RUC", provider.ruc)
                DetailRow("Teléfono", provider.telefono)
                DetailRow("Email", provider.email)
                DetailRow("Contacto", provider.contactoPrincipal)
                DetailRow("Compras", "${provider.numeroCompras}")
                DetailRow("Total Comprado", "S/. ${String.format("%.2f", provider.totalCompras)}")
                DetailRow("Calificación", "${provider.calificacion} ⭐")
                DetailRow("Estado", if (provider.activo) "Activo" else "Inactivo")
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                }
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        }
    )
    
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Confirmar Eliminación") },
            text = { Text("¿Estás seguro de eliminar este proveedor?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            value,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

