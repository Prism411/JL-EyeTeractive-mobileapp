package com.example.eyeteractive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.io.File
import android.media.MediaRecorder
import androidx.annotation.RequiresApi
import androidx.lifecycle.ProcessLifecycleOwner

class CameraService : Service() {

    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false

    override fun onBind(intent: Intent?): IBinder? {
        // Este serviço não suporta binding
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startCameraCapture()
    }

    // Inicia o serviço em primeiro plano para manter a gravação ativa em segundo plano
    private fun startForegroundService() {
        val channelId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel("camera_service", "Camera Service")
        } else {
            ""
        }
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Camera Service")
            .setContentText("Gravando da câmera frontal")
            .setSmallIcon(R.drawable.ic_camera)
            .build()

        startForeground(1, notification)
    }

    // Cria um canal de notificação para dispositivos Android O e posteriores
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun startCameraCapture() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Configuração do preview (não visível para o usuário, usado apenas para inicializar a câmera)
            val preview = Preview.Builder().build()

            // Inicializa e configura o MediaRecorder para gravação
            setupMediaRecorder()
            mediaRecorder.start() // Inicia a gravação

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(ProcessLifecycleOwner.get(), cameraSelector, preview)

                Log.d("CameraService", "Camera started and recording")
                isRecording = true
            } catch (exc: Exception) {
                Log.e("CameraService", "Camera binding failed", exc)
                stopSelf() // Encerra o serviço se ocorrer erro
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun setupMediaRecorder() {
        val videoFile = File(getExternalFilesDir(null), "camera_capture.mp4")

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(videoFile.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setVideoSize(1280, 720)
            prepare()
        }
    }

    // Encerra a gravação e limpa os recursos
    override fun onDestroy() {
        super.onDestroy()
        if (isRecording) {
            mediaRecorder.stop()
            mediaRecorder.reset()
            mediaRecorder.release()
            isRecording = false
            Log.d("CameraService", "Recording stopped and media recorder released")
        }
    }
}
