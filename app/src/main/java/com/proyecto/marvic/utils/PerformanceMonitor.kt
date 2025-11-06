package com.proyecto.marvic.utils

import kotlin.system.measureTimeMillis

/**
 * Monitor de rendimiento para medir tiempos de ejecución
 */
object PerformanceMonitor {
    
    private data class PerformanceMetric(
        val operation: String,
        val durationMs: Long,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    private val metrics = mutableListOf<PerformanceMetric>()
    private const val MAX_METRICS = 100 // Mantener solo las últimas 100 métricas
    
    /**
     * Mide el tiempo de ejecución de una operación
     */
    suspend fun <T> measure(operationName: String, block: suspend () -> T): T {
        val result: T
        val duration = measureTimeMillis {
            result = block()
        }
        
        addMetric(operationName, duration)
        logMetric(operationName, duration)
        
        return result
    }
    
    /**
     * Mide el tiempo de ejecución de una operación síncrona
     */
    fun <T> measureSync(operationName: String, block: () -> T): T {
        val result: T
        val duration = measureTimeMillis {
            result = block()
        }
        
        addMetric(operationName, duration)
        logMetric(operationName, duration)
        
        return result
    }
    
    /**
     * Agrega una métrica manual
     */
    fun addMetric(operation: String, durationMs: Long) {
        synchronized(metrics) {
            metrics.add(PerformanceMetric(operation, durationMs))
            
            // Mantener solo las últimas MAX_METRICS métricas
            if (metrics.size > MAX_METRICS) {
                metrics.removeAt(0)
            }
        }
    }
    
    /**
     * Obtiene el promedio de tiempo para una operación
     */
    fun getAverageDuration(operationName: String): Long {
        synchronized(metrics) {
            val filtered = metrics.filter { it.operation == operationName }
            if (filtered.isEmpty()) return 0L
            return filtered.map { it.durationMs }.average().toLong()
        }
    }
    
    /**
     * Obtiene las operaciones más lentas
     */
    fun getSlowestOperations(limit: Int = 10): List<Pair<String, Long>> {
        synchronized(metrics) {
            return metrics
                .groupBy { it.operation }
                .mapValues { (_, list) -> list.map { it.durationMs }.average().toLong() }
                .toList()
                .sortedByDescending { it.second }
                .take(limit)
        }
    }
    
    /**
     * Obtiene todas las métricas
     */
    fun getAllMetrics(): List<Pair<String, Long>> {
        synchronized(metrics) {
            return metrics.map { it.operation to it.durationMs }
        }
    }
    
    /**
     * Limpia todas las métricas
     */
    fun clear() {
        synchronized(metrics) {
            metrics.clear()
        }
    }
    
    /**
     * Genera un reporte de rendimiento
     */
    fun generateReport(): String {
        synchronized(metrics) {
            if (metrics.isEmpty()) {
                return "No hay métricas de rendimiento disponibles"
            }
            
            val report = StringBuilder()
            report.appendLine("=== REPORTE DE RENDIMIENTO ===")
            report.appendLine("Total de métricas: ${metrics.size}")
            report.appendLine()
            
            // Agrupar por operación
            val grouped = metrics.groupBy { it.operation }
            
            report.appendLine("Por operación:")
            grouped.forEach { (operation, list) ->
                val avg = list.map { it.durationMs }.average()
                val min = list.minOf { it.durationMs }
                val max = list.maxOf { it.durationMs }
                val count = list.size
                
                report.appendLine("  $operation:")
                report.appendLine("    Llamadas: $count")
                report.appendLine("    Promedio: ${avg.toLong()}ms")
                report.appendLine("    Mínimo: ${min}ms")
                report.appendLine("    Máximo: ${max}ms")
            }
            
            report.appendLine()
            report.appendLine("Operaciones más lentas (promedio):")
            getSlowestOperations(5).forEachIndexed { index, (op, duration) ->
                report.appendLine("  ${index + 1}. $op: ${duration}ms")
            }
            
            return report.toString()
        }
    }
    
    /**
     * Log de métrica
     */
    private fun logMetric(operation: String, durationMs: Long) {
        val level = when {
            durationMs < 100 -> "✅"
            durationMs < 500 -> "⚠️"
            else -> "❌"
        }
        println("[$level PERFORMANCE] $operation: ${durationMs}ms")
    }
    
    /**
     * Verifica si una operación es lenta
     */
    fun isSlow(operationName: String, thresholdMs: Long = 500): Boolean {
        val avg = getAverageDuration(operationName)
        return avg > thresholdMs
    }
}





