package com.proyecto.marvic.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class RoleConfig(
    val id: String = "",
    val nombre: String = "",
    val displayName: String = "",
    val nivel: Int = 0,
    val descripcion: String = "",
    val activo: Boolean = true
)

interface RoleRepository {
    suspend fun getAllRoles(): Result<List<RoleConfig>>
    suspend fun getRoleById(roleId: String): Result<RoleConfig?>
    suspend fun initializeDefaultRoles(): Result<Unit>
}

class FirestoreRoleRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : RoleRepository {
    
    private val collection = db.collection("roles")
    
    override suspend fun getAllRoles(): Result<List<RoleConfig>> {
        return try {
            val snapshot = collection
                .whereEqualTo("activo", true)
                .orderBy("nivel")
                .get()
                .await()
            
            val roles = snapshot.documents.mapNotNull { doc ->
                try {
                    RoleConfig(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        displayName = doc.getString("displayName") ?: "",
                        nivel = (doc.getLong("nivel") ?: 0).toInt(),
                        descripcion = doc.getString("descripcion") ?: "",
                        activo = doc.getBoolean("activo") ?: true
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(roles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRoleById(roleId: String): Result<RoleConfig?> {
        return try {
            val doc = collection.document(roleId).get().await()
            if (doc.exists()) {
                val role = RoleConfig(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    nivel = (doc.getLong("nivel") ?: 0).toInt(),
                    descripcion = doc.getString("descripcion") ?: "",
                    activo = doc.getBoolean("activo") ?: true
                )
                Result.success(role)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun initializeDefaultRoles(): Result<Unit> {
        return try {
            // Verificar si ya existen roles
            val existing = collection.limit(1).get().await()
            if (!existing.isEmpty) {
                return Result.success(Unit) // Ya existen roles
            }
            
            // Crear roles por defecto
            val defaultRoles = listOf(
                hashMapOf(
                    "nombre" to "almacenero",
                    "displayName" to "Almacenero",
                    "nivel" to 1,
                    "descripcion" to "Personal de almacén - Registra movimientos y consulta inventario",
                    "activo" to true,
                    "permisos" to listOf(
                        "registrar_movimientos",
                        "consultar_inventario",
                        "escanear_qr"
                    )
                ),
                hashMapOf(
                    "nombre" to "jefe_logistica",
                    "displayName" to "Jefe de Logística",
                    "nivel" to 2,
                    "descripcion" to "Supervisor de logística - Acceso a reportes y gestión avanzada",
                    "activo" to true,
                    "permisos" to listOf(
                        "registrar_movimientos",
                        "consultar_inventario",
                        "escanear_qr",
                        "ver_reportes",
                        "busqueda_avanzada",
                        "gestionar_proveedores",
                        "gestionar_proyectos"
                    )
                ),
                hashMapOf(
                    "nombre" to "gerente",
                    "displayName" to "Gerente",
                    "nivel" to 3,
                    "descripcion" to "Gerente general - Acceso completo al sistema",
                    "activo" to true,
                    "permisos" to listOf(
                        "registrar_movimientos",
                        "consultar_inventario",
                        "escanear_qr",
                        "ver_reportes",
                        "busqueda_avanzada",
                        "gestionar_proveedores",
                        "gestionar_proyectos",
                        "gestionar_usuarios",
                        "ver_analytics",
                        "exportar_pdf",
                        "configurar_sistema"
                    )
                )
            )
            
            // Insertar roles
            for ((index, roleData) in defaultRoles.withIndex()) {
                val roleId = when(index) {
                    0 -> "almacenero"
                    1 -> "jefe_logistica"
                    2 -> "gerente"
                    else -> "role_$index"
                }
                collection.document(roleId).set(roleData).await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}





