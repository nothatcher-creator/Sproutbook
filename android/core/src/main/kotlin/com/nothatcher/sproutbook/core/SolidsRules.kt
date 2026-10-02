package com.nothatcher.sproutbook.core
object SolidsRules {
    fun untried(catalog: List<String>, tried: List<String>): List<String> {
        val names = tried.map { it.trim().lowercase(java.util.Locale.ROOT) }.toSet()
        return catalog.filter { it.lowercase(java.util.Locale.ROOT) !in names }
    }

    fun mealId(child: String, day: Long, slot: String) =
        java.util.UUID.nameUUIDFromBytes("$child|$day|$slot".toByteArray()).toString()
}
