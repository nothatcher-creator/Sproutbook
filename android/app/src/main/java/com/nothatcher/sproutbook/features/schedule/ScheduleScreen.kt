package com.nothatcher.sproutbook.features.schedule

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.CalendarRules
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun ScheduleScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    val haptic = LocalHapticFeedback.current
    val appointments by
        remember(child.id) { vm.repo.db.appointments().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var month by remember { mutableStateOf(YearMonth.now()) }
    var day by remember { mutableStateOf(LocalDate.now()) }
    var edit by remember { mutableStateOf<Appointment?>(null) }
    val days = remember(month) { CalendarRules.monthDays(month) }
    val familyDates =
        remember(state.children, month) {
            buildMap<LocalDate, List<String>> {
                state.children.forEach { c ->
                    c.birthday?.let { birth ->
                        for (year in month.year - 1..month.year + 1) CalendarRules.birthday(
                                LocalDate.ofEpochDay(birth),
                                year,
                            )
                            ?.let { date ->
                                put(date, get(date).orEmpty() + "${c.name}'s birthday")
                            }
                    }
                    c.dueDate?.let { due ->
                        val date = LocalDate.ofEpochDay(due)
                        put(date, get(date).orEmpty() + "${c.name}'s due date")
                    }
                }
            }
        }
    fun create(date: LocalDate) {
        edit = Appointment(childId = child.id, title = "", startsAt = at(date, LocalTime.of(9, 0)))
    }
    Page("Schedule", "Plans, visits and family moments") {
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { month = month.minusMonths(1) },
                        Modifier.semantics { contentDescription = "Previous month" },
                    ) {
                        WoodlandIcon(com.nothatcher.sproutbook.R.drawable.woodland_back)
                    }
                    Text(
                        month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        Modifier.weight(1f),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    if (month != YearMonth.now() || day != LocalDate.now())
                        TextButton(onClick = {
                            month = YearMonth.now()
                            day = LocalDate.now()
                        }) { Text("Today") }
                    TextButton(
                        onClick = { month = month.plusMonths(1) },
                        Modifier.semantics { contentDescription = "Next month" },
                    ) {
                        WoodlandIcon(com.nothatcher.sproutbook.R.drawable.woodland_next)
                    }
                }
                Row(Modifier.fillMaxWidth()) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Muted(it) }
                    }
                }
                days.chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth()) {
                        week.forEach { date ->
                            val count =
                                appointments.count { dateOf(it.startsAt) == date } +
                                    familyDates[date].orEmpty().size
                            Box(
                                Modifier.weight(1f)
                                    .height(49.dp)
                                    .testTag("day-${date.toEpochDay()}")
                                    .semantics {
                                        contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")) +
                                            ", " + if (count == 1) "1 event" else "$count events"
                                        if (date == day) selected = true
                                    }
                                    .combinedClickable(
                                        onClick = { day = date },
                                        onLongClickLabel = "Create appointment on ${friendlyDate(date)}",
                                        onLongClick = {
                                            day = date
                                            if (!state.prefs.grandparent) {
                                                haptic.performHapticFeedback(
                                                    HapticFeedbackType.LongPress
                                                )
                                                create(date)
                                            }
                                        },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                val isToday = date == LocalDate.now()
                                Surface(
                                    shape = CircleShape,
                                    color =
                                        if (date == day) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceContainer,
                                    border = if (isToday) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                                ) {
                                    Column(
                                        Modifier.size(38.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                    ) {
                                        Text(
                                            date.dayOfMonth.toString(),
                                            color =
                                                if (YearMonth.from(date) == month)
                                                    MaterialTheme.colorScheme.onSurface
                                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f),
                                            fontWeight = if (isToday || date == day) FontWeight.Bold else null,
                                        )
                                        Box(
                                            Modifier.padding(top = 2.dp).size(5.dp).background(
                                                if (count > 0) MaterialTheme.colorScheme.secondary else Color.Transparent,
                                                CircleShape,
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Muted("Tap a day to see plans. Long press to add an appointment.")
            }
        }
        item { Action("Add appointment · ${friendlyDate(day)}", !state.prefs.grandparent) { create(day) } }
        item {
            Section(
                when (day) {
                    LocalDate.now() -> "Today · "
                    LocalDate.now().plusDays(1) -> "Tomorrow · "
                    else -> ""
                } + day.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
            )
        }
        familyDates[day].orEmpty().forEach { event ->
            item {
                Panel {
                    Section(event)
                    Muted("From your family profiles")
                }
            }
        }
        val selected = appointments.filter { dateOf(it.startsAt) == day }.sortedBy { it.startsAt }
        if (selected.isEmpty() && familyDates[day].isNullOrEmpty())
            item { Empty("A little breathing room", "No plans recorded for this day.") }
        items(selected, key = { "day-${it.id}" }) { a ->
            EntryRow(a.title, listOf(stamp(a.startsAt, state.prefs.time24), a.category, a.location).filter { it.isNotBlank() }.joinToString(" · ")) {
                edit = a
            }
        }
        val upcoming = appointments
            .filter { it.startsAt >= System.currentTimeMillis() }
            .sortedBy { it.startsAt }
            .take(12)
        item { Section("Coming up") }
        if (upcoming.isEmpty())
            item { Muted("Nothing coming up yet. Long press a day or use Add appointment to plan ahead.") }
        items(upcoming, key = { "upcoming-${it.id}" }) { a ->
            EntryRow(a.title, stamp(a.startsAt, state.prefs.time24)) { edit = a }
        }
    }
    edit?.let { a ->
        AppointmentEditor(a, appointments.any { it.id == a.id }, vm, state) { edit = null }
    }
}

@Composable
private fun AppointmentEditor(
    value: Appointment,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    val initial = Instant.ofEpochMilli(value.startsAt).atZone(ZoneId.systemDefault())
    var title by remember(value.id) { mutableStateOf(value.title) }
    var date by remember(value.id) { mutableStateOf(initial.toLocalDate()) }
    var time by remember(value.id) { mutableStateOf(initial.toLocalTime()) }
    var childId by remember(value.id) { mutableStateOf(value.childId) }
    var location by remember(value.id) { mutableStateOf(value.location) }
    var category by remember(value.id) { mutableStateOf(value.category) }
    var notes by remember(value.id) { mutableStateOf(value.notes) }
    var questions by remember(value.id) { mutableStateOf(value.questions) }
    var results by remember(value.id) { mutableStateOf(value.results) }
    var reminder by remember(value.id) { mutableIntStateOf(value.reminderMinutes) }
    var deleting by remember { mutableStateOf(false) }
    val reminders = mapOf("Off" to 0, "15 min" to 15, "1 hour" to 60, "1 day" to 1440)
    Editor(if (existing) "Appointment details" else "A new appointment", close) {
        if (state.prefs.grandparent) {
            Section(title)
            Text(stamp(value.startsAt, state.prefs.time24))
            Text(location)
            Text(notes)
            Section("Questions")
            Text(questions)
            Section("Notes & results")
            Text(results)
        } else {
            Field("Appointment title", title, { title = it })
            DateButton("Date", date) { date = it }
            TimeButton(time, state.prefs.time24) { time = it }
            if (!existing) {
                Section("For")
                state.children.forEach { c ->
                    FilterChip(childId == c.id, { childId = c.id }, label = { Text(c.name) })
                }
            }
            Choices(appointmentCategories, category) { category = it }
            Field("Location", location, { location = it })
            Field("Notes", notes, { notes = it }, lines = 2)
            Field("Questions for the visit", questions, { questions = it }, lines = 2)
            Field("After the visit · notes & results", results, { results = it }, lines = 3)
            Section("Reminder")
            Choices(reminders.keys.toList(), reminders.entries.firstOrNull { it.value == reminder }?.key ?: "Off") {
                reminder = reminders.getValue(it)
            }
            if (reminder > 0 && !state.prefs.notifications)
                Muted("Enable appointment notifications in settings to receive reminders.")
            Action("Save appointment") {
                vm.perform {
                    vm.repo.saveAppointment(
                        value.copy(
                            childId = childId,
                            title = title,
                            startsAt = at(date, time),
                            location = location,
                            category = category,
                            notes = notes,
                            questions = questions,
                            results = results,
                            reminderMinutes = reminder,
                        )
                    )
                    close()
                }
            }
            if (existing)
                TextButton(onClick = { deleting = true }) {
                    Text("Delete appointment", color = MaterialTheme.colorScheme.error)
                }
        }
    }
    if (deleting)
        ConfirmDelete(title, { deleting = false }) {
            vm.perform("Appointment deleted") {
                vm.repo.deleteAppointment(value)
                close()
            }
        }
}
