package com.proyecto.marvic.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
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
import com.proyecto.marvic.data.Project
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicGreen
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.viewmodel.ProjectViewModel
import com.proyecto.marvic.utils.PdfExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit,
    vm: ProjectViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isExporting by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var filterStatus by remember { mutableStateOf("TODOS") }
    
    LaunchedEffect(Unit) {
        vm.loadProjects()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Proyectos", color = Color.White) },
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
                                        pdfExporter.exportProjectsToPdf(vm.projects)
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
                        enabled = !isExporting && vm.projects.isNotEmpty()
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
                Icon(Icons.Default.Add, "Nuevo Proyecto", tint = Color.White)
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
                        vm.loadProjects()
                    } else {
                        vm.searchProjects(it)
                    }
                },
                label = { Text("Buscar proyecto") },
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
            
            // Filtros por estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChipProject(
                    label = "Todos",
                    selected = filterStatus == "TODOS",
                    onClick = {
                        filterStatus = "TODOS"
                        vm.loadProjects()
                    }
                )
                FilterChipProject(
                    label = "En Curso",
                    selected = filterStatus == "EN_CURSO",
                    onClick = {
                        filterStatus = "EN_CURSO"
                        vm.getProjectsByStatus("EN_CURSO")
                    }
                )
                FilterChipProject(
                    label = "Planificación",
                    selected = filterStatus == "PLANIFICACION",
                    onClick = {
                        filterStatus = "PLANIFICACION"
                        vm.getProjectsByStatus("PLANIFICACION")
                    }
                )
                FilterChipProject(
                    label = "Finalizados",
                    selected = filterStatus == "FINALIZADO",
                    onClick = {
                        filterStatus = "FINALIZADO"
                        vm.getProjectsByStatus("FINALIZADO")
                    }
                )
            }
            
            // Estadísticas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProjectStatCard(
                    title = "Total",
                    value = vm.projects.size.toString(),
                    icon = Icons.Default.Folder,
                    color = MarvicOrange
                )
                ProjectStatCard(
                    title = "Activos",
                    value = vm.projects.count { 
                        it.estado in listOf("EN_CURSO", "PLANIFICACION") 
                    }.toString(),
                    icon = Icons.Default.PlayArrow,
                    color = MarvicGreen
                )
                ProjectStatCard(
                    title = "Finalizados",
                    value = vm.projects.count { it.estado == "FINALIZADO" }.toString(),
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
            }
            
            // Lista de proyectos
            if (vm.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else if (vm.projects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay proyectos registrados",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
                        ) {
                            Text("Crear Primer Proyecto")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(vm.projects) { project ->
                        ProjectCard(
                            project = project,
                            onClick = { 
                                vm.selectProject(project)
                                selectedProject = project
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (showAddDialog) {
        AddProjectDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { project ->
                vm.createProject(project) { success ->
                    if (success) {
                        showAddDialog = false
                    }
                }
            }
        )
    }
    
    selectedProject?.let { project ->
        ProjectDetailDialog(
            project = project,
            vm = vm,
            onDismiss = { selectedProject = null },
            onDelete = {
                vm.deleteProject(project.id) { success ->
                    if (success) selectedProject = null
                }
            }
        )
    }
}

@Composable
fun ProjectCard(
    project: Project,
    onClick: () -> Unit
) {
    val statusColor = when (project.estado) {
        "EN_CURSO" -> MarvicGreen
        "PLANIFICACION" -> Color(0xFF2196F3)
        "FINALIZADO" -> Color(0xFF4CAF50)
        "PAUSADO" -> Color(0xFFFF9800)
        "CANCELADO" -> Color.Red
        else -> Color.Gray
    }
    
    val priorityColor = when (project.prioridad) {
        "URGENTE" -> Color.Red
        "ALTA" -> Color(0xFFFF5722)
        "MEDIA" -> Color(0xFF2196F3)
        "BAJA" -> Color.Gray
        else -> Color.Gray
    }
    
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = project.codigo,
                            fontSize = 12.sp,
                            color = MarvicOrange,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = priorityColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                project.prioridad,
                                color = priorityColor,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = project.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (project.cliente.isNotEmpty()) {
                        Text(
                            text = "Cliente: ${project.cliente}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
                
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        project.estado.replace("_", " "),
                        color = statusColor,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Barra de progreso
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Progreso",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        "${project.porcentajeAvance}%",
                        fontSize = 12.sp,
                        color = MarvicOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { project.porcentajeAvance / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MarvicOrange,
                    trackColor = Color(0xFF424242)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Presupuesto
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Presupuesto",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Text(
                        "S/. ${String.format("%.2f", project.presupuesto)}",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Gastado",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    val percentage = if (project.presupuesto > 0) {
                        (project.gastoReal / project.presupuesto) * 100
                    } else 0.0
                    val gastadoColor = when {
                        percentage > 100 -> Color.Red
                        percentage > 80 -> Color(0xFFFF9800)
                        else -> MarvicGreen
                    }
                    Text(
                        "S/. ${String.format("%.2f", project.gastoReal)}",
                        fontSize = 14.sp,
                        color = gastadoColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            if (project.responsable.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        project.responsable,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectStatCard(
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
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    title,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                Text(
                    value,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun FilterChipProject(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MarvicOrange,
            selectedLabelColor = Color.White,
            containerColor = Color(0xFF2A2A2A),
            labelColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (Project) -> Unit
) {
    var codigo by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var cliente by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var responsable by remember { mutableStateOf("") }
    var presupuesto by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("PLANIFICACION") }
    var prioridad by remember { mutableStateOf("MEDIA") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Proyecto") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = codigo,
                        onValueChange = { codigo = it.uppercase() },
                        label = { Text("Código") },
                        placeholder = { Text("PRY001") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Proyecto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = cliente,
                        onValueChange = { cliente = it },
                        label = { Text("Cliente") },
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
                        value = presupuesto,
                        onValueChange = { presupuesto = it },
                        label = { Text("Presupuesto (S/.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        Project(
                            codigo = codigo,
                            nombre = nombre,
                            cliente = cliente,
                            ubicacion = ubicacion,
                            responsable = responsable,
                            estado = estado,
                            prioridad = prioridad,
                            presupuesto = presupuesto.toDoubleOrNull() ?: 0.0,
                            fechaInicio = System.currentTimeMillis()
                        )
                    )
                },
                enabled = codigo.isNotEmpty() && nombre.isNotEmpty()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailDialog(
    project: Project,
    vm: ProjectViewModel,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Column {
                Text(project.codigo, fontSize = 14.sp, color = MarvicOrange)
                Text(project.nombre)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    if (project.cliente.isNotEmpty()) ProjectDetailRow("Cliente", project.cliente)
                    ProjectDetailRow("Estado", project.estado.replace("_", " "))
                    ProjectDetailRow("Prioridad", project.prioridad)
                    ProjectDetailRow("Responsable", project.responsable)
                    ProjectDetailRow("Ubicación", project.ubicacion)
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Financiero", fontWeight = FontWeight.Bold)
                    ProjectDetailRow("Presupuesto", "S/. ${String.format("%.2f", project.presupuesto)}")
                    ProjectDetailRow("Gastado", "S/. ${String.format("%.2f", project.gastoReal)}")
                    
                    val diferencia = project.presupuesto - project.gastoReal
                    val diferenciaColor = if (diferencia >= 0) MarvicGreen else Color.Red
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Diferencia", fontWeight = FontWeight.Bold)
                        Text(
                            "S/. ${String.format("%.2f", diferencia)}",
                            color = diferenciaColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    Text("Progreso", fontWeight = FontWeight.Bold)
                    ProjectDetailRow("Avance", "${project.porcentajeAvance}%")
                    
                    LinearProgressIndicator(
                        progress = { project.porcentajeAvance / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MarvicOrange
                    )
                }
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
            text = { Text("¿Estás seguro de eliminar este proyecto?") },
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
private fun ProjectDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(value, fontSize = 14.sp)
    }
}
