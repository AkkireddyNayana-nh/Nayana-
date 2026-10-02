package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditTaskSheet
import com.example.ui.components.EmptyTasksView
import com.example.ui.components.StatsOverviewCard
import com.example.ui.components.TaskItemCard
import com.example.ui.components.TodaysTasksSection
import com.example.ui.components.WelcomeBanner
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskBuddyScreen(
    viewModel: TaskBuddyViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "TaskBuddy",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            Text(
                                text = "College Task & Deadline Manager",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Dark / Light Mode Toggle Button
                    IconButton(
                        onClick = { viewModel.toggleThemeMode() },
                        modifier = Modifier.testTag("theme_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (uiState.themeMode == AppThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (uiState.themeMode == AppThemeMode.DARK) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Sort Tasks Button & Menu
                    Box {
                        IconButton(
                            onClick = { sortMenuExpanded = true },
                            modifier = Modifier.testTag("sort_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = "Sort tasks",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        DropdownMenu(
                            expanded = sortMenuExpanded,
                            onDismissRequest = { sortMenuExpanded = false }
                        ) {
                            TaskSortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = order.label,
                                            fontWeight = if (uiState.sortOrder == order) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        viewModel.setSortOrder(order)
                                        sortMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddTask() },
                modifier = Modifier.testTag("add_task_fab"),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add new task",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Welcome Message for First-Time Users
            item(key = "welcome_banner") {
                AnimatedVisibility(
                    visible = uiState.isWelcomeBannerVisible,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    WelcomeBanner(
                        onDismiss = { viewModel.dismissWelcomeBanner() }
                    )
                }
            }

            // 2. Study Progress Overview Card with Approaching Warning Pill
            item(key = "stats_card") {
                StatsOverviewCard(stats = uiState.stats)
            }

            // 3. Today's Tasks Section (prominently displayed in default view)
            if (uiState.currentFilter == TaskFilter.ALL && uiState.searchQuery.isBlank() && uiState.selectedCategory == null) {
                item(key = "today_tasks_highlight_section") {
                    TodaysTasksSection(
                        todayTasks = uiState.todayTasks,
                        onToggleCompleted = { viewModel.toggleTaskCompleted(it) },
                        onEdit = { viewModel.openEditTask(it) },
                        onDelete = { viewModel.promptDeleteTask(it) },
                        onAddTodayTask = { viewModel.openAddTask() }
                    )
                }
            }

            // 4. Search Bar
            item(key = "search_bar") {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_tasks_input"),
                    placeholder = { Text("Search by title, course, or notes...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            // 5. Status Filters (All, Today, Pending, Done)
            item(key = "status_filters") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatusFilterTab(
                        label = "All (${uiState.stats.totalCount})",
                        selected = uiState.currentFilter == TaskFilter.ALL,
                        onClick = { viewModel.setFilter(TaskFilter.ALL) },
                        modifier = Modifier.weight(1f),
                        testTag = "filter_all"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusFilterTab(
                        label = "Today (${uiState.stats.dueTodayCount})",
                        selected = uiState.currentFilter == TaskFilter.TODAY,
                        onClick = { viewModel.setFilter(TaskFilter.TODAY) },
                        modifier = Modifier.weight(1f),
                        testTag = "filter_today"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusFilterTab(
                        label = "Pending (${uiState.stats.pendingCount})",
                        selected = uiState.currentFilter == TaskFilter.PENDING,
                        onClick = { viewModel.setFilter(TaskFilter.PENDING) },
                        modifier = Modifier.weight(1f),
                        testTag = "filter_pending"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    StatusFilterTab(
                        label = "Done (${uiState.stats.completedCount})",
                        selected = uiState.currentFilter == TaskFilter.COMPLETED,
                        onClick = { viewModel.setFilter(TaskFilter.COMPLETED) },
                        modifier = Modifier.weight(1f),
                        testTag = "filter_completed"
                    )
                }
            }

            // 6. Course Category Chips horizontal scroll
            item(key = "category_chips") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.selectedCategory == null,
                        onClick = { viewModel.setSelectedCategory(null) },
                        label = { Text("All Categories") },
                        modifier = Modifier.testTag("category_all_chip")
                    )

                    com.example.data.TaskEntity.Category.entries.forEach { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat.name,
                            onClick = {
                                if (uiState.selectedCategory == cat.name) {
                                    viewModel.setSelectedCategory(null)
                                } else {
                                    viewModel.setSelectedCategory(cat.name)
                                }
                            },
                            label = { Text(cat.label) },
                            modifier = Modifier.testTag("category_filter_${cat.name}")
                        )
                    }
                }
            }

            // Section label for the list
            item(key = "list_header_label") {
                val headerText = when {
                    uiState.searchQuery.isNotBlank() -> "Search Results (${uiState.filteredTasks.size})"
                    uiState.currentFilter == TaskFilter.TODAY -> "Today's Schedule (${uiState.filteredTasks.size})"
                    uiState.currentFilter == TaskFilter.PENDING -> "Pending Tasks (${uiState.filteredTasks.size})"
                    uiState.currentFilter == TaskFilter.COMPLETED -> "Completed Tasks (${uiState.filteredTasks.size})"
                    uiState.selectedCategory != null -> "${uiState.selectedCategory} Tasks (${uiState.filteredTasks.size})"
                    else -> "All Course Tasks (${uiState.filteredTasks.size})"
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // 7. Task List or Empty State
            if (uiState.filteredTasks.isEmpty()) {
                item(key = "empty_view") {
                    EmptyTasksView(
                        onAddTask = { viewModel.openAddTask() },
                        isFilterActive = uiState.searchQuery.isNotBlank() ||
                                uiState.selectedCategory != null ||
                                uiState.currentFilter != TaskFilter.ALL
                    )
                }
            } else {
                items(
                    items = uiState.filteredTasks,
                    key = { it.id }
                ) { task ->
                    TaskItemCard(
                        task = task,
                        onToggleCompleted = { viewModel.toggleTaskCompleted(task) },
                        onEdit = { viewModel.openEditTask(task) },
                        onDelete = { viewModel.promptDeleteTask(task) }
                    )
                }
            }
        }
    }

    // Add / Edit Modal Bottom Sheet
    if (uiState.isAddEditSheetOpen) {
        AddEditTaskSheet(
            task = uiState.editingTask,
            onDismiss = { viewModel.closeAddEditSheet() },
            onSave = { title, course, deadlineMillis, priority, category, notes ->
                viewModel.saveTask(
                    title = title,
                    course = course,
                    deadlineEpochMillis = deadlineMillis,
                    priority = priority,
                    category = category,
                    notes = notes
                )
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = if (uiState.editingTask != null) "Task updated" else "Task added to TaskBuddy"
                    )
                }
            }
        )
    }

    // Delete Confirmation Dialog
    uiState.deleteConfirmationTask?.let { taskToDelete ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = {
                Text(
                    text = "Delete Task?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${taskToDelete.title}\"? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deletedTitle = taskToDelete.title
                        viewModel.confirmDeleteTask()
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Deleted \"$deletedTitle\"")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.dismissDeleteDialog() },
                    modifier = Modifier.testTag("cancel_delete_button")
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StatusFilterTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.surface
                else Color.Transparent
            )
            .testTag(testTag)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
