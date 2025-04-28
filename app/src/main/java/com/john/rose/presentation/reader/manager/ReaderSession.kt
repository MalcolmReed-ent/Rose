package com.john.rose.presentation.reader.manager

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import com.john.rose.data.local.chapter.Chapter
import com.john.rose.data.manager.ReaderPreferences
import com.john.rose.di.AppCoroutineScope
import com.john.rose.domain.manager.LocalUserManager
import com.john.rose.presentation.reader.ChapterState
import com.john.rose.presentation.reader.ReaderChaptersLoader
import com.john.rose.presentation.reader.ReaderState
import com.john.rose.presentation.reader.ReadingChapterPosStats
import com.john.rose.presentation.reader.chapterReadPercentage
import com.john.rose.presentation.reader.components.ChaptersIsReadRoutine
import com.john.rose.presentation.reader.components.InitialPositionChapter
import com.john.rose.repository.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.properties.Delegates

class ReaderSession @Inject constructor(
    val bookUrl: String,
    initialChapterUrl: String,
    private val scope: CoroutineScope,
    private val appScope: AppCoroutineScope,
    private val appRepository: AppRepository,
    private val context: Context,
    val forceUpdateListViewState: suspend () -> Unit,
    val maintainLastVisiblePosition: suspend (suspend () -> Unit) -> Unit,
    val maintainStartPosition: suspend (suspend () -> Unit) -> Unit,
    val setInitialPosition: suspend (InitialPositionChapter) -> Unit,
    val showInvalidChapterDialog: suspend () -> Unit,
    private val localUserManager: LocalUserManager,
) {
    private var chapterUrl: String = initialChapterUrl
    private val readRoutine = ChaptersIsReadRoutine(appRepository)
    private val orderedChapters = mutableListOf<Chapter>()

    var bookTitle: String? = null
    var bookCoverUrl: String? = null

    var currentChapter: ChapterState by Delegates.observable(
        ChapterState(
            chapterUrl = chapterUrl,
            chapterItemPosition = 0,
            offset = 0
        )
    ) { _, old, new ->
        chapterUrl = new.chapterUrl
        if (old.chapterUrl != new.chapterUrl) {
            saveLastReadPositionState(new, old)
        }
    }

    private val readerPreferences: MutableStateFlow<ReaderPreferences> = MutableStateFlow(
        ReaderPreferences(
            fontSize = 18f,
            fontFamily = "serif"
        )
    )

    init {
        scope.launch {
            localUserManager.userReaderPreferences()
                .flowOn(Dispatchers.IO)
                .collectLatest { preferences ->
                    readerPreferences.value = preferences
                }
        }
    }

    val readingStats = mutableStateOf<ReadingChapterPosStats?>(null)
    val readingChapterProgressPercentage = derivedStateOf {
        readingStats.value?.chapterReadPercentage() ?: 0f
    }

    val readerChaptersLoader = ReaderChaptersLoader(
        appRepository = appRepository,
        bookUrl = bookUrl,
        orderedChapters = orderedChapters,
        readerState = ReaderState.INITIAL_LOAD,
        forceUpdateListViewState = forceUpdateListViewState,
        maintainLastVisiblePosition = maintainLastVisiblePosition,
        maintainStartPosition = maintainStartPosition,
        setInitialPosition = setInitialPosition,
        showInvalidChapterDialog = showInvalidChapterDialog,
    )

    val items = readerChaptersLoader.getItems()

    fun init() {
        initLoadData()
        scope.launch {
            appRepository.libraryBooks.updateLastReadEpochTimeMilli(
                bookUrl,
                System.currentTimeMillis()
            )
        }
    }

    private fun initLoadData() {
        scope.launch {
            val book = async(Dispatchers.IO) { appRepository.libraryBooks.get(bookUrl) }
            val chapter = async(Dispatchers.IO) { appRepository.bookChapters.get(chapterUrl) }
            val chaptersList = async(Dispatchers.Default) {
                orderedChapters.also { it.addAll(appRepository.bookChapters.chapters(bookUrl)) }
            }
            val chapterIndex = async(Dispatchers.Default) {
                chaptersList.await().indexOfFirst { it.url == chapterUrl }
            }
            chaptersList.await()
            bookCoverUrl = book.await()?.coverImageUrl
            bookTitle = book.await()?.title
            currentChapter = ChapterState(
                chapterUrl = chapterUrl,
                chapterItemPosition = chapter.await()?.lastReadPosition ?: 0,
                offset = chapter.await()?.lastReadOffset ?: 0,
            )
            readerChaptersLoader.tryLoadInitial(chapterIndex = chapterIndex.await())
        }
    }

    fun close() {
        readerChaptersLoader.coroutineContext.cancelChildren()
        saveLastReadPositionState(currentChapter)
        scope.coroutineContext.cancelChildren()
    }

    fun reloadReader() {
        readerChaptersLoader.reload()
    }

    fun updateInfoViewTo(itemIndex: Int) {
        val stats = readerChaptersLoader.getItemContext(
            itemIndex = itemIndex,
            chapterUrl = chapterUrl
        ) ?: return
        readingStats.value = stats
    }

    fun markChapterStartAsSeen(chapterUrl: String) {
        readRoutine.setReadStart(chapterUrl = chapterUrl)
    }

    fun markChapterEndAsSeen(chapterUrl: String) {
        readRoutine.setReadEnd(chapterUrl = chapterUrl)
    }

    private fun saveLastReadPositionState(
        newChapter: ChapterState,
        oldChapter: ChapterState? = null,
    ) {
        saveBookLastReadPositionState(
            bookUrl = bookUrl,
            newChapter = newChapter,
            oldChapter = oldChapter,
            scope = appScope,
            appRepository = appRepository,
        )
    }
}

private fun saveBookLastReadPositionState(
    bookUrl: String,
    newChapter: ChapterState,
    oldChapter: ChapterState? = null,
    scope: AppCoroutineScope,
    appRepository: AppRepository,
) {
    scope.launch(Dispatchers.IO) {
        appRepository.withTransaction {
            appRepository.libraryBooks.updateLastReadChapter(
                bookUrl = bookUrl,
                lastReadChapterUrl = newChapter.chapterUrl
            )

            if (oldChapter?.chapterUrl != null) appRepository.bookChapters.updatePosition(
                chapterUrl = oldChapter.chapterUrl,
                lastReadPosition = oldChapter.chapterItemPosition,
                lastReadOffset = oldChapter.offset
            )

            appRepository.bookChapters.updatePosition(
                chapterUrl = newChapter.chapterUrl,
                lastReadPosition = newChapter.chapterItemPosition,
                lastReadOffset = newChapter.offset
            )
        }
    }
}
