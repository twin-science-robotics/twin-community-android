package com.twinscience.twin.lite.android.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.twinscience.twin.lite.android.project.data.ProjectEntity
import com.twinscience.twin.lite.android.project.db.ProjectDao

@Database(entities = [ProjectEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @JvmField
        var DATABASE_NAME: String = "twin-lite-app-db"
    }
}