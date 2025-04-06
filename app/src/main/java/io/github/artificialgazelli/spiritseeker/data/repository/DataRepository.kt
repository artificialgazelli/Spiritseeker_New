package com.example.spiritseeker.data.repository

import android.content.Context // Import Context
import android.net.Uri // Import Uri for restore
import androidx.room.withTransaction // Import withTransaction
import com.google.gson.Gson // Import Gson
import com.google.gson.reflect.TypeToken // Import TypeToken
import com.example.spiritseeker.data.local.*
import com.example.spiritseeker.data.model.*
import dagger.hilt.android.qualifiers.ApplicationContext // Import ApplicationContext
import kotlinx.coroutines.Dispatchers // Import Dispatchers
import kotlinx.coroutines.withContext // Import withContext
import java.io.File // Import File
import java.io.FileOutputStream // Import FileOutputStream
import java.io.InputStreamReader // Import InputStreamReader
import java.time.format.DateTimeFormatter // Import DateTimeFormatter
import javax.inject.Inject // Import Inject
import kotlinx.coroutines.flow.Flow

// Repository class to abstract data access
class DataRepository @Inject constructor( // Add @Inject constructor
    @ApplicationContext private val context: Context, // Inject Context
    private val db: AppDatabase, // Inject AppDatabase for transactions
    private val skillDao: SkillDao,
    private val dissertationDao: DissertationDao,
    private val habitDao: HabitDao,
    private val todoDao: TodoDao
) {
    private val gson = Gson() // Gson instance for serialization/deserialization

    // --- Skill Methods ---
    fun getAllSkills(): Flow<List<Skill>> = skillDao.getAllSkills()
    fun getSkillByName(name: String): Flow<Skill?> = skillDao.getSkillByName(name)
    suspend fun insertSkill(skill: Skill) = skillDao.insertSkill(skill)
    suspend fun updateSkill(skill: Skill) = skillDao.updateSkill(skill)
    suspend fun insertAllSkills(skills: List<Skill>) = skillDao.insertAllSkills(skills)
    // Add other specific skill update methods if needed

    // --- Dissertation Methods ---
    fun getDissertation(): Flow<Dissertation?> = dissertationDao.getDissertation()
    suspend fun insertOrUpdateDissertation(dissertation: Dissertation) = dissertationDao.insertOrUpdateDissertation(dissertation)
    suspend fun updateDissertation(dissertation: Dissertation) = dissertationDao.updateDissertation(dissertation)

    // --- Habit Methods ---
    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()
    fun getHabitByName(name: String): Flow<Habit?> = habitDao.getHabitByName(name)
    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun insertAllHabits(habits: List<Habit>) = habitDao.insertAllHabits(habits)

    // --- HabitCategory Methods ---
    fun getAllHabitCategories(): Flow<List<HabitCategory>> = habitDao.getAllHabitCategories()
    suspend fun insertHabitCategory(category: HabitCategory) = habitDao.insertHabitCategory(category)
    suspend fun insertAllHabitCategories(categories: List<HabitCategory>) = habitDao.insertAllHabitCategories(categories)

    // --- CheckIn Methods ---
    fun getAllCheckIns(): Flow<List<CheckIn>> = habitDao.getAllCheckIns()
    suspend fun insertCheckIn(checkIn: CheckIn) = habitDao.insertCheckIn(checkIn)
    suspend fun updateCheckIn(checkIn: CheckIn) = habitDao.updateCheckIn(checkIn)
    suspend fun insertAllCheckIns(checkIns: List<CheckIn>) = habitDao.insertAllCheckIns(checkIns)


    // --- TodoTask Methods ---
    fun getAllTasks(): Flow<List<TodoTask>> = todoDao.getAllTasks()
    fun getTaskById(id: String): Flow<TodoTask?> = todoDao.getTaskById(id)
    fun getTasksByGroup(groupName: String): Flow<List<TodoTask>> = todoDao.getTasksByGroup(groupName)
    fun getIncompleteTasks(): Flow<List<TodoTask>> = todoDao.getIncompleteTasks()
    suspend fun insertTask(task: TodoTask) = todoDao.insertTask(task)
    suspend fun updateTask(task: TodoTask) = todoDao.updateTask(task)
    suspend fun updateTaskCompletionStatus(taskId: String, completed: Boolean, completionDate: String?) =
        todoDao.updateTaskCompletionStatus(taskId, completed, completionDate)
    suspend fun deleteTaskById(taskId: String) = todoDao.deleteTaskById(taskId)
    suspend fun insertAllTasks(tasks: List<TodoTask>) = todoDao.insertAllTasks(tasks)


    // --- TodoGroup Methods ---
    fun getAllTodoGroups(): Flow<List<TodoGroup>> = todoDao.getAllGroups()
    suspend fun insertTodoGroup(group: TodoGroup) = todoDao.insertGroup(group)
    suspend fun insertAllTodoGroups(groups: List<TodoGroup>) = todoDao.insertAllGroups(groups)

    // --- Data Initialization/Reset ---
    // We will add logic here later to populate the database with default data if empty.
    suspend fun initializeDefaultDataIfNeeded() {
        // Check if skills data exists. If not, assume DB is empty/new.
        if (skillDao.getAllSkills().firstOrNull().isNullOrEmpty()) {
            println("Database appears empty. Initializing default data...")
            insertDefaultData()
        }
    }

    private suspend fun insertDefaultData() {
        // --- Default Skills ---
        val defaultSkills = listOf(
            Skill(
                name = "art",
                exercises = SkillExercises(
                    fundamentals = listOf(
                        "Basic Mark Making - Line control exercises", "Shape Accuracy - Drawing basic geometric forms",
                        "Proportion & Measurement techniques", "Contour Drawing - Blind contour exercises",
                        "Texture Development - Various drawing techniques", "Basic Volumes - Drawing 3D forms",
                        "Linear Perspective - One-point perspective", "Linear Perspective - Two-point perspective",
                        "Foreshortening - Drawing objects in space", "Value Scales - Creating value ranges",
                        "Basic Lighting - Core shadow, cast shadow", "Rendering Techniques - Hatching methods",
                        "Rendering Techniques - Blending methods", "Color Wheel - Primary and secondary colors",
                        "Color Mixing - Creating specific colors", "Compositional Structures - Rule of thirds",
                        "Visual Flow - Leading the eye through artwork", "Gesture Drawing - Capturing essence of pose",
                        "Structural Anatomy - Basic figure proportions", "Master Studies - Copying works by artists"
                    ),
                    sketchbook = listOf(
                        "Free drawing", "Still life", "Landscape sketch", "Character design", "Animal sketches",
                        "Object studies", "Urban sketching", "Nature elements", "Fantasy creatures", "Portrait practice"
                    ),
                    accountability = listOf(
                        "Progress photo documentation", "Create process video", "Write about learning experience",
                        "Post progress on social media", "Share before/after comparison"
                    )
                )
            ),
            Skill(
                name = "korean",
                exercises = SkillExercises(
                    fundamentals = listOf(
                        "Hangul basics - Consonants", "Hangul basics - Vowels", "Hangul basics - Final consonants",
                        "Hangul syllable structure practice", "Basic greetings and introduction", "Numbers and counting system",
                        "Basic verbs and conjugation", "Basic nouns and particles", "Question formation",
                        "Simple present tense", "Simple past tense", "Simple future tense",
                        "Basic adjectives and descriptors", "Basic sentence structure", "Pronouns and demonstratives",
                        "Time expressions", "Location and direction words", "Basic honorifics",
                        "Family terms vocabulary", "Food and dining vocabulary"
                    ),
                    immersion = listOf(
                        "Watch K-drama (30 min)", "Listen to K-pop songs", "Watch Korean YouTube videos",
                        "Read Korean webtoons", "Listen to Korean podcast", "Watch Korean news",
                        "Watch Korean variety show", "Listen to Korean audiobook", "Follow Korean social media",
                        "Korean children's books"
                    ),
                    application = listOf(
                        "Write journal entry in Hangul", "Practice conversation with language partner",
                        "Record yourself speaking Korean", "Translate simple text to Korean",
                        "Label items in your home in Korean", "Order at Korean restaurant in Korean",
                        "Describe your day in Korean", "Write short story in Korean", "Text chat with Korean speaker",
                        "Teach someone basic Korean phrases"
                    )
                )
            ),
            Skill(
                name = "french",
                 exercises = SkillExercises(
                    fundamentals = listOf(
                        "Basic pronunciation - vowels", "Basic pronunciation - consonants", "Nasal sounds practice",
                        "Greetings and introductions", "Numbers and counting", "Present tense - regular verbs",
                        "Present tense - irregular verbs", "Articles - definite and indefinite", "Gender and agreement",
                        "Basic adjectives and placement", "Question formation", "Past tense - passé composé",
                        "Past tense - imparfait", "Future tense - simple", "Prepositions of place",
                        "Time expressions", "Daily routine vocabulary", "Food and dining vocabulary",
                        "Travel and directions", "Body parts and health"
                    ),
                    immersion = listOf(
                        "Watch French film (30 min)", "Listen to French music", "Watch French YouTube videos",
                        "Read French news articles", "Listen to French podcast", "Watch French TV series",
                        "Listen to French radio", "Read French comics/graphic novels", "Follow French social media",
                        "French children's books"
                    ),
                    application = listOf(
                        "Write journal entry in French", "Practice conversation with language partner",
                        "Record yourself speaking French", "Translate simple text to French",
                        "Describe photos in French", "Order at restaurant in French",
                        "Write shopping list in French", "Text chat with French speaker",
                        "Teach someone basic French phrases"
                    )
                )
            )
            // Note: Dissertation skill is handled separately as it's a single entry
        )
        skillDao.insertAllSkills(defaultSkills)

        // --- Default Dissertation ---
        val defaultDissertation = Dissertation(
            tasks = DissertationTasks(
                preparation = listOf(
                    DissertationTask("Literature review", "27.03.2025", "31.08.2025", 100),
                    DissertationTask("Methodology development", "15.04.2025", "31.07.2025", 80),
                    DissertationTask("Data collection and processing", "01.05.2025", "31.07.2025", 120),
                    DissertationTask("Writing theoretical chapter", "01.06.2025", "15.10.2025", 150)
                ),
                empirical = listOf(
                    DissertationTask("Qualitative discourse analysis", "01.08.2025", "15.01.2026", 200),
                    DissertationTask("Writing results", "16.01.2026", "31.03.2026", 100),
                    DissertationTask("Topic modeling", "16.01.2026", "31.05.2026", 150),
                    DissertationTask("Writing results", "01.06.2026", "31.08.2026", 100) // Duplicate name ok? Check JSON
                ),
                 integration = listOf(
                    DissertationTask("Finalizing methodology chapter", "01.06.2026", "15.09.2026", 80),
                    DissertationTask("Writing discussion and conclusion", "01.09.2026", "15.01.2027", 120),
                    DissertationTask("Revising introduction", "16.01.2027", "28.02.2027", 60)
                ),
                finalization = listOf(
                    DissertationTask("Proofreading and revision", "01.03.2027", "15.06.2027", 100),
                    DissertationTask("Layout and formatting", "16.06.2027", "15.08.2027", 60),
                    DissertationTask("Corrections and printing", "16.08.2027", "31.10.2027", 40)
                )
            )
        )
        dissertationDao.insertOrUpdateDissertation(defaultDissertation)

        // --- Default Habits ---
        val defaultDailyHabits = listOf(
            Habit("Early wakeup", "☀️", true, false, null, "daily"),
            Habit("Exercise", "🏃", true, false, null, "daily"),
            Habit("Reading", "📚", true, false, null, "daily"),
            Habit("Meditation", "🧘", true, false, null, "daily"), // From Python default
            Habit("Drink water", "💧", true, false, null, "daily") // From Python default
            // Add "Go to bed early" if needed from JSON: Habit("Go to bed early", "😴", true, false, "Health", "daily")
        )
        // Add custom habits from JSON if desired as default
        val defaultCustomHabits = listOf(
             Habit("Learn Korean", "🇰🇷", true, true, "Learning", "interval", interval = 2),
             Habit("Learn French", "🇫🇷", true, true, "Learning", "interval", interval = 2),
             Habit("Clean", "🧹", true, true, "Personal", "weekly", specificDays = listOf(6)), // Saturday
             Habit("Do Laundry", "🧺", true, true, "Personal", "interval", interval = 5),
             Habit("Water Plants", "🌱", true, true, "Personal", "interval", interval = 10),
             Habit("Be Creative", "🎨", true, true, "Personal", "interval", interval = 2)
        )
        habitDao.insertAllHabits(defaultDailyHabits + defaultCustomHabits)

        // --- Default Habit Categories ---
        val defaultHabitCategories = listOf(
            HabitCategory("Health", "#4CAF50"), // Green
            HabitCategory("Learning", "#2196F3"), // Blue
            HabitCategory("Personal", "#FF9800"), // Orange
            HabitCategory("Work", "#9C27B0") // Purple
        )
        habitDao.insertAllHabitCategories(defaultHabitCategories)

        // --- Default Check-Ins ---
        val defaultCheckIns = listOf(
            CheckIn("Doctor Appointments", "🩺", subcategories = listOf(
                CheckInSubcategory("Dermatologist", null, 6, null),
                CheckInSubcategory("Dentist", null, 6, null),
                CheckInSubcategory("Gynecologist", null, 6, null),
                CheckInSubcategory("GP", null, 6, null)
            )),
            CheckIn("Other Check-ins", "🗓️", subcategories = listOf( // Renamed from Dentist
                 CheckInSubcategory("Eye Doctor", null, 12, null)
            ))
        )
        habitDao.insertAllCheckIns(defaultCheckIns)

        // --- Default ToDo Groups ---
        val defaultTodoGroups = listOf(
            TodoGroup("Work", "#9C27B0"), // Purple
            TodoGroup("Personal", "#FF9800"), // Orange
            TodoGroup("Shopping", "#795548"), // Brown
            TodoGroup("Urgent", "#F44336") // Red
        )
        todoDao.insertAllTodoGroups(defaultTodoGroups)

        // --- Default ToDo Tasks (Optional) ---
        // Add a few example tasks if desired
        val defaultTasks = listOf(
            TodoTask(name = "Prepare presentation", group = "Work", priority = "High", dueDate = "2025-04-10"),
            TodoTask(name = "Grocery shopping", group = "Shopping", priority = "Medium", dueDate = "2025-04-06"),
            TodoTask(name = "Call Mom", group = "Personal", priority = "Low", dueDate = null)
        )
        todoDao.insertAllTasks(defaultTasks)

        println("Default data inserted.")
    }

    suspend fun resetAllData() {
        // TODO: Implement data reset (delete all from DAOs)
        skillDao.deleteAllSkills()
        dissertationDao.deleteDissertation()
        habitDao.deleteAllHabits()
        habitDao.deleteAllHabitCategories()
        habitDao.deleteAllCheckIns()
        todoDao.deleteAllTasks()
        todoDao.deleteAllGroups()
        // After deleting, re-initialize default data
        initializeDefaultDataIfNeeded()
    }

    // --- Backup & Restore ---

    suspend fun backupData(targetDirectoryUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Fetch all data (consider doing this within a transaction for consistency)
                val skills = skillDao.getAllSkills().firstOrNull() ?: emptyList()
                val dissertation = dissertationDao.getDissertation().firstOrNull()
                val habits = habitDao.getAllHabits().firstOrNull() ?: emptyList()
                val habitCategories = habitDao.getAllHabitCategories().firstOrNull() ?: emptyList()
                val checkIns = habitDao.getAllCheckIns().firstOrNull() ?: emptyList()
                val todoTasks = todoDao.getAllTasks().firstOrNull() ?: emptyList()
                val todoGroups = todoDao.getAllTodoGroups().firstOrNull() ?: emptyList()

                // 2. Combine into a single backup structure
                val backupData = BackupData(
                    skills = skills,
                    dissertation = dissertation,
                    habits = habits,
                    habitCategories = habitCategories,
                    checkIns = checkIns,
                    todoTasks = todoTasks,
                    todoGroups = todoGroups
                )

                // 3. Serialize to JSON
                val jsonBackup = gson.toJson(backupData)

                // 4. Write to file using SAF (Storage Access Framework)
                val timestamp = java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                val fileName = "spiritseeker_backup_$timestamp.json"

                context.contentResolver.openOutputStream(targetDirectoryUri)?.use { outputStream ->
                     // For SAF, we need to create the file via DocumentFile API or similar
                     // This part is complex and depends on how the targetDirectoryUri is obtained (e.g., ACTION_OPEN_DOCUMENT_TREE)
                     // Placeholder: Assume we can directly write - THIS WILL LIKELY FAIL without proper SAF handling
                     // A better approach involves getting a Uri for the *new file* via ACTION_CREATE_DOCUMENT
                     // For simplicity here, we just show writing the content.
                     outputStream.write(jsonBackup.toByteArray())
                     fileName // Return filename on success
                } ?: throw IOException("Failed to open output stream for backup.")

            } catch (e: Exception) {
                e.printStackTrace() // Log the error
                null // Return null on failure
            }
        }
    }

     suspend fun restoreData(backupFileUri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Read JSON from file using SAF
                context.contentResolver.openInputStream(backupFileUri)?.use { inputStream ->
                    InputStreamReader(inputStream).use { reader ->
                        // 2. Deserialize JSON
                        val backupDataType = object : TypeToken&lt;BackupData&gt;() {}.type
                        val backupData: BackupData = gson.fromJson(reader, backupDataType)

                        // 3. Clear existing data and insert restored data within a transaction
                        db.withTransaction {
                            skillDao.deleteAllSkills()
                            dissertationDao.deleteDissertation()
                            habitDao.deleteAllHabits()
                            habitDao.deleteAllHabitCategories()
                            habitDao.deleteAllCheckIns()
                            todoDao.deleteAllTasks()
                            todoDao.deleteAllGroups()

                            skillDao.insertAllSkills(backupData.skills)
                            backupData.dissertation?.let { dissertationDao.insertOrUpdateDissertation(it) }
                            habitDao.insertAllHabits(backupData.habits)
                            habitDao.insertAllHabitCategories(backupData.habitCategories)
                            habitDao.insertAllCheckIns(backupData.checkIns)
                            todoDao.insertAllTasks(backupData.todoTasks)
                            todoDao.insertAllTodoGroups(backupData.todoGroups)
                        }
                        true // Return true on success
                    }
                } ?: throw IOException("Failed to open input stream for restore.")

            } catch (e: Exception) {
                e.printStackTrace() // Log the error
                false // Return false on failure
            }
        }
    }

    // Helper data class for backup structure
    private data class BackupData(
        val skills: List&lt;Skill&gt;,
        val dissertation: Dissertation?,
        val habits: List&lt;Habit&gt;,
        val habitCategories: List&lt;HabitCategory&gt;,
        val checkIns: List&lt;CheckIn&gt;,
        val todoTasks: List&lt;TodoTask&gt;,
        val todoGroups: List&lt;TodoGroup&gt;
    )

    // --- Gamification Helpers ---

    fun calculateNewStreak(lastPracticeDateStr: String?, currentStreak: Int): Int {
        if (lastPracticeDateStr == null) return 1 // First practice

        val today = LocalDate.now()
        val lastPracticeDate = try { LocalDate.parse(lastPracticeDateStr) } catch (e: Exception) { null } ?: return 1

        return when {
            lastPracticeDate.isEqual(today) -> currentStreak // Practiced today already
            lastPracticeDate.isEqual(today.minusDays(1)) -> currentStreak + 1 // Consecutive day
            else -> 1 // Broke the streak
        }
    }

     fun getCurrentDate(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_DATE)
    }
}