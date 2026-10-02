package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "tooths",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index("childId"), Index(value = ["childId", "toothIndex"], unique = true)],
)
data class Tooth(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val toothIndex: Int,
    val stage: String = "Not seen",
    val eruptedOn: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface ToothDao {
    @Query("SELECT * FROM tooths WHERE childId=:childId ORDER BY updatedAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Tooth>>

    @Query("SELECT * FROM tooths WHERE childId=:childId ORDER BY updatedAt DESC")
    suspend fun forChild(childId: String): List<Tooth>

    @Query("SELECT * FROM tooths") suspend fun all(): List<Tooth>

    @Query("SELECT * FROM tooths WHERE id=:id") suspend fun get(id: String): Tooth?

    @Query("SELECT COUNT(*) FROM tooths WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Tooth)

    @Query("DELETE FROM tooths WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
