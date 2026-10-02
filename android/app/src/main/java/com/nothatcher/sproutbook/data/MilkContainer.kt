package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "milkContainers",
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
                childColumns = ["pumpId"],
                onDelete = ForeignKey.SET_NULL,
            ),
        ],
    indices = [Index("childId"), Index(value = ["pumpId"], unique = true)],
)
data class MilkContainer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val label: String,
    val storedAt: Long,
    val amountMl: Double,
    val location: String = "",
    val notes: String = "",
    val status: String = "Frozen",
    val pumpId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface MilkContainerDao {
    @Query("SELECT * FROM milkContainers WHERE childId=:childId ORDER BY storedAt, id LIMIT :limit")
    fun observe(childId: String, limit: Int = 200): Flow<List<MilkContainer>>

    @Query("SELECT * FROM milkContainers WHERE childId=:childId")
    suspend fun forChild(childId: String): List<MilkContainer>

    @Query("SELECT * FROM milkContainers") suspend fun all(): List<MilkContainer>

    @Query("SELECT * FROM milkContainers WHERE id=:id") suspend fun get(id: String): MilkContainer?

    @Upsert suspend fun save(value: MilkContainer)

    @Query("DELETE FROM milkContainers WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)

    @Query(
        "SELECT * FROM milkContainers WHERE childId=:childId AND (:status='All' OR status=:status) ORDER BY storedAt, id LIMIT :limit"
    )
    fun history(childId: String, status: String, limit: Int): Flow<List<MilkContainer>>

    @Query(
        "SELECT COUNT(*) AS count, COALESCE(SUM(amountMl),0) AS totalMl FROM milkContainers WHERE childId=:childId AND status='Frozen'"
    )
    fun frozenSummary(childId: String): Flow<MilkStock>

    @Query("SELECT * FROM milkContainers WHERE pumpId=:pumpId")
    suspend fun forPump(pumpId: String): MilkContainer?

    @Query(
        "SELECT feeds.* FROM feeds LEFT JOIN milkContainers ON feeds.id=milkContainers.pumpId WHERE feeds.childId=:childId AND feeds.kind='Pump' AND milkContainers.id IS NULL ORDER BY feeds.startsAt DESC LIMIT :limit"
    )
    fun unstoredPumps(childId: String, limit: Int): Flow<List<Feed>>
}

data class MilkStock(val count: Int = 0, val totalMl: Double = 0.0)
