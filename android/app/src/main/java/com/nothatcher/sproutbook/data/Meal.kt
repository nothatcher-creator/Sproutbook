package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "meals",
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
data class Meal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val day: Long,
    val slot: String,
    val title: String,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE childId=:childId ORDER BY day DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE childId=:childId ORDER BY day DESC")
    suspend fun forChild(childId: String): List<Meal>

    @Query("SELECT * FROM meals") suspend fun all(): List<Meal>

    @Query("SELECT * FROM meals WHERE id=:id") suspend fun get(id: String): Meal?

    @Query("SELECT COUNT(*) FROM meals WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Meal)

    @Query("DELETE FROM meals WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
