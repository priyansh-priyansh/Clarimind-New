package com.example.clarimind.presentation.model


// Data Classes
data class SectionAQuestion(
    val id: Int,
    val question: String,
    val category: String
)

data class SectionBQuestion(
    val id: Int,
    val question: String,
    val isPositive: Boolean
)

data class QuestionnaireState(
    val sectionAAnswers: Map<Int, Int> = emptyMap(),
    val sectionBAnswers: Map<Int, Boolean> = emptyMap(),
    val currentSection: Int = 0, // 0 = Section A, 1 = Section B
    val isCompleted: Boolean = false,
    val phiScore: PHIScore? = null
)

data class PHIScore(
    val rememberedWellBeing: Double,
    val experiencedWellBeing: Double,
    val combinedPHI: Double
)