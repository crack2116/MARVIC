package com.proyecto.marvic.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.proyecto.marvic.ui.theme.MarvicOrange
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun RealBarcodeScanner(
    onBarcodeScanned: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
            if (!granted) {
                onError("Se requiere permiso de cámara para escanear códigos")
            }
        }
    )
    
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    if (!hasCameraPermission) {
        // Mostrar mensaje de permiso
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "Permiso de Cámara Requerido",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Se necesita acceso a la cámara para escanear códigos QR y de barras",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
                Button(
                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = MarvicOrange)
                ) {
                    Text("Conceder Permiso", color = Color.White)
                }
            }
        }
    } else {
        // Mostrar el escáner de cámara
        CameraPreview(
            onBarcodeScanned = onBarcodeScanned,
            onError = onError,
            modifier = modifier
        )
    }
}

@Composable
fun CameraPreview(
    onBarcodeScanned: (String) -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var imageAnalyzer by remember { mutableStateOf<ImageAnalysis?>(null) }
    
    // Estado para controlar el escaneo y evitar duplicados
    val isProcessing = remember { mutableStateOf(false) }
    val lastScannedCode = remember { mutableStateOf("") }
    val lastScannedTime = remember { mutableStateOf(0L) }
    
    // Configurar ML Kit Barcode Scanner
    val barcodeScanner = remember {
        try {
            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                    Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_CODE_128,
                    Barcode.FORMAT_CODE_39,
                    Barcode.FORMAT_CODE_93,
                    Barcode.FORMAT_CODABAR,
                    Barcode.FORMAT_DATA_MATRIX,
                    Barcode.FORMAT_EAN_13,
                    Barcode.FORMAT_EAN_8,
                    Barcode.FORMAT_ITF,
                    Barcode.FORMAT_UPC_A,
                    Barcode.FORMAT_UPC_E,
                    Barcode.FORMAT_PDF417,
                    Barcode.FORMAT_AZTEC
                )
                .build()
            BarcodeScanning.getClient(options)
        } catch (e: Exception) {
            onError("Error al inicializar ML Kit: ${e.message}")
            null
        }
    }
    
    // Executor para análisis de imagen
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner?.close()
        }
    }
    
    Box(modifier = modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            
            cameraProviderFuture.addListener({
                try {
                    val cameraProvider = cameraProviderFuture.get()
                    
                    // Verificar si hay cámaras disponibles
                    if (cameraProvider.availableCameraInfos.isEmpty()) {
                        onError("No se encontraron cámaras disponibles en el dispositivo")
                        return@addListener
                    }
                    
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    imageCapture = ImageCapture.Builder().build()
                    
                    // Configurar ImageAnalyzer para detectar códigos
                    imageAnalyzer = ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setTargetResolution(android.util.Size(1280, 720))
                        .build()
                        .also { analyzer ->
                            analyzer.setAnalyzer(cameraExecutor, { imageProxy ->
                                // Solo procesar si no estamos en medio de otro proceso
                                if (!isProcessing.value) {
                                    barcodeScanner?.let { scanner ->
                                        @OptIn(androidx.camera.core.ExperimentalGetImage::class)
                                        processImageProxy(scanner, imageProxy) { barcode ->
                                            val currentTime = System.currentTimeMillis()
                                            // Evitar duplicados: solo procesar si es un código diferente
                                            // o si han pasado más de 2 segundos desde el último escaneo
                                            if (barcode != lastScannedCode.value || (currentTime - lastScannedTime.value) > 2000) {
                                                isProcessing.value = true
                                                lastScannedCode.value = barcode
                                                lastScannedTime.value = currentTime
                                                onBarcodeScanned(barcode)
                                                // Resetear el estado después de 1 segundo
                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                    isProcessing.value = false
                                                }, 1000)
                                            }
                                        }
                                    } ?: imageProxy.close()
                                } else {
                                    imageProxy.close()
                                }
                            })
                        }
                    
                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture,
                        imageAnalyzer
                    )
                    
                    println("✓ Cámara iniciada correctamente - Escáner QR activo")
                } catch (exc: Exception) {
                    onError("Error al iniciar la cámara: ${exc.message}")
                    println("✗ Error detallado de cámara: ${exc.stackTraceToString()}")
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Overlay con marco de escaneo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .border(3.dp, MarvicOrange, RoundedCornerShape(16.dp))
                    .background(Color.Transparent)
            )
        }
        
        // Indicador de estado
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isProcessing.value) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MarvicOrange),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "✓ Código detectado",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Apunta al código QR",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@androidx.camera.core.ExperimentalGetImage
private fun processImageProxy(
    barcodeScanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onBarcodeDetected: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    println("✓ Detectados ${barcodes.size} código(s)")
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { rawValue ->
                            println("✓ Código QR detectado: $rawValue")
                            onBarcodeDetected(rawValue)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("✗ Error en procesamiento de imagen: ${exception.message}")
                exception.printStackTrace()
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        println("⚠ MediaImage es null, cerrando imageProxy")
        imageProxy.close()
    }
}
