package com.nothatcher.sproutbook.services

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.nothatcher.sproutbook.*
import com.nothatcher.sproutbook.R
import com.nothatcher.sproutbook.core.ReminderPolicy
import com.nothatcher.sproutbook.data.*
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

class AppointmentReminders(
    private val context: Context,
    private val db: AppDatabase,
    private val settings: Settings,
) {
    private val work
        get() = WorkManager.getInstance(context)

    suspend fun schedule(a: Appointment) {
        cancel(a.id)
        if (!settings.flow.first().notifications) return
        val delay =
            ReminderPolicy.delay(a.startsAt, a.reminderMinutes, System.currentTimeMillis())
                ?: return
        val request =
            OneTimeWorkRequestBuilder<AppointmentWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("id" to a.id, "updatedAt" to a.updatedAt))
                .addTag("appointments")
                .build()
        work.enqueueUniqueWork("appointment-${a.id}", ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(id: String) {
        work.cancelUniqueWork("appointment-$id")
    }

    fun cancelAll() {
        work.cancelAllWorkByTag("appointments")
    }

    suspend fun reconcile() {
        if (!settings.flow.first().notifications) cancelAll()
        else db.appointments().all().forEach { schedule(it) }
    }
}

class AppointmentWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as BabyForgeApp
        return try {
            val a =
                app.db.appointments().get(inputData.getString("id") ?: return Result.success())
                    ?: return Result.success()
            if (
                !app.settings.flow.first().notifications ||
                    a.updatedAt != inputData.getLong("updatedAt", -1) ||
                    a.startsAt < System.currentTimeMillis()
            )
                return Result.success()
            if (
                Build.VERSION.SDK_INT >= 33 &&
                    ContextCompat.checkSelfPermission(
                        app,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
            )
                return Result.success()
            val manager = app.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(
                NotificationChannel(
                    "appointments",
                    "Appointment reminders",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            )
            val intent =
                Intent(app, MainActivity::class.java)
                    .setAction("appointment-${a.id}")
                    .putExtra("childId", a.childId)
                    .putExtra("destination", "schedule")
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pending =
                PendingIntent.getActivity(
                    app,
                    a.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            val public =
                NotificationCompat.Builder(app, "appointments")
                    .setSmallIcon(R.drawable.ic_sprout)
                    .setContentTitle("SproutBook reminder")
                    .setContentText("Open your family schedule")
                    .build()
            manager.notify(
                a.id.hashCode(),
                NotificationCompat.Builder(app, "appointments")
                    .setSmallIcon(R.drawable.ic_sprout)
                    .setContentTitle(a.title)
                    .setContentText("An appointment is coming up. Tap to view your schedule.")
                    .setContentIntent(pending)
                    .setAutoCancel(true)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .setPublicVersion(public)
                    .build(),
            )
            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
