package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "feeds",
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
data class Feed(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val kind: String,
    val startsAt: Long,
    val amountMl: Double = 0.0,
    val leftMl: Double = 0.0,
    val rightMl: Double = 0.0,
    val durationSeconds: Long = 0,
    val side: String = "Both",
    val contents: String = "Formula",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface FeedDao {
    @Query("SELECT * FROM feeds WHERE childId=:childId AND startsAt>=:since ORDER BY startsAt DESC")
    fun since(childId: String, since: Long): Flow<List<Feed>>

    @Query(
        "SELECT * FROM feeds WHERE childId=:childId AND kind!='Pump' ORDER BY startsAt DESC LIMIT 1"
    )
    fun latestFeed(childId: String): Flow<Feed?>

    @Query(
        "SELECT * FROM feeds WHERE childId=:childId AND (:kind='All' OR kind=:kind) ORDER BY startsAt DESC, id DESC LIMIT :limit"
    )
    fun history(childId: String, kind: String, limit: Int): Flow<List<Feed>>

    @Query("SELECT * FROM feeds WHERE childId=:childId ORDER BY startsAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Feed>>

    @Query("SELECT * FROM feeds WHERE childId=:childId ORDER BY startsAt DESC")
    suspend fun forChild(childId: String): List<Feed>

    @Query("SELECT * FROM feeds") suspend fun all(): List<Feed>

    @Query("SELECT * FROM feeds WHERE id=:id") suspend fun get(id: String): Feed?

    @Query("SELECT COUNT(*) FROM feeds WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Feed)

    @Query("DELETE FROM feeds WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
