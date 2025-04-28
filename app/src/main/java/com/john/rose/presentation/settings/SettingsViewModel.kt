package com.john.rose.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.john.rose.data.manager.ReaderPreferences
import com.john.rose.data.manager.UserPreferences
import com.john.rose.data.manager.SortOrder
import com.john.rose.data.manager.ThemeMode
import com.john.rose.domain.manager.LocalUserManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val localUserManager: LocalUserManager
) : ViewModel() {

    private val _readerPreferences = MutableStateFlow(
        ReaderPreferences(
            fontSize = 18f,
            fontFamily = "serif",
            readerTheme = ThemeMode.SYSTEM,
            appTheme = ThemeMode.SYSTEM
        )
    )
    val readerPreferences = _readerPreferences.asStateFlow()

    private val _userPreferences = MutableStateFlow(
        UserPreferences(
            showUnread = false,
            showBookmarked = false,
            sortOrder = SortOrder.Ascending
        )
    )
    val userPreferences = _userPreferences.asStateFlow()

    init {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            localUserManager.userReaderPreferences().collect { preferences ->
                _readerPreferences.value = preferences
            }
        }
        viewModelScope.launch {
            localUserManager.userBookPreferences().collect { preferences ->
                _userPreferences.value = preferences
            }
        }
    }

    fun updateFontSize(size: Float) {
        viewModelScope.launch {
            try {
                localUserManager.updateFontSize(size)
                _readerPreferences.value = _readerPreferences.value.copy(fontSize = size)
            } catch (e: Exception) {
                // Handle error updating font size
            }
        }
    }

    fun cycleFontFamily() {
        val fonts = listOf("serif", "sans-serif", "monospace")
        val currentIndex = fonts.indexOf(_readerPreferences.value.fontFamily)
        val nextFont = fonts[(currentIndex + 1) % fonts.size]
        
        viewModelScope.launch {
            try {
                localUserManager.updateFontFamily(nextFont)
                _readerPreferences.value = _readerPreferences.value.copy(fontFamily = nextFont)
            } catch (e: Exception) {
                // Handle error updating font family
            }
        }
    }

    fun cycleReaderTheme() {
        viewModelScope.launch {
            try {
                val nextTheme = _readerPreferences.value.readerTheme.next()
                localUserManager.updateReaderTheme(nextTheme)
                _readerPreferences.value = _readerPreferences.value.copy(readerTheme = nextTheme)
            } catch (e: Exception) {
                // Handle error updating reader theme
            }
        }
    }

    fun cycleAppTheme() {
        viewModelScope.launch {
            try {
                val nextTheme = _readerPreferences.value.appTheme.next()
                localUserManager.updateAppTheme(nextTheme)
                _readerPreferences.value = _readerPreferences.value.copy(appTheme = nextTheme)
            } catch (e: Exception) {
                // Handle error updating app theme
            }
        }
    }

    fun toggleShowUnread() {
        viewModelScope.launch {
            try {
                val newValue = !_userPreferences.value.showUnread
                localUserManager.updateUnread(newValue)
                _userPreferences.value = _userPreferences.value.copy(showUnread = newValue)
            } catch (e: Exception) {
                // Handle error toggling unread
            }
        }
    }

    fun toggleShowBookmarked() {
        viewModelScope.launch {
            try {
                val newValue = !_userPreferences.value.showBookmarked
                localUserManager.updateBookmarkFilter(newValue)
                _userPreferences.value = _userPreferences.value.copy(showBookmarked = newValue)
            } catch (e: Exception) {
                // Handle error toggling bookmarked
            }
        }
    }

    fun toggleSortOrder() {
        viewModelScope.launch {
            try {
                val newOrder = _userPreferences.value.sortOrder.next()
                localUserManager.updateSort(newOrder)
                _userPreferences.value = _userPreferences.value.copy(sortOrder = newOrder)
            } catch (e: Exception) {
                // Handle error toggling sort order
            }
        }
    }
}
