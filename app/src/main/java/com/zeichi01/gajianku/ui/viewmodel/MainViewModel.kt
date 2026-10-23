package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.local.entity.User
import com.zeichi01.gajianku.data.repository.AllocationRepository
import com.zeichi01.gajianku.data.repository.PresetRepository
import com.zeichi01.gajianku.domain.AllocationResult
import com.zeichi01.gajianku.domain.SalaryAllocator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val userDao = db.userDao()
    private val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())
    private val allocationRepo = AllocationRepository(db.allocationDao())

    // State untuk UI
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentAllocation = MutableStateFlow<AllocationResult?>(null)
    val currentAllocation: StateFlow<AllocationResult?> = _currentAllocation.asStateFlow()

    private val _availablePresets = MutableStateFlow<List<Preset>>(emptyList())
    val availablePresets: StateFlow<List<Preset>> = _availablePresets.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = userDao.getUserOnce()
            _currentUser.value = user

            // Ambil daftar preset (untuk dialog)
            val presets = presetRepo.getAllPresets().first()
            _availablePresets.value = presets

            if (user != null) {
                // Cek apakah alokasi bulan ini sudah ada di database
                // (Untuk saat ini, setiap buka app kita biarkan user memilih ulang preset)
            }

            _isLoading.value = false
        }
    }

    fun saveOnboardingData(name: String, salary: Long) {
        viewModelScope.launch {
            userDao.insert(User(name = name, monthlySalary = salary))
            loadData() // Refresh data user
        }
    }

    fun applyPreset(presetId: Long) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            val presetItems = presetRepo.getPresetItemsOnce(presetId)
            val categories = presetRepo.getAllCategories().first()

            val allocationResult = SalaryAllocator.allocate(
                salary = user.monthlySalary,
                presetItems = presetItems,
                categories = categories,
                currentDeficit = 0
            )

            val allocationItems = allocationResult.items.map { item ->
                com.zeichi01.gajianku.data.local.entity.AllocationItem(
                    allocationId = 0,
                    categoryId = item.categoryId,
                    amount = item.amount
                )
            }

            allocationRepo.saveAllocation(
                month = allocationRepo.getCurrentMonth(),
                salaryAmount = user.monthlySalary,
                presetId = presetId,
                deficitPaid = allocationResult.deficitPaid,
                items = allocationItems
            )

            _currentAllocation.value = allocationResult
        }
    }

    fun resetAllocation() {
        _currentAllocation.value = null
    }

    // Fungsi BARU: Untuk mengambil teks preview persentase saat user klik preset
    suspend fun getPresetPreview(presetId: Long): String {
        val items = presetRepo.getPresetItemsOnce(presetId)
        val categories = presetRepo.getAllCategories().first()
        val catMap = categories.associateBy { it.id }

        return items.joinToString("\n") { item ->
            val catName = catMap[item.categoryId]?.name ?: "Unknown"
            "• $catName: ${item.percentage.toInt()}%"
        }
    }
}