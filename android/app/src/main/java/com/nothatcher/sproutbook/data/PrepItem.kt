package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "prepItems",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index("childId")],
)
data class PrepItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val kind: String,
    val title: String,
    val notes: String = "",
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface PrepItemDao {
    @Query("SELECT * FROM prepItems WHERE childId=:childId ORDER BY updatedAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<PrepItem>>

    @Query("SELECT * FROM prepItems WHERE childId=:childId ORDER BY updatedAt DESC")
    suspend fun forChild(childId: String): List<PrepItem>

    @Query("SELECT * FROM prepItems") suspend fun all(): List<PrepItem>

    @Query("SELECT * FROM prepItems WHERE id=:id") suspend fun get(id: String): PrepItem?

    @Query("SELECT COUNT(*) FROM prepItems WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: PrepItem)

    @Query("DELETE FROM prepItems WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
