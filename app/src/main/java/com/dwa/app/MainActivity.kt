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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
            val url = urlInput.text.toString().trim().lowercase()
            // Expandimos la validación para incluir X, Instagram y Facebook
            val redesValidas = listOf("tiktok", "youtu", "instagram", "facebook", "fb.watch", "x.com", "twitter")
            
            if (url.isNotEmpty() && redesValidas.any { url.contains(it) }) {
                Toast.makeText(this, "🔍 Analizando contenido multi-red...", Toast.LENGTH_SHORT).show()
                optionsPanel.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "❌ Link no soportado todavía", Toast.LENGTH_SHORT).show()
            }
        }

        btnHD.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.contains("tiktok")) {
                obtenerLinkTikTok(url, false)
            } else {
                // 
                obtenerLinkUniversal(url, false)
            }
        }

        btnMP3.setOnClickListener {
            val url = urlInput.text.toString().trim()
            if (url.contains("tiktok")) {
                obtenerLinkTikTok(url, true)
            } else {
                obtenerLinkUniversal(url, true)
            }
        }

        verificarPermisos()
    }

    private fun obtenerLinkTikTok(urlTiktok: String, soloAudio: Boolean) {
        val client = OkHttpClient()
        val request = Request.Builder().url("https://www.tikwm.com/api/?url=$urlTiktok").build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Error en TikTok", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (resBody != null) {
                    val json = JSONObject(resBody)
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        val downloadUrl = if (soloAudio) data.optString("music") else data.getString("play")
                        val titulo = data.optString("title", "TikTok_DWA")
                        runOnUiThread { ejecutarDescarga(downloadUrl, titulo, soloAudio) }
                    }
                }
            }
        })
    }

    // 
    private fun obtenerLinkUniversal(urlMedia: String, soloAudio: Boolean) {
        val client = OkHttpClient()
        val urlLimpia = if (urlMedia.contains("?")) urlMedia.split("?")[0] else urlMedia

        val json = JSONObject()
        json.put("url", urlLimpia)
        json.put("isAudioOnly", soloAudio)
        if (!soloAudio) json.put("vQuality", "720")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api.cobalt.tools/api/json")
            .post(body)
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { Toast.makeText(this@MainActivity, "Error de conexión", Toast.LENGTH_SHORT).show() }
            }
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body?.string()
                if (resBody != null) {
                    try {
                        val jsonRes = JSONObject(resBody)
                        val downloadUrl = if (jsonRes.has("url")) jsonRes.getString("url") else jsonRes.optString("picker")
                        
                        if (downloadUrl.isNotEmpty()) {
                            runOnUiThread { ejecutarDescarga(downloadUrl, "DWA_${System.currentTimeMillis()}", soloAudio) }
                        } else {
                            runOnUiThread { Toast.makeText(this@MainActivity, "Contenido privado o no encontrado", Toast.LENGTH_SHORT).show() }
                        }
                    } catch (e: Exception) {
                        runOnUiThread { Toast.makeText(this@MainActivity, "Error en el servidor de extracción", Toast.LENGTH_SHORT).show() }
                    }
                }
            }
        })
    }

    private fun ejecutarDescarga(url: String, nombreArchivo: String, esAudio: Boolean) {
        try {
            val extension = if (esAudio) ".mp3" else ".mp4"
            val mimeType = if (esAudio) "audio/mpeg" else "video/mp4"
            val subCarpeta = if (esAudio) "DWA/Music" else "DWA/Videos"

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("DWA: $nombreArchivo")
                .setDescription("Guardando archivo...")
                .setMimeType(mimeType)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "$subCarpeta/$nombreArchivo$extension")
                .setAllowedOverMetered(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            runOnUiThread { Toast.makeText(this, "📥 Iniciando: ${if (esAudio) "MP3" else "Video"}", Toast.LENGTH_SHORT).show() }

        } catch (e: Exception) {
            runOnUiThread { Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
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
