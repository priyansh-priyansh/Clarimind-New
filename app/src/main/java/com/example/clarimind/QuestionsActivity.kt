package com.example.clarimind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.clarimind.presentation.screens.QuestionnaireScreen
import com.example.clarimind.ui.theme.ClariMindTheme

class QuestionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClariMindTheme {
                QuestionnaireScreen(
                    onResultsCalculated = { phiScore ->
                        // Handle results - you can navigate to results screen or show results
                        // For now, just finish this activity
                        finish()
                    }
                )
            }
        }
    }
} 