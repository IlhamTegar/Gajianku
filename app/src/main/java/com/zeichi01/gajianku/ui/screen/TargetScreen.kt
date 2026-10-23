package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetScreen(
    targets: List<SavingsTarget>,
    onAddTarget: (String, Long, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Target Tabungan") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Target")
            }
        }
    ) { padding ->
        if (targets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Belum ada target tabungan. Klik + untuk menambah.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(targets) { target ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(target.name, style = MaterialTheme.typography.titleMedium)
                                // Tampilkan Label Prioritas
                                val priorityColor = when(target.priority) {
                                    "TINGGI" -> Color.Red
                                    "SEDANG" -> Color(0xFFFFA500) // Oranye
                                    else -> Color.Green
                                }
                                Text(
                                    text = target.priority,
                                    color = priorityColor,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Terkumpul: ${formatter.format(target.currentAmount)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Target: ${formatter.format(target.targetAmount)}", style = MaterialTheme.typography.bodySmall)

                            // Progress Bar sederhana
                            val progress = (target.currentAmount.toFloat() / target.targetAmount.toFloat()).coerceIn(0f, 1f)
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- DIALOG TAMBAH TARGET ---
    if (showDialog) {
        var name by remember { mutableStateOf("") }
        var amountText by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("SEDANG") }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Tambah Target Baru") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Barang/Target") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                        label = { Text("Target Dana (Rp)") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Prioritas:", style = MaterialTheme.typography.bodyMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = priority == "TINGGI", onClick = { priority = "TINGGI" })
                        Text("Tinggi")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = priority == "SEDANG", onClick = { priority = "SEDANG" })
                        Text("Sedang")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = priority == "RENDAH", onClick = { priority = "RENDAH" })
                        Text("Rendah")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val amount = amountText.toLongOrNull()
                    if (name.isNotBlank() && amount != null && amount > 0) {
                        onAddTarget(name, amount, priority)
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