package com.nothatcher.sproutbook.features.emergency

import android.content.*
import android.net.Uri
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.HouseholdRules
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate

fun emergencyText(child: Child, c: EmergencyCard) =
    listOf(
            child.name,
            "Date of birth: ${child.birthday?.let(LocalDate::ofEpochDay) ?: "Not recorded"}",
            "Allergies: ${c.allergies.ifBlank{"Not recorded"}}",
            "Medications: ${c.medications.ifBlank{"Not recorded"}}",
            "Conditions: ${c.conditions.ifBlank{"Not recorded"}}",
            "Doctor / clinic: ${c.clinic}",
            "Doctor phone: ${c.doctorPhone}",
            "Emergency contacts: ${c.contacts}",
            "Notes: ${c.notes}",
        )
        .joinToString("\n")

@Composable
fun EmergencyScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    val rows by
        remember(child.id) { vm.repo.db.emergencyCards().observe(child.id) }
            .collectAsStateWithLifecycle(emptyList())
    val card = rows.firstOrNull() ?: EmergencyCard(id = child.id, childId = child.id)
    var edit by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Page("Emergency card", "Always available on this device, even offline") {
        item {
            Panel {
                Text(child.name, style = MaterialTheme.typography.headlineLarge)
                Text("Born · ${child.birthday?.let(LocalDate::ofEpochDay) ?: "Not recorded"}")
                Section("Allergies")
                Text(
                    card.allergies.ifBlank { "Not recorded" },
                    style = MaterialTheme.typography.titleLarge,
                )
                Section("Medications")
                Text(card.medications.ifBlank { "Not recorded" })
                Section("Medical conditions")
                Text(card.conditions.ifBlank { "Not recorded" })
            }
        }
        item {
            Panel {
                Section("Care contacts")
                Text(card.clinic.ifBlank { "Doctor / clinic not recorded" })
                Text(card.doctorPhone)
                if (HouseholdRules.phone(card.doctorPhone).isNotEmpty())
                    Action("Call doctor / clinic") {
                        vm.perform("") {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_DIAL,
                                    Uri.fromParts(
                                        "tel",
                                        HouseholdRules.phone(card.doctorPhone),
                                        null,
                                    ),
                                )
                            )
                        }
                    }
                Section("Emergency contacts")
                Text(card.contacts.ifBlank { "Not recorded" })
                Section("Important notes")
                Text(card.notes.ifBlank { "None recorded" })
            }
        }
        item {
            Action("Copy emergency card") {
                vm.perform("Emergency card copied") {
                    context
                        .getSystemService(ClipboardManager::class.java)
                        .setPrimaryClip(
                            ClipData.newPlainText("Emergency card", emergencyText(child, card))
                        )
                }
            }
            OutlinedButton(
                onClick = {
                    vm.perform("") {
                        context.startActivity(
                            Intent.createChooser(
                                Intent(Intent.ACTION_SEND)
                                    .setType("text/plain")
                                    .putExtra(Intent.EXTRA_TEXT, emergencyText(child, card)),
                                "Share emergency card",
                            )
                        )
                    }
                }
            ) {
                Text("Share card")
            }
            if (!state.prefs.grandparent) Action("Edit emergency card") { edit = true }
        }
    }
    if (edit) {
        var allergies by remember { mutableStateOf(card.allergies) }
        var meds by remember { mutableStateOf(card.medications) }
        var conditions by remember { mutableStateOf(card.conditions) }
        var clinic by remember { mutableStateOf(card.clinic) }
        var phone by remember { mutableStateOf(card.doctorPhone) }
        var contacts by remember { mutableStateOf(card.contacts) }
        var notes by remember { mutableStateOf(card.notes) }
        Editor("Emergency information", { edit = false }) {
            Field("Allergies", allergies, { allergies = it }, lines = 2)
            Field("Medications", meds, { meds = it }, lines = 2)
            Field("Medical conditions", conditions, { conditions = it }, lines = 2)
            Field("Doctor / clinic", clinic, { clinic = it })
            Field("Doctor phone", phone, { phone = it })
            Field("Emergency contacts & phone numbers", contacts, { contacts = it }, lines = 3)
            Field("Important notes", notes, { notes = it }, lines = 3)
            Action("Save emergency card") {
                vm.perform {
                    vm.repo.saveEmergency(
                        card.copy(
                            allergies = allergies,
                            medications = meds,
                            conditions = conditions,
                            clinic = clinic,
                            doctorPhone = phone,
                            contacts = contacts,
                            notes = notes,
                        )
                    )
                    edit = false
                }
            }
        }
    }
}
