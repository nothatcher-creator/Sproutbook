package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "milestones",
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
data class Milestone(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val stage: String,
    val title: String,
    val description: String = "",
    val completedOn: Long? = null,
    val notes: String = "",
    val memoryId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones WHERE childId=:childId ORDER BY updatedAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Milestone>>

    @Query("SELECT * FROM milestones WHERE childId=:childId ORDER BY updatedAt DESC")
    suspend fun forChild(childId: String): List<Milestone>

    @Query("SELECT * FROM milestones") suspend fun all(): List<Milestone>

    @Query("SELECT * FROM milestones WHERE id=:id") suspend fun get(id: String): Milestone?

    @Query("SELECT COUNT(*) FROM milestones WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Milestone)

    @Query("DELETE FROM milestones WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
