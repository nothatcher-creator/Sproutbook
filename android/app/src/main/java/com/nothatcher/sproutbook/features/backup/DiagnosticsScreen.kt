package com.nothatcher.sproutbook.features.backup

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.BackupFormat
import com.nothatcher.sproutbook.data.AppDatabase
import com.nothatcher.sproutbook.ui.*
import kotlinx.coroutines.*

@Composable
fun DiagnosticsScreen(vm: FamilyViewModel, state: FamilyState) {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    val report by
        produceState<Pair<String, List<Pair<String, Long>>>?>(null, refresh) {
            value =
                withContext(Dispatchers.IO) {
                    try {
                        val db = vm.repo.db.openHelper.readableDatabase
                        val status =
                            db.query("PRAGMA quick_check").use {
                                if (it.moveToFirst() && it.getString(0) == "ok")
                                    "Storage check passed"
                                else "Storage needs attention"
                            }
                        status to
                            BackupFormat.tables.map { table ->
                                table to
                                    db.query("SELECT COUNT(*) FROM `$table`").use {
                                        it.moveToFirst()
                                        it.getLong(0)
                                    }
                            }
                    } catch (e: Exception) {
                        "Storage could not be checked. Your records have not been removed." to
                            emptyList()
                    }
                }
        }
    Page("Diagnostics", "Device storage and build information") {
        item {
            Panel {
                Section("SproutBook ${BuildConfig.VERSION_NAME}")
                Text("Database version · ${AppDatabase.VERSION}")
                Text("Backup format · ${BackupFormat.VERSION}")
                Text(report?.first ?: "Checking storage…")
                Muted("Available space · ${context.filesDir.usableSpace/1024/1024} MB")
                Muted(
                    if (state.prefs.lastBackup == 0L) "Last backup · Not yet exported"
                    else "Last backup · ${stamp(state.prefs.lastBackup,state.prefs.time24)}"
                )
                Action("Refresh storage check") { refresh++ }
            }
        }
        item {
            Panel {
                Section("Record counts")
                report?.second?.forEach { (table, count) -> Text("$table · $count") }
            }
        }
    }
}
