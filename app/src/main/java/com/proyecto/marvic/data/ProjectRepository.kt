package com.proyecto.marvic.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Project(
    val id: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cliente: String = "",
    val ubicacion: String = "",
    val responsable: String = "",
    val estado: String = "PLANIFICACION", // PLANIFICACION, EN_CURSO, PAUSADO, FINALIZADO, CANCELADO
    val fechaInicio: Long = 0L,
    val fechaFinPrevista: Long = 0L,
    val fechaFinReal: Long = 0L,
    val presupuesto: Double = 0.0,
    val gastoReal: Double = 0.0,
    val porcentajeAvance: Int = 0, // 0-100
    val prioridad: String = "MEDIA", // BAJA, MEDIA, ALTA, URGENTE
    val notas: String = "",
    val fechaCreacion: Long = 0L,
    val fechaActualizacion: Long = 0L
)

data class ProjectMaterial(
    val id: String = "",
    val projectId: String = "",
    val materialId: String = "",
    val materialNombre: String = "",
    val cantidadPlanificada: Int = 0,
    val cantidadUsada: Int = 0,
    val precioUnitarioEstimado: Double = 0.0,
    val costoTotal: Double = 0.0,
    val fechaAsignacion: Long = 0L
)

data class ProjectActivity(
    val id: String = "",
    val projectId: String = "",
    val tipo: String = "", // INICIO, ASIGNACION_MATERIAL, USO_MATERIAL, NOTA, CAMBIO_ESTADO
    val descripcion: String = "",
    val userId: String = "",
    val userName: String = "",
    val materialId: String = "",
    val cantidad: Int = 0,
    val timestamp: Long = 0L
)

interface ProjectRepository {
    // Proyectos
    suspend fun createProject(project: Project): Result<Project>
    suspend fun updateProject(project: Project): Result<Project>
    suspend fun deleteProject(projectId: String): Result<Unit>
    suspend fun getProjectById(projectId: String): Result<Project?>
    suspend fun getAllProjects(): Result<List<Project>>
    suspend fun getActiveProjects(): Result<List<Project>>
    suspend fun getProjectsByStatus(status: String): Result<List<Project>>
    suspend fun searchProjects(query: String): Result<List<Project>>
    
    // Materiales del proyecto
    suspend fun assignMaterialToProject(projectMaterial: ProjectMaterial): Result<ProjectMaterial>
    suspend fun updateProjectMaterial(projectMaterial: ProjectMaterial): Result<ProjectMaterial>
    suspend fun getProjectMaterials(projectId: String): Result<List<ProjectMaterial>>
    suspend fun useMaterial(projectId: String, materialId: String, cantidad: Int, userId: String): Result<Unit>
    
    // Actividades
    suspend fun logActivity(activity: ProjectActivity): Result<Unit>
    suspend fun getProjectActivities(projectId: String, limit: Int = 50): Result<List<ProjectActivity>>
    
    // Reportes
    suspend fun getProjectCosts(projectId: String): Result<Map<String, Double>>
    suspend fun getProjectProgress(projectId: String): Result<Int>
}

class FirestoreProjectRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProjectRepository {
    
    private val projectsCollection = db.collection("projects")
    private val projectMaterialsCollection = db.collection("project_materials")
    private val projectActivitiesCollection = db.collection("project_activities")
    private val materialsCollection = db.collection("materials")
    
