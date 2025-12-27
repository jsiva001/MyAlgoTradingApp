package com.trading.orb.ui.mvi.dashboard

import androidx.lifecycle.viewModelScope
import com.trading.orb.BuildConfig
import com.trading.orb.data.engine.OrbStrategyEngine
import com.trading.orb.data.engine.mock.MockOrderExecutor
import com.trading.orb.data.engine.mock.MockScenarios
import com.trading.orb.data.model.*
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.utils.ErrorMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

/**
 * Dashboard ViewModel with Hybrid MVVM-MVI Architecture
 *
 * This ViewModel follows the MVI pattern for state management while maintaining
 * compatibility with MVVM patterns used in the rest of the application.
 *
 * State Flow:
 * User Action (Intent) → handleIntent() → reduce() → New State
 *                     ↓
 *                  Effect (Toast, Navigation, etc.)
 *
 * Benefits:
 * - Unidirectional data flow (predictable)
 * - All state changes go through the same path
 * - Side effects are explicit and testable
 * - Easy to debug and reason about
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<DashboardState, DashboardIntent, DashboardEffect>() {

    // Strategy engine instance
    private var strategyEngine: OrbStrategyEngine? = null

    override fun createInitialState(): DashboardState = DashboardState()

    /**
     * Pure reducer function: transforms state based on intent
     * All state transitions happen here
     */
    override fun reduce(currentState: DashboardState, intent: DashboardIntent): DashboardState {
        return when (intent) {
            // Loading intents
            is DashboardIntent.LoadDashboard -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is DashboardIntent.RetryDashboard -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is DashboardIntent.RefreshDashboard -> {
                currentState.copy(isRefreshing = true)
            }

            // Strategy control intents
            is DashboardIntent.StartStrategy -> {
                currentState.copy(
                    strategyStatus = StrategyStatus.ACTIVE,
                    loading = LoadingState(isLoading = true)
                )
            }

            is DashboardIntent.StopStrategy -> {
                currentState.copy(strategyStatus = StrategyStatus.INACTIVE)
            }

            is DashboardIntent.PauseStrategy -> {
                currentState.copy(strategyStatus = StrategyStatus.PAUSED)
            }

            is DashboardIntent.ResumeStrategy -> {
                currentState.copy(strategyStatus = StrategyStatus.ACTIVE)
            }

            is DashboardIntent.ToggleStrategy -> {
                val newStatus = when (currentState.strategyStatus) {
                    StrategyStatus.ACTIVE -> StrategyStatus.INACTIVE
                    else -> StrategyStatus.ACTIVE
                }
                currentState.copy(strategyStatus = newStatus)
            }

            // Mode control
            is DashboardIntent.ToggleTradingMode -> {
                val newMode = when (currentState.tradingMode) {
                    TradingMode.PAPER -> TradingMode.LIVE
                    TradingMode.LIVE -> TradingMode.PAPER
                }
                currentState.copy(tradingMode = newMode)
            }

            // Position management
            is DashboardIntent.ClosePosition -> currentState
            is DashboardIntent.CloseAllPositions -> currentState
            is DashboardIntent.EmergencyStop -> {
                currentState.copy(strategyStatus = StrategyStatus.INACTIVE)
            }

            // Configuration
            is DashboardIntent.UpdateStrategyConfig -> {
                currentState.copy(strategyConfig = intent.config)
            }

            is DashboardIntent.UpdateRiskSettings -> {
                currentState.copy(riskSettings = intent.settings)
            }

            // Data updates
            is DashboardIntent.UpdateAppState -> {
                currentState.copy(
                    appState = intent.appState,
                    strategyStatus = intent.appState.strategyStatus,
                    tradingMode = intent.appState.tradingMode
                )
            }

            is DashboardIntent.UpdatePositions -> {
                currentState.copy(activePositions = intent.positions)
            }

            is DashboardIntent.UpdateTrades -> {
                currentState.copy(closedTrades = intent.trades)
            }

            // Market data updates (these update state)
            is DashboardIntent.OnOrbCaptured -> currentState
            is DashboardIntent.OnPriceUpdate -> currentState
            is DashboardIntent.OnPositionOpened -> {
                currentState.copy(
                    activePositions = currentState.activePositions + intent.position
                )
            }

            is DashboardIntent.OnPositionClosed -> {
                currentState.copy(
                    activePositions = currentState.activePositions.filter { it.id != intent.trade.id },
                    closedTrades = currentState.closedTrades + intent.trade
                )
            }

            is DashboardIntent.OnStrategyEvent -> currentState

            // Error handling
            is DashboardIntent.HandleError -> {
                currentState.copy(
                    loading = LoadingState(isLoading = false),
                    error = ErrorState(
                        hasError = true,
                        errorMessage = intent.error.message ?: ErrorMessages.UNKNOWN_ERROR,
                        isRetryable = true,
                        throwable = intent.error
                    )
                )
            }
        }
    }

    /**
     * Handle intents with side effects (I/O operations, network calls, etc.)
     * Call updateState() and emitEffect() here
     */
    override suspend fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadDashboard -> loadDashboardData()
            is DashboardIntent.RetryDashboard -> loadDashboardData()
            is DashboardIntent.RefreshDashboard -> refreshDashboardData()

            is DashboardIntent.StartStrategy -> initializeAndStartMockStrategy()
            is DashboardIntent.StopStrategy -> stopStrategyEngine()
            is DashboardIntent.PauseStrategy -> updateStateImmediate(reduce(state.value, intent))
            is DashboardIntent.ResumeStrategy -> updateStateImmediate(reduce(state.value, intent))
            is DashboardIntent.ToggleStrategy -> toggleStrategyWithSideEffects()
            is DashboardIntent.ToggleTradingMode -> toggleTradingModeWithSideEffects()

            is DashboardIntent.ClosePosition -> closePositionWithSideEffects(intent.positionId)
            is DashboardIntent.CloseAllPositions -> closeAllPositionsWithSideEffects()
            is DashboardIntent.EmergencyStop -> emergencyStopWithSideEffects()

            is DashboardIntent.UpdateStrategyConfig -> updateStrategyConfigWithSideEffects(intent.config)
            is DashboardIntent.UpdateRiskSettings -> updateRiskSettingsWithSideEffects(intent.settings)

            is DashboardIntent.UpdateAppState -> updateStateImmediate(
                reduce(state.value, intent)
            )

            is DashboardIntent.UpdatePositions -> updateStateImmediate(
                reduce(state.value, intent)
            )

            is DashboardIntent.UpdateTrades -> updateStateImmediate(
                reduce(state.value, intent)
            )

            is DashboardIntent.OnPositionOpened -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(DashboardEffect.LogEvent("position_opened"))
            }

            is DashboardIntent.OnPositionClosed -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(DashboardEffect.PositionClosed(intent.trade.id, intent.trade.pnl))
            }

            is DashboardIntent.OnStrategyEvent -> handleStrategyEvent(intent.event)

            is DashboardIntent.OnOrbCaptured -> {
                emitEffect(
                    DashboardEffect.ShowSuccess(
                        String.format(DashboardEffectMessages.ORB_CAPTURED_FORMAT, intent.levels.high, intent.levels.low)
                    )
                )
            }

            is DashboardIntent.OnPriceUpdate -> {
                // Just log price updates
                Timber.d(TimberLogs.DASHBOARD_LTP_UPDATE, intent.ltp)
            }

            is DashboardIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(
                    DashboardEffect.ShowError(
                        message = intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED
                    )
                )
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadDashboardData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )
            // Simulate data loading
            kotlinx.coroutines.delay(DASHBOARD_DATA_LOAD_DELAY_MS)

            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    error = ErrorState()
                )
            )
            emitEffect(DashboardEffect.LogEvent("dashboard_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.DASHBOARD_FAILED_LOAD)
            processIntent(DashboardIntent.HandleError(e))
        }
    }

    private suspend fun refreshDashboardData() {
        try {
            kotlinx.coroutines.delay(DASHBOARD_REFRESH_DELAY_MS)
            updateStateImmediate(
                state.value.copy(isRefreshing = false)
            )
            emitEffect(DashboardEffect.ShowSuccess(DashboardEffectMessages.DASHBOARD_REFRESHED))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.DASHBOARD_FAILED_REFRESH)
            processIntent(DashboardIntent.HandleError(e))
        }
    }

    private fun isMarketOpen(): Boolean {
        val now = LocalTime.now()
        val marketOpen = LocalTime.of(MARKET_OPEN_HOUR, MARKET_OPEN_MINUTE)
        val marketClose = LocalTime.of(MARKET_CLOSE_HOUR, MARKET_CLOSE_MINUTE)
        return now in marketOpen..marketClose
    }

    private suspend fun initializeAndStartMockStrategy() {
        try {
            Timber.i(TimberLogs.DASHBOARD_INIT_MOCK_STRATEGY)

            if (!isMarketOpen() && !BuildConfig.USE_MOCK_DATA) {
                emitEffect(
                    DashboardEffect.ShowError(
                        message = DashboardEffectMessages.MARKET_CLOSED_ERROR
                    )
                )
                return
            }

            val mockDataSource = MockScenarios.successfulHighBreakout().first
            val mockExecutor = MockOrderExecutor(executionDelayMs = MOCK_STRATEGY_EXECUTION_DELAY_MS, failureRate = MOCK_STRATEGY_FAILURE_RATE)
            val defaultInstrument = Instrument(
                symbol = DEFAULT_INSTRUMENT_SYMBOL,
                exchange = DEFAULT_INSTRUMENT_EXCHANGE,
                lotSize = DEFAULT_INSTRUMENT_LOT_SIZE,
                tickSize = DEFAULT_INSTRUMENT_TICK_SIZE,
                displayName = DEFAULT_INSTRUMENT_DISPLAY_NAME
            )
            val config = state.value.strategyConfig ?: StrategyConfig(instrument = defaultInstrument)

            strategyEngine = OrbStrategyEngine(
                marketDataSource = mockDataSource,
                orderExecutor = mockExecutor,
                config = config,
                riskSettings = state.value.riskSettings
            )

            observeStrategyEvents()
            strategyEngine?.start()

            updateStateImmediate(
                state.value.copy(
                    strategyStatus = StrategyStatus.ACTIVE,
                    loading = LoadingState(isLoading = false)
                )
            )

            emitEffect(DashboardEffect.StrategyStarted(DashboardEffectMessages.STRATEGY_STARTED))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.DASHBOARD_FAILED_INIT_STRATEGY)
            processIntent(DashboardIntent.HandleError(e))
        }
    }

    private suspend fun stopStrategyEngine() {
        try {
            strategyEngine?.stop()
            updateStateImmediate(
                state.value.copy(strategyStatus = StrategyStatus.INACTIVE)
            )
            emitEffect(DashboardEffect.StrategyStopped(DashboardEffectMessages.STRATEGY_STOPPED))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.DASHBOARD_FAILED_STOP_STRATEGY)
            emitEffect(DashboardEffect.ShowError(message = String.format(ErrorMessages.FAILED_STOP_STRATEGY, e.message)))
        }
    }

    private suspend fun toggleStrategyWithSideEffects() {
        val newIntent = if (state.value.strategyStatus == StrategyStatus.ACTIVE) {
            DashboardIntent.StopStrategy
        } else {
            DashboardIntent.StartStrategy
        }
        processIntent(newIntent)
    }

    private suspend fun toggleTradingModeWithSideEffects() {
        try {
            // Close all positions before switching mode
            val positionsToClose = state.value.activePositions.toList()
            if (positionsToClose.isNotEmpty()) {
                Timber.i(TimberLogs.DASHBOARD_CLOSING_POSITIONS, positionsToClose.size)
                positionsToClose.forEach { position ->
                    processIntent(DashboardIntent.ClosePosition(position.id))
                }
            }

            repository.toggleTradingMode().onSuccess {
                val newMode = if (state.value.tradingMode == TradingMode.PAPER) "Live" else "Paper"
                emitEffect(DashboardEffect.ShowSuccess(String.format(DashboardEffectMessages.SWITCHED_TO_MODE_FORMAT, newMode)))
            }.onFailure { error ->
                emitEffect(DashboardEffect.ShowError(message = error.message ?: ErrorMessages.FAILED_TOGGLE_MODE))
            }
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.DASHBOARD_FAILED_TOGGLE_MODE)
        }
    }

    private suspend fun closePositionWithSideEffects(positionId: String) {
        try {
            val position = state.value.activePositions.find { it.id == positionId }
            if (position == null) {
                emitEffect(DashboardEffect.ShowError(message = DashboardEffectMessages.POSITION_NOT_FOUND))
                return
            }

            val trade = Trade(
                id = position.id,
                instrument = position.instrument,
                side = position.side,
                quantity = position.quantity,
                entryPrice = position.entryPrice,
                exitPrice = position.currentPrice,
                entryTime = position.entryTime,
                exitTime = java.time.LocalDateTime.now(),
                exitReason = ExitReason.MANUAL_EXIT,
                pnl = position.pnl
            )

            processIntent(DashboardIntent.OnPositionClosed(trade))
            emitEffect(
                DashboardEffect.ShowSuccess(
                    String.format(DashboardEffectMessages.POSITION_CLOSED_AT_FORMAT, trade.exitPrice)
                )
            )
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.DASHBOARD_ERROR_CLOSE_TRADE)
            emitEffect(DashboardEffect.ShowError(message = e.message ?: ErrorMessages.UNKNOWN_ERROR))
        }
    }

    private suspend fun closeAllPositionsWithSideEffects() {
        repository.closeAllPositions().onFailure { error ->
            emitEffect(DashboardEffect.ShowError(message = error.message ?: ErrorMessages.FAILED_CLOSE_ALL_POSITIONS))
        }
    }

    private suspend fun emergencyStopWithSideEffects() {
        Timber.i(TimberLogs.DASHBOARD_EMERGENCY_STOP)
        val positionsToClose = state.value.activePositions.toList()

        if (positionsToClose.isEmpty()) {
            strategyEngine?.stop()
            repository.stopStrategy()
            emitEffect(DashboardEffect.ShowSuccess(DashboardEffectMessages.EMERGENCY_STOP_EXECUTED))
            return
        }

        positionsToClose.forEach { position ->
            processIntent(DashboardIntent.ClosePosition(position.id))
        }

        strategyEngine?.stop()
        repository.stopStrategy()
        updateStateImmediate(
            state.value.copy(strategyStatus = StrategyStatus.INACTIVE)
        )

        emitEffect(DashboardEffect.ShowSuccess(DashboardEffectMessages.EMERGENCY_STOP_EXECUTED))
    }

    private suspend fun updateStrategyConfigWithSideEffects(config: StrategyConfig) {
        repository.updateStrategyConfig(config).onSuccess {
            updateStateImmediate(state.value.copy(strategyConfig = config))
            emitEffect(DashboardEffect.ShowSuccess(DashboardEffectMessages.CONFIGURATION_SAVED))
        }.onFailure { error ->
            emitEffect(DashboardEffect.ShowError(message = error.message ?: ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private suspend fun updateRiskSettingsWithSideEffects(settings: RiskSettings) {
        repository.updateRiskSettings(settings).onSuccess {
            updateStateImmediate(state.value.copy(riskSettings = settings))
            emitEffect(DashboardEffect.ShowSuccess(DashboardEffectMessages.RISK_SETTINGS_SAVED))
        }.onFailure { error ->
            emitEffect(DashboardEffect.ShowError(message = error.message ?: ErrorMessages.FAILED_SAVE_RISK_SETTINGS))
        }
    }

    private fun observeStrategyEvents() {
        viewModelScope.launch {
            strategyEngine?.events?.collect { event ->
                processIntent(DashboardIntent.OnStrategyEvent(event))
            }
        }
    }

    private suspend fun handleStrategyEvent(event: StrategyEvent) {
        when (event) {
            is StrategyEvent.Started -> {
                Timber.i(TimberLogs.STRATEGY_STARTED)
                emitEffect(DashboardEffect.StrategyStarted(DashboardEffectMessages.STRATEGY_STARTED))
            }

            is StrategyEvent.OrbCaptured -> {
                Timber.i(TimberLogs.ORB_CAPTURED, event.levels.high, event.levels.low)
                processIntent(DashboardIntent.OnOrbCaptured(event.levels))
            }

            is StrategyEvent.PriceUpdate -> {
                Timber.d(TimberLogs.LTP_UPDATE, event.ltp)
                processIntent(DashboardIntent.OnPriceUpdate(event.ltp))
            }

            is StrategyEvent.PositionOpened -> {
                Timber.i(TimberLogs.POSITION_OPENED)
                processIntent(DashboardIntent.OnPositionOpened(event.position))
                emitEffect(
                    DashboardEffect.ShowSuccess(
                        String.format(DashboardEffectMessages.POSITION_OPENED_AT_FORMAT, event.position.entryPrice)
                    )
                )
            }

            is StrategyEvent.PositionClosed -> {
                Timber.i(TimberLogs.POSITION_CLOSED)
                processIntent(DashboardIntent.OnPositionClosed(event.trade))
            }

            is StrategyEvent.Stopped -> {
                Timber.i(TimberLogs.STRATEGY_STOPPED)
                emitEffect(DashboardEffect.StrategyStopped(DashboardEffectMessages.STRATEGY_STOPPED))
            }

            is StrategyEvent.Error -> {
                Timber.e(TimberLogs.STRATEGY_ERROR, event.message)
                emitEffect(DashboardEffect.ShowError(message = String.format(ErrorMessages.STRATEGY_ERROR_FORMAT, event.message)))
            }

            is StrategyEvent.OrderFailed -> {
                Timber.e(TimberLogs.ORDER_FAILED, event.message)
                emitEffect(DashboardEffect.ShowError(message = String.format(ErrorMessages.ORDER_FAILED_FORMAT, event.message)))
            }

            is StrategyEvent.RiskLimitReached -> {
                Timber.w(TimberLogs.RISK_LIMIT_REACHED)
                emitEffect(DashboardEffect.ShowError(message = ErrorMessages.RISK_LIMIT_REACHED))
            }

            is StrategyEvent.PositionUpdate -> {
                Timber.v(DashboardEffectMessages.POSITION_UPDATE)
            }
        }
    }
}
