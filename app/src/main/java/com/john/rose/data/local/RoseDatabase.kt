package com.john.rose.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.john.rose.data.local.chapter.Chapter
import com.john.rose.data.local.chapter.ChapterBody
import com.john.rose.data.local.chapter.ChapterBodyDao
import com.john.rose.data.local.chapter.ChapterDao
import com.john.rose.data.local.library.LibraryDao
import com.john.rose.data.local.library.LibraryItem
import com.john.rose.utils.Constants


/**
 * Execute the whole database calls as an atomic operation
 */
interface AppDatabaseOperations {
    suspend fun <T> transaction(block: suspend () -> T): T
}

@Database(
    entities = [
        LibraryItem::class,
        Chapter::class,
        ChapterBody::class],
    version = 6,
    exportSchema = false
) abstract class RoseDatabase : RoomDatabase(), AppDatabaseOperations {
    abstract fun getLibraryDao(): LibraryDao
    abstract fun getChapterDao(): ChapterDao

    abstract fun getChapterBody(): ChapterBodyDao
    override suspend fun <T> transaction(block: suspend () -> T): T = withTransaction(block)
    companion object {
        @Volatile
        private var INSTANCE: RoseDatabase? = null
        fun getInstance(context: Context): RoseDatabase {
            /*
            if the INSTANCE is not null, then return it,
            if it is, then create the database and save
            in instance variable then return it.
            */
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoseDatabase::class.java,
                    Constants.DATABASE_NAME
                ).fallbackToDestructiveMigration().build()

                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}



