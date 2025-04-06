package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.ui.viewmodels.AppStatistics
import com.example.spiritseeker.ui.viewmodels.SkillStat
import com.example.spiritseeker.ui.viewmodels.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val stats by viewModel.statistics.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Overall Statistics",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            OverallStatsCard(stats)
        }

        item {
            Text(
                text = "Skill Progress",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }

        items(stats.skillStats.values.toList()) { skillStat ->
            SkillStatCard(skillStat)
        }

        // Add more sections for detailed habit/task stats if needed
    }
}

@Composable
fun OverallStatsCard(stats: AppStatistics) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            StatRow("Total Points Earned:", stats.totalPoints.toString())
            StatRow("Habit Completion (Today):", String.format("%.1f%%", stats.habitCompletionRate))
            StatRow("Tasks Completed:", stats.tasksCompleted.toString())
            StatRow("Tasks Pending:", stats.tasksPending.toString())
            StatRow("Other Points Earned:", stats.generalPoints.toString()) // Display general points
        }
    }
}

@Composable
fun SkillStatCard(skillStat: SkillStat) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(skillStat.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            StatRow("Level:", skillStat.level.toString())
            StatRow("Points:", skillStat.points.toString())
            StatRow("Current Streak:", "${skillStat.streak} days")
            // Add specific stats based on skill type if needed
            if (skillStat.name.equals("Art", ignoreCase = true)) {
                 StatRow("Fundamentals Done:", skillStat.fundamentalsCompleted.toString())
                 StatRow("Sketchbook Pages:", skillStat.sketchbookPages.toString())
                 StatRow("Accountability Posts:", skillStat.accountabilityPosts.toString())
            } else if (skillStat.name.equals("Korean", ignoreCase = true) || skillStat.name.equals("French", ignoreCase = true)) {
                 StatRow("Fundamentals Done:", skillStat.fundamentalsCompleted.toString())
                 StatRow("Immersion Hours:", String.format("%.1f", skillStat.immersionHours))
                 StatRow("Application Sessions:", skillStat.applicationSessions.toString())
            }
            // Add Diss stats if tracked separately
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

// TODO: Add Preview