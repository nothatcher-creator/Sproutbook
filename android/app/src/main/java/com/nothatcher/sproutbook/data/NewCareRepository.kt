package com.nothatcher.sproutbook.data

import androidx.room.withTransaction

private fun past(time: Long) =
    require(time in 0..System.currentTimeMillis()) { "Choose today or an earlier time." }

suspend fun FamilyRepository.saveDiaper(d: Diaper) = write {
    validateText("Notes" to d.notes)
    require(d.kind in listOf("Wet", "Dirty", "Mixed", "Dry")) { "Choose a diaper type." }
    past(d.recordedAt)
    require(db.diapers().get(d.id)?.let { it.childId == d.childId } != false) {
        "This record belongs to another child."
    }
    db.diapers().save(d.copy(updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.saveBottlePrep(b: BottlePrep) = write {
    validateText("Notes" to b.notes)
    past(b.preparedAt)
    require(b.amountMl.isFinite() && b.amountMl > 0 && b.amountMl <= 2000) {
        "Enter a prepared amount up to 2,000 mL."
    }
    require(b.contents in listOf("Formula", "Breast milk")) { "Choose bottle contents." }
    val old = db.bottlePreps().get(b.id)
    require(old == null || old.childId == b.childId) { "This bottle belongs to another child." }
    require(b.status == (old?.status ?: "Prepared") && b.feedId == old?.feedId) {
        "Use the bottle action to change its status."
    }
    require(
        old == null ||
            old.status == "Prepared" ||
            (old.amountMl == b.amountMl &&
                old.preparedAt == b.preparedAt &&
                old.contents == b.contents)
    ) {
        "Only notes can change after a bottle is used or discarded."
    }
    db.bottlePreps().save(b.copy(updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.finishBottle(
    id: String,
    child: String,
    used: Boolean,
    usedAt: Long = System.currentTimeMillis(),
    consumedMl: Double? = null,
) = write {
    past(usedAt)
    db.withTransaction {
        val b = db.bottlePreps().get(id) ?: error("This bottle no longer exists.")
        require(b.childId == child && b.status == "Prepared") {
            "This bottle has already been recorded or belongs to another child."
        }
        require(!used || usedAt >= b.preparedAt) { "Feeding time must follow preparation." }
        val consumed = consumedMl ?: b.amountMl
        require(!used || (consumed.isFinite() && consumed > 0 && consumed <= b.amountMl)) {
            "Enter the amount actually fed, no more than the prepared amount."
        }
        val feed =
            if (used)
                Feed(
                    childId = child,
                    kind = "Bottle",
                    startsAt = usedAt,
                    amountMl = consumed,
                    contents = b.contents,
                    notes = b.notes,
                )
            else null
        if (feed != null) db.feeds().save(feed)
        db.bottlePreps()
            .save(
                b.copy(
                    status = if (used) "Used" else "Discarded",
                    feedId = feed?.id,
                    updatedAt = System.currentTimeMillis(),
                )
            )
    }
}

suspend fun FamilyRepository.saveShopping(s: ShoppingItem) = write {
    validateText("Unit" to s.unit, "Notes" to s.notes)
    require(s.name.trim().length in 1..120 && s.unit.isNotBlank()) {
        "Enter an item name and unit."
    }
    require(s.quantity.isFinite() && s.quantity > 0 && s.quantity <= 1000000) {
        "Enter a quantity above zero and up to 1,000,000."
    }
    val old = db.shoppingItems().get(s.id)
    require(old == null || old.childId == s.childId) { "This item belongs to another child." }
    require(s.checked == (old?.checked ?: false)) { "Use Mark purchased to complete shopping." }
    require(old?.checked != true || s == old) {
        "Purchased items are read-only; create a new item for another purchase."
    }
    if (s.inventoryId != null) {
        val stock = db.inventorys().get(s.inventoryId)
        require(stock?.childId == s.childId && stock.unit == s.unit) {
            "Choose supplies for this child using the same unit."
        }
    }
    db.shoppingItems().save(s.copy(name = s.name.trim(), updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.addLowStock(child: String) = write {
    db.withTransaction {
        val existing =
            db.shoppingItems()
                .forChild(child)
                .filter { !it.checked }
                .mapNotNull { it.inventoryId }
                .toSet()
        db.inventorys()
            .forChild(child)
            .filter { it.quantity <= it.threshold && it.id !in existing }
            .forEach {
                db.shoppingItems()
                    .save(
                        ShoppingItem(
                            childId = child,
                            name = it.name,
                            quantity = (it.threshold + 1 - it.quantity).coerceIn(1.0, 1000000.0),
                            unit = it.unit,
                            inventoryId = it.id,
                        )
                    )
            }
    }
}

suspend fun FamilyRepository.buyShopping(id: String, child: String) = write {
    db.withTransaction {
        val s = db.shoppingItems().get(id) ?: error("This shopping item no longer exists.")
        require(s.childId == child) { "This item belongs to another child." }
        if (!s.checked) {
            s.inventoryId?.let { link ->
                val stock = db.inventorys().get(link) ?: error("This supply no longer exists.")
                require(stock.childId == child && stock.unit == s.unit) {
                    "The supply unit changed. Edit this shopping item before restocking."
                }
                require(stock.quantity + s.quantity <= 1000000) {
                    "The resulting supply quantity exceeds 1,000,000."
                }
                db.inventorys()
                    .save(
                        stock.copy(
                            quantity = stock.quantity + s.quantity,
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
            }
            db.shoppingItems().save(s.copy(checked = true, updatedAt = System.currentTimeMillis()))
        }
    }
}
