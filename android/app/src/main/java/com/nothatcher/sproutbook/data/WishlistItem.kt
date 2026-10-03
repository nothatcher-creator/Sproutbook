package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "wishlistItems",
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
data class WishlistItem(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val childId: String,
    val title: String,
    val notes: String = "",
    val link: String = "",
    val obtained: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Dao
interface WishlistItemDao {
    @Query(
        "SELECT * FROM wishlistItems WHERE childId=:childId AND (:filter='All' OR (:filter='Wanted' AND obtained=0) OR (:filter='Obtained' AND obtained=1)) ORDER BY obtained, updatedAt DESC, id DESC LIMIT :limit"
    )
    fun observe(childId: String, limit: Int = 200, filter: String = "All"): Flow<List<WishlistItem>>

    @Query("SELECT COUNT(*) FROM wishlistItems WHERE childId=:childId")
    fun count(childId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM wishlistItems WHERE childId=:childId AND obtained=1")
    fun obtainedCount(childId: String): Flow<Int>

    @Query("SELECT * FROM wishlistItems WHERE childId=:childId")
    suspend fun forChild(childId: String): List<WishlistItem>

    @Query("SELECT * FROM wishlistItems") suspend fun all(): List<WishlistItem>

    @Query("SELECT * FROM wishlistItems WHERE id=:id") suspend fun get(id: String): WishlistItem?

    @Upsert suspend fun save(value: WishlistItem)

    @Query("DELETE FROM wishlistItems WHERE id=:id AND childId=:childId")
    suspend fun delete(id: String, childId: String)
}
