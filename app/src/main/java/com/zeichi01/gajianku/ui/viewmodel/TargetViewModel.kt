package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import com.zeichi01.gajianku.data.repository.SavingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TargetViewModel(
    db: AppDatabase
) : ViewModel() {

    private val targetRepo = SavingsRepository(db.savingsTargetDao())

    // Mengambil daftar target aktif
    val activeTargets: StateFlow<List<SavingsTarget>> = targetRepo.getActiveTargets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTarget(name: String, amount: Long, priority: String) {
        viewModelScope.launch {
            targetRepo.addTarget(
                name = name,
                targetAmount = amount,
                priority = priority
            )
        }
    }

    // Di TargetViewModel.kt
    fun updateTargetOrder(reorderedTargets: List<SavingsTarget>) {
        viewModelScope.launch {
            reorderedTargets.forEachIndexed { index, target ->
                val updatedTarget = target.copy(sortOrder = index)
                targetRepo.updateTarget(updatedTarget) // Pastikan fungsi ini ada di Repository
            }
        }
    }

    // Di dalam class TargetViewModel, tambahkan:
    val totalCollected: StateFlow<Long> = activeTargets
        .map { targets -> targets.sumOf { it.currentAmount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun deleteTarget(target: SavingsTarget) {
        viewModelScope.launch {
            targetRepo.deleteTarget(target)
        }
    }
}