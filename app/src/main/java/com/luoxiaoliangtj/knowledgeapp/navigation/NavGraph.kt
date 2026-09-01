
package com.luoxiaoliangtj.knowledgeapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.luoxiaoliangtj.knowledgeapp.ui.screens.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.AiViewModel
import com.luoxiaoliangtj.knowledgeapp.viewmodel.FileViewModel
import com.luoxiaoliangtj.knowledgeapp.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object FilePreview : Screen("preview/{filePath}") {
        fun createRoute(filePath: String) = "preview/${java.net.URLEncoder.encode(filePath, "UTF-8")}"
    }
}

@Composable
fun KnowledgeNavGraph(
    navController: NavHostController = rememberNavController(),
    fileViewModel: FileViewModel = viewModel(),
    aiViewModel: AiViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    NavHost(navController = navController, startDestination = Screen.Main.route) {
        composable(Screen.Main.route) {
            MainScreen(
                fileViewModel = fileViewModel,
                aiViewModel = aiViewModel,
                settingsViewModel = settingsViewModel,
                onNavigateToPreview = { filePath ->
                    navController.navigate(Screen.FilePreview.createRoute(filePath))
                }
            )
        }
        composable(Screen.FilePreview.route) { backStackEntry ->
            val filePath = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath") ?: "",
                "UTF-8"
            )
            FilePreviewScreen(
                filePath = filePath,
                fileViewModel = fileViewModel,
                onBack = { navController.popBackStack() },
                onAiSummarize = { /* TODO: pass settings */ },
                onAiClassify = { /* TODO: pass settings */ }
            )
        }
    }
}
