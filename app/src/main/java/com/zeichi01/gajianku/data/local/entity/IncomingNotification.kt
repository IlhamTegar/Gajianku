package com.zeichi01.gajianku.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incoming_notifications")
data class IncomingNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val rawText: String,

    // package name bank/e-wallet, misal "com.bca"
    val sourcePackage: String,

    // nominal yang berhasil di-parse (nullable kalau gagal)
    val parsedAmount: Long? = null,

    // "SALDO_MASUK", "TRANSFER_KELUAR", dll
    val parsedType: String? = null,

    // "PENDING", "PROCESSED", "IGNORED"
    val status: String = "PENDING",

    val createdAt: Long = System.currentTimeMillis()
)