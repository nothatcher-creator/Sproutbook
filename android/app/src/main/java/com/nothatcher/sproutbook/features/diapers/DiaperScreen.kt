package com.nothatcher.sproutbook.features.diapers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun DiaperScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    val rows by
        remember(child.id, limit) { vm.repo.db.diapers().observe(child.id, limit + 1) }
            .collectAsStateWithLifecycle(emptyList())
    val today = dateOf(rememberNow(interval = 60000))
    val daily by
        remember(child.id, today) {
                vm.repo.db.diapers().since(child.id, at(today, LocalTime.MIDNIGHT))
            }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<Diaper?>(null) }
    Page("Diapers", "A quick log for little changes") {
        item {
            Panel {
                Section("Today so far")
                Text("${daily.size} changes", style = MaterialTheme.typography.headlineLarge)
                Muted(
                    "${daily.count {it.kind in listOf("Wet","Mixed")}} wet · ${daily.count {it.kind in listOf("Dirty","Mixed")}} dirty · Mixed counts in both"
                )
                rows.firstOrNull()?.let {
                    Muted("Last change · ${stamp(it.recordedAt,state.prefs.time24)}")
                }
            }
        }
        item {
            Action("Log wet diaper", !state.prefs.grandparent) {
                vm.perform("Wet diaper logged") {
                    vm.repo.saveDiaper(
                        Diaper(childId = child.id, recordedAt = System.currentTimeMillis())
                    )
                }
            }
            Row {
                OutlinedButton(
                    onClick = {
                        vm.perform("Dirty diaper logged") {
                            vm.repo.saveDiaper(
                                Diaper(
                                    childId = child.id,
                                    recordedAt = System.currentTimeMillis(),
                                    kind = "Dirty",
                                )
                            )
                        }
                    },
                    enabled = !state.prefs.grandparent,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Log dirty")
                }
                OutlinedButton(
                    onClick = {
                        vm.perform("Mixed diaper logged") {
                            vm.repo.saveDiaper(
                                Diaper(
                                    childId = child.id,
                                    recordedAt = System.currentTimeMillis(),
                                    kind = "Mixed",
                                )
                            )
                        }
                    },
                    enabled = !state.prefs.grandparent,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Log mixed")
                }
            }
            TextButton(
                onClick = {
                    edit = Diaper(childId = child.id, recordedAt = System.currentTimeMillis())
                },
                enabled = !state.prefs.grandparent,
            ) {
                Text("Add an earlier change")
            }
        }
        if (rows.isEmpty())
            item {
                Empty(
                    "Little changes, remembered",
                    "Log a wet, dirty, mixed or dry diaper. Add time and notes when needed.",
                )
            }
        items(rows.take(limit), key = { it.id }) { d ->
            EntryRow("${d.kind} diaper", stamp(d.recordedAt, state.prefs.time24)) { edit = d }
        }
        if (rows.size > limit) item { Action("Load older diaper logs") { limit += 200 } }
    }
    edit?.let { d ->
        var kind by remember(d.id) { mutableStateOf(d.kind) }
        var notes by remember(d.id) { mutableStateOf(d.notes) }
        var date by remember(d.id) { mutableStateOf(dateOf(d.recordedAt)) }
        var time by
            remember(d.id) {
                mutableStateOf(
                    Instant.ofEpochMilli(d.recordedAt).atZone(ZoneId.systemDefault()).toLocalTime()
                )
            }
        var deleting by remember { mutableStateOf(false) }
        Editor("Diaper change", { edit = null }) {
            if (state.prefs.grandparent) {
                Section("${d.kind} diaper")
                Muted(stamp(d.recordedAt, state.prefs.time24))
                Text(d.notes)
            } else {
                Choices(listOf("Wet", "Dirty", "Mixed", "Dry"), kind) { kind = it }
                DateButton("Change date", date) { date = it }
                TimeButton(time, state.prefs.time24) { time = it }
                Field("Diaper notes", notes, { notes = it }, lines = 2)
                Action("Save diaper") {
                    vm.perform("Diaper saved") {
                        vm.repo.saveDiaper(
                            d.copy(kind = kind, notes = notes, recordedAt = at(date, time))
                        )
                        edit = null
                    }
                }
                if (rows.any { it.id == d.id })
                    TextButton(onClick = { deleting = true }) { Text("Delete diaper") }
            }
        }
        if (deleting)
            ConfirmDelete("this diaper log", { deleting = false }) {
                vm.perform("Diaper removed") {
                    vm.repo.write { vm.repo.db.diapers().delete(d.id, child.id) }
                    edit = null
                }
            }
    }
}
