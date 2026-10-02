package com.nothatcher.sproutbook.features.backup

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.ui.*

@Composable
fun NotificationSettings(vm: FamilyViewModel, state: FamilyState) {
    fun enable(value: Boolean) {
        vm.perform(
            if (value) "Appointment reminders enabled" else "Appointment reminders disabled"
        ) {
            vm.repo.write {
                vm.repo.settings.boolean("notifications", value)
                vm.repo.reminders?.reconcile()
            }
        }
    }
    val permission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) enable(true)
            else
                vm.message(
                    "Notifications remain off. You can enable permission in Android settings."
                )
        }
    Panel {
        Section("Optional reminders")
        Muted(
            "Only appointments where you choose a reminder. Delivery may be delayed by Android battery settings. Past reminder times are not sent."
        )
        com.nothatcher.sproutbook.features.profile.SettingSwitch(
            "Appointment notifications",
            state.prefs.notifications,
        ) { value ->
            if (state.prefs.grandparent)
                vm.message("Turn off read-only mode before changing reminders.")
            else if (value && Build.VERSION.SDK_INT >= 33)
                permission.launch(Manifest.permission.POST_NOTIFICATIONS)
            else enable(value)
        }
    }
}
