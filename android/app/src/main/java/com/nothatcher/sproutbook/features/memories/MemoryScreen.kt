package com.nothatcher.sproutbook.features.memories

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.nothatcher.sproutbook.core.TreeLayout
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.PhotoStore
import com.nothatcher.sproutbook.ui.*
import java.time.*

@Composable
fun MemoryScreen(vm: FamilyViewModel, state: FamilyState, openId: String? = null,
    addNew: Boolean = false, onAddHandled: () -> Unit = {}) {
    val child = state.child ?: return
    val observedRows by
        remember(child.id) { vm.repo.db.memorys().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    val rows = remember(observedRows, child.id) { observedRows.filter { it.childId == child.id } }
    var mode by rememberSaveable(child.id) { mutableStateOf("Tree") }
    var arrange by rememberSaveable(child.id) { mutableStateOf(false) }
    var chapter by rememberSaveable(child.id) { mutableIntStateOf(0) }
    var viewingId by rememberSaveable(child.id) { mutableStateOf<String?>(null) }
    var edit by remember(child.id) { mutableStateOf<Memory?>(null) }
    var opened by rememberSaveable(child.id, openId) { mutableStateOf(false) }
    LaunchedEffect(openId, rows) {
        if (!opened && openId != null)
            rows
                .find { it.id == openId }
                ?.let {
                    viewingId = it.id
                    chapter = it.chapter
                    opened = true
                }
    }
    var addHandled by rememberSaveable(child.id, addNew) { mutableStateOf(false) }
    LaunchedEffect(addNew, state.prefs.grandparent) {
        if (addNew && !addHandled) {
            addHandled = true
            onAddHandled()
            if (state.prefs.grandparent) vm.message("Grandparent mode is read only.")
            else edit = Memory(childId = child.id, title = "", occurredOn = LocalDate.now().toEpochDay())
        }
    }
    LaunchedEffect(state.prefs.grandparent) {
        if (state.prefs.grandparent) arrange = false
    }
    val canArrange = arrange && !state.prefs.grandparent
    val last = rows.maxOfOrNull { it.chapter } ?: 0
    val visible = remember(rows, chapter) { rows.filter { it.chapter == chapter } }
    Page("Growing memories", "${rows.size} moments in ${child.name}'s story") {
        item {
            Action("Add a memory", !state.prefs.grandparent) {
                edit =
                    Memory(
                        childId = child.id,
                        title = "",
                        occurredOn = LocalDate.now().toEpochDay(),
                    )
            }
        }
        item {
            Choices(listOf("Tree", "Timeline"), mode) {
                mode = it
                arrange = false
            }
        }
        if (mode == "Tree") {
            item {
                Panel {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Section("Chapter ${chapter+1}")
                            Muted("${child.treeStyle} woodland · ${visible.size} of ${TreeLayout.anchors.size} leaves")
                        }
                        TextButton(
                            onClick = { arrange = !arrange },
                            enabled = !state.prefs.grandparent && visible.isNotEmpty(),
                        ) {
                            Text(if (arrange) "Done arranging" else "Arrange")
                        }
                    }
                    MemoryTree(
                        visible,
                        canArrange,
                        onOpen = { viewingId = it.id },
                        onMove = { id, slot ->
                            vm.perform("Leaf placed") { vm.repo.arrangeMemory(child.id, id, slot) }
                        },
                        treeStyle = child.treeStyle,
                    )
                    if (canArrange)
                        Muted("Long press and drag a leaf to a glowing branch. Or tap a leaf, then a branch.")
                    else {
                        visible.maxByOrNull { it.occurredOn }?.let { newest ->
                            Surface(
                                onClick = { viewingId = newest.id },
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ) {
                                Row(Modifier.fillMaxWidth().heightIn(min = 52.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    LeafMarker(newest.category)
                                    Column(Modifier.weight(1f)) {
                                        Text("Newest leaf", style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.primary)
                                        Text(newest.title, style = MaterialTheme.typography.bodyLarge, maxLines = 1,
                                            overflow = TextOverflow.Ellipsis)
                                    }
                                    Muted(friendlyDate(LocalDate.ofEpochDay(newest.occurredOn)))
                                }
                            }
                        }
                        if (visible.isNotEmpty()) LeafLegend(visible.map { leafGroup(it.category) }.toSet())
                        Muted("Tap a leaf to revisit its story. New moments grow on the little buds.")
                    }
                    if (last > 0)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { chapter-- }, enabled = chapter > 0) {
                                Text("Previous")
                            }
                            Muted("${chapter + 1} of ${last + 1}")
                            TextButton(onClick = { chapter++ }, enabled = chapter < last) {
                                Text("Next chapter")
                            }
                        }
                }
            }
            if (visible.isEmpty())
                item {
                    Empty(
                        "Their first leaf is waiting",
                        "Add a first, a funny moment, or something you never want to forget.",
                    )
                }
        } else {
            if (rows.isEmpty())
                item {
                    Empty(
                        "A story waiting to begin",
                        "Your memories will appear here in date order.",
                    )
                }
            rows.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.occurredOn)) }.forEach { (month, moments) ->
                item(key = "month-$month") {
                    Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                        Modifier.padding(top = 6.dp, start = 4.dp).semantics { heading() },
                        style = MaterialTheme.typography.titleLarge)
                }
                items(moments, key = { it.id }) { m ->
                    TimelineMoment(m, last = m == moments.last()) { viewingId = m.id }
                }
            }
        }
    }
    if (edit == null) {
        rows.find { it.id == viewingId }?.let { memory ->
            key(memory.id) {
                MemoryLeafCard(
                    memory = memory,
                    child = child,
                    readOnly = state.prefs.grandparent,
                    onDismiss = { viewingId = null },
                    onEdit = { if (!state.prefs.grandparent) edit = memory },
                )
            }
        }
    }
    edit?.let { m ->
        MemoryEditor(m, rows.any { it.id == m.id }, vm, state, { chapter = it }, { edit = null })
    }
}

