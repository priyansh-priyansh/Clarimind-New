package com.example.clarimind.presentation.navigation

import kotlinx.serialization.Serializable

// Replace the sealed class with individual @Serializable classes
@Serializable
data object EmotionCameraScreen

@Serializable
data class QuestionsScreen(val mood: String)

@Serializable
data class DashBoardScreen(
    val mood: String,
    val rememberedWellBeing: Double,
    val experiencedWellBeing: Double,
    val combinedPHI: Double
)

@Serializable
data class ChatbotScreen(
    val mood: String,
    val rememberedWellBeing: Double,
    val experiencedWellBeing: Double,
    val combinedPHI: Double
)

@Serializable
data object ScreenTimeScreen