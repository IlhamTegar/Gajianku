package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emergency_fund")
data class EmergencyFund(
    @PrimaryKey
    val id: Long = 1,

    val currentBalance: Long = 0,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)