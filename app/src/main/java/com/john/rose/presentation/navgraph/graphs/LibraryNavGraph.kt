package com.john.rose.presentation.navgraph.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.john.rose.presentation.book.BookScreen
import com.john.rose.presentation.navgraph.Graph
import com.john.rose.presentation.navgraph.LibraryRouteScreen

fun NavGraphBuilder.libraryNavGraph(rootNavController: NavHostController) {
    navigation(
        route = Graph.LibraryScreenGraph,
        startDestination = LibraryRouteScreen.BookDetailScreen.route
    ) {
        composable(
            route = "book?bookUrl={rawBookUrl}&bookTitle={bookTitle}&libraryId={libraryId}",
            arguments = listOf(
                navArgument("rawBookUrl") {
                    type = NavType.StringType
                },
                navArgument("bookTitle") {
                    type = NavType.StringType
                },
                navArgument("libraryId") {
                    type = NavType.StringType
                }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val libraryId = backStackEntry.arguments?.getString("libraryId") ?: ""
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: ""
            val rawBookUrl = backStackEntry.arguments?.getString("rawBookUrl") ?: ""
            BookScreen(libraryId = libraryId, rawBookUrl = rawBookUrl, bookTitle = bookTitle, rootNavController = rootNavController)
        }
        composable(
            route = "reader?bookUrl={bookUrl}&chapterUrl={chapterUrl}",
            arguments = listOf(
                navArgument("bookUrl") {
                    type = NavType.StringType
                },
                navArgument("chapterUrl") {
                    type = NavType.StringType
                }
            ),
            enterTransition = { enterTransition() },
            exitTransition = { exitTransition() },
            popEnterTransition = { popEnterTransition() },
            popExitTransition = { popExitTransition() }
        ) { backStackEntry ->
            val bookUrl = backStackEntry.arguments?.getString("bookUrl") ?: ""
            val chapterUrl = backStackEntry.arguments?.getString("chapterUrl") ?: ""
            // TODO: Add ReaderScreen here when implemented
            // ReaderScreen(bookUrl = bookUrl, chapterUrl = chapterUrl, rootNavController = rootNavController)
        }
    }
}
