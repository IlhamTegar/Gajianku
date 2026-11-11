package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import com.zeichi01.gajianku.data.repository.AllocationRepository
import com.zeichi01.gajianku.data.repository.DeficitRepository
import com.zeichi01.gajianku.data.repository.SavingsRepository
import com.zeichi01.gajianku.domain.SavingsDistributor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TargetViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val targetRepo = SavingsRepository(db.savingsTargetDao())
    private val allocationRepo = AllocationRepository(db.allocationDao())

    val activeTargets: StateFlow<List<SavingsTarget>> = targetRepo.getActiveTargets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalCollected: StateFlow<Long> = activeTargets
        .map { targets -> targets.sumOf { it.currentAmount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val completedTargets: StateFlow<List<SavingsTarget>> = targetRepo.getCompletedTargets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTarget(name: String, amount: Long, priority: String) {
        viewModelScope.launch {
            targetRepo.addTarget(name = name, targetAmount = amount, priority = priority)
        }
    }

    fun updateTargetOrder(reorderedTargets: List<SavingsTarget>) {
        viewModelScope.launch {
            // 1. Simpan urutan baru
            reorderedTargets.forEachIndexed { index, target ->
                targetRepo.updateTarget(target.copy(sortOrder = index))
            }

            // 2. Ambil Tabungan Wajib penuh
            val currentMonth = allocationRepo.getCurrentMonth()
            val alloc = allocationRepo.getAllocationByMonth(currentMonth)
            if (alloc != null) {
                val items = allocationRepo.getItemsByAllocationOnce(alloc.id)
                val categories = db.categoryDao().getAll().first()
                val tabunganWajibCat = categories.find { it.name == "Tabungan Wajib" }
                val savingsWajib = if (tabunganWajibCat != null) {
                    items.find { it.categoryId == tabunganWajibCat.id }?.amount ?: 0L
                } else 0L

                if (savingsWajib > 0) {
                    // 3. RESET currentAmount semua target
                    reorderedTargets.forEach { target ->
                        db.savingsTargetDao().updateAmount(target.id, 0L)
                        db.savingsTargetDao().updateStatus(target.id, "AKTIF")
                    }

                    // 4. Distribusi ulang dari nol
                    val freshTargets = targetRepo.getActiveTargets().first()
                    val distribution = SavingsDistributor.distribute(savingsWajib, freshTargets)
                    distribution.allocations.forEach { (targetId, allocated) ->
                        if (allocated > 0) {
                            val target = freshTargets.find { it.id == targetId } ?: return@forEach
                            db.savingsTargetDao().updateAmount(targetId, allocated)
                            if (allocated >= target.targetAmount) {
                                db.savingsTargetDao().updateStatus(targetId, "SIAP_DIBELI")
                            }
                        }
                    }
                }
            }
        }
    }

    fun deleteTarget(target: SavingsTarget) {
        viewModelScope.launch {
            targetRepo.deleteTarget(target)
        }
    }

    fun markAsBought(target: SavingsTarget, actualPrice: Long) {
        viewModelScope.launch {
            // 1. Tandai target selesai
            val result = targetRepo.markAsBought(target.id, actualPrice)
            if (result.isFailure) return@launch

            val diff = result.getOrNull() ?: 0L

            // 2. Handle selisih
            if (diff > 0) {
                // Lebih murah: sisa masuk Tabungan Wajib, distribusi ulang
                val currentMonth = allocationRepo.getCurrentMonth()
                val alloc = allocationRepo.getAllocationByMonth(currentMonth)
                if (alloc != null) {
                    val items = allocationRepo.getItemsByAllocationOnce(alloc.id)
                    val categories = db.categoryDao().getAll().first()
                    val tabunganWajibCat = categories.find { it.name == "Tabungan Wajib" }
                    val existingSavingsWajib = if (tabunganWajibCat != null) {
                        items.find { it.categoryId == tabunganWajibCat.id }?.amount ?: 0L
                    } else 0L

                    // Sisa baru = tabungan wajib lama + diff
                    val totalTabunganWajib = existingSavingsWajib + diff

                    // Distribusi ulang ke target lain (yang masih AKTIF)
                    val activeTargets = targetRepo.getActiveTargets().first()
                    if (activeTargets.isNotEmpty()) {
                        val distribution = SavingsDistributor.distribute(totalTabunganWajib, activeTargets)
                        distribution.allocations.forEach { (targetId, allocated) ->
                            if (allocated > 0) {
                                val t = activeTargets.find { it.id == targetId } ?: return@forEach
                                val newAmount = t.currentAmount + allocated
                                db.savingsTargetDao().updateAmount(targetId, newAmount)
                                if (newAmount >= t.targetAmount && t.status == "AKTIF") {
                                    db.savingsTargetDao().updateStatus(targetId, "SIAP_DIBELI")
                                }
                            }
                        }
                    }
                }
            } else if (diff < 0) {
                // Lebih mahal: selisih negatif, ambil dari Tabungan Wajib atau Deficit
                val deficitAmount = -diff

                // Cek Tabungan Wajib tersisa
                val currentMonth = allocationRepo.getCurrentMonth()
                val alloc = allocationRepo.getAllocationByMonth(currentMonth)
                val tabunganWajibTersisa = if (alloc != null) {
                    val items = allocationRepo.getItemsByAllocationOnce(alloc.id)
                    val categories = db.categoryDao().getAll().first()
                    val tabunganWajibCat = categories.find { it.name == "Tabungan Wajib" }
                    val allTargets = targetRepo.getActiveTargets().first()
                    val totalTerpakai = allTargets.sumOf { it.currentAmount } + target.currentAmount
                    val totalWajib = if (tabunganWajibCat != null) {
                        items.find { it.categoryId == tabunganWajibCat.id }?.amount ?: 0L
                    } else 0L
                    (totalWajib - totalTerpakai).coerceAtLeast(0)
                } else 0L

                val fromTabunganWajib = minOf(deficitAmount, tabunganWajibTersisa)
                val sisaDeficit = deficitAmount - fromTabunganWajib

                if (sisaDeficit > 0) {
                    // Catat di Global Deficit
                    val deficitRepo = DeficitRepository(db.deficitDao())
                    deficitRepo.addDeficit(
                        amount = sisaDeficit,
                        targetId = target.id,
                        note = "Selisih harga ${target.name}"
                    )
                }
            }
        }
    }


}