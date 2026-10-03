package com.nothatcher.sproutbook.features.pregnancy

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*

internal fun guideItems(childId: String, guide: BirthGuide, saved: List<PrepItem>): List<PrepItem> {
    val prefix = BirthPlanningCatalog.prefix(childId, guide.id)
    val defaults = guide.items.map { item ->
        saved.firstOrNull { it.id == prefix + item.key } ?: PrepItem(
            id = prefix + item.key, childId = childId, kind = "Bag", title = item.title, notes = item.notes,
        )
    }
    return defaults + saved.filter { it.kind == "Bag" && it.id.startsWith(prefix) && it.id !in defaults.map { d -> d.id } }
}

internal fun planItems(childId: String, saved: List<PrepItem>) = BirthPlanCatalog.fields.map { field ->
    val id = BirthPlanCatalog.prefix(childId) + field.key
    saved.firstOrNull { it.id == id } ?: PrepItem(id = id, childId = childId, kind = "Note", title = field.title)
}

@Composable
internal fun DialButton(label: String, number: String = "") {
    val context = LocalContext.current
    Action(label) {
        runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", number.trim(), null))) }
            .onFailure { Toast.makeText(context, "No phone app is available. Use another phone to call for help.", Toast.LENGTH_LONG).show() }
    }
}

@Composable
internal fun BirthUrgentPanel(compact: Boolean = false, emergencyNumber: String = "") {
    var expanded by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Section(BirthPlanningCatalog.emergencyTitle)
            Text(if (compact) "Heavy bleeding, trouble breathing, chest pain, collapse, a seizure, or a newborn not breathing normally or unresponsive: call emergency services now. An imminent birth without qualified help also needs an emergency call. Follow the dispatcher."
                else BirthPlanningCatalog.emergency, color = MaterialTheme.colorScheme.onErrorContainer)
            DialButton(if (emergencyNumber.isBlank()) "Open phone for emergency help" else "Dial local emergency number", emergencyNumber)
            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Hide other warning signs" else "Other urgent warning signs")
            }
            if (expanded) {
                if (compact) Text(BirthPlanningCatalog.emergency)
                Text(BirthPlanningCatalog.urgent)
                SourceLink(BirthPlanningCatalog.labour.label, BirthPlanningCatalog.labour.url)
                SourceLink(BirthPlanningCatalog.warningSigns.label, BirthPlanningCatalog.warningSigns.url)
                SourceLink(BirthPlanningCatalog.newborn.label, BirthPlanningCatalog.newborn.url)
            }
        }
    }
}

@Composable
internal fun PreparationChoices(selected: String, onChoose: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (listOf("Hospital bag", "Birth plan") + BirthPlanningCatalog.guides.map { it.label }).forEach { label ->
            FilterChip(selected = selected == label, onClick = { onChoose(label) },
                label = { Text(label) }, modifier = Modifier.heightIn(min = 48.dp))
        }
    }
}

@Composable
internal fun ChecklistRow(p: PrepItem, vm: FamilyViewModel, readOnly: Boolean, edit: (PrepItem) -> Unit) {
    Panel {
        Row {
            Checkbox(p.completed, {
                vm.perform("Checklist updated") { vm.repo.togglePrep(p) }
            }, enabled = !readOnly, modifier = Modifier.size(48.dp).semantics { contentDescription = "Checklist: ${p.title}" })
            TextButton(onClick = { edit(p) }, modifier = Modifier.weight(1f).heightIn(min = 48.dp)) { Text(p.title) }
        }
        if (p.notes.isNotBlank()) Muted(p.notes)
    }
}

@Composable
internal fun ContractionControl(vm: FamilyViewModel, state: FamilyState, contraction: PregnancyEvent?, now: Long) {
    Panel {
        Section(if (contraction == null) "Contraction timer" else "Contraction in progress")
        Text(contraction?.let { durationText(now - it.startsAt) } ?: "Ready when you are",
            style = MaterialTheme.typography.headlineLarge)
        Action(if (contraction == null) "Start contraction" else "Stop contraction", !state.prefs.grandparent) {
            vm.perform(if (contraction == null) "Contraction started" else "Contraction saved") {
                if (contraction == null) vm.repo.startContraction(state.child!!.id)
                else vm.repo.finishPregnancySession(contraction.id, contraction.childId)
            }
        }
        Muted("The timer records time; it cannot diagnose labour or tell you to delay care. Follow your maternity team's advice and call if you are worried.")
    }
}
