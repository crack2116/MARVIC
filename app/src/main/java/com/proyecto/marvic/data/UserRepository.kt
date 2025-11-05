package com.proyecto.marvic.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class User(
    val id: String = "",
    val email: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val rol: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Long = 0L,
    val ultimoAcceso: Long = 0L,
    val permisos: List<String> = emptyList()
)

data class UserActivity(
    val id: String = "",
    val userId: String = "",
    val accion: String = "",
    val descripcion: String = "",
    val materialId: String = "",
    val cantidad: Int = 0,
    val timestamp: Long = 0L,
    val ipAddress: String = "",
    val deviceInfo: String = ""
)

interface UserRepository {
    suspend fun createUser(user: User): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun deleteUser(userId: String): Result<Unit>
    suspend fun getUserById(userId: String): Result<User?>
    suspend fun getAllUsers(): Result<List<User>>
    suspend fun getUserByEmail(email: String): Result<User?>
    suspend fun logUserActivity(activity: UserActivity): Result<Unit>
    suspend fun getUserActivities(userId: String, limit: Int = 50): Result<List<UserActivity>>
    suspend fun getAllActivities(limit: Int = 100): Result<List<UserActivity>>
}

class FirestoreUserRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) : UserRepository {
    private val usersCollection = db.collection("users")
    private val activitiesCollection = db.collection("user_activities")

    override suspend fun createUser(user: User): Result<User> = try {
        val userData = user.copy(
            fechaCreacion = System.currentTimeMillis(),
            ultimoAcceso = System.currentTimeMillis()
        )
        val docRef = usersCollection.document()
        docRef.set(userData.copy(id = docRef.id)).await()
        Result.success(userData.copy(id = docRef.id))
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun updateUser(user: User): Result<User> = try {
        val updatedUser = user.copy(ultimoAcceso = System.currentTimeMillis())
        usersCollection.document(user.id).set(updatedUser).await()
        Result.success(updatedUser)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteUser(userId: String): Result<Unit> = try {
        usersCollection.document(userId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserById(userId: String): Result<User?> = try {
        val doc = usersCollection.document(userId).get().await()
        if (doc.exists()) {
            val user = User(
                id = doc.id,
                email = doc.getString("email") ?: "",
                nombre = doc.getString("nombre") ?: "",
                apellido = doc.getString("apellido") ?: "",
                rol = doc.getString("rol") ?: "",
                activo = doc.getBoolean("activo") ?: true,
                fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                ultimoAcceso = doc.getLong("ultimoAcceso") ?: 0L,
                permisos = (doc.get("permisos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
            Result.success(user)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllUsers(): Result<List<User>> = try {
        val snapshot = usersCollection.get().await()
        val users = snapshot.documents.mapNotNull { doc ->
            try {
                User(
                    id = doc.id,
                    email = doc.getString("email") ?: "",
                    nombre = doc.getString("nombre") ?: "",
                    apellido = doc.getString("apellido") ?: "",
                    rol = doc.getString("rol") ?: "",
                    activo = doc.getBoolean("activo") ?: true,
                    fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                    ultimoAcceso = doc.getLong("ultimoAcceso") ?: 0L,
                    permisos = (doc.get("permisos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                )
            } catch (e: Exception) {
                null
            }
        }
        Result.success(users)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserByEmail(email: String): Result<User?> = try {
        val snapshot = usersCollection.whereEqualTo("email", email).limit(1).get().await()
        if (snapshot.documents.isNotEmpty()) {
            val doc = snapshot.documents.first()
            val user = User(
                id = doc.id,
                email = doc.getString("email") ?: "",
                nombre = doc.getString("nombre") ?: "",
                apellido = doc.getString("apellido") ?: "",
                rol = doc.getString("rol") ?: "",
                activo = doc.getBoolean("activo") ?: true,
                fechaCreacion = doc.getLong("fechaCreacion") ?: 0L,
                ultimoAcceso = doc.getLong("ultimoAcceso") ?: 0L,
                permisos = (doc.get("permisos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            )
            Result.success(user)
        } else {
            Result.success(null)
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun logUserActivity(activity: UserActivity): Result<Unit> = try {
        val activityData = activity.copy(
            timestamp = System.currentTimeMillis()
        )
        activitiesCollection.add(activityData).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getUserActivities(userId: String, limit: Int): Result<List<UserActivity>> = try {
        val snapshot = activitiesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        
        val activities = snapshot.documents.mapNotNull { doc ->
            try {
                UserActivity(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    accion = doc.getString("accion") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    materialId = doc.getString("materialId") ?: "",
                    cantidad = (doc.getLong("cantidad") ?: 0L).toInt(),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    ipAddress = doc.getString("ipAddress") ?: "",
                    deviceInfo = doc.getString("deviceInfo") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
        Result.success(activities)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllActivities(limit: Int): Result<List<UserActivity>> = try {
        val snapshot = activitiesCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .get()
            .await()
        
        val activities = snapshot.documents.mapNotNull { doc ->
            try {
                UserActivity(
                    id = doc.id,
                    userId = doc.getString("userId") ?: "",
                    accion = doc.getString("accion") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    materialId = doc.getString("materialId") ?: "",
                    cantidad = (doc.getLong("cantidad") ?: 0L).toInt(),
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    ipAddress = doc.getString("ipAddress") ?: "",
                    deviceInfo = doc.getString("deviceInfo") ?: ""
                )
            } catch (e: Exception) {
                null
            }
        }
        Result.success(activities)
    } catch (e: Exception) {
        Result.failure(e)
    }
}


