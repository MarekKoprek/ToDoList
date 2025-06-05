package com.example.todolist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ToDoViewModel(application: Application) : AndroidViewModel(application) {
    private val _notificationTime = MutableStateFlow<Int>(0)
    val notificationTime: StateFlow<Int> = _notificationTime.asStateFlow()


}