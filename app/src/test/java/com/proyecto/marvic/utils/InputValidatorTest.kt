package com.proyecto.marvic.utils

import org.junit.Assert.*
import org.junit.Test

class InputValidatorTest {
    
    @Test
    fun `email válido debe retornar true`() {
        assertTrue(InputValidator.isValidEmail("test@example.com"))
        assertTrue(InputValidator.isValidEmail("user.name@company.org"))
        assertTrue(InputValidator.isValidEmail("first+last@domain.co.uk"))
    }
    
    @Test
    fun `email inválido debe retornar false`() {
        assertFalse(InputValidator.isValidEmail(""))
        assertFalse(InputValidator.isValidEmail("invalid"))
        assertFalse(InputValidator.isValidEmail("@example.com"))
        assertFalse(InputValidator.isValidEmail("test@"))
        assertFalse(InputValidator.isValidEmail("test @example.com"))
    }
    
    @Test
    fun `teléfono válido debe retornar true`() {
        assertTrue(InputValidator.isValidPhone("987654321"))
        assertTrue(InputValidator.isValidPhone("123456789"))
        assertTrue(InputValidator.isValidPhone("(01) 234-5678"))
        assertTrue(InputValidator.isValidPhone("+51 987654321"))
    }
    
    @Test
    fun `teléfono inválido debe retornar false`() {
        assertFalse(InputValidator.isValidPhone(""))
        assertFalse(InputValidator.isValidPhone("12345"))
        assertFalse(InputValidator.isValidPhone("abc123"))
        assertFalse(InputValidator.isValidPhone("12345678901234567"))
    }
    
    @Test
    fun `RUC válido debe retornar true`() {
        assertTrue(InputValidator.isValidRUC("20123456789"))
        assertTrue(InputValidator.isValidRUC("10987654321"))
    }
    
    @Test
    fun `RUC inválido debe retornar false`() {
        assertFalse(InputValidator.isValidRUC(""))
        assertFalse(InputValidator.isValidRUC("123456789"))
        assertFalse(InputValidator.isValidRUC("123456789012"))
        assertFalse(InputValidator.isValidRUC("ABCDEFGHIJK"))
    }
    
    @Test
    fun `código válido debe retornar true`() {
        assertTrue(InputValidator.isValidCodigo("MAT001"))
        assertTrue(InputValidator.isValidCodigo("PROY-2024"))
        assertTrue(InputValidator.isValidCodigo("CODE_123"))
    }
    
    @Test
    fun `código inválido debe retornar false`() {
        assertFalse(InputValidator.isValidCodigo(""))
        assertFalse(InputValidator.isValidCodigo("código inválido"))
        assertFalse(InputValidator.isValidCodigo("A".repeat(21)))
    }
    
    @Test
    fun `cantidad válida debe retornar true`() {
        assertTrue(InputValidator.isValidCantidad("1"))
        assertTrue(InputValidator.isValidCantidad("100"))
        assertTrue(InputValidator.isValidCantidad("999999"))
    }
    
    @Test
    fun `cantidad inválida debe retornar false`() {
        assertFalse(InputValidator.isValidCantidad(""))
        assertFalse(InputValidator.isValidCantidad("0"))
        assertFalse(InputValidator.isValidCantidad("-5"))
        assertFalse(InputValidator.isValidCantidad("abc"))
        assertFalse(InputValidator.isValidCantidad("1000001"))
    }
    
    @Test
    fun `precio válido debe retornar true`() {
        assertTrue(InputValidator.isValidPrecio("0"))
        assertTrue(InputValidator.isValidPrecio("100.50"))
        assertTrue(InputValidator.isValidPrecio("9999999.99"))
    }
    
    @Test
    fun `precio inválido debe retornar false`() {
        assertFalse(InputValidator.isValidPrecio(""))
        assertFalse(InputValidator.isValidPrecio("-10"))
        assertFalse(InputValidator.isValidPrecio("abc"))
        assertFalse(InputValidator.isValidPrecio("10000001"))
    }
    
