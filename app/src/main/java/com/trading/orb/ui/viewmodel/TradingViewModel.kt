package com.trading.orb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.*
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.screens.HistoryFilter
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

    // UI events
    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

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
