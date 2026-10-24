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
import com.zeichi01.gajianku.ui.screen.CatatanScreen
import com.zeichi01.gajianku.ui.screen.HomeScreen
import com.zeichi01.gajianku.ui.screen.PlaceholderScreen
import com.zeichi01.gajianku.ui.screen.TargetScreen
import com.zeichi01.gajianku.ui.viewmodel.TargetViewModel
import com.zeichi01.gajianku.ui.viewmodel.CatatanViewModel
import com.zeichi01.gajianku.ui.screen.ProfileScreen
import com.zeichi01.gajianku.ui.viewmodel.ProfileViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Catatan : Screen("catatan", "Catatan", Icons.Default.List)
    object Target : Screen("target", "Target", Icons.Default.Star)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    user: User,
    allocation: AllocationResult?,
    db: AppDatabase,
    remainingLivingCost: Long,
    onResetAllocation: () -> Unit
) {
    val navController = rememberNavController()
    val items = listOf(Screen.Home, Screen.Catatan, Screen.Target, Screen.Profile)

    // 1. Inisialisasi CatatanViewModel (Untuk Catatan & Kategori)
    val catatanViewModel: CatatanViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CatatanViewModel(db) as T
            }
        }
    )
    val categories by catatanViewModel.categories.collectAsStateWithLifecycle()
    val expensesThisMonth by catatanViewModel.expensesThisMonth.collectAsStateWithLifecycle()

    // 2. Inisialisasi TargetViewModel
    val targetViewModel: TargetViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return TargetViewModel(db) as T
            }
        }
    )
    val activeTargets by targetViewModel.activeTargets.collectAsStateWithLifecycle()

    val profileViewModel: ProfileViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(db) as T
            }
        }
    )
    val emergencyFund by profileViewModel.emergencyFund.collectAsStateWithLifecycle()
    val emergencyHistory by profileViewModel.emergencyHistory.collectAsStateWithLifecycle()

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
                HomeScreen(
                    user = user,
                    allocation = allocation,
                    onChangePresetClick = onResetAllocation
                )
            }
            composable(Screen.Catatan.route) {
                CatatanScreen(
                    categories = categories,
                    expenses = expensesThisMonth,
                    onSaveExpense = { categoryId, amount, note ->
                        catatanViewModel.addExpense(categoryId, amount, note)
                    }
                )
            }
            composable(Screen.Target.route) {
                TargetScreen(
                    targets = activeTargets,
                    onAddTarget = { name, amount, priority ->
                        targetViewModel.addTarget(name, amount, priority)
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    user = user,
                    emergencyFund = emergencyFund,
                    history = emergencyHistory,
                    onUseFund = { amount, note ->
                        profileViewModel.useEmergencyFund(amount, note)
                    }
                )
            }
        }
    }
}