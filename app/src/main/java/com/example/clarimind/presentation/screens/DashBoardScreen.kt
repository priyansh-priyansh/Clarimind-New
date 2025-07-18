package com.example.clarimind.presentation.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.clarimind.presentation.model.DashboardUiState
import com.example.clarimind.presentation.model.PHIScore
import com.example.clarimind.presentation.model.User
import com.example.clarimind.presentation.viewmodels.DashboardViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.foundation.clickable

@Composable
fun DashBoardScreen(
    mood: String,
    phiScore: PHIScore,
    user: User, // Add user parameter
    viewModel: DashboardViewModel = viewModel(),
    onRetakeAssessment: () -> Unit,
    onChatbotClick: () -> Unit,
    onViewScreenTime: () -> Unit = {},
    onLogout: () -> Unit = {}, // Add logout callback
    onBreathingExerciseSelected: (BreathingExerciseType) -> Unit = {} // New callback
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBreathingSheet by remember { mutableStateOf(false) }

    // Update the ViewModel with the passed PHI score and user when the screen is first composed
    LaunchedEffect(phiScore, user) {
        viewModel.updatePhiScore(phiScore)
        viewModel.updateUser(user)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        DashboardContent(
            uiState = uiState,
            mood = mood,
            onRetakeAssessment = onRetakeAssessment,
            onChatbotClick = onChatbotClick,
            onViewScreenTime = onViewScreenTime,
            onLogout = onLogout,
            paddingValues = PaddingValues(0.dp)
        )
        // Breathing Exercises Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                onClick = { showBreathingSheet = true },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF)),
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 200.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
            ) {
                Icon(Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Breathing Exercises", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        if (showBreathingSheet) {
            BreathingExerciseSheet(
                onDismiss = { showBreathingSheet = false },
                onExerciseSelected = {
                    showBreathingSheet = false
                    onBreathingExerciseSelected(it)
                }
            )
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    mood: String,
    onRetakeAssessment: () -> Unit,
    onChatbotClick: () -> Unit,
    onViewScreenTime: () -> Unit,
    onLogout: () -> Unit,
    paddingValues: PaddingValues
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Section
        item {
            UserProfileSection(user = uiState.user, onLogout = onLogout)
        }

        // Mood Summary Section
        item {
            MoodSummarySection(
                phiScore = uiState.phiScore,
                detectedMood = mood
            )
        }

        // Suggestions Section
        item {
            SuggestionsSection(
                suggestions = uiState.suggestions,
                phiScore = uiState.phiScore.combinedPHI,
                mood = mood,
                onChatbotClick = onChatbotClick
            )
        }

        // Action Buttons Section
        item {
            ActionButtonsSection(
                onRetakeAssessment = onRetakeAssessment,
                onViewScreenTime = onViewScreenTime
            )
        }
        // Add extra space at the end so the last button is not covered by the floating button
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun UserProfileSection(user: User, onLogout: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box (){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3F2FD)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.profilePhotoUrl.isNullOrBlank()) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(user.profilePhotoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color(0xFF2196F3)
                                )
                            },
                            loading = {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Logout",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
            IconButton(
                onClick = {},
                modifier = Modifier.size(70.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

        }
    }
}

@Composable
private fun MoodSummarySection(
    phiScore: PHIScore,
    detectedMood: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = getMoodIcon(detectedMood),
                    contentDescription = null,
                    tint = getMoodColor(phiScore.combinedPHI),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Your Mood Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Detected Mood Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Detected Mood:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666)
                )
                Text(
                    text = detectedMood.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getMoodColor(phiScore.combinedPHI)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // PHI Score Display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Happiness Score:",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666)
                )
                Text(
                    text = "${String.format("%.1f", phiScore.combinedPHI)}/10",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = getMoodColor(phiScore.combinedPHI)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mood Description
            Text(
                text = getMoodDescription(phiScore.combinedPHI, detectedMood),
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF444444),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun SuggestionsSection(
    suggestions: List<String>,
    phiScore: Double,
    mood: String,
    onChatbotClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Suggestions for You",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            suggestions.forEach { suggestion ->
                SuggestionItem(suggestion = suggestion)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Show chatbot button for low PHI scores
            if (phiScore < 6.0) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onChatbotClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Talk to Chatbot",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SuggestionItem(suggestion: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = suggestion,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF333333)
        )
    }
}

