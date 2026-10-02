package com.nothatcher.sproutbook.features.growth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.GrowthMath
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*
import java.util.Locale

private fun number(value: Double) = String.format(Locale.getDefault(), "%.2f", value)

private fun measurement(h: HealthRecord, unit: String): Double? =
    h.value
        .replace(',', '.')
        .toDoubleOrNull()
        ?.let { GrowthMath.canonical(h.kind, it, h.unit) }
        ?.let { GrowthMath.display(h.kind, it, unit) }

@Composable
fun GrowthScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    var kind by rememberSaveable { mutableStateOf("Weight") }
    var heightUnit by rememberSaveable { mutableStateOf("cm") }
    var limit by rememberSaveable { mutableIntStateOf(200) }
    val unit = if (kind == "Weight") state.prefs.weightUnit else heightUnit
    val rows by
        remember(child.id, kind, limit) {
                vm.repo.db.healthRecords().history(child.id, kind, limit + 1)
            }
            .collectAsStateWithLifecycle(emptyList())
    var edit by remember { mutableStateOf<HealthRecord?>(null) }
    val points =
        remember(rows, unit) {
            rows.take(30).reversed().mapNotNull { h ->
                measurement(h, unit)?.let { h.recordedAt to it }
            }
        }
    Page("Growth journal", "Their measurements, one growing story") {
        item {
            Choices(listOf("Weight", "Height"), kind) {
                kind = it
                limit = 200
            }
            if (kind == "Height") Choices(listOf("cm", "in"), heightUnit) { heightUnit = it }
            Action("Add measurement", !state.prefs.grandparent) {
                edit =
                    HealthRecord(
                        childId = child.id,
                        kind = kind,
                        recordedAt = System.currentTimeMillis(),
                        title = kind,
                        unit = if (kind == "Weight") "kg" else "cm",
                    )
            }
        }
        item {
            Panel {
                Section("Recorded $kind")
                points.lastOrNull()?.let {
                    Text(
                        "${number(it.second)} $unit",
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
                Muted("Your most recent 30 measurements · $unit")
                GrowthChart(points, unit)
                Muted(
                    "A record of measurements, without growth percentiles or medical interpretation. Share concerns with your clinician."
                )
                if (rows.take(30).any { measurement(it, unit) == null })
                    Muted(
                        "Records with unrecognized units remain in your history and are excluded from the chart."
                    )
            }
        }
        if (rows.isEmpty())
            item {
                Empty(
                    "Growing at their own pace",
                    "Add a weight or height measurement to start their journal.",
                )
            }
        items(rows.take(limit), key = { it.id }) { h ->
            EntryRow(
                "${h.kind} · ${measurement(h,unit)?.let { number(it) + " " + unit } ?: (h.value + " " + h.unit)}",
                stamp(h.recordedAt, state.prefs.time24),
            ) {
                edit = h
            }
        }
        if (rows.size > limit) item { Action("Load older measurements") { limit += 200 } }
    }
    edit?.let { h ->
        val editorUnit = if (h.kind == "Weight") state.prefs.weightUnit else heightUnit
        val converted = measurement(h, editorUnit)
        val initialValue = remember(h.id, editorUnit) { converted?.toString() ?: h.value }
        var value by remember(h.id, editorUnit) { mutableStateOf(initialValue) }
        var notes by remember(h.id) { mutableStateOf(h.notes) }
        var date by remember(h.id) { mutableStateOf(dateOf(h.recordedAt)) }
        var time by
            remember(h.id) {
                mutableStateOf(
                    Instant.ofEpochMilli(h.recordedAt).atZone(ZoneId.systemDefault()).toLocalTime()
                )
            }
        var deleting by remember { mutableStateOf(false) }
        val existing = rows.any { it.id == h.id }
        Editor("${h.kind} measurement", { edit = null }) {
            if (state.prefs.grandparent) {
                Text("${h.value} ${h.unit}")
                Muted(stamp(h.recordedAt, state.prefs.time24))
                Text(h.notes)
            } else {
                if (converted != null || h.value.isBlank()) {
                    Muted("Enter ${h.kind.lowercase()} in $editorUnit")
                    Field("Measurement", value, { value = it }, numeric = true)
                } else {
                    Section("Original measurement · ${h.value} ${h.unit}")
                    Muted(
                        "This unit is not recognized. Notes and dates can change here; edit its value and unit in the Health journal."
                    )
                }
                DateButton("Measurement date", date) { date = it }
                TimeButton(time, state.prefs.time24) { time = it }
                Field("Measurement notes", notes, { notes = it }, lines = 2)
                Action("Save measurement") {
                    vm.perform("Measurement saved") {
                        val unchanged = existing && value == initialValue
                        val canonical =
                            value.replace(',', '.').toDoubleOrNull()?.let {
                                GrowthMath.canonical(h.kind, it, editorUnit)
                            }
                        require(unchanged || canonical != null) { "Enter a positive measurement." }
                        vm.repo.saveHealth(
                            h.copy(
                                value = if (unchanged) h.value else canonical.toString(),
                                unit =
                                    if (unchanged) h.unit
                                    else if (h.kind == "Weight") "kg" else "cm",
                                recordedAt = at(date, time),
                                notes = notes,
                            )
                        )
                        edit = null
                    }
                }
                if (existing)
                    TextButton(onClick = { deleting = true }) { Text("Delete measurement") }
            }
        }
        if (deleting)
            ConfirmDelete("this measurement", { deleting = false }) {
                vm.perform("Measurement removed") {
                    vm.repo.write { vm.repo.db.healthRecords().delete(h.id, child.id) }
                    edit = null
                }
            }
    }
}

@Composable
private fun GrowthChart(points: List<Pair<Long, Double>>, unit: String) {
    val color = MaterialTheme.colorScheme.primary
    val grid = MaterialTheme.colorScheme.outlineVariant
    val description =
        if (points.isEmpty()) "No measurements recorded"
        else
            "Recorded trend in $unit. ${points.size} measurements, from ${number(points.first().second)} to ${number(points.last().second)}. Exact values are available in the history below."
    Canvas(Modifier.fillMaxWidth().height(140.dp).semantics { contentDescription = description }) {
        val inset = 12.dp.toPx()
        val width = size.width - 2 * inset
        val height = size.height - 2 * inset
        for (i in 0..3) {
            val y = inset + height * i / 3
            drawLine(grid, Offset(inset, y), Offset(inset + width, y), 1.dp.toPx())
        }
        if (points.isNotEmpty()) {
            val min = points.minOf { it.second }
            val range = (points.maxOf { it.second } - min).coerceAtLeast(0.1)
            val start = points.minOf { it.first }
            val span = (points.maxOf { it.first } - start).coerceAtLeast(1)
            val locations =
                points.map { (time, value) ->
                    Offset(
                        if (points.size == 1) size.width / 2
                        else inset + width * ((time - start).toDouble() / span).toFloat(),
                        inset + height * (1 - ((value - min) / range).toFloat()),
                    )
                }
            locations.zipWithNext { a, b -> drawLine(color, a, b, 2.dp.toPx()) }
            locations.forEach { drawCircle(color, 4.dp.toPx(), it) }
        }
    }
}
