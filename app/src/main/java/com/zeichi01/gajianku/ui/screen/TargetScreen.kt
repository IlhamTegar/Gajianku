package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetScreen(
    targets: List<SavingsTarget>,
    totalCollected: Long,
    onAddTarget: (String, Long, String) -> Unit,
    onDeleteTarget: (SavingsTarget) -> Unit,
    onReorder: (List<SavingsTarget>) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))

    // State lokal untuk drag-and-drop
    val localTargets = remember(targets) { targets.toMutableStateList() }
    val lazyListState = rememberLazyListState()

    // State untuk drag
    var draggingItemIndex by remember { mutableStateOf<Int?>(null) }
    var draggingItemOffset by remember { mutableStateOf(0f) }

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
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // CARD TOTAL
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Total Dana Terkumpul (Semua Target)",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = formatter.format(totalCollected),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            if (localTargets.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada target tabungan. Klik + untuk menambah.")
                    }
                }
            } else {
                itemsIndexed(
                    items = localTargets,
                    key = { _, target -> target.id }
                ) { index, target ->
                    val isDragging = draggingItemIndex == index
                    val elevation = if (isDragging) 8.dp else 2.dp

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 1f else 0f)
                            .graphicsLayer {
                                if (isDragging) {
                                    translationY = draggingItemOffset
                                }
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    target.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )

                                val priorityColor = when(target.priority) {
                                    "TINGGI" -> Color.Red
                                    "SEDANG" -> Color(0xFFFFA500)
                                    else -> Color.Green
                                }
                                Text(
                                    text = target.priority,
                                    color = priorityColor,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(end = 8.dp)
                                )

                                IconButton(onClick = { onDeleteTarget(target) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                }

                                // Drag handle manual
                                Icon(
                                    imageVector = Icons.Default.DragHandle,
                                    contentDescription = "Geser",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .pointerInput(Unit) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = {
                                                    draggingItemIndex = index
                                                    draggingItemOffset = 0f
                                                },
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    draggingItemOffset += dragAmount.y

                                                    // Hitung posisi baru
                                                    val currentIndex = draggingItemIndex ?: return@detectDragGesturesAfterLongPress
                                                    val itemHeightPx = 150f // perkiraan tinggi item
                                                    val newIndex = (currentIndex + (draggingItemOffset / itemHeightPx).toInt())
                                                        .coerceIn(0, localTargets.size - 1)

                                                    if (newIndex != currentIndex) {
                                                        // Cek prioritas sama
                                                        val fromPriority = localTargets.getOrNull(currentIndex)?.priority
                                                        val toPriority = localTargets.getOrNull(newIndex)?.priority
                                                        if (fromPriority == toPriority) {
                                                            localTargets.add(newIndex, localTargets.removeAt(currentIndex))
                                                            draggingItemIndex = newIndex
                                                            draggingItemOffset = 0f
                                                        }
                                                    }
                                                },
                                                onDragEnd = {
                                                    draggingItemIndex = null
                                                    draggingItemOffset = 0f
                                                    // Simpan urutan baru
                                                    onReorder(localTargets.toList())
                                                },
                                                onDragCancel = {
                                                    draggingItemIndex = null
                                                    draggingItemOffset = 0f
                                                }
                                            )
                                        }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Terkumpul: ${formatter.format(target.currentAmount)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Target: ${formatter.format(target.targetAmount)}", style = MaterialTheme.typography.bodySmall)

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

    // DIALOG TAMBAH TARGET
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