package com.example.clarimind

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("clarimind_prefs", Context.MODE_PRIVATE)
        val completedWelcome = prefs.getBoolean("completed_welcome", false)
        if (completedWelcome) {
            // User already completed welcome, go straight to camera
            startActivity(Intent(this, EmotionCameraActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_welcome)

        val btnGetStarted = findViewById<Button>(R.id.btnGetStarted)
        btnGetStarted.setOnClickListener {
            prefs.edit().putBoolean("completed_welcome", true).apply()
            val intent = Intent(this, EmotionCameraActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
} 