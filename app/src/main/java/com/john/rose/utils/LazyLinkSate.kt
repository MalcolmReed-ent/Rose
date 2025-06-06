package com.john.rose.utils

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun LazyListState.isScrollingUp(): Boolean {

    var previousIndex by remember { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {

                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset

            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun LazyListState.isScrolledToEnd(): Boolean {
    return remember {
        derivedStateOf {
            val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
            lastItem == null || lastItem.size + lastItem.offset <= layoutInfo.viewportEndOffset
        }
    }.value
}