package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "emergencyCards",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            )
        ],
    indices = [Index(value = ["childId"], unique = true)],
)
data class EmergencyCard(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val allergies: String = "",
    val medications: String = "",
    val conditions: String = "",
    val clinic: String = "",
    val doctorPhone: String = "",
    val contacts: String = "",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface EmergencyCardDao {
    @Query(
        "SELECT * FROM emergencyCards WHERE childId=:childId ORDER BY updatedAt DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 500): Flow<List<EmergencyCard>>

    @Query("SELECT * FROM emergencyCards WHERE childId=:childId ORDER BY updatedAt DESC")
    suspend fun forChild(childId: String): List<EmergencyCard>

    @Query("SELECT * FROM emergencyCards") suspend fun all(): List<EmergencyCard>

    @Query("SELECT * FROM emergencyCards WHERE id=:id") suspend fun get(id: String): EmergencyCard?

    @Query("SELECT COUNT(*) FROM emergencyCards WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: EmergencyCard)

    @Query("DELETE FROM emergencyCards WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
