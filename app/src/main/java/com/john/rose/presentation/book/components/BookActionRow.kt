package com.john.rose.presentation.book.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.john.rose.R

@Composable
fun BookActionRow(
    inLibrary: Boolean,
    onAddToLibraryClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val defaultActionButtonColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)

    Row(
        modifier = modifier.padding(start = 14.dp, top = 8.dp)
    ) {
        BookActionButton(
            title = if (inLibrary) {
                stringResource(R.string.in_library)
            } else {
                stringResource(R.string.add_to_library)
            },
            icon = if (inLibrary) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            color = if (inLibrary) MaterialTheme.colorScheme.primary else defaultActionButtonColor,
            onClick = onAddToLibraryClicked
        )
    }
}

@Composable
private fun RowScope.BookActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.weight(1f),
    ) {
        Row(horizontalArrangement = Arrangement.Start) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = title,
                color = color,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}
