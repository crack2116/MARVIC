package com.proyecto.marvic.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Transfer(
    val id: String = "",
    val materialId: String = "",
    val materialNombre: String = "",
    val cantidad: Int = 0,
    val origenAlmacen: String = "",
    val destinoAlmacen: String = "",
    val responsable: String = "",
    val motivo: String = "",
    val estado: String = "PENDIENTE", // PENDIENTE, EN_TRANSITO, COMPLETADA, CANCELADA
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val fechaTransferencia: Long? = null,
    val fechaRecepcion: Long? = null,
    val notas: String = "",
    val autorizadoPor: String = ""
)

interface TransferRepository {
    suspend fun createTransfer(transfer: Transfer): Result<String>
    suspend fun getTransfers(): Result<List<Transfer>>
    suspend fun getTransferById(id: String): Result<Transfer?>
    suspend fun updateTransferStatus(id: String, status: String): Result<Unit>
    suspend fun getTransfersByMaterial(materialId: String): Result<List<Transfer>>
    suspend fun getPendingTransfers(): Result<List<Transfer>>
}

class FirestoreTransferRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val inventoryRepo: InventoryRepository = FirestoreInventoryRepository()
) : TransferRepository {
    
    private val collection get() = db.collection("transfers")
    
    override suspend fun createTransfer(transfer: Transfer): Result<String> {
        return try {
            // Validar stock disponible en origen
            val materialResult = inventoryRepo.getMaterialById(transfer.materialId)
            
            if (materialResult.isFailure) {
                return Result.failure(Exception("Material no encontrado"))
            }
            
            val material = materialResult.getOrNull()
            if (material == null) {
                return Result.failure(Exception("Material no encontrado"))
            }
            
            if (material.cantidad < transfer.cantidad) {
                return Result.failure(Exception("Stock insuficiente en ${transfer.origenAlmacen}. Disponible: ${material.cantidad}"))
            }
        
        // Crear la transferencia
        val docRef = collection.document()
        val transferData = hashMapOf(
            "materialId" to transfer.materialId,
            "materialNombre" to transfer.materialNombre,
            "cantidad" to transfer.cantidad,
            "origenAlmacen" to transfer.origenAlmacen,
            "destinoAlmacen" to transfer.destinoAlmacen,
            "responsable" to transfer.responsable,
            "motivo" to transfer.motivo,
            "estado" to transfer.estado,
            "fechaSolicitud" to Timestamp.now(),
            "notas" to transfer.notas,
            "autorizadoPor" to transfer.autorizadoPor
        )
        
        docRef.set(transferData).await()
        
        // Si es transferencia directa (sin aprobación), ejecutar el movimiento
        if (transfer.estado == "COMPLETADA") {
            executeTransfer(docRef.id)
        }
        
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTransfers(): Result<List<Transfer>> {
        return try {
        val snapshot = collection.orderBy("fechaSolicitud", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        
        val transfers = snapshot.documents.mapNotNull { doc ->
            Transfer(
                id = doc.id,
                materialId = doc.getString("materialId") ?: "",
                materialNombre = doc.getString("materialNombre") ?: "",
                cantidad = (doc.getLong("cantidad") ?: 0).toInt(),
                origenAlmacen = doc.getString("origenAlmacen") ?: "",
                destinoAlmacen = doc.getString("destinoAlmacen") ?: "",
                responsable = doc.getString("responsable") ?: "",
                motivo = doc.getString("motivo") ?: "",
                estado = doc.getString("estado") ?: "PENDIENTE",
                fechaSolicitud = doc.getTimestamp("fechaSolicitud")?.toDate()?.time ?: 0L,
                fechaTransferencia = doc.getTimestamp("fechaTransferencia")?.toDate()?.time,
                fechaRecepcion = doc.getTimestamp("fechaRecepcion")?.toDate()?.time,
                notas = doc.getString("notas") ?: "",
                autorizadoPor = doc.getString("autorizadoPor") ?: ""
            )
        }
        
            Result.success(transfers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTransferById(id: String): Result<Transfer?> {
        return try {
        val doc = collection.document(id).get().await()
        
        if (!doc.exists()) {
            return Result.success(null)
        }
        
        val transfer = Transfer(
            id = doc.id,
            materialId = doc.getString("materialId") ?: "",
            materialNombre = doc.getString("materialNombre") ?: "",
            cantidad = (doc.getLong("cantidad") ?: 0).toInt(),
            origenAlmacen = doc.getString("origenAlmacen") ?: "",
            destinoAlmacen = doc.getString("destinoAlmacen") ?: "",
            responsable = doc.getString("responsable") ?: "",
            motivo = doc.getString("motivo") ?: "",
            estado = doc.getString("estado") ?: "PENDIENTE",
            fechaSolicitud = doc.getTimestamp("fechaSolicitud")?.toDate()?.time ?: 0L,
            fechaTransferencia = doc.getTimestamp("fechaTransferencia")?.toDate()?.time,
            fechaRecepcion = doc.getTimestamp("fechaRecepcion")?.toDate()?.time,
            notas = doc.getString("notas") ?: "",
            autorizadoPor = doc.getString("autorizadoPor") ?: ""
        )
        
            Result.success(transfer)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTransferStatus(id: String, status: String): Result<Unit> {
        return try {
        collection.document(id).update(
            mapOf(
                "estado" to status,
                "fechaTransferencia" to if (status == "EN_TRANSITO") Timestamp.now() else null,
                "fechaRecepcion" to if (status == "COMPLETADA") Timestamp.now() else null
            )
        ).await()
        
        // Si se completa, ejecutar la transferencia de inventario
        if (status == "COMPLETADA") {
            executeTransfer(id)
        }
        
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getTransfersByMaterial(materialId: String): Result<List<Transfer>> {
        return try {
        val snapshot = collection
            .whereEqualTo("materialId", materialId)
            .orderBy("fechaSolicitud", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        
        val transfers = snapshot.documents.mapNotNull { doc ->
            Transfer(
                id = doc.id,
                materialId = doc.getString("materialId") ?: "",
                materialNombre = doc.getString("materialNombre") ?: "",
                cantidad = (doc.getLong("cantidad") ?: 0).toInt(),
                origenAlmacen = doc.getString("origenAlmacen") ?: "",
                destinoAlmacen = doc.getString("destinoAlmacen") ?: "",
                responsable = doc.getString("responsable") ?: "",
                motivo = doc.getString("motivo") ?: "",
                estado = doc.getString("estado") ?: "PENDIENTE",
                fechaSolicitud = doc.getTimestamp("fechaSolicitud")?.toDate()?.time ?: 0L,
                notas = doc.getString("notas") ?: ""
            )
        }
        
            Result.success(transfers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPendingTransfers(): Result<List<Transfer>> {
        return try {
        val snapshot = collection
            .whereEqualTo("estado", "PENDIENTE")
            .orderBy("fechaSolicitud", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        
        val transfers = snapshot.documents.mapNotNull { doc ->
            Transfer(
                id = doc.id,
                materialId = doc.getString("materialId") ?: "",
                materialNombre = doc.getString("materialNombre") ?: "",
                cantidad = (doc.getLong("cantidad") ?: 0).toInt(),
                origenAlmacen = doc.getString("origenAlmacen") ?: "",
                destinoAlmacen = doc.getString("destinoAlmacen") ?: "",
                responsable = doc.getString("responsable") ?: "",
                motivo = doc.getString("motivo") ?: "",
                estado = doc.getString("estado") ?: "PENDIENTE",
                fechaSolicitud = doc.getTimestamp("fechaSolicitud")?.toDate()?.time ?: 0L,
                notas = doc.getString("notas") ?: ""
            )
        }
        
            Result.success(transfers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun executeTransfer(transferId: String) {
        try {
            val transferResult = getTransferById(transferId)
            val transfer = transferResult.getOrNull() ?: return
            
            // Registrar salida del almacén origen
            inventoryRepo.registerMovement(
                materialId = transfer.materialId,
                delta = -transfer.cantidad
            )
            
            // Registrar entrada al almacén destino
            // Nota: En una implementación más compleja, se manejarían diferentes IDs para el mismo material en diferentes almacenes
            inventoryRepo.registerMovement(
                materialId = transfer.materialId,
                delta = transfer.cantidad
            )
            
        } catch (e: Exception) {
            println("Error ejecutando transferencia: ${e.message}")
        }
    }
}

