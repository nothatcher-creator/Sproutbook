package com.nothatcher.sproutbook.features.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.gson.JsonObject
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.BackupFormat
import com.nothatcher.sproutbook.services.BackupManager
import com.nothatcher.sproutbook.ui.*

@Composable
fun BackupScreen(vm: FamilyViewModel, state: FamilyState) {
    val context = LocalContext.current
    val manager = remember(vm) { BackupManager(context.applicationContext, vm.repo) }
    var review by remember { mutableStateOf<JsonObject?>(null) }
    var busy by remember { mutableStateOf(false) }
    val export =
        rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/json")
        ) { uri ->
            if (uri != null)
                vm.perform("Backup exported") {
                    busy = true
                    try {
                        manager.export(uri)
                    } finally {
                        busy = false
                    }
                }
        }
    val import =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null)
                vm.perform("") {
                    busy = true
                    try {
                        review = manager.read(uri)
                    } finally {
                        busy = false
                    }
                }
        }
    Page("Keep their story safe", "A portable, versioned copy of your family's records") {
        item {
            Panel {
                Section("Backup & restore")
                Text(
                    "A backup includes every child, saved record, tree position and attached photo. Store it somewhere you trust: the file contains private family information and is not encrypted by SproutBook."
                )
                Muted(
                    "Files up to 32 MB, including up to 20 MB of photos, are supported. The app checks the complete file before it can replace your records."
                )
                Action(if (busy) "Working…" else "Export structured backup", !busy) {
                    export.launch("SproutBook-${java.time.LocalDate.now()}.json")
                }
                Action("Choose backup to review", !busy && !state.prefs.grandparent) {
                    import.launch(
                        arrayOf("application/json", "text/plain", "application/octet-stream")
                    )
                }
                Muted(
                    if (state.prefs.lastBackup > 0)
                        "Last backup · ${stamp(state.prefs.lastBackup,state.prefs.time24)}"
                    else "No backup exported on this device yet."
                )
            }
        }
        item {
            Panel {
                Section("Before replacing records")
                Text(
                    "Export your current records first. Import replaces all children and their records on this device. Reminders stay off after import until you deliberately enable them again."
                )
            }
        }
    }
    review?.let { root ->
        val data = root.getAsJsonObject("tables")
        val children = data.getAsJsonArray("children").size()
        val count = BackupFormat.tables.sumOf { data.getAsJsonArray(it).size() }
        AlertDialog(
            onDismissRequest = { if (!busy) review = null },
            title = { Text("Replace with this backup?") },
            text = {
                Text(
                    "Validated: $children children, $count total records and ${root.getAsJsonObject("photos").size()} photos. Your current records will be replaced. This cannot be undone without a backup."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = !busy,
                    onClick = {
                        vm.perform("") {
                            busy = true
                            try {
                                val prefs = manager.restore(root)
                                review = null
                                vm.message(
                                    if (prefs) "Backup restored. Reminders are off."
                                    else
                                        "Records restored. Some display preferences could not be restored."
                                )
                            } finally {
                                busy = false
                            }
                        }
                    },
                ) {
                    Text("Replace records")
                }
            },
            dismissButton = {
                TextButton(enabled = !busy, onClick = { review = null }) { Text("Cancel") }
            },
        )
    }
}
