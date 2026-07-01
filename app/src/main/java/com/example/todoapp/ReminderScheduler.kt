package com.example.todoapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

/**
 * Schedules and cancels real, time-triggered reminder notifications using WorkManager.
 * Each task can have up to 3 reminders scheduled: 1 day before, 1 hour before, and
 * at the exact deadline. Each is a separate WorkRequest with a calculated initial delay,
 * so it fires at the right moment even if the app isn't open.
 */
object ReminderScheduler {

    fun canScheduleExactAlarms(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else {
            true // exact alarm permission isn't required before Android 12
        }
    }

    /**
     * Shows a direct system dialog asking the user to exempt this app from
     * battery optimizations. This is the one thing we CAN automate around
     * background reminder delivery - autostart/recents-lock toggles on
     * OEM skins (Xiaomi, Vivo, Oppo, etc.) have no public API and can only
     * be enabled manually by the user.
     */
    fun requestIgnoreBatteryOptimizations(context: Context) {
        val packageName = context.packageName
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
            }
            context.startActivity(intent)
        }
    }

    private fun workName(taskId: Int, kind: String) = "reminder_${taskId}_$kind"

    fun scheduleAllReminders(context: Context, task: TodoTask) {
        cancelAllReminders(context, task.id)
        if (task.isDone) return

        if (task.remindHourBefore) {
            val triggerAt = task.deadline.minusHours(1)
            scheduleOne(
                context, task, "hour_before", triggerAt,
                title = "⏰ Due in an hour!",
                body = "\"${task.title}\" is due at ${formatDeadline(task.deadline)}"
            )
        }
        if (task.remindAtDeadline) {
            scheduleOne(
                context, task, "at_deadline", task.deadline,
                title = "🔔 Task due now!",
                body = "\"${task.title}\" is due right now."
            )
        }
    }

    private fun scheduleOne(
        context: Context,
        task: TodoTask,
        kind: String,
        triggerAt: LocalDateTime,
        title: String,
        body: String
    ) {
        val now = LocalDateTime.now()
        val delayMillis = Duration.between(now, triggerAt).toMillis()
        if (delayMillis <= 0) return // that reminder's moment has already passed - skip it

        val notificationId = (task.id.toString() + kind).hashCode()

        val data = Data.Builder()
            .putString(ReminderWorker.KEY_TITLE, title)
            .putString(ReminderWorker.KEY_BODY, body)
            .putInt(ReminderWorker.KEY_NOTIFICATION_ID, notificationId)
            .putInt(ReminderWorker.KEY_TASK_ID, task.id)
            .putString(ReminderWorker.KEY_KIND, kind)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(task.id, kind),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelAllReminders(context: Context, taskId: Int) {
        val manager = WorkManager.getInstance(context)
        listOf("day_before", "hour_before", "at_deadline").forEach { kind ->
            manager.cancelUniqueWork(workName(taskId, kind))
        }
    }
}