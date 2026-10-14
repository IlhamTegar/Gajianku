package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_fund_history")
data class EmergencyFundHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Long,

    // "PAKAI" atau "TAMBAH"
    val type: String,

    val note: String,

    val createdAt: Long = System.currentTimeMillis()
)