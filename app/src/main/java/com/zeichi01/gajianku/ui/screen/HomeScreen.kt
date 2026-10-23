package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeichi01.gajianku.data.local.entity.User
import com.zeichi01.gajianku.domain.AllocationResult
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    user: User,
    allocation: AllocationResult?,
    onChangePresetClick: () -> Unit
) {
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- BAGIAN 1: SAMBUTAN ---
            item {
                Text(
                    text = "Halo, ${user.name}! 👋",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gaji Bulanan: ${formatter.format(user.monthlySalary)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (allocation != null) {
                // --- BAGIAN 2: HEADER PRESET ---
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Alokasi Bulan Ini:", style = MaterialTheme.typography.titleMedium)

                        TextButton(onClick = onChangePresetClick) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Ganti Preset",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ganti Preset")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- BAGIAN 3: GRAFIK & LEGENDA (KIRI & KANAN) ---
                item {
                    val colors = listOf(
                        Color(0xFFE57373), // Merah
                        Color(0xFF64B5F6), // Biru
                        Color(0xFF81C784), // Hijau
                        Color(0xFFFFB74D), // Oranye
                        Color(0xFFBA68C8), // Ungu
                        Color(0xFF90A4AE)  // Abu-abu
                    )
                    val total = allocation.items.sumOf { it.amount }.toFloat()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically // Agar sejajar tengah
                    ) {
                        // KIRI: Grafik Donut (Ukuran diperkecil sedikit agar muat)
                        Box(
                            modifier = Modifier
                                .weight(1f) // Ambil 1 bagian ruang
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(130.dp)) {
                                var startAngle = -90f
                                val centerOffset = Offset(size.width / 2f, size.height / 2f)

                                if (total > 0f) {
                                    allocation.items.forEachIndexed { index, item ->
                                        val sweepAngle = (item.amount / total) * 360f

                                        drawArc(
                                            color = colors.getOrElse(index) { Color.Gray },
                                            startAngle = startAngle,
                                            sweepAngle = sweepAngle,
                                            useCenter = true,
                                            size = Size(size.width, size.height)
                                        )
                                        startAngle += sweepAngle
                                    }

                                    // Lubang Donut
                                    drawCircle(
                                        color = Color.White, // Sesuaikan dengan background
                                        radius = size.width / 3.5f,
                                        center = centerOffset
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // KANAN: Legenda & Persentase
                        Column(
                            modifier = Modifier.weight(1.2f), // Ambil sedikit lebih banyak ruang untuk teks
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            allocation.items.forEachIndexed { index, item ->
                                val percentage = if (total > 0) (item.amount / total * 100).toInt() else 0
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Titik Warna
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(colors.getOrElse(index) { Color.Gray }, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Nama Kategori
                                    Text(
                                        text = item.categoryName,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodySmall // Dikecilkan sedikit
                                    )

                                    // Persentase
                                    Text(
                                        text = "$percentage%",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // --- BAGIAN 4: JUDUL RINCIAN NOMINAL ---
                item {
                    Text("Rincian Nominal:", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // --- BAGIAN 5: DAFTAR KARTU NOMINAL ---
                items(allocation.items) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(item.categoryName)
                            Text(
                                text = formatter.format(item.amount),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            } else {
                item {
                    Text("Alokasi belum tersedia.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}