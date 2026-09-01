package com.luoxiaoliangtj.knowledgeapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "files")
data class FileItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val path: String,
    val name: String,
    val ext: String,
    val size: Long,
    val modified: String,
    val category: String = "未分类",
    val tags: String = "[]",
    val summary: String = "",
    val contentHash: String = "",
    val indexedAt: String = ""
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = ""
)
