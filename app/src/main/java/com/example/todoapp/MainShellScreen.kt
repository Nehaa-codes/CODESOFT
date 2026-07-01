package com.example.todoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

enum class MainTab { HOME, COMPLETED, CATEGORIES, PROFILE }

/**
 * Owns the bottom navigation bar, the floating Add Task button, and the
 * shared task list used across the Home, Completed, and Categories tabs.
 * Loading/saving happens once here rather than separately per tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen() {
    val context = LocalContext.current
    val tasks = remember { mutableStateListOf<TodoTask>() }
    var showDialog by remember { mutableStateOf(false) }
    var taskBeingEdited by remember { mutableStateOf<TodoTask?>(null) }
    var taskCounter by remember { mutableStateOf(0) }
    var isLoaded by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    // Load saved tasks + name once, when the shell first appears
    LaunchedEffect(Unit) {
        val saved = TaskRepository.loadTasks(context)
        tasks.clear()
        tasks.addAll(saved)
        taskCounter = TaskRepository.nextTaskId(context)
        userName = TaskRepository.getUserName(context)
        isLoaded = true

        saved.forEach { task ->
            ReminderScheduler.scheduleAllReminders(context, task)
        }
    }

    // Save tasks to disk whenever the list changes (after initial load completes)
    LaunchedEffect(
        isLoaded,
        tasks.map {
            "${it.id}-${it.title}-${it.priority}-${it.deadline}-${it.isDone}-" +
                    "${it.notifiedDayBefore}-${it.notifiedHourBefore}-${it.remindDayBefore}-" +
                    "${it.remindHourBefore}-${it.remindAtDeadline}-${it.repeatDaily}-${it.category}"
        }
    ) {
        if (isLoaded) {
            TaskRepository.saveTasks(context, tasks.toList())
        }
    }

    fun onTaskChecked(task: TodoTask, checked: Boolean) {
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index != -1) {
            val updated = tasks[index].copy(isDone = checked)
            tasks[index] = updated
            if (checked) {
                ReminderScheduler.cancelAllReminders(context, updated.id)
                val message = congratsMessages.random()
                android.widget.Toast.makeText(
                    context, message, android.widget.Toast.LENGTH_SHORT
                ).show()
            } else {
                ReminderScheduler.scheduleAllReminders(context, updated)
            }
        }
    }

    fun onTaskEdit(task: TodoTask) {
        taskBeingEdited = task
        showDialog = true
    }

    fun onTaskDelete(task: TodoTask) {
        ReminderScheduler.cancelAllReminders(context, task.id)
        tasks.removeAll { it.id == task.id }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurpleDark,
                        selectedTextColor = PurpleDark,
                        indicatorColor = PurpleBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.COMPLETED,
                    onClick = { selectedTab = MainTab.COMPLETED },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Completed") },
                    label = { Text("Completed") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurpleDark,
                        selectedTextColor = PurpleDark,
                        indicatorColor = PurpleBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.CATEGORIES,
                    onClick = { selectedTab = MainTab.CATEGORIES },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Categories") },
                    label = { Text("Categories") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurpleDark,
                        selectedTextColor = PurpleDark,
                        indicatorColor = PurpleBg
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.PROFILE,
                    onClick = { selectedTab = MainTab.PROFILE },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PurpleDark,
                        selectedTextColor = PurpleDark,
                        indicatorColor = PurpleBg
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == MainTab.HOME) {
                FloatingActionButton(
                    onClick = {
                        taskBeingEdited = null
                        showDialog = true
                    },
                    containerColor = PurpleDark,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Task",
                        tint = Color.White
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeTabContent(
                    tasks = tasks,
                    userName = userName,
                    onTaskChecked = ::onTaskChecked,
                    onTaskEdit = ::onTaskEdit,
                    onTaskDelete = ::onTaskDelete
                )
                MainTab.COMPLETED -> CompletedTasksScreen(
                    tasks = tasks,
                    onTaskChecked = ::onTaskChecked,
                    onTaskDelete = ::onTaskDelete
                )
                MainTab.CATEGORIES -> CategoriesScreen(
                    tasks = tasks,
                    onTaskChecked = ::onTaskChecked,
                    onTaskEdit = ::onTaskEdit,
                    onTaskDelete = ::onTaskDelete
                )
                MainTab.PROFILE -> ProfileScreen(
                    onNameChanged = { newName -> userName = newName }
                )
            }
        }
    }

    if (showDialog) {
        TaskDialog(
            existingTask = taskBeingEdited,
            onDismiss = {
                showDialog = false
                taskBeingEdited = null
            },
            onSave = { title, priority, deadline, remindDay, remindHour, remindAt, repeatDaily, category ->
                val editing = taskBeingEdited
                val savedTask: TodoTask
                if (editing != null) {
                    val index = tasks.indexOfFirst { it.id == editing.id }
                    savedTask = tasks.getOrElse(index) { editing }.copy(
                        title = title,
                        priority = priority,
                        deadline = deadline,
                        remindDayBefore = remindDay,
                        remindHourBefore = remindHour,
                        remindAtDeadline = remindAt,
                        repeatDaily = repeatDaily,
                        category = category,
                        notifiedDayBefore = false,
                        notifiedHourBefore = false,
                        notifiedAtDeadline = false
                    )
                    if (index != -1) tasks[index] = savedTask
                } else {
                    savedTask = TodoTask(
                        id = taskCounter++,
                        title = title,
                        priority = priority,
                        deadline = deadline,
                        remindDayBefore = remindDay,
                        remindHourBefore = remindHour,
                        remindAtDeadline = remindAt,
                        repeatDaily = repeatDaily,
                        category = category
                    )
                    tasks.add(savedTask)
                }
                ReminderScheduler.scheduleAllReminders(context, savedTask)
                showDialog = false
                taskBeingEdited = null
            }
        )
    }
}