@Composable
private fun MemoryEditor(
    value: Memory,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    onChapter: (Int) -> Unit,
    close: () -> Unit,
) {
    val context = LocalContext.current
    var title by rememberSaveable(value.id) { mutableStateOf(value.title) }
    var notes by rememberSaveable(value.id) { mutableStateOf(value.notes) }
    var category by rememberSaveable(value.id) { mutableStateOf(value.category) }
    var photo by rememberSaveable(value.id) { mutableStateOf(value.photo) }
    var date by remember(value.id) { mutableStateOf(LocalDate.ofEpochDay(value.occurredOn)) }
    var deleting by remember { mutableStateOf(false) }
    val picker =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) vm.perform("Photo attached") { photo = PhotoStore.copy(context, uri) }
        }
    Editor(if (existing) "A moment to keep" else "A new leaf", close) {
        if (state.prefs.grandparent) {
            Section(title)
            Muted("$date · $category")
            Text(notes)
        } else {
            Field("Memory title", title, { title = it })
            DateButton("Date", date) { date = it }
            Choices(memoryCategories, category) { category = it }
            Field("Notes", notes, { notes = it }, lines = 4)
        }
        state.child?.birthday?.let { birth ->
            val birthday = LocalDate.ofEpochDay(birth)
            if (date >= birthday) {
                val age = Period.between(birthday, date)
                Muted(
                    "Age at this moment · ${age.years} years, ${age.months} months, ${age.days} days"
                )
            }
        }
        photo?.let { name ->
            val bitmap by
                produceState<android.graphics.Bitmap?>(null, name) {
                    this.value = PhotoStore.load(context, name)
                }
            bitmap?.let {
                Image(
                    it.asImageBitmap(),
                    "Memory photo",
                    Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        if (!state.prefs.grandparent) {
            OutlinedButton(onClick = { picker.launch(arrayOf("image/*")) }) {
                Text(if (photo == null) "Add a photo" else "Change photo")
            }
            if (photo != null) TextButton(onClick = { photo = null }) { Text("Remove photo") }
            Action("Save memory") {
                vm.perform {
                    val saved =
                        vm.repo.saveMemory(
                            value.copy(
                                title = title,
                                notes = notes,
                                category = category,
                                photo = photo,
                                occurredOn = date.toEpochDay(),
                            )
                        )
                    onChapter(saved.chapter)
                    close()
                }
            }
            if (existing)
                TextButton(onClick = { deleting = true }) {
                    Text("Delete memory", color = MaterialTheme.colorScheme.error)
                }
        }
    }
    if (deleting)
        ConfirmDelete(title, { deleting = false }) {
            vm.perform("Memory deleted") {
                vm.repo.write { vm.repo.db.memorys().delete(value.id, value.childId) }
                close()
            }
        }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LeafLegend(present: Set<String>) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        leafGroups.filter { it.first in present }.forEach { (label, color) ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Canvas(Modifier.size(10.dp)) { drawCircle(color) }
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/** A timeline entry: a coloured leaf on a quiet vine, then the moment itself. */
@Composable
private fun TimelineMoment(m: Memory, last: Boolean, onOpen: () -> Unit) {
    val vine = MaterialTheme.colorScheme.outlineVariant
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Box(Modifier.width(40.dp).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
            Canvas(Modifier.fillMaxSize()) {
                drawLine(vine, Offset(size.width / 2, 0f), Offset(size.width / 2, if (last) size.height / 2 else size.height),
                    2.dp.toPx())
            }
            Box(Modifier.padding(top = 22.dp)) { LeafMarker(m.category) }
        }
        Surface(
            onClick = onOpen,
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .55f)),
            modifier = Modifier.weight(1f).padding(vertical = 5.dp),
        ) {
            Column(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(m.title, style = MaterialTheme.typography.titleMedium)
                Muted(listOf(friendlyDate(LocalDate.ofEpochDay(m.occurredOn)), m.category,
                    if (m.photo != null) "Photo" else "").filter { it.isNotEmpty() }.joinToString(" · "))
                if (m.notes.isNotBlank()) Text(m.notes, style = MaterialTheme.typography.bodyMedium, maxLines = 2,
                    overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
