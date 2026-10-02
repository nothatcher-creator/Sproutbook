package com.nothatcher.sproutbook.data
suspend fun FamilyRepository.kick(child: String) = write {
    val active =
        db.pregnancyEvents().forChild(child).firstOrNull { it.kind == "Kicks" && it.endsAt == null }
            ?: PregnancyEvent(
                childId = child,
                kind = "Kicks",
                startsAt = System.currentTimeMillis(),
            )
    require(active.count < 100000) { "Finish this session before starting another." }
    db.pregnancyEvents()
        .save(active.copy(count = active.count + 1, updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.finishPregnancySession(id: String, child: String) = write {
    val active = db.pregnancyEvents().get(id) ?: return@write
    require(active.childId == child)
    if (active.endsAt == null)
        db.pregnancyEvents()
            .save(
                active.copy(
                    endsAt = maxOf(active.startsAt + 1, System.currentTimeMillis()),
                    updatedAt = System.currentTimeMillis(),
                )
            )
}

suspend fun FamilyRepository.startContraction(child: String) = write {
    require(
        db.pregnancyEvents().forChild(child).none { it.kind == "Contraction" && it.endsAt == null }
    ) {
        "A contraction is already running."
    }
    db.pregnancyEvents()
        .save(
            PregnancyEvent(
                childId = child,
                kind = "Contraction",
                startsAt = System.currentTimeMillis(),
            )
        )
}

suspend fun FamilyRepository.savePrep(p: PrepItem) = write {
    validateText("Notes" to p.notes)
    require(p.kind in listOf("Bag", "Note") && p.title.trim().length in 1..160) { "Enter a title." }
    db.prepItems().save(p.copy(title = p.title.trim(), updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.togglePrep(p: PrepItem) = write {
    val fresh = db.prepItems().get(p.id) ?: p
    require(fresh.childId == p.childId)
    db.prepItems()
        .save(fresh.copy(completed = !fresh.completed, updatedAt = System.currentTimeMillis()))
}
