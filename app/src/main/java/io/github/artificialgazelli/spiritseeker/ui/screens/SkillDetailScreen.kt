package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.data.model.*
import com.example.spiritseeker.ui.viewmodels.DissertationViewModel // Import Dissertation VM
import com.example.spiritseeker.ui.viewmodels.SkillDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillDetailScreen(
    skillId: String, // Received from navigation
    onNavigateBack: () -> Unit,
    viewModel: SkillDetailViewModel = hiltViewModel() // Hilt injects ViewModel with skillId
) {
    val skill by viewModel.skill.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${skillId.replaceFirstChar { it.titlecase() }} Quest") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (skill == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            SkillContent(
                skill = skill!!, // Safe non-null assertion after check
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun SkillContent(
    skill: Skill,
    viewModel: SkillDetailViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Stats
        item {
            SkillHeaderStats(skill)
        }

        // Sections based on skill type
        when (skill.name.lowercase()) {
            "art" -> {
                item { SkillSection("Fundamentals", skill.exercises?.fundamentals ?: emptyList()) { viewModel.logFundamentalCompleted(it) } }
                item { SkillSection("Sketchbook", skill.exercises?.sketchbook ?: emptyList()) { viewModel.logDrawing(it) } }
                item { SkillSection("Accountability", skill.exercises?.accountability ?: emptyList()) { viewModel.logAccountability(it) } }
                item { LogSection("Completed Fundamentals", skill.completedExercises) { CompletedExerciseLogItem(it) } }
                item { LogSection("Drawing Log", skill.drawingLog) { DrawingLogItem(it) } }
                // TODO: Add Accountability Log display if needed
            }
            "korean", "french" -> {
                item { SkillSection("Fundamentals", skill.exercises?.fundamentals ?: emptyList()) { viewModel.logFundamentalCompleted(it) } }
                item { SkillSection("Immersion", skill.exercises?.immersion ?: emptyList()) { viewModel.logImmersion(it, 0.5) } } // Default 0.5 hours
                item { SkillSection("Application", skill.exercises?.application ?: emptyList()) { viewModel.logApplication(it) } }
                item { LogSection("Completed Fundamentals", skill.completedLessons) { CompletedLessonLogItem(it) } }
                item { LogSection("Immersion Log", skill.immersionLog) { ImmersionLogItem(it) } }
                item { LogSection("Application Log", skill.applicationLog) { ApplicationLogItem(it) } }
            }
            "diss" -> {
                // Inject and use DissertationViewModel specifically for this case
                val dissViewModel: DissertationViewModel = hiltViewModel()
                val dissertation by dissViewModel.dissertation.collectAsState()

                item {
                    DissertationContent(
                        dissertation = dissertation,
                        onLogHours = { taskName, hours -> dissViewModel.logHoursWorked(taskName, hours) }
                    )
                }
            }
        }
    }
}

@Composable
fun SkillHeaderStats(skill: Skill) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Level", skill.level.toString())
            StatItem("Points", skill.points.toString())
            StatItem("Streak", "${skill.streak} days")
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.headlineSmall)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun SkillSection(
    title: String,
    items: List<String>,
    onItemClick: (String) -> Unit // Callback when an item is clicked/logged
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (items.isEmpty()) {
            Text("No items in this section.")
        } else {
            items.forEach { itemText ->
                SkillExerciseItem(text = itemText, onClick = { onItemClick(itemText) })
            }
        }
    }
}

@Composable
fun SkillExerciseItem(text: String, onClick: () -> Unit) {
    // Simple clickable text for now, can be enhanced
    Text(
        text = "- $text",
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    )
}

// --- Log Display Composables ---

@Composable
fun <T> LogSection(
    title: String,
    logEntries: List&lt;T&gt;,
    itemContent: @Composable (T) -> Unit
) {
    if (logEntries.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            logEntries.reversed().forEach { entry -> // Show newest first
                Divider(modifier = Modifier.padding(vertical = 4.dp))
                itemContent(entry)
            }
        }
    }
}

