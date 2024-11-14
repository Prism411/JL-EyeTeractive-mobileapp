package com.example.eyeteractive

import android.accessibilityservice.AccessibilityService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import kotlin.random.Random

class CursorAccessibilityService : AccessibilityService() {

    private lateinit var windowManager: WindowManager
    private lateinit var cursorView: View
    private val handler = Handler(Looper.getMainLooper())

    override fun onServiceConnected() {
        super.onServiceConnected()
        createNotificationChannel()

        Log.d("CursorService", "Service connected. Setting up the cursor view.")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        cursorView = inflater.inflate(R.layout.cursor_overlay, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(cursorView, params)
            Log.d("CursorService", "Cursor view added to WindowManager.")
        } catch (e: Exception) {
            Log.e("CursorService", "Failed to add cursor view: ${e.message}")
        }

        startMovingCursorRandomly(params)
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
    private fun startMovingCursorRandomly(params: WindowManager.LayoutParams) {
        handler.post(object : Runnable {
            override fun run() {
                params.x = Random.nextInt(0, resources.displayMetrics.widthPixels)
                params.y = Random.nextInt(0, resources.displayMetrics.heightPixels)

                try {
                    windowManager.updateViewLayout(cursorView, params)
                    Log.d("CursorService", "Cursor moved to x=${params.x}, y=${params.y}")
                } catch (e: Exception) {
                    Log.e("CursorService", "Failed to update cursor position: ${e.message}")
                }

                handler.postDelayed(this, 1000) // Move a cada 1 segundo
            }
        })
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}

    override fun onInterrupt() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            if (::windowManager.isInitialized && ::cursorView.isInitialized) {
                windowManager.removeView(cursorView)
                Log.d("CursorService", "Cursor view removed.")
            }
        } catch (e: Exception) {
            Log.e("CursorService", "Failed to remove cursor view: ${e.message}")
        }
    }
}
