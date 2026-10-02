package com.nothatcher.sproutbook.features.milk

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.FeedingMath
import com.nothatcher.sproutbook.core.RecordTime
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.features.feeding.volume
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun MilkScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    var filter by rememberSaveable { mutableStateOf("Frozen") }
    val rows by
        remember(child.id, filter, limit) {
                vm.repo.db.milkContainers().history(child.id, filter, limit + 1)
            }
            .collectAsStateWithLifecycle(emptyList())
    val stock by
        remember(child.id) { vm.repo.db.milkContainers().frozenSummary(child.id) }
            .collectAsStateWithLifecycle(MilkStock())
    var edit by remember { mutableStateOf<MilkContainer?>(null) }
    var choosing by remember { mutableStateOf(false) }
    var pumpLimit by remember { mutableIntStateOf(40) }
    Page("Milk freezer", "A little supply, kept in order") {
        item {
            Panel {
                Section("In your freezer")
                Text(
                    volume(stock.totalMl, state.prefs.volumeUnit),
                    style = MaterialTheme.typography.headlineLarge,
                )
                Muted("${stock.count} frozen containers · Oldest recorded first")
                Muted(
                    "This is an inventory journal. Dates and status do not establish that milk is safe to use."
                )
            }
        }
        item {
            Action("Add container", !state.prefs.grandparent) {
                edit =
                    MilkContainer(
                        childId = child.id,
                        label = "",
                        storedAt = System.currentTimeMillis(),
                        amountMl = 0.0,
                    )
            }
            OutlinedButton(onClick = { choosing = true }, enabled = !state.prefs.grandparent) {
                Text("Store a pump session")
            }
        }
        item {
            Choices(listOf("Frozen", "Used", "Discarded", "All"), filter) {
                filter = it
                limit = 200
            }
        }
        if (rows.isEmpty())
            item {
                Empty(
                    if (filter == "Frozen") "Room for a little reserve"
                    else "No containers in this view",
                    "Add a container manually, or record the full amount of a saved pump session once. Choose another filter to see your history.",
                )
            }
        items(rows.take(limit), key = { it.id }) { m ->
            EntryRow(
                m.label,
                "${m.status} · ${volume(m.amountMl,state.prefs.volumeUnit)}\n${stamp(m.storedAt,state.prefs.time24)}${m.location.takeIf{it.isNotBlank()}?.let{" · $it"} ?: ""}",
            ) {
                edit = m
            }
        }
        if (rows.size > limit) item { Action("Load more containers") { limit += 200 } }
    }
    if (choosing) {
        val pumps by
            remember(child.id, pumpLimit) {
                    vm.repo.db.milkContainers().unstoredPumps(child.id, pumpLimit + 1)
                }
                .collectAsStateWithLifecycle(emptyList())
        Editor("Choose a saved pump", { choosing = false }) {
            if (state.prefs.grandparent) Muted("Grandparent mode is read-only.")
            else {
                Muted(
                    "The full logged pump amount becomes one frozen container. Rename it or add a location afterward."
                )
                if (pumps.isEmpty())
                    Empty(
                        "No unstored pump sessions",
                        "Log pumping in Feeding first. Sessions already linked to a container stay out of this list.",
                    )
                pumps.take(pumpLimit).forEach { p ->
                    EntryRow(
                        "Pump · ${volume(p.amountMl,state.prefs.volumeUnit)}",
                        stamp(p.startsAt, state.prefs.time24),
                    ) {
                        vm.perform("Pump stored") {
                            vm.repo.storePump(p.id, child.id)
                            choosing = false
                        }
                    }
                }
                if (pumps.size > pumpLimit) Action("Load older pump sessions") { pumpLimit += 40 }
            }
        }
    }
    edit?.let { m -> MilkEditor(m, rows.any { it.id == m.id }, vm, state) { edit = null } }
}

@Composable
private fun MilkEditor(
    m: MilkContainer,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    val unit = state.prefs.volumeUnit
    val initialAmount =
        remember(m.id, unit) {
            if (m.amountMl == 0.0) ""
            else (if (unit == "mL") m.amountMl else FeedingMath.ounces(m.amountMl)).toString()
        }
    var label by remember(m.id) { mutableStateOf(m.label) }
    var amount by remember(m.id) { mutableStateOf(initialAmount) }
    var location by remember(m.id) { mutableStateOf(m.location) }
    var notes by remember(m.id) { mutableStateOf(m.notes) }
    val editZone = remember(m.id) { ZoneId.systemDefault() }
    val initial = Instant.ofEpochMilli(m.storedAt).atZone(editZone)
    var date by remember(m.id) { mutableStateOf(initial.toLocalDate()) }
    var time by remember(m.id) { mutableStateOf(initial.toLocalTime()) }
    val storedAt = RecordTime.edited(m.storedAt, date, time, editZone)
    var deleting by remember { mutableStateOf(false) }
    val dirty =
        label != m.label ||
            amount != initialAmount ||
            location != m.location ||
            notes != m.notes ||
            storedAt != m.storedAt
    Editor("A little reserve", close) {
        if (state.prefs.grandparent) {
            Section(m.label)
            Text("${m.status} · ${volume(m.amountMl,unit)}")
            Muted(stamp(m.storedAt, state.prefs.time24))
            Text(m.location)
            Text(m.notes)
        } else {
            if (m.status == "Frozen") {
                Field("Container label", label, { label = it })
                if (m.pumpId == null)
                    Field("Stored amount · $unit", amount, { amount = it }, numeric = true)
                else Muted("Linked pump · ${volume(m.amountMl,unit)} (original stored amount)")
                DateButton("Stored date", date) { date = it }
                TimeButton(time, state.prefs.time24) { time = it }
                Field("Freezer location (optional)", location, { location = it })
            } else {
                Section(m.label)
                Muted("${m.status} · ${volume(m.amountMl,unit)}")
                Muted(stamp(m.storedAt, state.prefs.time24))
                Muted(m.location)
                Muted("This container is in your history. Its original amount stays recorded.")
            }
            Field("Container notes", notes, { notes = it }, lines = 2)
            Action("Save container") {
                vm.perform("Container saved") {
                    val ml =
                        if (amount == initialAmount) m.amountMl
                        else
                            number(amount, "stored amount").let {
                                if (unit == "mL") it else FeedingMath.ml(it)
                            }
                    vm.repo.saveMilk(
                        if (m.status != "Frozen") m.copy(notes = notes)
                        else
                            m.copy(
                                label = label,
                                amountMl = ml,
                                location = location,
                                notes = notes,
                                storedAt = storedAt,
                            )
                    )
                    close()
                }
            }
            if (existing && m.status == "Frozen") {
                if (dirty) Muted("Save your changes before recording use or discard.")
                Action("Mark used", !dirty) {
                    vm.perform("Container marked used") {
                        vm.repo.finishMilk(m.id, m.childId, true)
                        close()
                    }
                }
                OutlinedButton(
                    onClick = {
                        vm.perform("Container marked discarded") {
                            vm.repo.finishMilk(m.id, m.childId, false)
                            close()
                        }
                    },
                    enabled = !dirty,
                ) {
                    Text("Mark discarded")
                }
                Muted(
                    "Recording use changes inventory only. Log any feeding separately in Feeding."
                )
            }
            if (existing) TextButton(onClick = { deleting = true }) { Text("Delete container") }
        }
    }
    if (deleting)
        ConfirmDelete("this milk container", { deleting = false }) {
            vm.perform("Container removed") {
                vm.repo.write { vm.repo.db.milkContainers().delete(m.id, m.childId) }
                close()
            }
        }
}
