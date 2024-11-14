package com.example.eyeteractive

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionHandler(
    private val activity: Activity,
    private val callback: (allPermissionsGranted: Boolean) -> Unit
) {

    companion object {
        private const val REQUEST_CODE_DRAW_OVERLAY = 1234
        private const val REQUEST_CODE_PERMISSIONS = 1001
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }

    // Função para verificar e solicitar todas as permissões
    fun verificarPermissoes() {
        var allPermissionsGranted = true

        if (!verificarPermissaoSobreposicao()) {
            allPermissionsGranted = false
        }
        if (!verificarPermissoesCameraAudio()) {
            allPermissionsGranted = false
        }
        if (!verificarOtimizacaoBateria()) {
            allPermissionsGranted = false
        }

        // Chama o callback para informar o status das permissões
        callback(allPermissionsGranted)
    }

    // Verifica e solicita permissão de sobreposição
    private fun verificarPermissaoSobreposicao(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(activity)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${activity.packageName}")
            )
            activity.startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY)
            false
        } else {
            true
        }
    }

    // Verifica e solicita permissões de câmera e áudio
    private fun verificarPermissoesCameraAudio(): Boolean {
        return if (!temPermissoesNecessarias()) {
            ActivityCompat.requestPermissions(activity, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            false
        } else {
            true
        }
    }

    // Verifica e solicita a permissão de otimização de bateria
    private fun verificarOtimizacaoBateria(): Boolean {
        val pm = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !pm.isIgnoringBatteryOptimizations(activity.packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${activity.packageName}")
            }
            activity.startActivity(intent)
            false
        } else {
            true
        }
    }

    // Verifica se todas as permissões necessárias estão concedidas
    private fun temPermissoesNecessarias(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Trata os resultados da solicitação de permissões
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            callback(allPermissionsGranted)
        }
    }

    // Trata os resultados para a permissão de sobreposição
    fun onActivityResult(requestCode: Int) {
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY) {
            val overlayPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(activity)
            callback(overlayPermissionGranted)
        }
    }
}
