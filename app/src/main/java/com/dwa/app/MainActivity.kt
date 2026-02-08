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

        // 
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)
        val optionsPanel = findViewById<LinearLayout>(R.id.optionsPanel)
        val btnHD = findViewById<Button>(R.id.btnHD)
        val btnMP3 = findViewById<Button>(R.id.btnMP3)

        // 
        optionsPanel.visibility = View.GONE

        // 
        btnAnalyze.setOnClickListener {
            val url = urlInput.text.toString().trim()

            if (url.isNotEmpty() && (url.contains("tiktok") || url.contains("youtu") || url.contains("instagram"))) {
                Toast.makeText(this, "🔍 Analizando contenido...", Toast.LENGTH_SHORT).show()
                // 
                optionsPanel.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "❌ Por favor, ingresá un link válido", Toast.LENGTH_SHORT).show()
            }
        }

        // 
        btnHD.setOnClickListener {
            val url = urlInput.text.toString().trim()
            Toast.makeText(this, "⏳ Iniciando descarga...", Toast.LENGTH_SHORT).show()
            
            // 
            ejecutarDescarga(url, "DWA_Video_${System.currentTimeMillis()}")
        }

        // 5. Botón Solo Audio
        btnMP3.setOnClickListener {
            Toast.makeText(this, "📻 Extrayendo audio...", Toast.LENGTH_SHORT).show()
            // Lógica similar para MP3
        }

        // 
        verificarPermisos()
    }

    private fun ejecutarDescarga(url: String, nombreArchivo: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle("DWA Descargando")
                .setDescription("Guardando en /Movies/DWA")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_MOVIES, "DWA/$nombreArchivo.mp4")
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)
            
            // 
            mostrarNotificacionExito(nombreArchivo)

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

    private fun mostrarNotificacionExito(archivo: String) {
        val channelId = "DWA_NOTIF"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Descargas", NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intentShare = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_TEXT, "¡Mirá lo que descargué con DWA!")
        }
        val pendingShare = PendingIntent.getActivity(this, 0, 
            Intent.createChooser(intentShare, "Compartir con..."), PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("DWA: ¡Video Listo! ✅")
            .setContentText("Se guardó $archivo")
            .addAction(android.R.drawable.ic_menu_share, "COMPARTIR", pendingShare)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).apply {
            if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                notify(1, notification)
            }
        }
    }
}
