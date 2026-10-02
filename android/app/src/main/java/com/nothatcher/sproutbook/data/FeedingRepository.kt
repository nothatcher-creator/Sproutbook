package com.nothatcher.sproutbook.data
suspend fun FamilyRepository.saveFeed(value: Feed) = write {
    validateText("Notes" to value.notes)
    require(db.feeds().get(value.id)?.let { it.childId == value.childId } != false) {
        "This feeding belongs to another child."
    }
    val stored = db.milkContainers().forPump(value.id)
    require(stored == null || (value.kind == "Pump" && stored.childId == value.childId)) {
        "This pump is linked to a milk container. Keep its feeding type as Pump."
    }
    if (value.kind == "Pump")
        require(value.leftMl + value.rightMl <= 2000.0) {
            "The combined pump amount must be 2,000 mL or less."
        }

    require(value.kind in listOf("Bottle", "Breast", "Pump")) { "Choose a feeding type." }
    require(value.startsAt <= System.currentTimeMillis()) {
        "Choose a start time no later than now."
    }
    require(
        listOf(value.amountMl, value.leftMl, value.rightMl).all {
            it.isFinite() && it in 0.0..2000.0
        }
    ) {
        "Enter an amount between 0 and 2,000 mL."
    }
    require(value.durationSeconds in 0..43200) { "Enter a duration up to 12 hours." }
    require(value.side in listOf("Left", "Right", "Both"))
    require(value.contents in listOf("Formula", "Breast milk"))
    when (value.kind) {
        "Bottle" -> require(value.amountMl > 0) { "Enter the bottle amount." }
        "Breast" -> require(value.durationSeconds > 0) { "Enter the nursing duration." }
        "Pump" -> require(value.leftMl + value.rightMl > 0) { "Enter at least one pump amount." }
    }
    db.feeds()
        .save(
            value.copy(
                amountMl =
                    if (value.kind == "Pump") value.leftMl + value.rightMl else value.amountMl,
                updatedAt = System.currentTimeMillis(),
            )
        )
}
