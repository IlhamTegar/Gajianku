package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "allocation_items",
    foreignKeys = [
        ForeignKey(
            entity = SalaryAllocation::class,
            parentColumns = ["id"],
            childColumns = ["allocationId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("allocationId"), Index("categoryId")]
)
data class AllocationItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val allocationId: Long,

    val categoryId: Long,

    val amount: Long
)