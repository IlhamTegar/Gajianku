package com.zeichi01.gajianku.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.local.entity.PresetItem
import kotlinx.coroutines.flow.Flow

@Dao
interface PresetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: Preset): Long

    @Update
    suspend fun updatePreset(preset: Preset)

    @Delete
    suspend fun deletePreset(preset: Preset)

    @Query("SELECT * FROM presets ORDER BY isDefault DESC, createdAt ASC")
    fun getAllPresets(): Flow<List<Preset>>

    @Query("SELECT * FROM presets WHERE id = :id")
    suspend fun getPresetById(id: Long): Preset?

    @Query("SELECT COUNT(*) FROM presets")
    suspend fun countPresets(): Int

    // --- Preset Items ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<PresetItem>)

    @Query("SELECT * FROM preset_items WHERE presetId = :presetId")
    fun getItemsByPreset(presetId: Long): Flow<List<PresetItem>>

    @Query("SELECT * FROM preset_items WHERE presetId = :presetId")
    suspend fun getItemsByPresetOnce(presetId: Long): List<PresetItem>

    @Query("DELETE FROM preset_items WHERE presetId = :presetId")
    suspend fun deleteItemsByPreset(presetId: Long)

    @Transaction
    suspend fun replaceItems(presetId: Long, items: List<PresetItem>) {
        deleteItemsByPreset(presetId)
        insertItems(items)
    }
}