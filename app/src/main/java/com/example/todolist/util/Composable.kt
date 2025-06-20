package com.example.todolist.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import com.example.todolist.ToDoViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenView(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    toDoViewModel: ToDoViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var currentView by remember { mutableStateOf("main") }
    val categories = listOf("Wszystkie", "Praca", "Spotkanie")
    var editId by remember { mutableStateOf(0) }

    var lastView by remember { mutableStateOf("") }

    val onAddClick: (Int, Int, Int, Int, Int, Int, String, String, String, Boolean, Boolean, Boolean, List<Uri>) -> Unit = {
        id, day, month, year, hour, minute, title, description, category, notification, finished, files, attachments ->
        toDoViewModel.addReminder(id, day, month, year, hour, minute, title, description, category, notification, finished, files, attachments)
        currentView = "main"
    }

    val onEditClick: (Int, Int, Int, Int, Int, Int, String, String, String, Boolean, Boolean, Boolean, List<Uri>) -> Unit = {
            id, day, month, year, hour, minute, title, description, category, notification, finished, files, attachments ->
        toDoViewModel.editReminder(id, day, month, year, hour, minute, title, description, category, notification, finished, files, attachments)
        currentView = "main"
    }

    val onDeleteClick: suspend (Int) -> Unit = {
        toDoViewModel.removeReminder(it)
        delay(500)
        currentView = "main"
    }

    val onViewChange: (String) -> Unit = {
        lastView = currentView
        currentView = it
    }

    val onReminderClick: (Int) -> Unit = {
        lastView = currentView
        currentView = "edit"
        editId = it
    }

    val reminders by toDoViewModel.reminders.collectAsState()
    val category by toDoViewModel.category.collectAsState()
    val notificationTime by toDoViewModel.notificationTime.collectAsState()
    val hideCompleted by toDoViewModel.hideFinished.collectAsState()
    val search by toDoViewModel.search.collectAsState()

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    BackHandler {
        if (lastView.length > 0 && currentView != "main" && currentView != lastView) {
            currentView = lastView
        } else {
            currentView = "main"
        }
    }

    toDoViewModel.getReminders()

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.15f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column (
                    modifier = Modifier.weight(0.85f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if(currentView == "main") {
                        OutlinedTextField(
                            value = search,
                            onValueChange = {
                                toDoViewModel.setSearch(it)
                            },
                            label = { Text("Wyszukaj") },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else if (currentView == "add") {
                        Text(
                            text = "Dodaj wydarzenie",
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else if (currentView == "settings") {
                        Text(
                            text = "Ustawienia",
                            style = MaterialTheme.typography.titleLarge
                        )
                    } else if (currentView == "edit") {
                        Text(
                            text = "Edytuj",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }

                Column (
                    modifier = Modifier.weight(0.15f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = {
                        if (currentView != "settings"){
                            onViewChange("settings")
                        }
                    }
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            }

            if (currentView == "main"){
                ToDoList(modifier = Modifier.weight(0.85f), reminders, onViewChange, onReminderClick)
            } else if (currentView == "add"){
                AddReminder(modifier = Modifier.weight(0.85f), toDoViewModel, onAddClick, categories.subList(1, categories.size))
            } else if (currentView == "settings") {
                SettingsScreen(
                    modifier = Modifier.weight(0.85f),
                    categories = categories,
                    selectedCategory = category,
                    onCategorySelected = { toDoViewModel.setCategory(it) },
                    notificationMinutes = notificationTime,
                    onNotificationMinutesChange = { toDoViewModel.setNotificationTime(it) },
                    hideCompleted = hideCompleted,
                    onHideCompletedChange = { toDoViewModel.setHideFinished(it) }
                    )
            } else if (currentView == "edit") {
                val reminder = reminders.first { it.id == editId }
                EditReminder(modifier = Modifier.weight(0.85f), toDoViewModel, onEditClick, categories.subList(1, categories.size), reminder, onDeleteClick)
            }
        }
    }
}

@Composable
fun ToDoList(
    modifier: Modifier = Modifier,
    reminders: List<Reminder>,
    onViewChange: (String) -> Unit,
    onCardClick: (Int) -> Unit
) {
    Row (
        modifier = modifier.fillMaxWidth()
    ) {
        Column (
            modifier = Modifier
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reminders) {
                    ReminderCard(it, onCardClick = onCardClick)
                }
            }

            FloatingActionButton(
                onClick = { onViewChange("add") },
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "Dodaj")
            }
        }
    }
}

@Composable
fun ReminderCard(
    reminder: Reminder,
    onCardClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
            .clickable { onCardClick(reminder.id) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val month = if (reminder.month < 10) "0${reminder.month}" else "${reminder.month}"
                Text(
                    text = "${reminder.day}.${month}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "%02d:%02d".format(reminder.hour, reminder.minute),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Dzwonek", tint = if (reminder.notification) Color.Green else Color.Gray )
                    Icon(imageVector = Icons.Default.Folder, contentDescription = "Plik", tint = if (reminder.files) Color.Green else Color.Gray)
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Zrobione", tint = if (reminder.finished) Color.Green else Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReminder(
    modifier: Modifier = Modifier,
    toDoViewModel: ToDoViewModel,
    onAddClick: (Int, Int, Int, Int, Int, Int, String, String, String, Boolean, Boolean, Boolean, List<Uri>) -> Unit,
    categories: List<String>
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var attachments by remember { mutableStateOf<MutableList<Uri>>(mutableListOf<Uri>()) }
    var category by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    val multipleAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachments = (attachments + uris).toMutableList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = {
                                    title = it
                                    titleError = false
                                },
                label = { Text("Tytuł") },
                isError = titleError,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (titleError) Text("Pole wymagane", color = MaterialTheme.colorScheme.error)
                }

            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
        }

        item {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DatePickerField(
                    modifier = Modifier.weight(0.4f),
                    selectedDate = date,
                    onDateSelected = {
                        date = it
                        dateError = false
                    },
                    isError = dateError
                )
                TimePickerField(
                    modifier = Modifier.weight(0.4f),
                    selectedTime = time,
                    onTimeSelected = {
                        time = it
                        timeError = false
                    },
                    isError = timeError
                )
            }
        }

        item {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = { Text(categoryItem) },
                                onClick = {
                                    category = categoryItem
                                    categoryError = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (categoryError) {
                Text(
                    text = "Pole wymagane",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Włącz powiadomienia")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        }

        item {
            Button(
                onClick = {
                    multipleAttachmentLauncher.launch("*/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj załączniki")
            }
        }

        if (attachments.isNotEmpty()) {
            item {
                Text("Załączniki:", style = MaterialTheme.typography.titleMedium)
            }

            items(attachments.size) { index ->
                val uri = attachments[index]
                val fileName = remember(uri) { toDoViewModel.getFileNameFromUri(uri) ?: "Nieznany plik" }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayFileName = fileName.substring(0, 29) + (if (fileName.length > 29) "..." else "")
                    Text(
                        text = "${index + 1}. $displayFileName",
                    )
                    IconButton(
                        onClick = {
                            attachments = attachments.toMutableList().apply {
                                removeAt(index)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Zamknij"
                        )
                    }
                }
            }
        }

        item {
            Button(
                onClick = {
                    var hasError = false

                    if (title.isBlank()) {
                        titleError = true
                        hasError = true
                    }

                    if (date.isBlank()) {
                        dateError = true
                        hasError = true
                    }

                    if (time.isBlank()) {
                        timeError = true
                        hasError = true
                    }

                    if (category.isBlank()) {
                        categoryError = true
                        hasError = true
                    }

                    if (!hasError) {
                        val dateSplit = date.split("-")
                        val timeSplit = time.split(":")
                        onAddClick(
                            0,
                            dateSplit[2].toInt(),
                            dateSplit[1].toInt(),
                            dateSplit[0].toInt(),
                            timeSplit[0].toInt(),
                            timeSplit[1].toInt(),
                            title,
                            description,
                            category,
                            notificationsEnabled,
                            false,
                            false,
                            attachments
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj wydarzenie")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReminder(
    modifier: Modifier = Modifier,
    toDoViewModel: ToDoViewModel,
    onAddClick: (Int, Int, Int, Int, Int, Int, String, String, String, Boolean, Boolean, Boolean, List<Uri>) -> Unit,
    categories: List<String>,
    reminder: Reminder,
    onDeleteClick: suspend (Int) -> Unit
) {
    val month10 = if (reminder.month < 10) "0" else ""
    val day10 = if (reminder.day < 10) "0" else ""
    val hour10 = if (reminder.hour < 10) "0" else ""
    val minute10 = if (reminder.minute < 10) "0" else ""
    var title by remember { mutableStateOf(reminder.title) }
    var description by remember { mutableStateOf(reminder.description) }
    var date by remember { mutableStateOf(reminder.year.toString() + "-" + month10 + reminder.month.toString() + "-" + day10 + reminder.day.toString()) }
    var time by remember { mutableStateOf(hour10 + reminder.hour.toString() + ":" + minute10 + reminder.minute.toString()) }
    var notificationsEnabled by remember { mutableStateOf(reminder.notification) }
    var attachments by remember { mutableStateOf<MutableList<Uri>>(reminder.attachments.toMutableList()) }
    var category by remember { mutableStateOf(reminder.category) }
    var finished by remember { mutableStateOf(reminder.finished) }
    var expanded by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    val multipleAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachments = (attachments + uris).toMutableList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                label = { Text("Tytuł") },
                isError = titleError,
                modifier = Modifier.fillMaxWidth(),
                supportingText = {
                    if (titleError) Text("Pole wymagane", color = MaterialTheme.colorScheme.error)
                }

            )
        }

        item {
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Opis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
        }

        item {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                DatePickerField(
                    modifier = Modifier.weight(0.4f),
                    selectedDate = date,
                    onDateSelected = {
                        date = it
                        dateError = false
                    },
                    isError = dateError
                )
                TimePickerField(
                    modifier = Modifier.weight(0.4f),
                    selectedTime = time,
                    onTimeSelected = {
                        time = it
                        timeError = false
                    },
                    isError = timeError
                )
            }
        }

        item {
            Row (
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ExposedDropdownMenuBox(
                    modifier = Modifier.fillMaxWidth(),
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Kategoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { categoryItem ->
                            DropdownMenuItem(
                                text = { Text(categoryItem) },
                                onClick = {
                                    category = categoryItem
                                    categoryError = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            if (categoryError) {
                Text(
                    text = "Pole wymagane",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Włącz powiadomienia")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
        }

        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Oznacz jako zakończone")
                Switch(
                    checked = finished,
                    onCheckedChange = { finished = it }
                )
            }
        }

        item {
            Button(
                onClick = {
                    multipleAttachmentLauncher.launch("*/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dodaj załączniki")
            }
        }

        if (attachments.isNotEmpty()) {
            item {
                Text("Załączniki:", style = MaterialTheme.typography.titleMedium)
            }

            items(attachments.size) { index ->
                val uri = attachments[index]
                val fileName = remember(uri) { toDoViewModel.getFileNameFromUri(uri) ?: "Nieznany plik" }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayFileName = if (fileName.length > 29) fileName.substring(0, 29) + "..." else fileName
                    Text(
                        text = "${index + 1}. $displayFileName",
                    )
                    IconButton(
                        onClick = {
                            attachments = attachments.toMutableList().apply {
                                removeAt(index)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Zamknij"
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    modifier = Modifier.weight(0.45f),
                    onClick = {
                        var hasError = false

                        if (title.isBlank()) {
                            titleError = true
                            hasError = true
                        }

                        if (date.isBlank()) {
                            dateError = true
                            hasError = true
                        }

                        if (time.isBlank()) {
                            timeError = true
                            hasError = true
                        }

                        if (category.isBlank()) {
                            categoryError = true
                            hasError = true
                        }

                        if (!hasError) {
                            val dateSplit = date.split("-")
                            val timeSplit = time.split(":")
                            onAddClick(
                                reminder.id,
                                dateSplit[2].toInt(),
                                dateSplit[1].toInt(),
                                dateSplit[0].toInt(),
                                timeSplit[0].toInt(),
                                timeSplit[1].toInt(),
                                title,
                                description,
                                category,
                                notificationsEnabled,
                                finished,
                                false,
                                attachments
                            )
                        }
                    },
                ) {
                    Text("Zapisz wydarzenie")
                }

                val coroutineScope = rememberCoroutineScope()
                Button(
                    modifier = Modifier.weight(0.45f),
                    onClick = {
                        coroutineScope.launch {
                            onDeleteClick(reminder.id)
                        }
                    }
                ) {
                    Text("Usuń wydarzenie")
                }
            }
        }
    }
}

@Composable
fun ClickableField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onClick: () -> Unit,
    isError: Boolean = false
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                .clickable { onClick() }
                .padding(16.dp)
        ) {
            Text(
                text = if (value.isNotEmpty()) value else "Wybierz...",
                color = if (isError) MaterialTheme.colorScheme.error else Color.Unspecified
            )
        }
        if (isError) {
            Text(
                text = "Pole wymagane",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun TimePickerField(
    modifier: Modifier = Modifier,
    label: String = "Godzina",
    selectedTime: String,
    onTimeSelected: (String) -> Unit,
    isError: Boolean
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val interactionSource = remember { MutableInteractionSource() }

    ClickableField(
        modifier = modifier,
        label = label,
        value = selectedTime,
        onClick = {
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selected = String.format("%02d:%02d", hourOfDay, minute)
                    onTimeSelected(selected)
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24h
            ).show()
        },
        isError = isError
    )
}

@Composable
fun DatePickerField(
    modifier: Modifier = Modifier,
    label: String = "Data",
    selectedDate: String,
    onDateSelected: (String) -> Unit,
    isError: Boolean
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }

    ClickableField(
        modifier = modifier,
        label = label,
        value = selectedDate,
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                    onDateSelected(date)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        },
        isError = isError
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    notificationMinutes: Int,
    onNotificationMinutesChange: (Int) -> Unit,
    hideCompleted: Boolean,
    onHideCompletedChange: (Boolean) -> Unit
) {
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // Wybór kategorii
        Text(text = "Wybierz kategorię", style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(
            expanded = categoryDropdownExpanded,
            onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
        ) {
            TextField(
                value = selectedCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Kategoria") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(categoryDropdownExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = categoryDropdownExpanded,
                onDismissRequest = { categoryDropdownExpanded = false }
            ) {
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = { Text(category) },
                        onClick = {
                            onCategorySelected(category)
                            categoryDropdownExpanded = false
                        }
                    )
                }
            }
        }

        var notificationError by remember { mutableStateOf(false) }

        Text(text = "Czas przypomnienia (w minutach)", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = notificationMinutes.toString(),
            onValueChange = {
                if (it.all { char -> char.isDigit() }) {
                    if (it.isNotBlank()){
                        onNotificationMinutesChange(it.toInt())
                    } else {
                        onNotificationMinutesChange(0)
                    }
                }
            },
            label = { Text("Minuty") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ukryj zakończone zadania", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = hideCompleted,
                onCheckedChange = onHideCompletedChange
            )
        }
    }
}