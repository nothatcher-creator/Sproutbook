package com.nothatcher.sproutbook.features.teeth

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate
import kotlin.math.*

fun toothName(index: Int): String {
    val j = index % 10
    val names =
        listOf("second molar", "first molar", "canine", "lateral incisor", "central incisor")
    return "${if(index<10)"Upper" else "Lower"} ${if(j<5)"right" else "left"} ${names[if(j<5)j else 9-j]}"
}

@Composable
fun TeethScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    val teeth by
        remember(child.id) { vm.repo.db.tooths().observe(child.id) }
            .collectAsStateWithLifecycle(emptyList())
    var viewing by remember { mutableStateOf<Tooth?>(null) }
    var changing by remember { mutableStateOf<Tooth?>(null) }
    val haptic = LocalHapticFeedback.current
    Page("Little teeth", if (state.prefs.grandparent) "Tap a tooth to view · Read only" else "Tap to view · Hold a tooth to change its stage") {
        item {
            Panel {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column { Text("${teeth.count { it.stage == "Erupted" }} / 20", style = MaterialTheme.typography.headlineMedium); Muted("Erupted teeth") }
                    Column { Text("${teeth.count { it.stage == "Observed" }}", style = MaterialTheme.typography.headlineMedium); Muted("Observed teeth") }
                    Column { Text("${20 - teeth.count { it.stage != "Not seen" }}", style = MaterialTheme.typography.headlineMedium); Muted("Not seen yet") }
                }
                BoxWithConstraints(Modifier.fillMaxWidth().height(506.dp)) {
                    val width = maxWidth.value
                    val points = remember(width) { archPoints(width, true) + archPoints(width, false) }
                    GumArtwork(Modifier.fillMaxSize(), width, Rose)
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("UPPER", style = MaterialTheme.typography.labelMedium)
                        Muted("Baby teeth")
                        Text("LOWER", style = MaterialTheme.typography.labelMedium)
                    }
                    for (index in 0..19) {
                        val point = points[index]
                        val t = teeth.firstOrNull { it.toothIndex == index }
                            ?: Tooth(childId = child.id, toothIndex = index)
                        val color = when (t.stage) {
                            "Erupted" -> MaterialTheme.colorScheme.primary
                            "Observed" -> Gold
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .3f)
                        }
                        Box(Modifier.offset((point.x - 24).dp, (point.y - 24).dp).size(48.dp)
                            .testTag("tooth-$index")
                            .semantics { contentDescription = "${toothName(index)}, ${t.stage}" }
                            .combinedClickable(onClick = { viewing = t },
                                onLongClickLabel = if (state.prefs.grandparent) null else "Change tooth stage",
                                onLongClick = if (state.prefs.grandparent) null else ({
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    changing = t
                                })), contentAlignment = Alignment.Center) {
                            val j = index % 10
                            val position = if (j < 5) j else 9 - j
                            ToothArtwork(position < 2, position == 2, color, point.rotation)
                            Text(when (t.stage) { "Erupted" -> "✓"; "Observed" -> "•"; else -> "–" },
                                style = MaterialTheme.typography.labelMedium,
                                color = when (t.stage) {
                                    "Erupted" -> MaterialTheme.colorScheme.onPrimary
                                    "Observed" -> androidx.compose.ui.graphics.Color(0xFF352919)
                                    else -> MaterialTheme.colorScheme.onSurface
                                })
                        }
                    }
                }
                Muted("– Not seen    • Observed    ✓ Erupted")
            }
        }
        if (teeth.any { it.stage != "Not seen" }) item {
            Panel {
                Section("Eruption journal")
                teeth.filter { it.stage != "Not seen" }.sortedWith(compareByDescending<Tooth> { it.eruptedOn ?: Long.MIN_VALUE }.thenBy { it.toothIndex }).forEach { t ->
                    TextButton(onClick = { viewing = t }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                        Column(Modifier.fillMaxWidth()) {
                            Section(toothName(t.toothIndex))
                            Muted("${t.stage} · ${t.eruptedOn?.let { LocalDate.ofEpochDay(it).toString() } ?: "Date not recorded"}")
                        }
                    }
                }
            }
        }
        item {
            Panel {
                Section("Their own little timeline")
                Muted(
                    "Teeth arrive at different times. This journal records what you notice; it does not diagnose delayed development. Ask a dental professional about concerns."
                )
                SourceLink(
                    "Canadian Paediatric Society · Teeth",
                    "https://caringforkids.cps.ca/handouts/healthy-living/healthy_teeth_for_children",
                )
            }
        }
    }
    viewing?.let { t ->
        Editor(toothName(t.toothIndex), { viewing = null }) {
            Section(t.stage)
            Muted(
                t.eruptedOn?.let { "Erupted · ${LocalDate.ofEpochDay(it)}" }
                    ?: "No eruption date recorded"
            )
            Muted("To change this record, close this view and long-press the tooth.")
        }
    }
    changing?.let { t ->
        var stage by remember(t.toothIndex) { mutableStateOf(t.stage) }
        var date by
            remember(t.toothIndex) { mutableStateOf(t.eruptedOn?.let(LocalDate::ofEpochDay)) }
        Editor(toothName(t.toothIndex), { changing = null }) {
            Choices(listOf("Not seen", "Observed", "Erupted"), stage) { stage = it }
            if (stage == "Erupted") {
                DateButton("Eruption date (optional)", date) { date = it }
                if (date != null) TextButton(onClick = { date = null }) { Text("Clear date") }
            }
            Action("Save tooth") {
                vm.perform {
                    vm.repo.saveTooth(
                        t.copy(
                            stage = stage,
                            eruptedOn = if (stage == "Erupted") date?.toEpochDay() else null,
                        )
                    )
                    changing = null
                }
            }
        }
    }
}
