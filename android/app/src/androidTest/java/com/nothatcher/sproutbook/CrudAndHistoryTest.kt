package com.nothatcher.sproutbook

import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import com.nothatcher.sproutbook.data.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class CrudAndHistoryTest {
    @Test
    fun connectedRecordsCreateUpdateDelete(): Unit = runBlocking {
        val c = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(c, AppDatabase::class.java).build()
        val settings = Settings(c)
        settings.boolean("grandparent", false)
        val r = FamilyRepository(db, settings)
        try {
            r.saveChild(Child(id = "a", name = "Rowan"))
            r.saveChild(Child(id = "b", name = "Ash"))
            val food = Food(id = "food", childId = "a", name = "Pear", introducedOn = 1)
            r.saveFood(food)
            r.saveFood(food.copy(liking = "Liked", reaction = "Discuss with clinician"))
            assertEquals("Liked", db.foods().get("food")!!.liking)
            val meal = Meal(childId = "a", day = 1, slot = "Lunch", title = "Pear")
            r.saveMeal(meal)
            r.saveMeal(meal.copy(title = "Soft pear"))
            assertEquals(1, db.meals().all().size)
            r.saveTooth(Tooth(childId = "a", toothIndex = 0, stage = "Observed"))
            r.saveTooth(Tooth(childId = "a", toothIndex = 0, stage = "Erupted", eruptedOn = 1))
            assertEquals(1, db.tooths().all().size)
            val stock =
                Inventory(
                    id = "stock",
                    childId = "a",
                    name = "Wipes",
                    quantity = 2.0,
                    unit = "packs",
                    threshold = 1.0,
                )
            r.saveInventory(stock)
            r.adjustInventory("stock", "a", -5.0)
            assertEquals(0.0, db.inventorys().get("stock")!!.quantity, 0.0)
            r.saveEmergency(EmergencyCard(childId = "a", allergies = "Record from care team"))
            assertEquals("Record from care team", db.emergencyCards().get("a")!!.allergies)
            val appointment =
                Appointment(
                    id = "visit",
                    childId = "a",
                    title = "Check-up",
                    startsAt = System.currentTimeMillis() + 3600000,
                )
            r.saveAppointment(appointment)
            val health =
                HealthRecord(
                    id = "health",
                    childId = "a",
                    kind = "Doctor note",
                    recordedAt = 1000,
                    title = "Follow-up",
                    appointmentId = "visit",
                )
            r.saveHealth(health)
            r.saveHealth(health.copy(notes = "Results saved"))
            r.deleteAppointment(appointment)
            assertNull(db.healthRecords().get("health")!!.appointmentId)
            r.savePrep(PrepItem(id = "prep", childId = "a", kind = "Bag", title = "Clothes"))
            r.togglePrep(db.prepItems().get("prep")!!)
            assertTrue(db.prepItems().get("prep")!!.completed)
            r.kick("a")
            r.kick("a")
            val kick = db.pregnancyEvents().all().single()
            assertEquals(2, kick.count)
            r.finishPregnancySession(kick.id, "a")
            assertNotNull(db.pregnancyEvents().get(kick.id)!!.endsAt)
            r.saveMemory(Memory(id = "m1", childId = "a", title = "First smile", occurredOn = 1))
            r.saveMemory(Memory(id = "m2", childId = "a", title = "Family day", occurredOn = 1))
            r.arrangeMemory("a", "m1", 1)
            assertEquals(1, db.memorys().get("m1")!!.anchor)
            assertEquals(0, db.memorys().get("m2")!!.anchor)
            r.deleteChild("a")
            assertTrue(db.foods().all().isEmpty())
            assertTrue(db.memorys().all().isEmpty())
            assertTrue(db.meals().all().isEmpty())
            assertTrue(db.healthRecords().all().isEmpty())
            assertEquals("b", db.children().all().single().id)
        } finally {
            db.close()
        }
    }

    @Test
    fun pagingHasNoHistoricalCutoffAndFiltersBeforeLimit(): Unit = runBlocking {
        val c = ApplicationProvider.getApplicationContext<android.content.Context>()
        val db = Room.inMemoryDatabaseBuilder(c, AppDatabase::class.java).build()
        try {
            db.children().save(Child(id = "a", name = "Rowan"))
            db.withTransaction {
                repeat(501) { i ->
                    db.feeds()
                        .save(
                            Feed(
                                id = "f$i",
                                childId = "a",
                                kind = if (i == 500) "Pump" else "Bottle",
                                startsAt = i.toLong(),
                                amountMl = 90.0,
                            )
                        )
                    db.sleeps()
                        .save(
                            Sleep(
                                id = "s$i",
                                childId = "a",
                                startsAt = i * 10000L,
                                endsAt = i * 10000L + 1000,
                            )
                        )
                    db.healthRecords()
                        .save(
                            HealthRecord(
                                id = "h$i",
                                childId = "a",
                                kind = "Doctor note",
                                recordedAt = i.toLong(),
                                title = "Note $i",
                            )
                        )
                    db.pregnancyEvents()
                        .save(
                            PregnancyEvent(
                                id = "p$i",
                                childId = "a",
                                kind = "Kicks",
                                startsAt = i.toLong(),
                                endsAt = i + 1L,
                            )
                        )
                }
            }
            assertEquals(201, db.feeds().history("a", "All", 201).first().size)
            assertEquals(501, db.feeds().history("a", "All", 601).first().size)
            assertEquals(1, db.feeds().history("a", "Pump", 201).first().size)
            assertEquals(501, db.sleeps().history("a", "All", 601).first().size)
            assertEquals(501, db.healthRecords().history("a", "All", 601).first().size)
            assertEquals(501, db.pregnancyEvents().history("a", "Kicks", 601).first().size)
            assertEquals(20, db.sleeps().recentNaps("a").first().size)
        } finally {
            db.close()
        }
    }
}
