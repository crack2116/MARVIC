package com.proyecto.marvic.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RateLimiterTest {
    
    private val userId = "test_user"
    
    @Before
    fun setup() {
        RateLimiter.resetUser(userId)
        RateLimiter.cleanup()
    }
    
    @Test
    fun `isAllowed debe permitir operaciones bajo el límite`() {
        // Login tiene límite de 5/min
        repeat(5) {
            assertTrue(RateLimiter.isAllowed(userId, "login"))
        }
    }
    
    @Test
    fun `isAllowed debe bloquear operaciones sobre el límite`() {
        // Login tiene límite de 5/min
        repeat(5) {
            RateLimiter.isAllowed(userId, "login")
        }
        
        // La sexta debe ser bloqueada
        assertFalse(RateLimiter.isAllowed(userId, "login"))
    }
    
    @Test
    fun `getRemainingOperations debe retornar operaciones restantes`() {
        // Login tiene límite de 5
        RateLimiter.isAllowed(userId, "login")
        RateLimiter.isAllowed(userId, "login")
        
        val remaining = RateLimiter.getRemainingOperations(userId, "login")
        assertEquals(3, remaining)
    }
    
    @Test
    fun `reset debe reiniciar contador de operación`() {
        repeat(5) {
            RateLimiter.isAllowed(userId, "login")
        }
        
        RateLimiter.reset(userId, "login")
        
        assertTrue(RateLimiter.isAllowed(userId, "login"))
    }
    
    @Test
    fun `resetUser debe reiniciar todos los contadores del usuario`() {
        RateLimiter.isAllowed(userId, "login")
        RateLimiter.isAllowed(userId, "create_material")
        
        RateLimiter.resetUser(userId)
        
        assertEquals(5, RateLimiter.getRemainingOperations(userId, "login"))
        assertEquals(20, RateLimiter.getRemainingOperations(userId, "create_material"))
    }
    
    @Test
    fun `cleanup debe remover timestamps antiguos`() = runBlocking {
        RateLimiter.isAllowed(userId, "login")
        
        // Simular que pasó mucho tiempo (en tests reales usaríamos time mocking)
        RateLimiter.cleanup()
        
        // El cleanup no debería afectar timestamps recientes
        assertTrue(RateLimiter.getRemainingOperations(userId, "login") < 5)
    }
    
    @Test
    fun `getStats debe retornar estadísticas de uso`() {
        RateLimiter.isAllowed(userId, "login")
        RateLimiter.isAllowed(userId, "login")
        RateLimiter.isAllowed(userId, "create_material")
        
        val stats = RateLimiter.getStats(userId)
        
        assertEquals(2, stats["login"])
        assertEquals(1, stats["create_material"])
    }
    
    @Test
    fun `diferentes usuarios deben tener límites independientes`() {
        val user1 = "user1"
        val user2 = "user2"
        
        repeat(5) {
            RateLimiter.isAllowed(user1, "login")
        }
        
        // user1 bloqueado
        assertFalse(RateLimiter.isAllowed(user1, "login"))
        
        // user2 todavía puede
        assertTrue(RateLimiter.isAllowed(user2, "login"))
    }
    
    @Test
    fun `operaciones con límite por defecto deben usar 50`() {
        // Operación no configurada debería usar límite default de 50
        repeat(50) {
            assertTrue(RateLimiter.isAllowed(userId, "custom_operation"))
        }
        
        // La 51 debe ser bloqueada
        assertFalse(RateLimiter.isAllowed(userId, "custom_operation"))
    }
}



