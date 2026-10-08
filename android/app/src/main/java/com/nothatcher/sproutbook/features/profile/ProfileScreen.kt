package com.nothatcher.sproutbook.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.core.TreeLayout
import com.nothatcher.sproutbook.core.TreeThemes
import com.nothatcher.sproutbook.features.memories.drawMemoryTreeScenery
import com.nothatcher.sproutbook.features.memories.memoryTreePalette
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate

@Composable
fun ProfileScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    var edit by remember { mutableStateOf<Child?>(null) }
    var adding by remember { mutableStateOf(false) }
    Page("Your family", "Profiles & settings, together") {
        item {
            EntryRow("Home screen organizer", "Arrange Today, choose quick actions and set a background for this child") { go("home-organizer") }
        }
        item {
            EntryRow("Family cupboard", "Keep everyday supplies topped up") { go("inventory") }
            EntryRow("Wishlist", "Save ideas for this child's next chapter") { go("wishlist") }
            EntryRow("Emergency card", "Contacts and important medical information") {
                go("emergency")
            }
        }
        item { Action("Add child", !state.prefs.grandparent) { adding = true } }
        items(state.children, key = { it.id }) { child ->
            EntryRow(
                child.name,
                "${Stage.valueOf(child.stage).label} · ${if(child.id==state.child?.id)"Selected" else "View profile"}\n${child.accent} accent · ${child.treeStyle} tree",
            ) {
                edit = child
            }
        }
        item {
            Panel {
                Section("Make yourself comfortable")
                SettingSwitch("Dark woodland theme", state.prefs.dark) {
                    vm.perform("") { vm.repo.settings.boolean("dark", it) }
                }
                SettingSwitch("24-hour time", state.prefs.time24) {
                    vm.perform("") { vm.repo.settings.boolean("time24", it) }
                }
                SettingSwitch("Reduce motion", state.prefs.reduceMotion) {
                    vm.perform("") { vm.repo.settings.boolean("reduceMotion", it) }
                }
                Muted("Turn Reduce motion off for woodland fireflies, gentle illustrations and animated tab icons. Motion rests during typing, battery saver and when the app is in the background.")
                Text("Bottle units")
                Choices(listOf("mL", "fl oz"), state.prefs.volumeUnit) {
                    vm.perform("") { vm.repo.settings.string("volumeUnit", it) }
                }
                Text("Weight units")
                Choices(listOf("kg", "lb"), state.prefs.weightUnit) {
                    vm.perform("") { vm.repo.settings.string("weightUnit", it) }
                }
            }
        }
        item {
            Panel {
                Section("Grandparent mode")
                Muted(
                    "A clear read-only view for caregivers. Turn this off deliberately when you want to edit records."
                )
                SettingSwitch("Read-only caregiver view", state.prefs.grandparent) {
                    vm.perform("") { vm.repo.settings.boolean("grandparent", it) }
                }
            }
        }
        item { com.nothatcher.sproutbook.features.backup.NotificationSettings(vm, state) }
        item {
            EntryRow("Backup & import", "Keep a portable copy of their story") { go("backup") }
            EntryRow("Diagnostics", "App version and storage status") { go("diagnostics") }
        }
        item { Muted("SproutBook ${BuildConfig.VERSION_NAME} · Your records stay on this device.") }
    }
    if (adding || edit != null)
        ChildEditor(edit, state.prefs.grandparent, vm) {
            adding = false
            edit = null
        }
}

@Composable
fun SettingSwitch(label: String, value: Boolean, change: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        Switch(value, change, Modifier.semantics { contentDescription = label })
    }
}

