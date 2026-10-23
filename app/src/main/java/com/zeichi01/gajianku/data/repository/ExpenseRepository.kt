package com.zeichi01.gajianku.data.repository

import com.zeichi01.gajianku.data.local.dao.ExpenseDao
import com.zeichi01.gajianku.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseRepository(
    private val expenseDao: ExpenseDao
) {

    fun getExpensesByMonth(month: String): Flow<List<Expense>> =
        expenseDao.getExpensesByMonth(month)

    fun getExpensesByCategory(month: String, categoryId: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByCategory(month, categoryId)

    fun getTotalByCategory(month: String, categoryId: Long): Flow<Long> =
        expenseDao.getTotalByCategory(month, categoryId).map { it ?: 0L }

    fun getTotalByMonth(month: String): Flow<Long> =
        expenseDao.getTotalByMonth(month).map { it ?: 0L }

    /**
     * Catat pengeluaran baru (Biaya Hidup, Investasi, dll).
     */
    suspend fun addExpense(
        categoryId: Long,
        amount: Long,
        note: String,
        month: String = getCurrentMonth()
    ): Result<Long> {
        if (amount <= 0) {
            return Result.failure(IllegalArgumentException("Nominal harus lebih dari 0"))
        }
        if (note.isBlank()) {
            return Result.failure(IllegalArgumentException("Catatan tidak boleh kosong"))
        }

        return try {
            val id = expenseDao.insert(
                Expense(
                    categoryId = categoryId,
                    amount = amount,
                    note = note,
                    month = month
                )
            )
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus catatan pengeluaran (kalau salah input).
     */
    suspend fun deleteExpense(expense: Expense): Result<Unit> {
        return try {
            expenseDao.delete(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Helper untuk bulan sekarang ("yyyy-MM").
     */
    fun getCurrentMonth(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    fun getAllExpensesThisMonth(): Flow<List<Expense>> {
        return expenseDao.getExpensesByMonth(getCurrentMonth())
    }
}