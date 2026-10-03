package com.nothatcher.sproutbook.core

/** Child-owned presentation choices. These never change, hide or delete the child's records. */
data class HomeCustomization(
    val sectionOrder: List<String> = HomeOrganizerCatalog.sectionIds,
    val hiddenSections: Set<String> = emptySet(),
    val quickActions: List<String> = listOf("memory", "schedule"),
    val background: String = "woodland",
    val backgroundPhoto: String? = null,
)

data class HomeSection(val id: String, val title: String, val detail: String)
data class HomeQuickAction(val id: String, val title: String, val route: String)
data class HomeBackground(val id: String, val title: String, val detail: String)

/** Stable IDs allow older saved orders to acquire new sections without losing existing choices. */
object HomeOrganizerCatalog {
    const val MAX_CSV_CHARS = 2048

    val sections = listOf(
        HomeSection("chapter", "Today's greeting", "Your child's stage and a little encouragement."),
        HomeSection("up_next", "Next appointment", "The next scheduled appointment, when one is coming up."),
        HomeSection("low_stock", "Low supplies", "Items that have reached their inventory reminder level."),
        HomeSection("shopping", "Shopping list", "A reminder when there are items left to pick up."),
        HomeSection("prepared_bottles", "Prepared bottles", "Available bottles during baby and toddler stages."),
        HomeSection("help", "Help right now", "Quick access to practical help and emergency guidance."),
        HomeSection("routines", "Today's routines", "Due routines or family responsibilities for older children."),
        HomeSection("milk_freezer", "Milk freezer", "Stored milk during baby and toddler stages."),
        HomeSection("pregnancy", "Pregnancy", "Your pregnancy journal and preparation, during pregnancy."),
        HomeSection("sleep", "Sleep", "Sleep logging and the latest sleep during baby and toddler stages."),
        HomeSection("milestones", "Milestones & achievements", "Development and achievements for children and teens."),
        HomeSection("feeding", "Feeding", "Feeding records during baby and toddler stages."),
        HomeSection("diapers", "Diapers", "The latest diaper entry during baby and toddler stages."),
        HomeSection("growth", "Growth journal", "Growth records after pregnancy."),
        HomeSection("quick_actions", "Quick actions", "Your chosen shortcuts, in your chosen order."),
        HomeSection("memory_tree", "Memory tree", "A growing tree of your child's saved memories."),
        HomeSection("latest_milestone", "Latest milestone", "The most recently completed milestone, when available."),
        HomeSection("health", "Health records", "Important health records for children and teens."),
        HomeSection("potty", "Potty journal", "Potty learning during the toddler stage."),
        HomeSection("meals", "Meal planning", "The week's meals during the toddler stage."),
        HomeSection("caregiver_notes", "Caregiver notes", "Your child's profile notes, when you have added them."),
    )

    val quickActions = listOf(
        HomeQuickAction("memory", "Add memory", "memories?new=true"),
        HomeQuickAction("schedule", "Schedule", "schedule"),
        HomeQuickAction("feeding", "Feeding", "feeding"),
        HomeQuickAction("sleep", "Sleep & sounds", "sleep"),
        HomeQuickAction("diapers", "Diapers", "diapers"),
        HomeQuickAction("growth", "Growth", "growth"),
        HomeQuickAction("milestones", "Milestones", "milestones"),
        HomeQuickAction("teeth", "Teeth", "teeth"),
        HomeQuickAction("solids", "First foods", "solids"),
        HomeQuickAction("meals", "Meal planning", "meals"),
        HomeQuickAction("pregnancy", "Pregnancy", "pregnancy"),
        HomeQuickAction("wishlist", "Wishlist", "wishlist"),
        HomeQuickAction("inventory", "Inventory", "inventory"),
        HomeQuickAction("shopping", "Shopping list", "shopping"),
        HomeQuickAction("milk", "Milk freezer", "milk"),
        HomeQuickAction("bottles", "Prepared bottles", "bottles"),
        HomeQuickAction("health", "Health records", "health"),
        HomeQuickAction("emergency", "Emergency information", "emergency"),
        HomeQuickAction("routines", "Routines", "routines"),
        HomeQuickAction("potty", "Potty journal", "potty"),
        HomeQuickAction("help", "Help right now", "help"),
        HomeQuickAction("advice", "Parent advice", "advice"),
        HomeQuickAction("formula", "Formula estimates", "formula"),
    )

    val backgrounds = listOf(
        HomeBackground("woodland", "Woodland", "SproutBook's original soft woodland painting."),
        HomeBackground("morning", "Morning", "A light woodland scene to start the day."),
        HomeBackground("meadow", "Meadow", "A peaceful green clearing."),
        HomeBackground("evening", "Evening", "A cozy woodland scene as the day settles."),
        HomeBackground("plain", "Plain", "A calm theme-colored background."),
        HomeBackground("photo", "Your picture", "A picture copied privately into SproutBook."),
    )

    val sectionIds = sections.map { it.id }
    val actionIds = quickActions.map { it.id }
    val backgroundIds = backgrounds.map { it.id }

    private fun csv(value: String, allowed: List<String>): List<String> {
        require(value.length <= MAX_CSV_CHARS) { "The saved home screen choices are too long." }
        if (value.isEmpty()) return emptyList()
        val values = value.split(',')
        require(values.size == values.distinct().size && values.all { it in allowed }) {
            "The saved home screen contains an unknown or repeated choice."
        }
        return values
    }

    fun decode(
        sectionsCSV: String,
        hiddenCSV: String,
        actionsCSV: String,
        background: String,
        photo: String?,
    ): HomeCustomization {
        val chosen = csv(sectionsCSV, sectionIds)
        return HomeCustomization(
            sectionOrder = chosen + sectionIds.filterNot { it in chosen },
            hiddenSections = csv(hiddenCSV, sectionIds).toSet(),
            quickActions = csv(actionsCSV, actionIds),
            background = background,
            backgroundPhoto = photo,
        ).also(::validate)
    }

    fun validate(config: HomeCustomization) {
        require(config.sectionOrder.size == sectionIds.size && config.sectionOrder.toSet() == sectionIds.toSet()) {
            "The home screen must contain each known section once."
        }
        require(config.hiddenSections.all { it in sectionIds }) {
            "The home screen contains an unknown hidden section."
        }
        require(config.quickActions.size == config.quickActions.distinct().size && config.quickActions.all { it in actionIds }) {
            "Quick actions must be known shortcuts without repeats."
        }
        require(config.background in backgroundIds) { "Choose an available home screen background." }
        if (config.background == "photo") {
            require(config.backgroundPhoto != null && config.backgroundPhoto.length in 5..160 &&
                config.backgroundPhoto.matches(Regex("[A-Za-z0-9_-]+\\.jpg"))) {
                "Choose a picture saved privately in SproutBook."
            }
        } else {
            require(config.backgroundPhoto == null) { "A preset background cannot reference a custom picture." }
        }
    }

    /** The same bounded operation supports sections and quick actions, including accessibility buttons. */
    fun move(order: List<String>, id: String, direction: Int): List<String> {
        require(direction == -1 || direction == 1) { "Move a choice one place up or down." }
        val from = order.indexOf(id)
        val to = from + direction
        if (from < 0 || to !in order.indices) return order
        return order.toMutableList().apply {
            removeAt(from)
            add(to, id)
        }
    }
}
