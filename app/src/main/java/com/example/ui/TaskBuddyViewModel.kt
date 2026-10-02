package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TaskFilter(val label: String) {
    ALL("All"),
    TODAY("Today"),
    PENDING("Pending"),
    COMPLETED("Completed")
}

enum class TaskSortOrder(val label: String) {
    DEADLINE("Deadline"),
    PRIORITY("Priority"),
    TITLE("Title (A-Z)")
}

data class TaskBuddyStats(
    val totalCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val overdueCount: Int = 0,
    val dueTodayCount: Int = 0,
    val urgentApproachingCount: Int = 0,
    val completionPercentage: Float = 0f
)

data class TaskBuddyUiState(
    val filteredTasks: List<TaskEntity> = emptyList(),
    val todayTasks: List<TaskEntity> = emptyList(),
    val stats: TaskBuddyStats = TaskBuddyStats(),
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val sortOrder: TaskSortOrder = TaskSortOrder.DEADLINE,
    val isAddEditSheetOpen: Boolean = false,
    val editingTask: TaskEntity? = null,
    val deleteConfirmationTask: TaskEntity? = null,
    val isWelcomeBannerVisible: Boolean = true,
    val themeMode: AppThemeMode = AppThemeMode.LIGHT
)

class TaskBuddyViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    private val _selectedCategory = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(TaskSortOrder.DEADLINE)
    private val _isAddEditSheetOpen = MutableStateFlow(false)
    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    private val _deleteConfirmationTask = MutableStateFlow<TaskEntity?>(null)
    private val _isWelcomeBannerVisible = MutableStateFlow(true)
    private val _themeMode = MutableStateFlow(AppThemeMode.LIGHT)

    init {
        viewModelScope.launch {
            repository.seedInitialTasksIfEmpty()
        }
    }

    val uiState: StateFlow<TaskBuddyUiState> = combine(
        repository.allTasks,
        _currentFilter,
        _selectedCategory,
        _searchQuery,
        _sortOrder,
        _isAddEditSheetOpen,
        _editingTask,
        _deleteConfirmationTask,
        _isWelcomeBannerVisible,
        _themeMode
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val tasks = params[0] as List<TaskEntity>
        val filter = params[1] as TaskFilter
        val category = params[2] as String?
        val query = params[3] as String
        val sort = params[4] as TaskSortOrder
        val isSheetOpen = params[5] as Boolean
        val editingTask = params[6] as TaskEntity?
        val deleteConfirmTask = params[7] as TaskEntity?
        val isWelcomeVisible = params[8] as Boolean
        val currentThemeMode = params[9] as AppThemeMode

        // Calculate stats on all tasks
        val total = tasks.size
        val completed = tasks.count { it.isCompleted }
        val pending = total - completed
        val overdue = tasks.count {
            !it.isCompleted && it.getDeadlineUrgency() == TaskEntity.DeadlineUrgency.OVERDUE
        }
        val dueToday = tasks.count {
            !it.isCompleted && (it.isDueToday() || it.getDeadlineUrgency() == TaskEntity.DeadlineUrgency.DUE_TODAY)
        }
        val urgentApproaching = tasks.count { it.isDeadlineApproaching() }
        val completionRate = if (total > 0) (completed.toFloat() / total.toFloat()) else 0f
        val stats = TaskBuddyStats(
            totalCount = total,
            completedCount = completed,
            pendingCount = pending,
            overdueCount = overdue,
            dueTodayCount = dueToday,
            urgentApproachingCount = urgentApproaching,
            completionPercentage = completionRate
        )

        // Today's tasks (due today or overdue tasks needing immediate focus)
        val todayTaskList = tasks.filter { task ->
            task.isDueToday() || task.getDeadlineUrgency() == TaskEntity.DeadlineUrgency.DUE_TODAY ||
                    (!task.isCompleted && task.getDeadlineUrgency() == TaskEntity.DeadlineUrgency.OVERDUE)
        }.sortedWith(
            compareBy<TaskEntity> { it.isCompleted }
                .thenBy { it.deadlineEpochMillis }
        )

        // Filter tasks
        var filtered = tasks.filter { task ->
            val matchesFilter = when (filter) {
                TaskFilter.ALL -> true
                TaskFilter.TODAY -> task.isDueToday() || task.getDeadlineUrgency() == TaskEntity.DeadlineUrgency.DUE_TODAY ||
                        (!task.isCompleted && task.getDeadlineUrgency() == TaskEntity.DeadlineUrgency.OVERDUE)
                TaskFilter.PENDING -> !task.isCompleted
                TaskFilter.COMPLETED -> task.isCompleted
            }

            val matchesCategory = category == null || task.category.equals(category, ignoreCase = true)

            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.course.contains(query, ignoreCase = true) ||
                    task.notes.contains(query, ignoreCase = true)

            matchesFilter && matchesCategory && matchesQuery
        }

        // Sort tasks
        filtered = when (sort) {
            TaskSortOrder.DEADLINE -> filtered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.deadlineEpochMillis }
            )
            TaskSortOrder.PRIORITY -> filtered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy {
                        when (TaskEntity.Priority.fromString(it.priority)) {
                            TaskEntity.Priority.HIGH -> 0
                            TaskEntity.Priority.MEDIUM -> 1
                            TaskEntity.Priority.LOW -> 2
                        }
                    }
                    .thenBy { it.deadlineEpochMillis }
            )
            TaskSortOrder.TITLE -> filtered.sortedWith(
                compareBy<TaskEntity> { it.isCompleted }
                    .thenBy { it.title.lowercase() }
            )
        }

        TaskBuddyUiState(
            filteredTasks = filtered,
            todayTasks = todayTaskList,
            stats = stats,
            currentFilter = filter,
            selectedCategory = category,
            searchQuery = query,
            sortOrder = sort,
            isAddEditSheetOpen = isSheetOpen,
            editingTask = editingTask,
            deleteConfirmationTask = deleteConfirmTask,
            isWelcomeBannerVisible = isWelcomeVisible,
            themeMode = currentThemeMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskBuddyUiState()
    )

    fun toggleThemeMode() {
        _themeMode.value = when (_themeMode.value) {
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
    }

    fun dismissWelcomeBanner() {
        _isWelcomeBannerVisible.value = false
    }

    fun showWelcomeBanner() {
        _isWelcomeBannerVisible.value = true
    }

    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortOrder(order: TaskSortOrder) {
        _sortOrder.value = order
    }

    fun openAddTask() {
        _editingTask.value = null
        _isAddEditSheetOpen.value = true
    }

    fun openEditTask(task: TaskEntity) {
        _editingTask.value = task
        _isAddEditSheetOpen.value = true
    }

    fun closeAddEditSheet() {
        _isAddEditSheetOpen.value = false
        _editingTask.value = null
    }

    fun saveTask(
        title: String,
        course: String,
        deadlineEpochMillis: Long,
        priority: TaskEntity.Priority,
        category: TaskEntity.Category,
        notes: String
    ) {
        val currentEdit = _editingTask.value
        viewModelScope.launch {
            if (currentEdit != null) {
                repository.updateTask(
                    currentEdit.copy(
                        title = title.trim(),
                        course = course.trim(),
                        deadlineEpochMillis = deadlineEpochMillis,
                        priority = priority.name,
                        category = category.name,
                        notes = notes.trim()
                    )
                )
            } else {
                repository.insertTask(
                    TaskEntity(
                        title = title.trim(),
                        course = course.trim(),
                        deadlineEpochMillis = deadlineEpochMillis,
                        priority = priority.name,
                        category = category.name,
                        notes = notes.trim()
                    )
                )
            }
            closeAddEditSheet()
        }
    }

    fun toggleTaskCompleted(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleCompleted(task)
        }
    }

    fun promptDeleteTask(task: TaskEntity) {
        _deleteConfirmationTask.value = task
    }

    fun dismissDeleteDialog() {
        _deleteConfirmationTask.value = null
    }

    fun confirmDeleteTask() {
        val task = _deleteConfirmationTask.value ?: return
        viewModelScope.launch {
            repository.deleteTask(task)
            _deleteConfirmationTask.value = null
        }
    }

    fun deleteTaskDirect(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}

class TaskBuddyViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskBuddyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TaskBuddyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
