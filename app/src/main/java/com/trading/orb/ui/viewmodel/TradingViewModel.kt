package com.trading.orb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.*
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.screens.tradehistory.HistoryFilter
import com.trading.orb.ui.state.DashboardUiState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

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

    // Actions
    fun toggleStrategy() {
        viewModelScope.launch {
            val result = if (appState.value.strategyStatus == StrategyStatus.ACTIVE) {
                repository.stopStrategy()
            } else {
                repository.startStrategy()
            }

            result.onFailure { error ->
                _uiEvent.emit(UiEvent.ShowError(error.message ?: "Unknown error"))
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
            // Stop strategy and close all positions
            repository.stopStrategy()
            repository.closeAllPositions().onFailure { error ->
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
