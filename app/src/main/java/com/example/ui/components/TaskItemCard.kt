package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskEntity
import com.example.ui.theme.CategoryAssignment
import com.example.ui.theme.CategoryExam
import com.example.ui.theme.CategoryOther
import com.example.ui.theme.CategoryProject
import com.example.ui.theme.CategoryReading
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusDueToday
import com.example.ui.theme.StatusOverdue

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleCompleted: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val urgency = task.getDeadlineUrgency()
    val isDone = task.isCompleted
    val isUrgent = task.isDeadlineApproaching()
    val warningText = task.getApproachingWarningText()

    val urgencyColor = when (urgency) {
        TaskEntity.DeadlineUrgency.COMPLETED -> StatusCompleted
        TaskEntity.DeadlineUrgency.OVERDUE -> StatusOverdue
        TaskEntity.DeadlineUrgency.DUE_TODAY -> StatusDueToday
        TaskEntity.DeadlineUrgency.DUE_TOMORROW -> MaterialTheme.colorScheme.primary
        TaskEntity.DeadlineUrgency.UPCOMING -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val priorityColor = when (TaskEntity.Priority.fromString(task.priority)) {
        TaskEntity.Priority.HIGH -> PriorityHigh
        TaskEntity.Priority.MEDIUM -> PriorityMedium
        TaskEntity.Priority.LOW -> PriorityLow
    }

    val cardBg by animateColorAsState(
        targetValue = if (isDone) {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        label = "cardBg"
    )

    // Border highlight if approaching deadline
    val borderStroke = when {
        isDone -> null
        urgency == TaskEntity.DeadlineUrgency.OVERDUE -> BorderStroke(1.5.dp, StatusOverdue.copy(alpha = 0.65f))
        isUrgent -> BorderStroke(1.5.dp, StatusDueToday.copy(alpha = 0.6f))
        else -> null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}")
            .clickable { onToggleCompleted() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left priority stripe
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(if (isDone) StatusCompleted.copy(alpha = 0.5f) else priorityColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                // Visual Warning Banner for approaching deadlines
                if (!isDone && warningText != null) {
                    val isOverdue = urgency == TaskEntity.DeadlineUrgency.OVERDUE
                    val bannerBg = if (isOverdue) StatusOverdue.copy(alpha = 0.12f) else StatusDueToday.copy(alpha = 0.12f)
                    val bannerColor = if (isOverdue) StatusOverdue else StatusDueToday

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bannerBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("approaching_deadline_warning_${task.id}")
                    ) {
                        Icon(
                            imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = warningText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = bannerColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Checkbox
                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { onToggleCompleted() },
                        modifier = Modifier
                            .testTag("task_checkbox_${task.id}")
                            .padding(top = 2.dp),
                        colors = CheckboxDefaults.colors(
                            checkedColor = StatusCompleted,
                            uncheckedColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        // Badges Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (task.course.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 7.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = task.course,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }

                            CategoryBadge(categoryStr = task.category)
                            PriorityBadge(priorityStr = task.priority)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Title
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            else MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (isDone) TextDecoration.LineThrough else null,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Notes snippet if available
                        if (task.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Deadline chip & action buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(urgencyColor.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = urgencyColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = task.getFormattedDeadline(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = urgencyColor
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = onEdit,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("edit_task_${task.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit task",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                IconButton(
                                    onClick = onDelete,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("delete_task_${task.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = "Delete task",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(categoryStr: String) {
    val category = TaskEntity.Category.fromString(categoryStr)
    val (color, icon) = when (category) {
        TaskEntity.Category.ASSIGNMENT -> CategoryAssignment to Icons.Default.Assignment
        TaskEntity.Category.EXAM -> CategoryExam to Icons.Default.School
        TaskEntity.Category.PROJECT -> CategoryProject to Icons.Default.Work
        TaskEntity.Category.READING -> CategoryReading to Icons.Default.MenuBook
        TaskEntity.Category.OTHER -> CategoryOther to Icons.Default.MoreHoriz
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = category.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PriorityBadge(priorityStr: String) {
    val priority = TaskEntity.Priority.fromString(priorityStr)
    val color = when (priority) {
        TaskEntity.Priority.HIGH -> PriorityHigh
        TaskEntity.Priority.MEDIUM -> PriorityMedium
        TaskEntity.Priority.LOW -> PriorityLow
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = priority.label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}
