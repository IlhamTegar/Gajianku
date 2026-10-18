package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zeichi01.gajianku.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE month = :month ORDER BY createdAt DESC")
    fun getExpensesByMonth(month: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE month = :month AND categoryId = :categoryId ORDER BY createdAt DESC")
    fun getExpensesByCategory(month: String, categoryId: Long): Flow<List<Expense>>

    /**
     * Hitung total pengeluaran untuk 1 kategori di bulan tertentu.
     * Berguna untuk menghitung "Sisa Biaya Hidup" atau "Total Investasi".
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE month = :month AND categoryId = :categoryId")
    fun getTotalByCategory(month: String, categoryId: Long): Flow<Long?>

    /**
     * Hitung total SEMUA pengeluaran di bulan tertentu.
     */
    @Query("SELECT SUM(amount) FROM expenses WHERE month = :month")
    fun getTotalByMonth(month: String): Flow<Long?>
}