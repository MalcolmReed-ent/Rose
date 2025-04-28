package com.john.rose.presentation.reader

import com.john.rose.epub.BookTextMapper

sealed interface ReaderItem {
    /**
     * Corresponds to index in the ordered chapter list.
     * Unique by chapter row
     */
    val chapterIndex: Int

    sealed interface Chapter : ReaderItem {
        val chapterUrl: String
    }

    sealed interface Position : ReaderItem, Chapter {
        /**
         * Index for the items of each [chapterIndex].
         * Unique by chapter
         */
        val chapterItemPosition: Int
    }

    enum class Location { FIRST, MIDDLE, LAST }
    sealed interface ParagraphLocation : ReaderItem {
        val location: Location
    }

    sealed interface Text : ReaderItem, Position {
        val text: String
        val textToDisplay get() = text
    }

    data class Title(
        override val chapterUrl: String,
        override val chapterIndex: Int,
        override val chapterItemPosition: Int,
        override val text: String
    ) : ReaderItem, Text, Position

    data class Body(
        override val chapterUrl: String,
        override val chapterIndex: Int,
        override val chapterItemPosition: Int,
        override val text: String,
        override val location: Location
    ) : ReaderItem, Text, Position, ParagraphLocation

    data class Image(
        override val chapterUrl: String,
        override val chapterIndex: Int,
        override val chapterItemPosition: Int,
        override val location: Location,
        val text: String,
        val image: BookTextMapper.ImgEntry
    ) : ReaderItem, Position, ParagraphLocation

    data class Progressbar(override val chapterIndex: Int) : ReaderItem
    data class Divider(override val chapterIndex: Int) : ReaderItem
    data class BookEnd(override val chapterIndex: Int) : ReaderItem
    data class BookStart(override val chapterIndex: Int) : ReaderItem
    data class Error(override val chapterIndex: Int, val text: String) : ReaderItem
    data class Padding(override val chapterIndex: Int) : ReaderItem
}
