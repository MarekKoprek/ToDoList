package com.example.todolist.util

import android.net.Uri
import androidx.room.*

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Query("SELECT * FROM reminder")
    suspend fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminder WHERE category = :category")
    suspend fun getAllRemindersByCategory(category: String): List<Reminder>

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("UPDATE reminder SET attachments = :newAttachments WHERE id = :entityId")
    suspend fun updateAttachments(entityId: Int, newAttachments: List<Uri>)
}