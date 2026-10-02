package com.nothatcher.sproutbook.features.health

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun HealthScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit = {}) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    var filter by rememberSaveable { mutableStateOf("All") }
    val rows by
        remember(child.id, filter, limit) {
                vm.repo.db.healthRecords().history(child.id, filter, limit + 1)
            }
            .collectAsStateWithLifecycle(emptyList())
    val appointments by
        remember(child.id) { vm.repo.db.appointments().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<HealthRecord?>(null) }
    Page("Health journal", "Observations and records for better care conversations") {
        item { EntryRow("Growth journal", "Recorded weight and height trends") { go("growth") } }
        item {
            Action("Add health record", !state.prefs.grandparent) {
                edit =
                    HealthRecord(
                        childId = child.id,
                        kind = "Doctor note",
                        recordedAt = System.currentTimeMillis(),
                        title = "",
                    )
            }
            Choices(listOf("All") + healthKinds, filter) {
                filter = it
                limit = 200
            }
            Muted(
                "Keep observations and clinician instructions here. This journal does not diagnose illness or calculate medicine doses."
            )
        }
        val shown = rows.take(limit)
        if (shown.isEmpty())
            item {
                Empty(
                    "Their care, kept together",
                    "Log measurements, symptoms, vaccination records, nappies or visit notes.",
                )
            }
        items(shown, key = { it.id }) { h ->
            EntryRow(h.title, "${h.kind} · ${stamp(h.recordedAt,state.prefs.time24)}") { edit = h }
        }
        if (rows.size > limit) item { Action("Load older health records") { limit += 200 } }
    }
    edit?.let { h ->
        var title by remember { mutableStateOf(h.title) }
        var kind by remember { mutableStateOf(h.kind) }
        var value by remember { mutableStateOf(h.value) }
        var unit by remember { mutableStateOf(h.unit) }
        var notes by remember { mutableStateOf(h.notes) }
        var appointment by remember { mutableStateOf(h.appointmentId) }
        var date by remember { mutableStateOf(dateOf(h.recordedAt)) }
        var time by remember {
            mutableStateOf(
                Instant.ofEpochMilli(h.recordedAt).atZone(ZoneId.systemDefault()).toLocalTime()
            )
        }
        var deleting by remember { mutableStateOf(false) }
        Editor("Health record", { edit = null }) {
            if (state.prefs.grandparent) {
                Section(title)
                Muted("$kind · ${stamp(h.recordedAt,state.prefs.time24)}")
                Text("$value $unit")
                Text(notes)
                appointments
                    .firstOrNull { it.id == appointment }
                    ?.let { Muted("Visit · ${it.title}") }
            } else {
                Choices(healthKinds, kind) { kind = it }
                Field("Title", title, { title = it })
                Field(
                    "Value / observation",
                    value,
                    { value = it },
                    numeric = kind in listOf("Weight", "Height", "Temperature"),
                )
                if (kind == "Diaper") Choices(listOf("Wet", "Dirty", "Mixed"), value) { value = it }
                Field("Unit (if applicable)", unit, { unit = it })
                DateButton("Date", date) { date = it }
                TimeButton(time, state.prefs.time24) { time = it }
                Field("Notes / clinician instructions", notes, { notes = it }, lines = 3)
                Section("Linked appointment (optional)")
                Choices(
                    listOf("None") + appointments.map { "${it.title} · ${dateOf(it.startsAt)}" },
                    appointments
                        .firstOrNull { it.id == appointment }
                        ?.let { "${it.title} · ${dateOf(it.startsAt)}" } ?: "None",
                ) { label ->
                    appointment =
                        appointments
                            .firstOrNull { "${it.title} · ${dateOf(it.startsAt)}" == label }
                            ?.id
                }
                Action("Save health record") {
                    vm.perform {
                        vm.repo.saveHealth(
                            h.copy(
                                title = title,
                                kind = kind,
                                value = value,
                                unit = unit,
                                notes = notes,
                                recordedAt = at(date, time),
                                appointmentId = appointment,
                            )
                        )
                        edit = null
                    }
                }
                if (rows.any { it.id == h.id })
                    TextButton(onClick = { deleting = true }) { Text("Delete health record") }
            }
        }
        if (deleting)
            ConfirmDelete("this health record", { deleting = false }) {
                vm.perform("Record removed") {
                    vm.repo.write { vm.repo.db.healthRecords().delete(h.id, h.childId) }
                    edit = null
                }
            }
    }
}
