package com.john.rose

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.john.rose.presentation.navgraph.graphs.RootNavGraph
import com.john.rose.presentation.theme.RoseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen().apply {
            setKeepOnScreenCondition{
                viewModel.splashCondition
            }
        }

        setContent {
            RoseTheme {
                Box(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .fillMaxSize()
                ) {
                    val startDestination = viewModel.startDestination
                    RootNavGraph(startDestination = startDestination)
                }
            }
        }
    }
}
