package com.example.todolist.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenView(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass,
    toDoViewModel: ToDoViewModel = viewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("Wybierz kategorię") }
    val categories = listOf("Kategoria 1", "Kategoria 2", "Kategoria 3")

    val sampleReminders = listOf(
        Reminder(12, "Mar", 14, 30, "Spotkanie z klientem", false, false, false),
        Reminder(13, "Mar", 9, 15, "Badanie lekarskie", false, true, false),
        Reminder(14, "Mar", 18, 0, "Kolacja z rodziną", true, false, true)
    )

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

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedCategory,
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
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Ikona ustawień
                IconButton(onClick = { /* TODO: przejście do ustawień */ }) {
                    Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                }
            }

            ToDoList(modifier = Modifier.weight(0.85f), sampleReminders)

        }
    }
}

@Composable
fun ToDoList(
    modifier: Modifier = Modifier,
    reminders: List<Reminder>
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
                onClick = { /* TODO: akcja dodawania */ },
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
            // DATA CZAS
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${reminder.day} ${reminder.month.uppercase()}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = "%02d:%02d".format(reminder.hour, reminder.minute),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // TYTUŁ I IKONY
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