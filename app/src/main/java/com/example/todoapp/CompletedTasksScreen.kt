package com.example.todoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CompletedTasksScreen(
    tasks: List<TodoTask>,
    onTaskChecked: (TodoTask, Boolean) -> Unit,
    onTaskDelete: (TodoTask) -> Unit
) {
    // Don't use remember(tasks) here - the list reference never changes
    // (it's a SnapshotStateList), so remember would never recompute when
    // items are checked/unchecked or deleted. Compute directly instead so
    // Compose recomputes it on every recomposition.
    val completedTasks = tasks.filter { it.isDone }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Completed Tasks",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${completedTasks.size} task${if (completedTasks.size == 1) "" else "s"} finished",
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = PurpleBg, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        if (completedTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🌱", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Nothing completed yet!\nFinish a task to see it here",
                        color = Color.LightGray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(completedTasks, key = { it.id }) { task ->
                    SwipeToDeleteWrapper(onDelete = { onTaskDelete(task) }) {
                        TaskCard(
                            task = task,
                            onCheckedChange = { checked -> onTaskChecked(task, checked) },
                            onEdit = { /* editing a completed task isn't needed here */ },
                            onDelete = { onTaskDelete(task) }
                        )
                    }
                }
            }
        }
    }
}