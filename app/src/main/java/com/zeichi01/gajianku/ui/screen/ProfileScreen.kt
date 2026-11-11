package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zeichi01.gajianku.data.local.entity.User
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    user: User,
    onNavigateToEmergencyFund: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profil") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- INFO USER ---
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Placeholder Avatar
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(user.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                            Text("Bank: ${user.bankName}", style = MaterialTheme.typography.bodyMedium)
                            Text("Gaji: ${formatter.format(user.monthlySalary)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // --- MENU ITEMS ---
            item {
                MenuItemCard(
                    title = "Tabungan Darurat",
                    subtitle = "Kelola saldo & riwayat darurat",
                    onClick = onNavigateToEmergencyFund
                )
            }

            item {
                MenuItemCard(
                    title = "Pengaturan",
                    subtitle = "Ubah bank, tanggal gajian, dll",
                    onClick = onNavigateToSettings
                )
            }

            item {
                MenuItemCard(
                    title = "Riwayat Target",
                    subtitle = "Target yang sudah dibeli",
                    onClick = onNavigateToHistory
                )
            }
        }
    }
}

@Composable
fun MenuItemCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}