package com.nothatcher.sproutbook.features.pregnancy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.PregnancyRules
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate

@Composable
fun PregnancyScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(100) }
    var tab by rememberSaveable { mutableStateOf("Overview") }
    val historyKind = if (tab == "Kicks") "Kicks" else "Contraction"
    val rows by
        remember(child.id, historyKind, limit) {
                vm.repo.db.pregnancyEvents().history(child.id, historyKind, limit + 1)
            }
            .collectAsStateWithLifecycle(emptyList())
    val active by
        remember(child.id) { vm.repo.db.pregnancyEvents().active(child.id) }
            .collectAsStateWithLifecycle(emptyList())
    val prep by
        remember(child.id) { vm.repo.db.prepItems().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<PrepItem?>(null) }
    var record by remember { mutableStateOf<PregnancyEvent?>(null) }
    val kicks = active.firstOrNull { it.kind == "Kicks" && it.endsAt == null }
    val contraction = active.firstOrNull { it.kind == "Contraction" && it.endsAt == null }
    val now = rememberNow(contraction != null)
    Page("Waiting for you", "Pregnancy, one day at a time") {
        item {
            Choices(listOf("Overview", "Kicks", "Contractions", "Preparation"), tab) {
                tab = it
                limit = 100
            }
        }
        if (tab == "Overview") {
            item {
                Panel {
                    val due = child.dueDate
                    Section("Your pregnancy")
                    if (due != null) {
                        val days = PregnancyRules.gestationDays(due, LocalDate.now().toEpochDay())
                        Text(
                            "${days/7} weeks · ${days%7} days",
                            style = MaterialTheme.typography.headlineLarge,
                        )
                        Muted("Due · ${LocalDate.ofEpochDay(due)}")
                        LinearProgressIndicator(
                            progress = { (days / 280f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Muted(
                            when {
                                days < 98 -> "First trimester · Make room for rest and questions."
                                days < 196 ->
                                    "Second trimester · Keep care visits and plans together."
                                else -> "Third trimester · Gather support and prepare for birth."
                            }
                        )
                    } else {
                        Muted("Add a due date in this child's profile.")
                        Action("Open profile") { go("more") }
                    }
                }
            }
            item {
                EntryRow("Prenatal appointments", "Visits, questions and notes") { go("schedule") }
            }
            item {
                EntryRow(
                    "Birth preferences & preparation",
                    "Hospital bag and questions to bring along",
                ) {
                    tab = "Preparation"
                }
            }
        }
        if (tab == "Kicks") {
            item {
                Panel {
                    Section("Movement journal")
                    Text("${kicks?.count ?: 0}", style = MaterialTheme.typography.headlineLarge)
                    Muted(
                        kicks?.let { "Session started · ${stamp(it.startsAt,state.prefs.time24)}" }
                            ?: "A new session begins with the first kick."
                    )
                    Action("+ Kick", !state.prefs.grandparent) {
                        vm.perform("") { vm.repo.kick(child.id) }
                    }
                    if (kicks != null)
                        OutlinedButton(
                            onClick = {
                                vm.perform("Session saved") {
                                    vm.repo.finishPregnancySession(kicks.id, child.id)
                                }
                            },
                            enabled = !state.prefs.grandparent,
                        ) {
                            Text("Finish & reset session")
                        }
                }
            }
            item {
                Panel {
                    Section("Know their usual pattern")
                    Text(
                        "If movements are reduced, stop, or change from their usual pattern, contact your maternity unit immediately—even at night. Do not wait to reach a count. There is no universal daily target, and this counter cannot assess your baby's wellbeing.",
                        color = MaterialTheme.colorScheme.error,
                    )
                    SourceLink(
                        "NHS · Baby's movements",
                        "https://www.nhs.uk/pregnancy/keeping-well/your-babys-movements/",
                    )
                }
            }
            items(
                rows.take(limit).filter { it.kind == "Kicks" && it.endsAt != null },
                key = { it.id },
            ) { e ->
                EntryRow("${e.count} movements", stamp(e.startsAt, state.prefs.time24)) {
                    record = e
                }
            }
        }
        if (tab == "Contractions") {
            item {
                Panel {
                    Section(
                        if (contraction != null) "Contraction in progress" else "Contraction timer"
                    )
                    Text(
                        if (contraction != null) durationText(now - contraction.startsAt)
                        else "Ready when you are",
                        style = MaterialTheme.typography.headlineMedium,
                    )
                    Action(
                        if (contraction != null) "Stop contraction" else "Start contraction",
                        !state.prefs.grandparent,
                    ) {
                        vm.perform("") {
                            if (contraction != null)
                                vm.repo.finishPregnancySession(contraction.id, child.id)
                            else vm.repo.startContraction(child.id)
                        }
                    }
                }
            }
            item {
                Panel {
                    Section("Contact your maternity care team")
                    Text(
                        "This timer does not diagnose labour. Call if you think labour is starting or you are worried. Seek urgent maternity advice for bleeding, waters breaking, reduced movement, possible labour before 37 weeks, or unusually prolonged or frequent contractions. If birth seems imminent or there is a life-threatening emergency, call emergency services."
                    )
                    SourceLink(
                        "NHS · Signs of labour",
                        "https://www.nhs.uk/pregnancy/labour-and-birth/signs-that-labour-has-begun/",
                    )
                }
            }
            val contractions = rows.filter { it.kind == "Contraction" }
            items(contractions.take(limit), key = { it.id }) { e ->
                val previous =
                    contractions.filter { it.startsAt < e.startsAt }.maxByOrNull { it.startsAt }
                EntryRow(
                    e.endsAt?.let { "Duration · ${durationText(it-e.startsAt)}" } ?: "Running",
                    stamp(e.startsAt, state.prefs.time24) +
                        (previous?.let {
                            " · ${durationText(e.startsAt-it.startsAt)} since prior start"
                        } ?: ""),
                ) {
                    record = e
                }
            }
        }
        if (tab in listOf("Kicks", "Contractions") && rows.size > limit)
            item { Action("Load older pregnancy records") { limit += 100 } }
        if (tab == "Preparation") {
            item {
                Section("Hospital bag")
                Muted("Adapt this list to your birth setting and care team's advice.")
            }
            val defaults =
                listOf(
                        "Care records and identification",
                        "Comfortable clothes",
                        "Toiletries and maternity pads",
                        "Phone charger",
                        "Baby clothes and nappies",
                        "Transport and car seat plan",
                    )
                    .mapIndexed { i, title ->
                        prep.firstOrNull { it.id == "${child.id}-bag-$i" }
                            ?: PrepItem(
                                id = "${child.id}-bag-$i",
                                childId = child.id,
                                kind = "Bag",
                                title = title,
                            )
                    }
            items(
                defaults + prep.filter { it.kind == "Bag" && it.id !in defaults.map { d -> d.id } },
                key = { it.id },
            ) { p ->
                Panel {
                    Row {
                        Checkbox(
                            p.completed,
                            { vm.perform("") { vm.repo.togglePrep(p) } },
                            enabled = !state.prefs.grandparent,
                        )
                        TextButton(onClick = { edit = p }, modifier = Modifier.weight(1f)) {
                            Text(p.title)
                        }
                    }
                }
            }
            item {
                Action("Add checklist item", !state.prefs.grandparent) {
                    edit = PrepItem(childId = child.id, kind = "Bag", title = "")
                }
                Section("Birth preferences & questions")
            }
            items(prep.filter { it.kind == "Note" }, key = { it.id }) { p ->
                EntryRow(p.title, p.notes.take(90)) { edit = p }
            }
            item {
                Action("Add a note or question", !state.prefs.grandparent) {
                    edit = PrepItem(childId = child.id, kind = "Note", title = "")
                }
            }
        }
    }
    edit?.let { p ->
        var title by remember(p.id) { mutableStateOf(p.title) }
        var notes by remember(p.id) { mutableStateOf(p.notes) }
        var deleting by remember { mutableStateOf(false) }
        Editor("Preparation", { edit = null }) {
            if (state.prefs.grandparent) {
                Section(title)
                Text(notes)
            } else {
                Field("Title", title, { title = it })
                Field("Notes", notes, { notes = it }, lines = 4)
                Action("Save preparation") {
                    vm.perform {
                        vm.repo.savePrep(p.copy(title = title, notes = notes))
                        edit = null
                    }
                }
                if (prep.any { it.id == p.id } && !p.id.startsWith("${child.id}-bag-"))
                    TextButton(onClick = { deleting = true }) { Text("Delete item") }
            }
        }
        if (deleting)
            ConfirmDelete("this item", { deleting = false }) {
                vm.perform("Item removed") {
                    vm.repo.write { vm.repo.db.prepItems().delete(p.id, p.childId) }
                    edit = null
                }
            }
    }
    record?.let { e ->
        var deleting by remember { mutableStateOf(false) }
        Editor("${e.kind} session", { record = null }) {
            Text(stamp(e.startsAt, state.prefs.time24))
            Text(
                if (e.kind == "Kicks") "${e.count} movements"
                else e.endsAt?.let { durationText(it - e.startsAt) } ?: "In progress"
            )
            if (!state.prefs.grandparent)
                TextButton(onClick = { deleting = true }) { Text("Delete session") }
        }
        if (deleting)
            ConfirmDelete("this session", { deleting = false }) {
                vm.perform("Session removed") {
                    vm.repo.write { vm.repo.db.pregnancyEvents().delete(e.id, e.childId) }
                    record = null
                }
            }
    }
}
