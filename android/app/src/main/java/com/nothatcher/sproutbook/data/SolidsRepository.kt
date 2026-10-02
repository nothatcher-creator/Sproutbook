package com.nothatcher.sproutbook.data

import com.nothatcher.sproutbook.core.SolidsRules
import java.time.LocalDate

val allergens =
    listOf(
        "None",
        "Milk",
        "Egg",
        "Peanut",
        "Tree nuts",
        "Wheat",
        "Soy",
        "Sesame",
        "Fish",
        "Shellfish",
        "Mustard",
    )
val mealSlots = listOf("Breakfast", "Lunch", "Dinner", "Snacks")

suspend fun FamilyRepository.saveFood(f: Food) = write {
    validateText("Possible reaction" to f.reaction, "Notes" to f.notes)

    require(f.name.trim().length in 1..120) { "Enter a food name (up to 120 characters)." }
    require(f.introducedOn <= LocalDate.now().toEpochDay()) {
        "Choose today or an earlier introduction date."
    }
    require(f.liking in listOf("Unsure", "Liked", "Disliked") && f.allergen in allergens)
    db.foods().save(f.copy(name = f.name.trim(), updatedAt = System.currentTimeMillis()))
}

suspend fun FamilyRepository.saveMeal(m: Meal) = write {
    validateText("Notes" to m.notes)
    require(m.title.trim().length in 1..160) { "Enter a meal." }
    require(m.slot in mealSlots)
    db.meals()
        .save(
            m.copy(
                id = SolidsRules.mealId(m.childId, m.day, m.slot),
                title = m.title.trim(),
                updatedAt = System.currentTimeMillis(),
            )
        )
}
