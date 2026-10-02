package com.nothatcher.sproutbook.data

import androidx.room.withTransaction
import com.nothatcher.sproutbook.core.RoutineRules
import java.time.LocalDate

private fun historical(time: Long) =
    require(time in 0..System.currentTimeMillis()) { "Choose today or an earlier time." }

suspend fun FamilyRepository.saveRoutine(r: Routine) = write {
    validateText("Responsible person" to r.owner, "Notes" to r.notes)
    require(r.title.trim().length in 1..120) { "Enter a routine title up to 120 characters." }
    require(RoutineRules.valid(r.weekdays, r.timeMinutes)) {
        "Choose at least one weekday and a valid optional time."
    }
    require(db.routines().get(r.id)?.let { it.childId == r.childId } != false) {
        "This routine belongs to another child."
    }
    db.routines().save(r.copy(title = r.title.trim(), updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.completeRoutine(id: String, child: String, day: Long, done: Boolean) =
    write {
        require(day in -25567..LocalDate.now().toEpochDay()) {
            "Choose today or an earlier completion date."
        }
        db.withTransaction {
            val r = db.routines().get(id) ?: error("This routine no longer exists.")
            require(r.childId == child) { "This routine belongs to another child." }
            if (done) {
                require(r.active && RoutineRules.isDue(r.weekdays, LocalDate.ofEpochDay(day))) {
                    "This routine is not scheduled on this date."
                }
                if (db.routineCompletions().onDay(id, day) == null)
                    db.routineCompletions()
                        .save(RoutineCompletion(childId = child, routineId = id, day = day))
            } else db.routineCompletions().undo(id, day, child)
        }
    }

suspend fun FamilyRepository.savePotty(p: PottyLog) = write {
    validateText("Notes" to p.notes)
    historical(p.recordedAt)
    require(p.kind in listOf("Tried", "Wet", "Dirty", "Mixed", "Accident")) {
        "Choose a visit type."
    }
    require(db.pottyLogs().get(p.id)?.let { it.childId == p.childId } != false) {
        "This visit belongs to another child."
    }
    db.pottyLogs().save(p.copy(updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.saveMilk(m: MilkContainer) = write {
    validateText("Location" to m.location, "Notes" to m.notes)
    require(m.label.trim().length in 1..120) { "Enter a container label up to 120 characters." }
    historical(m.storedAt)
    require(m.amountMl.isFinite() && m.amountMl > 0 && m.amountMl <= 2000) {
        "Enter a stored amount above zero and up to 2,000 mL."
    }
    val old = db.milkContainers().get(m.id)
    require(old == null || old.childId == m.childId) { "This container belongs to another child." }
    require(m.status == (old?.status ?: "Frozen") && m.pumpId == old?.pumpId) {
        "Use the container actions to change status or link a pump."
    }
    require(
        old == null ||
            old.status == "Frozen" ||
            (old.label == m.label &&
                old.storedAt == m.storedAt &&
                old.amountMl == m.amountMl &&
                old.location == m.location)
    ) {
        "Only notes can change after a container is used or discarded."
    }
    require(old?.pumpId == null || old.amountMl == m.amountMl) {
        "A linked container keeps the original pump amount. Add a separate manual container if needed."
    }
    db.milkContainers().save(m.copy(label = m.label.trim(), updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.storePump(id: String, child: String) = write {
    db.withTransaction {
        val pump = db.feeds().get(id) ?: error("This pump session no longer exists.")
        require(pump.childId == child && pump.kind == "Pump") {
            "Choose a pump session for this child."
        }
        require(db.milkContainers().forPump(id) == null) {
            "This pump session has already been stored."
        }
        require(pump.amountMl.isFinite() && pump.amountMl > 0 && pump.amountMl <= 2000) {
            "This pump amount cannot be stored."
        }
        db.milkContainers()
            .save(
                MilkContainer(
                    childId = child,
                    label = "Pump session",
                    storedAt = pump.startsAt,
                    amountMl = pump.amountMl,
                    pumpId = id,
                    notes = pump.notes,
                )
            )
    }
}

suspend fun FamilyRepository.finishMilk(id: String, child: String, used: Boolean) = write {
    val m = db.milkContainers().get(id) ?: error("This container no longer exists.")
    require(m.childId == child && m.status == "Frozen") {
        "This container has already been recorded or belongs to another child."
    }
    db.milkContainers()
        .save(
            m.copy(
                status = if (used) "Used" else "Discarded",
                updatedAt = System.currentTimeMillis(),
            )
        )
}
