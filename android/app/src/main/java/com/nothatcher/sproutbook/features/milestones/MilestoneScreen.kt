package com.nothatcher.sproutbook.features.milestones

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate

private val suggestions =
    mapOf(
        "PREGNANCY" to listOf("First scan", "First felt movement", "Birth preferences ready"),
        "BABY" to listOf("First smile", "Sitting independently", "First word"),
        "TODDLER" to
            listOf("First little sentence", "A new self-care skill", "Playing alongside a friend"),
        "CHILD" to listOf("First day at school", "Learned a new skill", "An act of kindness"),
        "TEEN" to
            listOf("A personal goal reached", "A new responsibility", "An achievement to remember"),
    )

@Composable
fun MilestoneScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    val rows by
        remember(child.id) { vm.repo.db.milestones().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var stage by remember(child.stage) { mutableStateOf(child.stage) }
    var edit by remember { mutableStateOf<Milestone?>(null) }
    Page("Growing, their way", "Milestones to notice and celebrate") {
        item {
            Muted(
                "Developmental timelines vary. This is a family journal, not a pass/fail assessment. Share any concerns with your child's clinician."
            )
            Choices(Stage.entries.map { it.label }, Stage.valueOf(stage).label) { label ->
                stage = Stage.entries.first { it.label == label }.name
            }
            Action("Add a milestone", !state.prefs.grandparent) {
                edit = Milestone(childId = child.id, stage = stage, title = "")
            }
        }
        items(rows.filter { it.stage == stage }, key = { it.id }) { m ->
            EntryRow(
                m.title,
                m.completedOn?.let { "Celebrated · ${LocalDate.ofEpochDay(it)}" } ?: "Still growing",
            ) {
                edit = m
            }
        }
        item { Section("Ideas for this chapter") }
        items(
            suggestions[stage].orEmpty().filter { title ->
                rows.none { it.stage == stage && it.title == title }
            }
        ) { title ->
            EntryRow(title, "Make this part of their story") {
                edit = Milestone(childId = child.id, stage = stage, title = title)
            }
        }
    }
    edit?.let { m -> MilestoneEditor(m, rows.any { it.id == m.id }, vm, state) { edit = null } }
}

@Composable
private fun MilestoneEditor(
    m: Milestone,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    var title by remember { mutableStateOf(m.title) }
    var description by remember { mutableStateOf(m.description) }
    var notes by remember { mutableStateOf(m.notes) }
    var done by remember { mutableStateOf(m.completedOn != null) }
    var date by remember {
        mutableStateOf(m.completedOn?.let(LocalDate::ofEpochDay) ?: LocalDate.now())
    }
    var leaf by remember { mutableStateOf(false) }
    var deleting by remember { mutableStateOf(false) }
    Editor("A growing moment", close) {
        if (state.prefs.grandparent) {
            Section(title)
            Text(description)
            Muted(if (done) "Completed · $date" else "Not yet recorded")
            Text(notes)
        } else {
            Field("Title", title, { title = it })
            Field("Description", description, { description = it }, lines = 2)
            com.nothatcher.sproutbook.features.profile.SettingSwitch("Completed", done) {
                done = it
            }
            if (done) {
                DateButton("Date", date) { date = it }
                if (m.memoryId == null)
                    com.nothatcher.sproutbook.features.profile.SettingSwitch(
                        "Create a memory leaf",
                        leaf,
                    ) {
                        leaf = it
                    }
                else Muted("This moment already has a memory leaf.")
            }
            Field("Notes", notes, { notes = it }, lines = 2)
            Action("Save milestone") {
                vm.perform {
                    vm.repo.saveMilestone(
                        m.copy(
                            title = title,
                            description = description,
                            notes = notes,
                            completedOn = if (done) date.toEpochDay() else null,
                        ),
                        leaf,
                    )
                    close()
                }
            }
            if (existing) TextButton(onClick = { deleting = true }) { Text("Delete milestone") }
            Muted("Changing or deleting a milestone keeps any memory leaf you created.")
        }
    }
    if (deleting)
        ConfirmDelete("this milestone", { deleting = false }) {
            vm.perform("Milestone removed") {
                vm.repo.write { vm.repo.db.milestones().delete(m.id, m.childId) }
                close()
            }
        }
}
