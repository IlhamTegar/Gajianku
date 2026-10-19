package com.zeichi01.gajianku.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zeichi01.gajianku.data.local.AppDatabase
import com.zeichi01.gajianku.data.local.entity.User
import com.zeichi01.gajianku.domain.AllocationResult
import com.zeichi01.gajianku.ui.screen.HomeScreen
import com.zeichi01.gajianku.ui.screen.PlaceholderScreen
import com.zeichi01.gajianku.ui.screen.PresetScreen
import com.zeichi01.gajianku.ui.viewmodel.PresetViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Preset : Screen("preset", "Preset", Icons.Default.List)
    object Target : Screen("target", "Target", Icons.Default.Star)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    user: User,
    allocation: AllocationResult?,
    db: AppDatabase // Tambahkan parameter db
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Preset, Screen.Target, Screen.Profile)

    // Inisialisasi PresetViewModel
    val presetViewModel: PresetViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return PresetViewModel(db) as T
            }
        }
    )

    val presets by presetViewModel.presets.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(user = user, allocation = allocation)
            }
            composable(Screen.Preset.route) {
                PresetScreen(
                    presets = presets,
                    onPresetClick = { preset ->
                        // Nanti kita arahkan ke detail preset
                        // Untuk sekarang kita biarkan kosong atau tampilkan Toast
                    }
                )
            }
            composable(Screen.Target.route) {
                PlaceholderScreen(title = "Target Tabungan")
            }
            composable(Screen.Profile.route) {
                PlaceholderScreen(title = "Profil & Tabungan Darurat")
            }
        }
    }
}