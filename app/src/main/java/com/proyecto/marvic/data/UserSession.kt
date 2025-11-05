package com.proyecto.marvic.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

object UserSession {
    var currentRole: String = "almacenero"
    
    // Firebase Auth integration
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isLoggedIn: Boolean
        get() = auth.currentUser != null
    
    val userId: String
        get() = auth.currentUser?.uid ?: "SYSTEM"
    
    val userEmail: String
        get() = auth.currentUser?.email ?: "unknown"
    
    fun setRole(role: String) {
        currentRole = when(role.lowercase()) {
            "almacenero" -> "almacenero"
            "jefe de logística", "jefe de logistica" -> "jefe_logistica"
            "gerente" -> "gerente"
            else -> "almacenero"
        }
    }
    
    fun canAccessReports(): Boolean = currentRole in listOf("jefe_logistica", "gerente")
    fun canAccessSearch(): Boolean = currentRole in listOf("jefe_logistica", "gerente")
    fun canAccessMovement(): Boolean = true // Todos pueden registrar movimientos
    fun canAccessProviders(): Boolean = currentRole in listOf("jefe_logistica", "gerente")
    fun canAccessProjects(): Boolean = currentRole in listOf("jefe_logistica", "gerente")
    fun canAccessTransfers(): Boolean = currentRole in listOf("jefe_logistica", "gerente")
    fun canAccessAnalytics(): Boolean = currentRole == "gerente"
    fun canAccessUserManagement(): Boolean = currentRole == "gerente"
    fun isAdmin(): Boolean = currentRole == "gerente"
    fun isJefeOrAbove(): Boolean = currentRole in listOf("jefe_logistica", "gerente")
    
    fun logout() {
        auth.signOut()
        currentRole = "almacenero"
    }
}


