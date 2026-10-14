package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.zeichi01.gajianku.data.local.entity.AllocationItem
import com.zeichi01.gajianku.data.local.entity.SalaryAllocation
import kotlinx.coroutines.flow.Flow

@Dao
interface AllocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllocation(allocation: SalaryAllocation): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<AllocationItem>)

    @Query("SELECT * FROM salary_allocations ORDER BY month DESC")
    fun getAllAllocations(): Flow<List<SalaryAllocation>>

    @Query("SELECT * FROM salary_allocations WHERE month = :month LIMIT 1")
    suspend fun getAllocationByMonth(month: String): SalaryAllocation?

    @Query("SELECT * FROM salary_allocations WHERE id = :id")
    suspend fun getAllocationById(id: Long): SalaryAllocation?

    @Query("SELECT * FROM allocation_items WHERE allocationId = :allocationId")
    fun getItemsByAllocation(allocationId: Long): Flow<List<AllocationItem>>

    @Query("SELECT * FROM allocation_items WHERE allocationId = :allocationId")
    suspend fun getItemsByAllocationOnce(allocationId: Long): List<AllocationItem>

    @Transaction
    suspend fun insertFullAllocation(
        allocation: SalaryAllocation,
        items: List<AllocationItem>
    ): Long {
        val allocationId = insertAllocation(allocation)
        val itemsWithId = items.map { it.copy(allocationId = allocationId) }
        insertItems(itemsWithId)
        return allocationId
    }
}