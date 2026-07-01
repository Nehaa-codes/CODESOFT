package com.example.todoapp

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime

private val Context.taskDataStore by preferencesDataStore(name = "todo_tasks_store")
private val TASKS_KEY = stringPreferencesKey("tasks_data")
private val USERNAME_KEY = stringPreferencesKey("user_name")

private const val FIELD_SEP = "\u0001"
private const val RECORD_SEP = "\u0002"

/**
 * Handles saving/loading the task list to disk using Preferences DataStore.
 * Tasks are hand-encoded as delimited strings (no JSON library dependency).
 * Title is sanitized so it can never contain the delimiter characters.
 */
object TaskRepository {

    suspend fun loadTasks(context: Context): List<TodoTask> {
        val prefs = context.taskDataStore.data.first()
        val raw = prefs[TASKS_KEY] ?: return emptyList()
        if (raw.isBlank()) return emptyList()

        return raw.split(RECORD_SEP).mapNotNull { record ->
            if (record.isBlank()) return@mapNotNull null
            val parts = record.split(FIELD_SEP)
            if (parts.size < 6) return@mapNotNull null
            try {
                TodoTask(
                    id = parts[0].toInt(),
                    title = parts[1],
                    priority = parts[2].toFloat(),
                    deadline = LocalDateTime.parse(parts[3]),
                    isDone = parts[4].toBoolean(),
                    notifiedDayBefore = parts[5].toBoolean(),
                    notifiedHourBefore = parts.getOrElse(6) { "false" }.toBoolean(),
                    remindDayBefore = parts.getOrElse(7) { "true" }.toBoolean(),
                    remindHourBefore = parts.getOrElse(8) { "true" }.toBoolean(),
                    remindAtDeadline = parts.getOrElse(9) { "true" }.toBoolean(),
                    repeatDaily = parts.getOrElse(10) { "false" }.toBoolean(),
                    notifiedAtDeadline = parts.getOrElse(11) { "false" }.toBoolean(),
                    category = try {
                        TaskCategory.valueOf(parts.getOrElse(12) { "OTHER" })
                    } catch (e: Exception) {
                        TaskCategory.OTHER
                    }
                )
            } catch (e: Exception) {
                null // skip any corrupted record rather than crashing the app
            }
        }
    }

    suspend fun saveTasks(context: Context, tasks: List<TodoTask>) {
        val encoded = tasks.joinToString(RECORD_SEP) { task ->
            val safeTitle = task.title.replace(FIELD_SEP, " ").replace(RECORD_SEP, " ")
            listOf(
                task.id.toString(),
                safeTitle,
                task.priority.toString(),
                task.deadline.toString(),
                task.isDone.toString(),
                task.notifiedDayBefore.toString(),
                task.notifiedHourBefore.toString(),
                task.remindDayBefore.toString(),
                task.remindHourBefore.toString(),
                task.remindAtDeadline.toString(),
                task.repeatDaily.toString(),
                task.notifiedAtDeadline.toString(),
                task.category.name
            ).joinToString(FIELD_SEP)
        }
        context.taskDataStore.edit { prefs ->
            prefs[TASKS_KEY] = encoded
        }
    }

    suspend fun nextTaskId(context: Context): Int {
        val tasks = loadTasks(context)
        return (tasks.maxOfOrNull { it.id } ?: -1) + 1
    }

    suspend fun getUserName(context: Context): String {
        val prefs = context.taskDataStore.data.first()
        return prefs[USERNAME_KEY] ?: ""
    }

    suspend fun saveUserName(context: Context, name: String) {
        context.taskDataStore.edit { prefs ->
            prefs[USERNAME_KEY] = name.trim()
        }
    }
}