package com.nothatcher.sproutbook.core

import com.google.gson.*
import java.time.LocalDate
import java.util.Base64

/** Versioned transport format only. Room remains the source of truth. */
object BackupFormat {
    const val VERSION = 5
    const val MAX_BYTES = 32 * 1024 * 1024

    private data class Rule(val type: String, val default: String? = null)

    private fun fields(vararg values: Pair<String, Rule>) =
        mapOf("id" to Rule("s"), "updatedAt" to Rule("l", "0")) + values

    private fun owned(vararg values: Pair<String, Rule>) = fields("childId" to Rule("s"), *values)

    private fun s(default: String? = null) =
        Rule("s", default?.let { JsonPrimitive(it).toString() })

    private fun l(default: String? = null) = Rule("l", default)

    private fun n() = Rule("l?", "null")

    private fun textRef() = Rule("s?", "null")

    private val schema =
        linkedMapOf(
            "children" to
                fields(
                    "name" to s(),
                    "stage" to s("BABY"),
                    "birthday" to n(),
                    "dueDate" to n(),
                    "avatar" to textRef(),
                    "notes" to s(""),
                    "accent" to s("Forest"),
                    "treeStyle" to s("Summer"),
                    "homeSections" to s(""),
                    "homeHiddenSections" to s(""),
                    "homeQuickActions" to s("memory,schedule"),
                    "homeBackground" to s("woodland"),
                    "homeBackgroundPhoto" to textRef(),
                ),
            "appointments" to
                owned(
                    "title" to s(),
                    "startsAt" to l(),
                    "location" to s(""),
                    "category" to s("Other"),
                    "notes" to s(""),
                    "questions" to s(""),
                    "results" to s(""),
                    "reminderMinutes" to l("0"),
                ),
            "memorys" to
                owned(
                    "title" to s(),
                    "occurredOn" to l(),
                    "category" to s("General"),
                    "notes" to s(""),
                    "photo" to textRef(),
                    "anchor" to l(),
                    "chapter" to l("0"),
                ),
            "feeds" to
                owned(
                    "kind" to s("Bottle"),
                    "startsAt" to l(),
                    "amountMl" to Rule("d", "0"),
                    "leftMl" to Rule("d", "0"),
                    "rightMl" to Rule("d", "0"),
                    "durationSeconds" to l("0"),
                    "side" to s("Both"),
                    "contents" to s("Formula"),
                    "notes" to s(""),
                ),
            "sleeps" to
                owned("startsAt" to l(), "endsAt" to n(), "kind" to s("Nap"), "notes" to s("")),
            "foods" to
                owned(
                    "name" to s(),
                    "introducedOn" to l(),
                    "liking" to s("Unsure"),
                    "reaction" to s(""),
                    "allergen" to s("None"),
                    "notes" to s(""),
                ),
            "meals" to owned("day" to l(), "slot" to s(), "title" to s(), "notes" to s("")),
            "tooths" to owned("toothIndex" to l(), "stage" to s("Not seen"), "eruptedOn" to n()),
            "milestones" to
                owned(
                    "stage" to s(),
                    "title" to s(),
                    "description" to s(""),
                    "completedOn" to n(),
                    "notes" to s(""),
                    "memoryId" to textRef(),
                ),
            "pregnancyEvents" to
                owned(
                    "kind" to s(),
                    "startsAt" to l(),
                    "endsAt" to n(),
                    "count" to l("0"),
                    "notes" to s(""),
                ),
            "prepItems" to
                owned(
                    "kind" to s(),
                    "title" to s(),
                    "notes" to s(""),
                    "completed" to Rule("b", "false"),
                ),
            "inventorys" to
                owned(
                    "name" to s(),
                    "quantity" to Rule("d", "0"),
                    "unit" to s("items"),
                    "threshold" to Rule("d", "0"),
                    "category" to s("Supplies"),
                ),
            "emergencyCards" to
                owned(
                    *listOf(
                            "allergies",
                            "medications",
                            "conditions",
                            "clinic",
                            "doctorPhone",
                            "contacts",
                            "notes",
                        )
                        .map { it to s("") }
                        .toTypedArray()
                ),
            "diapers" to owned("recordedAt" to l(), "kind" to s("Wet"), "notes" to s("")),
            "bottlePreps" to
                owned(
                    "preparedAt" to l(),
                    "amountMl" to Rule("d"),
                    "contents" to s("Formula"),
                    "status" to s("Prepared"),
                    "feedId" to textRef(),
                    "notes" to s(""),
                ),
            "shoppingItems" to
                owned(
                    "name" to s(),
                    "quantity" to Rule("d", "1"),
                    "unit" to s("items"),
                    "checked" to Rule("b", "false"),
                    "inventoryId" to textRef(),
                    "notes" to s(""),
                ),
            "wishlistItems" to
                owned(
                    "title" to s(),
                    "notes" to s(""),
                    "link" to s(""),
                    "obtained" to Rule("b", "false"),
                ),
            "routines" to
                owned(
                    "title" to s(),
                    "weekdays" to l("127"),
                    "timeMinutes" to l("-1"),
                    "owner" to s(""),
                    "notes" to s(""),
                    "active" to Rule("b", "true"),
                ),
            "routineCompletions" to owned("routineId" to s(), "day" to l()),
            "pottyLogs" to owned("recordedAt" to l(), "kind" to s("Tried"), "notes" to s("")),
            "milkContainers" to
                owned(
                    "label" to s(),
                    "storedAt" to l(),
                    "amountMl" to Rule("d"),
                    "location" to s(""),
                    "notes" to s(""),
                    "status" to s("Frozen"),
                    "pumpId" to textRef(),
                ),
            "healthRecords" to
                owned(
                    "kind" to s(),
                    "recordedAt" to l(),
                    "title" to s(),
                    "value" to s(""),
                    "unit" to s(""),
                    "notes" to s(""),
                    "appointmentId" to textRef(),
                ),
        )
    val tables
        get() = schema.keys.toList()

