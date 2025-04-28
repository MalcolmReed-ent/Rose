package com.john.rose.presentation.reader

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.john.rose.data.manager.ReaderPreferences
import com.john.rose.domain.manager.LocalUserManager
import com.john.rose.presentation.BaseViewModel
import com.john.rose.presentation.reader.manager.ReaderManager
import com.john.rose.presentation.reader.manager.ReaderManagerViewCallReferences
import com.john.rose.presentation.utils.StateExtraString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.properties.Delegates

interface ReaderStateBundle {
    var bookUrl: String
    var chapterUrl: String
}

@HiltViewModel
class ReaderViewModel @Inject constructor(
    stateHandler: SavedStateHandle,
    val localUserManager: LocalUserManager,
    private val readerManager: ReaderManager,
) : BaseViewModel(),
    ReaderStateBundle,
    ReaderManagerViewCallReferences by readerManager {

    override var bookUrl by StateExtraString(stateHandler)
    override var chapterUrl by StateExtraString(stateHandler)
    
    private val readerSession = readerManager.initiateOrGetSession(
        bookUrl = bookUrl,
        chapterUrl = chapterUrl
    )

    private val readingPosStats = readerSession.readingStats

    private val _readerPreferences = MutableStateFlow(
        ReaderPreferences(
            fontSize = 18f,
            fontFamily = "serif",
        )
    )
    val readerPreferences: MutableStateFlow<ReaderPreferences> = _readerPreferences

    val state = ReaderScreenState(
        showReaderInfo = mutableStateOf(false),
        readerInfo = ReaderScreenState.CurrentInfo(
            bookTitle = derivedStateOf { readerSession.bookTitle!! },
            chapterTitle = derivedStateOf {
                readingPosStats.value?.chapterTitle ?: ""
            },
            chapterCurrentNumber = derivedStateOf {
                readingPosStats.value?.run { chapterIndex + 1 } ?: 0
            },
            chapterPercentageProgress = readerSession.readingChapterProgressPercentage,
            chaptersCount = derivedStateOf { readingPosStats.value?.chapterCount ?: 0 },
            chapterUrl = derivedStateOf { readingPosStats.value?.chapterUrl ?: "" }
        ),
        settings = ReaderScreenState.Settings(
            selectedSetting = mutableStateOf(ReaderScreenState.Settings.Type.None),
            isTextSelectable = true,
            keepScreenOn = true,
            style = ReaderScreenState.Settings.StyleSettingsData(
                textFont = readerPreferences.value.fontFamily,
                textSize = readerPreferences.value.fontSize,
            )
        ),
        showInvalidChapterDialog = mutableStateOf(false)
    )

    init {
        showInvalidChapterDialog = {
            withContext(Dispatchers.Main) {
                state.showInvalidChapterDialog.value = true
            }
        }

        viewModelScope.launch {
            localUserManager.userReaderPreferences().flowOn(Dispatchers.IO).collect {
                _readerPreferences.value = it
            }
        }
    }

    val items = readerSession.items
    val chaptersLoader = readerSession.readerChaptersLoader
    var readingCurrentChapter by Delegates.observable(readerSession.currentChapter) { _, _, new ->
        readerSession.currentChapter = new
    }

    fun onScreenTapped() {
        state.showReaderInfo.value = !state.showReaderInfo.value
    }

    fun onCloseManually() {
        readerManager.close()
    }

    fun onViewDestroyed() {
        readerManager.invalidateViewsHandlers()
    }

    fun reloadReader() {
        val currentChapter = readingCurrentChapter.copy()
        readerSession.reloadReader()
        chaptersLoader.tryLoadRestartedInitial(currentChapter)
    }

    fun updateInfoViewTo(itemIndex: Int) =
        readerSession.updateInfoViewTo(itemIndex = itemIndex)

    fun markChapterStartAsSeen(chapterUrl: String) =
        readerSession.markChapterStartAsSeen(chapterUrl = chapterUrl)

    fun markChapterEndAsSeen(chapterUrl: String) =
        readerSession.markChapterEndAsSeen(chapterUrl = chapterUrl)
}
