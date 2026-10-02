package com.nothatcher.sproutbook.data

import androidx.room.withTransaction
import com.nothatcher.sproutbook.core.TreeLayout

val memoryCategories =
    listOf(
        "General",
        "First",
        "Milestone",
        "Funny moment",
        "Health",
        "Photo memory",
        "Achievement",
        "Family moment",
    )

suspend fun FamilyRepository.saveMemory(value: Memory): Memory = write {
    validateText("Notes" to value.notes)

    require(value.title.trim().length in 1..160) {
        "Give this memory a title (up to 160 characters)."
    }
    require(value.category in memoryCategories) { "Choose a category." }
    val existing = db.memorys().get(value.id)
    require(existing == null || existing.childId == value.childId) {
        "This memory belongs to another child."
    }
    val slot =
        existing?.let { it.chapter to it.anchor }
            ?: TreeLayout.next(db.memorys().forChild(value.childId).map { it.chapter to it.anchor })
    val saved =
        value.copy(
            title = value.title.trim(),
            chapter = slot.first,
            anchor = slot.second,
            updatedAt = System.currentTimeMillis(),
        )
    db.memorys().save(saved)
    saved
}

suspend fun FamilyRepository.arrangeMemory(childId: String, id: String, target: Int) = write {
    require(target in TreeLayout.anchors.indices)
    db.withTransaction {
        val current = db.memorys().get(id) ?: error("This memory no longer exists.")
        require(current.childId == childId)
        val siblings = db.memorys().forChild(childId).filter { it.chapter == current.chapter }
        val slots = TreeLayout.move(siblings.associate { it.id to it.anchor }, id, target)
        siblings
            .filter { it.anchor != slots[it.id] }
            .forEach {
                db.memorys()
                    .save(
                        it.copy(
                            anchor = slots.getValue(it.id),
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
            }
    }
}
