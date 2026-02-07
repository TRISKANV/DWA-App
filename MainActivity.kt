package com.dwa.app

import android.content.ClipboardManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        if (clipboard.hasPrimaryClip()) {
            val text = clipboard.primaryClip?.getItemAt(0)?.text.toString()
            if (text.contains("tiktok.com") || text.contains("youtube.com")) {
                Toast.makeText(this, "Link detectado: $text", Toast.LENGTH_LONG).show()
                // 
            }
        }
    }
}
