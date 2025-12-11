package com.trading.orb.ui.screens.risk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.RiskUiEvent
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.RiskAlertUiModel
import com.trading.orb.ui.state.RiskLimitsUiModel
import com.trading.orb.ui.state.RiskLevelUiModel
import com.trading.orb.ui.state.RiskMetricsUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for RiskScreen
 * Manages risk metrics and alerts UI state
 */
@HiltViewModel
class RiskViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // Risk UI State
    private val _riskUiState = MutableStateFlow(RiskUiState())
    val riskUiState: StateFlow<RiskUiState> = _riskUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<RiskUiEvent>()
    val uiEvent: SharedFlow<RiskUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadRiskMetrics()
        observeRiskData()
    }

    /**
     * Initial risk metrics load
     */
    private fun loadRiskMetrics() {
        viewModelScope.launch {
            _riskUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading risk metrics...")) }
            try {
                // Load sample risk data for now
                val sampleRiskMetrics = RiskMetricsUiModel(
                    portfolioValue = 50000.0,
                    dayDrawdown = 2500.0,
                    maxDrawdown = 3000.0,
                    sharpeRatio = 1.45,
                    exposurePercent = 75.0,
                    leverageRatio = 1.5
                )
                
                val sampleAlerts = listOf(
                    RiskAlertUiModel(
                        alertId = "1",
                        type = "EXPOSURE",
                        severity = "WARNING",
                        message = "Your exposure is at 75% of limit",
                        value = 75.0,
                        threshold = 80.0,
                        timestamp = "2024-01-10 14:30"
                    ),
                    RiskAlertUiModel(
                        alertId = "2",
                        type = "LOSS",
                        severity = "INFO",
                        message = "Daily loss is at 50% of limit",
                        value = 500.0,
                        threshold = 1000.0,
                        timestamp = "2024-01-10 14:25"
                    )
                )
                
                val sampleHeatmap = listOf(
                    RiskLevelUiModel(symbol = "AAPL", riskLevel = "HIGH", exposure = 25.0, concentration = 75.0),
                    RiskLevelUiModel(symbol = "GOOGL", riskLevel = "MEDIUM", exposure = 20.0, concentration = 60.0),
                    RiskLevelUiModel(symbol = "MSFT", riskLevel = "LOW", exposure = 15.0, concentration = 45.0),
                    RiskLevelUiModel(symbol = "TSLA", riskLevel = "HIGH", exposure = 15.0, concentration = 80.0)
                )
                
                _riskUiState.update {
                    it.copy(
                        riskMetrics = sampleRiskMetrics,
                        alerts = sampleAlerts,
                        portfolioHeatmap = sampleHeatmap,
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _riskUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load risk metrics",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Observe risk data changes from repository
     */
    private fun observeRiskData() {
        viewModelScope.launch {
            repository.appState.collect { appState ->
                // Update risk state based on app state
                _riskUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false)
                    )
                }
            }
        }
    }

    /**
     * Retry loading risk metrics
     */
    fun retryLoadRiskMetrics() {
        loadRiskMetrics()
    }

    /**
     * Update risk limits
     */
    fun updateRiskLimits(riskLimits: RiskLimitsUiModel) {
        _riskUiState.update {
            it.copy(riskLimits = riskLimits)
        }
    }

    /**
     * Toggle alert details view
     */
    fun toggleAlertDetails(alert: RiskAlertUiModel? = null) {
        _riskUiState.update {
            it.copy(
                showAlertDetails = !it.showAlertDetails,
                selectedAlert = alert ?: it.selectedAlert
            )
        }
    }

    /**
     * Close alert
     */
    fun closeAlert(alertId: String) {
        viewModelScope.launch {
            _riskUiState.update {
                it.copy(
                    alerts = it.alerts.filter { alert -> alert.alertId != alertId }
                )
            }
            _uiEvent.emit(RiskUiEvent.AlertDismissed(alertId))
        }
    }

    /**
     * Acknowledge all alerts
     */
    fun acknowledgeAllAlerts() {
        viewModelScope.launch {
            _riskUiState.update {
                it.copy(alerts = emptyList(), showAlertDetails = false)
            }
            _uiEvent.emit(RiskUiEvent.AllAlertsAcknowledged)
        }
    }

    /**
     * Save risk limits
     */
    fun saveRiskLimits() {
        viewModelScope.launch {
            try {
                // In a real scenario, this would update the repository
                _uiEvent.emit(RiskUiEvent.ShowSuccess("Risk limits saved successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(RiskUiEvent.ShowError(e.message ?: "Failed to save risk limits"))
            }
        }
    }

    /**
     * Refresh risk data
     */
    fun refreshRiskData() {
        viewModelScope.launch {
            try {
                // Trigger refresh from repository
                kotlinx.coroutines.delay(1000)
                _uiEvent.emit(RiskUiEvent.DataRefreshed)
            } catch (e: Exception) {
                _uiEvent.emit(RiskUiEvent.ShowError("Failed to refresh data"))
            }
        }
    }
}

