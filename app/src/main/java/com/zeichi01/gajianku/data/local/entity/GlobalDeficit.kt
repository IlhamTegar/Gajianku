package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "global_deficit")
data class GlobalDeficit(
    @PrimaryKey
    val id: Long = 1,

    val amount: Long = 0,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)