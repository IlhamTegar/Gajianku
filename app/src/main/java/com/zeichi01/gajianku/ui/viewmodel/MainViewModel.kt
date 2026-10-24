/*
 * CATATAN PERUBAHAN:
 * 1. Fungsi saveOnboardingData sekarang menerima (name: String, bankName: String).
 * 2. Gaji (monthlySalary) di-set ke 0 untuk sementara karena akan diisi otomatis oleh sistem deteksi nanti.
 * 3. Parameter bankName dimasukkan ke dalam entity User.
 */
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

            val presets = presetRepo.getAllPresets().first()
            _availablePresets.value = presets

            if (user != null && user.monthlySalary > 0) {
                // Nanti di sini kita akan memuat alokasi yang sudah ada
                // Saat ini dibiarkan kosong karena gaji belum terdeteksi
            }

            _isLoading.value = false
        }
    }

    fun saveOnboardingData(name: String, bankName: String) {
        viewModelScope.launch {
            // Simpan user dengan gaji 0 sementara (menunggu deteksi notifikasi)
            userDao.insert(User(name = name, monthlySalary = 0, bankName = bankName))
            loadData()
        }
    }

    fun applyPreset(presetId: Long) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            // Karena gaji masih 0, kita buat alokasi dummy untuk preview UI
            val salaryToAllocate = if (user.monthlySalary > 0) user.monthlySalary else 10000000L

            val presetItems = presetRepo.getPresetItemsOnce(presetId)
            val categories = presetRepo.getAllCategories().first()

            val allocationResult = SalaryAllocator.allocate(
                salary = salaryToAllocate,
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

            // Hanya simpan ke DB jika gaji sudah terdeteksi (bukan dummy)
            if (user.monthlySalary > 0) {
                allocationRepo.saveAllocation(
                    month = allocationRepo.getCurrentMonth(),
                    salaryAmount = user.monthlySalary,
                    presetId = presetId,
                    deficitPaid = allocationResult.deficitPaid,
                    items = allocationItems
                )
            }

            _currentAllocation.value = allocationResult
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