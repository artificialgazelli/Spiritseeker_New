package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.ui.viewmodels.PomodoroState
import com.example.spiritseeker.ui.viewmodels.PomodoroViewModel
import java.util.concurrent.TimeUnit

@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    val remainingTimeMillis by viewModel.remainingTimeMillis.collectAsState()
    val completedSessions by viewModel.completedSessions.collectAsState()
    // val settings by viewModel.settings.collectAsState() // Needed if displaying settings

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = formatTime(remainingTimeMillis),
            fontSize = 72.sp, // Large display for timer
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = getStatusText(timerState),
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Completed Sessions: $completedSessions",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Start/Pause Button
            Button(
                onClick = {
                    if (timerState == PomodoroState.RUNNING || timerState == PomodoroState.SHORT_BREAK || timerState == PomodoroState.LONG_BREAK) {
                        viewModel.pauseTimer()
                    } else {
                        viewModel.startTimer()
                    }
                },
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text(
                    when (timerState) {
                        PomodoroState.RUNNING, PomodoroState.SHORT_BREAK, PomodoroState.LONG_BREAK -> "Pause"
                        PomodoroState.PAUSED -> "Resume"
                        PomodoroState.IDLE -> "Start"
                    }
                )
            }

            // Reset Button (Enabled when not Idle)
            Button(
                onClick = { viewModel.resetTimer() },
                enabled = timerState != PomodoroState.IDLE,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text("Reset")
            }

             // Skip Button (Enabled when running)
            Button(
                onClick = { viewModel.skipTimer() },
                 enabled = timerState == PomodoroState.RUNNING || timerState == PomodoroState.SHORT_BREAK || timerState == PomodoroState.LONG_BREAK,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            ) {
                Text("Skip")
            }
        }

        // TODO: Add button/navigation to Pomodoro Settings screen later
    }
}

private fun formatTime(millis: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun getStatusText(state: PomodoroState): String {
    return when (state) {
        PomodoroState.IDLE -> "Ready to start"
        PomodoroState.RUNNING -> "Focus Time"
        PomodoroState.PAUSED -> "Paused"
        PomodoroState.SHORT_BREAK -> "Short Break"
        PomodoroState.LONG_BREAK -> "Long Break"
    }
}

// TODO: Add Preview if needed