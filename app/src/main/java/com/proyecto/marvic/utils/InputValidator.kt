package com.proyecto.marvic.utils

import java.util.regex.Pattern

/**
 * Utilidad para validar y sanitizar inputs del usuario
 */
object InputValidator {
    
    // Patrones de validación
    private val EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
    
    private val PHONE_PATTERN = Pattern.compile("^[0-9]{9,15}$")
    private val RUC_PATTERN = Pattern.compile("^[0-9]{11}$")
    private val CODIGO_PATTERN = Pattern.compile("^[A-Z0-9_-]+$")
    
    // Caracteres peligrosos para SQL injection
    private val DANGEROUS_CHARS = listOf(
        "'", "\"", ";", "--", "/*", "*/", "xp_", "sp_", 
        "<script", "javascript:", "onerror=", "onload="
    )
    
    /**
     * Valida un email
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && EMAIL_PATTERN.matcher(email).matches()
    }
    
    /**
     * Valida un teléfono
     */
    fun isValidPhone(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[\\s()-]"), "")
        return PHONE_PATTERN.matcher(cleanPhone).matches()
    }
    
    /**
     * Valida un RUC (Perú)
     */
    fun isValidRUC(ruc: String): Boolean {
        return RUC_PATTERN.matcher(ruc).matches()
    }
    
    /**
     * Valida un código de material/proyecto
     */
    fun isValidCodigo(codigo: String): Boolean {
        return codigo.isNotEmpty() && 
               codigo.length <= 20 && 
               CODIGO_PATTERN.matcher(codigo).matches()
    }
    
