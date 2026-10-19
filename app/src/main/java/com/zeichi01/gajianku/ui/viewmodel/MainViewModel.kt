package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
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

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val user = userDao.getUserOnce()
            _currentUser.value = user

            if (user != null) {
                // Ambil alokasi bulan ini dari database (jika ada)
                val currentMonth = allocationRepo.getCurrentMonth()
                val existingAllocation = allocationRepo.getAllocationByMonth(currentMonth)

                // (Untuk sementara, kita hanya menampilkan alokasi yang sudah tersimpan)
                // Nanti kita akan buat fitur untuk men-generate alokasi ini.
            }

            _isLoading.value = false
        }
    }

    fun saveOnboardingData(name: String, salary: Long) {
        viewModelScope.launch {
            // 1. Simpan User
            userDao.insert(User(name = name, monthlySalary = salary))

            // 2. Ambil Preset Default
            val presets = presetRepo.getAllPresets().first()
            val defaultPreset = presets.firstOrNull { it.isDefault } ?: presets.firstOrNull()

            if (defaultPreset != null) {
                // 3. Hitung Alokasi menggunakan Logic Inti kita
                val presetItems = presetRepo.getPresetItemsOnce(defaultPreset.id)
                val categories = presetRepo.getAllCategories().first()

                val allocationResult = SalaryAllocator.allocate(
                    salary = salary,
                    presetItems = presetItems,
                    categories = categories,
                    currentDeficit = 0 // Belum ada minus di awal
                )

                // 4. Simpan hasil alokasi ke database
                val allocationItems = allocationResult.items.map { item ->
                    com.zeichi01.gajianku.data.local.entity.AllocationItem(
                        allocationId = 0, // Akan di-generate otomatis oleh insertFullAllocation
                        categoryId = item.categoryId,
                        amount = item.amount
                    )
                }

                allocationRepo.saveAllocation(
                    month = allocationRepo.getCurrentMonth(),
                    salaryAmount = salary,
                    presetId = defaultPreset.id,
                    deficitPaid = allocationResult.deficitPaid,
                    items = allocationItems
                )

                // 5. Update state agar UI langsung berubah
                _currentAllocation.value = allocationResult
            }

            loadData() // Refresh data user
        }
    }
}