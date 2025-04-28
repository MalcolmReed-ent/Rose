package com.john.rose.repository

import android.content.Context
import androidx.core.net.toUri
import com.john.rose.data.Response
import com.john.rose.data.local.RoseDatabase
import com.john.rose.data.local.chapter.Chapter
import com.john.rose.data.local.library.LibraryItem
import com.john.rose.epub.epubParser
import com.john.rose.isContentUri
import com.john.rose.utils.tryAsResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import epubImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val db: RoseDatabase,
    @ApplicationContext private val context: Context,
    val name: String,
    val libraryBooks: LibraryBookRepository,
    val bookChapters: BookChaptersRepository,
    val chapterBody: ChapterBodyRepository,
    private val appFileResolver: AppFileResolver,
) {

    suspend fun libraryUpdate(bookUrl: String, bookTitle: String): Boolean {
        val realUrl = appFileResolver.getLocalIfContentType(bookUrl, bookFolderName = bookTitle)
        return if (bookUrl.isContentUri && libraryBooks.get(realUrl) == null) {
            importEpubFromContentUri(
                contentUri = bookUrl,
                bookTitle = bookTitle,
                addToLibrary = true
            ) is Response.Success
        } else {
            libraryBooks.toggleBookmark(bookUrl = realUrl, bookTitle = bookTitle)
        }
    }

    suspend fun importEpubFromContentUri(
        contentUri: String,
        bookTitle: String,
        addToLibrary: Boolean = false
    ) = tryAsResponse {
        val inputStream = context.contentResolver.openInputStream(contentUri.toUri())
            ?: return@tryAsResponse
        val epub = inputStream.use { epubParser(inputStream = inputStream) }
        epubImporter(
            storageFolderName =  bookTitle,
            appFileResolver = appFileResolver,
            appRepository = this,
            epub = epub,
            addToLibrary = addToLibrary
        )
    }

    suspend fun getDatabaseSizeBytes() = withContext(Dispatchers.IO) {
        context.getDatabasePath(name).length()
    }

    fun close() = db.close()
    fun delete() = context.deleteDatabase(name)
    fun clearAllTables() = db.clearAllTables()
    suspend fun vacuum() =
        withContext(Dispatchers.IO) { db.openHelper.writableDatabase.execSQL("VACUUM") }

    suspend fun <T> withTransaction(fn: suspend () -> T) = db.transaction(fn)

    inner class Settings {
        suspend fun clearNonLibraryData() = withContext(Dispatchers.IO)
        {
            db.getLibraryDao().removeAllNonLibraryRows()
            db.getChapterDao().removeAllNonLibraryRows()
            db.getChapterBody().removeAllNonChapterRows()
        }

        /**
         * Folder where additional book data like images is stored.
         * Each subfolder must be an unique folder for each book.
         * Each book folder can have an arbitrary structure internally.
         */
        val folderBooks = appFileResolver.folderBooks
    }
}

fun isValid(book: LibraryItem): Boolean = book.url.matches("""^(https?|local)://.*""".toRegex())
fun isValid(chapter: Chapter): Boolean =
    chapter.url.matches("""^(https?|local)://.*""".toRegex())
