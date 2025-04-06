package com.example.spiritseeker.ui.screens

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
import com.example.spiritseeker.data.model.CheckIn
import com.example.spiritseeker.data.model.CheckInSubcategory
import com.example.spiritseeker.ui.viewmodels.CheckInViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onNavigateBack: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel()
) {
    val checkIns by viewModel.checkIns.collectAsState()
    val showDatePicker = remember { mutableStateOf(false) }
    // State to hold which check-in/subcategory is being updated
    val checkInToUpdate = remember { mutableStateOf<Pair<String, String>?>(null) }

    if (showDatePicker.value && checkInToUpdate.value != null) {
        CheckInDatePickerDialog(
            onDateSelected = { date ->
                checkInToUpdate.value?.let { (checkInName, subcategoryName) ->
                    viewModel.updateCheckInDate(checkInName, subcategoryName, date)
                }
                showDatePicker.value = false
                checkInToUpdate.value = null
            },
            onDismiss = {
                showDatePicker.value = false
                checkInToUpdate.value = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Check-ins") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
        // TODO: Add FAB if needed for adding new check-in types
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(checkIns, key = { it.name }) { checkIn ->
                CheckInCategorySection(
                    checkIn = checkIn,
                    onUpdateDateClick = { checkInName, subcategoryName ->
                        checkInToUpdate.value = Pair(checkInName, subcategoryName)
                        showDatePicker.value = true
                    },
                    onNotesChange = { checkInName, subcategoryName, notes ->
                        viewModel.updateCheckInNotes(checkInName, subcategoryName, notes) // Call ViewModel
                    }
                )
            }
        }
    }
}

@Composable
fun CheckInCategorySection(
    checkIn: CheckIn,
    onUpdateDateClick: (String, String) -> Unit,
    onNotesChange: (checkInName: String, subcategoryName: String, notes: String?) -> Unit // Add callback for notes
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
                 Text(
                    text = checkIn.icon,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Text(
                    text = checkIn.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Divider()
            checkIn.subcategories.forEach { subcategory ->
                CheckInSubcategoryItem(
                    subcategory = subcategory,
                    onUpdateDateClick = { onUpdateDateClick(checkIn.name, subcategory.name) },
                    onNotesChange = { notes -> onNotesChange(checkIn.name, subcategory.name, notes) } // Pass notes change up
                )
            }
        }
    }
}

@Composable
fun CheckInSubcategoryItem(
    subcategory: CheckInSubcategory,
    onUpdateDateClick: () -> Unit,
    onNotesChange: (String?) -> Unit // Add callback for notes change
) {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
    val lastDateStr = subcategory.lastDate?.let { LocalDate.parse(it).format(dateFormatter) } ?: "N/A"
    val nextDateStr = subcategory.nextDate?.let { LocalDate.parse(it).format(dateFormatter) } ?: "N/A"

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(subcategory.name, style = MaterialTheme.typography.titleMedium)
            Text("Last: $lastDateStr", style = MaterialTheme.typography.bodyMedium)
            Text("Next: $nextDateStr", style = MaterialTheme.typography.bodyMedium)
            // Notes Field
            OutlinedTextField(
                value = subcategory.notes ?: "",
                onValueChange = { onNotesChange(it.takeIf { it.isNotBlank() }) }, // Pass null if blank
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                textStyle = MaterialTheme.typography.bodySmall, // Smaller text for notes
                maxLines = 3
            )
        }
        Button(onClick = onUpdateDateClick, modifier = Modifier.padding(start = 8.dp)) { // Add padding
            Text("Update")
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInDatePickerDialog(
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    // Use current date as default initial selection
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = LocalDate.now().toEpochDay() * 24 * 60 * 60 * 1000
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(LocalDate.ofEpochDay(millis / (1000 * 60 * 60 * 24)))
                }
            }) { Text("OK") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

// TODO: Add Preview