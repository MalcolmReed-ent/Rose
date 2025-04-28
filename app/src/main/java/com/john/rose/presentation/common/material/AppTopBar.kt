package com.john.rose.presentation.common.material

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import com.john.rose.R
import com.john.rose.utils.onDoImportEPUB

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    items: List<NavigationItem>,
    selectedItem: Int,
    isImportVisible: Boolean
) {
    var isOverflowExpanded by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            titleContentColor = colorResource(id = R.color.display_small),
        ),
        title = {
            Text(text = items[selectedItem].text)
        },
        actions = {
            Row {
                IconButton(onClick = { isOverflowExpanded = true }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_overflow_24dp),
                        contentDescription = "Overflow"
                    )
                }
                DropdownMenu(
                    expanded = isOverflowExpanded,
                    onDismissRequest = { isOverflowExpanded = false },
                ) {
                    if (isImportVisible) {
                        DropdownMenuItem(
                            text = { Text(text = "Import Book") },
                            onClick = onDoImportEPUB(),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.FileDownload,
                                    contentDescription = "Import Icon"
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}
