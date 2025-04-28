package com.john.rose.presentation.navgraph.graphs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.john.rose.presentation.library.LibraryScreen
import com.john.rose.presentation.settings.SettingsScreen
import com.john.rose.presentation.navgraph.Graph
import com.john.rose.presentation.navgraph.MainRouteScreen

@Composable
fun MainNavGraph(
    rootNavController: NavHostController,
    homeNavController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = homeNavController,
        route = Graph.MainScreenGraph,
        startDestination = MainRouteScreen.Library.route,
        modifier = Modifier.padding(innerPadding),
        enterTransition = { enterTransition() },
        exitTransition = { exitTransition() },
        popEnterTransition = { popEnterTransition() },
        popExitTransition = { popExitTransition() }
    ) {
        composable(route = MainRouteScreen.Library.route) {
            LibraryScreen(rootNavController)
        }
        composable(
            route = MainRouteScreen.Settings.route,
            deepLinks = listOf(
                navDeepLink { 
                    uriPattern = "android-app://androidx.navigation/settings"
                }
            )
        ) {
            SettingsScreen()
        }
    }
}