    @Test
    fun `presupuesto válido debe retornar true`() {
        assertTrue(InputValidator.isValidPresupuesto("0"))
        assertTrue(InputValidator.isValidPresupuesto("1000000"))
        assertTrue(InputValidator.isValidPresupuesto("99999999.99"))
    }
    
    @Test
    fun `sanitize debe remover caracteres peligrosos`() {
        val dangerous = "SELECT * FROM users; DROP TABLE"
        val sanitized = InputValidator.sanitize(dangerous)
        assertFalse(sanitized.contains(";"))
        assertFalse(sanitized.contains("DROP"))
    }
    
    @Test
    fun `sanitize debe limitar longitud`() {
        val longString = "A".repeat(600)
        val sanitized = InputValidator.sanitize(longString)
        assertTrue(sanitized.length <= 500)
    }
    
    @Test
    fun `sanitizeName debe permitir solo caracteres válidos`() {
        val name = "Juan Pérez <script>alert('xss')</script>"
        val sanitized = InputValidator.sanitizeName(name)
        assertFalse(sanitized.contains("<"))
        assertFalse(sanitized.contains(">"))
        assertTrue(sanitized.contains("Juan"))
    }
    
    @Test
    fun `sanitizeAddress debe permitir caracteres de dirección`() {
        val address = "Av. Principal #123, Lima"
        val sanitized = InputValidator.sanitizeAddress(address)
        assertTrue(sanitized.contains("Av"))
        assertTrue(sanitized.contains("#"))
        assertTrue(sanitized.contains(","))
    }
    
    @Test
    fun `validateMaterial debe validar todos los campos`() {
        val result = InputValidator.validateMaterial(
            codigo = "MAT001",
            nombre = "Cemento",
            cantidad = "100",
            ubicacion = "Almacén 1"
        )
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
    
    @Test
    fun `validateMaterial debe fallar con código inválido`() {
        val result = InputValidator.validateMaterial(
            codigo = "código inválido",
            nombre = "Cemento",
            cantidad = "100",
            ubicacion = "Almacén 1"
        )
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
    }
    
    @Test
    fun `validateProvider debe validar todos los campos`() {
        val result = InputValidator.validateProvider(
            nombre = "Proveedor S.A.",
            ruc = "20123456789",
            email = "info@proveedor.com",
            telefono = "987654321"
        )
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateProvider debe fallar con RUC inválido`() {
        val result = InputValidator.validateProvider(
            nombre = "Proveedor S.A.",
            ruc = "123",
            email = "info@proveedor.com",
            telefono = "987654321"
        )
        assertFalse(result.isValid)
    }
    
    @Test
    fun `validateTransfer debe validar todos los campos`() {
        val result = InputValidator.validateTransfer(
            materialId = "MAT001",
            cantidad = "50",
            origen = "Almacén 1",
            destino = "Almacén 2",
            responsable = "Juan Pérez"
        )
        assertTrue(result.isValid)
    }
    
    @Test
    fun `validateTransfer debe fallar con origen igual a destino`() {
        val result = InputValidator.validateTransfer(
            materialId = "MAT001",
            cantidad = "50",
            origen = "Almacén 1",
            destino = "Almacén 1",
            responsable = "Juan Pérez"
        )
        assertFalse(result.isValid)
    }
    
    @Test
    fun `isValidPercentage debe validar rango 0-100`() {
        assertTrue(InputValidator.isValidPercentage("0"))
        assertTrue(InputValidator.isValidPercentage("50"))
        assertTrue(InputValidator.isValidPercentage("100"))
        assertFalse(InputValidator.isValidPercentage("-1"))
        assertFalse(InputValidator.isValidPercentage("101"))
    }
    
    @Test
    fun `isValidRating debe validar rango 0-5`() {
        assertTrue(InputValidator.isValidRating(0.0))
        assertTrue(InputValidator.isValidRating(2.5))
        assertTrue(InputValidator.isValidRating(5.0))
        assertFalse(InputValidator.isValidRating(-0.1))
        assertFalse(InputValidator.isValidRating(5.1))
    }
}



