package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_targets")
data class SavingsTarget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val targetAmount: Long,

    val currentAmount: Long = 0,

    // "TINGGI", "SEDANG", "RENDAH"
    val priority: String,

    // "AKTIF", "SIAP_DIBELI", "SELESAI"
    val status: String = "AKTIF",

    // untuk drag-and-drop di dalam level prioritas yang sama
    val sortOrder: Int = 0,

    val createdAt: Long = System.currentTimeMillis(),

    // nullable, diisi saat user tandai "sudah dibeli"
    val completedAt: Long? = null,

    // nullable, harga aktual saat beli
    val actualPrice: Long? = null
)