package com.proyecto.marvic.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.proyecto.marvic.ui.theme.MarvicOrange
import java.io.File

@Composable
fun ImagePicker(
    currentImageUrl: String? = null,
    onImageSelected: (Uri) -> Unit,
    onImageRemoved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { onImageSelected(it) }
        }
    }
    
    // Crear archivo temporal para la foto
    fun createTempImageFile(): Uri {
        val tempFile = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, if (currentImageUrl != null) MarvicOrange else Color.Gray, RoundedCornerShape(12.dp))
            .background(Color(0xFF2A2A2A))
            .clickable { showDialog = true },
        contentAlignment = Alignment.Center
    ) {
        if (currentImageUrl != null) {
            // Mostrar imagen actual
            AsyncImage(
                model = currentImageUrl,
                contentDescription = "Imagen del material",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Botón para eliminar
            IconButton(
                onClick = onImageRemoved,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Eliminar imagen",
                    tint = Color.White
                )
            }
        } else {
            // Mostrar placeholder
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = "Agregar foto",
                    modifier = Modifier.size(48.dp),
                    tint = Color.Gray
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Toca para agregar foto",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }
    
    // Dialog para elegir fuente
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Seleccionar imagen") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            val uri = createTempImageFile()
                            tempImageUri = uri
                            cameraLauncher.launch(uri)
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Tomar foto")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch("image/*")
                            showDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Elegir de galería")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}





