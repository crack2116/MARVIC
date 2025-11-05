package com.proyecto.marvic.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class PerformanceMonitorTest {
    
    @Before
    fun setup() {
        PerformanceMonitor.clear()
    }
    
    @Test
    fun `measure debe ejecutar bloque y retornar resultado`() = runTest {
        val result = PerformanceMonitor.measure("test_operation") {
            delay(10)
            "result"
        }
        
        assertEquals("result", result)
    }
    
    @Test
    fun `measure debe registrar métrica`() = runTest {
        PerformanceMonitor.measure("test_operation") {
            delay(10)
            "result"
        }
        
        val average = PerformanceMonitor.getAverageDuration("test_operation")
        assertTrue(average >= 10) // Al menos 10ms
    }
    
    @Test
    fun `measureSync debe ejecutar bloque y retornar resultado`() {
        val result = PerformanceMonitor.measureSync("sync_operation") {
            Thread.sleep(10)
            42
        }
        
        assertEquals(42, result)
    }
    
    @Test
    fun `getAverageDuration debe calcular promedio correctamente`() = runTest {
        PerformanceMonitor.addMetric("operation1", 100)
        PerformanceMonitor.addMetric("operation1", 200)
        PerformanceMonitor.addMetric("operation1", 300)
        
        val average = PerformanceMonitor.getAverageDuration("operation1")
        assertEquals(200L, average)
    }
    
    @Test
    fun `getAverageDuration debe retornar 0 para operación inexistente`() {
        val average = PerformanceMonitor.getAverageDuration("nonexistent")
        assertEquals(0L, average)
    }
    
    @Test
    fun `getSlowestOperations debe retornar operaciones ordenadas por duración`() {
        PerformanceMonitor.addMetric("fast", 50)
        PerformanceMonitor.addMetric("medium", 150)
        PerformanceMonitor.addMetric("slow", 500)
        
        val slowest = PerformanceMonitor.getSlowestOperations(3)
        
        assertEquals("slow", slowest[0].first)
        assertEquals("medium", slowest[1].first)
        assertEquals("fast", slowest[2].first)
    }
    
    @Test
    fun `getAllMetrics debe retornar todas las métricas`() {
        PerformanceMonitor.addMetric("op1", 100)
        PerformanceMonitor.addMetric("op2", 200)
        
        val all = PerformanceMonitor.getAllMetrics()
        assertEquals(2, all.size)
    }
    
    @Test
    fun `clear debe limpiar todas las métricas`() {
        PerformanceMonitor.addMetric("op1", 100)
        PerformanceMonitor.clear()
        
        val all = PerformanceMonitor.getAllMetrics()
        assertTrue(all.isEmpty())
    }
    
    @Test
    fun `isSlow debe detectar operaciones lentas`() {
        PerformanceMonitor.addMetric("slow_op", 600)
        PerformanceMonitor.addMetric("fast_op", 50)
        
        assertTrue(PerformanceMonitor.isSlow("slow_op", 500))
        assertFalse(PerformanceMonitor.isSlow("fast_op", 500))
    }
    
    @Test
    fun `generateReport debe generar reporte válido`() {
        PerformanceMonitor.addMetric("operation1", 100)
        PerformanceMonitor.addMetric("operation1", 200)
        PerformanceMonitor.addMetric("operation2", 150)
        
        val report = PerformanceMonitor.generateReport()
        
        assertTrue(report.contains("REPORTE DE RENDIMIENTO"))
        assertTrue(report.contains("operation1"))
        assertTrue(report.contains("operation2"))
        assertTrue(report.contains("Promedio"))
    }
    
    @Test
    fun `debe mantener solo últimas 100 métricas`() {
        // Agregar más de 100 métricas
        repeat(150) { i ->
            PerformanceMonitor.addMetric("operation", i.toLong())
        }
        
        val all = PerformanceMonitor.getAllMetrics()
        assertTrue(all.size <= 100)
    }
}



