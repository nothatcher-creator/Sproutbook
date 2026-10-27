package com.nothatcher.sproutbook.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.FriendlyTime
import com.nothatcher.sproutbook.core.RoutineRules
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.core.HomeOrganizerCatalog
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.features.feeding.volume
import com.nothatcher.sproutbook.features.memories.MemoryTree
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vm: FamilyViewModel,
    state: FamilyState,
    customization: HomeCustomization? = null,
    preview: Boolean = false,
    go: (String) -> Unit,
) {
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
    val now = rememberNow(interval = 60000)
    val today = dateOf(now)
    fun ago(millis: Long) = FriendlyTime.ago(millis, now)
    val quick = !state.prefs.grandparent
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
    val config = customization ?: child.homeCustomization()
    val nextAppointment = appointments.filter { it.startsAt >= now }.minByOrNull { it.startsAt }
    val lowStock = stock.filter { it.quantity <= it.threshold }.take(3)
    val latestMilestone = milestones.firstOrNull { it.completedOn != null }
    val babyOrToddler = child.stage in listOf("BABY", "TODDLER")
    val childOrTeen = child.stage in listOf("CHILD", "TEEN")
    val visible = config.sectionOrder.filter { id ->
        id !in config.hiddenSections && when (id) {
            "up_next" -> nextAppointment != null
            "low_stock" -> lowStock.isNotEmpty()
            "shopping" -> shoppingCount > 0
            "prepared_bottles" -> preparedCount > 0 && babyOrToddler
            "routines" -> due.isNotEmpty() || childOrTeen
            "milk_freezer" -> milkStock.count > 0 && babyOrToddler
            "pregnancy" -> child.stage == "PREGNANCY"
            "sleep", "feeding", "diapers" -> babyOrToddler
            "milestones", "health" -> childOrTeen
            "growth" -> child.stage != "PREGNANCY"
            "quick_actions" -> config.quickActions.isNotEmpty()
            "latest_milestone" -> latestMilestone != null
            "potty", "meals" -> child.stage == "TODDLER"
            "caregiver_notes" -> child.notes.isNotBlank()
            else -> true
        }
    }
    TodayBackdrop(child.id, config) { photoUnavailable ->
        LazyColumn(
            Modifier.fillMaxSize().testTag("today-list"),
            contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "today-header") {
                val hour = java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).hour
                WoodlandHeader("Today", "${FriendlyTime.greeting(hour)} · ${today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))}")
            }
            if (photoUnavailable) item(key = "background-unavailable") {
                Panel {
                    Section("Background photo unavailable")
                    Muted("Your woodland background is showing. Choose another photo in the Home screen organizer.")
                    TextButton(onClick = { go("home-organizer") }, enabled = !preview) { Text("Home screen organizer") }
                }
            }
            if (visible.isEmpty()) item(key = "empty-layout") {
                Panel {
                    Section("Make Today yours")
                    Muted("Your sections are hidden or waiting for this child's next chapter. Choose what belongs here in the organizer.")
                    OutlinedButton(onClick = { go("home-organizer") }, enabled = !preview,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("today-organizer-empty")) {
                        Text("Home screen organizer")
                    }
                }
            }
            items(visible, key = { it }) { id ->
                Box(Modifier.fillMaxWidth().testTag("today-section-$id")) {
                    when (id) {
                        "chapter" -> Panel {
                            Text("YOUR ${Stage.valueOf(child.stage).label.uppercase()} CHAPTER", color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelMedium)
                            Text(when (child.stage) {
                                "PREGNANCY" -> "Getting ready, together."
                                "BABY" -> "Small moments.\nA whole lot of love."
                                "TODDLER" -> "A little more curious,\nevery day."
                                "CHILD" -> "Room to explore.\nRoots to come home to."
                                else -> "Growing into their\nown kind of wonderful."
                            }, style = MaterialTheme.typography.headlineSmall)
                            Muted("${child.name}'s day, all in one place.")
                        }
                        "up_next" -> nextAppointment?.let { a ->
                            val day = dateOf(a.startsAt)
                            val time = java.time.Instant.ofEpochMilli(a.startsAt).atZone(java.time.ZoneId.systemDefault()).toLocalTime()
                                .format(DateTimeFormatter.ofPattern(if (state.prefs.time24) "HH:mm" else "h:mm a"))
                            val whenText = when (day) {
                                today -> "Today · $time"
                                today.plusDays(1) -> "Tomorrow · $time"
                                else -> "${friendlyDate(day, today)} · $time"
                            }
                            EntryRow("Up next · ${a.title}", listOf(whenText, a.location).filter { it.isNotBlank() }.joinToString(" · "), !preview) { go("schedule") }
                        }
                        "low_stock" -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            lowStock.forEach { i ->
                                EntryRow("Low stock · ${i.name}", "${i.quantity.toString().removeSuffix(".0")} ${i.unit} left · Restock soon", !preview) { go("inventory") }
                            }
                        }
                        "shopping" -> EntryRow("Shopping · $shoppingCount to buy", "Your next shop, ready to go", !preview) { go("shopping") }
                        "prepared_bottles" -> EntryRow("$preparedCount prepared bottles", "View preparations and record use", !preview) { go("bottles") }
                        "help" -> EntryRow("Help right now", "Calm first steps, close at hand", !preview) { go("help") }
                        "routines" -> if (due.isNotEmpty()) {
                            EntryRow("Today’s routines · ${remaining.size} remaining", remaining.take(2).joinToString(" · ") { it.title }
                                .ifEmpty { "All scheduled routines completed" }, !preview) { go("routines") }
                        } else EntryRow("Routines & responsibilities", "Shared tasks and your family’s daily rhythm", !preview) { go("routines") }
                        "milk_freezer" -> EntryRow("Milk freezer · ${volume(milkStock.totalMl, state.prefs.volumeUnit)}", "${milkStock.count} frozen containers", !preview) { go("milk") }
                        "pregnancy" -> EntryRow("Your pregnancy", "Movements, appointments and preparation", !preview) { go("pregnancy") }
                        "sleep" -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EntryRow("Log sleep", sleep.firstOrNull()?.let { s -> s.endsAt?.let { end -> "Woke ${ago(end)} · ${durationText(end - s.startsAt)} sleep" }
                            ?: ("Sleeping now · since " + if (dateOf(s.startsAt) == today) java.time.Instant.ofEpochMilli(s.startsAt)
                                .atZone(java.time.ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(if (state.prefs.time24) "HH:mm" else "h:mm a"))
                                else stamp(s.startsAt, state.prefs.time24)) }
                            ?: "Start a nap or settle in for the night", !preview) { go("sleep") }
                            if (quick) {
                                val current = sleep.firstOrNull()?.takeIf { it.endsAt == null }
                                QuickLogRow(
                                    if (current != null) listOf("Stop ${current.kind.lowercase()}" + ((now - current.startsAt) / 60_000).let { m ->
                                        when { m < 1 -> ""; m < 60 -> " · $m min"; else -> " · ${m / 60} h ${m % 60} min" } })
                                    else listOf("Start nap", "Start night"),
                                    enabled = !preview, tag = "sleep",
                                ) { label ->
                                    if (current != null) vm.performUndoable("${current.kind} ended", {
                                        vm.repo.saveSleep(current.copy(endsAt = System.currentTimeMillis()))
                                    }) { vm.repo.saveSleep(current) }
                                    else {
                                        val started = Sleep(childId = child.id, startsAt = System.currentTimeMillis(),
                                            kind = if (label.endsWith("night")) "Night" else "Nap")
                                        vm.performUndoable("${started.kind} started", { vm.repo.saveSleep(started) }) {
                                            vm.repo.write { vm.repo.db.sleeps().delete(started.id, started.childId) }
                                        }
                                    }
                                }
                            }
                        }
                        "milestones" -> EntryRow("Milestones & achievements", "A growing story of their own", !preview) { go("milestones") }
                        "feeding" -> EntryRow("Log feeding", feeds.firstOrNull { it.kind != "Pump" }?.let { "Last feed ${ago(it.startsAt)} · ${it.kind.lowercase()}" }
                            ?: "Bottle, breast or pump", !preview) { go("feeding") }
                        "diapers" -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            EntryRow("Log diaper", diaper.firstOrNull()?.let { "Last change ${ago(it.recordedAt)} · ${it.kind.lowercase()}" }
                            ?: "Wet, dirty or mixed, in a tap", !preview) { go("diapers") }
                            if (quick) {
                                QuickLogRow(listOf("Wet", "Dirty", "Mixed"), enabled = !preview, tag = "diaper") { kind ->
                                    val change = Diaper(childId = child.id, recordedAt = System.currentTimeMillis(), kind = kind)
                                    vm.performUndoable("$kind diaper logged", { vm.repo.saveDiaper(change) }) {
                                        vm.repo.write { vm.repo.db.diapers().delete(change.id, change.childId) }
                                    }
                                }
                            }
                        }
                        "growth" -> EntryRow("Growth journal", "Weight, height and their recorded trends", !preview) { go("growth") }
                        "quick_actions" -> Panel {
                            Section("Quick actions")
                            run {
                                // Weights instead of computed widths: exact halves could round over and wrap.
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
                                    maxItemsInEachRow = 2) {
                                    config.quickActions.forEach { actionId ->
                                        val action = HomeOrganizerCatalog.quickActions.first { it.id == actionId }
                                        FilledTonalButton(onClick = { go(action.route) },
                                            enabled = !preview && !(state.prefs.grandparent && actionId == "memory"),
                                            shape = MaterialTheme.shapes.medium,
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                            modifier = Modifier.weight(1f).heightIn(min = 56.dp).testTag("today-quick-$actionId")) {
                                            WoodlandIcon(woodlandIconFor(action.title), modifier = Modifier.size(20.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(action.title, Modifier.weight(1f, fill = false), maxLines = 2)
                                        }
                                    }
                                }
                            }
                        }
                        "memory_tree" -> Panel {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Section("Growing memories")
                                TextButton(onClick = { go("memories") }, enabled = !preview) { Text("Open tree") }
                            }
                            Muted("${child.treeStyle} woodland · ${child.name}'s growing story")
                            MemoryTree(memories.filter { it.chapter == chapter }, onOpen = { go("memories?memoryId=${it.id}") },
                                treeStyle = child.treeStyle, enabled = !preview)
                            Muted(memories.firstOrNull()?.let { "${it.title} · Your latest little moment" }
                                ?: "Every story begins with a first leaf.")
                        }
                        "latest_milestone" -> latestMilestone?.let { m ->
                            EntryRow("A growing moment · ${m.title}", "Celebrated ${friendlyDate(LocalDate.ofEpochDay(m.completedOn!!), today)}", !preview) { go("milestones") }
                        }
                        "health" -> EntryRow("Health & important records", "Keep their care notes together", !preview) { go("health") }
                        "potty" -> EntryRow("Potty journal", "A little visit, remembered", !preview) { go("potty") }
                        "meals" -> EntryRow("This week’s meals", "A little planning for the family table", !preview) { go("meals") }
                        "caregiver_notes" -> Panel {
                            Section("For everyone who cares")
                            Text(child.notes)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLogRow(labels: List<String>, enabled: Boolean, tag: String, onLog: (String) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        labels.forEach { label ->
            FilledTonalButton(onClick = { onLog(label) }, enabled = enabled, shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                    .testTag("today-log-$tag-${label.substringBefore(" ·").lowercase().replace(' ', '-')}")) {
                Text(label, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
    }
}
