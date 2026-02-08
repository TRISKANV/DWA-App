package com.dwa.app

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.io.File
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val urlInput = findViewById<EditText>(R.id.urlInput)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)
        val optionsPanel = findViewById<LinearLayout>(R.id.optionsPanel)
        val btnHD = findViewById<Button>(R.id.btnHD)
        val btnMP3 = findViewById<Button>(R.id.btnMP3)

        optionsPanel.visibility = View.GONE

        btnAnalyze.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.isNotEmpty() && (url.contains("tiktok") || url.contains("youtu") || url.contains("instagram"))) {
                Toast.makeText(this, "🔍 Analizando contenido...", Toast.LENGTH_SHORT).show()
                optionsPanel.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "❌ Por favor, ingresá un link válido", Toast.LENGTH_SHORT).show()
            }
        }

        btnHD.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.contains("tiktok")) {
                obtenerLinkTikTok(url)
            } else {
                ejecutarDescarga(url, "DWA_Video_${System.currentTimeMillis()}")
            }
        }

        btnMP3.setOnClickListener {
            Toast.makeText(this, "📻 Función MP3 próximamente...", Toast.LENGTH_SHORT).show()
        }

        verificarPermisos()
    }

    private fun obtenerLinkTikTok(urlTiktok: String) {
        val client = OkHttpClient()
        // 
        val request = Request.Builder()
            .url("https://www.tikwm.com/api/?url=$urlTiktok")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Error de red", Toast.LENGTH_SHORT).show() }
            }

            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (resBody != null) {
                    val json = JSONObject(resBody)
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        val videoUrl = data.getString("play") // Link directo al video
                        val titulo = data.optString("title", "Video_DWA")
                        runOnUiThread {
                            ejecutarDescarga(videoUrl, titulo)
                        }
                    } else {
                        runOnUiThread { Toast.makeText(this@MainActivity, "No se encontró el video", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        })
    }

    private fun ejecutarDescarga(url: String, nombreArchivo: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("DWA: $nombreArchivo")
                .setDescription("Descargando video...")
                .setMimeType("video/mp4") // 
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "DWA/$nombreArchivo.mp4")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            Toast.makeText(this, "📥 Descarga iniciada...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error de descarga: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun verificarPermisos() {
        val permisos = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            permisos.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permisos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 100)
        }
    }
}
