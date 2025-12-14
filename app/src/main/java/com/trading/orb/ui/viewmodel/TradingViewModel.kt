package com.trading.orb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    // 🆕 ADD: Initialize and start mock engine
    fun initializeAndStartMockStrategy(scenario: String = "normal") {
        Timber.i("🧪 Initializing MOCK ORB Strategy Engine - Scenario: $scenario")
        
        viewModelScope.launch {
            try {
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
                _uiEvent.emit(UiEvent.ShowSuccess("ORB Levels Captured! High: ₹${String.format("%.2f", event.levels.high)}, Low: ₹${String.format("%.2f", event.levels.low)}"))
            }
            
            is StrategyEvent.PositionOpened -> {
                Timber.i("🟢 Position Opened - Side: ${event.position.side}, Price: ${event.position.entryPrice}")
                _uiEvent.emit(UiEvent.ShowSuccess("Position opened at ₹${String.format("%.2f", event.position.entryPrice)}"))
            }
            
            is StrategyEvent.PositionUpdate -> {
                Timber.v("💹 Position Update - Current Price: ${event.position.currentPrice}")
                // Silent update - don't spam notifications
            }
            
            is StrategyEvent.PositionClosed -> {
                Timber.i("🏁 Position Closed - P&L: ${event.trade.pnl}")
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
            
            is StrategyEvent.PriceUpdate -> {
                Timber.v("📊 Price Update: ₹${String.format("%.2f", event.ltp)}")
                // Silent update
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
            repository.toggleTradingMode().onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to toggle mode"))
            }
        }
    }

    fun emergencyStop() {
        viewModelScope.launch {
            Timber.i("🚨 EMERGENCY STOP triggered!")
            // Stop strategy engine
            strategyEngine?.stop()
            
            // Stop strategy and close all positions
            repository.stopStrategy()
            repository.closeAllPositions().onSuccess {
                Timber.i("✅ Emergency stop executed - all positions closed")
                _uiEvent.emit(UiEvent.ShowSuccess("Emergency stop executed - all positions closed"))
            }.onFailure { error ->
                Timber.e("Failed to close positions: ${error.message}")
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Emergency stop failed"))
            }
        }
    }

    fun closePosition(positionId: String) {
        viewModelScope.launch {
            repository.closePosition(positionId).onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Failed to close position"))
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
