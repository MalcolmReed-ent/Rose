package com.john.rose.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.john.rose.presentation.book.components.TriStateItem

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val readerPreferences by viewModel.readerPreferences.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Reading Preferences",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Font size") },
                supportingContent = { Text("${readerPreferences.fontSize.toInt()}sp") }, // Display as integer
                trailingContent = {
                    Slider(
                        value = readerPreferences.fontSize,
                        onValueChange = { viewModel.updateFontSize(it) },
                        valueRange = 12f..24f,
                        steps = 12, // (24 - 12) / 1 = 12 steps
                        modifier = Modifier.width(150.dp)
                    )
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Font family") },
                supportingContent = { Text(readerPreferences.fontFamily) },
                trailingContent = {
                    TextButton(onClick = { viewModel.cycleFontFamily() }) {
                        Text("Change")
                    }
                }
            )
        }

        // Divider after Reading Preferences
        item {
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }

        item {
            Text(
                text = "Theme Settings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("App Theme") },
                supportingContent = { Text(readerPreferences.appTheme.name) },
                trailingContent = {
                    TextButton(onClick = { viewModel.cycleAppTheme() }) {
                        Text("Change")
                    }
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Reader Theme") },
                supportingContent = { Text(readerPreferences.readerTheme.name) },
                trailingContent = {
                    TextButton(onClick = { viewModel.cycleReaderTheme() }) {
                        Text("Change")
                    }
                }
            )
        }

        // Divider after Theme Settings
        item {
            Divider(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }

        item {
            Text(
                text = "Library Preferences",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Sort Chapter List Order") },
                supportingContent = { Text(userPreferences.sortOrder.name) },
                trailingContent = {
                    TextButton(onClick = { viewModel.toggleSortOrder() }) {
                        Text("Change")
                    }
                }
            )
        }
    }
}
