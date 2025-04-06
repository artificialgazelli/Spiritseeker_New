package com.example.spiritseeker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HealthCheckDialog(
    onDismiss: () -> Unit,
    onSubmit: (eatingWell: Boolean, exercised: Boolean, mentalHealth: Boolean) -> Unit
) {
    var eatingWell by remember { mutableStateOf(true) }
    var exercised by remember { mutableStateOf(true) }
    var mentalHealth by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Health Check") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                HealthCheckItem(
                    text = "I ate well today",
                    checked = eatingWell,
                    onCheckedChange = { eatingWell = it }
                )
                HealthCheckItem(
                    text = "I exercised or moved my body today",
                    checked = exercised,
                    onCheckedChange = { exercised = it }
                )
                HealthCheckItem(
                    text = "I took care of my mental health today",
                    checked = mentalHealth,
                    onCheckedChange = { mentalHealth = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(eatingWell, exercised, mentalHealth) }) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Later") // Or "Cancel"
            }
        }
    )
}

@Composable
private fun HealthCheckItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}