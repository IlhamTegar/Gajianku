package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zeichi01.gajianku.data.local.entity.EmergencyFund
import com.zeichi01.gajianku.data.local.entity.EmergencyFundHistory
import com.zeichi01.gajianku.data.local.entity.User
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    emergencyFund: EmergencyFund?,
    history: List<EmergencyFundHistory>,
    onUseFund: (Long, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profil & Darurat") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- INFO USER ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nama: ${user.name}", style = MaterialTheme.typography.titleMedium)
                        Text("Gaji: ${formatter.format(user.monthlySalary)}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // --- KARTU TABUNGAN DARURAT ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Tabungan Darurat", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onErrorContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = formatter.format(emergencyFund?.currentBalance ?: 0L),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Pakai Dana Darurat")
                        }
                    }
                }
            }

            // --- HISTORY PEMAKAIAN DARURAT ---
            item {
                Text("Riwayat Pemakaian Darurat", style = MaterialTheme.typography.titleMedium)
            }

            if (history.isEmpty()) {
                item {
                    Text("Belum ada riwayat pemakaian.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                items(history) { item ->
                    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
                    val dateStr = sdf.format(Date(item.createdAt))
                    val isUsage = item.type == "PAKAI"

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(item.note, style = MaterialTheme.typography.bodyLarge)
                                Text(dateStr, style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = if (isUsage) "- ${formatter.format(item.amount)}" else "+ ${formatter.format(item.amount)}",
                                color = if (isUsage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG PAKAI DARURAT ---
    if (showDialog) {
        var amountText by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Pakai Dana Darurat") },
            text = {
                Column {
                    Text("Gunakan hanya untuk keadaan mendesak!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
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
                        label = { Text("Untuk apa? (Misal: Ban bocor)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountText.toLongOrNull()
                        if (amount != null && amount > 0 && note.isNotBlank()) {
                            onUseFund(amount, note)
                            showDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Gunakan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Batal") }
            }
        )
    }
}