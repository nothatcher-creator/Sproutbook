package com.nothatcher.sproutbook.services

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.withTransaction
import com.google.gson.*
import com.nothatcher.sproutbook.core.*
import com.nothatcher.sproutbook.data.*
import java.util.Base64
import java.util.UUID
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BackupManager(private val context: Context, private val repo: FamilyRepository) {
    private val gson = GsonBuilder().serializeNulls().create()

    suspend fun snapshot(): JsonObject =
        withContext(Dispatchers.IO) {
            val data =
                repo.db.withTransaction {
                    JsonObject().apply {
                        add("children", gson.toJsonTree(repo.db.children().all()))
                        add("routines", gson.toJsonTree(repo.db.routines().all()))
                        add("routineCompletions", gson.toJsonTree(repo.db.routineCompletions().all()))
                        add("pottyLogs", gson.toJsonTree(repo.db.pottyLogs().all()))
                        add("milkContainers", gson.toJsonTree(repo.db.milkContainers().all()))
                        add("appointments", gson.toJsonTree(repo.db.appointments().all()))
                        add("memorys", gson.toJsonTree(repo.db.memorys().all()))
                        add("feeds", gson.toJsonTree(repo.db.feeds().all()))
                        add("sleeps", gson.toJsonTree(repo.db.sleeps().all()))
                        add("foods", gson.toJsonTree(repo.db.foods().all()))
                        add("meals", gson.toJsonTree(repo.db.meals().all()))
                        add("tooths", gson.toJsonTree(repo.db.tooths().all()))
                        add("milestones", gson.toJsonTree(repo.db.milestones().all()))
                        add("pregnancyEvents", gson.toJsonTree(repo.db.pregnancyEvents().all()))
                        add("prepItems", gson.toJsonTree(repo.db.prepItems().all()))
                        add("diapers", gson.toJsonTree(repo.db.diapers().all()))
                        add("bottlePreps", gson.toJsonTree(repo.db.bottlePreps().all()))
                        add("shoppingItems", gson.toJsonTree(repo.db.shoppingItems().all()))
                        add("wishlistItems", gson.toJsonTree(repo.db.wishlistItems().all()))
                        add("inventorys", gson.toJsonTree(repo.db.inventorys().all()))
                        add("emergencyCards", gson.toJsonTree(repo.db.emergencyCards().all()))
                        add("healthRecords", gson.toJsonTree(repo.db.healthRecords().all()))
                    }
                }
            val prefs = repo.settings.flow.first()
            val settings =
                JsonObject().apply {
                    addProperty("selected", prefs.selected)
                    addProperty("dark", prefs.dark)
                    addProperty("time24", prefs.time24)
                    addProperty("reduceMotion", prefs.reduceMotion)
                    addProperty("volumeUnit", prefs.volumeUnit)
                    addProperty("weightUnit", prefs.weightUnit)
                }
            val photos = JsonObject()
            val names =
                (data.getAsJsonArray("memorys").mapNotNull {
                        it.asJsonObject["photo"].takeUnless { it.isJsonNull }?.asString
                    } +
                        data.getAsJsonArray("children").mapNotNull {
                            it.asJsonObject["avatar"].takeUnless { it.isJsonNull }?.asString
                        } +
                        data.getAsJsonArray("children").mapNotNull {
                            it.asJsonObject["homeBackgroundPhoto"].takeUnless { it.isJsonNull }?.asString
                        })
                    .distinct()
            var bytesTotal = 0
            names.forEach { name ->
                val file = PhotoStore.file(context, name)
                require(file.isFile) {
                    "A referenced photo is missing. Restore the photo or remove its reference before exporting."
                }
                val bytes = file.inputStream().use { BoundedIo.read(it, 15 * 1024 * 1024) }
                bytesTotal += bytes.size
                require(bytesTotal <= 20 * 1024 * 1024) {
                    "Photos exceed this backup format's 20 MB limit. No incomplete backup was created."
                }
                photos.addProperty(name, Base64.getEncoder().encodeToString(bytes))
            }
            BackupFormat.validate(
                JsonObject()
                    .apply {
                        addProperty("format", "sproutbook")
                        addProperty("version", BackupFormat.VERSION)
                        addProperty("exportedAt", System.currentTimeMillis())
                        add("tables", data)
                        add("settings", settings)
                        add("photos", photos)
                    }
                    .toString()
            )
        }

    suspend fun export(uri: Uri) {
        val text = snapshot().toString()
        withContext(Dispatchers.IO) {
            val output =
                context.contentResolver.openOutputStream(uri, "wt")
                    ?: error("Choose a writable backup location.")
            output.use { it.write(text.toByteArray(Charsets.UTF_8)) }
        }
        repo.settings.backupNow()
    }

    suspend fun read(uri: Uri): JsonObject =
        withContext(Dispatchers.IO) {
            val bytes =
                context.contentResolver.openInputStream(uri)?.use {
                    BoundedIo.read(it, BackupFormat.MAX_BYTES)
                } ?: error("The backup could not be opened.")
            val root = BackupFormat.validate(bytes.toString(Charsets.UTF_8))
            validatePhotos(root)
            root
        }

    private fun validatePhotos(root: JsonObject) {
        root.getAsJsonObject("photos").entrySet().forEach { (_, v) ->
            val bytes = Base64.getDecoder().decode(v.asString)
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            require(options.outWidth in 1..2000 && options.outHeight in 1..2000) {
                "A backup photo is invalid or too large."
            }
            val bitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: error("A backup photo could not be decoded.")
            bitmap.recycle()
        }
    }

    /** New photo files are staged before the transaction; existing photos are never overwritten. */
    suspend fun restore(reviewed: JsonObject): Boolean =
        repo.write {
            withContext(Dispatchers.IO) {
                val root = BackupFormat.validate(reviewed.toString())
                validatePhotos(root)
                val data = root.getAsJsonObject("tables")
                val created = mutableListOf<java.io.File>()
                var committed = false
                try {
                    val replacements =
                        root.getAsJsonObject("photos").entrySet().associate { (name, value) ->
                            val fresh = "${UUID.randomUUID()}.jpg"
                            val file = PhotoStore.file(context, fresh)
                            file.parentFile!!.mkdirs()
                            created += file
                            file.writeBytes(Base64.getDecoder().decode(value.asString))
                            name to fresh
                        }
                    for ((table, key) in
                        listOf(
                            "memorys" to "photo",
                            "children" to "avatar",
                            "children" to "homeBackgroundPhoto",
                        )) data
                        .getAsJsonArray(table)
                        .forEach {
                            val row = it.asJsonObject
                            if (!row[key].isJsonNull)
                                row.addProperty(key, replacements.getValue(row[key].asString))
                        }
                    // Turn off reminders before changing records; worker also checks current
                    // settings
                    // and row version.
                    repo.settings.boolean("notifications", false)
                    repo.reminders?.cancelAll()
                    repo.db.withTransaction {
                        repo.db.children().all().forEach { repo.db.children().delete(it.id) }
                        data.getAsJsonArray("children").forEach {
                            repo.db.children().save(gson.fromJson(it, Child::class.java))
                        }
                        data.getAsJsonArray("appointments").forEach {
                            repo.db.appointments().save(gson.fromJson(it, Appointment::class.java))
                        }
                        data.getAsJsonArray("memorys").forEach {
                            repo.db.memorys().save(gson.fromJson(it, Memory::class.java))
                        }
                        data.getAsJsonArray("feeds").forEach {
                            repo.db.feeds().save(gson.fromJson(it, Feed::class.java))
                        }
                        data.getAsJsonArray("routines").forEach {
                            repo.db.routines().save(gson.fromJson(it, Routine::class.java))
                        }
                        data.getAsJsonArray("routineCompletions").forEach {
                            repo.db.routineCompletions().save(gson.fromJson(it, RoutineCompletion::class.java))
                        }
                        data.getAsJsonArray("pottyLogs").forEach {
                            repo.db.pottyLogs().save(gson.fromJson(it, PottyLog::class.java))
                        }
                        data.getAsJsonArray("milkContainers").forEach {
                            repo.db.milkContainers().save(gson.fromJson(it, MilkContainer::class.java))
                        }
                        data.getAsJsonArray("sleeps").forEach {
                            repo.db.sleeps().save(gson.fromJson(it, Sleep::class.java))
                        }
                        data.getAsJsonArray("foods").forEach {
                            repo.db.foods().save(gson.fromJson(it, Food::class.java))
                        }
                        data.getAsJsonArray("meals").forEach {
                            repo.db.meals().save(gson.fromJson(it, Meal::class.java))
                        }
                        data.getAsJsonArray("tooths").forEach {
                            repo.db.tooths().save(gson.fromJson(it, Tooth::class.java))
                        }
                        data.getAsJsonArray("milestones").forEach {
                            repo.db.milestones().save(gson.fromJson(it, Milestone::class.java))
                        }
                        data.getAsJsonArray("pregnancyEvents").forEach {
                            repo.db
                                .pregnancyEvents()
                                .save(gson.fromJson(it, PregnancyEvent::class.java))
                        }
                        data.getAsJsonArray("prepItems").forEach {
                            repo.db.prepItems().save(gson.fromJson(it, PrepItem::class.java))
                        }
                        data.getAsJsonArray("inventorys").forEach {
                            repo.db.inventorys().save(gson.fromJson(it, Inventory::class.java))
                        }
                        data.getAsJsonArray("emergencyCards").forEach {
                            repo.db
                                .emergencyCards()
                                .save(gson.fromJson(it, EmergencyCard::class.java))
                        }
                        data.getAsJsonArray("diapers").forEach {
                            repo.db.diapers().save(gson.fromJson(it, Diaper::class.java))
                        }
                        data.getAsJsonArray("bottlePreps").forEach {
                            repo.db.bottlePreps().save(gson.fromJson(it, BottlePrep::class.java))
                        }
                        data.getAsJsonArray("shoppingItems").forEach {
                            repo.db
                                .shoppingItems()
                                .save(gson.fromJson(it, ShoppingItem::class.java))
                        }
                        data.getAsJsonArray("wishlistItems").forEach {
                            repo.db.wishlistItems().save(gson.fromJson(it, WishlistItem::class.java))
                        }
                        data.getAsJsonArray("healthRecords").forEach {
                            repo.db
                                .healthRecords()
                                .save(gson.fromJson(it, HealthRecord::class.java))
                        }
                    }
                    committed = true
                    val prefs = root.getAsJsonObject("settings")
                    // Room commit is authoritative. A preference-storage failure is reported
                    // separately.
                    try {
                        for (key in listOf("dark", "time24", "reduceMotion")) if (prefs.has(key))
                            repo.settings.boolean(key, prefs[key].asBoolean)
                        for (key in listOf("volumeUnit", "weightUnit")) if (prefs.has(key))
                            repo.settings.string(key, prefs[key].asString)
                        val selected = prefs["selected"]?.takeUnless { it.isJsonNull }?.asString
                        repo.settings.select(selected)
                        true
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        false
                    }
                } finally {
                    if (!committed) created.forEach { it.delete() }
                }
            }
        }
}
