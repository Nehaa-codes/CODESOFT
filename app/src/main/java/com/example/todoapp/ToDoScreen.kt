package com.example.todoapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

val PurpleLight = Color(0xFF9C89CC)
val PurpleDark = Color(0xFF6650A4)
val PurpleBg = Color(0xFFF3EEFF)
val TextDark = Color(0xFF1A1A1A)

enum class SortMode { PRIORITY, TIME }

val congratsMessages = listOf(
    "🎉 Great job! Task done!",
    "✅ Nailed it! Keep going!",
    "🌟 Awesome work!",
    "👏 One more done, way to go!",
    "🔥 You're on fire today!",
    "💪 Crushing it!",
    "🎊 Task complete, well done!"
)

@Composable
fun HomeTabContent(
    tasks: List<TodoTask>,
    userName: String,
    onTaskChecked: (TodoTask, Boolean) -> Unit,
    onTaskEdit: (TodoTask) -> Unit,
    onTaskDelete: (TodoTask) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val quotes = remember {
        listOf(
            "You can do it! 💪",
            "Stay focused! 🎯",
            "One task at a time! ✨",
            "Keep going! 🚀",
            "You're doing great! ⭐"
        )
    }
    val randomQuote = remember { quotes.random() }
    var sortMode by remember { mutableStateOf(SortMode.PRIORITY) }
    var searchQuery by remember { mutableStateOf("") }

    // Home shows only incomplete tasks - completed tasks live in their own tab
    val incompleteTasks = tasks.filter { !it.isDone }

    val sortedTasks = remember(sortMode, incompleteTasks) {
        when (sortMode) {
            SortMode.PRIORITY -> incompleteTasks.sortedWith(
                compareByDescending<TodoTask> { it.priority }.thenBy { it.deadline }
            )
            SortMode.TIME -> incompleteTasks.sortedWith(
                compareBy<TodoTask> { it.deadline }.thenByDescending { it.priority }
            )
        }
    }

    // Filter by search query - case insensitive title match
    val filteredTasks = remember(sortedTasks, searchQuery) {
        if (searchQuery.isBlank()) sortedTasks
        else sortedTasks.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF6E9EA), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hello, ${userName.ifBlank { "there" }}! 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleDark
                )
                Text(
                    text = randomQuote,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            IconButton(onClick = {
                tasks.forEach { task ->
                    ReminderScheduler.scheduleAllReminders(context, task)
                }
                android.widget.Toast.makeText(
                    context, "Reminders re-synced", android.widget.Toast.LENGTH_SHORT
                ).show()
            }) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Re-sync reminders",
                    tint = PurpleDark,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color.Black, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskStatBox(
                label = "Total task",
                value = tasks.size,
                color = PurpleDark,
                modifier = Modifier.weight(1f)
            )
            TaskStatBox(
                label = "Remaining task",
                value = tasks.count { !it.isDone },
                color = Color(0xFFBA7517),
                modifier = Modifier.weight(1f)
            )
            TaskStatBox(
                label = "Completed task",
                value = tasks.count { it.isDone },
                color = Color(0xFF3B6D11),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tasks...", fontSize = 13.sp, color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PurpleDark,
                unfocusedBorderColor = PurpleLight,
                focusedTextColor = TextDark,
                unfocusedTextColor = TextDark,
                cursorColor = PurpleDark
            ),
            leadingIcon = {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.Search,
                    contentDescription = "Search",
                    tint = PurpleLight,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            SortDropdown(
                sortMode = sortMode,
                onSortModeChange = { sortMode = it }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (searchQuery.isNotBlank()) {
                        Text(text = "🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks match \"$searchQuery\"",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    } else {
                        Text(text = "📝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No tasks yet!\nTap + to add one",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items(filteredTasks, key = { it.id }) { task ->
                    SwipeToDeleteWrapper(onDelete = { onTaskDelete(task) }) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    sortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val label = when (sortMode) {
        SortMode.PRIORITY -> "Sort: Priority"
        SortMode.TIME -> "Sort: Time"
    }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(PurpleBg, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = PurpleDark,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Sort options",
                tint = PurpleDark,
                modifier = Modifier.size(18.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Sort by Priority") },
                onClick = {
                    onSortModeChange(SortMode.PRIORITY)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("Sort by Time") },
                onClick = {
                    onSortModeChange(SortMode.TIME)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun TaskStatBox(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = color
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteWrapper(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { it * 0.4f } // must swipe 40% across to trigger
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false, // only right-to-left swipe
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val color by androidx.compose.animation.animateColorAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                    Color(0xFFE53935) else Color.Transparent,
                label = "swipe background"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color, RoundedCornerShape(12.dp))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        content = { content() }
    )
}

@Composable
fun TaskCard(
    task: TodoTask,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = priorityToColor(task.priority)
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleBg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.size(32.dp),
                    colors = CheckboxDefaults.colors(
                        checkedColor = PurpleDark,
                        uncheckedColor = PurpleLight
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (task.isDone) Color.Gray else TextDark,
                        textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF6D6875).copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${task.category.emoji} ${task.category.displayName}",
                                color = Color(0xFF6D6875),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(priorityColor.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = priorityLabel(task.priority),
                                color = priorityColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(PurpleDark.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "📅 ${formatDeadline(task.deadline)}",
                                color = PurpleDark,
                                fontSize = 11.sp
                            )
                        }
                        if (task.repeatDaily) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF43A047).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "🔁 Daily",
                                    color = Color(0xFF43A047),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = PurpleDark
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFE53935)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Priority bar below the task info - visual indicator of priority level
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 8.dp)
                    .height(5.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (task.priority / 100f).coerceIn(0.04f, 1f))
                        .fillMaxHeight()
                        .background(priorityColor, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
fun ReminderToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = TextDark)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PurpleDark
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    existingTask: TodoTask?,
    onDismiss: () -> Unit,
    onSave: (String, Float, LocalDateTime, Boolean, Boolean, Boolean, Boolean, TaskCategory) -> Unit
) {
    val isEditing = existingTask != null
    var title by remember { mutableStateOf(existingTask?.title ?: "") }
    var priority by remember { mutableStateOf(existingTask?.priority ?: 50f) }
    var selectedDate by remember { mutableStateOf(existingTask?.deadline?.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(existingTask?.deadline?.toLocalTime()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var remindHourBefore by remember { mutableStateOf(existingTask?.remindHourBefore ?: true) }
    var remindAtDeadline by remember { mutableStateOf(existingTask?.remindAtDeadline ?: true) }
    var repeatDaily by remember { mutableStateOf(existingTask?.repeatDaily ?: false) }
    var category by remember { mutableStateOf(existingTask?.category ?: TaskCategory.OTHER) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Prevent the keyboard from auto-popping up when the dialog first opens.
    // We steal focus into an invisible box the instant the dialog appears,
    // so the title field never receives initial focus - the keyboard then
    // only shows up once the user actually taps a field themselves.
    val dialogFocusRequester = remember { FocusRequester() }
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) {
        dialogFocusRequester.requestFocus()
        kotlinx.coroutines.delay(100)
        keyboardController?.hide()
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = PurpleDark,
        focusedLabelColor = PurpleDark,
        focusedTextColor = TextDark,
        unfocusedTextColor = TextDark,
        disabledTextColor = TextDark,
        disabledBorderColor = PurpleLight,
        disabledLabelColor = PurpleDark,
        disabledPlaceholderColor = Color.Gray,
        cursorColor = PurpleDark
    )

    AlertDialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        containerColor = Color.White,
        title = {
            Text(
                text = if (isEditing) "Edit Task" else "Add New Task",
                fontWeight = FontWeight.Bold,
                color = PurpleDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Invisible focusable that absorbs initial focus, so the
                // title field below doesn't get auto-focused (which would
                // otherwise trigger the keyboard to pop up immediately).
                Box(
                    modifier = Modifier
                        .size(0.dp)
                        .focusable()
                        .focusRequester(dialogFocusRequester)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )

                // Category picker
                Box {
                    OutlinedTextField(
                        value = "${category.emoji} ${category.displayName}",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Category") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = PurpleDark)
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { categoryExpanded = true }
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        TaskCategory.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text("${option.emoji} ${option.displayName}") },
                                onClick = {
                                    category = option
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                // Priority slider: green -> red
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Priority", fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Medium)
                        Text(
                            priorityLabel(priority),
                            fontSize = 12.sp,
                            color = priorityToColor(priority),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Slider(
                        value = priority,
                        onValueChange = { priority = it },
                        valueRange = 0f..100f,
                        modifier = Modifier.height(28.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = priorityToColor(priority),
                            activeTrackColor = priorityToColor(priority)
                        )
                    )
                }

                // Date picker trigger
                Box {
                    OutlinedTextField(
                        value = selectedDate?.toString() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Date") },
                        placeholder = { Text("Tap to choose date") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                errorMessage = null
                                showDatePicker = true
                            }
                    )
                }

                // Time picker trigger
                Box {
                    OutlinedTextField(
                        value = selectedTime?.let { formatTime12h(it) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Time") },
                        placeholder = { Text("Tap to choose time") },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                errorMessage = null
                                showTimePicker = true
                            }
                    )
                }

                HorizontalDivider(color = PurpleBg, thickness = 1.dp)

                Text(
                    text = "Remind me",
                    fontSize = 12.sp,
                    color = TextDark,
                    fontWeight = FontWeight.Medium
                )

                ReminderToggleRow(
                    label = "1 hour before",
                    checked = remindHourBefore,
                    onCheckedChange = { remindHourBefore = it }
                )
                ReminderToggleRow(
                    label = "At the task end",
                    checked = remindAtDeadline,
                    onCheckedChange = { remindAtDeadline = it }
                )

                HorizontalDivider(color = PurpleBg, thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repeat daily",
                            fontSize = 13.sp,
                            color = TextDark,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Good for habits like taking medicine",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = repeatDaily,
                        onCheckedChange = { repeatDaily = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PurpleDark
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val date = selectedDate
                    val time = selectedTime
                    when {
                        title.isBlank() -> {
                            errorMessage = "Please enter a task title."
                        }
                        date == null || time == null -> {
                            errorMessage = "Please select both a date and time."
                        }
                        LocalDateTime.of(date, time).isBefore(LocalDateTime.now()) -> {
                            errorMessage = "Please choose a date and time in the future."
                        }
                        else -> {
                            errorMessage = null
                            keyboardController?.hide()
                            onSave(
                                title, priority, LocalDateTime.of(date, time),
                                false, remindHourBefore, remindAtDeadline, repeatDaily,
                                category
                            )
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PurpleDark)
            ) {
                Text(if (isEditing) "Save Changes" else "Add Task", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = {
                keyboardController?.hide()
                onDismiss()
            }) {
                Text("Cancel", color = PurpleDark)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis == null) {
                        showDatePicker = false
                        return@TextButton
                    }
                    val pickedDate = java.time.Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.of("UTC"))
                        .toLocalDate()
                    if (pickedDate.isBefore(LocalDate.now())) {
                        errorMessage = "Please choose a date that isn't in the past."
                    } else {
                        selectedDate = pickedDate
                        errorMessage = null
                        showDatePicker = false
                    }
                }) { Text("OK", color = PurpleDark) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = PurpleDark)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(is24Hour = false)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            containerColor = Color.White,
            title = { Text("Select Time", color = PurpleDark, fontWeight = FontWeight.Bold) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    val pickedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                    val dateForCheck = selectedDate
                    if (dateForCheck != null && LocalDateTime.of(dateForCheck, pickedTime).isBefore(LocalDateTime.now())) {
                        errorMessage = "Please choose a time that isn't in the past."
                    } else {
                        selectedTime = pickedTime
                        errorMessage = null
                        showTimePicker = false
                    }
                }) { Text("OK", color = PurpleDark) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel", color = PurpleDark)
                }
            }
        )
    }

    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            containerColor = Color.White,
            title = {
                Text("Invalid date/time", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
            },
            text = {
                Text(message, color = TextDark)
            },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text("OK", color = PurpleDark, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

private fun formatTime12h(time: LocalTime): String =
    time.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm a"))