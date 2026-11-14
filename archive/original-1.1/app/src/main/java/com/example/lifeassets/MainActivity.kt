package com.example.lifeassets

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifeassets.ui.AssetDetailScreen
import com.example.lifeassets.ui.AssetEditorScreen
import com.example.lifeassets.ui.AssetsHomeScreen
import com.example.lifeassets.ui.ExpenseDetailScreen
import com.example.lifeassets.ui.ExpenseEditorScreen
import com.example.lifeassets.ui.ExpensesHomeScreen
import com.example.lifeassets.ui.LifeAssetsTheme
import com.example.lifeassets.ui.MainViewModel
import com.example.lifeassets.ui.ProfileScreen

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory((application as LifeAssetsApplication).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LifeAssetsTheme {
                LifeAssetsApp(viewModel)
            }
        }
    }
}

@Composable
private fun LifeAssetsApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: Routes.ASSETS
    val showBottomBar = route in setOf(Routes.ASSETS, Routes.EXPENSES, Routes.PROFILE)

    androidx.compose.material3.Scaffold(
        containerColor = com.example.lifeassets.ui.AppBackground,
        bottomBar = {
            if (showBottomBar) {
                com.example.lifeassets.ui.BottomNavigation(
                    current = when (route) {
                        Routes.EXPENSES -> "expenses"
                        Routes.PROFILE -> "profile"
                        else -> "assets"
                    },
                    onChange = { destination ->
                        navController.navigate(destination) {
                            popUpTo(Routes.ASSETS) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ASSETS,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Routes.ASSETS) {
                AssetsHomeScreen(
                    state = state,
                    viewModel = viewModel,
                    onAdd = { navController.navigate(Routes.ASSET_NEW) },
                    onEdit = { id -> navController.navigate(Routes.assetEdit(id)) },
                    onDetail = { id -> navController.navigate(Routes.assetDetail(id)) }
                )
            }
            composable(Routes.EXPENSES) {
                ExpensesHomeScreen(
                    state = state,
                    viewModel = viewModel,
                    onAdd = { navController.navigate(Routes.EXPENSE_NEW) },
                    onEdit = { id -> navController.navigate(Routes.expenseEdit(id)) },
                    onDetail = { id -> navController.navigate(Routes.expenseDetail(id)) }
                )
            }
            composable(Routes.PROFILE) {
                ProfileScreen(viewModel = viewModel)
            }
            composable(Routes.ASSET_NEW) {
                AssetEditorScreen(null, viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.ASSET_EDIT) { entry ->
                AssetEditorScreen(entry.arguments?.getString("id"), viewModel) { navController.popBackStack() }
            }
            composable(Routes.EXPENSE_NEW) {
                ExpenseEditorScreen(null, viewModel, onBack = { navController.popBackStack() })
            }
            composable(Routes.EXPENSE_EDIT) { entry ->
                ExpenseEditorScreen(entry.arguments?.getString("id"), viewModel) { navController.popBackStack() }
            }
            composable(Routes.ASSET_DETAIL) { entry ->
                AssetDetailScreen(entry.arguments?.getString("id"), state, viewModel) { navController.popBackStack() }
            }
            composable(Routes.EXPENSE_DETAIL) { entry ->
                ExpenseDetailScreen(entry.arguments?.getString("id"), state, viewModel) { navController.popBackStack() }
            }
        }
    }
}

private object Routes {
    const val ASSETS = "assets"
    const val EXPENSES = "expenses"
    const val PROFILE = "profile"
    const val ASSET_NEW = "asset/new"
    const val ASSET_EDIT = "asset/{id}"
    const val EXPENSE_NEW = "expense/new"
    const val EXPENSE_EDIT = "expense/{id}"
    const val ASSET_DETAIL = "asset-detail/{id}"
    const val EXPENSE_DETAIL = "expense-detail/{id}"

    fun assetEdit(id: String) = "asset/$id"
    fun expenseEdit(id: String) = "expense/$id"
    fun assetDetail(id: String) = "asset-detail/$id"
    fun expenseDetail(id: String) = "expense-detail/$id"
}
