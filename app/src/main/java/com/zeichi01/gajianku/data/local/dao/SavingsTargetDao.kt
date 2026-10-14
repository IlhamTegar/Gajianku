package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsTargetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(target: SavingsTarget): Long

    @Update
    suspend fun update(target: SavingsTarget)

    @Delete
    suspend fun delete(target: SavingsTarget)

    @Query("""
    SELECT * FROM savings_targets 
    WHERE status = 'AKTIF' 
    ORDER BY 
        CASE priority 
            WHEN 'TINGGI' THEN 1 
            WHEN 'SEDANG' THEN 2 
            WHEN 'RENDAH' THEN 3 
        END ASC, 
        sortOrder ASC""")
    fun getActiveTargets(): Flow<List<SavingsTarget>>

    @Query("SELECT * FROM savings_targets WHERE status = 'SIAP_DIBELI' ORDER BY priority ASC, sortOrder ASC")
    fun getReadyTargets(): Flow<List<SavingsTarget>>

    @Query("SELECT * FROM savings_targets WHERE status = 'SELESAI' ORDER BY completedAt DESC")
    fun getCompletedTargets(): Flow<List<SavingsTarget>>

    @Query("SELECT * FROM savings_targets WHERE id = :id")
    suspend fun getById(id: Long): SavingsTarget?

    @Query("SELECT * FROM savings_targets WHERE priority = :priority AND status = 'AKTIF' ORDER BY sortOrder ASC")
    suspend fun getActiveByPriority(priority: String): List<SavingsTarget>

    @Query("SELECT * FROM savings_targets WHERE status = 'AKTIF'")
    suspend fun getAllActiveOnce(): List<SavingsTarget>

    @Query("UPDATE savings_targets SET currentAmount = :amount WHERE id = :id")
    suspend fun updateAmount(id: Long, amount: Long)

    @Query("UPDATE savings_targets SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("""
        UPDATE savings_targets 
        SET status = 'SELESAI', completedAt = :completedAt, actualPrice = :actualPrice 
        WHERE id = :id
    """)
    suspend fun markAsCompleted(id: Long, completedAt: Long, actualPrice: Long)
}