package com.luoxiaoliangtj.knowledgeapp.data.db

import androidx.room.*
import com.luoxiaoliangtj.knowledgeapp.data.model.Category
import com.luoxiaoliangtj.knowledgeapp.data.model.FileItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {
    @Query("SELECT * FROM files ORDER BY modified DESC")
    fun getAll(): Flow<List<FileItem>>

    @Query("SELECT * FROM files WHERE id = :id")
    suspend fun getById(id: Long): FileItem?

    @Query("SELECT * FROM files WHERE path = :path LIMIT 1")
    suspend fun getByPath(path: String): FileItem?

    @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' OR path LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<FileItem>>

    @Query("SELECT * FROM files WHERE category = :category")
    fun getByCategory(category: String): Flow<List<FileItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: FileItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<FileItem>)

    @Update
    suspend fun update(file: FileItem)

    @Delete
    suspend fun delete(file: FileItem)

    @Query("DELETE FROM files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM files")
    suspend fun count(): Int

    @Query("SELECT * FROM files WHERE contentHash = :hash LIMIT 1")
    suspend fun findByHash(hash: String): FileItem?
}

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    @Delete
    suspend fun delete(category: Category)
}

@Database(entities = [FileItem::class, Category::class], version = 1, exportSchema = false)
abstract class KnowledgeDatabase : RoomDatabase() {
    abstract fun fileDao(): FileDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: KnowledgeDatabase? = null

        fun getInstance(context: android.content.Context): KnowledgeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KnowledgeDatabase::class.java,
                    "knowledge.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
