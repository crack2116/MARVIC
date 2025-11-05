package com.proyecto.marvic.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    
    fun formatDate(timestamp: Long): String {
        if (timestamp == 0L) return "Sin fecha"
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("es", "ES"))
        return format.format(date)
    }
    
    fun formatDateShort(timestamp: Long): String {
        if (timestamp == 0L) return "Sin fecha"
        val date = Date(timestamp)
        val format = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
        return format.format(date)
    }
    
    fun getCurrentTimestamp(): Long = System.currentTimeMillis()
}



