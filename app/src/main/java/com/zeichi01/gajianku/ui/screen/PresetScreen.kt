package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zeichi01.gajianku.data.local.entity.Preset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetScreen(
    presets: List<Preset>,
    onPresetClick: (Preset) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Manajemen Preset") })
        }
    ) { padding ->
        if (presets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPresetClick(preset) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = preset.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (preset.isDefault) "Preset Default" else "Preset Custom",
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