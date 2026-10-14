package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zeichi01.gajianku.data.local.entity.DeficitHistory
import com.zeichi01.gajianku.data.local.entity.GlobalDeficit
import kotlinx.coroutines.flow.Flow

@Dao
interface DeficitDao {

    // --- Global Deficit (single row, id = 1) ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeficit(deficit: GlobalDeficit)

    @Update
    suspend fun updateDeficit(deficit: GlobalDeficit)

    @Query("SELECT * FROM global_deficit WHERE id = 1")
    fun getDeficit(): Flow<GlobalDeficit?>

    @Query("SELECT * FROM global_deficit WHERE id = 1")
    suspend fun getDeficitOnce(): GlobalDeficit?

    @Query("UPDATE global_deficit SET amount = :amount, updatedAt = :updatedAt WHERE id = 1")
    suspend fun updateDeficitAmount(amount: Long, updatedAt: Long)

    // --- Deficit History ---

    @Insert
    suspend fun insertHistory(history: DeficitHistory): Long

    @Query("SELECT * FROM deficit_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<DeficitHistory>>

    @Transaction
    suspend fun addDeficit(amount: Long, targetId: Long?, note: String?) {
        val now = System.currentTimeMillis()
        val current = getDeficitOnce() ?: GlobalDeficit(id = 1, amount = 0, createdAt = now, updatedAt = now)
        updateDeficitAmount(current.amount + amount, now)
        insertHistory(
            DeficitHistory(
                amount = amount,
                type = "TAMBAH",
                targetId = targetId,
                note = note,
                createdAt = now
            )
        )
    }

    @Transaction
    suspend fun payDeficit(amount: Long) {
        val now = System.currentTimeMillis()
        val current = getDeficitOnce() ?: return
        val newAmount = (current.amount - amount).coerceAtLeast(0)
        updateDeficitAmount(newAmount, now)
        insertHistory(
            DeficitHistory(
                amount = amount,
                type = "BAYAR",
                note = null,
                createdAt = now
            )
        )
    }
}