package com.nothatcher.sproutbook.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun Page(title: String, subtitle: String = "", content: LazyListScope.() -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            WoodlandHeader(title, subtitle)
        }
        content()
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f)),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
fun Muted(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
fun EntryRow(title: String, detail: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f)),
    ) {
        Row(
            Modifier.fillMaxWidth().heightIn(min = 88.dp).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            WoodlandIcon(woodlandIconFor(title), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Section(title)
                Muted(detail)
            }
            WoodlandIcon(com.nothatcher.sproutbook.R.drawable.woodland_next,
                modifier = Modifier.padding(start = 12.dp).size(16.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun Empty(title: String, detail: String) {
    Panel {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WoodlandMotionAsset(if ("memor" in title.lowercase()) "memory-tree" else "brand-sprout-book",
                Modifier.size(48.dp), dark = MaterialTheme.colorScheme.background.red < .3f)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Section(title)
                Muted(detail)
            }
        }
    }
}

@Composable
fun Action(label: String, enabled: Boolean = true, onClick: () -> Unit) {
    var replay by remember { mutableIntStateOf(0) }
    Button(
        { replay++; onClick() },
        Modifier.fillMaxWidth().heightIn(min = 52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
    ) {
        WoodlandIcon(woodlandIconFor(label), animated = replay > 0, replay = replay)
        Spacer(Modifier.width(8.dp))
        Text(label)
    }
}

@Composable
fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    numeric: Boolean = false,
    lines: Int = 1,
) {
    OutlinedTextField(
        value,
        onChange,
        Modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = lines == 1,
        minLines = lines,
        keyboardOptions =
            KeyboardOptions(
                keyboardType = if (numeric) KeyboardType.Decimal else KeyboardType.Text
            ),
        shape = RoundedCornerShape(14.dp),
    )
}

@Composable
fun Choices(options: List<String>, selected: String, onChoose: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { FilterChip(selected == it, { onChoose(it) }, label = { Text(it) }) }
    }
}

@Composable
fun DateButton(label: String, date: LocalDate?, onPick: (LocalDate) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            val d = date ?: LocalDate.now()
            DatePickerDialog(
                    context,
                    { _, y, m, day -> onPick(LocalDate.of(y, m + 1, day)) },
                    d.year,
                    d.monthValue - 1,
                    d.dayOfMonth,
                )
                .show()
        },
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        WoodlandIcon(com.nothatcher.sproutbook.R.drawable.woodland_schedule)
        Spacer(Modifier.width(8.dp))
        Text("$label · ${date ?: "Choose date"}")
    }
}

@Composable
fun TimeButton(time: LocalTime, use24: Boolean = false, onPick: (LocalTime) -> Unit) {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            TimePickerDialog(
                    context,
                    { _, h, m -> onPick(LocalTime.of(h, m)) },
                    time.hour,
                    time.minute,
                    use24,
                )
                .show()
        },
        Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        WoodlandIcon(com.nothatcher.sproutbook.R.drawable.woodland_clock)
        Spacer(Modifier.width(8.dp))
        Text("Time · ${time.format(DateTimeFormatter.ofPattern(if(use24)"HH:mm" else "h:mm a"))}")
    }
}

@Composable
fun Editor(title: String, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            Modifier.fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineMedium)
            content()
        }
    }
}

@Composable
fun ConfirmDelete(title: String, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete $title?") },
        text = { Text("This removes the record from this device. Restore requires a backup.") },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Keep") } },
    )
}

fun stamp(millis: Long, use24: Boolean = false) =
    Instant.ofEpochMilli(millis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(if (use24) "MMM d · HH:mm" else "MMM d · h:mm a"))

fun at(date: LocalDate, time: LocalTime) =
    date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

fun dateOf(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
fun SourceLink(label: String, url: String) {
    val handler = androidx.compose.ui.platform.LocalUriHandler.current
    val context = LocalContext.current
    TextButton(
        onClick = {
            runCatching { handler.openUri(url) }
                .onFailure {
                    android.widget.Toast.makeText(
                            context,
                            "No browser available. The guidance remains readable here.",
                            android.widget.Toast.LENGTH_LONG,
                        )
                        .show()
                }
        }
    ) {
        Text(label)
    }
}
