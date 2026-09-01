
package com.luoxiaoliangtj.knowledgeapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.luoxiaoliangtj.knowledgeapp.data.db.KnowledgeDatabase
import com.luoxiaoliangtj.knowledgeapp.data.model.FileItem
import com.luoxiaoliangtj.knowledgeapp.data.repository.FileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

data class FileUiState(
    val files: List<FileItem> = emptyList(),
    val currentPath: String = "/sdcard",
    val breadcrumbs: List<Pair<String, String>> = listOf("存储" to "/sdcard"),
    val isLoading: Boolean = false,
    val searchResults: List<FileItem> = emptyList(),
    val isSearching: Boolean = false,
    val totalCount: Int = 0,
    val indexedCount: Int = 0
)

class FileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FileRepository
    private val _uiState = MutableStateFlow(FileUiState())
    val uiState: StateFlow<FileUiState> = _uiState.asStateFlow()
    
    init {
        val db = KnowledgeDatabase.getInstance(application)
        repository = FileRepository(db.fileDao())
        
        // Observe files
        viewModelScope.launch {
            repository.allFiles.collect { files ->
                _uiState.update { it.copy(files = files, totalCount = files.size) }
            }
        }
    }
    
    fun browse(path: String) {
        _uiState.update { it.copy(currentPath = path) }
        // Build breadcrumbs
        val parts = path.split("/").filter { it.isNotEmpty() }
        val crumbs = mutableListOf<Pair<String, String>>("存储" to "/sdcard")
        var buildPath = "/sdcard"
        for (part in parts) {
            if (part == "sdcard") continue
            buildPath += "/$part"
            crumbs.add(part to buildPath)
        }
        _uiState.update { it.copy(breadcrumbs = crumbs) }
    }
    
    fun getDirectoryItems(path: String): List<DirectoryItem> {
        val dir = File(path)
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        
        val items = mutableListOf<DirectoryItem>()
        if (path != "/sdcard") {
            items.add(DirectoryItem("..", dir.parent ?: "/sdcard", true, "folder", 0, ""))
        }
        
        dir.listFiles()?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))?.forEach { file ->
            val ext = file.extension.lowercase()
            val type = when {
                file.isDirectory -> "folder"
                ext in setOf("pdf") -> "pdf"
                ext in setOf("epub", "mobi", "azw3") -> "ebook"
                ext in setOf("txt", "md", "py", "js", "html", "css", "json", "xml", "csv", "log", "kt", "java") -> "text"
                ext in setOf("docx", "doc") -> "doc"
                ext in setOf("pptx", "ppt") -> "ppt"
                ext in setOf("xlsx", "xls") -> "excel"
                else -> "unknown"
            }
            items.add(DirectoryItem(
                name = file.name,
                path = file.absolutePath,
                isDirectory = file.isDirectory,
                type = type,
                size = if (file.isFile) file.length() else 0,
                modified = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified()))
            ))
        }
        return items
    }
    
    fun search(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
            return
        }
        _uiState.update { it.copy(isSearching = true) }
        viewModelScope.launch {
            repository.search(query).collect { results ->
                _uiState.update { it.copy(searchResults = results, isSearching = true) }
            }
        }
    }
    
    fun clearSearch() {
        _uiState.update { it.copy(isSearching = false, searchResults = emptyList()) }
    }
    
    fun indexDirectory(path: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = repository.indexDirectory(path)
            _uiState.update { it.copy(isLoading = false, indexedCount = result.getOrDefault(0)) }
        }
    }
    
    fun readFileContent(path: String): String? {
        val file = File(path)
        val ext = file.extension.lowercase()
        if (ext in setOf("txt", "md", "py", "js", "html", "css", "json", "xml", "csv", "log", "kt", "java", "cpp", "c", "h")) {
            return try { file.readText(Charsets.UTF_8) } catch (e: Exception) { "无法读取文件: ${e.message}" }
        }
        return null
    }
}

data class DirectoryItem(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val type: String,
    val size: Long,
    val modified: String
)
