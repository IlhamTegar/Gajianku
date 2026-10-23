package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.EmergencyFundHistory
import com.zeichi01.gajianku.data.repository.EmergencyFundRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    db: AppDatabase
) : ViewModel() {

    private val emergencyRepo = EmergencyFundRepository(db.emergencyFundDao())

    // Mengambil saldo darurat saat ini
    val emergencyFund = emergencyRepo.getFund()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Mengambil riwayat pemakaian darurat
    val emergencyHistory: StateFlow<List<EmergencyFundHistory>> = emergencyRepo.getAllHistory()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun useEmergencyFund(amount: Long, note: String) {
        viewModelScope.launch {
            emergencyRepo.useFunds(amount, note)
        }
    }
}