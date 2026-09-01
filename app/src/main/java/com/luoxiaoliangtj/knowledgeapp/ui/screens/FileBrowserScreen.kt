
package com.luoxiaoliangtj.knowledgeapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luoxiaoliangtj.knowledgeapp.ui.theme.*
import com.luoxiaoliangtj.knowledgeapp.viewmodel.DirectoryItem
import com.luoxiaoliangtj.knowledgeapp.viewmodel.FileViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FileBrowserScreen(
    viewModel: FileViewModel,
    onFileClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPath = uiState.currentPath
    
    LaunchedEffect(currentPath) {
        // Trigger recomposition when path changes
    }
    
    val items = remember(currentPath) {
        viewModel.getDirectoryItems(currentPath)
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Breadcrumb
        BreadcrumbView(
            breadcrumbs = uiState.breadcrumbs,
            onBreadcrumbClick = { path -> viewModel.browse(path) }
        )
        
        // File list
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(items) { item ->
                FileListItem(
                    item = item,
                    onClick = {
                        if (item.isDirectory) {
                            viewModel.browse(item.path)
                        } else {
                            onFileClick(item.path)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BreadcrumbView(
    breadcrumbs: List<Pair<String, String>>,
    onBreadcrumbClick: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        breadcrumbs.forEachIndexed { index, (name, path) ->
            Text(
                text = name,
                color = AccentBlue,
                fontSize = 12.sp,
                modifier = Modifier.clickable { onBreadcrumbClick(path) }
            )
            if (index < breadcrumbs.size - 1) {
                Text(
                    text = " / ",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun FileListItem(
    item: DirectoryItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    when (item.type) {
                        "folder" -> AccentBlue.copy(alpha = 0.12f)
                        "pdf" -> PdfRed.copy(alpha = 0.12f)
                        "text" -> TextGreen.copy(alpha = 0.12f)
                        "ebook" -> BookPurple.copy(alpha = 0.12f)
                        "code" -> CodeOrange.copy(alpha = 0.12f)
                        else -> TextSecondary.copy(alpha = 0.12f)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when (item.type) {
                    "folder" -> "📁"
                    "pdf" -> "📄"
                    "text" -> "📝"
                    "ebook" -> "📚"
                    "code" -> "💻"
                    "doc" -> "📘"
                    "ppt" -> "📊"
                    "excel" -> "📈"
                    else -> "📄"
                },
                fontSize = 18.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (item.isDirectory) "文件夹" else formatSize(item.size) + " · " + item.modified,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1048576) return "%.1f KB".format(bytes / 1024.0)
    return "%.1f MB".format(bytes / 1048576.0)
}
