package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "diapers",
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
data class Diaper(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val recordedAt: Long,
    val kind: String = "Wet",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface DiaperDao {
    @Query(
        "SELECT * FROM diapers WHERE childId=:childId AND recordedAt>=:since ORDER BY recordedAt DESC"
    )
    fun since(childId: String, since: Long): Flow<List<Diaper>>

    @Query(
        "SELECT * FROM diapers WHERE childId=:childId ORDER BY recordedAt DESC, id DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200): Flow<List<Diaper>>

    @Query("SELECT * FROM diapers WHERE childId=:childId")
    suspend fun forChild(childId: String): List<Diaper>

    @Query("SELECT * FROM diapers") suspend fun all(): List<Diaper>

    @Query("SELECT * FROM diapers WHERE id=:id") suspend fun get(id: String): Diaper?

    @Upsert suspend fun save(value: Diaper)

    @Query("DELETE FROM diapers WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
