package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "healthRecords",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = Appointment::class,
                parentColumns = ["id"],
                childColumns = ["appointmentId"],
                onDelete = ForeignKey.SET_NULL,
            ),
        ],
    indices = [Index("childId"), Index("appointmentId")],
)
data class HealthRecord(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val kind: String,
    val recordedAt: Long,
    val title: String,
    val value: String = "",
    val unit: String = "",
    val notes: String = "",
    val appointmentId: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface HealthRecordDao {
    @Query(
        "SELECT * FROM healthRecords WHERE childId=:childId AND (:kind='All' OR kind=:kind) ORDER BY recordedAt DESC, id DESC LIMIT :limit"
    )
    fun history(childId: String, kind: String, limit: Int): Flow<List<HealthRecord>>

    @Query(
        "SELECT * FROM healthRecords WHERE childId=:childId ORDER BY recordedAt DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 500): Flow<List<HealthRecord>>

    @Query("SELECT * FROM healthRecords WHERE childId=:childId ORDER BY recordedAt DESC")
    suspend fun forChild(childId: String): List<HealthRecord>

    @Query("SELECT * FROM healthRecords") suspend fun all(): List<HealthRecord>

    @Query("SELECT * FROM healthRecords WHERE id=:id") suspend fun get(id: String): HealthRecord?

    @Query("SELECT COUNT(*) FROM healthRecords WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: HealthRecord)

    @Query("DELETE FROM healthRecords WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
