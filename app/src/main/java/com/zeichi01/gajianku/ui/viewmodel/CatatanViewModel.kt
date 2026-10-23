package com.zeichi01.gajianku.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.Expense
import com.zeichi01.gajianku.data.repository.ExpenseRepository
import com.zeichi01.gajianku.data.repository.PresetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// NAMA CLASS BERUBAH MENJADI CatatanViewModel
class CatatanViewModel(
    db: AppDatabase
) : ViewModel() {

    private val presetRepo = PresetRepository(db.presetDao(), db.categoryDao())
    private val expenseRepo = ExpenseRepository(db.expenseDao())

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
}