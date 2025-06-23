package com.example.todolist.util

import android.net.Uri
import androidx.room.*

@Dao
interface ReminderDao {
    @Insert
    suspend fun insert(reminder: Reminder): Long

    @Update
    suspend fun update(reminder: Reminder)

    @Query("SELECT * FROM reminder WHERE id = :id ORDER BY month, day, hour, minute")
    suspend fun getReminderById(id: Int): Reminder?

    @Query("SELECT * FROM reminder WHERE title LIKE '%' || :search || '%' ORDER BY month, day, hour, minute")
    suspend fun getAllReminders(search: String): List<Reminder>

    @Query("SELECT * FROM reminder WHERE category = :category AND title LIKE '%' || :search || '%' ORDER BY month, day, hour, minute")
    suspend fun getAllRemindersByCategory(category: String, search: String): List<Reminder>

    @Delete
    suspend fun delete(reminder: Reminder)

    @Query("UPDATE reminder SET attachments = :newAttachments, files = true WHERE id = :entityId")
    suspend fun updateAttachments(entityId: Int, newAttachments: List<Uri>)

    @Query("UPDATE reminder SET notificationSent = true WHERE id = :entityId")
    suspend fun updateNotificationSent(entityId: Int)
}