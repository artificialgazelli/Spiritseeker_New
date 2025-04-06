package com.example.spiritseeker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // Import viewModels delegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons // Import Icons
import androidx.compose.material.icons.filled.* // Import standard filled icons
import androidx.compose.material.icons.outlined.* // Import standard outlined icons (optional)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.spiritseeker.data.HealthCheckSettingsManager // Import manager
import com.example.spiritseeker.data.HealthCheckSettingsManager
import com.example.spiritseeker.ui.components.HealthCheckDialog
import com.example.spiritseeker.ui.screens.*
import com.example.spiritseeker.ui.viewmodels.SettingsViewModel // Import SettingsViewModel
import com.example.spiritseeker.ui.theme.SpiritseekerTheme
import com.example.spiritseeker.ui.viewmodels.MainViewModel // Import MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate // Import LocalDate
import java.time.format.DateTimeFormatter // Import DateTimeFormatter
import javax.inject.Inject // Import Inject

// Define navigation routes
object AppDestinations {
    const val MAIN_TABS = "main_tabs"
    const val SKILL_DETAIL_ROUTE = "skill_detail"
    const val SKILL_ID_ARG = "skillId" // Argument name for skill ID
    const val SKILL_DETAIL = "$SKILL_DETAIL_ROUTE/{$SKILL_ID_ARG}" // Route with argument
    const val HABIT_TRACKER = "habit_tracker"
    const val TODO_LIST = "todo_list"
    const val ADD_EDIT_HABIT_ROUTE = "add_edit_habit"
    const val HABIT_NAME_ARG = "habitName" // Optional argument for editing
    const val ADD_HABIT = ADD_EDIT_HABIT_ROUTE // Route for adding
    const val EDIT_HABIT = "$ADD_EDIT_HABIT_ROUTE?$HABIT_NAME_ARG={$HABIT_NAME_ARG}" // Route for editing (optional arg)
    // ToDo Add/Edit Routes
    const val ADD_EDIT_TASK_ROUTE = "add_edit_task"
    const val TASK_ID_ARG = "taskId" // Optional argument for editing
    const val ADD_TASK = ADD_EDIT_TASK_ROUTE
    const val EDIT_TASK = "$ADD_EDIT_TASK_ROUTE?$TASK_ID_ARG={$TASK_ID_ARG}"
    // Check-in Route
    const val CHECK_IN = "check_in"
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Inject MainViewModel using Hilt
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpiritseekerTheme {
                val settings by viewModel.healthSettings.collectAsState()
                val showHealthCheckDialog by viewModel.showHealthCheckDialog.collectAsState()

                AppNavigation(navController = rememberNavController()) // Pass NavController

                // Show dialog if needed
                if (showHealthCheckDialog) {
                    HealthCheckDialog(
                        onDismiss = { viewModel.dismissHealthCheckDialog() },
                        onSubmit = { eatingWell, exercised, mentalHealth ->
                            viewModel.submitHealthCheck(eatingWell, exercised, mentalHealth)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) { // Accept NavController
    // val navController = rememberNavController() // Remove local instance
    NavHost(navController = navController, startDestination = AppDestinations.MAIN_TABS) {
        // Main screen with Bottom Navigation (or Tabs)
        composable(AppDestinations.MAIN_TABS) {
            MainTabsScreen(navController = navController)
        }

        // Skill Detail Screen
        composable(
            route = AppDestinations.SKILL_DETAIL,
            arguments = listOf(navArgument(AppDestinations.SKILL_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val skillId = backStackEntry.arguments?.getString(AppDestinations.SKILL_ID_ARG)
            if (skillId != null) {
                // Call the actual SkillDetailScreen
                SkillDetailScreen(skillId = skillId, onNavigateBack = { navController.popBackStack() })
            } else {
                // Handle error: skillId not found
                Text("Error: Skill ID not found")
            }
        }
        // Habit Tracker Screen
        composable(AppDestinations.HABIT_TRACKER) {
             HabitTrackerScreen(
                 onNavigateBack = { navController.popBackStack() },
                 onNavigateToAddEditHabit = { habitName ->
                     // Navigate to Add/Edit screen, passing optional habit name
                     val route = if (habitName == null) {
                         AppDestinations.ADD_HABIT
                     } else {
                         "${AppDestinations.ADD_EDIT_HABIT_ROUTE}?${AppDestinations.HABIT_NAME_ARG}=$habitName"
                     }
                     navController.navigate(route)
                 }
             )
        }
        // ToDo List Screen
        composable(AppDestinations.TODO_LIST) {
             TodoListScreen(
                 onNavigateBack = { navController.popBackStack() },
                 onNavigateToAddEditTask = { taskId ->
                     val route = if (taskId == null) {
                         AppDestinations.ADD_TASK
                     } else {
                         "${AppDestinations.ADD_EDIT_TASK_ROUTE}?${AppDestinations.TASK_ID_ARG}=$taskId"
                     }
                     navController.navigate(route)
                 }
             )
        }
         // Add/Edit Habit Screen
        composable(
            route = EDIT_HABIT, // Use route that accepts optional argument
             arguments = listOf(navArgument(HABIT_NAME_ARG) { nullable = true; defaultValue = null }) // Set default explicitly
        ) { backStackEntry ->
             val habitName = backStackEntry.arguments?.getString(HABIT_NAME_ARG)
             // Use the actual AddEditHabitScreen
             AddEditHabitScreen(
                 habitName = habitName,
                 onNavigateBack = { navController.popBackStack() }
             )
        }
         // Add/Edit Task Screen
        composable(
            route = EDIT_TASK, // Use route that accepts optional argument
            arguments = listOf(navArgument(TASK_ID_ARG) { nullable = true; defaultValue = null })
        ) { backStackEntry ->
             val taskId = backStackEntry.arguments?.getString(TASK_ID_ARG)
             // Use the actual AddEditTaskScreen
             AddEditTaskScreen(
                 taskId = taskId,
                 onNavigateBack = { navController.popBackStack() }
             )
        }
         // Check-in Screen
        composable(AppDestinations.CHECK_IN) {
             CheckInScreen(onNavigateBack = { navController.popBackStack() })
        }
        // Add other destinations here
    }
}


@Composable
fun MainTabsScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel = hiltViewModel() // Inject SettingsViewModel
) {
    // Using Scaffold with BottomNavigation instead of TabRow for a more common mobile pattern
    // Define tabs with icons
    val tabs = listOf(
        ScreenTab("Modules", "modules_tab", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        ScreenTab("Rewards", "rewards_tab", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
        ScreenTab("Pomodoro", "pomodoro_tab", Icons.Filled.Timer, Icons.Outlined.Timer),
        ScreenTab("Statistics", "statistics_tab", Icons.Filled.BarChart, Icons.Outlined.BarChart),
        ScreenTab("Settings", "settings_tab", Icons.Filled.Settings, Icons.Outlined.Settings)
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar { // Use NavigationBar for Material 3
                tabs.forEachIndexed { index, screenTab ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (selectedTabIndex == index) screenTab.selectedIcon else screenTab.unselectedIcon,
                                contentDescription = screenTab.label
                            )
                        },
                        label = { Text(screenTab.label) },
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index }
                        // If using navigation routes per tab:
                        // onClick = {
                        //     navController.navigate(screenTab.route) {
                        //         popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        //         launchSingleTop = true
                        //         restoreState = true
                        //     }
                        //     selectedTabIndex = index // Update state even if navigating
                        // }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            // Content based on selected tab index
            when (selectedTabIndex) {
                0 -> ModulesScreen(
                    // Pass navigation lambda to ModulesScreen
                    onNavigateToSkill = { skillId ->
                        navController.navigate("${AppDestinations.SKILL_DETAIL_ROUTE}/$skillId")
                    },
                    // Pass navigation lambda for Habit Tracker
                    onNavigateToHabitTracker = {
                        navController.navigate(AppDestinations.HABIT_TRACKER)
                    },
                    // Pass navigation lambda for ToDo List
                    onNavigateToTodoList = {
                         navController.navigate(AppDestinations.TODO_LIST)
                    }
                )
                1 -> RewardsScreen()
                2 -> PomodoroScreen()
                3 -> StatisticsScreen()
                4 -> SettingsScreen(
                    onNavigateToCheckIn = { navController.navigate(AppDestinations.CHECK_IN) },
                    // Pass backup/restore callbacks to trigger ViewModel functions
                    onBackupRequest = { uri ->
                        if (uri != null) {
                            settingsViewModel.backupData(uri)
                        } else {
                            // Handle case where user cancelled directory selection
                            println("Backup cancelled by user.") // Placeholder feedback
                        }
                    },
                    onRestoreRequest = { uri ->
                         if (uri != null) {
                            settingsViewModel.restoreData(uri)
                        } else {
                            // Handle case where user cancelled file selection
                             println("Restore cancelled by user.") // Placeholder feedback
                        }
                    }
                )
            }
        }
    }
}

// Update ScreenTab data class to include icons
data class ScreenTab(
    val label: String,
    val route: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

// Remove the placeholder function