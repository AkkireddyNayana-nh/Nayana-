package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val course: String = "",
    val deadlineEpochMillis: Long,
    val isCompleted: Boolean = false,
    val priority: String = Priority.MEDIUM.name,
    val category: String = Category.ASSIGNMENT.name,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class Priority(val label: String) {
        HIGH("High"),
        MEDIUM("Medium"),
        LOW("Low");

        companion object {
            fun fromString(value: String): Priority {
                return entries.find { it.name.equals(value, ignoreCase = true) } ?: MEDIUM
            }
        }
    }

    enum class Category(val label: String) {
        ASSIGNMENT("Assignment"),
        EXAM("Exam / Quiz"),
        PROJECT("Project"),
        READING("Reading"),
        OTHER("Other");

        companion object {
            fun fromString(value: String): Category {
                return entries.find { it.name.equals(value, ignoreCase = true) } ?: ASSIGNMENT
            }
        }
    }

    enum class DeadlineUrgency {
        COMPLETED,
        OVERDUE,
        DUE_TODAY,
        DUE_TOMORROW,
        UPCOMING
    }

    fun getDeadlineUrgency(): DeadlineUrgency {
        if (isCompleted) return DeadlineUrgency.COMPLETED

        val now = Calendar.getInstance()
        val deadlineCal = Calendar.getInstance().apply { timeInMillis = deadlineEpochMillis }

        if (deadlineEpochMillis < now.timeInMillis) {
            return DeadlineUrgency.OVERDUE
        }

        val isSameDay = now.get(Calendar.YEAR) == deadlineCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == deadlineCal.get(Calendar.DAY_OF_YEAR)

        if (isSameDay) return DeadlineUrgency.DUE_TODAY

        val tomorrowCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val isTomorrow = tomorrowCal.get(Calendar.YEAR) == deadlineCal.get(Calendar.YEAR) &&
                tomorrowCal.get(Calendar.DAY_OF_YEAR) == deadlineCal.get(Calendar.DAY_OF_YEAR)

        if (isTomorrow) return DeadlineUrgency.DUE_TOMORROW

        return DeadlineUrgency.UPCOMING
    }

    /**
     * Returns true if task is incomplete and either overdue or due within 24 hours.
     */
    fun isDeadlineApproaching(): Boolean {
        if (isCompleted) return false
        val now = System.currentTimeMillis()
        val diffMillis = deadlineEpochMillis - now
        // Overdue or due within 24 hours (86,400,000 ms)
        return diffMillis < 86_400_000L
    }

    /**
     * Checks if task deadline falls on today's calendar day or is overdue and incomplete.
     */
    fun isDueToday(): Boolean {
        val now = Calendar.getInstance()
        val deadlineCal = Calendar.getInstance().apply { timeInMillis = deadlineEpochMillis }
        return now.get(Calendar.YEAR) == deadlineCal.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == deadlineCal.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Dynamic warning text for approaching deadlines.
     */
    fun getApproachingWarningText(): String? {
        if (isCompleted) return null
        val now = System.currentTimeMillis()
        val diffMillis = deadlineEpochMillis - now

        return when {
            diffMillis < 0 -> {
                val overdueMinutes = (-diffMillis) / (60 * 1000)
                val overdueHours = overdueMinutes / 60
                val overdueDays = overdueHours / 24
                when {
                    overdueDays > 0 -> "Overdue by $overdueDays ${if (overdueDays == 1L) "day" else "days"}!"
                    overdueHours > 0 -> "Overdue by $overdueHours ${if (overdueHours == 1L) "hr" else "hrs"}!"
                    else -> "Overdue by $overdueMinutes mins!"
                }
            }
            diffMillis < 60 * 60 * 1000L -> {
                val mins = (diffMillis / (60 * 1000L)).coerceAtLeast(1)
                "Due in $mins mins!"
            }
            diffMillis < 24 * 60 * 60 * 1000L -> {
                val hours = diffMillis / (60 * 60 * 1000L)
                "Due in $hours hrs!"
            }
            else -> null
        }
    }

    fun getFormattedDeadline(): String {
        val date = Date(deadlineEpochMillis)
        val urgency = getDeadlineUrgency()
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val timeStr = timeFormat.format(date)

        return when (urgency) {
            DeadlineUrgency.COMPLETED -> {
                val fullFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                "Completed (was due ${fullFormat.format(date)})"
            }
            DeadlineUrgency.DUE_TODAY -> "Today at $timeStr"
            DeadlineUrgency.DUE_TOMORROW -> "Tomorrow at $timeStr"
            DeadlineUrgency.OVERDUE -> {
                val fullFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                "Overdue (${fullFormat.format(date)})"
            }
            DeadlineUrgency.UPCOMING -> {
                val fullFormat = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                fullFormat.format(date)
            }
        }
    }
}
