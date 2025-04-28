@file:OptIn(ExperimentalAnimationGraphicsApi::class)

package com.john.rose.presentation.common.material

import android.content.res.Configuration
import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.john.rose.R
import com.john.rose.presentation.theme.RoseTheme

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun NavBar(
    items: List<NavigationItem>,
    selectedItem: Int,
    onItemClick: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.contentColorFor(NavigationBarDefaults.containerColor),
        windowInsets = NavigationBarDefaults.windowInsets,
        tonalElevation = NavigationBarDefaults.Elevation,
    ){
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedItem,
                onClick = { onItemClick(index) },
                alwaysShowLabel = true,
                icon = {
                        items[index].icon?.let { imageVector -> // Only include icon if present
                            val atEnd = index == selectedItem
                            Icon(painter = rememberAnimatedVectorPainter(
                                animatedImageVector = imageVector,
                                atEnd = atEnd
                            ), contentDescription = null)
                        }
                },
                label = { Text(text = item.text, style = MaterialTheme.typography.labelMedium) }
            )
        }
    }
}

data class NavigationItem @OptIn(ExperimentalAnimationGraphicsApi::class) constructor(
    val icon: AnimatedImageVector?,
    val text: String
)

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NewsBottomNavigationPreview() {
    var selectedItem by remember { mutableIntStateOf(0) }  // Track selection
    val libraryAnimatedIcon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_library_enter)
    val moreAnimatedIcon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_more_enter)

    RoseTheme(dynamicColor = false) {
        NavBar(items = listOf(
            NavigationItem(libraryAnimatedIcon, text = "Library"),
        ), selectedItem = selectedItem , onItemClick = { selectedItem = it})
    }
}
