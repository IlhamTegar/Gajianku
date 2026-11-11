package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import com.zeichi01.gajianku.data.repository.AllocationRepository
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
}