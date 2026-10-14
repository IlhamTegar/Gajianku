package com.zeichi01.gajianku.data.local

import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.EmergencyFund
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.local.entity.PresetItem
import kotlinx.coroutines.flow.first

class DatabaseSeeder(private val db: AppDatabase) {

    suspend fun seedIfNeeded() {
        seedCategories()
        seedPresets()
        seedEmergencyFund()
    }

    private suspend fun seedCategories() {
        val categoryDao = db.categoryDao()
        if (categoryDao.count() > 0) return

        val categories = listOf(
            Category(
                name = "Orangtua",
                isSavingsPocket = false,
                displayOrder = 1
            ),
            Category(
                name = "Biaya Hidup",
                isSavingsPocket = false,
                displayOrder = 2
            ),
            Category(
                name = "Tabungan Wajib",
                isSavingsPocket = true,
                displayOrder = 3
            ),
            Category(
                name = "Tabungan Darurat",
                isSavingsPocket = true,
                displayOrder = 4
            ),
            Category(
                name = "Investasi",
                isSavingsPocket = false,
                displayOrder = 5
            ),
            Category(
                name = "Gaya Hidup",
                isSavingsPocket = false,
                displayOrder = 6
            )
        )

        categoryDao.insertAll(categories)
    }

    private suspend fun seedPresets() {
        val presetDao = db.presetDao()
        if (presetDao.countPresets() > 0) return

        // Ambil kategori untuk dapat ID-nya
        val categories = db.categoryDao().getAll().first()
        val categoryMap = categories.associateBy { it.name }

        // --- Preset 1 ---
        val preset1Id = presetDao.insertPreset(
            Preset(
                name = "Preset Standar",
                isDefault = true
            )
        )

        val preset1Items = listOf(
            PresetItem(presetId = preset1Id, categoryId = categoryMap["Orangtua"]!!.id, percentage = 40f),
            PresetItem(presetId = preset1Id, categoryId = categoryMap["Biaya Hidup"]!!.id, percentage = 20f),
            PresetItem(presetId = preset1Id, categoryId = categoryMap["Tabungan Wajib"]!!.id, percentage = 15f),
            PresetItem(presetId = preset1Id, categoryId = categoryMap["Tabungan Darurat"]!!.id, percentage = 10f),
            PresetItem(presetId = preset1Id, categoryId = categoryMap["Investasi"]!!.id, percentage = 10f),
            PresetItem(presetId = preset1Id, categoryId = categoryMap["Gaya Hidup"]!!.id, percentage = 5f)
        )

        presetDao.insertItems(preset1Items)

        // --- Preset 2 ---
        val preset2Id = presetDao.insertPreset(
            Preset(
                name = "Preset Hemat",
                isDefault = true
            )
        )

        val preset2Items = listOf(
            PresetItem(presetId = preset2Id, categoryId = categoryMap["Orangtua"]!!.id, percentage = 35f),
            PresetItem(presetId = preset2Id, categoryId = categoryMap["Tabungan Wajib"]!!.id, percentage = 25f),
            PresetItem(presetId = preset2Id, categoryId = categoryMap["Biaya Hidup"]!!.id, percentage = 25f),
            PresetItem(presetId = preset2Id, categoryId = categoryMap["Tabungan Darurat"]!!.id, percentage = 10f),
            PresetItem(presetId = preset2Id, categoryId = categoryMap["Investasi"]!!.id, percentage = 5f)
        )

        presetDao.insertItems(preset2Items)
    }

    private suspend fun seedEmergencyFund() {
        val dao = db.emergencyFundDao()
        if (dao.getFundOnce() == null) {
            dao.upsertFund(EmergencyFund(id = 1, currentBalance = 0))
        }
    }
}