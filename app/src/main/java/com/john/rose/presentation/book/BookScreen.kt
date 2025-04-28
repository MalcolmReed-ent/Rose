package com.john.rose.presentation.book

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.john.rose.R
import com.john.rose.presentation.book.components.BookActionRow
import com.john.rose.presentation.book.components.BookInfoHeader
import com.john.rose.presentation.book.components.ChapterHeader
import com.john.rose.presentation.book.components.ChapterListItem
import com.john.rose.presentation.book.components.ExpandableMangaDescription
import com.john.rose.presentation.common.VerticalFastScroller
import com.john.rose.presentation.common.screens.EmptyScreen
import com.john.rose.presentation.reader.ReaderActivity
import com.john.rose.utils.isScrolledToEnd
import com.john.rose.utils.isScrollingUp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookScreen(
    rawBookUrl: String,
    bookTitle: String,
    libraryId: String,
    rootNavController: NavController,
) {
    val context = LocalContext.current
    val viewModel: BookViewModel = hiltViewModel()
    val state = viewModel.state
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val lazyListState = rememberLazyListState()

    val onCloseSelectionBar = viewModel::libraryUpdate

    if (state.isInSelectionMode.value) BackHandler {
        onCloseSelectionBar()
    }

    val userPreferences by viewModel.userPreferences.collectAsState()

    val topAppBarElementColor = if (scrollBehavior.state.overlappedFraction > 0) {
        MaterialTheme.colorScheme.onBackground
    } else {
        Color.Transparent
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .fillMaxHeight(),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                title = {
                    Text(
                        text = state.book.value.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = topAppBarElementColor
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { rootNavController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            val isFABVisible = remember(state.chapters.fastAny { !it.chapter.read }) {
                state.chapters.fastAny { !it.chapter.read }
            }
            AnimatedVisibility(
                visible = isFABVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        val isReading = remember(state.chapters) {
                            state.chapters.fastAny { it.chapter.read }
                        }
                        Text(
                            text = stringResource(
                                if (isReading) R.string.action_resume else R.string.action_start,
                            ),
                        )
                    },
                    icon = { Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = null) },
                    onClick = { 
                        coroutineScope.launch {
                            viewModel.onOpenLastActiveChapter(context = context)
                        }
                    },
                    expanded = lazyListState.isScrollingUp() || lazyListState.isScrolledToEnd(),
                )
            }
        }
    ) { contentPadding ->
        val topPadding = contentPadding.calculateTopPadding()
        val layoutDirection = LocalLayoutDirection.current

        VerticalFastScroller(
            listState = lazyListState,
            topContentPadding = topPadding,
            endContentPadding = contentPadding.calculateEndPadding(layoutDirection)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding()
                )
            ) {
                item(key = "header", contentType = { 0 }) {
                    BookInfoHeader(
                        state.book.value,
                        paddingValues = contentPadding,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                item(key = "actionRow", contentType = { 1 }) {
                        BookActionRow(
                        inLibrary = state.book.value.inLibrary,
                        onAddToLibraryClicked = viewModel::libraryUpdate,
                        modifier = Modifier
                    )
                }
                
                item(key = "description", contentType = { 2 }) {
                    ExpandableMangaDescription(
                        defaultExpandState = false,
                        description = state.book.value.description,
                        tagsProvider = { null },
                        onTagSearch = { },
                        onCopyTagToClipboard = { },
                    )
                    Spacer(modifier = Modifier.size(6.dp))
                }

                item(key = "chapterHeader", contentType = { 3 }) {
                    ChapterHeader(
                        enabled = true,
                        chapterCount = state.chapters.size,
                        onClick = { }
                    )
                }

                items(
                    items = state.chapters,
                    key = { "_" + it.chapter.url },
                    contentType = { 4 }
                ) { chapterWithContext ->
                    ChapterListItem(
                        chapterWithContext = chapterWithContext,
                        selected = false,
                        onClick = {
                            coroutineScope.launch {
                                val intent = Intent(context, ReaderActivity::class.java).apply {
                                    putExtra("bookUrl", state.book.value.url)
                                    putExtra("chapterUrl", chapterWithContext.chapter.url)
                                }
                                context.startActivity(intent)
                            }
                        },
                        onLongClick = { },
                        bookmark = false,
                        readProgress = null,
                    )
                }

                if (state.error.value.isNotBlank()) {
                    item(key = "error", contentType = { 2 }) {
                        EmptyScreen(message = state.error.value)
                    }
                }
            }
        }
    }
}
