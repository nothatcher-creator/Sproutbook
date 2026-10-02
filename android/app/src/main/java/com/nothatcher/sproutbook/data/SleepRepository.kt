package com.nothatcher.sproutbook.data

import com.nothatcher.sproutbook.core.SleepMath

suspend fun FamilyRepository.saveSleep(value: Sleep) = write {
    validateText("Notes" to value.notes)

    val now = System.currentTimeMillis()
    val end = value.endsAt ?: Long.MAX_VALUE
    require(value.kind in listOf("Nap", "Night")) { "Choose nap or night." }
    require(
        value.startsAt <= now && (value.endsAt == null || value.endsAt in (value.startsAt + 1)..now)
    ) {
        "Sleep must end after it starts and cannot be in the future."
    }
    require(
        !SleepMath.overlaps(
            value.startsAt,
            end,
            db.sleeps()
                .forChild(value.childId)
                .filter { it.id != value.id }
                .map { it.startsAt to (it.endsAt ?: Long.MAX_VALUE) },
        )
    ) {
        "This overlaps an existing sleep. Edit that record first."
    }
    db.sleeps().save(value.copy(updatedAt = now))
}
