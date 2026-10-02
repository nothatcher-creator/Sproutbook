package com.nothatcher.sproutbook.features.solids

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.*
import java.time.temporal.TemporalAdjusters

@Composable
fun MealScreen(vm: FamilyViewModel, state: FamilyState) {
    val child = state.child ?: return
    val rows by
        remember(child.id) { vm.repo.db.meals().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var week by remember {
        mutableStateOf(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }
    var edit by remember { mutableStateOf<Meal?>(null) }
    Page("A week at the table", "Meals and snacks, one day at a time") {
        item {
            Row {
                OutlinedButton({ week = week.minusWeeks(1) }, Modifier.weight(1f)) {
                    Text("Previous")
                }
                TextButton(
                    {
                        week =
                            LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    },
                    Modifier.weight(1f),
                ) {
                    Text("This week")
                }
                OutlinedButton({ week = week.plusWeeks(1) }, Modifier.weight(1f)) { Text("Next") }
            }
        }
        (0L..6L).forEach { offset ->
            val date = week.plusDays(offset)
            item {
                Panel {
                    Section(
                        "${date.dayOfWeek.name.lowercase().replaceFirstChar{it.uppercase()}} · ${date.monthValue}/${date.dayOfMonth}"
                    )
                    mealSlots.forEach { slot ->
                        val meal = rows.firstOrNull {
                            it.day == date.toEpochDay() && it.slot == slot
                        }
                        EntryRow(slot, meal?.title ?: "Plan something nourishing") {
                            edit =
                                meal
                                    ?: Meal(
                                        childId = child.id,
                                        day = date.toEpochDay(),
                                        slot = slot,
                                        title = "",
                                    )
                        }
                    }
                }
            }
        }
    }
    edit?.let { meal ->
        var title by remember(meal.id) { mutableStateOf(meal.title) }
        var notes by remember(meal.id) { mutableStateOf(meal.notes) }
        var deleting by remember { mutableStateOf(false) }
        Editor("${meal.slot} · ${LocalDate.ofEpochDay(meal.day)}", { edit = null }) {
            if (state.prefs.grandparent) {
                Text(title.ifBlank { "No meal planned" })
                Text(notes)
            } else {
                Field("Meal", title, { title = it })
                Field("Notes", notes, { notes = it }, lines = 2)
                Action("Save meal") {
                    vm.perform {
                        vm.repo.saveMeal(meal.copy(title = title, notes = notes))
                        edit = null
                    }
                }
                if (meal.title.isNotBlank())
                    TextButton(onClick = { deleting = true }) { Text("Remove meal") }
            }
        }
        if (deleting)
            ConfirmDelete("this meal", { deleting = false }) {
                vm.perform("Meal removed") {
                    vm.repo.write { vm.repo.db.meals().delete(meal.id, meal.childId) }
                    edit = null
                }
            }
    }
}
