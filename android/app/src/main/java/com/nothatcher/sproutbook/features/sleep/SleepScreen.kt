package com.nothatcher.sproutbook.features.sleep

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.SleepMath
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.SoundService
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun SleepScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    val rows by
        remember(child.id, limit) { vm.repo.db.sleeps().history(child.id, "All", limit + 1) }
            .collectAsStateWithLifecycle(emptyList())
    val day = dateOf(rememberNow(interval = 60000))
    val start = at(day, LocalTime.MIDNIGHT)
    val today by
        remember(child.id, day) { vm.repo.db.sleeps().since(child.id, start) }
            .collectAsStateWithLifecycle(emptyList())
    val naps by
        remember(child.id) { vm.repo.db.sleeps().recentNaps(child.id) }
            .collectAsStateWithLifecycle(emptyList())
    val running = today.firstOrNull { it.endsAt == null }
    val now = rememberNow(running != null)
    var kind by remember { mutableStateOf("Nap") }
    var edit by remember { mutableStateOf<Sleep?>(null) }
    var soundKind by rememberSaveable { mutableStateOf(SoundService.state.value.kind) }
    var soundVolume by rememberSaveable { mutableFloatStateOf(SoundService.state.value.volume) }
    var soundMinutes by rememberSaveable { mutableIntStateOf(SoundService.state.value.minutes) }
    var tab by rememberSaveable { mutableStateOf("Sleep journal") }
    Page("Rest & rhythm", "Sleep tracking and a little background calm") {
        item { Choices(listOf("Sleep journal", "Sound machine"), tab) { tab = it } }
        if (tab == "Sound machine") {
            item { SoundPanel(soundKind, soundVolume, soundMinutes,
                { soundKind = it }, { soundVolume = it }, { soundMinutes = it }) }
        } else {
        item {
            Panel {
                Section("Sleep today")
                Text(
                    durationText(
                        SleepMath.total(today.map { it.startsAt to (it.endsAt ?: now) }, start, now)
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Muted(
                    rows.firstOrNull()?.let {
                        "Last sleep · ${stamp(it.startsAt,state.prefs.time24)}"
                    } ?: "Your sleep history starts here."
                )
                if (naps.isNotEmpty())
                    Muted(
                        "Average recent nap · ${durationText(naps.map{it.endsAt!!-it.startsAt}.average().toLong())}"
                    )
                if (running != null) {
                    Section("${running.kind} in progress · ${durationText(now-running.startsAt)}")
                    Action("Stop sleep", !state.prefs.grandparent) {
                        // The timer and history give immediate feedback without covering controls.
                        vm.perform("") {
                            vm.repo.saveSleep(running.copy(endsAt = System.currentTimeMillis()))
                        }
                    }
                } else {
                    Choices(listOf("Nap", "Night"), kind) { kind = it }
                    Action("Start ${kind.lowercase()}", !state.prefs.grandparent) {
                        vm.perform("") {
                            vm.repo.saveSleep(
                                Sleep(
                                    childId = child.id,
                                    startsAt = System.currentTimeMillis(),
                                    kind = kind,
                                )
                            )
                        }
                    }
                }
                TextButton(
                    onClick = {
                        edit =
                            Sleep(
                                childId = child.id,
                                startsAt = System.currentTimeMillis() - 3600000,
                                endsAt = System.currentTimeMillis(),
                            )
                    },
                    enabled = !state.prefs.grandparent,
                ) {
                    Text("Add sleep manually")
                }
            }
        }
        item { EntryRow("Open sound machine", "Eight quiet backdrops · Timer · Screen-off playback") { tab = "Sound machine" } }
        item { Section("Recent sleep") }
        if (rows.isEmpty())
            item { Empty("Room for rest", "Start a timer or add a sleep you remember.") }
        items(rows.take(limit), key = { it.id }) { s ->
            EntryRow(
                "${s.kind} · ${s.endsAt?.let{durationText(it-s.startsAt)} ?: "In progress"}",
                stamp(s.startsAt, state.prefs.time24),
            ) {
                edit = s
            }
        }
        if (rows.size > limit) item { Action("Load older sleep records") { limit += 200 } }
        item {
            Panel {
                Section("A safer sleep space")
                Muted(
                    "Place babies on their back on a firm, flat sleep surface, with no pillows, loose blankets or toys. Keep sound quiet and the device well away from the cot; a sound machine does not make a sleep space safer."
                )
                SourceLink(
                    "Health Canada · Safe sleep",
                    "https://www.canada.ca/en/health-canada/services/safe-sleep/safe-sleep-tips.html",
                )
            }
        }
        }
    }
    edit?.let { s -> SleepEditor(s, rows.any { it.id == s.id }, vm, state) { edit = null } }
}

@Composable
private fun SleepEditor(
    s: Sleep,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    var kind by remember { mutableStateOf(s.kind) }
    var sd by remember { mutableStateOf(dateOf(s.startsAt)) }
    var st by remember {
        mutableStateOf(
            Instant.ofEpochMilli(s.startsAt).atZone(ZoneId.systemDefault()).toLocalTime()
        )
    }
    val end = s.endsAt ?: System.currentTimeMillis()
    var ed by remember { mutableStateOf(dateOf(end)) }
    var et by remember {
        mutableStateOf(Instant.ofEpochMilli(end).atZone(ZoneId.systemDefault()).toLocalTime())
    }
    var notes by remember { mutableStateOf(s.notes) }
    var deleting by remember { mutableStateOf(false) }
    Editor("Sleep record", close) {
        if (state.prefs.grandparent) {
            Section(s.kind)
            Text(stamp(s.startsAt, state.prefs.time24))
            Text(notes)
        } else {
            Choices(listOf("Nap", "Night"), kind) { kind = it }
            DateButton("Started", sd) { sd = it }
            TimeButton(st, state.prefs.time24) { st = it }
            DateButton("Ended", ed) { ed = it }
            TimeButton(et, state.prefs.time24) { et = it }
            Field("Notes", notes, { notes = it }, lines = 2)
            Action("Save sleep") {
                vm.perform {
                    vm.repo.saveSleep(
                        s.copy(
                            kind = kind,
                            startsAt = at(sd, st),
                            endsAt = at(ed, et),
                            notes = notes,
                        )
                    )
                    close()
                }
            }
            if (existing && !state.prefs.grandparent)
                TextButton(onClick = { deleting = true }) { Text("Delete sleep") }
        }
    }
    if (deleting)
        ConfirmDelete("this sleep", { deleting = false }) {
            vm.perform("Sleep deleted") {
                vm.repo.write { vm.repo.db.sleeps().delete(s.id, s.childId) }
                close()
            }
        }
}
