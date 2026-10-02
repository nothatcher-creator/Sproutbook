package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "shoppingItems",
    foreignKeys =
        [
            ForeignKey(
                entity = Child::class,
                parentColumns = ["id"],
                childColumns = ["childId"],
                onDelete = ForeignKey.CASCADE,
            ),
            ForeignKey(
                entity = Inventory::class,
                parentColumns = ["id"],
                childColumns = ["inventoryId"],
                onDelete = ForeignKey.SET_NULL,
            ),
        ],
    indices = [Index("childId"), Index("inventoryId")],
)
data class ShoppingItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "items",
    val checked: Boolean = false,
    val inventoryId: String? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface ShoppingItemDao {
    @Query("SELECT COUNT(*) FROM shoppingItems WHERE childId=:childId AND checked=0")
    fun pendingCount(childId: String): Flow<Int>

    @Query(
        "SELECT * FROM shoppingItems WHERE childId=:childId ORDER BY updatedAt DESC, id DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200): Flow<List<ShoppingItem>>

    @Query("SELECT * FROM shoppingItems WHERE childId=:childId")
    suspend fun forChild(childId: String): List<ShoppingItem>

    @Query("SELECT * FROM shoppingItems") suspend fun all(): List<ShoppingItem>

    @Query("SELECT * FROM shoppingItems WHERE id=:id") suspend fun get(id: String): ShoppingItem?

    @Upsert suspend fun save(value: ShoppingItem)

    @Query("DELETE FROM shoppingItems WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
