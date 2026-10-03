package com.nothatcher.sproutbook.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.RoutineRules
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.features.feeding.volume
import com.nothatcher.sproutbook.features.memories.MemoryTree
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    val child = state.child ?: return
    val memories by
        remember(child.id) { vm.repo.db.memorys().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    val appointments by
        remember(child.id) { vm.repo.db.appointments().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    val feeds by
        remember(child.id) { vm.repo.db.feeds().observe(child.id, 10) }
            .collectAsStateWithLifecycle(emptyList())
    val stock by
        remember(child.id) { vm.repo.db.inventorys().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    val sleep by
        remember(child.id) { vm.repo.db.sleeps().observe(child.id, 1) }
            .collectAsStateWithLifecycle(emptyList())
    val milestones by
        remember(child.id) { vm.repo.db.milestones().observe(child.id, 10) }
            .collectAsStateWithLifecycle(emptyList())
    val diaper by
        remember(child.id) { vm.repo.db.diapers().observe(child.id, 1) }
            .collectAsStateWithLifecycle(emptyList())
    val shoppingCount by
        remember(child.id) { vm.repo.db.shoppingItems().pendingCount(child.id) }
            .collectAsStateWithLifecycle(0)
    val preparedCount by
        remember(child.id) { vm.repo.db.bottlePreps().preparedCount(child.id) }
            .collectAsStateWithLifecycle(0)
    val today = dateOf(rememberNow(interval = 60000))
    val routines by
        remember(child.id) { vm.repo.db.routines().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    val completed by
        remember(child.id, today) {
                vm.repo.db.routineCompletions().forDay(child.id, today.toEpochDay())
            }
            .collectAsStateWithLifecycle(emptyList())
    val milkStock by
        remember(child.id) { vm.repo.db.milkContainers().frozenSummary(child.id) }
            .collectAsStateWithLifecycle(MilkStock())
    val due =
        remember(routines, today) {
            routines.filter { it.active && RoutineRules.isDue(it.weekdays, today) }
        }
    val doneIds = remember(completed) { completed.map { it.routineId }.toSet() }
    val remaining = due.filter { it.id !in doneIds }
    val chapter = memories.firstOrNull()?.chapter ?: 0
    Page("Today", LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))) {
        item {
            Panel {
                Text("YOUR ${Stage.valueOf(child.stage).label.uppercase()} CHAPTER", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
                Text(
                    when (child.stage) {
                        "PREGNANCY" -> "Getting ready, together."
                        "BABY" -> "Small moments.\nA whole lot of love."
                        "TODDLER" -> "A little more curious,\nevery day."
                        "CHILD" -> "Room to explore.\nRoots to come home to."
                        else -> "Growing into their\nown kind of wonderful."
                    },
                    style = MaterialTheme.typography.headlineSmall,
                )
                Muted("${child.name}'s day, all in one place.")
            }
        }
        appointments
            .filter { it.startsAt >= System.currentTimeMillis() }
            .minByOrNull { it.startsAt }
            ?.let { a ->
                item {
                    EntryRow("Up next · ${a.title}", stamp(a.startsAt, state.prefs.time24)) {
                        go("schedule")
                    }
                }
            }
        stock
            .filter { it.quantity <= it.threshold }
            .take(3)
            .forEach { i ->
                item {
                    EntryRow(
                        "Low stock · ${i.name}",
                        "${i.quantity.toString().removeSuffix(".0")} ${i.unit} left",
                    ) {
                        go("inventory")
                    }
                }
            }
        if (shoppingCount > 0)
            item {
                EntryRow("Shopping · $shoppingCount to buy", "Your next shop, ready to go") {
                    go("shopping")
                }
            }
        if (preparedCount > 0 && child.stage in listOf("BABY", "TODDLER"))
            item {
                EntryRow("$preparedCount prepared bottles", "View preparations and record use") {
                    go("bottles")
                }
            }
        item { EntryRow("Help right now", "Calm first steps, close at hand") { go("help") } }
        if (due.isNotEmpty())
            item {
                EntryRow(
                    "Today’s routines · ${remaining.size} remaining",
                    remaining
                        .take(2)
                        .joinToString(" · ") { it.title }
                        .ifEmpty { "All scheduled routines completed" },
                ) {
                    go("routines")
                }
            }
        else if (child.stage in listOf("CHILD", "TEEN"))
            item {
                EntryRow(
                    "Routines & responsibilities",
                    "Shared tasks and your family’s daily rhythm",
                ) {
                    go("routines")
                }
            }
        if (milkStock.count > 0 && child.stage in listOf("BABY", "TODDLER"))
            item {
                EntryRow(
                    "Milk freezer · ${volume(milkStock.totalMl,state.prefs.volumeUnit)}",
                    "${milkStock.count} frozen containers",
                ) {
                    go("milk")
                }
            }
        item { Section("A little care, right here") }
        if (child.stage == "PREGNANCY")
            item {
                EntryRow("Your pregnancy", "Movements, appointments and preparation") {
                    go("pregnancy")
                }
            }
        if (child.stage in listOf("BABY", "TODDLER"))
            item {
                EntryRow(
                    "Log sleep",
                    sleep.firstOrNull()?.let {
                        "Last sleep · ${stamp(it.startsAt,state.prefs.time24)}"
                    } ?: "Start a nap or settle in for the night",
                ) {
                    go("sleep")
                }
            }
        if (child.stage in listOf("CHILD", "TEEN"))
            item {
                EntryRow("Milestones & achievements", "A growing story of their own") {
                    go("milestones")
                }
            }
        if (child.stage in listOf("BABY", "TODDLER"))
            item {
                EntryRow(
                    "Log feeding",
                    feeds
                        .firstOrNull { it.kind != "Pump" }
                        ?.let { "Last feed · ${stamp(it.startsAt,state.prefs.time24)}" }
                        ?: "Bottle, breast or pump",
                ) {
                    go("feeding")
                }
            }
        if (child.stage in listOf("BABY", "TODDLER"))
            item {
                EntryRow(
                    "Log diaper",
                    diaper.firstOrNull()?.let {
                        "Last change · ${stamp(it.recordedAt,state.prefs.time24)}"
                    } ?: "Wet, dirty or mixed, in a tap",
                ) {
                    go("diapers")
                }
            }
        if (child.stage != "PREGNANCY")
            item {
                EntryRow("Growth journal", "Weight, height and their recorded trends") {
                    go("growth")
                }
            }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(
                    onClick = { go("memories") },
                    Modifier.weight(1f).heightIn(min = 52.dp),
                ) {
                    Text("Add memory")
                }
                OutlinedButton(
                    onClick = { go("schedule") },
                    Modifier.weight(1f).heightIn(min = 52.dp),
                ) {
                    Text("Schedule")
                }
            }
        }
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Section("Growing memories")
                    TextButton(onClick = { go("memories") }) { Text("Open tree") }
                }
                MemoryTree(
                    memories.filter { it.chapter == chapter },
                    onOpen = { go("memories?memoryId=${it.id}") },
                    treeStyle = child.treeStyle,
                )
                Muted(
                    memories.firstOrNull()?.let { "${it.title} · Your latest little moment" }
                        ?: "Every story begins with a first leaf."
                )
            }
        }
        milestones
            .firstOrNull { it.completedOn != null }
            ?.let { m ->
                item {
                    EntryRow(
                        "A growing moment · ${m.title}",
                        "Celebrated ${LocalDate.ofEpochDay(m.completedOn!!)}",
                    ) {
                        go("milestones")
                    }
                }
            }
        if (child.stage in listOf("CHILD", "TEEN"))
            item {
                EntryRow("Health & important records", "Keep their care notes together") {
                    go("health")
                }
            }
        if (child.stage == "TODDLER")
            item { EntryRow("Potty journal", "A little visit, remembered") { go("potty") } }
        if (child.stage == "TODDLER")
            item {
                EntryRow("This week’s meals", "A little planning for the family table") {
                    go("meals")
                }
            }
        if (child.notes.isNotBlank())
            item {
                Panel {
                    Section("For everyone who cares")
                    Text(child.notes)
                }
            }
    }
}
