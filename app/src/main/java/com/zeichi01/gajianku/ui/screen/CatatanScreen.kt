package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
    remainingLivingCost: Long,
    totalInvestment: Long,
    onSaveExpense: (Long, Long, String) -> Unit,
    onDeleteExpense: (Expense) -> Unit
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- CARD SALDO BIAYA HIDUP (DI PALING ATAS) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KIRI: Sisa Biaya Hidup
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (remainingLivingCost < 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Sisa Biaya Hidup",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (remainingLivingCost < 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = formatter.format(remainingLivingCost),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (remainingLivingCost < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // KANAN: Total Investasi
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Total Investasi",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatter.format(totalInvestment),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (expenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada catatan pengeluaran bulan ini.")
                    }
                }
            } else {
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
                        var showDeleteConfirm by remember { mutableStateOf(false) }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(expense.note, style = MaterialTheme.typography.bodyLarge)
                                    Text(categoryName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "- ${formatter.format(expense.amount)}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    IconButton(onClick = { showDeleteConfirm = true }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }

                        // Dialog Konfirmasi Hapus
                        if (showDeleteConfirm) {
                            AlertDialog(
                                onDismissRequest = { showDeleteConfirm = false },
                                title = { Text("Hapus Catatan?") },
                                text = { Text("Apakah kamu yakin ingin menghapus catatan '${expense.note}'?") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            onDeleteExpense(expense)
                                            showDeleteConfirm = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Text("Hapus")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteConfirm = false }) { Text("Batal") }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH PENGELUARAN ---
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

// Fungsi bantuan untuk memformat tanggal
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