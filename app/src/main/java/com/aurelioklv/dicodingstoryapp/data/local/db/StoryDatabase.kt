package com.aurelioklv.dicodingstoryapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.aurelioklv.dicodingstoryapp.data.local.db.dao.RemoteKeysDao
import com.aurelioklv.dicodingstoryapp.data.local.db.dao.StoryDao
import com.aurelioklv.dicodingstoryapp.data.local.entity.RemoteKeys
import com.aurelioklv.dicodingstoryapp.data.remote.api.StoryItem

@Database(entities = [StoryItem::class, RemoteKeys::class], version = 1, exportSchema = false)
abstract class StoryDatabase : RoomDatabase() {

    abstract fun storyDao(): StoryDao
    abstract fun remoteKeysDao(): RemoteKeysDao

    companion object {
        @Volatile
        private var INSTANCE: StoryDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): StoryDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    StoryDatabase::class.java,
                    "story_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}