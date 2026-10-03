package com.nothatcher.sproutbook.data

import com.nothatcher.sproutbook.core.ProfileRules
import com.nothatcher.sproutbook.core.HomeOrganizerCatalog
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FamilyRepository(
    val db: AppDatabase,
    val settings: Settings,
    val reminders: com.nothatcher.sproutbook.services.AppointmentReminders? = null,
) {
    private val writes = Mutex()

    suspend fun <T> write(block: suspend () -> T): T = writes.withLock {
        check(!settings.flow.first().grandparent) {
            "Grandparent mode is read-only. Turn it off in Profile & settings to make changes."
        }
        block()
    }

    suspend fun saveChild(child: Child) = write {
        validateText("Notes" to child.notes)

        require(ProfileRules.validName(child.name)) { "Enter a name between 1 and 80 characters." }
        require(child.stage in Stage.entries.map { it.name }) { "Choose a stage." }
        require(child.accent in listOf("Forest", "Moss", "Amber", "Sky")) { "Choose a profile accent." }
        require(child.treeStyle in listOf("Summer", "Autumn", "Night")) { "Choose a memory tree style." }
        require(
            child.birthday == null ||
                ProfileRules.validBirthday(LocalDate.ofEpochDay(child.birthday), LocalDate.now())
        ) {
            "Birthday must be today or earlier."
        }
        // A profile editor can hold an older Child while Today is customized elsewhere.
        val current = db.children().get(child.id)
        val saved = if (current == null) {
            HomeOrganizerCatalog.validate(child.homeCustomization())
            child
        } else {
            child.copy(
                homeSections = current.homeSections,
                homeHiddenSections = current.homeHiddenSections,
                homeQuickActions = current.homeQuickActions,
                homeBackground = current.homeBackground,
                homeBackgroundPhoto = current.homeBackgroundPhoto,
            )
        }
        db.children().save(saved.copy(name = child.name.trim(), updatedAt = System.currentTimeMillis()))
        settings.select(child.id)
    }

    suspend fun deleteChild(id: String) = write {
        val appointments = db.appointments().forChild(id)
        db.children().delete(id)
        appointments.forEach { reminders?.cancel(it.id) }
    }
}
