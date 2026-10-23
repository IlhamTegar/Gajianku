package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.Expense
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatanScreen(
    categories: List<Category>,
    expenses: List<Expense>,
    onSaveExpense: (Long, Long, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    // Kelompokkan expenses berdasarkan tanggal (Hari)
    val groupedExpenses = expenses.groupBy {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.format(Date(it.createdAt))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Mutasi Catatan") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Catat Pengeluaran")
            }
        }
    ) { padding ->
        if (expenses.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada catatan pengeluaran bulan ini.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Iterasi setiap tanggal
                groupedExpenses.forEach { (dateString, dailyExpenses) ->
                    item {
                        Text(
                            text = formatDateHeader(dateString),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Iterasi pengeluaran di tanggal tersebut
                    items(dailyExpenses) { expense ->
                        val categoryName = categories.find { it.id == expense.categoryId }?.name ?: "Lainnya"

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(expense.note, style = MaterialTheme.typography.bodyLarge)
                                    Text(categoryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                // Karena ini pengeluaran, beri tanda minus (-)
                                Text(
                                    text = "- ${formatter.format(expense.amount)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH PENGELUARAN (Sama seperti sebelumnya) ---
    if (showDialog) {
        var selectedCategory by remember { mutableStateOf<Category?>(null) }
        var amountText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }
        var expanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Catat Pengeluaran") },
            text = {
                Column {
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory?.name ?: "Pilih Kategori",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            val filteredCategories = categories.filter {
                                it.name == "Biaya Hidup" || it.name == "Investasi"
                            }
                            filteredCategories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.name) },
                                    onClick = {
                                        selectedCategory = category
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Catatan (Misal: Makan Siang)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountText.toLongOrNull()
                    if (selectedCategory != null && amount != null && amount > 0 && note.isNotBlank()) {
                        onSaveExpense(selectedCategory!!.id, amount, note)
                        showDialog = false
                    }
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Batal") }
            }
        )
    }
}

// Fungsi bantuan untuk memformat "2026-10-23" menjadi "Kamis, 23 Oktober 2026" atau "Hari Ini"
fun formatDateHeader(dateString: String): String {
    return try {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = sdfInput.parse(dateString) ?: return dateString

        val today = Calendar.getInstance().time
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time

        val sdfToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdfToday.format(today)
        val yesterdayStr = sdfToday.format(yesterday)

        when (dateString) {
            todayStr -> "Hari Ini"
            yesterdayStr -> "Kemarin"
            else -> {
                val sdfOutput = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("in", "ID"))
                sdfOutput.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}