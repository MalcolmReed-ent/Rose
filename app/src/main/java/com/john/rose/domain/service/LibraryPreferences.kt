package com.john.rose.domain.service

import com.john.rose.domain.PreferenceStore
import com.john.rose.domain.getEnum

class LibraryPreferences(
    private val preferenceStore: PreferenceStore
) {

    fun swipeToStartAction() = preferenceStore.getEnum(
        "pref_chapter_swipe_end_action",
        ChapterSwipeAction.ToggleBookmark,
    )

    fun swipeToEndAction() = preferenceStore.getEnum(
        "pref_chapter_swipe_start_action",
        ChapterSwipeAction.ToggleRead,
    )
    enum class ChapterSwipeAction {
        ToggleRead,
        ToggleBookmark,
        Disabled,
    }
}

