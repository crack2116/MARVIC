package com.proyecto.marvic.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Provider(
    val id: String = "",
    val nombre: String = "",
    val razonSocial: String = "",
    val ruc: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val contactoPrincipal: String = "",
    val categorias: List<String> = emptyList(), // Categorías de productos que provee
    val calificacion: Double = 0.0, // 0.0 - 5.0
    val activo: Boolean = true,
    val notas: String = "",
    val fechaCreacion: Long = 0L,
    val fechaActualizacion: Long = 0L,
    val ultimaCompra: Long = 0L,
    val totalCompras: Double = 0.0,
    val numeroCompras: Int = 0
)

data class ProviderPurchase(
    val id: String = "",
    val providerId: String = "",
    val numeroOrden: String = "",
    val fecha: Long = 0L,
    val items: List<PurchaseItem> = emptyList(),
    val subtotal: Double = 0.0,
    val igv: Double = 0.0,
    val total: Double = 0.0,
    val estado: String = "PENDIENTE", // PENDIENTE, RECIBIDO, CANCELADO
    val documentoReferencia: String = "", // Número de factura/guía
    val observaciones: String = "",
    val recibidoPor: String = "",
    val fechaRecepcion: Long = 0L
)

data class PurchaseItem(
    val materialId: String = "",
    val materialNombre: String = "",
    val cantidad: Int = 0,
    val precioUnitario: Double = 0.0,
    val subtotal: Double = 0.0
)

interface ProviderRepository {
    suspend fun createProvider(provider: Provider): Result<Provider>
    suspend fun updateProvider(provider: Provider): Result<Provider>
    suspend fun deleteProvider(providerId: String): Result<Unit>
    suspend fun getProviderById(providerId: String): Result<Provider?>
    suspend fun getAllProviders(): Result<List<Provider>>
    suspend fun getActiveProviders(): Result<List<Provider>>
    suspend fun searchProviders(query: String): Result<List<Provider>>
    
    // Compras
    suspend fun createPurchase(purchase: ProviderPurchase): Result<ProviderPurchase>
    suspend fun updatePurchase(purchase: ProviderPurchase): Result<ProviderPurchase>
    suspend fun getPurchasesByProvider(providerId: String): Result<List<ProviderPurchase>>
    suspend fun getAllPurchases(limit: Int = 50): Result<List<ProviderPurchase>>
    suspend fun receivePurchase(purchaseId: String, receivedBy: String): Result<Unit>
}

class FirestoreProviderRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProviderRepository {
    
    private val providersCollection = db.collection("providers")
    private val purchasesCollection = db.collection("purchases")
    
    override suspend fun createProvider(provider: Provider): Result<Provider> = try {
        val providerData = provider.copy(
            fechaCreacion = System.currentTimeMillis(),
            fechaActualizacion = System.currentTimeMillis()
        )
        val docRef = providersCollection.document()
        docRef.set(providerData.copy(id = docRef.id)).await()
        Result.success(providerData.copy(id = docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun updateProvider(provider: Provider): Result<Provider> = try {
        val updatedProvider = provider.copy(fechaActualizacion = System.currentTimeMillis())
        providersCollection.document(provider.id).set(updatedProvider).await()
        Result.success(updatedProvider)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun deleteProvider(providerId: String): Result<Unit> = try {
        providersCollection.document(providerId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProviderById(providerId: String): Result<Provider?> = try {
        val doc = providersCollection.document(providerId).get().await()
        if (doc.exists()) {
            val provider = doc.toObject(Provider::class.java)?.copy(id = doc.id)
            Result.success(provider)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getAllProviders(): Result<List<Provider>> = try {
        val snapshot = providersCollection.get().await()
        val providers = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Provider::class.java)?.copy(id = doc.id)
        }
        Result.success(providers)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getActiveProviders(): Result<List<Provider>> = try {
        val snapshot = providersCollection.whereEqualTo("activo", true).get().await()
        val providers = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Provider::class.java)?.copy(id = doc.id)
        }
        Result.success(providers)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun searchProviders(query: String): Result<List<Provider>> = try {
        val snapshot = providersCollection
            .whereGreaterThanOrEqualTo("nombre", query)
            .whereLessThan("nombre", query + '\uf8ff')
            .get()
            .await()
        val providers = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Provider::class.java)?.copy(id = doc.id)
        }
        Result.success(providers)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun createPurchase(purchase: ProviderPurchase): Result<ProviderPurchase> = try {
        val purchaseData = purchase.copy(fecha = System.currentTimeMillis())
        val docRef = purchasesCollection.document()
        docRef.set(purchaseData.copy(id = docRef.id)).await()
        
        // Actualizar estadísticas del proveedor
        updateProviderStats(purchase.providerId, purchase.total)
        
        Result.success(purchaseData.copy(id = docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun updatePurchase(purchase: ProviderPurchase): Result<ProviderPurchase> = try {
        purchasesCollection.document(purchase.id).set(purchase).await()
        Result.success(purchase)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getPurchasesByProvider(providerId: String): Result<List<ProviderPurchase>> = try {
        val snapshot = purchasesCollection
            .whereEqualTo("providerId", providerId)
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        val purchases = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProviderPurchase::class.java)?.copy(id = doc.id)
        }
        Result.success(purchases)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getAllPurchases(limit: Int): Result<List<ProviderPurchase>> = try {
        val snapshot = purchasesCollection
            .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        val purchases = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProviderPurchase::class.java)?.copy(id = doc.id)
        }
        Result.success(purchases)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun receivePurchase(purchaseId: String, receivedBy: String): Result<Unit> = try {
        val purchase = purchasesCollection.document(purchaseId).get().await()
            .toObject(ProviderPurchase::class.java)
        
        if (purchase != null) {
            // Actualizar estado de la compra
            purchasesCollection.document(purchaseId).update(
                mapOf(
                    "estado" to "RECIBIDO",
                    "recibidoPor" to receivedBy,
                    "fechaRecepcion" to System.currentTimeMillis()
                )
            ).await()
            
            // Agregar items al inventario
            val materialsCollection = db.collection("materials")
            for (item in purchase.items) {
                val materialDoc = materialsCollection.document(item.materialId)
                db.runTransaction { transaction ->
                    val materialSnap = transaction.get(materialDoc)
                    val currentQty = (materialSnap.getLong("cantidad") ?: 0L).toInt()
                    transaction.update(materialDoc, "cantidad", currentQty + item.cantidad)
                    transaction.update(materialDoc, "precioUnitario", item.precioUnitario)
                    transaction.update(materialDoc, "fechaActualizacion", Timestamp.now())
                }.await()
            }
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    private suspend fun updateProviderStats(providerId: String, amount: Double) {
        try {
            val providerDoc = providersCollection.document(providerId)
            db.runTransaction { transaction ->
                val provider = transaction.get(providerDoc).toObject(Provider::class.java)
                if (provider != null) {
                    transaction.update(
                        providerDoc,
                        mapOf(
                            "totalCompras" to (provider.totalCompras + amount),
                            "numeroCompras" to (provider.numeroCompras + 1),
                            "ultimaCompra" to System.currentTimeMillis()
                        )
                    )
                }
            }.await()
        } catch (e: Exception) {
            println("Error actualizando stats del proveedor: ${e.message}")
        }
    }
}





