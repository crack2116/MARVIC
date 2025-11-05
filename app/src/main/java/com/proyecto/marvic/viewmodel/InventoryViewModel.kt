package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.FirestoreInventoryRepository
import com.proyecto.marvic.data.InventoryRepository
import com.proyecto.marvic.data.MaterialItem
import com.proyecto.marvic.data.demo.DemoConfig
import com.proyecto.marvic.notifications.StockMonitor
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.content.Context

class InventoryViewModel(private val repo: InventoryRepository = FirestoreInventoryRepository()) : ViewModel() {
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    val items = mutableStateListOf<MaterialItem>()
    var total by mutableStateOf(0)
    var critical by mutableStateOf<List<MaterialItem>>(emptyList())
    var allMaterials by mutableStateOf<List<MaterialItem>>(emptyList())
    private var criticalListener: ListenerRegistration? = null
    private var materialsListener: ListenerRegistration? = null

    fun search(query: String) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            val result = if (DemoConfig.useDemo) {
                Result.success(
                    listOf(
                        MaterialItem(id = "cemento", codigo = "CEM001", nombre = "Cemento Pacasmayo", cantidad = 85, ubicacion = "Almacén 2"),
                        MaterialItem(id = "varillas", codigo = "VAR001", nombre = "Varillas 1/2\"", cantidad = 120, ubicacion = "Almacén 1")
                    ).filter { it.nombre.contains(query, true) }
                )
            } else repo.searchMaterials(query)
            isLoading = false
            if (result.isSuccess) {
                items.clear(); items.addAll(result.getOrDefault(emptyList()))
            } else {
                errorMessage = result.exceptionOrNull()?.localizedMessage
            }
        }
    }

    fun refreshTotals() {
        viewModelScope.launch {
            val r = if (DemoConfig.useDemo) Result.success(12450) else repo.totalStock()
            if (r.isSuccess) total = r.getOrDefault(0)
        }
    }

    fun move(materialId: String, delta: Int, onDone: (Boolean) -> Unit, context: Context? = null) {
        isLoading = true
        viewModelScope.launch {
            val r = if (DemoConfig.useDemo) Result.success(Unit) else repo.registerMovement(materialId, delta)
            
            if (r.isSuccess && context != null) {
                // Obtener información del material para la notificación
                val materialName = getMaterialName(materialId)
                val movementType = if (delta > 0) "entrada" else "salida"
                val quantity = kotlin.math.abs(delta)
                
                // Mostrar notificación de movimiento
                StockMonitor.onMovementRegistered(context, materialName, movementType, quantity)
            }
            
            isLoading = false
            onDone(r.isSuccess)
            refreshTotals()
        }
    }
    
    private suspend fun getMaterialName(materialId: String): String {
        return try {
            val doc = FirebaseFirestore.getInstance().collection("materials").document(materialId).get().await()
            doc.getString("nombre") ?: "Material desconocido"
        } catch (e: Exception) {
            "Material $materialId"
        }
    }

    fun observeCritical(threshold: Int = 50) {
        if (DemoConfig.useDemo) {
            critical = listOf(MaterialItem(id = "MAT001", codigo = "MAT001", nombre = "Cemento Pacasmayo", cantidad = 20, ubicacion = "Almacén 2"))
            return
        }
        criticalListener?.remove()
        val db = FirebaseFirestore.getInstance()
        criticalListener = db.collection("materials").whereLessThanOrEqualTo("cantidad", threshold).addSnapshotListener { snap, _ ->
            val list = snap?.documents?.map {
                MaterialItem(
                    id = it.id,
                    codigo = it.getString("codigo") ?: it.id,
                    nombre = it.getString("nombre") ?: "",
                    cantidad = (it.getLong("cantidad") ?: 0L).toInt(),
                    ubicacion = it.getString("ubicacion") ?: "",
                    fechaCreacion = it.getTimestamp("fechaCreacion")?.toDate()?.time ?: 0L,
                    fechaActualizacion = it.getTimestamp("fechaActualizacion")?.toDate()?.time ?: 0L
                )
            } ?: emptyList()
            critical = list
        }
    }

    fun observeAllMaterials() {
        if (DemoConfig.useDemo) {
            allMaterials = listOf(
                MaterialItem(id = "MAT001", codigo = "MAT001", nombre = "Cemento Portland", cantidad = 2450, ubicacion = "Almacén 1"),
                MaterialItem(id = "MAT002", codigo = "MAT002", nombre = "Varilla #3", cantidad = 1890, ubicacion = "Patio Exterior"),
                MaterialItem(id = "MAT003", codigo = "MAT003", nombre = "Tubo PVC 4\"", cantidad = 567, ubicacion = "Almacén 2"),
                MaterialItem(id = "MAT004", codigo = "MAT004", nombre = "Arena Fina", cantidad = 3200, ubicacion = "Patio Exterior"),
                MaterialItem(id = "MAT005", codigo = "MAT005", nombre = "Ladrillo Hueco", cantidad = 1200, ubicacion = "Almacén 3"),
                MaterialItem(id = "MAT006", codigo = "MAT006", nombre = "Cable THW #12", cantidad = 89, ubicacion = "Almacén 4"),
                MaterialItem(id = "MAT007", codigo = "MAT007", nombre = "Pintura Blanca", cantidad = 45, ubicacion = "Almacén 1"),
                MaterialItem(id = "MAT008", codigo = "MAT008", nombre = "Malla Electrosoldada", cantidad = 234, ubicacion = "Patio Exterior")
            )
            return
        }
        
        materialsListener?.remove()
        val db = FirebaseFirestore.getInstance()
        
        // Intentar obtener datos sin ordenamiento primero
        materialsListener = db.collection("materials")
            .addSnapshotListener { snap, error ->
                if (error != null) {
                    println("Error obteniendo materiales: ${error.message}")
                    // Si hay error, usar datos demo
                    allMaterials = listOf(
                        MaterialItem(id = "MAT001", codigo = "MAT001", nombre = "Cemento Portland", cantidad = 2450, ubicacion = "Almacén 1"),
                        MaterialItem(id = "MAT002", codigo = "MAT002", nombre = "Varilla #3", cantidad = 1890, ubicacion = "Patio Exterior"),
                        MaterialItem(id = "MAT003", codigo = "MAT003", nombre = "Tubo PVC 4\"", cantidad = 567, ubicacion = "Almacén 2"),
                        MaterialItem(id = "MAT004", codigo = "MAT004", nombre = "Arena Fina", cantidad = 3200, ubicacion = "Patio Exterior"),
                        MaterialItem(id = "MAT005", codigo = "MAT005", nombre = "Ladrillo Hueco", cantidad = 1200, ubicacion = "Almacén 3"),
                        MaterialItem(id = "MAT006", codigo = "MAT006", nombre = "Cable THW #12", cantidad = 89, ubicacion = "Almacén 4"),
                        MaterialItem(id = "MAT007", codigo = "MAT007", nombre = "Pintura Blanca", cantidad = 45, ubicacion = "Almacén 1"),
                        MaterialItem(id = "MAT008", codigo = "MAT008", nombre = "Malla Electrosoldada", cantidad = 234, ubicacion = "Patio Exterior")
                    )
                    return@addSnapshotListener
                }
                
                val list = snap?.documents?.map {
                    MaterialItem(
                        id = it.id,
                        codigo = it.getString("codigo") ?: it.id,
                        nombre = it.getString("nombre") ?: "Sin nombre",
                        cantidad = (it.getLong("cantidad") ?: 0L).toInt(),
                        ubicacion = it.getString("ubicacion") ?: "Sin ubicación",
                        fechaCreacion = it.getTimestamp("fechaCreacion")?.toDate()?.time ?: 0L,
                        fechaActualizacion = it.getTimestamp("fechaActualizacion")?.toDate()?.time ?: 0L
                    )
                } ?: emptyList()
                
                println("Materiales obtenidos de Firebase: ${list.size}")
                if (list.isEmpty()) {
                    println("No hay materiales en Firebase, usando datos demo")
                    // Si no hay datos en Firebase, usar datos demo
                    allMaterials = listOf(
                        MaterialItem(id = "MAT001", codigo = "MAT001", nombre = "Cemento Portland", cantidad = 2450, ubicacion = "Almacén 1"),
                        MaterialItem(id = "MAT002", codigo = "MAT002", nombre = "Varilla #3", cantidad = 1890, ubicacion = "Patio Exterior"),
                        MaterialItem(id = "MAT003", codigo = "MAT003", nombre = "Tubo PVC 4\"", cantidad = 567, ubicacion = "Almacén 2"),
                        MaterialItem(id = "MAT004", codigo = "MAT004", nombre = "Arena Fina", cantidad = 3200, ubicacion = "Patio Exterior"),
                        MaterialItem(id = "MAT005", codigo = "MAT005", nombre = "Ladrillo Hueco", cantidad = 1200, ubicacion = "Almacén 3"),
                        MaterialItem(id = "MAT006", codigo = "MAT006", nombre = "Cable THW #12", cantidad = 89, ubicacion = "Almacén 4"),
                        MaterialItem(id = "MAT007", codigo = "MAT007", nombre = "Pintura Blanca", cantidad = 45, ubicacion = "Almacén 1"),
                        MaterialItem(id = "MAT008", codigo = "MAT008", nombre = "Malla Electrosoldada", cantidad = 234, ubicacion = "Patio Exterior")
                    )
                } else {
                    allMaterials = list
                }
            }
    }

    // Función para simular movimientos de inventario y entrenar la IA
    fun simulateInventoryActivity() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                
                // Simular movimientos de los últimos 6 meses
                val materials = listOf("Cemento Portland", "Varilla #3", "Tubo PVC 4\"", "Arena Fina")
                val months = listOf("2024-01", "2024-02", "2024-03", "2024-04", "2024-05", "2024-06")
                
                materials.forEach { materialName ->
                    months.forEach { month ->
                        // Simular consumo mensual (10-50 unidades)
                        val consumption = (10..50).random()
                        
                        val movement = mapOf(
                            "material" to materialName,
                            "tipo" to "salida",
                            "cantidad" to consumption,
                            "fecha" to com.google.firebase.Timestamp.now(),
                            "mes" to month,
                            "descripcion" to "Consumo mensual simulado"
                        )
                        
                        db.collection("movements").add(movement).await()
                    }
                }
                
                println("Actividad de inventario simulada para entrenar IA")
            } catch (e: Exception) {
                println("Error simulando actividad: ${e.message}")
            }
        }
    }

    // Función para agregar datos de prueba a Firebase
    fun seedFirebaseData() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                
                // Verificar si ya existen datos
                val snapshot = db.collection("materials").get().await()
                if (snapshot.isEmpty) {
                    println("Agregando datos de prueba a Firebase...")
                    
                    val sampleMaterials = listOf(
                        mapOf(
                            "nombre" to "Cemento Portland",
                            "cantidad" to 2450L,
                            "ubicacion" to "Almacén 1",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Varilla #3",
                            "cantidad" to 1890L,
                            "ubicacion" to "Patio Exterior",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Tubo PVC 4\"",
                            "cantidad" to 567L,
                            "ubicacion" to "Almacén 2",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Arena Fina",
                            "cantidad" to 3200L,
                            "ubicacion" to "Patio Exterior",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Ladrillo Hueco",
                            "cantidad" to 1200L,
                            "ubicacion" to "Almacén 3",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Cable THW #12",
                            "cantidad" to 89L,
                            "ubicacion" to "Almacén 4",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Pintura Blanca",
                            "cantidad" to 45L,
                            "ubicacion" to "Almacén 1",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        ),
                        mapOf(
                            "nombre" to "Malla Electrosoldada",
                            "cantidad" to 234L,
                            "ubicacion" to "Patio Exterior",
                            "fechaCreacion" to com.google.firebase.Timestamp.now(),
                            "fechaActualizacion" to com.google.firebase.Timestamp.now()
                        )
                    )
                    
                    sampleMaterials.forEach { material ->
                        db.collection("materials").add(material).await()
                    }
                    
                    println("Datos de prueba agregados exitosamente a Firebase")
                } else {
                    println("Firebase ya contiene ${snapshot.size()} materiales")
                }
            } catch (e: Exception) {
                println("Error agregando datos a Firebase: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        criticalListener?.remove()
        materialsListener?.remove()
    }
}