    private fun fail(): Nothing =
        throw IllegalArgumentException(
            "This backup is incomplete or contains invalid records. Nothing has been replaced."
        )

    fun validate(text: String): JsonObject {
        require(text.toByteArray(Charsets.UTF_8).size <= MAX_BYTES) {
            "Backup exceeds the 32 MB import limit."
        }
        try {
            val root =
                GsonBuilder()
                    .setStrictness(Strictness.STRICT)
                    .create()
                    .fromJson(text, JsonObject::class.java) ?: fail()
            if (
                root["format"]?.asString != "sproutbook" ||
                    root["version"]?.asBigDecimal?.intValueExact() !in 1..VERSION
            )
                fail()
            val data = root["tables"]?.takeIf { it.isJsonObject }?.asJsonObject ?: fail()
            val version = root["version"].asInt
            val new3 = setOf("routines", "routineCompletions", "pottyLogs", "milkContainers")
            val added =
                when (version) {
                    1 -> new3 + setOf("diapers", "bottlePreps", "shoppingItems")
                    2 -> new3
                    else -> emptySet()
                }
            // Prior exports had no Wishlist. Format 4 requires it, even when the list is empty.
            val optional = if (version < 4) setOf("wishlistItems") else emptySet()
            if (data.keySet() - optional != schema.keys - added - optional) fail()
            added.forEach { data.add(it, JsonArray()) }
            optional.filterNot { data.has(it) }.forEach { data.add(it, JsonArray()) }
            root.addProperty("version", VERSION)
            if (data.keySet() != schema.keys) fail()
            var count = 0
            schema.forEach { (table, rules) ->
                val array = data[table].takeIf { it.isJsonArray }?.asJsonArray ?: fail()
                count += array.size()
                if (count > 50000) fail()
                val ids = mutableSetOf<String>()
                array.forEach { entry ->
                    val row = entry.takeIf { it.isJsonObject }?.asJsonObject ?: fail()
                    if (row.keySet().any { it !in rules }) fail()
                    if (version >= 4 && table == "children" &&
                        (!row.has("accent") || !row.has("treeStyle"))) fail()
                    if (version >= 5 && table == "children" &&
                        listOf("homeSections", "homeHiddenSections", "homeQuickActions", "homeBackground", "homeBackgroundPhoto")
                            .any { !row.has(it) }) fail()
                    rules.forEach { (name, rule) ->
                        if (!row.has(name)) {
                            row.add(
                                name,
                                rule.default?.let { JsonParser.parseString(it) } ?: fail(),
                            )
                        }
                        val v = row[name]
                        if (v.isJsonNull) {
                            if (!rule.type.endsWith("?")) fail()
                        } else {
                            if (!v.isJsonPrimitive) fail()
                            val p = v.asJsonPrimitive
                            when (rule.type.first()) {
                                's' -> if (!p.isString || p.asString.length > 10000) fail()
                                'b' -> if (!p.isBoolean) fail()
                                'l' -> {
                                    if (!p.isNumber) fail()
                                    p.asBigDecimal.longValueExact()
                                }
                                'd' -> if (!p.isNumber || !p.asDouble.isFinite()) fail()
                            }
                        }
                    }
                    if (row.str("id").length !in 1..160 || !ids.add(row.str("id"))) fail()
                    for (key in listOf("name", "title", "label")) if (
                        row.has(key) && row.str(key).trim().length !in 1..160
                    )
                        fail()
                    for (key in
                        listOf(
                            "startsAt",
                            "endsAt",
                            "recordedAt",
                            "preparedAt",
                            "storedAt",
                            "updatedAt",
                        )) if (
                        row.has(key) && !row[key].isJsonNull && row.num(key) !in 0..7258118400000L
                    )
                        fail()
                    for (key in
                        listOf(
                            "birthday",
                            "dueDate",
                            "occurredOn",
                            "introducedOn",
                            "day",
                            "eruptedOn",
                            "completedOn",
                        )) if (
                        row.has(key) && !row[key].isJsonNull && row.num(key) !in -25567..84006
                    )
                        fail()
                    checkRecord(table, row)
                }
            }
            val children = data.rows("children").map { it.str("id") }.toSet()
            schema.keys
                .filter { it != "children" }
                .forEach { table ->
                    data.rows(table).forEach { if (it.str("childId") !in children) fail() }
                }
            fun unique(table: String, key: (JsonObject) -> String) {
                val keys = data.rows(table).map(key)
                if (keys.distinct().size != keys.size) fail()
            }
            unique("memorys") { "${it.str("childId")}:${it.num("chapter")}:${it.num("anchor")}" }
            unique("tooths") { "${it.str("childId")}:${it.num("toothIndex")}" }
            unique("meals") { "${it.str("childId")}:${it.num("day")}:${it.str("slot")}" }
            unique("routineCompletions") { "${it.str("routineId")}:${it.num("day")}" }
            val linkedPumps = data.rows("milkContainers").mapNotNull { it.nullStr("pumpId") }
            if (linkedPumps.distinct().size != linkedPumps.size) fail()
            unique("emergencyCards") { it.str("childId") }
            data
                .rows("sleeps")
                .groupBy { it.str("childId") }
                .values
                .forEach { rows ->
                    val intervals = rows.sortedBy { it.num("startsAt") }
                    intervals.zipWithNext { a, b ->
                        if ((a.nullNum("endsAt") ?: Long.MAX_VALUE) > b.num("startsAt")) fail()
                    }
                }
            val appointments = data.rows("appointments").associateBy { it.str("id") }
            data.rows("healthRecords").forEach { r ->
                r.nullStr("appointmentId")?.let { id ->
                    if (appointments[id]?.str("childId") != r.str("childId")) fail()
                }
            }
            for ((table, key, target) in
                listOf(
                    Triple("bottlePreps", "feedId", "feeds"),
                    Triple("shoppingItems", "inventoryId", "inventorys"),
                    Triple("routineCompletions", "routineId", "routines"),
                    Triple("milkContainers", "pumpId", "feeds"),
                )) {
                val parents = data.rows(target).associateBy { it.str("id") }
                data.rows(table).forEach { r ->
                    r.nullStr(key)?.let { id ->
                        if (parents[id]?.str("childId") != r.str("childId")) fail()
                    }
                }
            }
            val feedParents = data.rows("feeds").associateBy { it.str("id") }
            data.rows("milkContainers").forEach { r ->
                r.nullStr("pumpId")?.let { if (feedParents[it]?.str("kind") != "Pump") fail() }
            }
            val memories = data.rows("memorys").associateBy { it.str("id") }
            data.rows("milestones").forEach { r ->
                r.nullStr("memoryId")?.let { id ->
                    if (memories[id] != null && memories[id]?.str("childId") != r.str("childId"))
                        fail()
                }
            }
            data
                .rows("pregnancyEvents")
                .filter { it["endsAt"].isJsonNull }
                .groupBy { "${it.str("childId")}:${it.str("kind")}" }
                .values
                .forEach { if (it.size > 1) fail() }
            val photos = root["photos"]?.takeIf { it.isJsonObject }?.asJsonObject ?: fail()
            var photoBytes = 0
            photos.entrySet().forEach { (name, value) ->
                if (
                    !name.matches(Regex("[A-Za-z0-9_-]+\\.jpg")) ||
                        !value.isJsonPrimitive ||
                        !value.asJsonPrimitive.isString
                )
                    fail()
                val bytes = Base64.getDecoder().decode(value.asString)
                photoBytes += bytes.size
                if (bytes.size > 15 * 1024 * 1024 || photoBytes > 20 * 1024 * 1024) fail()
            }
            (data.rows("memorys").mapNotNull { it.nullStr("photo") } +
                    data.rows("children").mapNotNull { it.nullStr("avatar") } +
                    data.rows("children").mapNotNull { it.nullStr("homeBackgroundPhoto") })
                .forEach { if (!photos.has(it)) fail() }
            val settings = root["settings"]?.takeIf { it.isJsonObject }?.asJsonObject ?: fail()
            for (key in listOf("dark", "time24", "reduceMotion")) if (
                settings.has(key) &&
                    (!settings[key].isJsonPrimitive || !settings[key].asJsonPrimitive.isBoolean)
            )
                fail()
            for ((key, options) in
                mapOf(
                    "volumeUnit" to listOf("mL", "fl oz"),
                    "weightUnit" to listOf("kg", "lb"),
                )) if (
                settings.has(key) &&
                    (!settings[key].isJsonPrimitive ||
                        !settings[key].asJsonPrimitive.isString ||
                        settings[key].asString !in options)
            )
                fail()
            if (
                settings.has("selected") &&
                    !settings["selected"].isJsonNull &&
                    (!settings["selected"].isJsonPrimitive ||
                        !settings["selected"].asJsonPrimitive.isString)
            )
                fail()
            return root
        } catch (e: Exception) {
            if (e is IllegalArgumentException && e.message?.startsWith("This backup") == true)
                throw e
            fail()
        }
    }

