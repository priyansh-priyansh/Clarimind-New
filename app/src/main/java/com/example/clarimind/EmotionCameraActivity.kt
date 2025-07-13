package com.example.clarimind

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.clarimind.presentation.screens.EmotionCameraScreen
import com.example.clarimind.ui.theme.ClariMindTheme

class EmotionCameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClariMindTheme {
                EmotionCameraScreen(
                    onEmotionDetected = { emotion ->
                        // Navigate to QuestionsActivity when emotion is detected
                        val intent = Intent(this, QuestionsActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onBackPressed = { finish() }
                )
            }
        }
    }
} 