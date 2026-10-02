package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "routineCompletions",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = Routine::class,
                parentColumns = ["id"],
                childColumns = ["routineId"],
                onDelete = ForeignKey.CASCADE,
            ),
        ],
    indices = [Index("childId"), Index(value = ["routineId", "day"], unique = true)],
)
data class RoutineCompletion(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val routineId: String,
    val day: Long,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface RoutineCompletionDao {
    @Query(
        "SELECT * FROM routineCompletions WHERE childId=:childId ORDER BY day DESC, id DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200): Flow<List<RoutineCompletion>>

    @Query("SELECT * FROM routineCompletions WHERE childId=:childId")
    suspend fun forChild(childId: String): List<RoutineCompletion>

    @Query("SELECT * FROM routineCompletions") suspend fun all(): List<RoutineCompletion>

    @Query("SELECT * FROM routineCompletions WHERE id=:id")
    suspend fun get(id: String): RoutineCompletion?

    @Upsert suspend fun save(value: RoutineCompletion)

    @Query("DELETE FROM routineCompletions WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)

    @Query("SELECT * FROM routineCompletions WHERE childId=:childId AND day=:day")
    fun forDay(childId: String, day: Long): Flow<List<RoutineCompletion>>

    @Query("SELECT * FROM routineCompletions WHERE routineId=:routineId AND day=:day")
    suspend fun onDay(routineId: String, day: Long): RoutineCompletion?

    @Query(
        "DELETE FROM routineCompletions WHERE routineId=:routineId AND day=:day AND childId=:childId"
    )
    suspend fun undo(routineId: String, day: Long, childId: String)
}
