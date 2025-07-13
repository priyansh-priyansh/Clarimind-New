package com.example.clarimind.presentation.model

// Data Classes
data class User(
    val name: String,
    val email: String,
    val profilePhotoUrl: String? = null
)


data class DashboardUiState(
    val user: User,
    val phiScore: PHIScore,
    val suggestions: List<String>
)