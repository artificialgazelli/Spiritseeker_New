package com.example.spiritseeker.di

import android.content.Context
import com.example.spiritseeker.data.HealthCheckSettingsManager // Import new manager
import com.example.spiritseeker.data.local.*
import com.example.spiritseeker.data.repository.DataRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return AppDatabase.getDatabase(appContext)
    }

    @Provides
    @Singleton
    fun provideSkillDao(appDatabase: AppDatabase): SkillDao {
        return appDatabase.skillDao()
    }

    @Provides
    @Singleton
    fun provideDissertationDao(appDatabase: AppDatabase): DissertationDao {
        return appDatabase.dissertationDao()
    }

    @Provides
    @Singleton
    fun provideHabitDao(appDatabase: AppDatabase): HabitDao {
        return appDatabase.habitDao()
    }

    @Provides
    @Singleton
    fun provideTodoDao(appDatabase: AppDatabase): TodoDao {
        return appDatabase.todoDao()
    }

    @Provides
    @Singleton
    fun provideDataRepository(
        @ApplicationContext context: Context, // Add Context
        db: AppDatabase, // Add AppDatabase
        skillDao: SkillDao,
        dissertationDao: DissertationDao,
        habitDao: HabitDao,
        todoDao: TodoDao
    ): DataRepository {
        return DataRepository(context, db, skillDao, dissertationDao, habitDao, todoDao) // Pass context and db
    }

    // No need to provide PomodoroSettingsManager explicitly if @Inject constructor is used
    // Hilt handles it automatically. Same for HealthCheckSettingsManager.
}