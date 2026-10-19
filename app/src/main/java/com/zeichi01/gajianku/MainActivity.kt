package com.zeichi01.gajianku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.DatabaseSeeder
import com.zeichi01.gajianku.ui.screen.HomeScreen
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

        // Seed database
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

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    if (currentUser == null) {
                        OnboardingScreen(
                            onSave = { name, salary -> viewModel.saveOnboardingData(name, salary) }
                        )
                    } else {
                        // GANTI HomeScreen MENJADI AppNavigation
                        AppNavigation(
                            user = currentUser!!,
                            allocation = currentAllocation,
                            db = db // <-- Tambahkan ini
                        )
                    }
                }
            }
        }
    }
}