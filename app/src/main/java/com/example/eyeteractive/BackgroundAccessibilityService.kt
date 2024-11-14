package com.example.eyeteractive

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class CameraForegroundService : Service() {

    private val reconnectHandler = Handler(Looper.getMainLooper())
    private val reconnectRunnable = Runnable { startCameraCapture() }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService()  // Inicia o serviço em primeiro plano com uma notificação
        startCameraCapture()       // Inicia a captura da câmera
    }

    private fun startCameraCapture() {
        // Implementação da captura da câmera com Camera2 API
        Log.d("CameraService", "Iniciando captura de câmera em segundo plano")
        // Se a conexão for encerrada, tente reconectar
        scheduleReconnect()
    }

    private fun scheduleReconnect() {
        // Tenta reconectar a cada 10 segundos se a câmera se desconectar
        reconnectHandler.postDelayed(reconnectRunnable, 10000)
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "CameraServiceChannel")
            .setContentTitle("Captura de Câmera em Segundo Plano")
            .setContentText("Capturando frames da câmera frontal")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                "CameraServiceChannel",
                "Camera Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        reconnectHandler.removeCallbacks(reconnectRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
