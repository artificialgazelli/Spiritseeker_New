package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowDropDown // Import dropdown icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.ui.viewmodels.AddEditHabitViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditHabitScreen(
    habitName: String?, // Null when adding, non-null when editing
    onNavigateBack: () -> Unit,
    viewModel: AddEditHabitViewModel = hiltViewModel()
) {
    val categories by viewModel.categories.collectAsState()
    val habitNameInput by viewModel.habitNameInput.collectAsState()
    val iconInput by viewModel.iconInput.collectAsState()
    val categoryInput by viewModel.categoryInput.collectAsState()
    val frequencyInput by viewModel.frequencyInput.collectAsState()
    val intervalInput by viewModel.intervalInput.collectAsState()
    val specificDaysInput by viewModel.specificDaysInput.collectAsState()

    var categoryExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Habit" else "Add New Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        viewModel.saveHabit()
                        onNavigateBack() // Navigate back after saving
                    }) {
                        Text("Save")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = habitNameInput,
                onValueChange = { viewModel.habitNameInput.value = it },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                readOnly = viewModel.isEditing // Don't allow editing name for now
            )

            OutlinedTextField(
                value = iconInput,
                onValueChange = { viewModel.iconInput.value = it }, // Basic icon input, could use emoji picker
                label = { Text("Icon (Emoji)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded },
                modifier = Modifier.fillMaxWidth() // Apply fillMaxWidth here
            ) {
                 OutlinedTextField(
                    value = categoryInput ?: "Select Category", // Display selected or placeholder
                    onValueChange = {}, // Not directly editable
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth() // Apply menuAnchor modifier
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.exposedDropdownSize() // Recommended modifier
                ) {
                    // Option for no category
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            viewModel.categoryInput.value = null
                            categoryExpanded = false
                        }
                    )
                    // Options for existing categories
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.categoryInput.value = category.name
                                categoryExpanded = false
                            }
                        )
                    }
                    // TODO: Option to add a new category?
                }
            }


            // Frequency Selection
            Text("Frequency", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth()) {
                FrequencyChip("daily", frequencyInput == "daily") { viewModel.frequencyInput.value = "daily" }
                FrequencyChip("weekly", frequencyInput == "weekly") { viewModel.frequencyInput.value = "weekly" }
                FrequencyChip("interval", frequencyInput == "interval") { viewModel.frequencyInput.value = "interval" }
            }

            // Conditional Inputs based on Frequency
            when (frequencyInput) {
                "weekly" -> {
                    Text("Select Days", style = MaterialTheme.typography.titleSmall)
                    FlowRow(modifier = Modifier.fillMaxWidth(), maxItemsInEachRow = 4) {
                        // Display chips for each day of the week (Sunday=0)
                        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        days.forEachIndexed { index, day ->
                            FilterChip(
                                selected = specificDaysInput.contains(index),
                                onClick = { viewModel.updateSpecificDays(index, !specificDaysInput.contains(index)) },
                                label = { Text(day) },
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
                "interval" -> {
                    OutlinedTextField(
                        value = intervalInput,
                        onValueChange = { viewModel.intervalInput.value = it.filter { it.isDigit() } },
                        label = { Text("Repeat Every (days)") },
                        keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun FrequencyChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label.replaceFirstChar { it.titlecase() }) },
        modifier = Modifier.padding(end = 8.dp)
    )
}

// TODO: Add Preview