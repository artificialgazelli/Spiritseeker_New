package com.example.spiritseeker.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.spiritseeker.data.repository.DataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // Import map, stateIn, SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

// Data class to hold all reward tiers
data class AllRewards(
    val small: List<String> = emptyList(),
    val medium: List<String> = emptyList(),
    val large: List<String> = emptyList()
)

@HiltViewModel
class RewardsViewModel @Inject constructor(
    private val repository: DataRepository // Inject repository for future use (fetching unlocked rewards)
) : ViewModel() {

    // Predefined rewards (mirroring the Python app's defaults)
    private val _rewards = MutableStateFlow(
        AllRewards(
            small = listOf(
                "New art supplies (pencils, pens)", "Korean snacks package", "French pastry treat",
                "Download a new playlist", "Movie night", "Special coffee or tea",
                "New stickers for journal", "Bath bomb or relaxation item", "Small plant or succulent",
                "Art print or bookmark"
            ),
            medium = listOf(
                "Art instruction book", "Korean webtoon collection", "French film collection",
                "Nice sketchbook or journal", "Language learning app subscription (1 month)",
                "Art supply set (markers, paints)", "Korean or French cuisine cookbook",
                "Online class or workshop", "Streaming service subscription (1 month)",
                "Museum or gallery admission"
            ),
            large = listOf(
                "Premium art course", "TOPIK prep materials full set", "Trip to a French cafe or restaurant",
                "Art software or digital tools", "Language tutoring session", "Premium art supplies kit",
                "Cultural experience or event ticket", "Annual subscription to learning platform",
                "Weekend creative retreat", "Professional drawing tablet"
            )
        )
    )
    val rewards: StateFlow<AllRewards> = _rewards.asStateFlow()

    // Flow that combines all unlocked rewards from all skills into a single set
    val unlockedRewards: StateFlow&lt;Set&lt;String&gt;&gt; = repository.getAllSkills()
        .map { skills -> skills.flatMap { it.rewardsUnlocked }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
}