package com.trading.orb.ui.state

/**
 * Base UI State for all screens
 * Represents the loading, error, and success states
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
    data object Idle : UiState<Nothing>()
}

/**
 * Common UI Event for user interactions
 */
sealed class UiEvent {
    data object Retry : UiEvent()
    data object Dismiss : UiEvent()
    data class OnAction(val action: String) : UiEvent()
}

/**
 * Common loading state
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading..."
)

/**
 * Common error state
 */
data class ErrorState(
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val isRetryable: Boolean = true,
    val throwable: Throwable? = null
)

/**
 * Dashboard Screen UI State
 */
data class DashboardUiState(
    val quickStats: QuickStatsUiModel = QuickStatsUiModel(),
    val strategyStatus: StrategyStatusUiModel = StrategyStatusUiModel(),
    val orbLevels: OrbLevelsUiModel? = null,
    val recentTrades: List<RecentTradeUiModel> = emptyList(),
    val performanceMetrics: PerformanceMetricsUiModel = PerformanceMetricsUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false
)

/**
 * Strategy Config Screen UI State
 */
data class StrategyConfigUiState(
    val strategyName: String = "",
    val selectedStrategy: String = "",
    val riskPercentage: Float = 1f,
    val maxPositions: Int = 3,
    val tradingHours: TradingHoursUiModel = TradingHoursUiModel(),
    val advancedSettings: AdvancedSettingsUiModel = AdvancedSettingsUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)

/**
 * Positions Screen UI State
 */
data class PositionsUiState(
    val positions: List<PositionUiModel> = emptyList(),
    val totalOpenPositions: Int = 0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val selectedPosition: PositionUiModel? = null,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    val filterType: PositionFilterType = PositionFilterType.ALL
)

/**
 * Risk Screen UI State
 */
data class RiskUiState(
    val riskMetrics: RiskMetricsUiModel = RiskMetricsUiModel(),
    val portfolioHeatmap: List<RiskLevelUiModel> = emptyList(),
    val riskLimits: RiskLimitsUiModel = RiskLimitsUiModel(),
    val alerts: List<RiskAlertUiModel> = emptyList(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val showAlertDetails: Boolean = false,
    val selectedAlert: RiskAlertUiModel? = null
)

/**
 * Trade History Screen UI State
 */
data class TradeHistoryUiState(
    val trades: List<TradeHistoryUiModel> = emptyList(),
    val statistics: TradeStatisticsUiModel = TradeStatisticsUiModel(),
    val selectedTrade: TradeHistoryUiModel? = null,
    val filterDateRange: DateRangeUiModel = DateRangeUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    val sortType: SortType = SortType.RECENT
)

/**
 * Live Logs Screen UI State
 */
data class LiveLogsUiState(
    val logs: List<LogEntryUiModel> = emptyList(),
    val logLevel: LogLevelFilter = LogLevelFilter.ALL,
    val autoScroll: Boolean = true,
    val isPaused: Boolean = false,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val selectedLog: LogEntryUiModel? = null,
    val unreadCount: Int = 0
)

/**
 * More Screen UI State
 */
data class MoreUiState(
    val appVersion: String = "",
    val userSettings: UserSettingsUiModel = UserSettingsUiModel(),
    val aboutInfo: AboutInfoUiModel = AboutInfoUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val showSettingsDialog: Boolean = false,
    val settingsChanged: Boolean = false
)

// ============================================================================
// Enum classes for filtering and sorting
// ============================================================================

enum class PositionFilterType {
    ALL, LONG, SHORT, BREAKEVEN, PROFIT, LOSS
}

enum class LogLevelFilter {
    ALL, INFO, DEBUG, WARNING, ERROR
}

enum class SortType {
    RECENT, OLDEST, PROFIT_HIGH, PROFIT_LOW
}
