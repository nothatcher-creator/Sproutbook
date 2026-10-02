package com.nothatcher.sproutbook.features.feeding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.FeedingMath
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*

fun volume(ml: Double, unit: String): String =
    if (unit == "mL") "%.0f mL".format(ml) else "%.1f US fl oz".format(FeedingMath.ounces(ml))

@Composable
fun FeedingScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    var filter by rememberSaveable { mutableStateOf("All") }
    val rows by
        remember(child.id, filter, limit) {
                vm.repo.db.feeds().history(child.id, filter, limit + 1)
            }
            .collectAsStateWithLifecycle(emptyList())
    val now = rememberNow(interval = 60000)
    val today = dateOf(now)
    val week by
        remember(child.id, today) {
                vm.repo.db.feeds().since(child.id, at(today.minusDays(6), LocalTime.MIDNIGHT))
            }
            .collectAsStateWithLifecycle(emptyList())
    val last by
        remember(child.id) { vm.repo.db.feeds().latestFeed(child.id) }
            .collectAsStateWithLifecycle(null)
    var kind by remember { mutableStateOf("Bottle") }
    var edit by remember { mutableStateOf<Feed?>(null) }
    val daily = week.filter { dateOf(it.startsAt) == today }
    Page("Feeding", "Bottle, breast and pump, in one rhythm") {
        item {
            Panel {
                Section("Today so far")
                Text(
                    volume(
                        daily.filter { it.kind == "Bottle" }.sumOf { it.amountMl },
                        state.prefs.volumeUnit,
                    ),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Muted("Bottle total · ${daily.count{it.kind!="Pump"}} feeds")
                Muted(
                    "Nursing · ${daily.filter{it.kind=="Breast"}.sumOf{it.durationSeconds}/60} min"
                )
                Muted(
                    "Pumped · ${volume(daily.filter{it.kind=="Pump"}.sumOf{it.amountMl},state.prefs.volumeUnit)}"
                )
                Muted(
                    last?.let { "Last feed ${durationText(now-it.startsAt)} ago" }
                        ?: "Your first feed is waiting to be logged."
                )
            }
        }
        item {
            Choices(listOf("Bottle", "Breast", "Pump"), kind) { kind = it }
            Action("Log ${kind.lowercase()}", !state.prefs.grandparent) {
                edit = Feed(childId = child.id, kind = kind, startsAt = System.currentTimeMillis())
            }
        }
        item {
            EntryRow("Prepared bottles", "Preparation and one-tap feeding logs") { go("bottles") }
        }
        item {
            EntryRow("Milk freezer", "Store saved pump sessions and track your reserve") {
                go("milk")
            }
        }
        item {
            EntryRow("Formula planning", "A rough estimate for a day's supplies") { go("formula") }
        }
        item {
            Choices(listOf("All", "Bottle", "Breast", "Pump"), filter) {
                filter = it
                limit = 200
            }
        }
        items(rows.take(limit), key = { it.id }) { f ->
            EntryRow(
                if (f.kind == "Breast") "${f.side} · ${f.durationSeconds/60} min"
                else "${f.kind} · ${volume(f.amountMl,state.prefs.volumeUnit)}",
                stamp(f.startsAt, state.prefs.time24),
            ) {
                edit = f
            }
        }
        if (rows.size > limit) item { Action("Load older feeding records") { limit += 200 } }
        item {
            Panel {
                Section("The last seven days")
                (6L downTo 0L).forEach { offset ->
                    val day = today.minusDays(offset)
                    val feeds = week.filter { dateOf(it.startsAt) == day && it.kind != "Pump" }
                    Row {
                        Text(day.dayOfWeek.name.take(3), Modifier.weight(1f))
                        Muted(
                            "${feeds.size} feeds · ${volume(feeds.filter{it.kind=="Bottle"}.sumOf{it.amountMl},state.prefs.volumeUnit)}"
                        )
                    }
                }
            }
        }
    }
    edit?.let { f -> FeedEditor(f, rows.any { it.id == f.id }, vm, state) { edit = null } }
}

