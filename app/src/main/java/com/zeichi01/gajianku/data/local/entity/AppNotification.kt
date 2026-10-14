package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app_notifications",
    foreignKeys = [
        ForeignKey(
            entity = SavingsTarget::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("targetId")]
)
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // nullable, karena tidak semua notifikasi terkait target
    val targetId: Long? = null,

    // "TARGET_TERCAPAI", "DEFICIT_WARNING", dll
    val type: String,

    val title: String,

    val message: String,

    val isRead: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)