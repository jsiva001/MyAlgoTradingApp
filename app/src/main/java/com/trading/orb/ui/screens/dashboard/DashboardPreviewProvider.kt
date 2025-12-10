package com.trading.orb.ui.screens.dashboard

import com.trading.orb.data.model.*
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in DashboardScreen
 */
object DashboardPreviewProvider {
    
    fun sampleDailyStats(
        totalPnl: Double = 2450.0,
        winRate: Double = 68.0,
        activePositions: Int = 2
    ): DailyStats {
        return DailyStats(
            totalPnl = totalPnl,
            activePositions = activePositions,
            winRate = winRate
        )
    }
    
    fun sampleOrbLevels(
        symbol: String = "NIFTY24DEC22000CE",
        high: Double = 188.0,
        low: Double = 183.0,
        ltp: Double = 185.50
    ): OrbLevels {
        return OrbLevels(
            instrument = Instrument(
                symbol = symbol,
                exchange = "NSE",
                lotSize = 50,
                tickSize = 0.05,
                displayName = "NIFTY 22000 CE"
            ),
            high = high,
            low = low,
            ltp = ltp,
            breakoutBuffer = 2
        )
    }
    
    fun sampleAppState(
        tradingMode: TradingMode = TradingMode.PAPER,
        strategyStatus: StrategyStatus = StrategyStatus.ACTIVE,
        totalPnl: Double = 2450.0,
        activePositions: Int = 2,
        winRate: Double = 68.0
    ): AppState {
        return AppState(
            tradingMode = tradingMode,
            strategyStatus = strategyStatus,
            connectionStatus = ConnectionStatus.CONNECTED,
            dailyStats = sampleDailyStats(totalPnl, winRate, activePositions),
            orbLevels = sampleOrbLevels()
        )
    }
    
    fun sampleDashboardUiState(
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load dashboard data"
    ): DashboardUiState {
        return DashboardUiState(
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading dashboard..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            isRefreshing = false
        )
    }
}
