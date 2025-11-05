package com.proyecto.marvic.utils

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class LazyListOptimizerTest {
    
    @Test
    fun `paginateList debe retornar primera página correctamente`() {
        val fullList = (1..100).toList()
        val page = LazyListOptimizer.paginateList(fullList, pageSize = 20, currentPage = 0)
        
        assertEquals(20, page.size)
        assertEquals(1, page.first())
        assertEquals(20, page.last())
    }
    
    @Test
    fun `paginateList debe retornar segunda página correctamente`() {
        val fullList = (1..100).toList()
        val page = LazyListOptimizer.paginateList(fullList, pageSize = 20, currentPage = 1)
        
        assertEquals(20, page.size)
        assertEquals(21, page.first())
        assertEquals(40, page.last())
    }
    
    @Test
    fun `paginateList debe manejar última página parcial`() {
        val fullList = (1..95).toList()
        val page = LazyListOptimizer.paginateList(fullList, pageSize = 20, currentPage = 4)
        
        assertEquals(15, page.size)
        assertEquals(81, page.first())
        assertEquals(95, page.last())
    }
    
    @Test
    fun `paginateList debe retornar lista vacía para página fuera de rango`() {
        val fullList = (1..50).toList()
        val page = LazyListOptimizer.paginateList(fullList, pageSize = 20, currentPage = 10)
        
        assertTrue(page.isEmpty())
    }
    
    @Test
    fun `shouldLoadNextPage debe retornar true cerca del final`() {
        val shouldLoad = LazyListOptimizer.shouldLoadNextPage(
            currentIndex = 17,
            totalLoaded = 20,
            pageSize = 20
        )
        
        assertTrue(shouldLoad)
    }
    
    @Test
    fun `shouldLoadNextPage debe retornar false lejos del final`() {
        val shouldLoad = LazyListOptimizer.shouldLoadNextPage(
            currentIndex = 5,
            totalLoaded = 20,
            pageSize = 20
        )
        
        assertFalse(shouldLoad)
    }
    
    @Test
    fun `calculateTotalPages debe calcular correctamente`() {
        assertEquals(5, LazyListOptimizer.calculateTotalPages(100, 20))
        assertEquals(6, LazyListOptimizer.calculateTotalPages(101, 20))
        assertEquals(1, LazyListOptimizer.calculateTotalPages(15, 20))
    }
    
    @Test
    fun `createCacheKey debe generar clave única`() {
        val key1 = LazyListOptimizer.createCacheKey("search", "query", 1, true)
        val key2 = LazyListOptimizer.createCacheKey("search", "query", 1, true)
        val key3 = LazyListOptimizer.createCacheKey("search", "query", 2, true)
        
        assertEquals(key1, key2)
        assertNotEquals(key1, key3)
    }
    
    @Test
    fun `PaginatedList debe cargar primera página`() = runTest {
        val paginated = PaginatedList<Int>(pageSize = 10) { page, size ->
            val start = page * size
            (start until start + size).toList()
        }
        
        val items = paginated.loadMore()
        
        assertEquals(10, items.size)
        assertEquals(0, items.first())
        assertEquals(9, items.last())
    }
    
    @Test
    fun `PaginatedList debe cargar múltiples páginas`() = runTest {
        val paginated = PaginatedList<Int>(pageSize = 10) { page, size ->
            val start = page * size
            (start until start + size).toList()
        }
        
        paginated.loadMore()
        val items = paginated.loadMore()
        
        assertEquals(20, items.size)
        assertEquals(10, items[10])
        assertEquals(19, items.last())
    }
    
    @Test
    fun `PaginatedList debe detectar fin de datos`() = runTest {
        var callCount = 0
        val paginated = PaginatedList<Int>(pageSize = 10) { _, _ ->
            callCount++
            if (callCount <= 2) {
                (1..10).toList()
            } else {
                emptyList()
            }
        }
        
        paginated.loadMore()
        paginated.loadMore()
        paginated.loadMore()
        
        assertFalse(paginated.canLoadMore())
    }
    
    @Test
    fun `PaginatedList reset debe limpiar datos`() = runTest {
        val paginated = PaginatedList<Int>(pageSize = 10) { page, size ->
            val start = page * size
            (start until start + size).toList()
        }
        
        paginated.loadMore()
        paginated.reset()
        
        val items = paginated.getLoadedItems()
        assertTrue(items.isEmpty())
        assertTrue(paginated.canLoadMore())
    }
}



