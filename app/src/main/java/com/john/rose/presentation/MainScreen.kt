package com.john.rose.presentation

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.john.rose.R
import com.john.rose.presentation.common.material.AppTopBar
import com.john.rose.presentation.common.material.NavBar
import com.john.rose.presentation.common.material.NavigationItem
import com.john.rose.presentation.navgraph.MainRouteScreen
import com.john.rose.presentation.navgraph.graphs.MainNavGraph

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun MainScreen(
    rootNavController: NavHostController,
    homeNavController: NavHostController = rememberNavController()
) {
    val libraryAnimatedIcon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_library_enter)
    val moreAnimatedIcon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_more_enter)
    val settingsAnimatedIcon = AnimatedImageVector.animatedVectorResource(R.drawable.anim_settings_enter)
    
    val navigationItem = remember {
        listOf(
            NavigationItem(libraryAnimatedIcon, text = "Library"),
            NavigationItem(settingsAnimatedIcon, text = "Settings"),
        )
    }

    val backStackState = homeNavController.currentBackStackEntryAsState().value
    var selectedItem by rememberSaveable {
        mutableIntStateOf(0)
    }

    selectedItem = when (backStackState?.destination?.route) {
        MainRouteScreen.Library.route -> 0
        MainRouteScreen.Settings.route -> 1
        else -> 0
    }

    //Hide the bottom navigation when the user is in the details screen
    val isBarVisible = remember(key1 = backStackState) {
        backStackState?.destination?.route == MainRouteScreen.Library.route ||
                backStackState?.destination?.route == MainRouteScreen.Settings.route
    }

    val isImportVisible = remember(key1 = backStackState) {
        backStackState?.destination?.route == MainRouteScreen.Library.route
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (isBarVisible) {
                AppTopBar(
                    items = navigationItem,
                    selectedItem = selectedItem,
                    isImportVisible = isImportVisible
                )
            }
        },
        bottomBar = {
            if (isBarVisible) {
                NavBar(
                    items = navigationItem,
                    selectedItem = selectedItem,
                    onItemClick = { index ->
                        when (index) {
                            0 -> navigateToTab(
                                navController = homeNavController,
                                route = MainRouteScreen.Library.route
                            )
                            1 -> navigateToTab(
                                navController = homeNavController,
                                route = MainRouteScreen.Settings.route
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        MainNavGraph(
            rootNavController = rootNavController,
            homeNavController,
            innerPadding
        )
    }
}

private fun navigateToTab(navController: NavController, route: String) {
    navController.navigate(route) {
        navController.graph.startDestinationRoute?.let { screenRoute ->
            popUpTo(screenRoute) {
                saveState = true
            }
        }
        launchSingleTop = true
        restoreState = true
    }
}
