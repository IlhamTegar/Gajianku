/*
 * CATATAN PERUBAHAN:
 * 1. Pemanggilan OnboardingScreen disesuaikan dengan parameter baru (name, bankName).
 * 2. Parameter 'salary' dihapus dari pemanggilan viewModel.saveOnboardingData.
 */
package com.zeichi01.gajianku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.DatabaseSeeder
import com.zeichi01.gajianku.ui.screen.OnboardingScreen
import com.zeichi01.gajianku.ui.theme.GajiankuTheme
import com.zeichi01.gajianku.ui.viewmodel.MainViewModel
import com.zeichi01.gajianku.ui.navigation.AppNavigation
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getInstance(this)

        lifecycleScope.launch {
            DatabaseSeeder(db).seedIfNeeded()
        }

        setContent {
            GajiankuTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return MainViewModel(db) as T
                        }
                    }
                )

                val isLoading by viewModel.isLoading.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val currentAllocation by viewModel.currentAllocation.collectAsState()
                val availablePresets by viewModel.availablePresets.collectAsState()

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (currentUser == null) {
                        OnboardingScreen(
                            onSave = { name, bankName -> viewModel.saveOnboardingData(name, bankName) }
                        )
                    } else {
                        if (currentAllocation == null && availablePresets.isNotEmpty()) {
                            var selectedPreviewId by remember { mutableStateOf<Long?>(null) }
                            var previewText by remember { mutableStateOf("") }

                            LaunchedEffect(selectedPreviewId) {
                                if (selectedPreviewId != null) {
                                    previewText = viewModel.getPresetPreview(selectedPreviewId!!)
                                } else {
                                    previewText = ""
                                }
                            }

                            AlertDialog(
                                onDismissRequest = { },
                                title = { Text("Pilih Preset Alokasi") },
                                text = {
                                    Column {
                                        availablePresets.forEach { preset ->
                                            val isSelected = selectedPreviewId == preset.id

                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clickable { selectedPreviewId = preset.id }
                                                    .then(
                                                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                                        else Modifier
                                                    ),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
                                                    Text(preset.name, style = MaterialTheme.typography.titleMedium)

                                                    if (isSelected) {
                                                        Spacer(modifier = Modifier.padding(top = 8.dp))
                                                        Text(previewText, style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { selectedPreviewId?.let { viewModel.applyPreset(it) } },
                                        enabled = selectedPreviewId != null
                                    ) {
                                        Text("Terapkan")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { }) {
                                        Text("Kembali", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }
                            )
                        } else {
                            AppNavigation(
                                user = currentUser!!,
                                allocation = currentAllocation,
                                db = db,
                                remainingLivingCost = 0L,
                                onResetAllocation = { viewModel.resetAllocation() }
                            )
                        }
                    }
                }
            }
        }
    }
}