@Composable
fun ChildEditor(child: Child?, readOnly: Boolean, vm: FamilyViewModel, close: () -> Unit) {
    var name by rememberSaveable(child?.id) { mutableStateOf(child?.name ?: "") }
    var stage by rememberSaveable(child?.id) { mutableStateOf(child?.stage ?: "BABY") }
    var notes by rememberSaveable(child?.id) { mutableStateOf(child?.notes ?: "") }
    var accent by rememberSaveable(child?.id) { mutableStateOf(child?.accent ?: "Forest") }
    var treeStyle by rememberSaveable(child?.id) { mutableStateOf(child?.treeStyle ?: TreeThemes.default.style) }
    var birth by remember(child?.id) { mutableStateOf(child?.birthday?.let(LocalDate::ofEpochDay)) }
    var due by remember(child?.id) { mutableStateOf(child?.dueDate?.let(LocalDate::ofEpochDay)) }
    var deleting by remember { mutableStateOf(false) }
    Editor(if (child == null) "Welcome, little one" else "${child.name}'s profile", close) {
        if (readOnly) {
            Section(name)
            Text(Stage.valueOf(stage).label)
            Text("Birthday · ${birth ?: "Not recorded"}")
            Text("Due date · ${due ?: "Not recorded"}")
            Text("Woodland accent · $accent")
            Text("Memory tree · $treeStyle")
            Text(notes)
        } else {
            Field("Child's name", name, { name = it })
            Choices(Stage.entries.map { it.label }, Stage.valueOf(stage).label) { label ->
                stage = Stage.entries.first { it.label == label }.name
            }
            DateButton("Birthday", birth) { birth = it }
            if (birth != null) TextButton(onClick = { birth = null }) { Text("Clear birthday") }
            DateButton("Due date (optional)", due) { due = it }
            if (due != null) TextButton(onClick = { due = null }) { Text("Clear due date") }
            Field("Important caregiver notes", notes, { notes = it }, lines = 3)
            Panel {
                Section("Their woodland")
                Muted("Give each child an accent colour and a memory tree of their own.")
                Text("Accent colour")
                ChildAppearanceChoices(listOf("Forest", "Moss", "Amber", "Sky"), accent, true) {
                    accent = it
                }
                Text("Memory tree style")
                ChildAppearanceChoices(TreeThemes.names, treeStyle, true) {
                    treeStyle = it
                }
                TreeStylePreview(treeStyle)
            }
            Action("Save profile", name.isNotBlank()) {
                vm.perform {
                    vm.repo.saveChild(
                        (child ?: Child(name = name)).copy(
                            name = name,
                            stage = stage,
                            birthday = birth?.toEpochDay(),
                            dueDate = due?.toEpochDay(),
                            notes = notes,
                            accent = accent,
                            treeStyle = treeStyle,
                        )
                    )
                    close()
                }
            }
            if (child != null)
                TextButton(onClick = { deleting = true }) {
                    Text(
                        "Delete child and all their records",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
        }
    }
    if (deleting && !readOnly)
        ConfirmDelete("${child?.name} and all their records", { deleting = false }) {
            vm.perform("Child removed") {
                vm.repo.deleteChild(child!!.id)
                close()
            }
        }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChildAppearanceChoices(
    options: List<String>,
    selected: String,
    swatches: Boolean = false,
    choose: (String) -> Unit,
) {
    val dark = MaterialTheme.colorScheme.surface.luminance() < .35f
    FlowRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            FilterChip(
                selected = selected == option,
                onClick = { choose(option) },
                label = { Text(option) },
                leadingIcon =
                    if (swatches) {
                        {
                            val colour =
                                when {
                                    TreeThemes.find(option) != null -> memoryTreePalette(option, dark).foliage
                                    option == "Moss" -> Color(0xFF768F56)
                                    option == "Amber" -> Color(0xFFAA722B)
                                    option == "Sky" -> Color(0xFF4F8194)
                                    else -> Color(0xFF365B35)
                                }
                            Box(Modifier.size(16.dp).clip(CircleShape).background(colour))
                        }
                    } else null,
                modifier = Modifier.heightIn(min = 48.dp),
            )
        }
    }
}

@Composable
private fun TreeStylePreview(style: String) {
    val theme = TreeThemes.find(style) ?: TreeThemes.default
    val dark = MaterialTheme.colorScheme.surface.luminance() < .35f
    val palette = remember(style, dark) { memoryTreePalette(style, dark) }
    Surface(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Canvas(Modifier.size(width = 96.dp, height = 112.dp)) {
                drawMemoryTreeScenery(TreeLayout.anchors, palette)
                // A decorative sample canopy, with no memory records or tap targets.
                listOf(1, 4, 5, 8, 11, 15, 23, 26).forEach { i ->
                    val anchor = TreeLayout.anchors[i]
                    val center = Offset(size.width * anchor.x, size.height * anchor.y)
                    rotate(if (anchor.x < .5f) -38f else 38f, center) {
                        val leaf = Path().apply {
                            moveTo(center.x - 5.dp.toPx(), center.y)
                            cubicTo(center.x - 3.dp.toPx(), center.y - 7.dp.toPx(),
                                center.x + 4.dp.toPx(), center.y - 5.dp.toPx(), center.x + 6.dp.toPx(), center.y)
                            cubicTo(center.x + 3.dp.toPx(), center.y + 6.dp.toPx(),
                                center.x - 3.dp.toPx(), center.y + 5.dp.toPx(), center.x - 5.dp.toPx(), center.y)
                            close()
                        }
                        drawPath(leaf, palette.foliage)
                        drawPath(leaf, palette.leafOutline.copy(alpha = .65f), style = Stroke(.6.dp.toPx()))
                    }
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${theme.style} woodland", style = MaterialTheme.typography.titleSmall)
                Text(theme.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
