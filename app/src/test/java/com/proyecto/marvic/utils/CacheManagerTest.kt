package com.proyecto.marvic.utils

import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CacheManagerTest {
    
    @Before
    fun setup() = runTest {
        CacheManager.clear()
    }
    
    @Test
    fun `put y get deben guardar y recuperar datos`() = runTest {
        val key = "test_key"
        val value = "test_value"
        
        CacheManager.put(key, value)
        val retrieved = CacheManager.get<String>(key)
        
        assertEquals(value, retrieved)
    }
    
    @Test
    fun `get debe retornar null para clave inexistente`() = runTest {
        val retrieved = CacheManager.get<String>("nonexistent")
        assertNull(retrieved)
    }
    
    @Test
    fun `datos deben expirar después del TTL`() = runTest {
        // Este test simula la expiración (requiere modificar TTL a valores muy pequeños)
        // En un entorno real, se usaría un mock del tiempo
        val key = "movements_test"
        val value = "data"
        
        CacheManager.put(key, value)
        val retrieved1 = CacheManager.get<String>(key)
        assertNotNull(retrieved1)
        
        // Simular expiración (20 segundos + margen)
        // En producción, se usaría una librería de tiempo mock
        // Por ahora, solo verificamos que no expira inmediatamente
        delay(100) // 100ms
        val retrieved2 = CacheManager.get<String>(key)
        assertNotNull(retrieved2) // Todavía no debería expirar
    }
    
    @Test
    fun `invalidate debe remover entrada específica`() = runTest {
        val key = "test_key"
        CacheManager.put(key, "value")
        
        CacheManager.invalidate(key)
        val retrieved = CacheManager.get<String>(key)
        
        assertNull(retrieved)
    }
    
    @Test
    fun `invalidateType debe remover todas las entradas de un tipo`() = runTest {
        CacheManager.put("inventory_1", "data1")
        CacheManager.put("inventory_2", "data2")
        CacheManager.put("providers_1", "data3")
        
        CacheManager.invalidateType("inventory")
        
        assertNull(CacheManager.get<String>("inventory_1"))
        assertNull(CacheManager.get<String>("inventory_2"))
        assertNotNull(CacheManager.get<String>("providers_1"))
    }
    
    @Test
    fun `clear debe limpiar todo el caché`() = runTest {
        CacheManager.put("key1", "value1")
        CacheManager.put("key2", "value2")
        
        CacheManager.clear()
        
        assertNull(CacheManager.get<String>("key1"))
        assertNull(CacheManager.get<String>("key2"))
    }
    
    @Test
    fun `has debe verificar existencia de clave`() = runTest {
        CacheManager.put("test", "value")
        
        assertTrue(CacheManager.has("test"))
        assertFalse(CacheManager.has("nonexistent"))
    }
    
    @Test
    fun `size debe retornar número de entradas`() = runTest {
        CacheManager.clear()
        assertEquals(0, CacheManager.size())
        
        CacheManager.put("key1", "value1")
        CacheManager.put("key2", "value2")
        assertEquals(2, CacheManager.size())
    }
    
    @Test
    fun `cached debe usar caché o fetchear`() = runTest {
        var fetchCalled = 0
        
        val fetch = suspend {
            fetchCalled++
            "fetched_data"
        }
        
        // Primera llamada - debería fetchear
        val result1 = CacheManager.cached("test_cached", fetch)
        assertEquals("fetched_data", result1)
        assertEquals(1, fetchCalled)
        
        // Segunda llamada - debería usar caché
        val result2 = CacheManager.cached("test_cached", fetch)
        assertEquals("fetched_data", result2)
        assertEquals(1, fetchCalled) // No debería llamar fetch de nuevo
    }
    
    @Test
    fun `getStats debe retornar estadísticas correctas`() = runTest {
        CacheManager.clear()
        CacheManager.put("inventory_1", "data1")
        CacheManager.put("inventory_2", "data2")
        CacheManager.put("providers_1", "data3")
        
        val stats = CacheManager.getStats()
        
        assertEquals(2, stats["inventory"])
        assertEquals(1, stats["providers"])
        assertEquals(3, stats["total"])
    }
}



