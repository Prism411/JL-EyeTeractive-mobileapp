package com.example.eyeteractive

import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService

class CameraBackgroundService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        startCameraCapture()
    }

    private fun startCameraCapture() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            // Configure ImageAnalysis
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().apply {
                    setAnalyzer(ContextCompat.getMainExecutor(this@CameraBackgroundService), ImageAnalyzer())
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this@CameraBackgroundService, cameraSelector, imageAnalyzer)
            } catch (e: Exception) {
                Log.e("CameraService", "Erro ao iniciar captura da câmera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private class ImageAnalyzer : ImageAnalysis.Analyzer {
        override fun analyze(imageProxy: ImageProxy) {
            Log.d("CameraService", "Frame capturado na câmera frontal")
            imageProxy.close()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

}
