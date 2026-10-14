package com.zeichi01.gajianku.data.repository

import com.zeichi01.gajianku.data.local.dao.DeficitDao
import com.zeichi01.gajianku.data.local.entity.DeficitHistory
import com.zeichi01.gajianku.data.local.entity.GlobalDeficit
import kotlinx.coroutines.flow.Flow

class DeficitRepository(
    private val deficitDao: DeficitDao
) {

    fun getDeficit(): Flow<GlobalDeficit?> = deficitDao.getDeficit()

    suspend fun getDeficitOnce(): GlobalDeficit? = deficitDao.getDeficitOnce()

    fun getAllHistory(): Flow<List<DeficitHistory>> = deficitDao.getAllHistory()

    /**
     * Tambah minus baru (dari pembelian barang yang lebih mahal dari target).
     */
    suspend fun addDeficit(
        amount: Long,
        targetId: Long?,
        note: String?
    ): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah minus harus lebih dari 0"))
        }
        return try {
            deficitDao.addDeficit(amount, targetId, note)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Bayar minus dari alokasi tabungan wajib.
     * Return: berapa yang benar-benar terbayar (bisa kurang kalau dana tidak cukup).
     */
    suspend fun payDeficit(amountAvailable: Long): Result<Long> {
        if (amountAvailable <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        return try {
            val current = deficitDao.getDeficitOnce()
                ?: return Result.success(0L)

            if (current.amount <= 0) {
                return Result.success(0L)
            }

            // Berapa yang bisa dibayar (min dari dana tersedia & total minus)
            val paid = minOf(amountAvailable, current.amount)
            deficitDao.payDeficit(paid)

            Result.success(paid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Cek apakah ada minus aktif.
     */
    suspend fun hasDeficit(): Boolean {
        val deficit = deficitDao.getDeficitOnce() ?: return false
        return deficit.amount > 0
    }
}