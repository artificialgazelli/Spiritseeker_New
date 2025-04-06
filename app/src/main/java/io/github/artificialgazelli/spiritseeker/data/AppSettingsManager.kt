package com.example.spiritseeker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.spiritseeker.data.model.AppSettings // Import AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

// Create a single DataStore for all app settings
private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsManager @Inject constructor(@ApplicationContext appContext: Context) {

    private val dataStore = appContext.appSettingsDataStore

    // Define keys
    private object PreferencesKeys {
        // Pomodoro
        val DURATION = intPreferencesKey("pomodoro_duration")
        val SHORT_BREAK = intPreferencesKey("pomodoro_short_break")
        val LONG_BREAK = intPreferencesKey("pomodoro_long_break")
        val SESSIONS_BEFORE_LONG_BREAK = intPreferencesKey("pomodoro_sessions_before_long_break")
        val AUTO_START_BREAKS = booleanPreferencesKey("pomodoro_auto_start_breaks")
        val AUTO_START_POMODOROS = booleanPreferencesKey("pomodoro_auto_start_pomodoros")
        // Health Check
        val HEALTH_STATUS = booleanPreferencesKey("health_status")
        val LAST_HEALTH_CHECK = stringPreferencesKey("last_health_check")
        // General Points
        val TOTAL_POINTS = intPreferencesKey("total_points")
    }

    // Flow to read settings
    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                duration = preferences[PreferencesKeys.DURATION] ?: 25,
                shortBreak = preferences[PreferencesKeys.SHORT_BREAK] ?: 5,
                longBreak = preferences[PreferencesKeys.LONG_BREAK] ?: 15,
                sessionsBeforeLongBreak = preferences[PreferencesKeys.SESSIONS_BEFORE_LONG_BREAK] ?: 4,
                autoStartBreaks = preferences[PreferencesKeys.AUTO_START_BREAKS] ?: false,
                autoStartPomodoros = preferences[PreferencesKeys.AUTO_START_POMODOROS] ?: false,
                healthStatus = preferences[PreferencesKeys.HEALTH_STATUS] ?: true,
                lastHealthCheck = preferences[PreferencesKeys.LAST_HEALTH_CHECK],
                totalPoints = preferences[PreferencesKeys.TOTAL_POINTS] ?: 0
            )
        }

    // Function to update Pomodoro settings
    suspend fun updatePomodoroSettings(settings: AppSettings) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.DURATION] = settings.duration
            preferences[PreferencesKeys.SHORT_BREAK] = settings.shortBreak
            preferences[PreferencesKeys.LONG_BREAK] = settings.longBreak
            preferences[PreferencesKeys.SESSIONS_BEFORE_LONG_BREAK] = settings.sessionsBeforeLongBreak
            preferences[PreferencesKeys.AUTO_START_BREAKS] = settings.autoStartBreaks
            preferences[PreferencesKeys.AUTO_START_POMODOROS] = settings.autoStartPomodoros
            // Note: Doesn't update health or points here
        }
    }

    // Function to update health status
    suspend fun updateHealthCheck(status: Boolean, date: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.HEALTH_STATUS] = status
            preferences[PreferencesKeys.LAST_HEALTH_CHECK] = date
        }
    }

     // Function to add points
    suspend fun addTotalPoints(pointsToAdd: Int) {
         if (pointsToAdd <= 0) return
         dataStore.edit { preferences ->
            val currentPoints = preferences[PreferencesKeys.TOTAL_POINTS] ?: 0
            preferences[PreferencesKeys.TOTAL_POINTS] = currentPoints + pointsToAdd
        }
    }

     // Function to reset points (e.g., when redeeming rewards - maybe?)
     suspend fun setTotalPoints(points: Int) {
         dataStore.edit { preferences ->
            preferences[PreferencesKeys.TOTAL_POINTS] = points
        }
    }
}