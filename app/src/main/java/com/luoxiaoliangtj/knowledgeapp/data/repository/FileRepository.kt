
package com.luoxiaoliangtj.knowledgeapp.data.repository

import android.content.Context
import android.os.Environment
import com.luoxiaoliangtj.knowledgeapp.data.db.FileDao
import com.luoxiaoliangtj.knowledgeapp.data.model.FileItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class FileRepository(private val fileDao: FileDao) {
    
    val allFiles: Flow<List<FileItem>> = fileDao.getAll()
    
    fun search(query: String): Flow<List<FileItem>> = fileDao.search(query)
    
    fun getByCategory(category: String): Flow<List<FileItem>> = fileDao.getByCategory(category)
    
    suspend fun getById(id: Long): FileItem? = fileDao.getById(id)
    
    suspend fun indexDirectory(path: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val dir = File(path)
            if (!dir.exists() || !dir.isDirectory) {
                return@withContext Result.failure(Exception("路径不存在"))
            }
            
            var count = 0
            val supportedExts = setOf(
                "pdf", "epub", "mobi", "azw3", "txt", "md", "docx", "doc",
                "pptx", "ppt", "xlsx", "xls", "py", "js", "html", "css",
                "json", "xml", "csv", "log", "kt", "java", "cpp", "c", "h"
            )
            
            dir.walkTopDown()
                .filter { it.isFile && it.extension.lowercase() in supportedExs }
                .forEach { file ->
                    try {
                        val hash = md5(file.readBytes().take(8192).toByteArray())
                        val existing = fileDao.findByHash(hash)
                        if (existing == null) {
                            val item = FileItem(
                                path = file.absolutePath,
                                name = file.name,
                                ext = file.extension.lowercase(),
                                size = file.length(),
                                modified = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(file.lastModified())),
                                contentHash = hash,
                                indexedAt = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
                            )
                            fileDao.insert(item)
                            count++
                        }
                    } catch (e: Exception) {
                        // skip
                    }
                }
            
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun insert(file: FileItem) = fileDao.insert(file)
    suspend fun update(file: FileItem) = fileDao.update(file)
    suspend fun delete(file: FileItem) = fileDao.delete(file)
    suspend fun count(): Int = fileDao.count()
    
    private fun md5(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("MD5")
        return md.digest(bytes).joinToString("") { "%02x".format(it) }
    }
    
    fun getDefaultScanPaths(): List<String> {
        val paths = mutableListOf<String>()
        paths.add(Environment.getExternalStorageDirectory().absolutePath)
        paths.add(Environment.getExternalStorageDirectory().absolutePath + "/Download")
        paths.add(Environment.getExternalStorageDirectory().absolutePath + "/Documents")
        return paths
    }
}