    /**
     * Valida una cantidad numérica
     */
    fun isValidCantidad(cantidad: String): Boolean {
        return try {
            val num = cantidad.toInt()
            num > 0 && num <= 1000000
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Valida un precio
     */
    fun isValidPrecio(precio: String): Boolean {
        return try {
            val num = precio.toDouble()
            num >= 0.0 && num <= 10000000.0
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Valida un presupuesto
     */
    fun isValidPresupuesto(presupuesto: String): Boolean {
        return try {
            val num = presupuesto.toDouble()
            num >= 0.0 && num <= 100000000.0
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Sanitiza un string para prevenir injection
     */
    fun sanitize(input: String): String {
        var sanitized = input.trim()
        
        // Remover caracteres peligrosos
        DANGEROUS_CHARS.forEach { dangerous ->
            sanitized = sanitized.replace(dangerous, "", ignoreCase = true)
        }
        
        // Limitar longitud
        if (sanitized.length > 500) {
            sanitized = sanitized.substring(0, 500)
        }
        
        return sanitized
    }
    
    /**
     * Sanitiza un nombre (permite letras, números, espacios, guiones)
     */
    fun sanitizeName(name: String): String {
        return name.trim()
            .replace(Regex("[^a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-_.]"), "")
            .take(100)
    }
    
    /**
     * Sanitiza una dirección
     */
    fun sanitizeAddress(address: String): String {
        return address.trim()
            .replace(Regex("[^a-zA-ZáéíóúÁÉÍÓÚñÑ0-9\\s\\-#.,]"), "")
            .take(200)
    }
    
    /**
     * Sanitiza notas/observaciones
     */
    fun sanitizeNotes(notes: String): String {
        return notes.trim()
            .replace(Regex("[<>{}\\[\\]]"), "")
            .take(500)
    }
    
    /**
     * Valida que un string no esté vacío y tenga longitud mínima
     */
    fun isNotEmptyAndMinLength(text: String, minLength: Int = 1): Boolean {
        return text.trim().length >= minLength
    }
    
    /**
     * Valida longitud máxima
     */
    fun isMaxLength(text: String, maxLength: Int): Boolean {
        return text.length <= maxLength
    }
    
    /**
     * Valida rango de fecha (no puede ser futuro lejano)
     */
    fun isValidDate(timestamp: Long): Boolean {
        val now = System.currentTimeMillis()
        val tenYearsFromNow = now + (10 * 365 * 24 * 60 * 60 * 1000L)
        return timestamp in 0..tenYearsFromNow
    }
    
    /**
     * Valida URL de imagen
     */
    fun isValidImageUrl(url: String): Boolean {
        return url.startsWith("https://") && 
               (url.contains("firebasestorage.googleapis.com") || 
                url.contains("storage.googleapis.com"))
    }
    
    /**
     * Valida un porcentaje (0-100)
     */
    fun isValidPercentage(percentage: String): Boolean {
        return try {
            val num = percentage.toInt()
            num in 0..100
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Valida calificación (0.0-5.0)
     */
    fun isValidRating(rating: Double): Boolean {
        return rating in 0.0..5.0
    }
    
    /**
     * Resultado de validación con mensaje
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * Valida todos los campos de un material
     */
    fun validateMaterial(
        codigo: String,
        nombre: String,
        cantidad: String,
        ubicacion: String
    ): ValidationResult {
        return when {
            !isValidCodigo(codigo) -> ValidationResult(
                false, 
                "Código inválido. Use solo letras mayúsculas, números y guiones"
            )
            !isNotEmptyAndMinLength(nombre, 3) -> ValidationResult(
                false,
                "El nombre debe tener al menos 3 caracteres"
            )
            !isValidCantidad(cantidad) -> ValidationResult(
                false,
                "Cantidad inválida. Debe ser un número positivo menor a 1,000,000"
            )
            !isNotEmptyAndMinLength(ubicacion, 2) -> ValidationResult(
                false,
                "La ubicación debe tener al menos 2 caracteres"
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Valida campos de un proveedor
     */
    fun validateProvider(
        nombre: String,
        ruc: String,
        email: String,
        telefono: String
    ): ValidationResult {
        return when {
            !isNotEmptyAndMinLength(nombre, 3) -> ValidationResult(
                false,
                "El nombre debe tener al menos 3 caracteres"
            )
            !isValidRUC(ruc) -> ValidationResult(
                false,
                "RUC inválido. Debe tener 11 dígitos"
            )
            email.isNotEmpty() && !isValidEmail(email) -> ValidationResult(
                false,
                "Email inválido"
            )
            telefono.isNotEmpty() && !isValidPhone(telefono) -> ValidationResult(
                false,
                "Teléfono inválido. Debe tener entre 9 y 15 dígitos"
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Valida campos de un proyecto
     */
    fun validateProject(
        codigo: String,
        nombre: String,
        presupuesto: String,
        responsable: String
    ): ValidationResult {
        return when {
            !isValidCodigo(codigo) -> ValidationResult(
                false,
                "Código inválido. Use solo letras mayúsculas, números y guiones"
            )
            !isNotEmptyAndMinLength(nombre, 3) -> ValidationResult(
                false,
                "El nombre debe tener al menos 3 caracteres"
            )
            !isValidPresupuesto(presupuesto) -> ValidationResult(
                false,
                "Presupuesto inválido"
            )
            !isNotEmptyAndMinLength(responsable, 3) -> ValidationResult(
                false,
                "El responsable debe tener al menos 3 caracteres"
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Valida campos de una transferencia
     */
    fun validateTransfer(
        materialId: String,
        cantidad: String,
        origen: String,
        destino: String,
        responsable: String
    ): ValidationResult {
        return when {
            materialId.isEmpty() -> ValidationResult(
                false,
                "Debe seleccionar un material"
            )
            !isValidCantidad(cantidad) -> ValidationResult(
                false,
                "Cantidad inválida"
            )
            origen.isEmpty() -> ValidationResult(
                false,
                "Debe especificar el almacén de origen"
            )
            destino.isEmpty() -> ValidationResult(
                false,
                "Debe especificar el almacén de destino"
            )
            origen == destino -> ValidationResult(
                false,
                "El origen y destino no pueden ser el mismo"
            )
            !isNotEmptyAndMinLength(responsable, 3) -> ValidationResult(
                false,
                "El responsable debe tener al menos 3 caracteres"
            )
            else -> ValidationResult(true)
        }
    }
}



