package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.repository.PresetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PresetViewModel(
    db: AppDatabase
) : ViewModel() {

    private val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())

    // Mengambil daftar preset sebagai StateFlow agar UI otomatis update
    val presets: StateFlow<List<Preset>> = presetRepo.getAllPresets()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}