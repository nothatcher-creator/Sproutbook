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
)

@Dao
interface ChildDao {
    @Query("SELECT * FROM children ORDER BY name COLLATE NOCASE") fun observe(): Flow<List<Child>>

    @Query("SELECT * FROM children ORDER BY name COLLATE NOCASE") suspend fun all(): List<Child>

    @Upsert suspend fun save(value: Child)

    @Query("DELETE FROM children WHERE id=:id") suspend fun delete(id: String)
}
