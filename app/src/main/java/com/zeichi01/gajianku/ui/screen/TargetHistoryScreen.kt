package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetHistoryScreen(
    targets: List<SavingsTarget>,
    onBackClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Target") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (targets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada target selesai.")
                    }
                }
            } else {
                items(targets) { target ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(target.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Harga Target: ${formatter.format(target.targetAmount)}", style = MaterialTheme.typography.bodySmall)
                            Text("Harga Aktual: ${formatter.format(target.actualPrice ?: 0L)}", style = MaterialTheme.typography.bodySmall)

                            val selisih = (target.actualPrice ?: 0L) - target.targetAmount
                            val selisihColor = if (selisih > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            Text(
                                text = "Selisih: ${if (selisih >= 0) "+" else ""}${formatter.format(selisih)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = selisihColor,
                                fontWeight = FontWeight.Bold
                            )

                            target.completedAt?.let { completedAt ->
                                val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
                                Text(
                                    text = "Dibeli: ${sdf.format(Date(completedAt))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}