package com.example.spiritseeker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.spiritseeker.ui.viewmodels.RewardsViewModel

@Composable
fun RewardsScreen(
    viewModel: RewardsViewModel = hiltViewModel()
) {
    val allRewards by viewModel.rewards.collectAsState()
    val unlockedRewards by viewModel.unlockedRewards.collectAsState() // Collect unlocked rewards
    // val unlockedRewards by viewModel.unlockedRewards.collectAsState() // Use this later

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Rewards Shop",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            RewardSection(title = "Small Rewards", rewards = allRewards.small, unlockedRewards = unlockedRewards)
        }

        item {
            RewardSection(title = "Medium Rewards", rewards = allRewards.medium, unlockedRewards = unlockedRewards)
        }

        item {
            RewardSection(title = "Large Rewards", rewards = allRewards.large, unlockedRewards = unlockedRewards)
        }
    }
}

@Composable
fun RewardSection(
    title: String,
    rewards: List&lt;String&gt;,
    unlockedRewards: Set&lt;String&gt; // Pass unlocked set
) {
    Column {
        Text( // Add the missing Text call for the title
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        rewards.forEach { reward ->
            val isUnlocked = unlockedRewards.contains(reward) // Check if unlocked
            RewardItem(rewardText = reward, isUnlocked = isUnlocked) // Pass unlocked status
        }
    }
}
@Composable
fun RewardItem(
    rewardText: String,
    isUnlocked: Boolean // Use for styling
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
         elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 0.dp else 2.dp), // Different elevation
         colors = CardDefaults.cardColors(
             containerColor = if (isUnlocked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface // Different background
         )
    ) {
        Text(
            text = rewardText,
            modifier = Modifier.padding(16.dp),
             color = if (isUnlocked) MaterialTheme.colorScheme.onSurfaceVariant else LocalContentColor.current // Different text color
        )
    }
}
// Remove extra closing braces from previous attempt

// TODO: Add Preview