package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.data.model.Habit
import com.example.spiritseeker.data.model.Skill
import com.example.spiritseeker.data.model.TodoTask
import com.example.spiritseeker.ui.viewmodels.ModulesViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ModulesScreen(
    onNavigateToSkill: (String) -> Unit,
    onNavigateToHabitTracker: () -> Unit,
    onNavigateToTodoList: () -> Unit, // Add callback for ToDo List
    viewModel: ModulesViewModel = hiltViewModel()
) {
    val skills by viewModel.skills.collectAsState()
    val todaysHabits by viewModel.todaysHabits.collectAsState()
    val todaysTasks by viewModel.todaysTasks.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title (Optional, could be in TopAppBar)
        item {
            Text(
                text = "SKILL QUEST 2025",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Choose your skill adventure!",
                style = MaterialTheme.typography.bodyLarge
            )
        }

        // Skill Quest Buttons Section
        item {
            SkillQuestButtons(skills = skills, onOpenQuest = onNavigateToSkill) // Pass navigation callback
        }

        // Daily Dashboard Section
        item {
            DailyDashboard(
                habits = todaysHabits,
                tasks = todaysTasks,
                onToggleHabit = viewModel::toggleHabitCompletion,
                onToggleTask = viewModel::toggleTaskCompletion,
                onOpenHabitTracker = onNavigateToHabitTracker, // Use navigation callback
                onOpenTodoList = onNavigateToTodoList, // Use navigation callback
                onAddNewTask = viewModel::onAddNewTask,
                onAddNewHabit = viewModel::onAddNewHabit
            )
        }
    }
}

@Composable
fun SkillQuestButtons(
    skills: Map<String, Skill>,
    onOpenQuest: (String) -> Unit
) {
    // Use FlowRow from Accompanist or basic Row with wrapping if needed
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly // Adjust arrangement as needed
    ) {
        SkillButton(skills["art"], "Art Quest", Color(0xFFE57373), onOpenQuest) // Example Color
        SkillButton(skills["korean"], "Korean Quest", Color(0xFF81C784), onOpenQuest) // Example Color
        SkillButton(skills["french"], "French Quest", Color(0xFF64B5F6), onOpenQuest) // Example Color
        SkillButton(skills["diss"], "Diss Quest", Color(0xFFFFF176), onOpenQuest) // Example Color
    }
}

@Composable
fun SkillButton(
    skill: Skill?,
    title: String,
    color: Color,
    onOpenQuest: (String) -> Unit
) {
    val skillName = title.split(" ")[0].lowercase() // Extract skill name ("art", "korean", etc.)

    OutlinedCard(
        modifier = Modifier.padding(4.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = { onOpenQuest(skillName) },
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text(title, color = Color.Black) // Adjust text color based on button color
            }
            if (skill != null) {
                Text(
                    text = "Level: ${skill.level} | Pts: ${skill.points}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Streak: ${skill.streak} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary // Or a specific streak color
                )
            } else {
                Text(text = "Loading...", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}


@Composable
fun DailyDashboard(
    habits: List<Habit>,
    tasks: List<TodoTask>,
    onToggleHabit: (Habit) -> Unit,
    onToggleTask: (TodoTask) -> Unit,
    onOpenHabitTracker: () -> Unit,
    onOpenTodoList: () -> Unit,
    onAddNewTask: () -> Unit,
    onAddNewHabit: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Daily Dashboard",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Habits Column
                Column(modifier = Modifier.weight(1f)) {
                    Text("Today's Habits", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                    if (habits.isEmpty()) {
                        Text("No habits for today.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        habits.forEach { habit ->
                            HabitItem(habit = habit, onToggleHabit = onToggleHabit)
                        }
                    }
                }
                // Tasks Column
                Column(modifier = Modifier.weight(1f)) {
                    Text("Today's Tasks", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(bottom = 8.dp))
                     if (tasks.isEmpty()) {
                        Text("No tasks for today.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        tasks.forEach { task ->
                            TaskItem(task = task, onToggleTask = onToggleTask)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = onOpenHabitTracker) { Text("Habits") }
                Button(onClick = onOpenTodoList) { Text("Tasks") }
                Button(onClick = onAddNewHabit) { Text("+ Habit") }
                Button(onClick = onAddNewTask) { Text("+ Task") }
            }
        }
    }
}

@Composable
fun HabitItem(habit: Habit, onToggleHabit: (Habit) -> Unit) {
    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    val isCompleted = habit.completedDates.contains(todayStr)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = { onToggleHabit(habit) }
        )
        Text(
            text = "${habit.icon} ${habit.name}",
            modifier = Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
        // Optionally show streak
        // Text(text = " (${habit.streak})", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun TaskItem(task: TodoTask, onToggleTask: (TodoTask) -> Unit) {
     Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = task.completed,
            onCheckedChange = { onToggleTask(task) }
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
             Text(
                text = task.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (task.priority == "High") FontWeight.Bold else FontWeight.Normal
            )
            task.dueDate?.let {
                 Text(
                    text = "Due: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (it < LocalDate.now().format(DateTimeFormatter.ISO_DATE) && !task.completed) MaterialTheme.colorScheme.error else Color.Gray
                )
            }
        }
    }
}