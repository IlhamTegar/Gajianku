package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "presets")
data class Preset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val isDefault: Boolean = false,

    val createdAt: Long = System.currentTimeMillis()
)