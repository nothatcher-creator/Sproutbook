package com.nothatcher.sproutbook.core

data class BirthPlanField(val key: String, val title: String, val prompt: String, val phone: Boolean = false)

object BirthPlanCatalog {
    val fields = listOf(
        BirthPlanField("maternity-phone", "Maternity team phone", "The number your care team asks you to use during labour, including out of hours.", true),
        BirthPlanField("emergency-phone", "Local emergency number", "Confirm the emergency number for your location. The app never chooses a country or calls automatically.", true),
        BirthPlanField("address", "Birth address & access", "Full address, postcode or local equivalent, entry instructions and who will open the door."),
        BirthPlanField("attendants", "Attending team & backup", "Qualified attendants, backup arrangements, when to call and what to do if the team cannot attend."),
        BirthPlanField("support", "Support people & contact details", "Who you want with you, contact details and each person's role."),
        BirthPlanField("consent", "Communication, consent & privacy", "How you want information explained, privacy needs, who is present and what matters to you."),
        BirthPlanField("comfort", "Comfort & pain relief preferences", "Positions, surroundings and comfort preferences to discuss with the care team; ask about available pain relief and changes of setting."),
        BirthPlanField("care", "Care information to share", "Allergies, medicines and important care information for your clinicians. Keep official maternity records available."),
        BirthPlanField("transfer", "Hospital & transfer plan", "Agreed destination, who arranges transport, companion travel, care records and the alternative plan if services are unavailable."),
        BirthPlanField("aftercare", "After-birth care & feeding preferences", "Visits, newborn assessment/screening, feeding support and questions for qualified clinicians."),
        BirthPlanField("family", "Other children, pets & practical support", "Childcare, home access, meals, travel and recovery support."),
        BirthPlanField("priorities", "What matters most", "A short summary you want your support people and care team to see first."),
    )

    fun prefix(childId: String) = BirthPlanningCatalog.prefix(childId, "home").replaceFirst("birth-v1-", "birth-plan-v1-")
    fun fieldForItem(childId: String, itemId: String) = fields.firstOrNull { prefix(childId) + it.key == itemId }
    fun validPhone(value: String) = value.isBlank() ||
        (value.length <= 40 && value.count(Char::isDigit) >= 2 && value.all { it.isDigit() || it in "+() .-" })
}
