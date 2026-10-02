package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "bottlePreps",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = Feed::class,
                parentColumns = ["id"],
                childColumns = ["feedId"],
                onDelete = ForeignKey.SET_NULL,
            ),
        ],
    indices = [Index("childId"), Index("feedId")],
)
data class BottlePrep(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val preparedAt: Long,
    val amountMl: Double,
    val contents: String = "Formula",
    val status: String = "Prepared",
    val feedId: String? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface BottlePrepDao {
    @Query("SELECT COUNT(*) FROM bottlePreps WHERE childId=:childId AND status='Prepared'")
    fun preparedCount(childId: String): Flow<Int>

    @Query(
        "SELECT * FROM bottlePreps WHERE childId=:childId ORDER BY preparedAt DESC, id DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200): Flow<List<BottlePrep>>

    @Query("SELECT * FROM bottlePreps WHERE childId=:childId")
    suspend fun forChild(childId: String): List<BottlePrep>

    @Query("SELECT * FROM bottlePreps") suspend fun all(): List<BottlePrep>

    @Query("SELECT * FROM bottlePreps WHERE id=:id") suspend fun get(id: String): BottlePrep?

    @Upsert suspend fun save(value: BottlePrep)

    @Query("DELETE FROM bottlePreps WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
