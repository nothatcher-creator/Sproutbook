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
    val config = customization ?: child.homeCustomization()
    val nextAppointment = appointments.filter { it.startsAt >= System.currentTimeMillis() }.minByOrNull { it.startsAt }
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
                WoodlandHeader("Today", today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")))
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
                            EntryRow("Up next · ${a.title}", stamp(a.startsAt, state.prefs.time24), !preview) { go("schedule") }
                        }
                        "low_stock" -> Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            lowStock.forEach { i ->
                                EntryRow("Low stock · ${i.name}", "${i.quantity.toString().removeSuffix(".0")} ${i.unit} left", !preview) { go("inventory") }
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
                        "sleep" -> EntryRow("Log sleep", sleep.firstOrNull()?.let { "Last sleep · ${stamp(it.startsAt, state.prefs.time24)}" }
                            ?: "Start a nap or settle in for the night", !preview) { go("sleep") }
                        "milestones" -> EntryRow("Milestones & achievements", "A growing story of their own", !preview) { go("milestones") }
                        "feeding" -> EntryRow("Log feeding", feeds.firstOrNull { it.kind != "Pump" }?.let { "Last feed · ${stamp(it.startsAt, state.prefs.time24)}" }
                            ?: "Bottle, breast or pump", !preview) { go("feeding") }
                        "diapers" -> EntryRow("Log diaper", diaper.firstOrNull()?.let { "Last change · ${stamp(it.recordedAt, state.prefs.time24)}" }
                            ?: "Wet, dirty or mixed, in a tap", !preview) { go("diapers") }
                        "growth" -> EntryRow("Growth journal", "Weight, height and their recorded trends", !preview) { go("growth") }
                        "quick_actions" -> Panel {
                            Section("Quick actions")
                            BoxWithConstraints {
                                val buttonWidth = (maxWidth - 10.dp) / 2
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
                                    maxItemsInEachRow = 2) {
                                    config.quickActions.forEach { actionId ->
                                        val action = HomeOrganizerCatalog.quickActions.first { it.id == actionId }
                                        FilledTonalButton(onClick = { go(action.route) },
                                            enabled = !preview && !(state.prefs.grandparent && actionId == "memory"),
                                            modifier = Modifier.width(buttonWidth).heightIn(min = 52.dp).testTag("today-quick-$actionId")) {
                                            Text(action.title)
                                        }
                                    }
                                }
                            }
                        }
                        "memory_tree" -> Panel {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
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
                            EntryRow("A growing moment · ${m.title}", "Celebrated ${LocalDate.ofEpochDay(m.completedOn!!)}", !preview) { go("milestones") }
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
