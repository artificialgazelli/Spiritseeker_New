package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.data.model.TodoTask
import com.example.spiritseeker.ui.viewmodels.TodoListViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEditTask: (String?) -> Unit, // Add callback
    viewModel: TodoListViewModel = hiltViewModel()
    // TODO: Add callback for navigating to Add/Edit Task screen
) {
    val tasks by viewModel.filteredTasks.collectAsState() // Use filtered list
    val groups by viewModel.groups.collectAsState()
    val selectedGroup by viewModel.selectedGroupFilter.collectAsState()

    // Separate tasks into pending and completed
    val (pendingTasks, completedTasks) = tasks.partition { !it.completed }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("To-Do List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddEditTask(null) }) { // Navigate for adding
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Group Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGroup == null,
                        onClick = { viewModel.setGroupFilter(null) },
                        label = { Text("All") }
                    )
                    groups.forEach { group ->
                        FilterChip(
                            selected = selectedGroup == group.name,
                            onClick = { viewModel.setGroupFilter(group.name) },
                            label = { Text(group.name) }
                        )
                    }
                     // TODO: Add chip/button to manage groups
                }
            }

            item {
                Text("Pending Tasks", style = MaterialTheme.typography.titleMedium)
            }
            if (pendingTasks.isEmpty()) {
                item { Text("No pending tasks!", modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
            } else {
                items(pendingTasks, key = { it.id }) { task ->
                    TodoListItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onClick = { onNavigateToAddEditTask(task.id) }, // Navigate for editing
                        onDelete = { viewModel.deleteTask(task) } // Add delete callback
                    )
                }
            }

            item {
                 Spacer(modifier = Modifier.height(16.dp))
                 Text("Completed Tasks", style = MaterialTheme.typography.titleMedium)
            }
             if (completedTasks.isEmpty()) {
                item { Text("No completed tasks yet.", modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
            } else {
                items(completedTasks, key = { it.id }) { task ->
                     TodoListItem(
                        task = task,
                        onToggle = { viewModel.toggleTaskCompletion(task) },
                        onClick = { onNavigateToAddEditTask(task.id) }, // Navigate for editing
                        onDelete = { viewModel.deleteTask(task) } // Add delete callback
                    )
                }
            }
        }
    }
}

@Composable
fun TodoListItem(task: TodoTask, onToggle: () -> Unit, onClick: () -> Unit, onDelete: () -> Unit) { // Add onDelete
    val isOverdue = task.dueDate != null && task.dueDate!! < LocalDate.now().format(DateTimeFormatter.ISO_DATE) && !task.completed
    val textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
    val textColor = if (task.completed) Color.Gray else LocalContentColor.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() } // Make row clickable
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = task.completed, onCheckedChange = { onToggle() })
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = task.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (task.priority == "High") FontWeight.Bold else FontWeight.Normal,
                textDecoration = textDecoration,
                color = textColor
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                 if (task.dueDate != null) {
                    Text(
                        text = "Due: ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error else Color.Gray
                    )
                }
                Text(
                    text = task.group, // Display group
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                 // Could add priority text as well
            }
        }
        // Add Delete Button
        IconButton(onClick = onDelete, modifier = Modifier.padding(start = 8.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = MaterialTheme.colorScheme.error)
        }
    }
}

// TODO: Add Preview