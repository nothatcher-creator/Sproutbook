package com.nothatcher.sproutbook.data
val appointmentCategories =
    listOf(
        "Doctor",
        "Dentist",
        "Vaccination",
        "Prenatal",
        "School",
        "Activity",
        "Therapy",
        "Family",
        "Other",
    )

suspend fun FamilyRepository.saveAppointment(value: Appointment) = write {
    validateText(
        "Location" to value.location,
        "Notes" to value.notes,
        "Questions" to value.questions,
        "Results" to value.results,
    )

    require(value.title.trim().length in 1..160) { "Enter an appointment title." }
    require(value.category in appointmentCategories)
    require(value.reminderMinutes in listOf(0, 15, 60, 1440))
    val existing = db.appointments().get(value.id)
    require(existing == null || existing.childId == value.childId) {
        "Create a separate appointment for another child."
    }
    val saved = value.copy(title = value.title.trim(), updatedAt = System.currentTimeMillis())
    db.appointments().save(saved)
    reminders?.schedule(saved)
}

suspend fun FamilyRepository.deleteAppointment(value: Appointment) = write {
    db.appointments().delete(value.id, value.childId)
    reminders?.cancel(value.id)
}
