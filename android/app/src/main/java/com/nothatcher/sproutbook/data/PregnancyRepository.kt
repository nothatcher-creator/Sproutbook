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
    val existing = db.prepItems().get(p.id)
    require(existing == null || existing.childId == p.childId) { "This item belongs to another child." }
    db.prepItems().save(validPreparation(p))
}

suspend fun FamilyRepository.togglePrep(p: PrepItem) = write {
    val fresh = db.prepItems().get(p.id) ?: p
    require(fresh.childId == p.childId) { "This item belongs to another child." }
    db.prepItems()
        .save(validPreparation(fresh.copy(completed = !fresh.completed)))
}

private fun validPreparation(p: PrepItem): PrepItem {
    require(p.id.isNotBlank() && p.id.length <= 160) { "This preparation item has an invalid ID." }
    validateText("Notes" to p.notes)
    require(p.kind in listOf("Bag", "Note")) { "Choose a supported preparation item." }
    require(p.title.trim().length in 1..160) { "Enter a title between 1 and 160 characters." }
    return p.copy(title = p.title.trim(), updatedAt = System.currentTimeMillis())
}
