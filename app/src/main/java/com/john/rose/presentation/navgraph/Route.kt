package com.john.rose.presentation.navgraph

const val BOOK_ID_ARG_KEY = "bookId"

object Graph {
    const val RootGraph = "rootGraph"
    const val OnboardingGraph = "onBoardingGraph"
    const val MainScreenGraph = "mainScreenGraph"
    const val LibraryScreenGraph = "libraryScreenGraph"
    const val SettingsGraph = "settingsGraph"
}

sealed class OnboardingRoute(var route: String) {
    data object OnboardingScreen: OnboardingRoute("onBoarding")
    data object Login: OnboardingRoute("login")
    data object Register: OnboardingRoute("register")
}

sealed class MainRouteScreen(var route: String) {
    data object Library: MainRouteScreen("library")
    data object Settings: MainRouteScreen("settings")
}

sealed class LibraryRouteScreen(var route: String) {
    data object BookDetailScreen: LibraryRouteScreen("book")
}

sealed class SettingsRouteScreen(var route: String) {
    data object SettingsDetail: SettingsRouteScreen("settingsDetail")
}
