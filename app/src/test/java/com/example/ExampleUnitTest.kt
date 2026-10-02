package com.example

import com.example.data.TaskEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {

    @Test
    fun `test TaskEntity priority parsing`() {
        assertEquals(TaskEntity.Priority.HIGH, TaskEntity.Priority.fromString("high"))
        assertEquals(TaskEntity.Priority.MEDIUM, TaskEntity.Priority.fromString("medium"))
        assertEquals(TaskEntity.Priority.LOW, TaskEntity.Priority.fromString("low"))
        assertEquals(TaskEntity.Priority.MEDIUM, TaskEntity.Priority.fromString("unknown"))
    }

    @Test
    fun `test TaskEntity category parsing`() {
        assertEquals(TaskEntity.Category.ASSIGNMENT, TaskEntity.Category.fromString("assignment"))
        assertEquals(TaskEntity.Category.EXAM, TaskEntity.Category.fromString("exam"))
        assertEquals(TaskEntity.Category.PROJECT, TaskEntity.Category.fromString("project"))
        assertEquals(TaskEntity.Category.READING, TaskEntity.Category.fromString("reading"))
        assertEquals(TaskEntity.Category.OTHER, TaskEntity.Category.fromString("other"))
    }

    @Test
    fun `test completed task urgency returns completed`() {
        val task = TaskEntity(
            title = "Done Task",
            deadlineEpochMillis = System.currentTimeMillis() - 100000L,
            isCompleted = true
        )
        assertEquals(TaskEntity.DeadlineUrgency.COMPLETED, task.getDeadlineUrgency())
        assertFalse(task.isDeadlineApproaching())
        assertNull(task.getApproachingWarningText())
    }

    @Test
    fun `test overdue task urgency and visual warning`() {
        val task = TaskEntity(
            title = "Late Assignment",
            deadlineEpochMillis = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2L), // 2 days ago
            isCompleted = false
        )
        assertEquals(TaskEntity.DeadlineUrgency.OVERDUE, task.getDeadlineUrgency())
        assertTrue(task.isDeadlineApproaching())
        assertNotNull(task.getApproachingWarningText())
        assertTrue(task.getApproachingWarningText()!!.contains("Overdue"))
    }

    @Test
    fun `test due today task urgency and approaching warning`() {
        val todayLater = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
        }.timeInMillis

        val task = TaskEntity(
            title = "Today Task",
            deadlineEpochMillis = todayLater,
            isCompleted = false
        )
        assertEquals(TaskEntity.DeadlineUrgency.DUE_TODAY, task.getDeadlineUrgency())
        assertTrue(task.isDueToday())
        assertTrue(task.isDeadlineApproaching())
    }

    @Test
    fun `test editing task properties creates updated copy`() {
        val original = TaskEntity(
            title = "Original Title",
            course = "CS101",
            deadlineEpochMillis = 1000000L,
            priority = TaskEntity.Priority.LOW.name
        )

        val edited = original.copy(
            title = "Updated Title",
            course = "CS102",
            priority = TaskEntity.Priority.HIGH.name,
            isCompleted = true
        )

        assertEquals("Updated Title", edited.title)
        assertEquals("CS102", edited.course)
        assertEquals(TaskEntity.Priority.HIGH.name, edited.priority)
        assertTrue(edited.isCompleted)
    }
}
