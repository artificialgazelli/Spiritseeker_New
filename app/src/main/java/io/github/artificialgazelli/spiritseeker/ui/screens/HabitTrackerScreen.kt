package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState // Import rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Import Delete icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.data.model.Habit
import com.example.spiritseeker.ui.viewmodels.HabitTrackerViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddEditHabit: (String?) -> Unit, // Add callback for add/edit
    viewModel: HabitTrackerViewModel = hiltViewModel()
    // TODO: Add callback for navigating to Add/Edit Habit screen
) {
    val habits by viewModel.filteredHabits.collectAsState() // Use filtered list
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit Tracker") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToAddEditHabit(null) }) { // Navigate for adding
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp), // Add horizontal padding
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp) // Add padding top/bottom
        ) {
            // Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { viewModel.setCategoryFilter(null) },
                        label = { Text("All") }
                    )
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category.name,
                            onClick = { viewModel.setCategoryFilter(category.name) },
                            label = { Text(category.name) }
                        )
                    }
                    // TODO: Add chip/button to manage categories
                }
            }

            if (habits.isEmpty()) {
                item {
                    Text(
                        text = "No habits yet. Add some!",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                // Simple list for now, can be grouped by category later
                items(habits, key = { it.name }) { habit ->
                    HabitTrackerItem(
                        habit = habit,
                        onToggle = { viewModel.toggleHabitCompletion(habit) },
                        onClick = { onNavigateToAddEditHabit(habit.name) }, // Navigate for editing
                        onDelete = { viewModel.deleteHabit(habit) } // Add delete callback
                    )
                }
            }
        }
    }
}

@Composable
fun HabitTrackerItem(habit: Habit, onToggle: () -> Unit, onClick: () -> Unit, onDelete: () -> Unit) { // Add onDelete
    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    val isCompletedToday = habit.completedDates.contains(todayStr)

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() } // Make the whole card clickable
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                 Text(
                    text = habit.icon, // Display icon
                    style = MaterialTheme.typography.headlineSmall, // Larger icon
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column {
                    Text(text = habit.name, style = MaterialTheme.typography.titleMedium)
                    // Optionally display frequency, category, streak etc.
                    Text(
                        text = "Streak: ${habit.streak}", // Example detail
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Checkbox(checked = isCompletedToday, onCheckedChange = { onToggle() })
            // Add Delete Button
            IconButton(onClick = onDelete, modifier = Modifier.padding(start = 8.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Habit", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// TODO: Add Preview