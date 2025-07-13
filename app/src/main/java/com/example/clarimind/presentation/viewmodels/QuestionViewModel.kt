package com.example.clarimind.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clarimind.presentation.model.PHIScore
import com.example.clarimind.presentation.model.QuestionnaireState
import com.example.clarimind.presentation.model.SectionAQuestion
import com.example.clarimind.presentation.model.SectionBQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class QuestionnaireViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionnaireState())
    val uiState: StateFlow<QuestionnaireState> = _uiState.asStateFlow()

    val sectionAQuestions = listOf(
        SectionAQuestion(1, "I am very satisfied with my life", "General well-being"),
        SectionAQuestion(2, "I have the energy to accomplish my daily tasks", "General well-being"),
        SectionAQuestion(3, "I think my life is useful and worthwhile", "Eudaimonic well-being"),
        SectionAQuestion(4, "I am satisfied with myself", "Eudaimonic well-being"),
        SectionAQuestion(5, "My life is full of learning experiences and challenges that make me grow", "Eudaimonic well-being"),
        SectionAQuestion(6, "I feel very connected to the people around me", "Eudaimonic well-being"),
        SectionAQuestion(7, "I feel able to solve the majority of my daily problems", "Eudaimonic well-being"),
        SectionAQuestion(8, "I think that I can be myself on the important things", "Eudaimonic well-being"),
        SectionAQuestion(9, "I enjoy a lot of little things every day", "Hedonic well-being"),
        SectionAQuestion(10, "I have a lot of bad moments in my daily life", "Hedonic well-being"),
        SectionAQuestion(11, "I think that I live in a society that lets me fully realize my potential", "Social well-being")
    )

    val sectionBQuestions = listOf(
        SectionBQuestion(1, "Something I did made me proud", true),
        SectionBQuestion(2, "I did something fun with someone", true),
        SectionBQuestion(3, "I did something I really enjoy doing", true),
        SectionBQuestion(4, "I learned something interesting", true),
        SectionBQuestion(5, "I gave myself a treat", true),
        SectionBQuestion(6, "At times, I felt overwhelmed", false),
        SectionBQuestion(7, "I was bored for a lot of the time", false),
        SectionBQuestion(8, "I was worried about personal matters", false),
        SectionBQuestion(9, "Things happened that made me really angry", false),
        SectionBQuestion(10, "I felt disrespected by someone", false)
    )

    fun updateSectionAAnswer(questionId: Int, answer: Int) {
        _uiState.value = _uiState.value.copy(
            sectionAAnswers = _uiState.value.sectionAAnswers + (questionId to answer)
        )
    }

    fun updateSectionBAnswer(questionId: Int, answer: Boolean) {
        _uiState.value = _uiState.value.copy(
            sectionBAnswers = _uiState.value.sectionBAnswers + (questionId to answer)
        )
    }

    fun goToNextSection() {
        _uiState.value = _uiState.value.copy(currentSection = 1)
    }

    fun goToPreviousSection() {
        _uiState.value = _uiState.value.copy(currentSection = 0)
    }

    fun calculateResults(onResultsCalculated: (PHIScore) -> Unit) {
        viewModelScope.launch {
            val sectionAScore = calculateSectionAScore()
            val sectionBScore = calculateSectionBScore()
            val combinedScore = (sectionAScore + sectionBScore) / 2

            val phiScore = PHIScore(
                rememberedWellBeing = sectionAScore,
                experiencedWellBeing = sectionBScore,
                combinedPHI = combinedScore
            )

            _uiState.value = _uiState.value.copy(
                phiScore = phiScore,
                isCompleted = true
            )

            onResultsCalculated(phiScore)
        }
    }

    private fun calculateSectionAScore(): Double {
        val answers = _uiState.value.sectionAAnswers
        var total = 0.0

        answers.forEach { (questionId, answer) ->
            // Question 10 is reverse-scored
            val score = if (questionId == 10) {
                10 - answer
            } else {
                answer
            }
            total += score
        }

        return total / 11.0
    }

    private fun calculateSectionBScore(): Double {
        val answers = _uiState.value.sectionBAnswers
        var score = 0.0

        answers.forEach { (questionId, answer) ->
            val question = sectionBQuestions.find { it.id == questionId }
            question?.let {
                if (it.isPositive && answer) {
                    score += 1
                } else if (!it.isPositive && !answer) {
                    score += 1
                }
            }
        }

        return score
    }

    fun resetQuestionnaire() {
        _uiState.value = QuestionnaireState()
    }
}
