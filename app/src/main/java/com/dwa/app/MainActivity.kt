package com.dwa.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //
        val urlInput = findViewById<EditText>(R.id.urlInput)
        val btnAnalyze = findViewById<Button>(R.id.btnAnalyze)
        val optionsPanel = findViewById<LinearLayout>(R.id.optionsPanel)
        
        // 
        val btnDownloadHD = optionsPanel.getChildAt(0) as Button 

        // 
        optionsPanel.visibility = View.GONE

        // 
        btnAnalyze.setOnClickListener {
            val url = urlInput.text.toString()

            if (url.isNotEmpty() && (url.contains("tiktok") || url.contains("youtube") || url.contains("instagram"))) {
                Toast.makeText(this, "🔍 Analizando video...", Toast.LENGTH_SHORT).show()
                
                // 
                optionsPanel.visibility = View.VISIBLE
            } else {
                Toast.makeText(this, "❌ Pegá un link válido de TikTok o YouTube", Toast.LENGTH_SHORT).show()
            }
        }

        //
        btnDownloadHD.setOnClickListener {
            Toast.makeText(this, "⏳ Iniciando descarga...", Toast.LENGTH_SHORT).show()
            
            // 
            it.postDelayed({
                mostrarNotificacionFinalizado("DWA_Video_Prueba.mp4")
            }, 3000)
        }

        // 
        solicitarPermisosNotificacion()
    }

    private fun solicitarPermisosNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    fun mostrarNotificacionFinalizado(nombreArchivo: String) {
        val channelId = "DWA_DOWNLOADS"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Descargas DWA"
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_DEFAULT)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_TEXT, "¡Mirá este video que bajé con DWA!")
        }
        
        val pendingShare = PendingIntent.getActivity(this, 0, 
            Intent.createChooser(shareIntent, "Compartir con..."), 
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done) 
            .setContentTitle("¡Descarga Lista! ✅")
            .setContentText("El video $nombreArchivo ya está en tu carpeta /DWA")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(android.R.drawable.ic_menu_share, "COMPARTIR", pendingShare)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(1, builder.build())
            }
        } catch (e: SecurityException) {
            
        }
    }
}
