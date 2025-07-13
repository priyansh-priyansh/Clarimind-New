package com.example.clarimind.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Routes{
    @Serializable
    data object EmotionCameraScreen : Routes()
    @Serializable
    data object QuestionsScreen : Routes()
    @Serializable
    data object DashBoardScreen : Routes()
}