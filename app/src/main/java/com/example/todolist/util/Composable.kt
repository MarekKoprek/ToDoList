package com.example.todolist.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
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

    var lastView by remember { mutableStateOf("") }

    val onAddClick: (Int, Int, Int, Int, Int, Int, String, String, String, Boolean, Boolean, Boolean, List<Uri>) -> Unit = {
        id, day, month, year, hour, minute, title, description, category, notification, finished, files, attachments ->
        toDoViewModel.addReminder(id, day, month, year, hour, minute, title, description, category, notification, finished, files, attachments)
        currentView = "main"
    }

    val onViewChange: (String) -> Unit = {
        lastView = currentView
        currentView = it
    }

    val sampleReminders by toDoViewModel.reminders.collectAsState()
    val category by toDoViewModel.category.collectAsState()
    val notificationTime by toDoViewModel.notificationTime.collectAsState()

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    BackHandler {
        if (lastView.length > 0 && currentView != "main") {
            currentView = lastView
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
                    ExposedDropdownMenuBox(
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
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        toDoViewModel.setCategory(category)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column (
                    modifier = Modifier.weight(0.15f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(onClick = { /* TODO: przejście do ustawień */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            }

            if (currentView == "main"){
                ToDoList(modifier = Modifier.weight(0.85f), sampleReminders, onViewChange)
            }
            else if (currentView == "add"){
                AddReminder(modifier = Modifier.weight(0.85f), toDoViewModel, onAddClick, categories.subList(1, categories.size))
            }

        }
    }
}

@Composable
fun ToDoList(
    modifier: Modifier = Modifier,
    reminders: List<Reminder>,
    onViewChange: (String) -> Unit
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
                    ReminderCard(it)
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
fun ReminderCard(reminder: Reminder) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
    var attachments by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var category by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    var titleError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }

    val multipleAttachmentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        attachments = attachments + uris
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
                Text(text = "${index + 1}. $fileName")
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