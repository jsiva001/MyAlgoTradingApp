package com.trading.orb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.BuildConfig
import com.trading.orb.data.engine.OrbStrategyEngine
import com.trading.orb.data.engine.mock.MockOrderExecutor
import com.trading.orb.data.engine.mock.MockScenarios
import com.trading.orb.data.model.*
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.screens.dashboard.DashboardUiState
import com.trading.orb.ui.state.HistoryFilter
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // 🆕 ADD: Strategy engine
    private var strategyEngine: OrbStrategyEngine? = null

    // State flows from repository
    val appState: StateFlow<AppState> = repository.appState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppState()
        )

    val positions: StateFlow<List<Position>> = repository.positions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trades: StateFlow<List<Trade>> = repository.trades
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val strategyConfig: StateFlow<StrategyConfig> = repository.strategyConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StrategyConfig(
                instrument = Instrument(
                    symbol = "NIFTY24DEC22000CE",
                    exchange = "NSE",
                    lotSize = 50,
                    tickSize = 0.05,
                    displayName = "NIFTY 22000 CE"
                )
            )
        )

    val riskSettings: StateFlow<RiskSettings> = repository.riskSettings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = RiskSettings()
        )

    // Dashboard UI State
    private val _dashboardUiState = MutableStateFlow(DashboardUiState())
    val dashboardUiState: StateFlow<DashboardUiState> = _dashboardUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    init {
        loadDashboard()
    }

    // 🆕 ADD: Check if market is open
    private fun isMarketOpen(): Boolean {
        val now = LocalTime.now()
        val marketOpen = LocalTime.of(9, 15)
        val marketClose = LocalTime.of(15, 30)
        return now in marketOpen..marketClose
    }

    // 🆕 ADD: Initialize and start mock engine
    fun initializeAndStartMockStrategy(scenario: String = "normal") {
        Timber.i("🧪 Initializing MOCK ORB Strategy Engine - Scenario: $scenario")
        
        viewModelScope.launch {
            try {
                // Check if market is open (or using mock)
                if (!isMarketOpen() && !BuildConfig.USE_MOCK_DATA) {
                    Timber.w("⚠️ Market is closed! Cannot initiate strategy in real mode")
                    _uiEvent.emit(UiEvent.ShowError("❌ Market is closed (9:15 AM - 3:30 PM IST). Cannot initiate strategy!"))
                    return@launch
                }

                if (BuildConfig.USE_MOCK_DATA) {
                    Timber.i("✅ Using MOCK DATA - Market time validation skipped")
                }

                // Create mock data source and config based on scenario
                val mockDataSource = when (scenario) {
                    "high_breakout" -> MockScenarios.successfulHighBreakout().first
                    "stop_loss" -> MockScenarios.stopLossScenario().first
                    else -> MockScenarios.successfulHighBreakout().first
                }

                // Create mock order executor
                val mockExecutor = MockOrderExecutor(executionDelayMs = 500, failureRate = 0)

                // Get strategy config from repository
                val config = strategyConfig.value

                // Initialize ORB Strategy Engine
                strategyEngine = OrbStrategyEngine(
                    marketDataSource = mockDataSource,
                    orderExecutor = mockExecutor,
                    config = config,
                    riskSettings = riskSettings.value
                )

                Timber.i("✅ MOCK Strategy Engine initialized successfully")

                // Start observing strategy events
                observeStrategyEvents()

                // Start the strategy
                strategyEngine?.start()
                Timber.i("✅ MOCK Strategy started!")
                Timber.i("📊 Market Data Source: ${mockDataSource.javaClass.simpleName}")
                Timber.i("🎯 Trading Symbol: ${config.instrument.symbol} | Lot Size: ${config.lotSize}")
                Timber.i("⏰ ORB Window: ${config.orbStartTime} - ${config.orbEndTime}")
                Timber.i("💰 Stop Loss: ${config.stopLossPoints} points | Target: ${config.targetPoints} points")
                
                // Update repository state to ACTIVE
                repository.startStrategy().onSuccess {
                    Timber.i("✅ Repository strategy state updated to ACTIVE")
                    _uiEvent.emit(UiEvent.ShowSuccess("Strategy started successfully!"))
                }
                
            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to initialize mock strategy")
                _dashboardUiState.update {
                    it.copy(
                        error = ErrorState(
                            hasError = true,
                            errorMessage = "Failed to start strategy: ${e.message}",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
                _uiEvent.emit(UiEvent.ShowError("Failed to start strategy: ${e.message}"))
            }
        }
    }

    // 🆕 ADD: Observe strategy events and update UI
    private fun observeStrategyEvents() {
        viewModelScope.launch {
            strategyEngine?.events?.collect { event ->
                handleStrategyEvent(event)
            }
        }
    }

    // 🆕 ADD: Handle strategy events and update dashboard UI
    private suspend fun handleStrategyEvent(event: StrategyEvent) {
        Timber.i("📊 Strategy Event: ${event.javaClass.simpleName}")
        
        when (event) {
            is StrategyEvent.Started -> {
                Timber.i("🟢 Strategy Started - Symbol: ${event.config.instrument.symbol}")
                _uiEvent.emit(UiEvent.ShowSuccess("Strategy started successfully!"))
            }
            
            is StrategyEvent.OrbCaptured -> {
                Timber.i("📈 ORB Captured - High: ${event.levels.high}, Low: ${event.levels.low}")
                Timber.i("🎯 Buy Trigger: ₹${String.format("%.2f", event.levels.buyTrigger)} | Sell Trigger: ₹${String.format("%.2f", event.levels.sellTrigger)}")
                
                repository.updateAppState(
                    appState.value.copy(orbLevels = event.levels)
                ).onSuccess {
                    Timber.d("✅ ORB levels updated in repository")
                }.onFailure { error ->
                    Timber.e(error, "Failed to update ORB levels in repository")
                }
                
                _uiEvent.emit(UiEvent.ShowSuccess("ORB Levels Captured! High: ₹${String.format("%.2f", event.levels.high)}, Low: ₹${String.format("%.2f", event.levels.low)}"))
            }
            
            is StrategyEvent.PriceUpdate -> {
                Timber.d("💹 LTP Update: ₹${String.format("%.2f", event.ltp)}")
                
                val currentOrbLevels = appState.value.orbLevels
                val instrument = appState.value.strategyConfig?.instrument
                val breakoutBuffer = appState.value.strategyConfig?.breakoutBuffer ?: 0
                
                if (currentOrbLevels != null) {
                    // If ORB is already captured, update the LTP in orbLevels
                    val updatedOrbLevels = currentOrbLevels.copy(ltp = event.ltp)
                    repository.updateAppState(
                        appState.value.copy(orbLevels = updatedOrbLevels)
                    )
                } else if (instrument != null) {
                    // Before ORB capture, create a temporary OrbLevels with only LTP
                    val tempOrbLevels = OrbLevels(
                        instrument = instrument,
                        high = 0.0,
                        low = 0.0,
                        ltp = event.ltp,
                        breakoutBuffer = breakoutBuffer
                    )
                    repository.updateAppState(
                        appState.value.copy(orbLevels = tempOrbLevels)
                    )
                }
            }
            
            is StrategyEvent.PositionOpened -> {
                Timber.i("🟢 Position Opened - Side: ${event.position.side}, Entry Price: ₹${String.format("%.2f", event.position.entryPrice)}, SL: ₹${String.format("%.2f", event.position.stopLoss)}, Target: ₹${String.format("%.2f", event.position.target)}")
                
                // Use appState.activePositions as source of truth
                val updatedPositions = appState.value.activePositions + event.position
                
                // Update dailyStats with new position count
                val updatedDailyStats = appState.value.dailyStats.copy(
                    activePositions = updatedPositions.size
                )
                
                repository.updateAppState(
                    appState.value.copy(
                        activePositions = updatedPositions,
                        dailyStats = updatedDailyStats
                    )
                ).onSuccess {
                    Timber.d("✅ Position added to repository. Total active positions: ${updatedPositions.size}")
                }.onFailure { error ->
                    Timber.e(error, "Failed to update position in repository")
                }
                
                _uiEvent.emit(UiEvent.ShowSuccess("Position opened at ₹${String.format("%.2f", event.position.entryPrice)}"))
            }
            
            is StrategyEvent.PositionUpdate -> {
                Timber.v("💹 Position Update - Current Price: ₹${String.format("%.2f", event.position.currentPrice)} | P&L: ₹${String.format("%.2f", event.position.currentPrice - event.position.entryPrice)}")
                
                // Update active positions with latest position data
                val updatedPositions = appState.value.activePositions.map { pos ->
                    if (pos.id == event.position.id) event.position else pos
                }
                
                // Update orbLevels with latest LTP
                val updatedOrbLevels = appState.value.orbLevels?.copy(ltp = event.position.currentPrice)
                
                // Calculate updated P&L for all positions
                val totalPnl = updatedPositions.sumOf { it.pnl }
                
                // Update dailyStats with P&L and active position count
                val updatedDailyStats = appState.value.dailyStats.copy(
                    totalPnl = totalPnl,
                    activePositions = updatedPositions.size
                )
                
                repository.updateAppState(
                    appState.value.copy(
                        activePositions = updatedPositions,
                        orbLevels = updatedOrbLevels,
                        dailyStats = updatedDailyStats
                    )
                )
            }
            
            is StrategyEvent.PositionClosed -> {
                Timber.i("🏁 Position Closed - Exit Price: ₹${String.format("%.2f", event.trade.exitPrice)}, Reason: ${event.trade.exitReason}, P&L: ₹${String.format("%.2f", event.trade.pnl)}")
                
                val updatedPositions = appState.value.activePositions.filter { it.id != event.trade.id }
                val updatedTrades = trades.value + event.trade
                
                // Update daily stats with new active positions count
                val totalPnl = appState.value.dailyStats.totalPnl + event.trade.pnl
                val totalTrades = appState.value.dailyStats.totalTrades + 1
                val winningTrades = appState.value.dailyStats.winningTrades + (if (event.trade.pnl > 0) 1 else 0)
                val losingTrades = appState.value.dailyStats.losingTrades + (if (event.trade.pnl < 0) 1 else 0)
                val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100 else 0.0
                
                val updatedDailyStats = appState.value.dailyStats.copy(
                    totalPnl = totalPnl,
                    activePositions = updatedPositions.size,
                    totalTrades = totalTrades,
                    winningTrades = winningTrades,
                    losingTrades = losingTrades,
                    winRate = winRate
                )
                
                repository.updateAppState(
                    appState.value.copy(
                        activePositions = updatedPositions,
                        closedTrades = updatedTrades,
                        dailyStats = updatedDailyStats
                    )
                ).onSuccess {
                    Timber.d("✅ Trade closed and updated in repository")
                }.onFailure { error ->
                    Timber.e(error, "Failed to update closed trade in repository")
                }
                
                val pnlText = if (event.trade.pnl >= 0) "+₹" else "₹"
                _uiEvent.emit(UiEvent.ShowSuccess("Trade closed with P&L: $pnlText${String.format("%.0f", event.trade.pnl)}"))
            }
            
            is StrategyEvent.Stopped -> {
                Timber.i("⏹️ Strategy Stopped")
                _uiEvent.emit(UiEvent.ShowSuccess("Strategy stopped"))
            }
            
            is StrategyEvent.Error -> {
                Timber.e("❌ Strategy Error: ${event.message}")
                _uiEvent.emit(UiEvent.ShowError("Strategy error: ${event.message}"))
            }
            
            is StrategyEvent.OrderFailed -> {
                Timber.e("❌ Order Failed: ${event.message}")
                _uiEvent.emit(UiEvent.ShowError("Order failed: ${event.message}"))
            }
            
            is StrategyEvent.RiskLimitReached -> {
                Timber.w("⚠️ Risk Limit Reached")
                _uiEvent.emit(UiEvent.ShowError("Risk limit reached - strategy paused"))
            }
        }
    }

    // 🆕 ADD: Start strategy (after initialization)
    fun startStrategy() {
        Timber.i("Starting strategy...")
        viewModelScope.launch {
            try {
                if (strategyEngine == null) {
                    // If engine not initialized, initialize first
                    initializeAndStartMockStrategy()
                } else {
                    // Otherwise just start
                    strategyEngine?.start()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to start strategy")
                _uiEvent.emit(UiEvent.ShowError("Failed to start strategy: ${e.message}"))
            }
        }
    }

    // 🆕 ADD: Stop strategy
    fun stopStrategy() {
        Timber.i("Stopping strategy...")
        viewModelScope.launch {
            try {
                strategyEngine?.stop()
                Timber.i("Strategy stopped")
                _uiEvent.emit(UiEvent.ShowSuccess("Strategy stopped"))
            } catch (e: Exception) {
                Timber.e(e, "Failed to stop strategy")
                _uiEvent.emit(UiEvent.ShowError("Failed to stop strategy: ${e.message}"))
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _dashboardUiState.update { it.copy(loading = LoadingState(isLoading = true)) }
            try {
                // Simulate data loading - in reality, data comes from repository flows
                // The dashboard data is continuously streamed from repository.appState
                _dashboardUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _dashboardUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load dashboard",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    fun retryDashboard() {
        loadDashboard()
    }

    /**
     * Refresh dashboard data
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _dashboardUiState.update { it.copy(isRefreshing = true) }
            try {
                // Trigger repository refresh - simulate with delay
                kotlinx.coroutines.delay(1000)
                _dashboardUiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _dashboardUiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to refresh",
                            isRetryable = true
                        )
                    )
                }
            }
        }
    }

    // Actions
    fun toggleStrategy() {
        viewModelScope.launch {
            if (appState.value.strategyStatus == StrategyStatus.ACTIVE) {
                // Stop the strategy
                Timber.i("🛑 Stopping strategy...")
                strategyEngine?.stop()
                repository.stopStrategy().onSuccess {
                    Timber.i("✅ Strategy stopped successfully")
                    _uiEvent.emit(UiEvent.ShowSuccess("Strategy stopped"))
                }.onFailure { error ->
                    Timber.e("Failed to stop strategy: ${error.message}")
                    _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to stop strategy"))
                }
            } else {
                // Start the strategy with mock engine
                Timber.i("🚀 Starting MOCK ORB Strategy...")
                initializeAndStartMockStrategy("normal")
            }
        }
    }

    fun toggleTradingMode() {
        viewModelScope.launch {
            Timber.i("🔄 Toggling trading mode...")
            
            // First, close all active positions at market price
            val positionsToClose = appState.value.activePositions.toList()
            
            if (positionsToClose.isNotEmpty()) {
                Timber.i("Closing ${positionsToClose.size} position(s) before mode switch")
                
                // Close each position with market order
                positionsToClose.forEach { position ->
                    closeTradeAtMarketPrice(position.id, ExitReason.MANUAL_EXIT)
                }
            }
            
            // Then toggle the trading mode
            repository.toggleTradingMode().onSuccess {
                val newMode = if (appState.value.tradingMode == TradingMode.PAPER) "Live" else "Paper"
                Timber.i("✅ Switched to $newMode mode")
                _uiEvent.emit(UiEvent.ShowSuccess("Switched to $newMode mode"))
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to toggle mode"))
            }
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            Timber.i("🚨 EMERGENCY STOP triggered!")
            
            // Close all active positions at market price
            val positionsToClose = appState.value.activePositions.toList()
            
            if (positionsToClose.isEmpty()) {
                Timber.i("No active positions to close")
                _uiEvent.emit(UiEvent.ShowSuccess("Emergency stop executed - no active positions"))
                
                // Stop strategy engine
                strategyEngine?.stop()
                repository.stopStrategy()
                return@launch
            }

            // Close each position with market order
            positionsToClose.forEach { position ->
                closeTradeAtMarketPrice(position.id, ExitReason.EMERGENCY_EXIT)
            }
            
            // Stop strategy engine
            strategyEngine?.stop()
            repository.stopStrategy()
            
            Timber.i("✅ Emergency stop executed - ${positionsToClose.size} position(s) closed")
        }
    }

    fun closePosition(positionId: String) {
        viewModelScope.launch {
            // Close with market order (current LTP)
            closeTradeAtMarketPrice(positionId, ExitReason.MANUAL_EXIT)
        }
    }

    /**
     * Common method to close a trade at current market price
     * Can be called from any screen in the app
     * Accessible: Emergency Stop, Close Position, Manual Exit
     */
    fun closeTradeAtMarketPrice(positionId: String, reason: ExitReason = ExitReason.MANUAL_EXIT) {
        viewModelScope.launch {
            Timber.d("🔍 closeTradeAtMarketPrice called: positionId=$positionId, reason=$reason")
            Timber.d("📊 Current appState.activePositions count: ${appState.value.activePositions.size}")
            Timber.d("📊 All active position IDs: ${appState.value.activePositions.map { "${it.id} (${it.instrument.symbol})" }}")
            try {
                val position = appState.value.activePositions.find { it.id == positionId }
                if (position == null) {
                    Timber.w("❌ Position not found for ID: $positionId")
                    Timber.d("❌ Current activePositions: ${appState.value.activePositions.map { it.id }}")
                    _uiEvent.emit(UiEvent.ShowError("Position not found: $positionId"))
                    return@launch
                }

                Timber.i("✅ Position FOUND! Symbol: ${position.instrument.symbol}, Current Price: ₹${position.currentPrice}")
                Timber.i("📍 Closing position ${position.id} at market price: ₹${position.currentPrice} | Reason: $reason")

                // Create trade with current market price as exit price
                val trade = Trade(
                    id = position.id,
                    instrument = position.instrument,
                    side = position.side,
                    quantity = position.quantity,
                    entryPrice = position.entryPrice,
                    exitPrice = position.currentPrice, // Market order at current LTP
                    entryTime = position.entryTime,
                    exitTime = java.time.LocalDateTime.now(),
                    exitReason = reason,
                    pnl = position.pnl
                )

                Timber.i("✅ Trade closed - Exit Price: ₹${String.format("%.2f", trade.exitPrice)}, P&L: ₹${String.format("%.2f", trade.pnl)}, Reason: ${reason.name}")

                // Remove from active positions
                val updatedPositions = appState.value.activePositions.filter { it.id != positionId }
                val updatedTrades = appState.value.closedTrades + trade
                
                // Update daily stats
                val totalPnl = appState.value.dailyStats.totalPnl + trade.pnl
                val totalTrades = appState.value.dailyStats.totalTrades + 1
                val winningTrades = appState.value.dailyStats.winningTrades + (if (trade.pnl > 0) 1 else 0)
                val losingTrades = appState.value.dailyStats.losingTrades + (if (trade.pnl < 0) 1 else 0)
                val winRate = if (totalTrades > 0) (winningTrades.toDouble() / totalTrades) * 100 else 0.0
                
                val updatedDailyStats = appState.value.dailyStats.copy(
                    totalPnl = totalPnl,
                    activePositions = updatedPositions.size,
                    totalTrades = totalTrades,
                    winningTrades = winningTrades,
                    losingTrades = losingTrades,
                    winRate = winRate
                )

                // Update repository with all changes
                Timber.d("🔄 Updating repository...")
                Timber.d("   Old positions: ${appState.value.activePositions.size}, New positions: ${updatedPositions.size}")
                Timber.d("   Old trades: ${appState.value.closedTrades.size}, New trades: ${updatedTrades.size}")
                Timber.d("   Old active count in stats: ${appState.value.dailyStats.activePositions}, New: ${updatedDailyStats.activePositions}")
                repository.updateAppState(
                    appState.value.copy(
                        activePositions = updatedPositions,
                        closedTrades = updatedTrades,
                        dailyStats = updatedDailyStats
                    )
                ).onSuccess {
                    Timber.d("✅ Repository update SUCCESS")
                    Timber.d("✅ Position closed and updated in repository")
                    
                    // Show appropriate message based on reason
                    val message = when (reason) {
                        ExitReason.MANUAL_EXIT -> "Position closed manually at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                        ExitReason.TARGET_HIT -> "Target reached! Position closed at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                        ExitReason.SL_HIT -> "Stop Loss hit! Position closed at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                        ExitReason.TIME_EXIT -> "Time exit triggered! Position closed at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                        ExitReason.EMERGENCY_EXIT -> "Emergency stop! Position closed at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                        ExitReason.MANUAL -> "Position closed at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                        ExitReason.CIRCUIT_BREAKER -> "Circuit breaker! Position closed at ₹${String.format("%.2f", trade.exitPrice)} | P&L: ₹${String.format("%.0f", trade.pnl)}"
                    }
                    Timber.d("📢 Emitting success event: $message")
                    _uiEvent.emit(UiEvent.ShowSuccess(message))
                }.onFailure { error ->
                    Timber.e(error, "❌ Repository update FAILED")
                    _uiEvent.emit(UiEvent.ShowError("Failed to close position: ${error.message}"))
                }
            } catch (e: Exception) {
                Timber.e(e, "Error closing trade")
                _uiEvent.emit(UiEvent.ShowError(e.message ?: "Unknown error while closing trade"))
            }
        }
    }

    fun closeAllPositions() {
        viewModelScope.launch {
            repository.closeAllPositions().onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to close all positions"))
            }
        }
    }

    fun updateStrategyConfig(config: StrategyConfig) {
        viewModelScope.launch {
            repository.updateStrategyConfig(config).onSuccess {
                _uiEvent.emit(UiEvent.ShowSuccess("Configuration saved"))
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to save configuration"))
            }
        }
    }

    fun updateRiskSettings(settings: RiskSettings) {
        viewModelScope.launch {
            repository.updateRiskSettings(settings).onSuccess {
                _uiEvent.emit(UiEvent.ShowSuccess("Risk settings saved"))
            }.onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to save risk settings"))
            }
        }
    }

    fun pauseTrading() {
        viewModelScope.launch {
            repository.updateAppState(
                appState.value.copy(strategyStatus = StrategyStatus.PAUSED)
            )
        }
    }

    fun resumeTrading() {
        viewModelScope.launch {
            repository.updateAppState(
                appState.value.copy(strategyStatus = StrategyStatus.ACTIVE)
            )
        }
    }

    fun fetchTradesHistory(filter: HistoryFilter = HistoryFilter.TODAY) {
        viewModelScope.launch {
            repository.fetchTradesHistory(filter).onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to fetch trades"))
            }
        }
    }
}

sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class ShowSuccess(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}
