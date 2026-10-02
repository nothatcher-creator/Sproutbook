package com.nothatcher.sproutbook.core
object DevelopmentRules {
    fun validTooth(index: Int, stage: String, date: Long?, today: Long) =
        index in 0..19 &&
            stage in listOf("Not seen", "Observed", "Erupted") &&
            (date == null || (stage == "Erupted" && date <= today))

    fun memoryId(milestoneId: String) = "milestone-$milestoneId"
}
