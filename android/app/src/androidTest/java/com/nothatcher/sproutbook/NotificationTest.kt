package com.nothatcher.sproutbook

import android.app.NotificationManager
import androidx.test.core.app.ApplicationProvider
import androidx.work.*
import com.nothatcher.sproutbook.data.*
import com.nothatcher.sproutbook.services.AppointmentWorker
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class NotificationTest {
    @Test
    fun appointmentNotificationsRequireOptInAndFreshRecord(): Unit = runBlocking {
        val app = ApplicationProvider.getApplicationContext<BabyForgeApp>()
        val manager = app.getSystemService(NotificationManager::class.java)
        val work = WorkManager.getInstance(app)
        val a =
            Appointment(
                id = "qa-notification",
                childId = "qa-notify-child",
                title = "Check-up",
                startsAt = System.currentTimeMillis() + 3600000,
                updatedAt = 17,
            )
        app.db.children().save(Child(id = a.childId, name = "Juniper"))
        app.db.appointments().save(a)
        manager.cancel(a.id.hashCode())
        suspend fun run(version: Long) {
            val request =
                OneTimeWorkRequestBuilder<AppointmentWorker>()
                    .setInputData(workDataOf("id" to a.id, "updatedAt" to version))
                    .build()
            work.enqueue(request).result.get(30, TimeUnit.SECONDS)
            repeat(300) {
                if (
                    work.getWorkInfoById(request.id).get(10, TimeUnit.SECONDS)?.state?.isFinished ==
                        true
                )
                    return
                delay(100)
            }
            fail("Worker did not finish")
        }
        try {
            app.settings.boolean("notifications", false)
            run(17)
            assertFalse(manager.activeNotifications.any { it.id == a.id.hashCode() })
            app.settings.boolean("notifications", true)
            run(16)
            assertFalse(manager.activeNotifications.any { it.id == a.id.hashCode() })
            run(17)
            val posted = manager.activeNotifications.single { it.id == a.id.hashCode() }
            assertEquals("Check-up", posted.notification.extras.getString("android.title"))
            assertNotNull(posted.notification.contentIntent)
        } finally {
            manager.cancel(a.id.hashCode())
            app.settings.boolean("notifications", false)
            app.db.children().delete(a.childId)
        }
    }
}
