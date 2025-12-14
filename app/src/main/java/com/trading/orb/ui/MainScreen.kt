// File: ui/MainScreen.kt (Updated with ViewModel)
package com.trading.orb.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.trading.orb.ui.components.*
import com.trading.orb.ui.navigation.*
import com.trading.orb.ui.screens.dashboard.DashboardScreen
import com.trading.orb.ui.screens.dashboard.DashboardUiState
import com.trading.orb.ui.screens.dashboard.AppState as DashboardAppState
import com.trading.orb.ui.screens.liveloggers.LiveLogsScreen
import com.trading.orb.ui.screens.more.MoreScreen
import com.trading.orb.ui.screens.positions.PositionsScreen
import com.trading.orb.ui.screens.risk.RiskScreen
import com.trading.orb.ui.screens.strategy.StrategyConfigScreen
import com.trading.orb.ui.screens.tradehistory.TradeHistoryScreen
import com.trading.orb.ui.theme.*
import com.trading.orb.ui.viewmodel.TradingViewModel
import com.trading.orb.ui.viewmodel.UiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MainScreen(
    viewModel: TradingViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect state from ViewModel
    val appState by viewModel.appState.collectAsState()
    val positions by viewModel.positions.collectAsState()
    val trades by viewModel.trades.collectAsState()
    val strategyConfig by viewModel.strategyConfig.collectAsState()

    // Handle UI events
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is UiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is UiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    OrbTradingTheme(tradingMode = appState.tradingMode) {
        Scaffold(
            topBar = {
                Column {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                    val canNavigateBack = currentRoute != Screen.Dashboard.route
                    AppTopBar(
                        title = getCurrentScreenTitle(navController),
                        tradingMode = appState.tradingMode,
                        connectionStatus = appState.connectionStatus,
                        canNavigateBack = canNavigateBack,
                        onNavigateUp = { navController.navigateUp() }
                    )
                }
            },
            bottomBar = {
                if (shouldShowBottomBar(navController)) {
                    BottomNavigationBar(navController = navController)
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            NavigationHost(
                navController = navController,
                appState = appState,
                positions = positions,
                trades = trades,
                strategyConfig = strategyConfig,
                viewModel = viewModel,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun getCurrentScreenTitle(navController: NavHostController): String {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    return when (currentRoute) {
        Screen.Dashboard.route -> "Dashboard"
        Screen.Positions.route -> "Active Positions"
        Screen.History.route -> "Trade History"
        Screen.Strategy.route -> "Strategy Config"
        Screen.More.route -> "More"
        Screen.Risk.route -> "Risk & Safety"
        Screen.Logs.route -> "Live Logs"
        else -> "ORB Trading"
    }
}

@Composable
private fun shouldShowBottomBar(navController: NavHostController): Boolean {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    return currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Positions.route,
        Screen.History.route,
        Screen.Strategy.route,
        Screen.More.route
    )
}

@Composable
private fun BottomNavigationBar(navController: NavHostController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(Screen.Dashboard.route) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun NavigationHost(
    navController: NavHostController,
    appState: com.trading.orb.data.model.AppState,
    positions: List<com.trading.orb.data.model.Position>,
    trades: List<com.trading.orb.data.model.Trade>,
    strategyConfig: com.trading.orb.data.model.StrategyConfig,
    viewModel: TradingViewModel,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route,
        modifier = modifier
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(tradingViewModel = viewModel)
        }

        composable(Screen.Positions.route) {
            PositionsScreen()
        }

        composable(Screen.History.route) {
            TradeHistoryScreen()
        }

        composable(Screen.Strategy.route) {
            StrategyConfigScreen()
        }

        composable(Screen.More.route) {
            MoreScreen(navController = navController)
        }

        composable(Screen.Risk.route) {
            RiskScreen()
        }

        composable(Screen.Logs.route) {
            LiveLogsScreen()
        }
    }
}
