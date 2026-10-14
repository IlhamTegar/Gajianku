package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zeichi01.gajianku.data.local.entity.EmergencyFund
import com.zeichi01.gajianku.data.local.entity.EmergencyFundHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface EmergencyFundDao {

    // --- Saldo darurat (single row, id = 1) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFund(fund: EmergencyFund)

    @Query("SELECT * FROM emergency_fund WHERE id = 1")
    fun getFund(): Flow<EmergencyFund?>

    @Query("SELECT * FROM emergency_fund WHERE id = 1")
    suspend fun getFundOnce(): EmergencyFund?

    @Query("UPDATE emergency_fund SET currentBalance = :balance, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateBalance(balance: Long, updatedAt: Long)

    // --- History ---

    @Insert
    suspend fun insertHistory(history: EmergencyFundHistory): Long

    @Query("SELECT * FROM emergency_fund_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<EmergencyFundHistory>>

    @Query("SELECT * FROM emergency_fund_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): EmergencyFundHistory?

    @Query("DELETE FROM emergency_fund_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    // --- Transaksi atomik ---

    /**
     * Tambah dana darurat (dari alokasi gaji atau top up manual).
     */
    @Transaction
    suspend fun addFunds(amount: Long, note: String, type: String = "TAMBAH") {
        val now = System.currentTimeMillis()
        val current = getFundOnce()
            ?: EmergencyFund(id = 1, currentBalance = 0, createdAt = now, updatedAt = now)
        updateBalance(current.currentBalance + amount, now)
        insertHistory(
            EmergencyFundHistory(
                amount = amount,
                type = type,
                note = note,
                createdAt = now
            )
        )
    }

    /**
     * Pakai dana darurat. Return true kalau berhasil, false kalau saldo tidak cukup.
     */
    @Transaction
    suspend fun useFunds(amount: Long, note: String): Boolean {
        val now = System.currentTimeMillis()
        val current = getFundOnce() ?: return false

        if (current.currentBalance < amount) {
            return false
        }

        updateBalance(current.currentBalance - amount, now)
        insertHistory(
            EmergencyFundHistory(
                amount = amount,
                type = "PAKAI",
                note = note,
                createdAt = now
            )
        )
        return true
    }

    /**
     * Hapus history & kembalikan saldo sesuai jenisnya.
     */
    @Transaction
    suspend fun deleteHistoryAndRevert(historyId: Long): Boolean {
        val history = getHistoryById(historyId) ?: return false
        val current = getFundOnce() ?: return false
        val now = System.currentTimeMillis()

        // Kalau history-nya "PAKAI", hapus = saldo ditambah kembali
        // Kalau history-nya "TAMBAH", hapus = saldo dikurangi
        val newBalance = if (history.type == "PAKAI") {
            current.currentBalance + history.amount
        } else {
            (current.currentBalance - history.amount).coerceAtLeast(0)
        }

        updateBalance(newBalance, now)
        deleteHistoryById(historyId)
        return true
    }
}