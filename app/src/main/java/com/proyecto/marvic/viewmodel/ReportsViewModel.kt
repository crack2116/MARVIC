package com.proyecto.marvic.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyecto.marvic.data.FirestoreInventoryRepository
import com.proyecto.marvic.data.InventoryRepository
import com.proyecto.marvic.data.Movement
import kotlinx.coroutines.launch

class ReportsViewModel(private val repo: InventoryRepository = FirestoreInventoryRepository()) : ViewModel() {
    val items = mutableStateListOf<Movement>()

    fun load() {
        viewModelScope.launch {
            val r = repo.recentMovements()
            if (r.isSuccess) {
                items.clear(); items.addAll(r.getOrDefault(emptyList()))
            }
        }
    }
}


