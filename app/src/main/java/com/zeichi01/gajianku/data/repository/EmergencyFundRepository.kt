package com.zeichi01.gajianku.data.repository

import com.zeichi01.gajianku.data.local.dao.EmergencyFundDao
import com.zeichi01.gajianku.data.local.entity.EmergencyFund
import com.zeichi01.gajianku.data.local.entity.EmergencyFundHistory
import kotlinx.coroutines.flow.Flow

class EmergencyFundRepository(
    private val dao: EmergencyFundDao
) {

    fun getFund(): Flow<EmergencyFund?> = dao.getFund()

    suspend fun getFundOnce(): EmergencyFund? = dao.getFundOnce()

    fun getAllHistory(): Flow<List<EmergencyFundHistory>> = dao.getAllHistory()

    /**
     * Tambah dana darurat (dari alokasi gaji atau top up manual).
     */
    suspend fun addFunds(amount: Long, note: String): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        return try {
            dao.addFunds(amount, note, "TAMBAH")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pakai dana darurat. Validasi: saldo harus cukup.
     */
    suspend fun useFunds(amount: Long, note: String): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        if (note.isBlank()) {
            return Result.failure(IllegalArgumentException("Keterangan harus diisi"))
        }
        val success = dao.useFunds(amount, note)
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("Saldo darurat tidak cukup"))
        }
    }

    /**
     * Hapus history & kembalikan saldo otomatis.
     */
    suspend fun deleteHistory(historyId: Long): Result<Unit> {
        val success = dao.deleteHistoryAndRevert(historyId)
        return if (success) {
            Result.success(Unit)
        } else {
            Result.failure(IllegalArgumentException("History tidak ditemukan"))
        }
    }
}