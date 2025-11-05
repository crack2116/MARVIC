package com.proyecto.marvic.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

interface AuthRepository {
    suspend fun signIn(email: String, password: String): Result<Unit>
    fun isSignedIn(): Boolean
    fun signOut()
    suspend fun getRole(): String?
}

class FirebaseAuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<Unit> = try {
        auth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (t: Throwable) {
        Result.failure(t)
    }

    override fun isSignedIn(): Boolean = auth.currentUser != null
    override fun signOut() { auth.signOut() }
    override suspend fun getRole(): String? = try {
        auth.currentUser?.getIdToken(true)?.await()?.claims?.get("role") as? String
    } catch (_: Throwable) { null }
}