@Composable
fun CompletedExerciseLogItem(entry: CompletedExercise) {
    Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${entry.timestamp}: ${entry.exercise} (${entry.type})")
        Text("+${entry.points} pts")
    }
}

@Composable
fun CompletedLessonLogItem(entry: CompletedLesson) {
     Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${entry.timestamp}: ${entry.lesson} (${entry.type})")
        Text("+${entry.points} pts")
    }
}

@Composable
fun DrawingLogItem(entry: DrawingLogEntry) {
     Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${entry.timestamp}: ${entry.type}")
        Text("+${entry.points} pts")
    }
    // entry.notes?.let { Text(" Notes: $it", style = MaterialTheme.typography.bodySmall) } // Optional notes display
}

@Composable
fun ImmersionLogItem(entry: ImmersionLogEntry) {
     Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${entry.timestamp}: ${entry.type} (${entry.duration ?: ""})")
        Text("+${entry.points} pts")
    }
     // entry.title?.let { Text(" Title: $it", style = MaterialTheme.typography.bodySmall) } // Optional title display
}

@Composable
fun ApplicationLogItem(entry: ApplicationLogEntry) {
     Row(modifier = Modifier.padding(vertical = 4.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("${entry.timestamp}: ${entry.type}")
        Text("+${entry.points} pts")
    }
     // entry.notes?.let { Text(" Notes: $it", style = MaterialTheme.typography.bodySmall) } // Optional notes display
}


// --- Dissertation Specific Composables ---

@Composable
fun DissertationContent(
    dissertation: Dissertation?,
    onLogHours: (String, Double) -> Unit
) {
    if (dissertation == null) {
        Text("Loading dissertation data...")
        return
    }

    // Display overall dissertation stats (similar to SkillHeaderStats if needed)
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            StatItem("Level", dissertation.level.toString())
            StatItem("Points", dissertation.points.toString())
            StatItem("Streak", "${dissertation.streak} days") // If streak is tracked
        }
    }


    dissertation.tasks?.let { tasks ->
        DissertationTaskSection("Preparation", tasks.preparation, onLogHours)
        DissertationTaskSection("Empirical", tasks.empirical, onLogHours)
        DissertationTaskSection("Integration", tasks.integration, onLogHours)
        DissertationTaskSection("Finalization", tasks.finalization, onLogHours)
    } ?: Text("No dissertation tasks found.")
}

@Composable
fun DissertationTaskSection(
    title: String,
    tasks: List&lt;DissertationTask&gt;,
    onLogHours: (String, Double) -> Unit
) {
     Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        tasks.forEach { task ->
            DissertationTaskItem(task = task, onLogHours = onLogHours)
            Divider(modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
fun DissertationTaskItem(
    task: DissertationTask,
    onLogHours: (String, Double) -> Unit
) {
    var hoursToAdd by remember { mutableStateOf("") }
    val progress = if (task.totalHours > 0) (task.hoursWorked / task.totalHours).toFloat() else 0f

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(task.name, style = MaterialTheme.typography.titleMedium)
        Text(
            "Timeline: ${task.startDate} - ${task.endDate}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
             "Progress: ${task.hoursWorked.toInt()} / ${task.totalHours} hours (${String.format("%.1f", progress * 100)}%)",
             style = MaterialTheme.typography.bodyMedium
        )
        LinearProgressIndicator(
            progress = { progress }, // Use lambda for state reading
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            OutlinedTextField(
                value = hoursToAdd,
                onValueChange = { hoursToAdd = it.filter { char -> char.isDigit() || char == '.' } }, // Allow digits and decimal
                label = { Text("Log Hours") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f).height(50.dp), // Adjust height
                 textStyle = LocalTextStyle.current.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize), // Adjust text size
                 singleLine = true
            )
            Spacer(Modifier.width(8.dp))
            Button(
                onClick = {
                    val hours = hoursToAdd.toDoubleOrNull()
                    if (hours != null && hours > 0) {
                        onLogHours(task.name, hours)
                        hoursToAdd = "" // Clear input field
                    }
                },
                 modifier = Modifier.height(50.dp) // Match height
            ) {
                Text("Log")
            }
        }
    }
}


// TODO: Add Preview