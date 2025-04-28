package com.john.rose.domain.manager

import com.john.rose.data.manager.ReaderPreferences
import com.john.rose.data.manager.SortOrder
import com.john.rose.data.manager.ThemeMode
import com.john.rose.data.manager.UserPreferences
import kotlinx.coroutines.flow.Flow

interface LocalUserManager {
    suspend fun saveAppEntry()
    fun readAppEntry(): Flow<Boolean>

    fun userBookPreferences(): Flow<UserPreferences>
    fun userReaderPreferences(): Flow<ReaderPreferences>
    
    // Book preferences
    suspend fun updateSort(sortOrder: SortOrder)
    suspend fun updateUnread(showUnread: Boolean)
    suspend fun updateBookmarkFilter(showBookmarked: Boolean)
    
    // Reader preferences
    suspend fun updateFontSize(size: Float)
    suspend fun updateFontFamily(family: String)
    suspend fun updateReaderTheme(theme: ThemeMode)
    suspend fun updateAppTheme(theme: ThemeMode)
}
