package com.zeichi01.gajianku.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.local.entity.User
import com.zeichi01.gajianku.data.repository.AllocationRepository
import com.zeichi01.gajianku.data.repository.PresetRepository
import com.zeichi01.gajianku.domain.AllocationResult
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

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _currentAllocation = MutableStateFlow<AllocationResult?>(null)
    val currentAllocation: StateFlow<AllocationResult?> = _currentAllocation.asStateFlow()

    private val _availablePresets = MutableStateFlow<List<Preset>>(emptyList())
    val availablePresets: StateFlow<List<Preset>> = _availablePresets.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeData()
    }

    // BARU: Reaktif memantau perubahan database
    private fun observeData() {
        // Pantau user (gaji, activePresetId)
        viewModelScope.launch {
            userDao.observeUser().collect { user ->
                _currentUser.value = user
            }
        }
        // Pantau preset
        viewModelScope.launch {
            presetRepo.getAllPresets().collect { presets ->
                _availablePresets.value = presets
            }
        }
        // Pantau alokasi bulan ini
        viewModelScope.launch {
            val currentMonth = allocationRepo.getCurrentMonth()
            db.allocationDao().observeAllocationByMonth(currentMonth).collect { alloc ->
                if (alloc == null) {
                    _currentAllocation.value = null
                } else {
                    val items = allocationRepo.getItemsByAllocationOnce(alloc.id)
                    val categories = presetRepo.getAllCategories().first()
                    val allocatedItems = items.map { item ->
                        val cat = categories.find { it.id == item.categoryId }
                        com.zeichi01.gajianku.domain.AllocatedItem(
                            categoryId = item.categoryId,
                            categoryName = cat?.name ?: "Unknown",
                            percentage = 0f,
                            amount = item.amount,
                            isSavingsPocket = cat?.isSavingsPocket ?: false
                        )
                    }
                    val savingsWajib = allocatedItems.find { it.categoryName == "Tabungan Wajib" }?.amount ?: 0L
                    val savingsDarurat = allocatedItems.find { it.categoryName == "Tabungan Darurat" }?.amount ?: 0L
                    _currentAllocation.value = AllocationResult(
                        salaryAmount = alloc.salaryAmount,
                        items = allocatedItems,
                        totalSavingsPocket = savingsWajib + savingsDarurat,
                        savingsWajibAmount = savingsWajib,
                        savingsDaruratAmount = savingsDarurat,
                        deficitPaid = alloc.deficitPaid
                    )
                }
                _isLoading.value = false
            }
        }
    }

    fun saveOnboardingData(name: String, bankName: String) {
        viewModelScope.launch {
            userDao.insert(User(name = name, monthlySalary = 0, bankName = bankName))
        }
    }

    fun applyPreset(presetId: Long) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            userDao.update(user.copy(activePresetId = presetId))
            Log.d("MainVM", "Preset $presetId disimpan")
        }
    }

    fun resetAllocation() {
        _currentAllocation.value = null
    }

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