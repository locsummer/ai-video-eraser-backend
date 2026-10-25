package com.aivideoeraser.android.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.aivideoeraser.android.ui.screens.*
import com.aivideoeraser.android.viewmodel.AuthViewModel
import com.aivideoeraser.android.viewmodel.EditorViewModel
import com.aivideoeraser.android.viewmodel.TaskViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Editor : Screen("editor?uri={uri}") {
        fun createRoute(uri: String) = "editor?uri=${Uri.encode(uri)}"
    }
    object Processing : Screen("processing/{taskId}") {
        fun createRoute(taskId: String) = "processing/$taskId"
    }
    object Result : Screen("result/{taskId}") {
        fun createRoute(taskId: String) = "result/$taskId"
    }
    object Subscription : Screen("subscription")
    object Auth : Screen("auth")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel(),
    editorViewModel: EditorViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onVideoSelected = { uri ->
                    navController.navigate(Screen.Editor.createRoute(uri.toString()))
                },
                onOpenSubscription = { navController.navigate(Screen.Subscription.route) },
                onOpenAuth = { navController.navigate(Screen.Auth.route) }
            )
        }

        composable(
            route = Screen.Editor.route,
            arguments = listOf(navArgument("uri") { type = NavType.StringType })
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString("uri") ?: ""
            val videoUri = Uri.parse(Uri.decode(uriString))

            EditorScreen(
                videoUri = videoUri,
                editorViewModel = editorViewModel,
                onBack = { navController.popBackStack() },
                onTaskCreated = { taskId ->
                    navController.navigate(Screen.Processing.createRoute(taskId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Processing.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            ProcessingScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onComplete = { completedTaskId ->
                    navController.navigate(Screen.Result.createRoute(completedTaskId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onCancelled = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            ResultScreen(
                taskId = taskId,
                taskViewModel = taskViewModel,
                onDone = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Subscription.route) {
            SubscriptionScreen(
                taskViewModel = taskViewModel,
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                authViewModel = authViewModel,
                onSuccess = { navController.popBackStack() },
                onClose = { navController.popBackStack() }
            )
        }
    }
}
