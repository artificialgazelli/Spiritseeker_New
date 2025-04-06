package com.example.spiritseeker.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult // Import launcher
import androidx.activity.result.contract.ActivityResultContracts // Import contracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController // Import NavController if passing it down
import com.example.spiritseeker.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    // navController: NavController, // Option 1: Pass NavController
    onNavigateToCheckIn: () -> Unit, // Option 2: Pass specific lambda
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val pomodoroSettings by viewModel.pomodoroSettings.collectAsState()
    val dataOperationStatus by viewModel.dataOperationStatus.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show snackbar when data operation status changes
    LaunchedEffect(dataOperationStatus) {
        dataOperationStatus?.let { status ->
            scope.launch {
                snackbarHostState.showSnackbar(status)
                viewModel.resetDataOperationStatus() // Clear status after showing
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // Make column scrollable
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            PomodoroSettingsSection(
                settings = pomodoroSettings,
                onSettingsChange = { viewModel.updatePomodoroSettings(it) }
            )

            DataManagementSection(
                onBackup = { viewModel.backupData() },
                onRestore = { viewModel.restoreData() }, // Needs file picker integration later
                onReset = { viewModel.resetData() },
                onNavigateToCheckIn = onNavigateToCheckIn // Pass lambda down
            )

            // TODO: Add Theme Settings Section
        }
    }
}

@Composable
fun PomodoroSettingsSection(
    settings: PomodoroSettings,
    onSettingsChange: (PomodoroSettings) -> Unit
) {
    var duration by remember(settings.duration) { mutableStateOf(settings.duration.toString()) }
    var shortBreak by remember(settings.shortBreak) { mutableStateOf(settings.shortBreak.toString()) }
    var longBreak by remember(settings.longBreak) { mutableStateOf(settings.longBreak.toString()) }
    var sessions by remember(settings.sessionsBeforeLongBreak) { mutableStateOf(settings.sessionsBeforeLongBreak.toString()) }
    var autoStartBreaks by remember(settings.autoStartBreaks) { mutableStateOf(settings.autoStartBreaks) }
    var autoStartPomodoros by remember(settings.autoStartPomodoros) { mutableStateOf(settings.autoStartPomodoros) }

    // Function to update settings via ViewModel
    fun update() {
        val newSettings = PomodoroSettings(
            duration = duration.toIntOrNull() ?: settings.duration,
            shortBreak = shortBreak.toIntOrNull() ?: settings.shortBreak,
            longBreak = longBreak.toIntOrNull() ?: settings.longBreak,
            sessionsBeforeLongBreak = sessions.toIntOrNull() ?: settings.sessionsBeforeLongBreak,
            autoStartBreaks = autoStartBreaks,
            autoStartPomodoros = autoStartPomodoros
        )
        onSettingsChange(newSettings)
    }

    Column {
        Text("Pomodoro Timer", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))

        SettingTextField(label = "Focus Duration (min)", value = duration) { duration = it; update() }
        SettingTextField(label = "Short Break (min)", value = shortBreak) { shortBreak = it; update() }
        SettingTextField(label = "Long Break (min)", value = longBreak) { longBreak = it; update() }
        SettingTextField(label = "Sessions before Long Break", value = sessions) { sessions = it; update() }

        SettingSwitch(label = "Auto-start Breaks", checked = autoStartBreaks) { autoStartBreaks = it; update() }
        SettingSwitch(label = "Auto-start Pomodoros", checked = autoStartPomodoros) { autoStartPomodoros = it; update() }
    }
}

@Composable
fun SettingTextField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        singleLine = true
    )
}

@Composable
fun SettingSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}


@Composable
fun DataManagementSection(
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onReset: () -> Unit,
    onNavigateToCheckIn: () -> Unit,
    // Add ViewModel interaction for backup/restore which now need URIs
    onBackupRequest: (Uri?) -> Unit,
    onRestoreRequest: (Uri?) -> Unit
) {
    val showResetDialog = remember { mutableStateOf(false) }
    if (showResetDialog.value) {
            AlertDialog(
                onDismissRequest = { showResetDialog.value = false },
                title = { Text("Confirm Reset") },
                text = { Text("Are you sure you want to reset all application data? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            onReset()
                            showResetDialog.value = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset Data")
                    }
                },
                dismissButton = {
                    Button(onClick = { showResetDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

    Column {
        Text("Data Management", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 8.dp))
        // --- Backup ---
        val backupLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocumentTree() // For selecting a directory
        ) { uri: Uri? ->
            onBackupRequest(uri) // Pass selected directory URI to ViewModel via callback
        }
        Button(
             onClick = { backupLauncher.launch(null) }, // Launch directory picker
             modifier = Modifier.fillMaxWidth()
        ) {
            Text("Backup Data")
        }
        Spacer(Modifier.height(8.dp))

        // --- Restore ---
         val restoreLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument() // For selecting a file
        ) { uri: Uri? ->
             onRestoreRequest(uri) // Pass selected file URI to ViewModel via callback
        }
        Button(
            onClick = { restoreLauncher.launch(arrayOf("application/json")) }, // Launch file picker for JSON
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Restore Data")
        }
         Spacer(Modifier.height(8.dp))

         // --- Check-in Nav Button ---
         Button(onClick = onNavigateToCheckIn, modifier = Modifier.fillMaxWidth()) {
            Text("View Health Check-ins")
        }
        Spacer(Modifier.height(8.dp))

        // --- Reset ---
        Button(onClick = onBackup, modifier = Modifier.fillMaxWidth()) {
            Text("Backup Data")
        }
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRestore, modifier = Modifier.fillMaxWidth()) {
            Text("Restore Data (Not Implemented)") // Indicate WIP
        }
         Spacer(Modifier.height(8.dp))
        Button(
            onClick = { showResetDialog.value = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Reset All Data", color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

// TODO: Add Preview