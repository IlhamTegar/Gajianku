package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.Expense
import com.zeichi01.gajianku.data.repository.AllocationRepository
import com.zeichi01.gajianku.data.repository.ExpenseRepository
import com.zeichi01.gajianku.data.repository.PresetRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatatanViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())
    private val expenseRepo = ExpenseRepository(db.expenseDao())
    private val allocationRepo = AllocationRepository(db.allocationDao())

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

    // DIUBAH: Saldo biaya hidup sekarang reaktif (auto-refresh)
    @OptIn(ExperimentalCoroutinesApi::class)
    val remainingLivingCost: StateFlow<Long> = run {
        val month = allocationRepo.getCurrentMonth()
        combine(
            db.allocationDao().observeAllocationByMonth(month)
                .flatMapLatest { alloc ->
                    if (alloc == null) flowOf(emptyList())
                    else allocationRepo.getItemsByAllocation(alloc.id)
                },
            presetRepo.getAllCategories(),
            expenseRepo.getAllExpensesThisMonth()
        ) { items, cats, expenses ->
            val biayaCat = cats.find { it.name == "Biaya Hidup" } ?: return@combine 0L
            val allocated = items.find { it.categoryId == biayaCat.id }?.amount ?: 0L
            val spent = expenses.filter { it.categoryId == biayaCat.id }.sumOf { it.amount }
            allocated - spent
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )
    }

    // DIUBAH: Total investasi juga reaktif
    val totalInvestment: StateFlow<Long> = run {
        combine(
            presetRepo.getAllCategories(),
            expenseRepo.getAllExpensesThisMonth()
        ) { cats, expenses ->
            val investCat = cats.find { it.name == "Investasi" } ?: return@combine 0L
            expenses.filter { it.categoryId == investCat.id }.sumOf { it.amount }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )
    }

    fun addExpense(categoryId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            expenseRepo.addExpense(categoryId = categoryId, amount = amount, note = note)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepo.deleteExpense(expense)
        }
    }
}