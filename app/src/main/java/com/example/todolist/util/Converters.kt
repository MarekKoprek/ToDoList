package com.example.todolist.util

import android.net.Uri
import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromUriList(uris: List<Uri>): String {
        return uris.joinToString(separator = ",") { it.toString() }
    }

    @TypeConverter
    fun toUriList(data: String): List<Uri> {
        return if (data.isEmpty()) emptyList() else
            data.split(",").map { Uri.parse(it) }
    }
}