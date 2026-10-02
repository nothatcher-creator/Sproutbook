package com.nothatcher.sproutbook.data

import androidx.room.withTransaction
import com.nothatcher.sproutbook.core.*
import java.time.LocalDate

suspend fun FamilyRepository.saveTooth(t: Tooth) = write {
    require(
        DevelopmentRules.validTooth(
            t.toothIndex,
            t.stage,
            t.eruptedOn,
            LocalDate.now().toEpochDay(),
        )
    ) {
        "Choose a valid tooth stage and a date no later than today."
    }
    db.tooths()
        .save(
            t.copy(
                id = "${t.childId}-tooth-${t.toothIndex}",
                updatedAt = System.currentTimeMillis(),
            )
        )
}

suspend fun FamilyRepository.saveMilestone(m: Milestone, makeMemory: Boolean) = write {
    validateText("Description" to m.description, "Notes" to m.notes)

    require(m.title.trim().length in 1..160) { "Give this milestone a title." }
    require(m.stage in Stage.entries.map { it.name })
    require(m.completedOn == null || m.completedOn <= LocalDate.now().toEpochDay()) {
        "Choose today or an earlier date."
    }
    db.withTransaction {
        var saved = m.copy(title = m.title.trim(), updatedAt = System.currentTimeMillis())
        if (makeMemory && m.completedOn != null) {
            var id = m.memoryId ?: DevelopmentRules.memoryId(m.id)
            var existing = db.memorys().get(id)
            if (existing != null && existing.childId != m.childId) {
                id = java.util.UUID.randomUUID().toString()
                existing = null
            }
            if (existing == null) {
                val slot =
                    TreeLayout.next(
                        db.memorys().forChild(m.childId).map { it.chapter to it.anchor }
                    )
                db.memorys()
                    .save(
                        Memory(
                            id = id,
                            childId = m.childId,
                            title = m.title,
                            occurredOn = m.completedOn,
                            category = "Milestone",
                            notes = m.notes,
                            chapter = slot.first,
                            anchor = slot.second,
                        )
                    )
            }
            saved = saved.copy(memoryId = id)
        }
        db.milestones().save(saved)
    }
}
