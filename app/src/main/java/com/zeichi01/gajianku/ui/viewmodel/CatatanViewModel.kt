package com.zeichi01.gajianku.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.Expense
import com.zeichi01.gajianku.data.repository.AllocationRepository
import com.zeichi01.gajianku.data.repository.ExpenseRepository
import com.zeichi01.gajianku.data.repository.PresetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// NAMA CLASS BERUBAH MENJADI CatatanViewModel
class CatatanViewModel(
    db: AppDatabase
) : ViewModel() {

    private val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())
    private val expenseRepo = ExpenseRepository(db.expenseDao())
    private val allocationRepo = AllocationRepository(db.allocationDao())
    // Tambahkan State untuk Sisa Saldo Biaya Hidup
    private val _remainingLivingCost = MutableStateFlow<Long>(0L)
    val remainingLivingCost: StateFlow<Long> = _remainingLivingCost.asStateFlow()
    private val _totalInvestment = MutableStateFlow<Long>(0L)
    val totalInvestment: StateFlow<Long> = _totalInvestment.asStateFlow()

    init {
        // Panggil fungsi hitung saat ViewModel dibuat
        calculateRemainingLivingCost()
    }

    val categories: StateFlow<List<Category>> = presetRepo.getAllCategories()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expensesThisMonth: StateFlow<List<Expense>> = expenseRepo.getAllExpensesThisMonth()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addExpense(categoryId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            expenseRepo.addExpense(
                categoryId = categoryId,
                amount = amount,
                note = note
            )
        }
    }

    // Di dalam class CatatanViewModel

    fun calculateRemainingLivingCost() {
        viewModelScope.launch {
            val currentMonth = allocationRepo.getCurrentMonth()
            val allocation = allocationRepo.getAllocationByMonth(currentMonth) ?: return@launch

            val items = allocationRepo.getItemsByAllocationOnce(allocation.id)
            val categories = presetRepo.getAllCategories().first()

            val biayaHidupCat = categories.find { it.name == "Biaya Hidup" } ?: return@launch
            val allocatedAmount = items.find { it.categoryId == biayaHidupCat.id }?.amount ?: 0L

            // TAMBAHKAN LOG INI UNTUK DEBUG
            Log.d("CatatanVM", "Alokasi Biaya Hidup dari DB: $allocatedAmount")

            val expenses = expenseRepo.getTotalByCategory(currentMonth, biayaHidupCat.id).first()

            // TAMBAHKAN LOG INI UNTUK DEBUG
            Log.d("CatatanVM", "Total Pengeluaran: $expenses")

            _remainingLivingCost.value = allocatedAmount - expenses
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepo.deleteExpense(expense)
            calculateRemainingLivingCost() // Hitung ulang saldo setelah dihapus
        }
    }
}