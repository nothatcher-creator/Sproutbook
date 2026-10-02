package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "sleeps",
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
data class Sleep(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val startsAt: Long,
    val endsAt: Long? = null,
    val kind: String = "Nap",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface SleepDao {
    @Query(
        "SELECT * FROM sleeps WHERE childId=:childId AND (endsAt IS NULL OR endsAt>=:since) ORDER BY startsAt DESC"
    )
    fun since(childId: String, since: Long): Flow<List<Sleep>>

    @Query(
        "SELECT * FROM sleeps WHERE childId=:childId AND kind='Nap' AND endsAt IS NOT NULL ORDER BY startsAt DESC LIMIT 20"
    )
    fun recentNaps(childId: String): Flow<List<Sleep>>

    @Query(
        "SELECT * FROM sleeps WHERE childId=:childId AND (:kind='All' OR kind=:kind) ORDER BY startsAt DESC, id DESC LIMIT :limit"
    )
    fun history(childId: String, kind: String, limit: Int): Flow<List<Sleep>>

    @Query("SELECT * FROM sleeps WHERE childId=:childId ORDER BY startsAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Sleep>>

    @Query("SELECT * FROM sleeps WHERE childId=:childId ORDER BY startsAt DESC")
    suspend fun forChild(childId: String): List<Sleep>

    @Query("SELECT * FROM sleeps") suspend fun all(): List<Sleep>

    @Query("SELECT * FROM sleeps WHERE id=:id") suspend fun get(id: String): Sleep?

    @Query("SELECT COUNT(*) FROM sleeps WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Sleep)

    @Query("DELETE FROM sleeps WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
