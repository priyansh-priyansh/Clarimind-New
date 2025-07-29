package com.example.clarimind.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.example.clarimind.presentation.model.DashboardUiState
import com.example.clarimind.presentation.model.PHIScore
import com.example.clarimind.presentation.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(
        DashboardUiState(
            user = User(
                name = "John Doe",
                email = "john.doe@example.com",
                profilePhotoUrl = null
            ),
            phiScore = PHIScore(
                rememberedWellBeing = 7.5,
                experiencedWellBeing = 6.8,
                combinedPHI = 7.2
            ),
            suggestions = emptyList()
        )
    )

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    private var currentMood: String = ""

    init {
        updateSuggestions()
    }

    fun updatePhiScore(phiScore: PHIScore) {
        _uiState.value = _uiState.value.copy(phiScore = phiScore)
        updateSuggestions()
    }

    fun updateUser(user: User) {
        _uiState.value = _uiState.value.copy(user = user)
    }

    fun updateMood(mood: String) {
        currentMood = mood
        updateSuggestions()
    }

    private fun updateSuggestions() {
        val suggestions = generateSuggestions(_uiState.value.phiScore.combinedPHI, currentMood)
        _uiState.value = _uiState.value.copy(suggestions = suggestions)
    }

    private fun generateSuggestions(phiScore: Double, mood: String): List<String> {
        val baseSuggestions = when {
            phiScore >= 8.0 -> listOf(
                "Continue your positive routines",
                "Share your good mood with others",
                "Try a new hobby or activity",
                "Practice gratitude journaling"
            )
            phiScore >= 6.0 -> listOf(
                "Try journaling your thoughts",
                "Take a short walk or meditate",
                "Connect with friends or family",
                "Listen to uplifting music",
                "Practice deep breathing exercises"
            )
            phiScore >= 4.0 -> listOf(
                "Talk to our mental health chatbot",
                "Try gentle physical activity",
                "Practice mindfulness meditation",
                "Consider talking to someone you trust",
                "Engage in a creative activity"
            )
            else -> listOf(
                "Talk to our mental health chatbot",
                "Consider professional support",
                "Reach out to trusted friends or family",
                "Try grounding techniques",
                "Practice self-compassion"
            )
        }

        val moodSpecificSuggestions = getMoodSpecificSuggestions(mood.lowercase())

        // Combine base suggestions with mood-specific ones, ensuring no duplicates
        val combinedSuggestions = (baseSuggestions + moodSpecificSuggestions).distinct()

        // Return top 5 suggestions
        return combinedSuggestions.take(5)
    }

    private fun getMoodSpecificSuggestions(mood: String): List<String> {
        return when (mood) {
            "happy", "joy", "excited" -> listOf(
                "Channel your positive energy into helping others",
                "Document this good feeling in a journal",
                "Share your happiness with loved ones",
                "Try a new activity while you're feeling good"
            )
            "sad", "depressed", "disappointed" -> listOf(
                "Allow yourself to feel these emotions",
                "Reach out to a supportive friend or family member",
                "Try gentle movement like a short walk",
                "Practice self-compassion and be patient with yourself",
                "Consider watching something uplifting"
            )
            "angry", "frustrated", "annoyed" -> listOf(
                "Take deep breaths and count to 10",
                "Try physical exercise to release tension",
                "Write down your feelings to process them",
                "Practice progressive muscle relaxation",
                "Take a timeout before responding to situations"
            )
            "calm", "peaceful", "relaxed" -> listOf(
                "Maintain this peaceful state through meditation",
                "Practice mindfulness to stay present",
                "Use this calm energy for creative activities",
                "Help others who might be struggling"
            )
            "anxious", "worried", "stressed" -> listOf(
                "Practice the 4-7-8 breathing technique",
                "Try grounding exercises (5-4-3-2-1 technique)",
                "Limit caffeine and get adequate sleep",
                "Break large tasks into smaller, manageable steps",
                "Consider talking to someone about your worries"
            )
            "surprised", "shocked" -> listOf(
                "Take time to process what happened",
                "Practice grounding techniques to feel centered",
                "Talk to someone about your experience",
                "Write about your feelings to understand them better"
            )
            else -> listOf(
                "Practice mindfulness to understand your feelings",
                "Take time for self-reflection",
                "Consider journaling about your current state"
            )
        }
    }
}