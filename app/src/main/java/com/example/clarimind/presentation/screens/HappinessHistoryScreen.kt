package com.example.clarimind.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clarimind.data.HappinessHistoryEntity
import com.example.clarimind.data.UsageDatabase
import com.example.clarimind.data.FirebaseSyncHelper
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HappinessHistoryScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var history by remember { mutableStateOf<List<HappinessHistoryEntity>>(emptyList()) }
    val userId = FirebaseSyncHelper.getCurrentUserId()

    LaunchedEffect(Unit) {
        val db = UsageDatabase.getInstance(context)
        history = db.happinessHistoryDao().getHistoryForUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Happiness & Mood History", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val filteredHistory = history.filter {
            it.rememberedWellBeing != 0.0 && it.experiencedWellBeing != 0.0 && it.combinedPHI != 0.0
        }
        if (filteredHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No valid happiness history yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredHistory) { record ->
                    HistoryItem(record)
                }
            }
        }
    }
}

@Composable
private fun HistoryItem(record: HappinessHistoryEntity) {
    val date = remember(record.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(record.timestamp))
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Mood, contentDescription = null, tint = Color(0xFF6C63FF), modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "${record.mood}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6C63FF)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Remembered: ${"%.1f".format(record.rememberedWellBeing)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    "Experienced: ${"%.1f".format(record.experiencedWellBeing)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Combined PHI: ${"%.1f".format(record.combinedPHI)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF333333)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    date,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }
        }
    }
} 