package com.example.todolist

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.todolist.util.AppDatabase
import com.example.todolist.util.Reminder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val _notificationTime = MutableStateFlow<Int>(0)
    val notificationTime: StateFlow<Int> = _notificationTime.asStateFlow()

    private val _category = MutableStateFlow<String>("Wszystkie")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _reminders = MutableStateFlow<List<Reminder>>(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    private val _hideFinished = MutableStateFlow<Boolean>(false)
    val hideFinished: StateFlow<Boolean> = _hideFinished.asStateFlow()

    private val _search = MutableStateFlow<String>("")
    val search: StateFlow<String> = _search.asStateFlow()

    private val db: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "my-database"
    ).build()

    private val reminderDao = db.reminderDao()

    fun addReminder(id: Int = 0,
                    day: Int,
                    month: Int,
                    year: Int,
                    hour: Int,
                    minute: Int,
                    title: String,
                    description: String,
                    category: String,
                    notification: Boolean,
                    finished: Boolean,
                    files: Boolean,
                    attachments: List<Uri>){
        viewModelScope.launch {
            val newId = reminderDao.insert(Reminder(id = id,
                day = day,
                month = month,
                year = year,
                hour = hour,
                minute = minute,
                title = title,
                description = description,
                category = category,
                notification = notification,
                finished = finished,
                files = files,
                attachments = emptyList(),
                notificationSent = false,
            ))
            val context: Context = getApplication()
            val newUris: MutableList<Uri> = emptyList<Uri>().toMutableList()
            for (attachment in attachments) {
                val newFileName = "${newId}_${getFileNameFromUri(attachment)}"
                newUris += context.copyUriToAppStorage(attachment, newFileName)
            }
            reminderDao.updateAttachments(newId.toInt(), newUris)
            getReminders()
        }
    }

    fun getReminders(){
        viewModelScope.launch {
            val list: List<Reminder>
            if (_category.value == "Wszystkie") {
                list = reminderDao.getAllReminders(_search.value)
            } else {
                list = reminderDao.getAllRemindersByCategory(_category.value, _search.value)
            }
            if (_hideFinished.value) {
                _reminders.value = list.filter { !it.finished }
            } else {
                _reminders.value = list
            }
        }
    }

    fun setCategory(category: String){
        _category.value = category
    }

    fun setNotificationTime(time: Int) {
        _notificationTime.value = time
    }

    fun setHideFinished(value: Boolean) {
        _hideFinished.value = value
    }

    fun setSearch(search: String) {
        _search.value = search
    }

    private fun Context.copyUriToAppStorage(uri: Uri, targetFileName: String): Uri {
        val targetFile = File(filesDir, targetFileName)
        contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return Uri.fromFile(targetFile)
    }

    fun getFileNameFromUri(uri: Uri): String? {
        val context: Context = getApplication()
        return context.getFileNameFromUri(uri)
    }

    private fun Context.getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }
}