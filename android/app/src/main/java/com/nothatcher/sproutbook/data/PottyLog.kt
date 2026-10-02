package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "pottyLogs",
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
data class PottyLog(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val recordedAt: Long,
    val kind: String = "Tried",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface PottyLogDao {
    @Query(
        "SELECT * FROM pottyLogs WHERE childId=:childId ORDER BY recordedAt DESC, id DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200): Flow<List<PottyLog>>

    @Query("SELECT * FROM pottyLogs WHERE childId=:childId")
    suspend fun forChild(childId: String): List<PottyLog>

    @Query("SELECT * FROM pottyLogs") suspend fun all(): List<PottyLog>

    @Query("SELECT * FROM pottyLogs WHERE id=:id") suspend fun get(id: String): PottyLog?

    @Upsert suspend fun save(value: PottyLog)

    @Query("DELETE FROM pottyLogs WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)

    @Query(
        "SELECT * FROM pottyLogs WHERE childId=:childId AND recordedAt>=:since ORDER BY recordedAt DESC"
    )
    fun since(childId: String, since: Long): Flow<List<PottyLog>>
}
