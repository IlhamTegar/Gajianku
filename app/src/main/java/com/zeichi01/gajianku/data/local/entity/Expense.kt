package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("month")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val categoryId: Long,

    val amount: Long,

    val note: String,

    // format: "2025-10"
    val month: String,

    val createdAt: Long = System.currentTimeMillis()
)