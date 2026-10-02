package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "appointments",
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
data class Appointment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val title: String,
    val startsAt: Long,
    val location: String = "",
    val category: String = "Other",
    val notes: String = "",
    val questions: String = "",
    val results: String = "",
    val reminderMinutes: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments WHERE childId=:childId ORDER BY startsAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Appointment>>

    @Query("SELECT * FROM appointments WHERE childId=:childId ORDER BY startsAt DESC")
    suspend fun forChild(childId: String): List<Appointment>

    @Query("SELECT * FROM appointments") suspend fun all(): List<Appointment>

    @Query("SELECT * FROM appointments WHERE id=:id") suspend fun get(id: String): Appointment?

    @Query("SELECT COUNT(*) FROM appointments WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Appointment)

    @Query("DELETE FROM appointments WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
