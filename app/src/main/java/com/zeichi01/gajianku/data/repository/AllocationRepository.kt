package com.zeichi01.gajianku.data.repository

import com.zeichi01.gajianku.data.local.dao.AllocationDao
import com.zeichi01.gajianku.data.local.entity.AllocationItem
import com.zeichi01.gajianku.data.local.entity.SalaryAllocation
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AllocationRepository(
    private val allocationDao: AllocationDao
) {

    fun getAllAllocations(): Flow<List<SalaryAllocation>> =
        allocationDao.getAllAllocations()

    fun getItemsByAllocation(allocationId: Long): Flow<List<AllocationItem>> =
        allocationDao.getItemsByAllocation(allocationId)

    suspend fun getAllocationByMonth(month: String): SalaryAllocation? =
        allocationDao.getAllocationByMonth(month)

    suspend fun getAllocationById(id: Long): SalaryAllocation? =
        allocationDao.getAllocationById(id)

    suspend fun getItemsByAllocationOnce(allocationId: Long): List<AllocationItem> =
        allocationDao.getItemsByAllocationOnce(allocationId)

    /**
     * Simpan alokasi gaji untuk bulan tertentu.
     */
    suspend fun saveAllocation(
        month: String,
        salaryAmount: Long,
        presetId: Long?,
        deficitPaid: Long,
        items: List<AllocationItem>
    ): Result<Long> {
        if (salaryAmount <= 0) {
            return Result.failure(IllegalArgumentException("Gaji harus lebih dari 0"))
        }
        return try {
            // Cek apakah bulan ini sudah ada alokasi
            val existing = allocationDao.getAllocationByMonth(month)
            if (existing != null) {
                return Result.failure(
                    IllegalArgumentException("Alokasi untuk bulan $month sudah ada")
                )
            }

            val id = allocationDao.insertFullAllocation(
                SalaryAllocation(
                    month = month,
                    salaryAmount = salaryAmount,
                    presetId = presetId,
                    deficitPaid = deficitPaid
                ),
                items
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper: dapatkan bulan sekarang dalam format "YYYY-MM".
     */
    fun getCurrentMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }
}