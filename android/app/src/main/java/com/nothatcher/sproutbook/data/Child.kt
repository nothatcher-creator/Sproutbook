package com.nothatcher.sproutbook.data

import androidx.room.*
import java.util.UUID
import kotlinx.coroutines.flow.Flow

enum class Stage(val label: String) {
    PREGNANCY("Pregnancy"),
    BABY("Baby"),
    TODDLER("Toddler"),
    CHILD("Child"),
    TEEN("Teen"),
}

@Entity(tableName = "children")
data class Child(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val stage: String = "BABY",
    val birthday: Long? = null,
    val dueDate: Long? = null,
    val avatar: String? = null,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "'Forest'") val accent: String = "Forest",
    @ColumnInfo(defaultValue = "'Summer'") val treeStyle: String = "Summer",
    @ColumnInfo(defaultValue = "''") val homeSections: String = "",
    @ColumnInfo(defaultValue = "''") val homeHiddenSections: String = "",
    @ColumnInfo(defaultValue = "'memory,schedule'") val homeQuickActions: String = "memory,schedule",
    @ColumnInfo(defaultValue = "'woodland'") val homeBackground: String = "woodland",
    @ColumnInfo(defaultValue = "NULL") val homeBackgroundPhoto: String? = null,
)

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY name COLLATE NOCASE") fun observe(): Flow<List<Child>>

    @Query("SELECT * FROM children ORDER BY name COLLATE NOCASE") suspend fun all(): List<Child>

    @Query("SELECT * FROM children WHERE id=:id") suspend fun get(id: String): Child?

    @Upsert suspend fun save(value: Child)

    @Query(
        "UPDATE children SET homeSections=:sections, homeHiddenSections=:hidden, " +
            "homeQuickActions=:actions, homeBackground=:background, " +
            "homeBackgroundPhoto=:photo, updatedAt=:updatedAt WHERE id=:id"
    )
    suspend fun updateHomeCustomization(
        id: String,
        sections: String,
        hidden: String,
        actions: String,
        background: String,
        photo: String?,
        updatedAt: Long,
    ): Int

    @Query("DELETE FROM children WHERE id=:id") suspend fun delete(id: String)
}
