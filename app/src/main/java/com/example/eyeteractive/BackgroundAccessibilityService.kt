package com.example.eyeteractive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class CameraForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundServiceWithNotification() // Inicia o serviço em primeiro plano com uma notificação simples
    }

    private fun startForegroundServiceWithNotification() {
        val notification = NotificationCompat.Builder(this, "CameraServiceChannel")
            .setContentTitle("Serviço em Segundo Plano")
            .setContentText("Serviço rodando em segundo plano")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridade alta
            .build()

        startForeground(1, notification)
        Log.d("CameraService", "Serviço de notificação simples iniciado")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "CameraServiceChannel",
                "Canal do Serviço Simples",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        Log.d("CameraService", "Serviço simples destruído")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
