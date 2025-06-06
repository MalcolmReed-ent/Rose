package com.john.rose.presentation.common.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.john.rose.presentation.Dimens.IndicatorSize
import com.john.rose.presentation.theme.LightBlack

@Composable
fun PageIndicator(
    modifier: Modifier = Modifier,
    pageSize: Int,
    selectedPage: Int,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = LightBlack
) {
    Row (modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween){
        repeat(times = pageSize) { page ->
            Box(
                modifier = Modifier
                    .height(IndicatorSize)
                    .width(
                        if(page == selectedPage) 40.dp
                        else IndicatorSize
                    )
                    .clip(
                        if (page == selectedPage) RoundedCornerShape(50)
                        else CircleShape
                    )
                    .background(color = if (page == selectedPage) selectedColor else unselectedColor)
            )
        }
    }
}