package com.zeichi01.gajianku.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onSave: (name: String, salary: Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var salaryText by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Selamat Datang di GajianKu!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mari atur keuanganmu dari sekarang.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama Kamu") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = salaryText,
                onValueChange = {
                    if (it.all { char -> char.isDigit() }) {
                        salaryText = it
                    }
                },
                label = { Text("Gaji Bulanan (Rp)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = errorMsg != null
            )

            if (errorMsg != null) {
                Text(
                    text = errorMsg!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val salary = salaryText.toLongOrNull()
                    if (name.isBlank()) {
                        errorMsg = "Nama tidak boleh kosong"
                    } else if (salary == null || salary <= 0) {
                        errorMsg = "Gaji harus berupa angka lebih dari 0"
                    } else {
                        onSave(name, salary)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mulai Sekarang")
            }
        }
    }
}