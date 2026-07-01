package com.example.todoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoriesScreen(
    tasks: List<TodoTask>,
    onTaskChecked: (TodoTask, Boolean) -> Unit,
    onTaskEdit: (TodoTask) -> Unit,
    onTaskDelete: (TodoTask) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<TaskCategory?>(null) }

    val filteredTasks = remember(tasks, selectedCategory) {
        val base = if (selectedCategory == null) tasks
        else tasks.filter { it.category == selectedCategory }

        // Completed tasks sink to the bottom, within each category view
        base.sortedBy { it.isDone }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Categories",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDark
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                CategoryChip(
                    label = "All",
                    emoji = "📋",
                    count = tasks.size,
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
            }
            items(TaskCategory.values()) { cat ->
                CategoryChip(
                    label = cat.displayName,
                    emoji = cat.emoji,
                    count = tasks.count { it.category == cat },
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = PurpleBg, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🗂️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No tasks in this category",
                        color = Color.LightGray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onCheckedChange = { checked -> onTaskChecked(task, checked) },
                        onEdit = { onTaskEdit(task) },
                        onDelete = { onTaskDelete(task) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    emoji: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                if (selected) PurpleDark else PurpleBg,
                RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label ($count)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) Color.White else PurpleDark
        )
    }
}