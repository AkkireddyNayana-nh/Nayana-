package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    suspend fun toggleCompleted(task: TaskEntity) {
        taskDao.setCompleted(task.id, !task.isCompleted)
    }

    suspend fun seedInitialTasksIfEmpty() {
        if (taskDao.getTaskCount() == 0) {
            val now = Calendar.getInstance()

            val dueToday = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val dueInThreeDays = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 3)
                set(Calendar.HOUR_OF_DAY, 14)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val dueNextWeek = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 7)
                set(Calendar.HOUR_OF_DAY, 17)
                set(Calendar.MINUTE, 30)
                set(Calendar.SECOND, 0)
            }.timeInMillis

            val sampleTasks = listOf(
                TaskEntity(
                    title = "Submit CS101 Algorithm Lab Report",
                    course = "CS 101",
                    deadlineEpochMillis = dueToday,
                    isCompleted = false,
                    priority = TaskEntity.Priority.HIGH.name,
                    category = TaskEntity.Category.ASSIGNMENT.name,
                    notes = "Submit via campus Canvas portal in PDF format with test cases."
                ),
                TaskEntity(
                    title = "Review Chapters 4-6 for Math Midterm",
                    course = "MATH 201",
                    deadlineEpochMillis = dueInThreeDays,
                    isCompleted = false,
                    priority = TaskEntity.Priority.HIGH.name,
                    category = TaskEntity.Category.EXAM.name,
                    notes = "Practice linear transformation and eigenvector questions from textbook."
                ),
                TaskEntity(
                    title = "Read History Chapter 8 & summarize primary sources",
                    course = "HIST 110",
                    deadlineEpochMillis = dueNextWeek,
                    isCompleted = false,
                    priority = TaskEntity.Priority.MEDIUM.name,
                    category = TaskEntity.Category.READING.name,
                    notes = "Prepare discussion points for Wednesday seminar."
                )
            )
            taskDao.insertAll(sampleTasks)
        }
    }
}
