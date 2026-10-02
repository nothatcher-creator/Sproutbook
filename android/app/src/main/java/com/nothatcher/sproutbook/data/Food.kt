package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "foods",
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
data class Food(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val name: String,
    val introducedOn: Long,
    val liking: String = "Unsure",
    val reaction: String = "",
    val allergen: String = "None",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods WHERE childId=:childId ORDER BY introducedOn DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Food>>

    @Query("SELECT * FROM foods WHERE childId=:childId ORDER BY introducedOn DESC")
    suspend fun forChild(childId: String): List<Food>

    @Query("SELECT * FROM foods") suspend fun all(): List<Food>

    @Query("SELECT * FROM foods WHERE id=:id") suspend fun get(id: String): Food?

    @Query("SELECT COUNT(*) FROM foods WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Food)

    @Query("DELETE FROM foods WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
