
package com.luoxiaoliangtj.knowledgeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.luoxiaoliangtj.knowledgeapp.ui.theme.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.AiViewModel
import com.luoxiaoliangtj.knowledgeapp.viewmodel.FileViewModel
import com.luoxiaoliangtj.knowledgeapp.viewmodel.SettingsViewModel

sealed class BottomTab(val label: String, val icon: ImageVector, val route: String) {
    object Files : BottomTab("文件", Icons.Default.Folder, "files")
    object Search : BottomTab("搜索", Icons.Default.Search, "search")
    object AI : BottomTab("AI", Icons.Default.SmartToy, "ai")
    object Settings : BottomTab("设置", Icons.Default.Settings, "settings")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    fileViewModel: FileViewModel,
    aiViewModel: AiViewModel,
    settingsViewModel: SettingsViewModel,
    onNavigateToPreview: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(BottomTab.Files, BottomTab.Search, BottomTab.AI, BottomTab.Settings)
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                contentColor = TextPrimary
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentBlue,
                            selectedTextColor = AccentBlue,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary,
                            indicatorColor = DarkCard
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            when (selectedTab) {
                0 -> FileBrowserScreen(viewModel = fileViewModel, onFileClick = onNavigateToPreview)
                1 -> SearchScreen(viewModel = fileViewModel, onFileClick = onNavigateToPreview)
                2 -> AiChatScreen(aiViewModel = aiViewModel, settingsViewModel = settingsViewModel)
                3 -> SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}
