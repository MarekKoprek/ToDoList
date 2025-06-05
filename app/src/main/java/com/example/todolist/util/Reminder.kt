package com.example.todolist.util

data class Reminder(
    val day: Int,
    val month: String,
    val hour: Int,
    val minute: Int,
    val title: String,
    val notification: Boolean,
    val finished: Boolean,
    val files: Boolean
)
