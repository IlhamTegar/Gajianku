package com.zeichi01.gajianku.domain

import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.PresetItem

/**
 * Hasil alokasi gaji.
 */
data class AllocationResult(
    val salaryAmount: Long,
    val items: List<AllocatedItem>,
    val totalSavingsPocket: Long,     // total Tabungan Wajib + Darurat
    val savingsWajibAmount: Long,     // hanya Tabungan Wajib (yang dibagi ke target)
    val savingsDaruratAmount: Long,   // hanya Tabungan Darurat
    val deficitPaid: Long             // berapa minus yang dibayar dari Tabungan Wajib
)

data class AllocatedItem(
    val categoryId: Long,
    val categoryName: String,
    val percentage: Float,
    val amount: Long,
    val isSavingsPocket: Boolean
)

object SalaryAllocator {

    /**
     * Bagi gaji ke kategori berdasarkan preset.
     *
     * @param salary Gaji bulan ini
     * @param presetItems Item preset (categoryId + percentage)
     * @param categories Daftar semua kategori
     * @param currentDeficit Total minus yang harus dibayar (0 kalau tidak ada)
     * @return AllocationResult — rincian alokasi
     */
    fun allocate(
        salary: Long,
        presetItems: List<PresetItem>,
        categories: List<Category>,
        currentDeficit: Long = 0
    ): AllocationResult {
        if (salary <= 0) {
            return AllocationResult(
                salaryAmount = 0,
                items = emptyList(),
                totalSavingsPocket = 0,
                savingsWajibAmount = 0,
                savingsDaruratAmount = 0,
                deficitPaid = 0
            )
        }

        val categoryMap = categories.associateBy { it.id }

        // 1. Hitung alokasi awal per kategori
        val rawItems = presetItems.mapNotNull { presetItem ->
            val category = categoryMap[presetItem.categoryId] ?: return@mapNotNull null
            AllocatedItem(
                categoryId = category.id,
                categoryName = category.name,
                percentage = presetItem.percentage,
                amount = (salary * presetItem.percentage / 100.0).toLong(),
                isSavingsPocket = category.isSavingsPocket
            )
        }

        // 2. Cari Tabungan Wajib & Darurat
        val savingsWajibItem = rawItems.find { it.categoryName == "Tabungan Wajib" }
        val savingsDaruratItem = rawItems.find { it.categoryName == "Tabungan Darurat" }

        val savingsWajibRaw = savingsWajibItem?.amount ?: 0
        val savingsDaruratRaw = savingsDaruratItem?.amount ?: 0

        // 3. Potong minus dari Tabungan Wajib
        val deficitPaid = minOf(currentDeficit, savingsWajibRaw)
        val savingsWajibAfterDeficit = savingsWajibRaw - deficitPaid

        // 4. Update item Tabungan Wajib dengan nilai setelah dipotong minus
        val finalItems = rawItems.map { item ->
            when (item.categoryName) {
                "Tabungan Wajib" -> item.copy(amount = savingsWajibAfterDeficit)
                else -> item
            }
        }

        return AllocationResult(
            salaryAmount = salary,
            items = finalItems,
            totalSavingsPocket = savingsWajibAfterDeficit + savingsDaruratRaw,
            savingsWajibAmount = savingsWajibAfterDeficit,
            savingsDaruratAmount = savingsDaruratRaw,
            deficitPaid = deficitPaid
        )
    }
}