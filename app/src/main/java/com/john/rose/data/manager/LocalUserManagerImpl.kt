package com.john.rose.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.john.rose.domain.manager.LocalUserManager
import com.john.rose.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "libraryPreferences")

enum class ThemeMode {
    LIGHT, DARK, SYSTEM;
    
    fun next(): ThemeMode = when (this) {
        LIGHT -> DARK
        DARK -> SYSTEM
        SYSTEM -> LIGHT
    }
}

enum class SortOrder {
    Descending,
    Ascending;
    fun next() = when (this) {
        Descending -> Ascending
        Ascending -> Descending
    }
}

data class UserPreferences(
    val showUnread: Boolean,
    val showBookmarked: Boolean,
    val sortOrder: SortOrder,
)

data class ReaderPreferences(
    val fontSize: Float,
    val fontFamily: String,
    val readerTheme: ThemeMode = ThemeMode.SYSTEM,
    val appTheme: ThemeMode = ThemeMode.SYSTEM
)

class LocalUserManagerImpl(
    private val context: Context
): LocalUserManager {

    private object PreferencesKeys {
        val APP_ENTRY = booleanPreferencesKey(Constants.APP_ENTRY)
    }

    private object LibraryPreferencesKeys {
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val SHOW_UNREAD = booleanPreferencesKey("show_unread")
        val SHOW_BOOKMARKED = booleanPreferencesKey("show_bookmarked")
    }

    private object ReaderPreferenceKeys {
        val FONT_SIZE = stringPreferencesKey("font_size")
        val FONT_FAMILY = stringPreferencesKey("font_family")
        val READER_THEME = stringPreferencesKey("reader_theme")
        val APP_THEME = stringPreferencesKey("app_theme")
    }

    private val datastore = context.dataStore

    override suspend fun saveAppEntry() {
        datastore.edit { settings ->
            settings[PreferencesKeys.APP_ENTRY] = true
        }
    }

    override fun readAppEntry(): Flow<Boolean> {
        return datastore.data.map { preferences ->
            preferences[PreferencesKeys.APP_ENTRY] ?: false
        }
    }

    override fun userBookPreferences(): Flow<UserPreferences> {
        return datastore.data.catch {
            emit(emptyPreferences())
        }
        .map { preferences->
            val showBookmarked = preferences[LibraryPreferencesKeys.SHOW_BOOKMARKED] ?: false
            val showUnread = preferences[LibraryPreferencesKeys.SHOW_UNREAD] ?: false
            val sortOrder = SortOrder.valueOf(
                preferences[LibraryPreferencesKeys.SORT_ORDER] ?: SortOrder.Ascending.name
            )
            UserPreferences(showUnread, showBookmarked, sortOrder)
        }
    }

    override fun userReaderPreferences(): Flow<ReaderPreferences> {
        return datastore.data.catch {
            emit(emptyPreferences())
        }
        .map { preferences->
            val fontSize = preferences[ReaderPreferenceKeys.FONT_SIZE] ?: "18f"
            val fontFamily = preferences[ReaderPreferenceKeys.FONT_FAMILY] ?: "serif"
            val readerTheme = ThemeMode.valueOf(
                preferences[ReaderPreferenceKeys.READER_THEME] ?: ThemeMode.SYSTEM.name
            )
            val appTheme = ThemeMode.valueOf(
                preferences[ReaderPreferenceKeys.APP_THEME] ?: ThemeMode.SYSTEM.name
            )
            ReaderPreferences(
                fontSize = fontSize.removeSuffix("f").toFloat(),
                fontFamily = fontFamily,
                readerTheme = readerTheme,
                appTheme = appTheme
            )
        }
    }

    override suspend fun updateSort(sortOrder: SortOrder) {
        datastore.edit { preferences->
            preferences[LibraryPreferencesKeys.SORT_ORDER] = sortOrder.name
        }
    }

    override suspend fun updateUnread(showUnread: Boolean) {
        datastore.edit { preferences->
            preferences[LibraryPreferencesKeys.SHOW_UNREAD] = showUnread
        }
    }

    override suspend fun updateBookmarkFilter(showBookmarked: Boolean) {
        datastore.edit { preferences->
            preferences[LibraryPreferencesKeys.SHOW_BOOKMARKED] = showBookmarked
        }
    }

    override suspend fun updateFontSize(size: Float) {
        datastore.edit { preferences ->
            preferences[ReaderPreferenceKeys.FONT_SIZE] = "${size}f"
        }
    }

    override suspend fun updateFontFamily(family: String) {
        datastore.edit { preferences ->
            preferences[ReaderPreferenceKeys.FONT_FAMILY] = family
        }
    }

    override suspend fun updateReaderTheme(theme: ThemeMode) {
        datastore.edit { preferences ->
            preferences[ReaderPreferenceKeys.READER_THEME] = theme.name
        }
    }

    override suspend fun updateAppTheme(theme: ThemeMode) {
        datastore.edit { preferences ->
            preferences[ReaderPreferenceKeys.APP_THEME] = theme.name
        }
    }
}