    override suspend fun createProject(project: Project): Result<Project> = try {
        val projectData = project.copy(
            fechaCreacion = System.currentTimeMillis(),
            fechaActualizacion = System.currentTimeMillis()
        )
        val docRef = projectsCollection.document()
        docRef.set(projectData.copy(id = docRef.id)).await()
        
        // Log activity
        logActivity(ProjectActivity(
            projectId = docRef.id,
            tipo = "INICIO",
            descripcion = "Proyecto creado",
            timestamp = System.currentTimeMillis()
        ))
        
        Result.success(projectData.copy(id = docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun updateProject(project: Project): Result<Project> = try {
        val updatedProject = project.copy(fechaActualizacion = System.currentTimeMillis())
        projectsCollection.document(project.id).set(updatedProject).await()
        Result.success(updatedProject)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun deleteProject(projectId: String): Result<Unit> = try {
        projectsCollection.document(projectId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProjectById(projectId: String): Result<Project?> = try {
        val doc = projectsCollection.document(projectId).get().await()
        if (doc.exists()) {
            val project = doc.toObject(Project::class.java)?.copy(id = doc.id)
            Result.success(project)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getAllProjects(): Result<List<Project>> = try {
        val snapshot = projectsCollection
            .orderBy("fechaCreacion", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        val projects = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Project::class.java)?.copy(id = doc.id)
        }
        Result.success(projects)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getActiveProjects(): Result<List<Project>> = try {
        val snapshot = projectsCollection
            .whereIn("estado", listOf("PLANIFICACION", "EN_CURSO"))
            .get()
            .await()
        val projects = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Project::class.java)?.copy(id = doc.id)
        }
        Result.success(projects)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProjectsByStatus(status: String): Result<List<Project>> = try {
        val snapshot = projectsCollection
            .whereEqualTo("estado", status)
            .get()
            .await()
        val projects = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Project::class.java)?.copy(id = doc.id)
        }
        Result.success(projects)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun searchProjects(query: String): Result<List<Project>> = try {
        val snapshot = projectsCollection
            .whereGreaterThanOrEqualTo("nombre", query)
            .whereLessThan("nombre", query + '\uf8ff')
            .get()
            .await()
        val projects = snapshot.documents.mapNotNull { doc ->
            doc.toObject(Project::class.java)?.copy(id = doc.id)
        }
        Result.success(projects)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun assignMaterialToProject(projectMaterial: ProjectMaterial): Result<ProjectMaterial> = try {
        val materialData = projectMaterial.copy(fechaAsignacion = System.currentTimeMillis())
        val docRef = projectMaterialsCollection.document()
        docRef.set(materialData.copy(id = docRef.id)).await()
        
        // Log activity
        logActivity(ProjectActivity(
            projectId = projectMaterial.projectId,
            tipo = "ASIGNACION_MATERIAL",
            descripcion = "Material ${projectMaterial.materialNombre} asignado (${projectMaterial.cantidadPlanificada} unidades)",
            materialId = projectMaterial.materialId,
            cantidad = projectMaterial.cantidadPlanificada,
            timestamp = System.currentTimeMillis()
        ))
        
        Result.success(materialData.copy(id = docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun updateProjectMaterial(projectMaterial: ProjectMaterial): Result<ProjectMaterial> = try {
        projectMaterialsCollection.document(projectMaterial.id).set(projectMaterial).await()
        Result.success(projectMaterial)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProjectMaterials(projectId: String): Result<List<ProjectMaterial>> = try {
        val snapshot = projectMaterialsCollection
            .whereEqualTo("projectId", projectId)
            .get()
            .await()
        val materials = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProjectMaterial::class.java)?.copy(id = doc.id)
        }
        Result.success(materials)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun useMaterial(
        projectId: String,
        materialId: String,
        cantidad: Int,
        userId: String
    ): Result<Unit> = try {
        // Descontar del inventario
        val materialDoc = materialsCollection.document(materialId)
        db.runTransaction { transaction ->
            val materialSnap = transaction.get(materialDoc)
            val currentQty = (materialSnap.getLong("cantidad") ?: 0L).toInt()
            
            if (currentQty < cantidad) {
                throw IllegalStateException("Stock insuficiente")
            }
            
            transaction.update(materialDoc, "cantidad", currentQty - cantidad)
            transaction.update(materialDoc, "fechaActualizacion", Timestamp.now())
        }.await()
        
        // Actualizar cantidad usada en el proyecto
        val projectMaterialsQuery = projectMaterialsCollection
            .whereEqualTo("projectId", projectId)
            .whereEqualTo("materialId", materialId)
            .get()
            .await()
        
        if (projectMaterialsQuery.documents.isNotEmpty()) {
            val doc = projectMaterialsQuery.documents.first()
            val projectMaterial = doc.toObject(ProjectMaterial::class.java)
            if (projectMaterial != null) {
                projectMaterialsCollection.document(doc.id).update(
                    "cantidadUsada", projectMaterial.cantidadUsada + cantidad
                ).await()
            }
        }
        
        // Actualizar gasto del proyecto
        val material = materialsCollection.document(materialId).get().await()
        val precio = material.getDouble("precioUnitario") ?: 0.0
        val project = projectsCollection.document(projectId).get().await().toObject(Project::class.java)
        if (project != null) {
            projectsCollection.document(projectId).update(
                "gastoReal", project.gastoReal + (precio * cantidad)
            ).await()
        }
        
        // Log activity
        val materialNombre = material.getString("nombre") ?: "Material"
        logActivity(ProjectActivity(
            projectId = projectId,
            tipo = "USO_MATERIAL",
            descripcion = "Usado $cantidad unidades de $materialNombre",
            userId = userId,
            materialId = materialId,
            cantidad = cantidad,
            timestamp = System.currentTimeMillis()
        ))
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun logActivity(activity: ProjectActivity): Result<Unit> = try {
        val activityData = activity.copy(timestamp = System.currentTimeMillis())
        projectActivitiesCollection.add(activityData).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProjectActivities(projectId: String, limit: Int): Result<List<ProjectActivity>> = try {
        val snapshot = projectActivitiesCollection
            .whereEqualTo("projectId", projectId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        val activities = snapshot.documents.mapNotNull { doc ->
            doc.toObject(ProjectActivity::class.java)?.copy(id = doc.id)
        }
        Result.success(activities)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProjectCosts(projectId: String): Result<Map<String, Double>> = try {
        val project = projectsCollection.document(projectId).get().await()
            .toObject(Project::class.java)
        
        val costs = mutableMapOf<String, Double>()
        costs["presupuesto"] = project?.presupuesto ?: 0.0
        costs["gastoReal"] = project?.gastoReal ?: 0.0
        costs["diferencia"] = (project?.presupuesto ?: 0.0) - (project?.gastoReal ?: 0.0)
        costs["porcentajeUsado"] = if (project?.presupuesto ?: 0.0 > 0) {
            ((project?.gastoReal ?: 0.0) / (project?.presupuesto ?: 1.0)) * 100
        } else 0.0
        
        Result.success(costs)
    } catch (e: Exception) {
        Result.failure(e)
    }
    
    override suspend fun getProjectProgress(projectId: String): Result<Int> = try {
        val projectMaterials = getProjectMaterials(projectId).getOrNull() ?: emptyList()
        
        if (projectMaterials.isEmpty()) {
            Result.success(0)
        } else {
            val totalPlanificado = projectMaterials.sumOf { it.cantidadPlanificada }
            val totalUsado = projectMaterials.sumOf { it.cantidadUsada }
            
            val progress = if (totalPlanificado > 0) {
                ((totalUsado.toDouble() / totalPlanificado.toDouble()) * 100).toInt()
            } else 0
            
            Result.success(progress.coerceIn(0, 100))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}





