package com.proyecto.marvic.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.proyecto.marvic.MainActivity
import com.proyecto.marvic.R

object SmartNotificationManager {
    private const val CHANNEL_ID_CRITICAL = "critical_stock"
    private const val CHANNEL_ID_WARNING = "stock_warning"
    private const val CHANNEL_ID_INFO = "stock_info"
    
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Canal crítico
            val criticalChannel = NotificationChannel(
                CHANNEL_ID_CRITICAL,
                "Stock Crítico",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alertas cuando el stock está críticamente bajo"
                enableVibration(true)
                enableLights(true)
            }
            
            // Canal de advertencia
            val warningChannel = NotificationChannel(
                CHANNEL_ID_WARNING,
                "Advertencias de Stock",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Advertencias cuando el stock está bajo"
                enableVibration(true)
            }
            
            // Canal informativo
            val infoChannel = NotificationChannel(
                CHANNEL_ID_INFO,
                "Información de Inventario",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Información general sobre el inventario"
            }
            
            notificationManager.createNotificationChannels(listOf(
                criticalChannel, warningChannel, infoChannel
            ))
        }
    }
    
    fun showCriticalStockAlert(context: Context, materialName: String, currentStock: Int, location: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_CRITICAL)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 STOCK CRÍTICO")
            .setContentText("$materialName: Solo $currentStock unidades restantes")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$materialName en $location\nStock actual: $currentStock unidades\n⚠️ Reposición urgente requerida"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setLights(0xFF0000, 1000, 1000)
            .build()
        
        // Verificar permisos antes de mostrar notificación
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(context).notify(
                "critical_${materialName.hashCode()}".hashCode(),
                notification
            )
        }
    }
    
    fun showLowStockWarning(context: Context, materialName: String, currentStock: Int, location: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_WARNING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ Stock Bajo")
            .setContentText("$materialName: $currentStock unidades restantes")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$materialName en $location\nStock actual: $currentStock unidades\n📋 Considerar reposición pronto"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Verificar permisos antes de mostrar notificación
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(context).notify(
                "warning_${materialName.hashCode()}".hashCode(),
                notification
            )
        }
    }
    
    fun showReplenishmentReminder(context: Context, materialName: String, daysLeft: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INFO)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("📋 Recordatorio de Reposición")
            .setContentText("$materialName se agotará en $daysLeft días")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Material: $materialName\nTiempo estimado hasta agotarse: $daysLeft días\n💡 Programar pedido de reposición"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Verificar permisos antes de mostrar notificación
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(context).notify(
                "reminder_${materialName.hashCode()}".hashCode(),
                notification
            )
        }
    }
    
    fun showMovementNotification(context: Context, materialName: String, movementType: String, quantity: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val emoji = if (movementType == "entrada") "📥" else "📤"
        val action = if (movementType == "entrada") "Ingreso" else "Salida"
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_INFO)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("$emoji $action de Material")
            .setContentText("$materialName: $quantity unidades")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("$action registrada:\nMaterial: $materialName\nCantidad: $quantity unidades\n✅ Movimiento procesado correctamente"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        // Verificar permisos antes de mostrar notificación
        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(context).notify(
                "movement_${System.currentTimeMillis()}".hashCode(),
                notification
            )
        }
    }
    
    fun cancelNotification(context: Context, notificationId: String) {
        NotificationManagerCompat.from(context).cancel(notificationId.hashCode())
    }
    
    fun cancelAllNotifications(context: Context) {
        NotificationManagerCompat.from(context).cancelAll()
    }
}


