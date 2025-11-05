package com.proyecto.marvic.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.proyecto.marvic.data.UserSession
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageManager(private val context: Context) {
    
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference
    
    /**
     * Comprime una imagen para reducir su tamaño (optimizado)
     */
    fun compressImage(uri: Uri, maxWidth: Int = 1024, maxHeight: Int = 1024, quality: Int = 80): ByteArray? {
        return PerformanceMonitor.measureSync("compress_image") {
            compressImageInternal(uri, maxWidth, maxHeight, quality)
        }
    }
    
    private fun compressImageInternal(uri: Uri, maxWidth: Int, maxHeight: Int, quality: Int): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Calcular nuevo tamaño manteniendo aspecto
            val width = originalBitmap.width
            val height = originalBitmap.height
            val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
            
            val newWidth = (width * ratio).toInt()
            val newHeight = (height * ratio).toInt()
            
            // Redimensionar
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            
            // Comprimir a JPEG
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            
            val compressedData = outputStream.toByteArray()
            
            // Limpiar
            originalBitmap.recycle()
            resizedBitmap.recycle()
            outputStream.close()
            
            compressedData
        } catch (e: Exception) {
            println("Error comprimiendo imagen: ${e.message}")
            null
        }
    }
    
    /**
     * Sube una imagen a Firebase Storage
     */
    suspend fun uploadImage(
        imageData: ByteArray,
        materialId: String,
        onProgress: (Int) -> Unit = {}
    ): Result<String> {
        return try {
            // Rate limiting
            val userId = UserSession.userId
            if (!RateLimiter.isAllowed(userId, "upload_photo")) {
                throw Exception("Has excedido el límite de uploads por minuto")
            }
            
            // Validar tamaño de imagen (máximo 5MB)
            if (imageData.size > 5 * 1024 * 1024) {
                throw Exception("La imagen es muy grande. Máximo 5MB permitido.")
            }
            
            val fileName = "${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child("materials/$materialId/$fileName")
            
            val uploadTask = imageRef.putBytes(imageData)
            
            // Observar progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }
            
            // Esperar que termine
            uploadTask.await()
            
            // Obtener URL de descarga
            val downloadUrl = imageRef.downloadUrl.await()
            val downloadUrlString = downloadUrl.toString()
            
            // Audit log
            AuditLogger.logPhotoUpload(materialId, downloadUrlString)
            
            Result.success(downloadUrlString)
        } catch (e: Exception) {
            println("Error subiendo imagen: ${e.message}")
            AuditLogger.logError(
                AuditLogger.Module.PHOTOS,
                "Error al subir imagen para material $materialId: ${e.message}"
            )
            Result.failure(e)
        }
    }
    
    /**
     * Elimina una imagen de Firebase Storage
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error eliminando imagen: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Lista todas las imágenes de un material
     */
    suspend fun listImages(materialId: String): Result<List<String>> {
        return try {
            val imagesRef = storageRef.child("materials/$materialId")
            val listResult = imagesRef.listAll().await()
            
            val urls = mutableListOf<String>()
            for (item in listResult.items) {
                val url = item.downloadUrl.await()
                urls.add(url.toString())
            }
            
            Result.success(urls)
        } catch (e: Exception) {
            println("Error listando imágenes: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Guarda una imagen temporalmente en caché
     */
    fun saveToCache(uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val cacheDir = context.cacheDir
            val tempFile = File(cacheDir, "temp_${System.currentTimeMillis()}.jpg")
            
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            
            inputStream.close()
            outputStream.close()
            
            tempFile
        } catch (e: Exception) {
            println("Error guardando en caché: ${e.message}")
            null
        }
    }
    
    /**
     * Limpia archivos temporales
     */
    fun clearCache() {
        try {
            context.cacheDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("temp_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            println("Error limpiando caché: ${e.message}")
        }
    }
}

