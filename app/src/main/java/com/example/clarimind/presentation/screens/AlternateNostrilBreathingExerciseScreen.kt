package com.example.clarimind.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.clarimind.presentation.viewmodels.AlternateNostrilBreathingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlternateNostrilBreathingExerciseScreen(onBack: () -> Unit, viewModel: AlternateNostrilBreathingViewModel = viewModel()) {
    val phase by viewModel.phase.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val side by viewModel.side.collectAsState()
    val targetScale = if (isRunning) 1.2f else 0.7f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 4000, easing = LinearEasing),
        label = "nostril"
    )
    val nostrilColor = if (side == "Left") Color(0xFFB39DDB) else Color(0xFFFFCC80)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alternate Nostril Breathing", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF6C63FF), titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8F9FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Alternate Nostril Breathing",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Breathe in through one nostril, out through the other. Alternate sides.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF444444),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size((180 * scale).coerceAtMost(220f).dp)
                            .clip(CircleShape)
                            .background(nostrilColor.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${side} Nostril",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6C63FF)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "$phase: $timeLeft s",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF666666)
                )
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { viewModel.toggleBreathing() },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text(if (isRunning) "Pause" else "Start", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("How to do Alternate Nostril Breathing:", fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Sit comfortably and relax.")
                        Text("2. Close your right nostril with your thumb and inhale through your left nostril.")
                        Text("3. Close your left nostril with your ring finger, release your right nostril, and exhale through your right nostril.")
                        Text("4. Inhale through your right nostril, then close it and exhale through your left nostril.")
                        Text("5. Repeat the cycle for a few minutes, alternating sides.")
                    }
                }
            }
        }
    }
} 