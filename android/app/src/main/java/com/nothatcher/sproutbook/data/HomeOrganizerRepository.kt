package com.nothatcher.sproutbook.data

import android.content.Context
import android.net.Uri
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.core.HomeOrganizerCatalog
import com.nothatcher.sproutbook.services.PhotoStore
import kotlinx.coroutines.flow.first

fun Child.homeCustomization(): HomeCustomization = HomeOrganizerCatalog.decode(
    homeSections,
    homeHiddenSections,
    homeQuickActions,
    homeBackground,
    homeBackgroundPhoto,
)

fun Child.withHomeCustomization(config: HomeCustomization): Child {
    val clean = config.trimmed()
    HomeOrganizerCatalog.validate(clean)
    return copy(
        homeSections = clean.sectionOrder.joinToString(","),
        homeHiddenSections = clean.hiddenSections.sorted().joinToString(","),
        homeQuickActions = clean.quickActions.joinToString(","),
        homeBackground = clean.background,
        homeBackgroundPhoto = clean.backgroundPhoto,
    )
}

suspend fun FamilyRepository.saveHomeCustomization(childId: String, config: HomeCustomization): Unit =
    write { updateHomeCustomization(childId, config) }

/** Stage one chosen image privately; only an explicit organizer save changes the home. */
suspend fun FamilyRepository.importHomeBackground(context: Context, childId: String, uri: Uri): String =
    write {
        require(db.children().get(childId) != null) { "This child profile no longer exists." }
        var created: String? = null
        var retained = false
        try {
            val name = PhotoStore.copy(context, uri)
            created = name
            // Photo decoding can take time; caregiver mode may have changed during that work.
            check(!settings.flow.first().grandparent) {
                "Grandparent mode is read-only. Turn it off in Profile & settings to make changes."
            }
            require(db.children().get(childId) != null) { "This child profile no longer exists." }
            retained = true
            name
        } finally {
            // Old images can still be referenced by an export in progress and are retained.
            if (!retained) created?.let { PhotoStore.file(context, it).delete() }
        }
    }

private suspend fun FamilyRepository.updateHomeCustomization(childId: String, config: HomeCustomization) {
    val clean = config.trimmed()
    HomeOrganizerCatalog.validate(clean)
    val changed = db.children().updateHomeCustomization(
        childId,
        clean.sectionOrder.joinToString(","),
        clean.hiddenSections.sorted().joinToString(","),
        clean.quickActions.joinToString(","),
        clean.background,
        clean.backgroundPhoto,
        System.currentTimeMillis(),
    )
    require(changed == 1) { "This child profile no longer exists." }
}

private fun HomeCustomization.trimmed() = copy(
    sectionOrder = sectionOrder.map { it.trim() },
    hiddenSections = hiddenSections.map { it.trim() }.toSet(),
    quickActions = quickActions.map { it.trim() },
    background = background.trim(),
    backgroundPhoto = backgroundPhoto?.trim(),
)
