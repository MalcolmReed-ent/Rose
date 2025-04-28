package com.john.rose.di

import android.app.Application
import android.content.Context
import com.john.rose.AppPreferences
import com.john.rose.data.local.AppDatabaseOperations
import com.john.rose.data.local.RoseDatabase
import com.john.rose.data.local.chapter.ChapterBodyDao
import com.john.rose.data.local.chapter.ChapterDao
import com.john.rose.data.local.library.LibraryDao
import com.john.rose.data.manager.LocalUserManagerImpl
import com.john.rose.domain.manager.LocalUserManager
import com.john.rose.domain.usecases.AppEntryUseCases
import com.john.rose.domain.usecases.ReadAppEntry
import com.john.rose.domain.usecases.SaveAppEntry
import com.john.rose.presentation.reader.manager.ReaderManager
import com.john.rose.repository.AppFileResolver
import com.john.rose.repository.AppRepository
import com.john.rose.repository.BookChaptersRepository
import com.john.rose.repository.ChapterBodyRepository
import com.john.rose.repository.LibraryBookRepository
import com.john.rose.utils.NotificationsCenter

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    val mainDatabaseName = "bookEntry"

    @Provides
    fun provideAppContext(@ApplicationContext context: Context) = context

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }

    @Provides
    @Singleton
    fun provideRepository(
        database: RoseDatabase,
        @ApplicationContext context: Context,
        libraryBooksRepository: LibraryBookRepository,
        bookChaptersRepository: BookChaptersRepository,
        chapterBodyRepository: ChapterBodyRepository,
        appFileResolver: AppFileResolver,
    ): AppRepository {
        return AppRepository(
            database,
            context,
            mainDatabaseName,
            libraryBooksRepository,
            bookChaptersRepository,
            chapterBodyRepository,
            appFileResolver
        )
    }

    @Singleton
    @Provides
    fun provideRoseDatabase(@ApplicationContext context: Context) =
        RoseDatabase.getInstance(context)

    @Provides
    fun provideLibraryDao(roseDatabase: RoseDatabase) = roseDatabase.getLibraryDao()


    @Provides
    @Singleton
    fun provideLocalUserManager(
        application: Application
    ): LocalUserManager = LocalUserManagerImpl(context = application)

    @Provides
    @Singleton
    fun provideAppEntryUseCases(
        localUserManager: LocalUserManager
    ): AppEntryUseCases = AppEntryUseCases(
        readAppEntry = ReadAppEntry(localUserManager),
        saveAppEntry = SaveAppEntry(localUserManager)
    )

    @Provides
    @Singleton
    fun provideChapterDao(database: RoseDatabase): ChapterDao = database.getChapterDao()

    @Provides
    @Singleton
    fun provideChapterBodyDao(database: RoseDatabase): ChapterBodyDao = database.getChapterBody()

    @Provides
    @Singleton
    fun provideLibraryBooksRepository(
        libraryDao: LibraryDao,
        operations: AppDatabaseOperations,
        @ApplicationContext context: Context,
        appFileResolver: AppFileResolver,
        appCoroutineScope: AppCoroutineScope,
    ): LibraryBookRepository = LibraryBookRepository(
        libraryDao, operations, context, appFileResolver, appCoroutineScope,
    )

    @Provides
    @Singleton
    fun provideAppCoroutineScope(): AppCoroutineScope {
        return object : AppCoroutineScope {
            override val coroutineContext =
                SupervisorJob() + Dispatchers.Main.immediate + CoroutineName("App")
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabaseOperations(database: RoseDatabase): AppDatabaseOperations {
        return database
    }

    @Provides
    @Singleton
    fun provideAppFileResolver(@ApplicationContext context: Context): AppFileResolver {
        return AppFileResolver(context = context)
    }

    @Provides
    @Singleton
    fun provideChapterBooksRepository(
        chapterDao: ChapterDao,
        databaseOperations: AppDatabaseOperations
    ): BookChaptersRepository {
        return BookChaptersRepository(chapterDao, databaseOperations)
    }

    @Provides
    @Singleton
    fun provideChapterBodyRepository(
        database: RoseDatabase,
        bookChaptersRepository: BookChaptersRepository,
    ): ChapterBodyRepository {
        return ChapterBodyRepository(
            chapterBodyDao = database.getChapterBody(),
            bookChaptersRepository = bookChaptersRepository,
            operations = database
        )
    }

    @Provides
    @Singleton
    fun provideNotificationCenter(
        @ApplicationContext context: Context,
    ): NotificationsCenter {
        return NotificationsCenter(context)
    }

    @Provides
    @Singleton
    fun provideReaderManager(
        appRepository: AppRepository,
        appCoroutineScope: AppCoroutineScope,
        @ApplicationContext context: Context,
        localUserManager: LocalUserManager
    ): ReaderManager {
        return ReaderManager(
            appRepository,
            context,
            appScope = appCoroutineScope,
            localUserManager = localUserManager
        )
    }
}