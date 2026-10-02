package com.nothatcher.sproutbook.features.potty

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.RecordTime
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun PottyScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    val rows by
        remember(child.id, limit) { vm.repo.db.pottyLogs().observe(child.id, limit + 1) }
            .collectAsStateWithLifecycle(emptyList())
    val today = dateOf(rememberNow(interval = 60000))
    val daily by
        remember(child.id, today) {
                vm.repo.db.pottyLogs().since(child.id, at(today, LocalTime.MIDNIGHT))
            }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<PottyLog?>(null) }
    fun quick(kind: String) {
        vm.perform("Potty visit logged") {
            vm.repo.savePotty(
                PottyLog(childId = child.id, recordedAt = System.currentTimeMillis(), kind = kind)
            )
        }
    }
    Page("Potty journal", "Small steps, at their own pace") {
        item {
            Panel {
                Section("Today’s little visits")
                Text("${daily.size} recorded", style = MaterialTheme.typography.headlineMedium)
                Muted(
                    "A gentle observation journal. Readiness and progress vary; this is not a scorecard."
                )
            }
        }
        item {
            Action("Log wet potty", !state.prefs.grandparent) { quick("Wet") }
            Row {
                OutlinedButton(
                    onClick = { quick("Dirty") },
                    enabled = !state.prefs.grandparent,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Log dirty potty")
                }
                OutlinedButton(
                    onClick = { quick("Tried") },
                    enabled = !state.prefs.grandparent,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Log a try")
                }
            }
            TextButton(
                onClick = {
                    edit = PottyLog(childId = child.id, recordedAt = System.currentTimeMillis())
                },
                enabled = !state.prefs.grandparent,
            ) {
                Text("Add a potty visit")
            }
        }
        if (rows.isEmpty())
            item {
                Empty(
                    "Their own little rhythm",
                    "Record a try, a wet or dirty visit, or an accident. Add notes when you need them.",
                )
            }
        items(rows.take(limit), key = { it.id }) { p ->
            EntryRow("${p.kind} potty visit", stamp(p.recordedAt, state.prefs.time24)) { edit = p }
        }
        if (rows.size > limit) item { Action("Load older potty visits") { limit += 200 } }
    }
    edit?.let { p ->
        var kind by remember(p.id) { mutableStateOf(p.kind) }
        var notes by remember(p.id) { mutableStateOf(p.notes) }
        val editZone = remember(p.id) { ZoneId.systemDefault() }
        val initial = Instant.ofEpochMilli(p.recordedAt).atZone(editZone)
        var date by remember(p.id) { mutableStateOf(initial.toLocalDate()) }
        var time by remember(p.id) { mutableStateOf(initial.toLocalTime()) }
        var deleting by remember { mutableStateOf(false) }
        Editor("A little potty visit", { edit = null }) {
            if (state.prefs.grandparent) {
                Section("${p.kind} potty visit")
                Muted(stamp(p.recordedAt, state.prefs.time24))
                Text(p.notes)
            } else {
                Choices(listOf("Tried", "Wet", "Dirty", "Mixed", "Accident"), kind) { kind = it }
                DateButton("Visit date", date) { date = it }
                TimeButton(time, state.prefs.time24) { time = it }
                Field("Potty notes", notes, { notes = it }, lines = 2)
                Action("Save potty visit") {
                    vm.perform("Potty visit saved") {
                        vm.repo.savePotty(
                            p.copy(
                                kind = kind,
                                notes = notes,
                                recordedAt = RecordTime.edited(p.recordedAt, date, time, editZone),
                            )
                        )
                        edit = null
                    }
                }
                if (rows.any { it.id == p.id })
                    TextButton(onClick = { deleting = true }) { Text("Delete potty visit") }
            }
        }
        if (deleting)
            ConfirmDelete("this potty visit", { deleting = false }) {
                vm.perform("Potty visit removed") {
                    vm.repo.write { vm.repo.db.pottyLogs().delete(p.id, child.id) }
                    edit = null
                }
            }
    }
}
