package com.nothatcher.sproutbook.features.solids

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.core.SolidsRules
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.ui.*
import java.time.LocalDate

private val foodIdeas =
    listOf(
        "Avocado",
        "Banana",
        "Soft cooked broccoli",
        "Mashed lentils",
        "Iron-fortified cereal",
        "Well-cooked egg",
        "Plain yogurt",
        "Tofu",
        "Flaked boneless fish",
        "Smooth peanut butter, thinned",
    )

@Composable
fun SolidsScreen(vm: FamilyViewModel, state: FamilyState, go: (String) -> Unit) {
    val child = state.child ?: return
    val rows by
        remember(child.id) { vm.repo.db.foods().observe(child.id, 10000) }
            .collectAsStateWithLifecycle(emptyList())
    var filter by remember { mutableStateOf("Tried") }
    var search by remember { mutableStateOf("") }
    var edit by remember { mutableStateOf<Food?>(null) }
    val visible = rows.filter {
        it.name.contains(search, true) &&
            when (filter) {
                "Favorites" -> it.liking == "Liked"
                "Reactions" -> it.reaction.isNotBlank()
                "Allergens" -> it.allergen != "None"
                else -> true
            }
    }
    Page("Little tastes", "A growing world of food") {
        item {
            Panel {
                Text(
                    "${rows.map{it.name.lowercase()}.distinct().size} foods explored",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Muted("Follow curiosity, appetite and readiness.")
                Action("Introduce a food", !state.prefs.grandparent) {
                    edit =
                        Food(
                            childId = child.id,
                            name = "",
                            introducedOn = LocalDate.now().toEpochDay(),
                        )
                }
            }
        }
        item {
            EntryRow("This week's meals", "Seven days, a little less to think about") {
                go("meals")
            }
        }
        item {
            EntryRow("Readiness & safer eating", "Stages, allergens and choking safety") {
                go("solids-guide")
            }
        }
        item {
            Field("Find a food", search, { search = it })
            Choices(listOf("Tried", "Not tried", "Favorites", "Reactions", "Allergens"), filter) {
                filter = it
            }
        }
        if (filter == "Not tried")
            items(
                SolidsRules.untried(foodIdeas, rows.map { it.name }).filter {
                    it.contains(search, true)
                }
            ) { name ->
                EntryRow(name, "An idea to discuss and prepare for your child's abilities") {
                    if (!state.prefs.grandparent)
                        edit =
                            Food(
                                childId = child.id,
                                name = name,
                                introducedOn = LocalDate.now().toEpochDay(),
                            )
                }
            }
        else {
            if (visible.isEmpty())
                item { Empty("A fresh page", "Food records will appear here as you add them.") }
            items(visible, key = { it.id }) { f ->
                EntryRow(
                    f.name,
                    "${LocalDate.ofEpochDay(f.introducedOn)} · ${f.liking}${if(f.reaction.isNotBlank())" · Possible reaction" else ""}",
                ) {
                    edit = f
                }
            }
        }
    }
    edit?.let { f -> FoodEditor(f, rows.any { it.id == f.id }, vm, state) { edit = null } }
}

@Composable
private fun FoodEditor(
    f: Food,
    existing: Boolean,
    vm: FamilyViewModel,
    state: FamilyState,
    close: () -> Unit,
) {
    var name by remember { mutableStateOf(f.name) }
    var date by remember { mutableStateOf(LocalDate.ofEpochDay(f.introducedOn)) }
    var liking by remember { mutableStateOf(f.liking) }
    var allergen by remember { mutableStateOf(f.allergen) }
    var reaction by remember { mutableStateOf(f.reaction) }
    var notes by remember { mutableStateOf(f.notes) }
    var deleting by remember { mutableStateOf(false) }
    Editor("Food journal", close) {
        if (state.prefs.grandparent) {
            Section(name)
            Text("$date · $liking · $allergen")
            Text(reaction)
            Text(notes)
        } else {
            Field("Food", name, { name = it })
            DateButton("Introduced", date) { date = it }
            Choices(listOf("Unsure", "Liked", "Disliked"), liking) { liking = it }
            Text("Common allergen")
            Choices(allergens, allergen) { allergen = it }
            Field("Possible reaction", reaction, { reaction = it }, lines = 2)
            if (reaction.isNotBlank())
                Muted(
                    "Stop offering a suspected trigger and ask a clinician for advice before trying it again. Trouble breathing, swelling of the tongue or collapse needs emergency care now."
                )
            Field("Notes", notes, { notes = it }, lines = 2)
            Action("Save food") {
                vm.perform {
                    vm.repo.saveFood(
                        f.copy(
                            name = name,
                            introducedOn = date.toEpochDay(),
                            liking = liking,
                            allergen = allergen,
                            reaction = reaction,
                            notes = notes,
                        )
                    )
                    close()
                }
            }
            if (existing) TextButton(onClick = { deleting = true }) { Text("Delete food record") }
        }
    }
    if (deleting)
        ConfirmDelete("this food record", { deleting = false }) {
            vm.perform("Food removed") {
                vm.repo.write { vm.repo.db.foods().delete(f.id, f.childId) }
                close()
            }
        }
}
