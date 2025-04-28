package com.john.rose.presentation.reader

import android.app.Activity
import android.util.Log
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    state: ReaderScreenState,
    onKeepScreenOn: Boolean,
    onTextFontChanged: (String) -> Unit,
    onTextSizeChanged: (Float) -> Unit,
    onPressBack: () -> Unit,
    readerContent: @Composable (paddingValues: PaddingValues) -> Unit,
) {
    val view = LocalView.current
    val activity = LocalContext.current as Activity
    
    // Handle system UI visibility
    LaunchedEffect(state.showReaderInfo.value) {
        if (state.showReaderInfo.value) {
            // Show system bars
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        } else {
            // Hide system bars
            @Suppress("DEPRECATION")
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }
    }

    // Clean up when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            // Restore system UI visibility
            view.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    // Back handler will now always exit the reader
    BackHandler {
        onPressBack()
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = state.showReaderInfo.value,
                enter = expandVertically(initialHeight = { 0 }, expandFrom = Alignment.Top)
                        + fadeIn(),
                exit = shrinkVertically(targetHeight = { 0 }, shrinkTowards = Alignment.Top)
                        + fadeOut(),
            ) {
                Surface {
                    Log.d("screen", "${state.readerInfo.chapterCurrentNumber.value}")
                    Column {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            title = {
                                Column(
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = state.readerInfo.bookTitle.value,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.animateContentSize()
                                    )
                                    Text(
                                        text = state.readerInfo.chapterTitle.value,
                                        style = MaterialTheme.typography.titleSmall,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.animateContentSize()
                                    )
                                }
                            },
                            navigationIcon = {
                                IconButton(onClick = onPressBack) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                                }
                            },
                            actions = {
                            }
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null // No ripple effect
                    ) {
                        state.showReaderInfo.value = !state.showReaderInfo.value
                    }
            ) {
                readerContent(paddingValues)
            }
        }
    )
}
