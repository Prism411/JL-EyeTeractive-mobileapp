package com.example.eyeteractive

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.eyeteractive.ui.theme.EyeteractiveTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val REQUEST_CODE_DRAW_OVERLAY = 1234
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Verifica se a permissão de sobreposição foi concedida
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            // Solicita a permissão se necessário
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE_DRAW_OVERLAY)
        } else {
            // Inicia o serviço se a permissão já foi concedida
            iniciarCursorService()
        }

        setContent {
            EyeteractiveTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    // Verifica o resultado da solicitação de permissão
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_DRAW_OVERLAY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
                // Permissão concedida, inicia o serviço
                iniciarCursorService()
            } else {
                // Exibe mensagem se a permissão foi negada
                Toast.makeText(this, "Permissão necessária para desenhar sobre outros aplicativos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarCursorService() {
        val intent = Intent(this, CursorAccessibilityService::class.java)
        startService(intent)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    EyeteractiveTheme {
        Greeting("Android")
    }
}
