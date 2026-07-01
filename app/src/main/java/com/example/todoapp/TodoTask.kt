package com.example.todoapp

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

enum class TaskCategory(val displayName: String, val emoji: String) {
    WORK("Work", "💼"),
    PERSONAL("Personal", "🏠"),
    HEALTH("Health", "💗"),
    STUDY("Study", "📚"),
    OTHER("Other", "📌")
}

data class TodoTask(
    val id: Int,
    val title: String,
    val priority: Float,          // 0f (low/green) .. 100f (high/red)
    val deadline: LocalDateTime,   // real date + time
    var isDone: Boolean = false,
    val category: TaskCategory = TaskCategory.OTHER,
    // Which reminders the user wants for this task (all optional, chosen at creation)
    val remindDayBefore: Boolean = true,
    val remindHourBefore: Boolean = true,
    val remindAtDeadline: Boolean = true,
    // Whether this task repeats every day (medicine, daily habits, etc.)
    val repeatDaily: Boolean = false,
    // Internal bookkeeping so each reminder only fires once per occurrence
    var notifiedDayBefore: Boolean = false,
    var notifiedHourBefore: Boolean = false,
    var notifiedAtDeadline: Boolean = false
)

/** Interpolates green -> yellow -> red as priority goes 0 -> 100 */
fun priorityToColor(priority: Float): Color {
    val p = priority.coerceIn(0f, 100f) / 100f
    return if (p < 0.5f) {
        // green -> yellow
        val t = p / 0.5f
        Color(
            red = (76 + t * (251 - 76)) / 255f,
            green = (175 + t * (192 - 175)) / 255f,
            blue = (80 + t * (0 - 80)) / 255f
        )
    } else {
        // yellow -> red
        val t = (p - 0.5f) / 0.5f
        Color(
            red = (251 + t * (229 - 251)) / 255f,
            green = (192 + t * (57 - 192)) / 255f,
            blue = (0 + t * (53 - 0)) / 255f
        )
    }
}

fun priorityLabel(priority: Float): String = when {
    priority >= 66f -> "High"
    priority >= 33f -> "Medium"
    else -> "Low"
}

private val deadlineFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

fun formatDeadline(deadline: LocalDateTime): String = deadline.format(deadlineFormatter)