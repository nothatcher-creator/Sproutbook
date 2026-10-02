package com.nothatcher.sproutbook.data

import com.nothatcher.sproutbook.core.HouseholdRules

val healthKinds =
    listOf(
        "Weight",
        "Height",
        "Temperature",
        "Medication note",
        "Symptom",
        "Vaccination",
        "Doctor note",
        "Diaper",
        "Other",
    )

suspend fun FamilyRepository.saveInventory(i: Inventory) = write {
    validateText("Unit" to i.unit, "Category" to i.category)
    require(i.name.trim().length in 1..120 && i.unit.trim().isNotBlank()) {
        "Enter a supply name and unit."
    }
    require(HouseholdRules.validStock(i.quantity, i.threshold)) {
        "Quantities must be between 0 and 1,000,000."
    }
    db.inventorys().save(i.copy(name = i.name.trim(), updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.adjustInventory(id: String, child: String, delta: Double) = write {
    val fresh = db.inventorys().get(id) ?: error("This supply no longer exists.")
    require(fresh.childId == child)
    db.inventorys()
        .save(
            fresh.copy(
                quantity = HouseholdRules.quantity(fresh.quantity, delta),
                updatedAt = System.currentTimeMillis(),
            )
        )
}

suspend fun FamilyRepository.saveEmergency(c: EmergencyCard) = write {
    validateText(
        "Allergies" to c.allergies,
        "Medications" to c.medications,
        "Conditions" to c.conditions,
        "Clinic" to c.clinic,
        "Doctor phone" to c.doctorPhone,
        "Emergency contacts" to c.contacts,
        "Notes" to c.notes,
    )
    db.emergencyCards().save(c.copy(id = c.childId, updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.saveHealth(h: HealthRecord) = write {
    validateText("Value / observation" to h.value, "Unit" to h.unit, "Notes" to h.notes)

    require(h.title.trim().length in 1..160 && h.kind in healthKinds) {
        "Enter a title and choose a record type."
    }
    require(h.recordedAt <= System.currentTimeMillis()) { "Choose today or an earlier time." }
    if (h.kind in listOf("Weight", "Height", "Temperature")) {
        val v = h.value.replace(',', '.').toDoubleOrNull()
        require(v != null && v.isFinite() && v > 0 && v < 10000) {
            "Enter a valid positive measurement."
        }
        require(h.unit.isNotBlank()) { "Enter the measurement unit." }
    }
    if (h.appointmentId != null)
        require(db.appointments().get(h.appointmentId)?.childId == h.childId) {
            "Choose an appointment for this child."
        }
    db.healthRecords().save(h.copy(title = h.title.trim(), updatedAt = System.currentTimeMillis()))
}
