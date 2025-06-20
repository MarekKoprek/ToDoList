package com.example.todolist.util

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "reminder")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val day: Int,
    val month: Int,
    val year: Int,
    val hour: Int,
    val minute: Int,
    val title: String,
    val description: String,
    val category: String,
    val notification: Boolean,
    val finished: Boolean,
    val files: Boolean,
    val attachments: List<Uri>,
    val notificationSent: Boolean
)
