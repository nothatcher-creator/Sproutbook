package com.nothatcher.sproutbook.features.bottles

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.FeedingMath
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.features.feeding.volume
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun BottlePrepScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    val child = state.child ?: return
    var limit by rememberSaveable { mutableIntStateOf(200) }
    var filter by rememberSaveable { mutableStateOf("Prepared") }
    val rows by
        remember(child.id, limit) { vm.repo.db.bottlePreps().observe(child.id, limit + 1) }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<BottlePrep?>(null) }
    Page("Prepared bottles", "From preparation to feeding, kept together") {
        item {
            Action("Prepare a bottle", !state.prefs.grandparent) {
                edit =
                    BottlePrep(
                        childId = child.id,
                        preparedAt = System.currentTimeMillis(),
                        amountMl = 0.0,
                    )
            }
            Choices(listOf("Prepared", "Used", "Discarded", "All"), filter) { filter = it }
            Muted(
                "Preparation times are a record only, not a storage-safety timer. Use your product instructions and clinician's guidance for preparation and storage."
            )
        }
        val shown = rows.take(limit).filter { filter == "All" || it.status == filter }
        if (shown.isEmpty())
            item {
                Empty(
                    "Ready for the next little feed",
                    "Prepared bottles stay here until used or discarded.",
                )
            }
        items(shown, key = { it.id }) { b ->
            EntryRow(
                "${b.status} · ${volume(b.amountMl,state.prefs.volumeUnit)}",
                "${b.contents} · ${stamp(b.preparedAt,state.prefs.time24)}",
            ) {
                edit = b
            }
        }
        if (rows.size > limit) item { Action("Load older preparations") { limit += 200 } }
        item {
            EntryRow("Feeding history", "All bottle, breast and pump records") { go("feeding") }
        }
    }
    edit?.let { b ->
        val existing = rows.any { it.id == b.id }
        var amount by
            remember(b.id) {
                mutableStateOf(
                    if (b.amountMl == 0.0) ""
                    else
                        (if (state.prefs.volumeUnit == "mL") b.amountMl
                            else FeedingMath.ounces(b.amountMl))
                            .toString()
                )
            }
        var contents by remember(b.id) { mutableStateOf(b.contents) }
        var notes by remember(b.id) { mutableStateOf(b.notes) }
        var date by remember(b.id) { mutableStateOf(dateOf(b.preparedAt)) }
        var time by
            remember(b.id) {
                mutableStateOf(
                    Instant.ofEpochMilli(b.preparedAt).atZone(ZoneId.systemDefault()).toLocalTime()
                )
            }
        var deleting by remember { mutableStateOf(false) }
        var consumed by remember(b.id) { mutableStateOf(amount) }
        val draftAmount =
            amount.replace(',', '.').toDoubleOrNull()?.let {
                if (state.prefs.volumeUnit == "mL") it else FeedingMath.ml(it)
            }
        val dirty =
            draftAmount == null ||
                kotlin.math.abs(draftAmount - b.amountMl) > 0.000001 ||
                contents != b.contents ||
                notes != b.notes ||
                at(date, time) != b.preparedAt
        Editor("Bottle preparation", { edit = null }) {
            if (state.prefs.grandparent) {
                Text("${b.status} · ${volume(b.amountMl,state.prefs.volumeUnit)}")
                Muted(stamp(b.preparedAt, state.prefs.time24))
                Text(b.notes)
            } else {
                if (b.status == "Prepared") {
                    Choices(listOf("Formula", "Breast milk"), contents) { contents = it }
                    Field(
                        "Prepared amount · ${state.prefs.volumeUnit}",
                        amount,
                        { amount = it },
                        numeric = true,
                    )
                    DateButton("Prepared date", date) { date = it }
                    TimeButton(time, state.prefs.time24) { time = it }
                } else {
                    Section("${b.status} · ${volume(b.amountMl,state.prefs.volumeUnit)}")
                    Muted(stamp(b.preparedAt, state.prefs.time24))
                }
                Field("Preparation notes", notes, { notes = it }, lines = 2)
                Action("Save preparation") {
                    vm.perform("Preparation saved") {
                        val parsed = amount.replace(',', '.').toDoubleOrNull() ?: 0.0
                        vm.repo.saveBottlePrep(
                            b.copy(
                                amountMl =
                                    if (b.status != "Prepared") b.amountMl
                                    else if (state.prefs.volumeUnit == "mL") parsed
                                    else FeedingMath.ml(parsed),
                                contents = contents,
                                preparedAt = at(date, time),
                                notes = notes,
                            )
                        )
                        edit = null
                    }
                }
                if (existing && b.status == "Prepared") {
                    if (dirty) Muted("Save preparation changes before recording use or discard.")
                    Field(
                        "Amount actually fed · ${state.prefs.volumeUnit}",
                        consumed,
                        { consumed = it },
                        numeric = true,
                    )
                    Muted(
                        "Records the amount actually fed at the current time; the prepared amount stays in this record."
                    )
                    Action("Use & log feeding", !dirty) {
                        vm.perform("Bottle used · feeding logged") {
                            val parsed = consumed.replace(',', '.').toDoubleOrNull() ?: 0.0
                            vm.repo.finishBottle(
                                b.id,
                                child.id,
                                true,
                                consumedMl =
                                    if (state.prefs.volumeUnit == "mL") parsed
                                    else FeedingMath.ml(parsed),
                            )
                            edit = null
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            vm.perform("Bottle discarded") {
                                vm.repo.finishBottle(b.id, child.id, false)
                                edit = null
                            }
                        },
                        enabled = !dirty,
                    ) {
                        Text("Discard bottle")
                    }
                }
                if (b.feedId != null)
                    Muted(
                        "The linked feeding stays in Feeding history if this preparation is deleted."
                    )
                if (existing)
                    TextButton(onClick = { deleting = true }) { Text("Delete preparation") }
            }
        }
        if (deleting)
            ConfirmDelete("this preparation", { deleting = false }) {
                vm.perform("Preparation removed") {
                    vm.repo.write { vm.repo.db.bottlePreps().delete(b.id, child.id) }
                    edit = null
                }
            }
    }
}