@Composable
private fun ActionButtonsSection(
    onRetakeAssessment: () -> Unit,
    onViewScreenTime: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Retake Assessment Button
        Button(
            onClick = onRetakeAssessment,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Retake PHI Assessment",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        // View Screen Time Button
        Button(
            onClick = onViewScreenTime,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "View Screen Time",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Helper Functions
private fun getMoodColor(phiScore: Double): Color {
    return when {
        phiScore >= 8.0 -> Color(0xFF4CAF50) // Green
        phiScore >= 6.0 -> Color(0xFF2196F3) // Blue
        phiScore >= 4.0 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFE53935) // Red
    }
}

private fun getMoodIcon(mood: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (mood.lowercase()) {
        "happy", "joy", "excited" -> Icons.Default.SentimentVerySatisfied
        "sad", "depressed", "disappointed" -> Icons.Default.SentimentVeryDissatisfied
        "angry", "frustrated", "annoyed" -> Icons.Default.SentimentDissatisfied
        "calm", "peaceful", "relaxed" -> Icons.Default.SentimentSatisfied
        "anxious", "worried", "stressed" -> Icons.Default.SentimentNeutral
        "surprised", "shocked" -> Icons.Default.SentimentSatisfied
        else -> Icons.Default.FavoriteBorder
    }
}

private fun getMoodDescription(phiScore: Double, detectedMood: String): String {
    val baseDescription = when {
        phiScore >= 8.5 -> "You're in an excellent emotional state! You're feeling balanced, positive, and emotionally resilient."
        phiScore >= 7.0 -> "You're doing really well overall. You seem emotionally balanced and stable, with good coping mechanisms in place."
        phiScore >= 5.5 -> "Your emotional state shows you're managing reasonably well, though there might be some areas for improvement."
        phiScore >= 4.0 -> "Your emotional state indicates some imbalance that's worth addressing. You might be experiencing some challenges with your well-being right now."
        phiScore >= 2.5 -> "You're currently experiencing some difficulties with your emotional well-being. This is a temporary state, and there are ways to improve how you're feeling."
        else -> "You're currently experiencing low well-being, and that's okay - you're not alone in feeling this way. It's important to be gentle with yourself right now."
    }

    val moodSpecificMessage = when (detectedMood.lowercase()) {
        "happy", "joy", "excited" -> " Your detected mood of ${detectedMood.lowercase()} aligns well with your current state."
        "sad", "depressed", "disappointed" -> " I notice you're feeling ${detectedMood.lowercase()} right now. Remember that these feelings are temporary."
        "angry", "frustrated", "annoyed" -> " Your ${detectedMood.lowercase()} feelings are valid, and there are healthy ways to process them."
        "calm", "peaceful", "relaxed" -> " Your ${detectedMood.lowercase()} state is beneficial for your overall well-being."
        "anxious", "worried", "stressed" -> " I can see you're feeling ${detectedMood.lowercase()}. Let's work on some techniques to help you feel more centered."
        else -> " Your current mood of ${detectedMood.lowercase()} is an important part of understanding your emotional state."
    }

    return baseDescription + moodSpecificMessage
}

@Preview(showBackground = true)
@Composable
fun Edit() {
    Box {
        TranslucentCircleIconButton(Icons.Default.Edit,"") { }
    }
}

@Composable
fun TranslucentCircleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(56.dp)
            .background(
                color = Color.White.copy(alpha = 0.2f),
                shape = CircleShape
            )
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = CircleShape
            )
            .shadow(8.dp, CircleShape, ambientColor = Color.White.copy(alpha = 0.05f))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
        )
    }
}

// Data model for exercise type
enum class BreathingExerciseType {
    BOX, FOUR_SEVEN_EIGHT, ALTERNATE_NOSTRIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingExerciseSheet(
    onDismiss: () -> Unit,
    onExerciseSelected: (BreathingExerciseType) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Choose a Breathing Exercise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            BreathingExerciseCard(
                icon = Icons.Default.CropSquare,
                title = "Box Breathing",
                description = "Breathe in, hold, out, hold (4-4-4-4)",
                onClick = { onExerciseSelected(BreathingExerciseType.BOX) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BreathingExerciseCard(
                icon = Icons.Default.Timer,
                title = "4-7-8 Breathing",
                description = "Inhale 4s, hold 7s, exhale 8s",
                onClick = { onExerciseSelected(BreathingExerciseType.FOUR_SEVEN_EIGHT) }
            )
            Spacer(modifier = Modifier.height(12.dp))
            BreathingExerciseCard(
                icon = Icons.Default.Air,
                title = "Alternate Nostril",
                description = "Breathe through alternate nostrils",
                onClick = { onExerciseSelected(BreathingExerciseType.ALTERNATE_NOSTRIL) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun BreathingExerciseCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(32.dp), tint = Color(0xFF6C63FF))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF666666))
            }
        }
    }
}