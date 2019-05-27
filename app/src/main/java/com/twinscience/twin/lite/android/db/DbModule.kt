package com.twinscience.twin.lite.android.db

import android.content.Context
import androidx.room.Room
import com.twinscience.twin.lite.android.project.db.ProjectDao
import com.twinscience.twin.lite.android.viewmodel.ViewModelModule
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class DbModule {
    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(context,
                AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build()
    }

    @Singleton
    @Provides
    fun provideProjectDao(appDatabase: AppDatabase): ProjectDao {
        return appDatabase.projectDao()
    }
}