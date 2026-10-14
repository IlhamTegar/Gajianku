package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zeichi01.gajianku.data.local.entity.AppNotification
import com.zeichi01.gajianku.data.local.entity.IncomingNotification
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    // --- Incoming Notifications (dari bank/e-wallet) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncoming(notification: IncomingNotification): Long

    @Update
    suspend fun updateIncoming(notification: IncomingNotification)

    @Query("SELECT * FROM incoming_notifications ORDER BY createdAt DESC")
    fun getAllIncoming(): Flow<List<IncomingNotification>>

    @Query("SELECT * FROM incoming_notifications WHERE status = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingIncoming(): List<IncomingNotification>

    @Query("UPDATE incoming_notifications SET status = :status WHERE id = :id")
    suspend fun updateIncomingStatus(id: Long, status: String)

    // --- App Notifications (notifikasi ke user) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppNotification(notification: AppNotification): Long

    @Query("SELECT * FROM app_notifications ORDER BY createdAt DESC")
    fun getAllAppNotifications(): Flow<List<AppNotification>>

    @Query("SELECT * FROM app_notifications WHERE isRead = 0 ORDER BY createdAt DESC")
    fun getUnreadNotifications(): Flow<List<AppNotification>>

    @Query("SELECT COUNT(*) FROM app_notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Query("SELECT * FROM app_notifications WHERE targetId = :targetId AND type = 'TARGET_TERCAPAI' LIMIT 1")
    suspend fun getTargetAchievedNotification(targetId: Long): AppNotification?

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()
}