    private fun checkRecord(table: String, r: JsonObject) {
        val today = LocalDate.now().toEpochDay()
        val now = System.currentTimeMillis()
        val historyTime =
            when (table) {
                "feeds",
                "sleeps",
                "pregnancyEvents" -> "startsAt"
                "healthRecords",
                "diapers",
                "pottyLogs" -> "recordedAt"
                "bottlePreps" -> "preparedAt"
                "milkContainers" -> "storedAt"
                else -> null
            }
        if (historyTime != null && r.num(historyTime) > now) fail()
        if (table in listOf("sleeps", "pregnancyEvents") && (r.nullNum("endsAt") ?: 0) > now) fail()
        val historyDate =
            when (table) {
                "foods" -> "introducedOn"
                "milestones" -> "completedOn"
                "routineCompletions" -> "day"
                else -> null
            }
        if (historyDate != null && (r.nullNum(historyDate) ?: Long.MIN_VALUE) > today) fail()

        fun one(field: String, vararg options: String) {
            if (r.str(field) !in options) fail()
        }
        when (table) {
            "children" -> {
                one("stage", "PREGNANCY", "BABY", "TODDLER", "CHILD", "TEEN")
                one("accent", "Forest", "Moss", "Amber", "Sky")
                one("treeStyle", "Summer", "Autumn", "Night")
                HomeOrganizerCatalog.decode(
                    r.str("homeSections"),
                    r.str("homeHiddenSections"),
                    r.str("homeQuickActions"),
                    r.str("homeBackground"),
                    r.nullStr("homeBackgroundPhoto"),
                )
                if ((r.nullNum("birthday") ?: Long.MIN_VALUE) > LocalDate.now().toEpochDay()) fail()
            }
            "appointments" -> {
                one(
                    "category",
                    "Doctor",
                    "Dentist",
                    "Vaccination",
                    "Prenatal",
                    "School",
                    "Activity",
                    "Therapy",
                    "Family",
                    "Other",
                )
                if (r.num("reminderMinutes") !in listOf(0L, 15L, 60L, 1440L)) fail()
            }
            "memorys" -> {
                one(
                    "category",
                    "General",
                    "First",
                    "Milestone",
                    "Funny moment",
                    "Health",
                    "Photo memory",
                    "Achievement",
                    "Family moment",
                )
                if (r.num("anchor") !in 0..31 || r.num("chapter") !in 0..50000) fail()
            }
            "feeds" -> {
                one("kind", "Bottle", "Breast", "Pump")
                one("side", "Left", "Right", "Both")
                one("contents", "Formula", "Breast milk")
                for (k in listOf("amountMl", "leftMl", "rightMl")) if (
                    r[k].asDouble !in 0.0..2000.0
                )
                    fail()
                if (r.num("durationSeconds") !in 0..43200) fail()
                when (r.str("kind")) {
                    "Bottle" -> if (r["amountMl"].asDouble <= 0) fail()
                    "Breast" -> if (r.num("durationSeconds") == 0L) fail()
                    "Pump" ->
                        if (
                            r["leftMl"].asDouble + r["rightMl"].asDouble <= 0 ||
                                kotlin.math.abs(
                                    r["amountMl"].asDouble -
                                        r["leftMl"].asDouble -
                                        r["rightMl"].asDouble
                                ) > .01
                        )
                            fail()
                }
            }
            "sleeps" -> {
                one("kind", "Nap", "Night")
                if (r.nullNum("endsAt")?.let { it <= r.num("startsAt") } == true) fail()
            }
            "foods" -> {
                one("liking", "Unsure", "Liked", "Disliked")
                one(
                    "allergen",
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
            }
            "meals" -> {
                one("slot", "Breakfast", "Lunch", "Dinner", "Snacks")
                if (
                    r.str("id") != SolidsRules.mealId(r.str("childId"), r.num("day"), r.str("slot"))
                )
                    fail()
            }
            "tooths" -> {
                if (
                    r.num("toothIndex") !in 0L..19L ||
                        !DevelopmentRules.validTooth(
                            r.num("toothIndex").toInt(),
                            r.str("stage"),
                            r.nullNum("eruptedOn"),
                            LocalDate.now().toEpochDay(),
                        ) ||
                        r.str("id") != "${r.str("childId")}-tooth-${r.num("toothIndex")}"
                )
                    fail()
            }
            "milestones" -> one("stage", "PREGNANCY", "BABY", "TODDLER", "CHILD", "TEEN")
            "pregnancyEvents" -> {
                one("kind", "Kicks", "Contraction")
                if (
                    r.num("count") !in 0..100000 ||
                        r.nullNum("endsAt")?.let { it <= r.num("startsAt") } == true
                )
                    fail()
            }
            "prepItems" -> one("kind", "Bag", "Note")
            "inventorys" ->
                if (
                    !HouseholdRules.validStock(r["quantity"].asDouble, r["threshold"].asDouble) ||
                        r.str("unit").isBlank()
                )
                    fail()
            "routines" ->
                if (r.num("weekdays") !in 1..127 || r.num("timeMinutes") !in -1..1439) fail()
            "routineCompletions" -> if (r.num("day") !in -25567..today) fail()
            "pottyLogs" -> one("kind", "Tried", "Wet", "Dirty", "Mixed", "Accident")
            "milkContainers" -> {
                one("status", "Frozen", "Used", "Discarded")
                if (r["amountMl"].asDouble <= 0 || r["amountMl"].asDouble > 2000) fail()
            }
            "diapers" -> one("kind", "Wet", "Dirty", "Mixed", "Dry")
            "bottlePreps" -> {
                one("contents", "Formula", "Breast milk")
                one("status", "Prepared", "Used", "Discarded")
                if (
                    r["amountMl"].asDouble <= 0 ||
                        r["amountMl"].asDouble > 2000 ||
                        (r.nullStr("feedId") != null && r.str("status") != "Used")
                )
                    fail()
            }
            "shoppingItems" ->
                if (
                    r["quantity"].asDouble <= 0 ||
                        r["quantity"].asDouble > 1000000 ||
                        r.str("unit").isBlank()
                )
                    fail()
            "wishlistItems" -> if (!WishlistRules.validLink(r.str("link"))) fail()
            "emergencyCards" -> if (r.str("id") != r.str("childId")) fail()
            "healthRecords" -> {
                one(
                    "kind",
                    "Weight",
                    "Height",
                    "Temperature",
                    "Medication note",
                    "Symptom",
                    "Vaccination",
                    "Doctor note",
                    "Diaper",
                    "Other",
                )
                if (r.str("kind") in listOf("Weight", "Height", "Temperature")) {
                    val v = r.str("value").replace(',', '.').toDoubleOrNull()
                    if (
                        v == null ||
                            !v.isFinite() ||
                            v <= 0 ||
                            v >= 10000 ||
                            r.str("unit").isBlank()
                    )
                        fail()
                }
            }
        }
    }

    fun JsonObject.rows(name: String) = getAsJsonArray(name).map { it.asJsonObject }

    private fun JsonObject.str(name: String) = get(name).asString

    private fun JsonObject.num(name: String) = get(name).asLong

    private fun JsonObject.nullNum(name: String) = get(name).takeUnless { it.isJsonNull }?.asLong

    private fun JsonObject.nullStr(name: String) = get(name).takeUnless { it.isJsonNull }?.asString
}

object ReminderPolicy {
    fun delay(start: Long, minutes: Int, now: Long): Long? =
        if (minutes in listOf(15, 60, 1440)) (start - minutes * 60000L - now).takeIf { it > 0 }
        else null
}
