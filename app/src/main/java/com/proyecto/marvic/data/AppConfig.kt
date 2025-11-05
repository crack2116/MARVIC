package com.proyecto.marvic.data

/**
 * Configuración global de la aplicación
 */
object AppConfig {
    /**
     * Si es TRUE: Valida usuarios contra Firestore (sistema completo de roles)
     * Si es FALSE: Solo usa Firebase Auth (comportamiento anterior más simple)
     * 
     * Cambiar a FALSE si quieres el comportamiento simple original
     */
    const val REQUIRE_FIRESTORE_USER = false  // ← CAMBIADO A FALSE para simplificar
}



