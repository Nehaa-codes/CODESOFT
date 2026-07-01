package com.example.todoapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * Fires exactly one notification when WorkManager triggers it at the scheduled time.
 * All the data it needs travels in via input data - it doesn't read the task list itself.
 *
 * For the "at_deadline" reminder of a repeating task, it also advances that task's
 * deadline by one day and reschedules its reminders, so daily tasks (like medicine)
 * keep firing every day without the user needing to reopen the app.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: return Result.success()
        val body = inputData.getString(KEY_BODY) ?: ""
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, 0)
        val taskId = inputData.getInt(KEY_TASK_ID, -1)
        val kind = inputData.getString(KEY_KIND) ?: ""

        ensureChannel()

        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId,
            openAppIntent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        manager.notify(notificationId, notification)

        // If this was the "due now" reminder for a repeating task, roll it forward a day
        if (kind == "at_deadline" && taskId != -1) {
            val tasks = TaskRepository.loadTasks(applicationContext).toMutableList()
            val index = tasks.indexOfFirst { it.id == taskId }
            if (index != -1 && tasks[index].repeatDaily) {
                val advanced = tasks[index].copy(
                    deadline = tasks[index].deadline.plusDays(1),
                    notifiedDayBefore = false,
                    notifiedHourBefore = false,
                    notifiedAtDeadline = false
                )
                tasks[index] = advanced
                TaskRepository.saveTasks(applicationContext, tasks)
                ReminderScheduler.scheduleAllReminders(applicationContext, advanced)
            }
        }

        return Result.success()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Todo Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = applicationContext.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "todo_channel"
        const val KEY_TITLE = "title"
        const val KEY_BODY = "body"
        const val KEY_NOTIFICATION_ID = "notification_id"
        const val KEY_TASK_ID = "task_id"
        const val KEY_KIND = "kind"
    }
}