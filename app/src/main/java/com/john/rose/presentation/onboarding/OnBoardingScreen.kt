package com.john.rose.presentation.onboarding


import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.john.rose.presentation.Dimens.MediumPadding2
import com.john.rose.presentation.common.material.OnTextButton
import com.john.rose.presentation.common.material.OnboardButton
import com.john.rose.presentation.common.material.PageIndicator
import com.john.rose.presentation.onboarding.components.OnBoardingPage
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnBoardingScreen(
    onEvent: (OnBoardingEvent) -> Unit
) {
    val notificationPermissionResultLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {isGranted ->
            if (isGranted) {
                onEvent(OnBoardingEvent.SaveAppEntry)
            }
            else
                onEvent(OnBoardingEvent.SaveAppEntry)
        }
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        Alignment.Center
        ) {
        Column (
            modifier = Modifier
                .fillMaxSize(),
                Arrangement.Bottom
        ){
            val pagerState = rememberPagerState(initialPage = 0) {
                pages.size
            }
            val buttonState = remember {
                derivedStateOf {
                    when(pagerState.currentPage) {
                        0 -> listOf("", "GET STARTED")
                        1 -> listOf("Back", "I UNDERSTAND")
                        2 -> listOf("Back", "REGISTER")
                        else -> listOf("", "")
                    }
                }
            }
            HorizontalPager(state = pagerState) {index ->
                OnBoardingPage(page = pages[index])
            }
        Spacer(modifier = Modifier.height(100.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MediumPadding2)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ){
                PageIndicator(
                    modifier = Modifier.width(80.dp),
                    pageSize = pages.size,
                    selectedPage = pagerState.currentPage
                )

                Row(verticalAlignment = Alignment.CenterVertically){
                    val scope = rememberCoroutineScope()

                    if (buttonState.value[0].isNotEmpty()) {
                        OnTextButton(
                            text = buttonState.value[0],
                            onClick = {
                                scope.launch {
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage - 1
                                    )
                                }

                            }
                        )
                    }

                    OnboardButton(
                        text = buttonState.value[1],
                        onClick = {
                            scope.launch {
                                if (pagerState.currentPage == 2) {
                                    notificationPermissionResultLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    )
                                }else{
                                    pagerState.animateScrollToPage(
                                        page = pagerState.currentPage + 1
                                    )
                                }
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}