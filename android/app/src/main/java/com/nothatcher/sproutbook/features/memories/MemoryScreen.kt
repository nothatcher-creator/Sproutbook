package com.nothatcher.sproutbook.features.memories

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
fun MemoryScreen(vm: FamilyViewModel, state: FamilyState, openId: String? = null) {
    val child = state.child ?: return
    val rows by
        remember(child.id) { vm.repo.db.memorys().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var mode by rememberSaveable { mutableStateOf("Tree") }
    var arrange by rememberSaveable { mutableStateOf(false) }
    var chapter by rememberSaveable { mutableIntStateOf(0) }
    var edit by remember { mutableStateOf<Memory?>(null) }
    var opened by remember(openId) { mutableStateOf(false) }
    LaunchedEffect(openId, rows) {
        if (!opened && openId != null)
            rows
                .find { it.id == openId }
                ?.let {
                    edit = it
                    chapter = it.chapter
                    opened = true
                }
    }
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
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Section("Chapter ${chapter+1}")
                        TextButton(
                            onClick = { arrange = !arrange },
                            enabled = !state.prefs.grandparent && visible.isNotEmpty(),
                        ) {
                            Text(if (arrange) "Done arranging" else "Arrange")
                        }
                    }
                    MemoryTree(
                        visible,
                        arrange,
                        onOpen = { edit = it },
                        onMove = { id, slot ->
                            vm.perform("Leaf placed") { vm.repo.arrangeMemory(child.id, id, slot) }
                        },
                        treeStyle = child.treeStyle,
                    )
                    Muted(
                        if (arrange)
                            "Long press and drag a leaf to a glowing branch. Or tap a leaf, then a branch."
                        else "Tap a leaf to revisit its story. New moments bring this tree to life."
                    )
                    if (last > 0)
                        Row {
                            TextButton(onClick = { chapter-- }, enabled = chapter > 0) {
                                Text("Previous")
                            }
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
            items(rows, key = { it.id }) { m ->
                EntryRow(
                    m.title,
                    "${LocalDate.ofEpochDay(m.occurredOn)} · ${m.category}${if(m.photo!=null)" · Photo" else ""}",
                ) {
                    edit = m
                }
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
