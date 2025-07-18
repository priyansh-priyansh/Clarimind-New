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
        if (history.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No history yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { record ->
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mood: ${record.mood}", fontWeight = FontWeight.Bold)
            Text("Remembered Well-being: ${record.rememberedWellBeing}")
            Text("Experienced Well-being: ${record.experiencedWellBeing}")
            Text("Combined PHI: ${record.combinedPHI}")
            Text("Date: $date", style = MaterialTheme.typography.bodySmall)
        }
    }
} 