package com.nothatcher.sproutbook.features.home

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.nothatcher.sproutbook.FamilyViewModel
import com.nothatcher.sproutbook.core.HomeCustomization
import com.nothatcher.sproutbook.core.HomeOrganizerCatalog
import com.nothatcher.sproutbook.data.Child
import com.nothatcher.sproutbook.data.homeCustomization
import com.nothatcher.sproutbook.data.importHomeBackground
import com.nothatcher.sproutbook.data.saveHomeCustomization
import com.nothatcher.sproutbook.services.PhotoStore
import com.nothatcher.sproutbook.ui.Muted
import com.nothatcher.sproutbook.ui.Page
import com.nothatcher.sproutbook.ui.Panel
import com.nothatcher.sproutbook.ui.Section
import kotlinx.coroutines.CancellationException

private val HomeDraftSaver = listSaver<HomeCustomization, String>(
    save = {
        listOf(
            it.sectionOrder.joinToString(","),
            it.hiddenSections.sorted().joinToString(","),
            it.quickActions.joinToString(","),
            it.background,
            it.backgroundPhoto.orEmpty(),
        )
    },
    restore = {
        HomeOrganizerCatalog.decode(it[0], it[1], it[2], it[3], it[4].ifBlank { null })
    },
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeOrganizerScreen(
    vm: FamilyViewModel,
    child: Child,
    readOnly: Boolean,
    onClose: () -> Unit,
    onPreview: (HomeCustomization) -> Unit,
) {
    var draft by rememberSaveable(child.id, stateSaver = HomeDraftSaver) {
        mutableStateOf(child.homeCustomization())
    }
    // Selecting a preset clears the draft reference; the photo remains available to reselect.
    var availablePhoto by rememberSaveable(child.id) {
        mutableStateOf(child.homeCustomization().backgroundPhoto)
    }
    var editor by rememberSaveable(child.id) { mutableStateOf("Sections") }
    var restoring by remember(child.id) { mutableStateOf(false) }
    var saving by remember(child.id) { mutableStateOf(false) }
    var importing by remember(child.id) { mutableStateOf(false) }
    var pendingPhotoChildId by rememberSaveable { mutableStateOf<String?>(null) }
    var attached by remember(child.id) { mutableStateOf(true) }
    DisposableEffect(child.id) {
        onDispose { attached = false }
    }
    val currentChildId by rememberUpdatedState(child.id)
    val currentReadOnly by rememberUpdatedState(readOnly)
    val currentOnClose by rememberUpdatedState(onClose)
    val context = LocalContext.current
    val mutable = !readOnly && !saving && !importing
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        val originatingChildId = pendingPhotoChildId
        pendingPhotoChildId = null
        if (uri == null) importing = false
        else if (originatingChildId == null || !attached || originatingChildId != currentChildId || currentReadOnly) {
            importing = false
            vm.message("That photo was not applied. Open the organizer for the child you want to change.")
        } else {
            vm.perform("") {
                try {
                    val photo = vm.repo.importHomeBackground(context, originatingChildId, uri)
                    // The picker and decoding can outlive a child switch or a mode change.
                    if (attached && currentChildId == originatingChildId && !currentReadOnly) {
                        availablePhoto = photo
                        draft = draft.copy(background = "photo", backgroundPhoto = photo)
                        vm.message("Photo ready to preview. Save layout to keep it.")
                    } else {
                        // ViewModel work can finish after rotation/navigation; report the discarded result.
                        vm.message("That photo was not applied. Choose it again in the organizer. Your saved layout is unchanged.")
                    }
                } finally {
                    importing = false
                }
            }
        }
    }
    fun choosePhoto() {
        pendingPhotoChildId = child.id
        importing = true
        try {
            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: CancellationException) {
            pendingPhotoChildId = null
            importing = false
            throw e
        } catch (e: Exception) {
            pendingPhotoChildId = null
            importing = false
            vm.message("The photo picker could not be opened. Your layout is unchanged. Please try again.")
        }
    }

    Page(
        "Home screen organizer",
        "${child.name}'s Today page",
        modifier = Modifier.testTag("organizer-list"),
    ) {
        item(key = "organizer-controls") {
            Panel {
                Muted("Arrange sections, choose shortcuts and pick a background for ${child.name}. Changes wait until you save.")
                if (readOnly) Muted("Grandparent mode · View and preview this layout. Turn off read-only mode in More to make changes.")
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("organizer-cancel"),
                        enabled = !saving,
                    ) { Text("Cancel") }
                    Button(
                        onClick = {
                            val targetChildId = child.id
                            val savedDraft = draft
                            saving = true
                            vm.perform("Layout saved") {
                                try {
                                    check(currentChildId == targetChildId && !currentReadOnly) {
                                        "This layout was not saved. Open the organizer for the child you want to change."
                                    }
                                    vm.repo.saveHomeCustomization(targetChildId, savedDraft)
                                    if (attached && currentChildId == targetChildId) currentOnClose()
                                } finally {
                                    saving = false
                                }
                            }
                        },
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp).testTag("organizer-save"),
                        enabled = mutable,
                    ) { Text(if (saving) "Saving…" else "Save layout") }
                }
                OutlinedButton(
                    onClick = { onPreview(draft) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("organizer-preview"),
                    enabled = !saving && !importing,
                ) { Text("Preview") }
                TextButton(
                    onClick = { restoring = true },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("organizer-restore"),
                    enabled = mutable,
                ) { Text("Restore defaults") }
            }
        }
        item(key = "organizer-editors") {
            FlowRow(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(
                    "Sections" to "organizer-sections",
                    "Quick actions" to "organizer-quick-actions",
                    "Background" to "organizer-background",
                ).forEach { (label, tag) ->
                    FilterChip(
                        selected = editor == label,
                        onClick = { editor = label },
                        label = { Text(label) },
                        modifier = Modifier.heightIn(min = 48.dp).testTag(tag),
                    )
                }
            }
        }
        when (editor) {
            "Sections" -> {
                item(key = "sections-description") {
                    Section("Sections on Today")
                    Muted("Use the arrows to change the order. Hidden sections stay available in Care and More. Some sections appear when there is something to show or when they fit this child's chapter.")
                }
                draft.sectionOrder.forEachIndexed { index, id ->
                    val section = HomeOrganizerCatalog.sections.first { it.id == id }
                    item(key = "section-$id") {
                        OrganizerChoice(
                            section.title,
                            section.detail,
                            checked = id !in draft.hiddenSections,
                            onCheckedChange = { shown ->
                                draft = draft.copy(hiddenSections =
                                    if (shown) draft.hiddenSections - id else draft.hiddenSections + id)
                            },
                            switchLabel = "Show ${section.title}",
                            tag = "section-$id",
                            modifier = Modifier.testTag("organizer-section-$id"),
                            enabled = mutable,
                            position = index,
                            total = draft.sectionOrder.size,
                            onMove = { direction ->
                                draft = draft.copy(sectionOrder = HomeOrganizerCatalog.move(draft.sectionOrder, id, direction))
                            },
                        )
                    }
                }
            }
            "Quick actions" -> {
                item(key = "quick-description") {
                    Section("Your quick actions")
                    Muted("Choose the shortcuts you want, then put them in order. A shortcut opens its page. You can leave this list empty.")
                }
                val orderedActions = draft.quickActions +
                    HomeOrganizerCatalog.quickActions.map { it.id }.filter { it !in draft.quickActions }
                orderedActions.forEach { id ->
                    val action = HomeOrganizerCatalog.quickActions.first { it.id == id }
                    val position = draft.quickActions.indexOf(id)
                    item(key = "quick-$id") {
                        OrganizerChoice(
                            action.title,
                            if (position >= 0) "Shown in your quick actions" else "Available to add",
                            checked = position >= 0,
                            onCheckedChange = { included ->
                                draft = draft.copy(quickActions =
                                    if (included) draft.quickActions + id else draft.quickActions - id)
                            },
                            switchLabel = "Include ${action.title}",
                            tag = "quick-$id",
                            modifier = Modifier.testTag("organizer-quick-$id"),
                            enabled = mutable,
                            position = position,
                            total = draft.quickActions.size,
                            onMove = { direction ->
                                draft = draft.copy(quickActions = HomeOrganizerCatalog.move(draft.quickActions, id, direction))
                            },
                        )
                    }
                }
            }
            "Background" -> {
                item(key = "background-description") {
                    Section("A background for Today")
                    Muted("Choose a woodland scene, a plain background or a photo. Preview shows your choice behind the Today cards.")
                }
                HomeOrganizerCatalog.backgrounds.forEach { background ->
                    item(key = "background-${background.id}") {
                        Panel {
                            FilterChip(
                                selected = draft.background == background.id,
                                onClick = {
                                    if (background.id == "photo") {
                                        val photo = availablePhoto
                                        if (photo == null) choosePhoto()
                                        else draft = draft.copy(background = "photo", backgroundPhoto = photo)
                                    } else draft = draft.copy(background = background.id, backgroundPhoto = null)
                                },
                                label = { Text(background.title) },
                                enabled = mutable,
                                modifier = Modifier.heightIn(min = 48.dp).testTag("background-${background.id}"),
                            )
                            Muted(background.detail)
                            if (background.id == "photo") {
                                availablePhoto?.let { photo ->
                                    ChosenBackgroundPhoto(photo)
                                }
                                OutlinedButton(
                                    onClick = { choosePhoto() },
                                    enabled = mutable,
                                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("organizer-photo-import"),
                                ) {
                                    Text(if (importing) "Opening photo…" else if (availablePhoto == null) "Choose a photo" else "Choose another photo")
                                }
                                if (availablePhoto != null) {
                                    TextButton(
                                        onClick = {
                                            availablePhoto = null
                                            if (draft.background == "photo") {
                                                draft = draft.copy(background = "woodland", backgroundPhoto = null)
                                            }
                                            vm.message("Photo removed from this draft. Save layout to keep the change.")
                                        },
                                        enabled = mutable,
                                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("organizer-photo-remove"),
                                    ) { Text("Remove photo from layout") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (restoring) {
        AlertDialog(
            onDismissRequest = { restoring = false },
            title = { Text("Restore the default layout?") },
            text = { Text("This resets sections, quick actions and the background in this draft. Save layout to keep the defaults. Your family records stay as they are.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        restoring = false
                        if (!currentReadOnly) {
                            draft = HomeCustomization()
                            vm.message("Default layout ready to preview. Save layout to keep it.")
                        }
                    },
                    enabled = mutable,
                    modifier = Modifier.heightIn(min = 48.dp).testTag("organizer-restore-confirm"),
                ) { Text("Restore defaults") }
            },
            dismissButton = {
                TextButton(
                    onClick = { restoring = false },
                    modifier = Modifier.heightIn(min = 48.dp).testTag("organizer-restore-keep"),
                ) { Text("Keep my choices") }
            },
        )
    }
}

@Composable
private fun OrganizerChoice(
    title: String,
    detail: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchLabel: String,
    tag: String,
    enabled: Boolean,
    position: Int,
    total: Int,
    onMove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Panel(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Section(title)
                Muted(detail)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                modifier = Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .testTag("$tag-toggle").semantics { contentDescription = switchLabel },
            )
        }
        if (position >= 0) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) { Muted("Position ${position + 1} of $total") }
                IconButton(
                    onClick = { onMove(-1) },
                    enabled = enabled && position > 0,
                    modifier = Modifier.size(48.dp).testTag("$tag-up").semantics {
                        contentDescription = "Move $title up. Position ${position + 1} of $total"
                    },
                ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = null) }
                IconButton(
                    onClick = { onMove(1) },
                    enabled = enabled && position < total - 1,
                    modifier = Modifier.size(48.dp).testTag("$tag-down").semantics {
                        contentDescription = "Move $title down. Position ${position + 1} of $total"
                    },
                ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) }
            }
        }
    }
}

@Composable
private fun ChosenBackgroundPhoto(name: String) {
    val context = LocalContext.current
    val bitmap by produceState<android.graphics.Bitmap?>(null, context, name) {
        value = PhotoStore.load(context, name)
    }
    bitmap?.let {
        Image(
            it.asImageBitmap(),
            contentDescription = "Chosen background photo",
            modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
            contentScale = ContentScale.Fit,
        )
    } ?: Muted("If this photo is unavailable, Today uses the woodland background. You can choose another photo.")
}
