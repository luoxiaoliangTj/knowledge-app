
package com.luoxiaoliangtj.knowledgeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luoxiaoliangtj.knowledgeapp.data.model.FileItem
import com.luoxiaoliangtj.knowledgeapp.ui.theme.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.FileViewModel

@Composable
fun SearchScreen(
    viewModel: FileViewModel,
    onFileClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var query by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = query,
            onValueChange = { 
                query = it
                viewModel.search(it)
            },
            placeholder = { Text("搜索文件、标签、内容...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, tint = TextSecondary, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = DividerColor,
                cursorColor = AccentBlue,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            ),
            singleLine = true
        )
        
        // Results
        if (query.isBlank()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("输入关键词搜索", color = TextSecondary)
            }
        } else if (uiState.searchResults.isEmpty() && !uiState.isSearching) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("没有找到结果", color = TextSecondary)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.searchResults) { file ->
                    SearchResultItem(file = file, onClick = { onFileClick(file.path) })
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(file: FileItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(AccentBlue.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (file.ext) {
                    "pdf" -> "📄"
                    "txt", "md" -> "📝"
                    "epub", "mobi" -> "📚"
                    else -> "📄"
                },
                fontSize = 16.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = file.name, color = TextPrimary, fontSize = 14.sp, maxLines = 1)
            Text(text = file.path, color = TextSecondary, fontSize = 11.sp, maxLines = 1)
            if (file.category != "未分类") {
                Text(text = file.category, color = AccentBlue, fontSize = 11.sp)
            }
        }
    }
}
