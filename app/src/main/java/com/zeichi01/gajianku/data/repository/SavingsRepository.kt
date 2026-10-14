package com.zeichi01.gajianku.data.repository

import com.zeichi01.gajianku.data.local.dao.SavingsTargetDao
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import kotlinx.coroutines.flow.Flow

class SavingsRepository(
    private val targetDao: SavingsTargetDao
) {

    fun getActiveTargets(): Flow<List<SavingsTarget>> = targetDao.getActiveTargets()

    fun getReadyTargets(): Flow<List<SavingsTarget>> = targetDao.getReadyTargets()

    fun getCompletedTargets(): Flow<List<SavingsTarget>> = targetDao.getCompletedTargets()

    suspend fun getTargetById(id: Long): SavingsTarget? = targetDao.getById(id)

    suspend fun getAllActiveOnce(): List<SavingsTarget> = targetDao.getAllActiveOnce()

    /**
     * Tambah target baru.
     */
    suspend fun addTarget(
        name: String,
        targetAmount: Long,
        priority: String
    ): Result<Long> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama target tidak boleh kosong"))
        }
        if (targetAmount <= 0) {
            return Result.failure(IllegalArgumentException("Target dana harus lebih dari 0"))
        }
        if (priority !in listOf("TINGGI", "SEDANG", "RENDAH")) {
            return Result.failure(IllegalArgumentException("Prioritas tidak valid"))
        }

        return try {
            // Cari sortOrder tertinggi di level prioritas yang sama
            val samePriority = targetDao.getActiveByPriority(priority)
            val maxSortOrder = samePriority.maxOfOrNull { it.sortOrder } ?: 0

            val id = targetDao.insert(
                SavingsTarget(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = 0,
                    priority = priority,
                    status = "AKTIF",
                    sortOrder = maxSortOrder + 1
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update target yang sudah ada.
     */
    suspend fun updateTarget(target: SavingsTarget): Result<Unit> {
        if (target.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Nama target tidak boleh kosong"))
        }
        if (target.targetAmount <= 0) {
            return Result.failure(IllegalArgumentException("Target dana harus lebih dari 0"))
        }
        return try {
            targetDao.update(target)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus target.
     */
    suspend fun deleteTarget(target: SavingsTarget): Result<Unit> {
        return try {
            targetDao.delete(target)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tambah dana ke target. Kalau sudah penuh, ubah status ke SIAP_DIBELI.
     */
    suspend fun addFunds(targetId: Long, amount: Long): Result<Unit> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Jumlah harus lebih dari 0"))
        }
        return try {
            val target = targetDao.getById(targetId)
                ?: return Result.failure(IllegalArgumentException("Target tidak ditemukan"))

            val newAmount = target.currentAmount + amount
            targetDao.updateAmount(targetId, newAmount)

            // Cek apakah sudah penuh
            if (newAmount >= target.targetAmount && target.status == "AKTIF") {
                targetDao.updateStatus(targetId, "SIAP_DIBELI")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Tandai target sudah dibeli.
     * Return: selisih (positif = lebih murah, negatif = lebih mahal / minus)
     */
    suspend fun markAsBought(targetId: Long, actualPrice: Long): Result<Long> {
        return try {
            val target = targetDao.getById(targetId)
                ?: return Result.failure(IllegalArgumentException("Target tidak ditemukan"))

            val now = System.currentTimeMillis()
            targetDao.markAsCompleted(targetId, now, actualPrice)

            // Selisih: currentAmount - actualPrice
            // Positif = lebih murah (sisa masuk tabungan)
            // Negatif = lebih mahal (minus)
            val diff = target.currentAmount - actualPrice
            Result.success(diff)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}