package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "memorys",
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
data class Memory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val title: String,
    val occurredOn: Long,
    val category: String = "General",
    val notes: String = "",
    val photo: String? = null,
    val anchor: Int = 0,
    val chapter: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memorys WHERE childId=:childId ORDER BY occurredOn DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Memory>>

    @Query("SELECT * FROM memorys WHERE childId=:childId ORDER BY occurredOn DESC")
    suspend fun forChild(childId: String): List<Memory>

    @Query("SELECT * FROM memorys") suspend fun all(): List<Memory>

    @Query("SELECT * FROM memorys WHERE id=:id") suspend fun get(id: String): Memory?

    @Query("SELECT COUNT(*) FROM memorys WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Memory)

    @Query("DELETE FROM memorys WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
