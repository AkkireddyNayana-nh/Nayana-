package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = TaskRepository(database.taskDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `verify app name resource is TaskBuddy`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("TaskBuddy", appName)
    }

    @Test
    fun `insert task and verify persistence in Room`() = runBlocking {
        val testTask = TaskEntity(
            title = "CS101 Lab 3",
            course = "CS101",
            deadlineEpochMillis = System.currentTimeMillis() + 86400000L,
            isCompleted = false,
            priority = TaskEntity.Priority.HIGH.name,
            category = TaskEntity.Category.ASSIGNMENT.name
        )

        val id = repository.insertTask(testTask)
        val loadedTask = repository.getTaskById(id)

        assertEquals("CS101 Lab 3", loadedTask?.title)
        assertEquals("CS101", loadedTask?.course)
        assertFalse(loadedTask?.isCompleted ?: true)
    }

    @Test
    fun `edit and save task in Room`() = runBlocking {
        val original = TaskEntity(
            title = "Draft Essay",
            course = "ENG101",
            deadlineEpochMillis = System.currentTimeMillis() + 50000L,
            isCompleted = false,
            notes = "First draft"
        )
        val id = repository.insertTask(original)
        val inserted = repository.getTaskById(id)!!

        val updated = inserted.copy(
            title = "Finalize Essay Submission",
            notes = "Include citations in APA format"
        )
        repository.updateTask(updated)

        val retrieved = repository.getTaskById(id)!!
        assertEquals("Finalize Essay Submission", retrieved.title)
        assertEquals("Include citations in APA format", retrieved.notes)
    }

    @Test
    fun `toggle completed updates status in Room`() = runBlocking {
        val testTask = TaskEntity(
            title = "Math Midterm",
            course = "MATH201",
            deadlineEpochMillis = System.currentTimeMillis() + 86400000L,
            isCompleted = false
        )

        val id = repository.insertTask(testTask)
        val inserted = repository.getTaskById(id)!!
        repository.toggleCompleted(inserted)

        val updated = repository.getTaskById(id)!!
        assertTrue(updated.isCompleted)
    }

    @Test
    fun `delete task removes from Room`() = runBlocking {
        val testTask = TaskEntity(
            title = "Temporary Task",
            deadlineEpochMillis = System.currentTimeMillis()
        )

        val id = repository.insertTask(testTask)
        val inserted = repository.getTaskById(id)!!
        repository.deleteTask(inserted)

        val all = repository.allTasks.first()
        assertTrue(all.none { it.id == id })
    }
}
