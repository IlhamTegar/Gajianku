package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "deficit_history",
    foreignKeys = [
        ForeignKey(
            entity = SavingsTarget::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("targetId")]
)
data class DeficitHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Long,

    // "TAMBAH" atau "BAYAR"
    val type: String,

    // nullable, target yang menyebabkan minus
    val targetId: Long? = null,

    val note: String? = null,

    val createdAt: Long = System.currentTimeMillis()
)