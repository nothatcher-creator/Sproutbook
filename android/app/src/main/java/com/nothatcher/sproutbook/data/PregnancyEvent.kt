package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "pregnancyEvents",
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
data class PregnancyEvent(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val kind: String,
    val startsAt: Long,
    val endsAt: Long? = null,
    val count: Int = 0,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface PregnancyEventDao {
    @Query(
        "SELECT * FROM pregnancyEvents WHERE childId=:childId AND endsAt IS NULL ORDER BY startsAt DESC"
    )
    fun active(childId: String): Flow<List<PregnancyEvent>>

    @Query(
        "SELECT * FROM pregnancyEvents WHERE childId=:childId AND (:kind='All' OR kind=:kind) ORDER BY startsAt DESC, id DESC LIMIT :limit"
    )
    fun history(childId: String, kind: String, limit: Int): Flow<List<PregnancyEvent>>

    @Query(
        "SELECT * FROM pregnancyEvents WHERE childId=:childId ORDER BY startsAt DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 500): Flow<List<PregnancyEvent>>

    @Query("SELECT * FROM pregnancyEvents WHERE childId=:childId ORDER BY startsAt DESC")
    suspend fun forChild(childId: String): List<PregnancyEvent>

    @Query("SELECT * FROM pregnancyEvents") suspend fun all(): List<PregnancyEvent>

    @Query("SELECT * FROM pregnancyEvents WHERE id=:id")
    suspend fun get(id: String): PregnancyEvent?

    @Query("SELECT COUNT(*) FROM pregnancyEvents WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: PregnancyEvent)

    @Query("DELETE FROM pregnancyEvents WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
