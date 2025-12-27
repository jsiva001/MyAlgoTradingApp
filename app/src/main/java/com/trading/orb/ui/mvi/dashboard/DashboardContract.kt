package com.trading.orb.ui.mvi.dashboard

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.data.model.*
import com.trading.orb.ui.utils.DEFAULT_UPTIME
import com.trading.orb.ui.utils.DialogMessages

/**
 * Dashboard UI State (MVI Pattern)
 * Represents the complete dashboard UI state at any point in time
 * 
 * Immutable data class - ensures predictable state management
 * and easier debugging
 */
data class DashboardState(
    val loading: LoadingState = LoadingState(isLoading = false),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Trading State
    val appState: AppState = AppState(),
    val activePositions: List<Position> = emptyList(),
    val closedTrades: List<Trade> = emptyList(),
    val strategyConfig: StrategyConfig? = null,
    val riskSettings: RiskSettings = RiskSettings(),
    
    // UI-specific state
    val strategyStatus: StrategyStatus = StrategyStatus.INACTIVE,
    val tradingMode: TradingMode = TradingMode.PAPER,
    val uptime: String = DEFAULT_UPTIME,
    val lastUpdated: String = ""
) : MviState

/**
 * Dashboard User Intents (MVI Pattern)
 * Represents all possible user actions and events on the dashboard
 * 
 * Sealed class ensures type-safe intent handling
 */
sealed class DashboardIntent : MviIntent {
    // Initialization & Loading
    object LoadDashboard : DashboardIntent()
    object RetryDashboard : DashboardIntent()
    object RefreshDashboard : DashboardIntent()

    // Strategy Control
    object ToggleStrategy : DashboardIntent()
    object StartStrategy : DashboardIntent()
    object StopStrategy : DashboardIntent()
    object PauseStrategy : DashboardIntent()
    object ResumeStrategy : DashboardIntent()

    // Mode Control
    object ToggleTradingMode : DashboardIntent()

    // Position Management
    data class ClosePosition(val positionId: String) : DashboardIntent()
    object CloseAllPositions : DashboardIntent()
    object EmergencyStop : DashboardIntent()

    // Configuration
    data class UpdateStrategyConfig(val config: StrategyConfig) : DashboardIntent()
    data class UpdateRiskSettings(val settings: RiskSettings) : DashboardIntent()

    // Data Updates (from repository/engine)
    data class UpdateAppState(val appState: AppState) : DashboardIntent()
    data class UpdatePositions(val positions: List<Position>) : DashboardIntent()
    data class UpdateTrades(val trades: List<Trade>) : DashboardIntent()

    // Market Data Updates
    data class OnOrbCaptured(val levels: OrbLevels) : DashboardIntent()
    data class OnPriceUpdate(val ltp: Double) : DashboardIntent()
    data class OnPositionOpened(val position: Position) : DashboardIntent()
    data class OnPositionClosed(val trade: Trade) : DashboardIntent()
    data class OnStrategyEvent(val event: StrategyEvent) : DashboardIntent()

    // Error Handling
    data class HandleError(val error: Throwable) : DashboardIntent()
}

/**
 * Dashboard Side Effects (MVI Pattern)
 * Represents one-time events that should be handled by the View
 * 
 * Effects are NOT part of the state - they are emitted once and consumed
 * Examples: Toast messages, Navigation, Dialog prompts, etc.
 */
sealed class DashboardEffect : MviEffect {
    // User Feedback
    data class ShowToast(val message: String, val isError: Boolean = false) : DashboardEffect()
    data class ShowError(val title: String = DialogMessages.ERROR, val message: String) : DashboardEffect()
    data class ShowSuccess(val message: String) : DashboardEffect()

    // Navigation
    data class NavigateTo(val screen: String, val args: Map<String, Any>? = null) : DashboardEffect()
    object NavigateBack : DashboardEffect()

    // Dialogs
    data class ShowConfirmDialog(
        val title: String,
        val message: String,
        val positiveButtonText: String = DialogMessages.OK,
        val negativeButtonText: String? = null,
        val actionId: String = ""
    ) : DashboardEffect()

    // Strategy-specific effects
    data class StrategyStarted(val message: String) : DashboardEffect()
    data class StrategyStopped(val message: String) : DashboardEffect()
    data class PositionClosed(val tradeId: String, val pnl: Double) : DashboardEffect()

    // Logging/Analytics
    data class LogEvent(val eventName: String, val params: Map<String, String> = emptyMap()) : DashboardEffect()
}
