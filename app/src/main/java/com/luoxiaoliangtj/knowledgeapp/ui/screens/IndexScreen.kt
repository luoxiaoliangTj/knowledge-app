
package com.luoxiaoliangtj.knowledgeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luoxiaoliangtj.knowledgeapp.ui.theme.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.FileViewModel

@Composable
fun IndexScreen(viewModel: FileViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            fontSize = 48.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "${uiState.totalCount}",
            fontSize = 36.sp,
            color = AccentBlue
        )
        Text(
            text = "已索引文件",
            fontSize = 14.sp,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = { viewModel.indexDirectory("/sdcard") },
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔄 扫描 /sdcard 建立索引")
        }
        
        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(color = AccentBlue)
        }
        
        if (uiState.indexedCount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✅ 新索引 ${uiState.indexedCount} 个文件",
                color = TextGreen,
                fontSize = 14.sp
            )
        }
    }
}
