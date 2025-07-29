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
import com.example.clarimind.presentation.viewmodels.FourSevenEightBreathingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FourSevenEightBreathingExerciseScreen(onBack: () -> Unit, viewModel: FourSevenEightBreathingViewModel = viewModel()) {
    val phase by viewModel.phase.collectAsState()
    val timeLeft by viewModel.timeLeft.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val targetScale = if (isRunning) 1.2f else 0.7f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = 8000, easing = LinearEasing),
        label = "breath478"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("4-7-8 Breathing", fontWeight = FontWeight.Bold) },
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
                    "4-7-8 Breathing",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Inhale for 4s, hold for 7s, exhale for 8s. Repeat.",
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
                            .background(Color(0xFFC8E6C9).copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = phase,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF388E3C)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Time left: $timeLeft s",
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
                        Text("How to do 4-7-8 Breathing:", fontWeight = FontWeight.Bold, color = Color(0xFF6C63FF))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("1. Inhale quietly through your nose for 4 seconds.")
                        Text("2. Hold your breath for 7 seconds.")
                        Text("3. Exhale completely through your mouth for 8 seconds.")
                        Text("4. Repeat the cycle for a few minutes.")
                    }
                }
            }
        }
    }
} 