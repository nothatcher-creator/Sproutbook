package com.nothatcher.sproutbook

import android.app.Application
import androidx.room.Room
import com.nothatcher.sproutbook.data.*

class BabyForgeApp : Application() {
    val settings by lazy { Settings(this) }
    val db by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "sproutbook-v3.db").build()
    }
    val reminders by lazy {
        com.nothatcher.sproutbook.services.AppointmentReminders(this, db, settings)
    }
    val repository by lazy { FamilyRepository(db, settings, reminders) }
}
