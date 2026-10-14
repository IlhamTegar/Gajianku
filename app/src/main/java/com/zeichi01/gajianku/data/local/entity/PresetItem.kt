package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "preset_items",
    foreignKeys = [
        ForeignKey(
            entity = Preset::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("presetId"), Index("categoryId")]
)
data class PresetItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val presetId: Long,

    val categoryId: Long,

    val percentage: Float
)