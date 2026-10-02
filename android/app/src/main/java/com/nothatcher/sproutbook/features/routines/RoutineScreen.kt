package com.nothatcher.sproutbook.features.routines

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.RoutineRules
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun RoutineScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    val today = dateOf(rememberNow(interval = 60000))
    var day by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }
    var limit by rememberSaveable { mutableIntStateOf(200) }
    val rows by
        remember(child.id) { vm.repo.db.routines().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    val done by
        remember(child.id, day) { vm.repo.db.routineCompletions().forDay(child.id, day) }
            .collectAsStateWithLifecycle(emptyList())
    val history by
        remember(child.id, limit) { vm.repo.db.routineCompletions().observe(child.id, limit + 1) }
            .collectAsStateWithLifecycle(emptyList())
    val date = LocalDate.ofEpochDay(day)
    val doneIds = remember(done) { done.map { it.routineId }.toSet() }
    val due =
        remember(rows, day, doneIds) {
            rows.filter { (it.active && RoutineRules.isDue(it.weekdays, date)) || it.id in doneIds }
        }
    var edit by remember { mutableStateOf<Routine?>(null) }
    Page("Routines & responsibilities", "Small rhythms for every growing chapter") {
        item {
            Panel {
                Section(if (date == today) "A little structure for today" else "Your daily journal")
                Text(
                    "${due.count{it.id in doneIds}} of ${due.size} completed",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Muted(
                    "Flexible routines, shared responsibilities. Choose what works for your family."
                )
                DateButton("Routine date", date) { day = it.toEpochDay() }
                if (date != today)
                    TextButton(onClick = { day = today.toEpochDay() }) { Text("Back to today") }
            }
        }
        item {
            Action("Add routine", !state.prefs.grandparent) {
                edit = Routine(childId = child.id, title = "")
            }
        }
        if (due.isEmpty())
            item {
                Empty(
                    "Room for your own rhythm",
                    "No routines scheduled on this date. Add a routine or choose another day.",
                )
            }
        items(due, key = { "due-${it.id}" }) { r ->
            Panel {
                EntryRow(r.title, routineDetail(r, state.prefs.time24)) { edit = r }
                val completed = r.id in doneIds
                Muted(if (completed) "Completed on $date" else "Still to do · $date")
                Action(
                    if (completed) "Undo ${r.title}" else "Complete ${r.title}",
                    !state.prefs.grandparent && date <= today,
                ) {
                    vm.perform("") { vm.repo.completeRoutine(r.id, child.id, day, !completed) }
                }
            }
        }
        if (rows.isNotEmpty()) item { Section("Manage your routines") }
        items(rows, key = { "manage-${it.id}" }) { r ->
            EntryRow(
                "Manage · ${r.title}",
                if (r.active) weekdayNames(r.weekdays) else "Paused · Your history stays here",
            ) {
                edit = r
            }
        }
        if (history.isNotEmpty()) item { Section("Completion history") }
        items(history.take(limit), key = { "history-${it.id}" }) { c ->
            EntryRow(
                "Completed · ${rows.firstOrNull{it.id==c.routineId}?.title ?: "Routine"}",
                LocalDate.ofEpochDay(c.day).toString(),
            ) {
                day = c.day
            }
        }
        if (history.size > limit) item { Action("Load older completions") { limit += 200 } }
    }
    edit?.let { r -> RoutineEditor(r, rows.any { it.id == r.id }, vm, state) { edit = null } }
}

fun weekdayNames(mask: Int) =
    DayOfWeek.entries
        .filter { mask and (1 shl (it.value - 1)) != 0 }
        .joinToString(" · ") { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }

fun routineDetail(r: Routine, time24: Boolean): String =
    listOfNotNull(
            if (r.timeMinutes < 0) "Any time"
            else
                LocalTime.of(r.timeMinutes / 60, r.timeMinutes % 60)
                    .format(DateTimeFormatter.ofPattern(if (time24) "HH:mm" else "h:mm a")),
            r.owner.takeIf { it.isNotBlank() },
        )
        .joinToString(" · ")

@Composable
private fun RoutineEditor(
    r: Routine,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    var title by remember(r.id) { mutableStateOf(r.title) }
    var owner by remember(r.id) { mutableStateOf(r.owner) }
    var notes by remember(r.id) { mutableStateOf(r.notes) }
    var weekdays by remember(r.id) { mutableIntStateOf(r.weekdays) }
    var active by remember(r.id) { mutableStateOf(r.active) }
    var timed by remember(r.id) { mutableStateOf(r.timeMinutes >= 0) }
    var time by
        remember(r.id) {
            mutableStateOf(
                if (r.timeMinutes < 0) LocalTime.of(8, 0)
                else LocalTime.of(r.timeMinutes / 60, r.timeMinutes % 60)
            )
        }
    var deleting by remember { mutableStateOf(false) }
    Editor("Your family rhythm", close) {
        if (state.prefs.grandparent) {
            Section(r.title)
            Muted(weekdayNames(r.weekdays))
            Muted(routineDetail(r, state.prefs.time24))
            Text(r.notes)
            Muted(if (r.active) "Active routine" else "Paused routine")
        } else {
            Field("Routine title", title, { title = it })
            Section("Days of the week")
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DayOfWeek.entries.forEach { d ->
                    val bit = 1 shl (d.value - 1)
                    FilterChip(
                        weekdays and bit != 0,
                        { weekdays = weekdays xor bit },
                        label = {
                            Text(d.name.take(3).lowercase().replaceFirstChar(Char::uppercase))
                        },
                    )
                }
            }
            Row {
                Text("Add a time", Modifier.weight(1f))
                Switch(
                    timed,
                    { timed = it },
                    Modifier.semantics { contentDescription = "Add a time" },
                )
            }
            if (timed) TimeButton(time, state.prefs.time24) { time = it }
            Field("Responsible person (optional)", owner, { owner = it })
            Field("Routine notes", notes, { notes = it }, lines = 2)
            Row {
                Text("Active routine", Modifier.weight(1f))
                Switch(
                    active,
                    { active = it },
                    Modifier.semantics { contentDescription = "Active routine" },
                )
            }
            Action("Save routine") {
                vm.perform("Routine saved") {
                    vm.repo.saveRoutine(
                        r.copy(
                            title = title,
                            owner = owner,
                            notes = notes,
                            weekdays = weekdays,
                            active = active,
                            timeMinutes = if (timed) time.hour * 60 + time.minute else -1,
                        )
                    )
                    close()
                }
            }
            if (existing) TextButton(onClick = { deleting = true }) { Text("Delete routine") }
        }
    }
    if (deleting)
        ConfirmDelete("this routine and its completion history", { deleting = false }) {
            vm.perform("Routine removed") {
                vm.repo.write { vm.repo.db.routines().delete(r.id, r.childId) }
                close()
            }
        }
}
