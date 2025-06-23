package com.example.todolist

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.core.content.FileProvider
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
import java.time.LocalDateTime
import java.util.Calendar
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

    @SuppressLint("ScheduleExactAlarm")
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
            if (attachments.isNotEmpty()) {
                reminderDao.updateAttachments(newId.toInt(), newUris)
            }
            val newReminder = reminderDao.getReminderById(newId.toInt())
            if(newReminder != null) {
                scheduleReminder(newReminder)
            }
            getReminders()
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    fun editReminder(id: Int,
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
                     attachments: List<Uri>) {
        viewModelScope.launch {
            val reminder = reminderDao.getReminderById(id)
            reminder?.attachments?.forEach {
                deleteFileFromUri(it)
            }
            reminderDao.update(Reminder(id = id,
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
                val newFileName = "${id}_${getFileNameFromUri(attachment)}"
                val newUri = context.copyUriToAppStorage(attachment, newFileName)
                val newName = getFileNameFromUri(newUri)
                Log.d("NewUri", "$newUri - $newName")
                newUris += newUri
            }
            if (attachments.isNotEmpty()) {
                reminderDao.updateAttachments(id, newUris)
            }
            val newReminder = reminderDao.getReminderById(id)
            if(newReminder != null) {
                unscheduleReminder(newReminder)
                scheduleReminder(newReminder)
            }
            getReminders()
        }
    }

    fun removeReminder(id: Int) {
        viewModelScope.launch {
            val reminder = reminderDao.getReminderById(id)
            reminder?.attachments?.forEach {
                deleteFileFromUri(it)
            }
            if (reminder != null) {
                unscheduleReminder(reminder)
                reminderDao.delete(reminder)
            }
        }
        getReminders()
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

    @SuppressLint("ScheduleExactAlarm")
    fun setNotificationTime(time: Int) {
        _notificationTime.value = time
        _reminders.value.forEach {
            unscheduleReminder(it)
            scheduleReminder(it)
        }
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
        Log.d("FileSaved", "Plik zapisano: $targetFile")
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", targetFile)
    }

    private fun deleteFileFromUri(uri: Uri) {
        val file = File(uri.path!!)
        if (file.exists()) {
            val deleted = file.delete()
            Log.d("FileDelete", "Plik usunięty: $deleted")
        }
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

    @SuppressLint("ScheduleExactAlarm")
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun scheduleReminder(
        reminder: Reminder,
    ) {
        if (!reminder.notification || reminder.notificationSent || reminder.finished) {
            return
        }
        val context: Context = getApplication()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("description", reminder.description)
            putExtra("reminderId", reminder.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var date = LocalDateTime.of(reminder.year, reminder.month, reminder.day, reminder.hour, reminder.minute)
        date = date.minusMinutes(_notificationTime.value.toLong())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.monthValue - 1)
            set(Calendar.DAY_OF_MONTH, date.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, date.hour)
            set(Calendar.MINUTE, date.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        Log.d("schedule", date.toString())

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )

        viewModelScope.launch {
            reminderDao.updateNotificationSent(reminder.id)
        }
    }

    private fun unscheduleReminder(
        reminder: Reminder
    ) {
        val context: Context = getApplication()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("description", reminder.description)
            putExtra("reminderId", reminder.id)
        }
        Log.d("unschedule", reminder.title)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    fun openFileFromUri(fileUri: Uri) {
        val context: Context = getApplication()
        val openFileIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, context.contentResolver.getType(fileUri) ?: "*/*")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(openFileIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Brak aplikacji do otwarcia tego pliku", Toast.LENGTH_SHORT).show()
        }
    }
}