
package com.luoxiaoliangtj.knowledgeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luoxiaoliangtj.knowledgeapp.ui.theme.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.FileViewModel

@Composable
fun FilePreviewScreen(
    filePath: String,
    fileViewModel: FileViewModel,
    onBack: () -> Unit,
    onAiSummarize: () -> Unit,
    onAiClassify: () -> Unit
) {
    val fileName = remember(filePath) { filePath.substringAfterLast("/") }
    val content = remember(filePath) { fileViewModel.readFileContent(filePath) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回", tint = TextPrimary)
            }
            Text(
                text = fileName,
                color = TextPrimary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }
        
        // Content
        if (content != null) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = content,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )
            }
        } else {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text("无法预览此文件类型", color = TextSecondary)
            }
        }
        
        // Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onAiSummarize,
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                modifier = Modifier.weight(1f).padding(end = 4.dp)
            ) {
                Text("📝 AI总结", fontSize = 13.sp)
            }
            Button(
                onClick = onAiClassify,
                colors = ButtonDefaults.buttonColors(containerColor = DarkCard),
                modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
            ) {
                Text("🏷️ AI分类", fontSize = 13.sp)
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).padding(start = 4.dp)
            ) {
                Text("关闭", fontSize = 13.sp, color = TextPrimary)
            }
        }
    }
}
