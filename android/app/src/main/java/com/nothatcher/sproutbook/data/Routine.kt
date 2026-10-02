package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "routines",
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
data class Routine(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val title: String,
    val weekdays: Int = 127,
    val timeMinutes: Int = -1,
    val owner: String = "",
    val notes: String = "",
    val active: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface RoutineDao {
    @Query(
        "SELECT * FROM routines WHERE childId=:childId ORDER BY active DESC, timeMinutes, title, id LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200): Flow<List<Routine>>

    @Query("SELECT * FROM routines WHERE childId=:childId")
    suspend fun forChild(childId: String): List<Routine>

    @Query("SELECT * FROM routines") suspend fun all(): List<Routine>

    @Query("SELECT * FROM routines WHERE id=:id") suspend fun get(id: String): Routine?

    @Upsert suspend fun save(value: Routine)

    @Query("DELETE FROM routines WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
