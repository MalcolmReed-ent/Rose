package com.john.rose.presentation.reader

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State

class ReaderScreenState(
    val showReaderInfo: MutableState<Boolean>,
    val readerInfo: CurrentInfo,
    val settings: Settings,
    val showInvalidChapterDialog: MutableState<Boolean>
) {
    data class CurrentInfo(
        val bookTitle: State<String>,
        val chapterTitle: State<String>,
        val chapterCurrentNumber: State<Int>,
        val chapterPercentageProgress: State<Float>,
        val chaptersCount: State<Int>,
        val chapterUrl: State<String>
    )

    data class Settings(
        val isTextSelectable: Boolean,
        val keepScreenOn: Boolean,
        val style: StyleSettingsData,
        val selectedSetting: MutableState<Type>,
    ) {
        data class StyleSettingsData(
            val textFont: String,
            val textSize: Float,
        )

        enum class Type {
            None, Style, More
        }
    }
}
