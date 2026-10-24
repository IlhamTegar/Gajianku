package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val monthlySalary: Long,

    val bankName: String,

    val createdAt: Long = System.currentTimeMillis()
)