package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salary_allocations",
    foreignKeys = [
        ForeignKey(
            entity = Preset::class,
            parentColumns = ["id"],
            childColumns = ["presetId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("presetId")]
)
data class SalaryAllocation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // format: "2025-10"
    val month: String,

    val salaryAmount: Long,

    // nullable, karena preset bisa dihapus
    val presetId: Long? = null,

    // berapa minus yang dibayar bulan ini
    val deficitPaid: Long = 0,

    val createdAt: Long = System.currentTimeMillis()
)