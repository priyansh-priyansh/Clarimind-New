package com.example.clarimind.presentation.screens

// Composable Screen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clarimind.presentation.model.PHIScore
import com.example.clarimind.presentation.model.SectionAQuestion
import com.example.clarimind.presentation.model.SectionBQuestion
import com.example.clarimind.presentation.viewmodels.QuestionnaireViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionnaireScreen(
    onResultsCalculated: (PHIScore) -> Unit = {},
    viewModel: QuestionnaireViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Auto-scroll to top when section changes
    LaunchedEffect(uiState.currentSection) {
        if (uiState.currentSection == 1) {
            scrollState.animateScrollTo(0)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Personal Happiness Index",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Progress indicator
            LinearProgressIndicator(
                progress = when {
                    uiState.currentSection == 0 -> 0.5f
                    uiState.isCompleted -> 1.0f
                    else -> 0.8f
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            when (uiState.currentSection) {
                0 -> {
                    SectionAContent(
                        questions = viewModel.sectionAQuestions,
                        answers = uiState.sectionAAnswers,
                        onAnswerChange = { questionId, answer ->
                            viewModel.updateSectionAAnswer(questionId, answer)
                        },
                        onNext = { viewModel.goToNextSection() },
                        canProceed = uiState.sectionAAnswers.size == viewModel.sectionAQuestions.size
                    )
                }
                else -> {
                    SectionBContent(
                        questions = viewModel.sectionBQuestions,
                        answers = uiState.sectionBAnswers,
                        onAnswerChange = { questionId, answer ->
                            viewModel.updateSectionBAnswer(questionId, answer)
                        },
                        onBack = { viewModel.goToPreviousSection() },
                        onComplete = {
                            viewModel.calculateResults(onResultsCalculated)
                        },
                        canComplete = uiState.sectionBAnswers.size == viewModel.sectionBQuestions.size
                    )
                }
            }
        }
    }
}

@Composable
fun SectionAContent(
    questions: List<SectionAQuestion>,
    answers: Map<Int, Int>,
    onAnswerChange: (Int, Int) -> Unit,
    onNext: () -> Unit,
    canProceed: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📋 Section A – Remembered Well-Being (11 items)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Rate from 0 (\"totally disagree\") to 10 (\"totally agree\")",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            questions.forEach { question ->
                QuestionCard(
                    question = question.question,
                    category = question.category,
                    currentAnswer = answers[question.id],
                    onAnswerChange = { answer ->
                        onAnswerChange(question.id, answer)
                    },
                    isReversed = question.id == 10
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Button(
                onClick = onNext,
                enabled = canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Next: Section B")
            }
        }
    }
}

@Composable
fun SectionBContent(
    questions: List<SectionBQuestion>,
    answers: Map<Int, Boolean>,
    onAnswerChange: (Int, Boolean) -> Unit,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    canComplete: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 Section B – Experienced Well-Being (10 items)",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Based on your experiences yesterday, answer Yes or No:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Positive questions
            Text(
                text = "✅ Positive Experiences:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            questions.filter { it.isPositive }.forEach { question ->
                YesNoQuestionCard(
                    question = question.question,
                    currentAnswer = answers[question.id],
                    onAnswerChange = { answer ->
                        onAnswerChange(question.id, answer)
                    },
                    isPositive = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Negative questions
            Text(
                text = "❌ Negative Experiences:",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF5722),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            questions.filter { !it.isPositive }.forEach { question ->
                YesNoQuestionCard(
                    question = question.question,
                    currentAnswer = answers[question.id],
                    onAnswerChange = { answer ->
                        onAnswerChange(question.id, answer)
                    },
                    isPositive = false
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Back")
                }

                Button(
                    onClick = onComplete,
                    enabled = canComplete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Calculate Results")
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: String,
    category: String,
    currentAnswer: Int?,
    onAnswerChange: (Int) -> Unit,
    isReversed: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isReversed) Color(0xFFFFE0B2) else Color(0xFFF3F4F6)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = category,
                fontSize = 12.sp,
                color = Color(0xFF1976D2),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = question + if (isReversed) " (reverse-scored)" else "",
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Rating slider
            Column {
                Slider(
                    value = (currentAnswer ?: 0).toFloat(),
                    onValueChange = { onAnswerChange(it.toInt()) },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0 - Totally disagree", fontSize = 10.sp, color = Color(0xFF666666))
                    Text("Current: ${currentAnswer ?: 0}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                    Text("10 - Totally agree", fontSize = 10.sp, color = Color(0xFF666666))
                }
            }
        }
    }
}

@Composable
fun YesNoQuestionCard(
    question: String,
    currentAnswer: Boolean?,
    onAnswerChange: (Boolean) -> Unit,
    isPositive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) Color(0xFFE8F5E8) else Color(0xFFFFE0B2)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = question,
                fontSize = 14.sp,
                color = Color(0xFF1A1A1A),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    onClick = { onAnswerChange(true) },
                    label = { Text("Yes") },
                    selected = currentAnswer == true,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White
                    )
                )

                FilterChip(
                    onClick = { onAnswerChange(false) },
                    label = { Text("No") },
                    selected = currentAnswer == false,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFF5722),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}