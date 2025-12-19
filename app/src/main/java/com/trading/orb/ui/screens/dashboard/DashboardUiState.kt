package com.trading.orb.ui.screens.dashboard

import com.trading.orb.data.model.*
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState

/**
 * UI State specific to Dashboard Screen
 * Manages all UI-related data for dashboard display
 */
data class DashboardUiState(
    val dailyStats: DailyStats = DailyStats(),
    val orbLevels: OrbLevels? = null,
    val recentTrades: List<Trade> = emptyList(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false
)

/**
 * App State - Global state shared across the app
 * Represents the overall state of the trading application
 */
data class AppState(
    val tradingMode: TradingMode = TradingMode.PAPER,
    val strategyStatus: StrategyStatus = StrategyStatus.INACTIVE,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val dailyStats: DailyStats = DailyStats(),
    val orbLevels: OrbLevels? = null,
    val strategyConfig: StrategyConfig? = null,
    val activePositions: List<Position> = emptyList(),
    val closedTrades: List<Trade> = emptyList(),
    val isLoading: Boolean = false
)
