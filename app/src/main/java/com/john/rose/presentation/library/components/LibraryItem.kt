package com.john.rose.presentation.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.john.rose.presentation.common.MangaCover

object CommonMangaItemDefaults {
    val GridHorizontalSpacer = 4.dp
    val GridVerticalSpacer = 4.dp

    const val BrowseFavoriteCoverAlpha = 0.34f
}

private val ContinueReadingButtonSize = 28.dp
private val ContinueReadingButtonGridPadding = 6.dp
private val ContinueReadingButtonListSpacing = 8.dp

private const val GridSelectedCoverAlpha = 0.76f

///**
// * Layout of grid list item with title overlaying the cover.
// * Accepts null [title] for a cover-only view.
// */
//@Composable
//fun MangaCompactGridItem(
//    coverData: tachiyomi.domain.manga.model.MangaCover,
//    onClick: () -> Unit,
//    onLongClick: () -> Unit,
//    isSelected: Boolean = false,
//    title: String? = null,
//    onClickContinueReading: (() -> Unit)? = null,
//    coverAlpha: Float = 1f,
//    coverBadgeStart: @Composable (RowScope.() -> Unit)? = null,
//    coverBadgeEnd: @Composable (RowScope.() -> Unit)? = null,
//) {
//    GridItemSelectable(
//        isSelected = isSelected,
//        onClick = onClick,
//        onLongClick = onLongClick,
//    ) {
//        MangaGridCover(
//            cover = {
//                MangaCover.Book(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .alpha(if (isSelected) GridSelectedCoverAlpha else coverAlpha),
//                    data = coverData,
//                )
//            },
//            badgesStart = coverBadgeStart,
//            badgesEnd = coverBadgeEnd,
//            content = {
//                if (title != null) {
//                    CoverTextOverlay(
//                        title = title,
//                        onClickContinueReading = onClickContinueReading,
//                    )
//                } else if (onClickContinueReading != null) {
//                    ContinueReadingButton(
//                        modifier = Modifier
//                            .padding(ContinueReadingButtonGridPadding)
//                            .align(Alignment.BottomEnd),
//                        onClickContinueReading = onClickContinueReading,
//                    )
//                }
//            },
//        )
//    }
//}
//
///**
// * Title overlay for [MangaCompactGridItem]
// */
//@Composable
//private fun BoxScope.CoverTextOverlay(
//    title: String,
//    onClickContinueReading: (() -> Unit)? = null,
//) {
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
//            .background(
//                Brush.verticalGradient(
//                    0f to Color.Transparent,
//                    1f to Color(0xAA000000),
//                ),
//            )
//            .fillMaxHeight(0.33f)
//            .fillMaxWidth()
//            .align(Alignment.BottomCenter),
//    )
//    Row(
//        modifier = Modifier.align(Alignment.BottomStart),
//        verticalAlignment = Alignment.Bottom,
//    ) {
//        GridItemTitle(
//            modifier = Modifier
//                .weight(1f)
//                .padding(8.dp),
//            title = title,
//            style = MaterialTheme.typography.titleSmall.copy(
//                color = Color.White,
//                shadow = Shadow(
//                    color = Color.Black,
//                    blurRadius = 4f,
//                ),
//            ),
//            minLines = 1,
//        )
//        if (onClickContinueReading != null) {
//            ContinueReadingButton(
//                modifier = Modifier.padding(
//                    end = ContinueReadingButtonGridPadding,
//                    bottom = ContinueReadingButtonGridPadding,
//                ),
//                onClickContinueReading = onClickContinueReading,
//            )
//        }
//    }
//}

/**
 * Layout of grid list item with title below the cover.
 */
@Composable
fun ComfortableGridItem(
    coverData: Any,
    title: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isSelected: Boolean = false,
    titleMaxLines: Int = 2,
    coverAlpha: Float = 1f,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    coverBadgeStart: (@Composable RowScope.() -> Unit)? = null,
    coverBadgeEnd: (@Composable RowScope.() -> Unit)? = null,
    onClickContinueReading: (() -> Unit)? = null,
) {
    GridItemSelectable(
        isSelected = isSelected,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        Column {
            GridCover(
                cover = {
                    MangaCover.Book(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(if (isSelected) GridSelectedCoverAlpha else coverAlpha),
                        data = coverData,
                    )
                },
//                badgesStart = coverBadgeStart,
//                badgesEnd = coverBadgeEnd,
                content = {
                    if (onClickContinueReading != null) {
                        ContinueReadingButton(
                            modifier = Modifier
                                .padding(ContinueReadingButtonGridPadding)
                                .align(Alignment.BottomEnd),
                            onClickContinueReading = onClickContinueReading,
                        )
                    }
                },
            )
            GridItemTitle(
                modifier = Modifier.padding(4.dp),
                title = title,
                style = MaterialTheme.typography.titleSmall,
                minLines = 2,
                maxLines = titleMaxLines,
            )
        }
    }
}

/**
 * Common cover layout to add contents to be drawn on top of the cover.
 */
@Composable
private fun GridCover(
    modifier: Modifier = Modifier,
    cover: @Composable BoxScope.() -> Unit = {},
    content: @Composable (BoxScope.() -> Unit)? = null,
//    badgesStart: (@Composable RowScope.() -> Unit)? = null,
//    badgesEnd: (@Composable RowScope.() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(MangaCover.Book.ratio),
    ) {
        cover()
        content?.invoke(this)
//        if (badgesStart != null) {
//            BadgeGroup(
//                modifier = Modifier
//                    .padding(4.dp)
//                    .align(Alignment.TopStart),
//                content = badgesStart,
//            )
//        }
//
//        if (badgesEnd != null) {
//            BadgeGroup(
//                modifier = Modifier
//                    .padding(4.dp)
//                    .align(Alignment.TopEnd),
//                content = badgesEnd,
//            )
//        }
    }
}

@Composable
private fun GridItemTitle(
    title: String,
    style: TextStyle,
    minLines: Int,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
) {
    Text(
        modifier = modifier,
        text = title,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        minLines = minLines,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        style = style,
    )
}

/**
 * Wrapper for grid items to handle selection state, click and long click.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridItemSelectable(
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .selectedOutline(isSelected = isSelected, color = MaterialTheme.colorScheme.secondary)
            .padding(4.dp),
    ) {
        val contentColor = if (isSelected) {
            MaterialTheme.colorScheme.onSecondary
        } else {
            LocalContentColor.current
        }
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

/**
 * @see GridItemSelectable
 */
private fun Modifier.selectedOutline(
    isSelected: Boolean,
    color: Color,
) = this then drawBehind { if (isSelected) drawRect(color = color) }


@Composable
private fun ContinueReadingButton(
    modifier: Modifier = Modifier,
    onClickContinueReading: () -> Unit,
) {
    Box(modifier = modifier) {
        FilledIconButton(
            onClick = onClickContinueReading,
            modifier = Modifier.size(ContinueReadingButtonSize),
            shape = MaterialTheme.shapes.small,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                contentColor = contentColorFor(MaterialTheme.colorScheme.primaryContainer),
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Resume",
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
