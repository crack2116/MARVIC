package com.proyecto.marvic.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.proyecto.marvic.R

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: "Alerta de Inventario"
        val body = message.notification?.body ?: "Tienes una nueva notificación"
        showNotification(title, body)
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aquí puedes enviar el token al servidor si es necesario
        // Por ejemplo, para enviar notificaciones push específicas
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "inventory_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alertas de Inventario", NotificationManager.IMPORTANCE_HIGH)
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        // Verificar permisos antes de mostrar notificación
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            @Suppress("MissingPermission")
            NotificationManagerCompat.from(this).notify((0..99999).random(), builder.build())
        }
    }
}




