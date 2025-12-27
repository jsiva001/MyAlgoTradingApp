package com.trading.orb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.AppState
import com.trading.orb.data.model.Position
import com.trading.orb.data.model.StrategyConfig
import com.trading.orb.data.model.Trade
import com.trading.orb.ui.screens.dashboard.DashboardUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Legacy TradingViewModel for backward compatibility
 * This acts as a bridge until all screens are migrated to individual MVI ViewModels
 * Most functionality is now in individual MVI ViewModels in ui.mvi package
 */
sealed class UiEvent {
    data class ShowError(val message: String) : UiEvent()
    data class ShowSuccess(val message: String) : UiEvent()
    object NavigateBack : UiEvent()
}

@HiltViewModel
class TradingViewModel @Inject constructor(
) : ViewModel() {

    private val _appState = MutableStateFlow(AppState())
    val appState: StateFlow<AppState> = _appState.asStateFlow()

    private val _dashboardUiState = MutableStateFlow(DashboardUiState())
    val dashboardUiState: StateFlow<DashboardUiState> = _dashboardUiState.asStateFlow()

    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    val positions: StateFlow<List<Position>> = _positions.asStateFlow()

    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    val trades: StateFlow<List<Trade>> = _trades.asStateFlow()

    private val _strategyConfig = MutableStateFlow<StrategyConfig?>(null)
    val strategyConfig: StateFlow<StrategyConfig?> = _strategyConfig.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        Timber.d("TradingViewModel initialized - Legacy bridge for backward compatibility")
    }

    fun showError(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowError(message))
        }
    }

    fun showSuccess(message: String) {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.ShowSuccess(message))
        }
    }

    fun navigateBack() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.NavigateBack)
        }
    }

    // Stub methods for backward compatibility with existing screens
    fun toggleStrategy() {
        Timber.d("toggleStrategy called on legacy ViewModel - use DashboardViewModel instead")
    }

    fun toggleTradingMode() {
        Timber.d("toggleTradingMode called on legacy ViewModel - use DashboardViewModel instead")
    }

    fun emergencyStop() {
        Timber.d("emergencyStop called on legacy ViewModel - use RiskManagementViewModel instead")
    }

    fun retryDashboard() {
        Timber.d("retryDashboard called on legacy ViewModel - use DashboardViewModel instead")
    }

    fun updateRiskSettings(riskPercentage: Double) {
        Timber.d("updateRiskSettings called on legacy ViewModel - use RiskManagementViewModel instead")
    }

    fun saveStrategyConfig() {
        Timber.d("saveStrategyConfig called on legacy ViewModel - use StrategyConfigurationViewModel instead")
    }

    fun exportConfiguration() {
        Timber.d("exportConfiguration called on legacy ViewModel - use StrategyConfigurationViewModel instead")
    }

    fun closePosition(positionId: String) {
        Timber.d("closePosition called on legacy ViewModel - use PositionsViewModel instead")
    }

    fun exportTrades() {
        Timber.d("exportTrades called on legacy ViewModel - use TradeHistoryViewModel instead")
    }
}
