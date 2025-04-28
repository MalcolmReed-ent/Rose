package com.john.rose.presentation.navgraph.graphs

import androidx.compose.animation.fadeOut
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.john.rose.presentation.navgraph.Graph
import com.john.rose.presentation.navgraph.OnboardingRoute
import com.john.rose.presentation.onboarding.OnBoardingScreen
import com.john.rose.presentation.onboarding.OnBoardingViewModel

fun NavGraphBuilder.onBoardingNavGraph(rootNavController: NavHostController) {
    navigation(
        route = Graph.OnboardingGraph,
        startDestination = OnboardingRoute.OnboardingScreen.route
    ) {
        composable(route = OnboardingRoute.OnboardingScreen.route, exitTransition = { fadeOut() }) {
            val viewModel: OnBoardingViewModel = hiltViewModel()
            OnBoardingScreen(onEvent = viewModel::onEvent)
        }
    }
}
