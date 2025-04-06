package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext // Import if needed for resources
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.ui.viewmodels.AddEditTaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: String?, // Null when adding
    onNavigateBack: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    val groups by viewModel.groups.collectAsState()
    val taskNameInput by viewModel.taskNameInput.collectAsState()
    val groupInput by viewModel.groupInput.collectAsState()
    val priorityInput by viewModel.priorityInput.collectAsState()
    val dueDateInput by viewModel.dueDateInput.collectAsState()
    val notesInput by viewModel.notesInput.collectAsState()

    var groupExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    val priorities = listOf("High", "Medium", "Low") // Define priorities

    val showDatePicker = remember { mutableStateOf(false) }

    if (showDatePicker.value) {
        TaskDatePickerDialog(
            initialDate = dueDateInput ?: LocalDate.now(),
            onDateSelected = {
                viewModel.updateDueDate(it)
                showDatePicker.value = false
            },
            onDismiss = { showDatePicker.value = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditing) "Edit Task" else "Add New Task") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        viewModel.saveTask()
                        onNavigateBack()
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
                value = taskNameInput,
                onValueChange = { viewModel.taskNameInput.value = it },
                label = { Text("Task Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Group Dropdown
            ExposedDropdownMenuBox(
                expanded = groupExpanded,
                onExpandedChange = { groupExpanded = !groupExpanded },
                modifier = Modifier.fillMaxWidth() // Apply fillMaxWidth here
            ) {
                 OutlinedTextField(
                    value = groupInput,
                    onValueChange = {}, readOnly = true, label = { Text("Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                 ExposedDropdownMenu(
                    expanded = groupExpanded,
                    onDismissRequest = { groupExpanded = false },
                    modifier = Modifier.exposedDropdownSize() // Recommended modifier
                ) {
                    groups.forEach { group ->
                        DropdownMenuItem(
                            text = { Text(group.name) },
                            onClick = {
                                viewModel.groupInput.value = group.name
                                groupExpanded = false
                            }
                        )
                    }
                     // TODO: Option to add new group?
                }
            }

             // Priority Dropdown
            ExposedDropdownMenuBox(
                expanded = priorityExpanded,
                onExpandedChange = { priorityExpanded = !priorityExpanded },
                modifier = Modifier.fillMaxWidth() // Apply fillMaxWidth here
            ) {
                 OutlinedTextField(
                    value = priorityInput,
                    onValueChange = {}, readOnly = true, label = { Text("Priority") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = priorityExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                 ExposedDropdownMenu(
                    expanded = priorityExpanded,
                    onDismissRequest = { priorityExpanded = false },
                    modifier = Modifier.exposedDropdownSize() // Recommended modifier
                ) {
                    priorities.forEach { priority ->
                        DropdownMenuItem(
                            text = { Text(priority) },
                            onClick = {
                                viewModel.priorityInput.value = priority
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }

            // Due Date Picker
            OutlinedTextField(
                value = dueDateInput?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Select Due Date (Optional)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Due Date") },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker.value = true },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select Date") }
            )

            OutlinedTextField(
                value = notesInput,
                onValueChange = { viewModel.notesInput.value = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp), // Allow multiple lines
                maxLines = 5
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            RecurrenceOptionsSection(viewModel) // Add recurrence section
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDatePickerDialog(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.toEpochDay() * 24 * 60 * 60 * 1000 // Convert LocalDate to millis UTC
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    // Convert millis back to LocalDate (assuming UTC)
                    onDateSelected(LocalDate.ofEpochDay(millis / (1000 * 60 * 60 * 24)))
                }
            }) { Text("OK") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun RecurrenceOptionsSection(viewModel: AddEditTaskViewModel) {
    // Get state from ViewModel
    val recurrenceEnabled by viewModel.recurrenceEnabled.collectAsState()
    val recurrenceType by viewModel.recurrenceType.collectAsState()
    val interval by viewModel.recurrenceInterval.collectAsState()
    val selectedDays by viewModel.recurrenceDaysOfWeek.collectAsState()
    val endDate by viewModel.recurrenceEndDate.collectAsState()

    val showEndDatePicker = remember { mutableStateOf(false) }

     if (showEndDatePicker.value) {
        TaskDatePickerDialog( // Re-use the date picker dialog
            initialDate = endDate ?: LocalDate.now().plusYears(1), // Default end date
            onDateSelected = {
                // endDate = it // State is now in ViewModel
                viewModel.updateRecurrenceEndDate(it) // Call ViewModel function
                showEndDatePicker.value = false
            },
            onDismiss = { showEndDatePicker.value = false }
        )
    }


    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Repeat Task?", style = MaterialTheme.typography.titleMedium)
            Switch(checked = recurrenceEnabled, onCheckedChange = { viewModel.recurrenceEnabled.value = it })
        }

        if (recurrenceEnabled) {
            Spacer(Modifier.height(8.dp))

            // Recurrence Type Dropdown
            var recurrenceTypeExpanded by remember { mutableStateOf(false) } // State for this dropdown
            ExposedDropdownMenuBox(
                expanded = recurrenceTypeExpanded,
                onExpandedChange = { recurrenceTypeExpanded = !recurrenceTypeExpanded }, // Toggle expanded state
                modifier = Modifier.fillMaxWidth()
            ) {
                 OutlinedTextField(
                    value = recurrenceType.replaceFirstChar { it.titlecase() },
                    onValueChange = {}, readOnly = true, label = { Text("Repeats") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = recurrenceTypeExpanded) }, // Use state for icon
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                 ExposedDropdownMenu(
                    expanded = recurrenceTypeExpanded, // Use state here
                    onDismissRequest = { recurrenceTypeExpanded = false }, // Close on dismiss
                    modifier = Modifier.exposedDropdownSize()
                 ) {
                     listOf("daily", "weekly", "monthly", "yearly").forEach { type ->
                         DropdownMenuItem(
                            text = { Text(type.replaceFirstChar { it.titlecase() }) },
                            onClick = {
                                viewModel.recurrenceType.value = type
                                recurrenceTypeExpanded = false // Close dropdown on selection
                            }
                        )
                     }
                 }
            }

            Spacer(Modifier.height(8.dp))

             OutlinedTextField(
                value = interval,
                onValueChange = { viewModel.recurrenceInterval.value = it.filter { it.isDigit() }.takeIf { it.isNotEmpty() } ?: "1" },
                label = { Text("Every") },
                 suffix = { Text( when(recurrenceType) { "weekly" -> " week(s)" "monthly" -> " month(s)" "yearly" -> " year(s)" else -> " day(s)" } ) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

             if (recurrenceType == "weekly") {
                Spacer(Modifier.height(8.dp))
                Text("On Days:", style = MaterialTheme.typography.titleSmall)
                 FlowRow(modifier = Modifier.fillMaxWidth(), maxItemsInEachRow = 4) {
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    days.forEachIndexed { index, day ->
                        FilterChip(
                            selected = selectedDays.contains(index),
                            onClick = {
                                // selectedDays = if (selectedDays.contains(index)) selectedDays - index else selectedDays + index
                                viewModel.updateRecurrenceDays(index, !selectedDays.contains(index)) // Call VM function
                            },
                            label = { Text(day) },
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // End Date Picker
             OutlinedTextField(
                value = endDate?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)) ?: "Never Ends",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ends On") },
                modifier = Modifier.fillMaxWidth().clickable { showEndDatePicker.value = true },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = "Select End Date") }
            )
        }
    }
}


// TODO: Add Preview