package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "inventorys",
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
data class Inventory(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val name: String,
    val quantity: Double = 0.0,
    val unit: String = "items",
    val threshold: Double = 0.0,
    val category: String = "Supplies",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventorys WHERE childId=:childId ORDER BY updatedAt DESC LIMIT :limit")
    fun observe(childId: String, limit: Int = 500): Flow<List<Inventory>>

    @Query("SELECT * FROM inventorys WHERE childId=:childId ORDER BY updatedAt DESC")
    suspend fun forChild(childId: String): List<Inventory>

    @Query("SELECT * FROM inventorys") suspend fun all(): List<Inventory>

    @Query("SELECT * FROM inventorys WHERE id=:id") suspend fun get(id: String): Inventory?

    @Query("SELECT COUNT(*) FROM inventorys WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Upsert suspend fun save(value: Inventory)

    @Query("DELETE FROM inventorys WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
