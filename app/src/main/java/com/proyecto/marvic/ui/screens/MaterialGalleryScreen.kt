package com.proyecto.marvic.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.proyecto.marvic.data.MaterialItem
import com.proyecto.marvic.ui.components.ImagePicker
import com.proyecto.marvic.ui.theme.MarvicCard
import com.proyecto.marvic.ui.theme.MarvicOrange
import com.proyecto.marvic.utils.ImageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialGalleryScreen(
    material: MaterialItem,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageManager = remember { ImageManager(context) }
    
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImage by remember { mutableStateOf<String?>(null) }
    
    // Cargar imágenes existentes
    LaunchedEffect(material.id) {
        isLoading = true
        val result = imageManager.listImages(material.id)
        result.onSuccess { urls ->
            images = urls
        }.onFailure { error ->
            Toast.makeText(context, "Error cargando imágenes: ${error.message}", Toast.LENGTH_SHORT).show()
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Galería", color = Color.White, fontSize = 18.sp)
                        Text(material.nombre, color = Color.Gray, fontSize = 14.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1A1A1A)
                ),
                actions = {
                    Text(
                        "${images.size} fotos",
                        color = MarvicOrange,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Picker para agregar nueva imagen
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MarvicCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Agregar Nueva Foto",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    
                    ImagePicker(
                        onImageSelected = { uri ->
                            scope.launch {
                                isUploading = true
                                try {
                                    // Comprimir imagen
                                    val compressedData = withContext(Dispatchers.IO) {
                                        imageManager.compressImage(uri)
                                    }
                                    
                                    if (compressedData != null) {
                                        // Subir a Firebase
                                        val result = imageManager.uploadImage(
                                            compressedData,
                                            material.id
                                        ) { progress ->
                                            uploadProgress = progress
                                        }
                                        
                                        result.onSuccess { url ->
                                            images = images + url
                                            Toast.makeText(context, "Foto agregada exitosamente", Toast.LENGTH_SHORT).show()
                                        }.onFailure { error ->
                                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Error al comprimir imagen", Toast.LENGTH_SHORT).show()
                                    }
                                } finally {
                                    isUploading = false
                                    uploadProgress = 0
                                }
                            }
                        },
                        modifier = Modifier.height(180.dp)
                    )
                    
                    if (isUploading) {
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MarvicOrange
                        )
                        Text(
                            "Subiendo... $uploadProgress%",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Galería de imágenes
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MarvicOrange)
                }
            } else if (images.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "No hay fotos aún",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Usa el selector de arriba para agregar",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(images) { imageUrl ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2A2A2A))
                                .clickable {
                                    selectedImage = imageUrl
                                    showImageDialog = true
                                }
                        ) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = "Foto del material",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Dialog para ver imagen en grande y eliminar
    if (showImageDialog && selectedImage != null) {
        AlertDialog(
            onDismissRequest = { 
                showImageDialog = false
                selectedImage = null
            },
            title = { Text("Foto del Material") },
            text = {
                Column {
                    AsyncImage(
                        model = selectedImage,
                        contentDescription = "Foto del material",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            scope.launch {
                                selectedImage?.let { url ->
                                    val result = imageManager.deleteImage(url)
                                    result.onSuccess {
                                        images = images.filter { it != url }
                                        Toast.makeText(context, "Foto eliminada", Toast.LENGTH_SHORT).show()
                                        showImageDialog = false
                                        selectedImage = null
                                    }.onFailure { error ->
                                        Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, "Eliminar", tint = Color.Red)
                    }
                    TextButton(onClick = { 
                        showImageDialog = false
                        selectedImage = null
                    }) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }
}



