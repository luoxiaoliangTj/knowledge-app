
package com.luoxiaoliangtj.knowledgeapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luoxiaoliangtj.knowledgeapp.ui.theme.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "⚙️ AI 设置",
            fontSize = 20.sp,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Provider
        Text(text = "API 提供商", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = settings.provider,
            onValueChange = { viewModel.updateProvider(it) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerColor,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // API Key
        Text(text = "API Key", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = settings.apiKey,
            onValueChange = { viewModel.updateApiKey(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerColor,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Base URL
        Text(text = "API 地址 (Base URL)", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = settings.baseUrl,
            onValueChange = { viewModel.updateBaseUrl(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerColor,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Model
        Text(text = "模型名称", color = TextSecondary, fontSize = 13.sp)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = settings.model,
            onValueChange = { viewModel.updateModel(it) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerColor,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            )
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Save button
        Button(
            onClick = { viewModel.saveSettings() },
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存设置")
        }
        
        if (settings.isSaved) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("✅ 已保存", color = TextGreen, fontSize = 13.sp)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info box
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("💡 说明", color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• 默认使用远端大模型，需要 API Key
" +
                           "• 支持任何 OpenAI 兼容的 API
" +
                           "• 默认已填入 Agnes AI 接口
" +
                           "• 本地 Ollama 需要自行安装并启动",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
