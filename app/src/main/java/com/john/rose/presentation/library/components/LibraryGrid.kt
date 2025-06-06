package com.john.rose.presentation.library.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.john.rose.data.BookWithContext
import com.john.rose.repository.rememberResolvedBookImagePath

@Composable
fun LibraryComfortableGrid(
    rootNavController: NavController,
    list: List<BookWithContext>,
    contentPadding: PaddingValues,
    onClick: (BookWithContext) -> Unit,
    onLongClick: (BookWithContext) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(160.dp), //Orig was 105
        contentPadding = contentPadding
    ) {
        items(
            items = list,
            key = { it.book.url }
        ){
            val interactionSource = remember { MutableInteractionSource() }
            ComfortableGridItem(
                coverData = rememberResolvedBookImagePath(
                    bookUrl = it.book.url,
                    imagePath = it.book.coverImageUrl),
                title = it.book.title,
                onClick = {
                    rootNavController.navigate("book?bookUrl=${it.book.url}&bookTitle=${it.book.title}&libraryId=${it.book.libraryid}")
                },
                onLongClick = { /*TODO*/ })
        }
    }
}