@Composable
private fun FeedEditor(
    value: Feed,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    val unit = state.prefs.volumeUnit
    fun input(ml: Double) =
        if (ml == 0.0) ""
        else "%.1f".format(java.util.Locale.US, if (unit == "mL") ml else FeedingMath.ounces(ml))
    var kind by remember(value.id) { mutableStateOf(value.kind) }
    var amount by remember(value.id) { mutableStateOf(input(value.amountMl)) }
    var left by remember(value.id) { mutableStateOf(input(value.leftMl)) }
    var right by remember(value.id) { mutableStateOf(input(value.rightMl)) }
    var duration by
        remember(value.id) {
            mutableStateOf(
                if (value.durationSeconds == 0L) "" else (value.durationSeconds / 60.0).toString()
            )
        }
    var side by remember(value.id) { mutableStateOf(value.side) }
    var contents by remember(value.id) { mutableStateOf(value.contents) }
    var notes by remember(value.id) { mutableStateOf(value.notes) }
    var deleting by remember { mutableStateOf(false) }
    val initial = Instant.ofEpochMilli(value.startsAt).atZone(ZoneId.systemDefault())
    var date by remember(value.id) { mutableStateOf(initial.toLocalDate()) }
    var time by remember(value.id) { mutableStateOf(initial.toLocalTime()) }
    fun ml(text: String, label: String, optional: Boolean = false): Double {
        val raw = number(text, label, optional)
        return if (unit == "mL") raw else FeedingMath.ml(raw)
    }
    Editor("Feeding record", close) {
        if (state.prefs.grandparent) {
            Section(kind)
            Text(stamp(value.startsAt, state.prefs.time24))
            Text(
                if (kind == "Breast") "${value.side} · ${value.durationSeconds/60} min"
                else volume(value.amountMl, unit)
            )
            Text(notes)
        } else {
            Choices(listOf("Bottle", "Breast", "Pump"), kind) { kind = it }
            if (kind == "Bottle") {
                Field(
                    "Amount · ${if(unit=="mL")unit else "US fl oz"}",
                    amount,
                    { amount = it },
                    numeric = true,
                )
                Choices(
                    if (unit == "mL") listOf("30", "60", "90", "120", "150")
                    else listOf("1", "2", "3", "4", "5"),
                    amount,
                ) {
                    amount = it
                }
                Choices(listOf("Formula", "Breast milk"), contents) { contents = it }
            }
            if (kind == "Breast") Choices(listOf("Left", "Right", "Both"), side) { side = it }
            if (kind == "Pump") {
                Field("Left amount · $unit", left, { left = it }, numeric = true)
                Field("Right amount · $unit", right, { right = it }, numeric = true)
            }
            if (kind != "Bottle")
                Field(
                    if (kind == "Breast") "Duration · minutes" else "Duration · minutes (optional)",
                    duration,
                    { duration = it },
                    numeric = true,
                )
            DateButton("Date", date) { date = it }
            TimeButton(time, state.prefs.time24) { time = it }
            Field("Notes", notes, { notes = it }, lines = 2)
            Action("Save feeding") {
                vm.perform {
                    vm.repo.saveFeed(
                        value.copy(
                            kind = kind,
                            startsAt = at(date, time),
                            amountMl = if (kind == "Bottle") ml(amount, "bottle amount") else 0.0,
                            leftMl = if (kind == "Pump") ml(left, "left amount", true) else 0.0,
                            rightMl = if (kind == "Pump") ml(right, "right amount", true) else 0.0,
                            durationSeconds =
                                if (kind == "Bottle") 0
                                else (number(duration, "duration", kind == "Pump") * 60).toLong(),
                            side = side,
                            contents = contents,
                            notes = notes,
                        )
                    )
                    close()
                }
            }
            if (existing)
                TextButton(onClick = { deleting = true }) {
                    Text("Delete feeding", color = MaterialTheme.colorScheme.error)
                }
        }
    }
    if (deleting)
        ConfirmDelete("this feeding", { deleting = false }) {
            vm.perform("Feeding deleted") {
                vm.repo.write { vm.repo.db.feeds().delete(value.id, value.childId) }
                close()
            }
        }
}
