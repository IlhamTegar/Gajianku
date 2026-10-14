package com.zeichi01.gajianku.data.repository

import com.zeichi01.gajianku.data.local.dao.CategoryDao
import com.zeichi01.gajianku.data.local.dao.PresetDao
import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.local.entity.PresetItem
import kotlinx.coroutines.flow.Flow

class PresetRepository(
    private val presetDao: PresetDao,
    private val categoryDao: CategoryDao
) {

    fun getAllPresets(): Flow<List<Preset>> = presetDao.getAllPresets()

    fun getPresetItems(presetId: Long): Flow<List<PresetItem>> =
        presetDao.getItemsByPreset(presetId)

    suspend fun getPresetItemsOnce(presetId: Long): List<PresetItem> =
        presetDao.getItemsByPresetOnce(presetId)

    suspend fun getPresetById(id: Long): Preset? = presetDao.getPresetById(id)

    suspend fun countPresets(): Int = presetDao.countPresets()

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAll()

    /**
     * Simpan preset baru atau update yang sudah ada.
     * Return: Result<Long> — ID preset, atau error kalau validasi gagal.
     */
    suspend fun savePreset(
        presetId: Long?,
        name: String,
        items: List<PresetItem>
    ): Result<Long> {
        // Validasi 1: total harus 100%
        val total = items.sumOf { it.percentage.toDouble() }
        if (total != 100.0) {
            return Result.failure(
                IllegalArgumentException("Total persentase harus 100%, saat ini $total%")
            )
        }

        // Validasi 2: maks 5 preset
        if (presetId == null) {
            val count = presetDao.countPresets()
            if (count >= 5) {
                return Result.failure(
                    IllegalArgumentException("Maksimal 5 preset. Hapus salah satu dulu.")
                )
            }
        }

        return try {
            val id = if (presetId == null) {
                // Preset baru
                presetDao.insertPreset(
                    Preset(name = name, isDefault = false)
                )
            } else {
                // Update preset yang ada
                val existing = presetDao.getPresetById(presetId)
                    ?: return Result.failure(
                        IllegalArgumentException("Preset tidak ditemukan")
                    )
                presetDao.updatePreset(existing.copy(name = name))
                presetId
            }

            // Simpan items (replace)
            val itemsWithId = items.map { it.copy(presetId = id) }
            presetDao.replaceItems(id, itemsWithId)

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Hapus preset. Preset default tidak boleh dihapus.
     */
    suspend fun deletePreset(preset: Preset): Result<Unit> {
        if (preset.isDefault) {
            return Result.failure(
                IllegalArgumentException("Preset default tidak bisa dihapus")
            )
        }
        return try {
            presetDao.deletePreset(preset)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}