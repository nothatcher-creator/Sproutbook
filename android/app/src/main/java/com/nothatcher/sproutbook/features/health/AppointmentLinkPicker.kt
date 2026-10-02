package com.nothatcher.sproutbook.features.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.data.Appointment
import com.nothatcher.sproutbook.data.AppointmentDao
import com.nothatcher.sproutbook.ui.Muted
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

internal fun visitDetails(visit: Appointment, time24: Boolean): String {
    val date = Instant.ofEpochMilli(visit.startsAt).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(if (time24) "MMM d, yyyy · HH:mm" else "MMM d, yyyy · h:mm a"))
    return if (visit.location.isBlank()) date else "$date · ${visit.location}"
}

/** Selection edits only the health draft; repository guards still apply on save. */
@Composable
internal fun AppointmentLinkPicker(
    dao: AppointmentDao,
    childId: String,
    selectedId: String?,
    selectedVisit: Appointment?,
    time24: Boolean,
    onSelect: (String?) -> Unit,
) {
    var opened by remember { mutableStateOf(false) }
    selectedVisit?.takeIf { it.id == selectedId }?.let {
        Text(it.title)
        Muted(visitDetails(it, time24))
    } ?: Muted(if (selectedId == null) "No linked appointment" else "Appointment unavailable. Choose another or clear the link.")
    OutlinedButton(
        onClick = { opened = true },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) { Text("Choose appointment") }

    if (opened) {
        var query by remember { mutableStateOf("") }
        var limit by remember { mutableIntStateOf(50) }
        val search = query.trim()
        // A new search must not briefly display the previous query's results.
        // Keep existing rows while extending the same search so pagination stays put.
        val candidates: List<Appointment>? = key(dao, childId, search) {
            remember(limit) { dao.search(childId, search, limit + 1) }
                .collectAsStateWithLifecycle(initialValue = null).value
        }
        fun choose(id: String?) {
            onSelect(id)
            opened = false
        }
        AlertDialog(
            onDismissRequest = { opened = false },
            title = { Text("Link an appointment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; limit = 50 },
                        label = { Text("Search title or place") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp)
                            .testTag("health-appointment-options"),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item(key = "none") {
                            VisitOption("No linked appointment", "", selectedId == null,
                                "health-appointment-none") { choose(null) }
                        }
                        when {
                            candidates == null -> item { Text("Loading appointments…") }
                            candidates!!.isEmpty() -> item {
                                Text(if (query.isBlank()) "No appointments for this child yet." else "No matching appointments. Try another title or place.")
                            }
                            else -> {
                                items(candidates!!.take(limit), key = { "visit-${it.id}" }) { visit ->
                                    VisitOption(visit.title, visitDetails(visit, time24),
                                        visit.id == selectedId, "health-appointment-${visit.id}") {
                                        choose(visit.id)
                                    }
                                }
                                if (candidates!!.size > limit) item(key = "more") {
                                    TextButton(onClick = { limit += 50 }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Load more appointments")
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { opened = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun VisitOption(title: String, details: String, selected: Boolean, tag: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(selected = selected, onClick = null)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title)
            if (details.isNotEmpty()) Text(details, style = MaterialTheme.typography.bodySmall)
        }
    }
}
