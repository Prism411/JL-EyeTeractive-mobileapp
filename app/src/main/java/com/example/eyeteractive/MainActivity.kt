package com.example.eyeteractive

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {

    private lateinit var permissionHandler: PermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialize o PermissionHandler com um callback para tratar o status das permissões
        permissionHandler = PermissionHandler(this) { allPermissionsGranted ->
            if (allPermissionsGranted) {
                iniciarCursorService()
                iniciarCamera()
            } else {
                Toast.makeText(this, "Todas as permissões são necessárias para iniciar o serviço", Toast.LENGTH_SHORT).show()
            }
        }

        // Verifique e solicite as permissões necessárias
        permissionHandler.verificarPermissoes()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        permissionHandler.onActivityResult(requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionHandler.onRequestPermissionsResult(requestCode, grantResults)
    }

    private fun iniciarCursorService() {
        // Iniciar o CursorAccessibilityService
        val intent = Intent(this, CursorAccessibilityService::class.java)
        startService(intent)


    }
    private fun iniciarCamera(){
        val intent = Intent(this, CameraService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}