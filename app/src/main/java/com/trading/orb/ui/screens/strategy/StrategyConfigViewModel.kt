package com.trading.orb.ui.screens.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.Instrument
import com.trading.orb.data.model.StrategyConfig
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.StrategyConfigUiEvent
import com.trading.orb.ui.state.AdvancedSettingsUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.TradingHoursUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for StrategyConfigScreen
 * Manages strategy configuration UI state and user interactions
 */
@HiltViewModel
class StrategyConfigViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // Strategy config from repository
    val strategyConfig: StateFlow<StrategyConfig> = repository.strategyConfig
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StrategyConfig(
                instrument = Instrument(
                    symbol = "NIFTY50",
                    exchange = "NSE",
                    lotSize = 1,
                    tickSize = 0.05
                )
            )
        )

    // Strategy Config UI State
    private val _strategyConfigUiState = MutableStateFlow(StrategyConfigUiState())
    val strategyConfigUiState: StateFlow<StrategyConfigUiState> = _strategyConfigUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<StrategyConfigUiEvent>()
    val uiEvent: SharedFlow<StrategyConfigUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadStrategyConfig()
        observeStrategyConfig()
    }

    /**
     * Initial strategy config load
     */
    private fun loadStrategyConfig() {
        viewModelScope.launch {
            _strategyConfigUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading configuration...")) }
            try {
                // Data is continuously streamed from repository
                _strategyConfigUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _strategyConfigUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load configuration",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Observe repository strategy config changes
     */
    private fun observeStrategyConfig() {
        viewModelScope.launch {
            strategyConfig.collect { config ->
                _strategyConfigUiState.update {
                    it.copy(
                        strategyName = config.name,
                        selectedStrategy = "ORB",
                        riskPercentage = config.targetPoints.toFloat() / 100f,
                        maxPositions = config.maxPositions,
                        targetPoints = config.targetPoints,
                        stopLossPoints = config.stopLossPoints,
                        lotSize = config.lotSize,
                        loading = LoadingState(isLoading = false)
                    )
                }
            }
        }
    }

    /**
     * Retry loading configuration
     */
    fun retryLoadConfig() {
        loadStrategyConfig()
    }

    /**
     * Update strategy name
     */
    fun updateStrategyName(name: String) {
        _strategyConfigUiState.update {
            it.copy(strategyName = name)
        }
    }

    /**
     * Update selected strategy type
     */
    fun updateStrategyType(strategyType: String) {
        _strategyConfigUiState.update {
            it.copy(selectedStrategy = strategyType)
        }
    }

    /**
     * Update risk percentage
     */
    fun updateRiskPercentage(percentage: Float) {
        _strategyConfigUiState.update {
            it.copy(riskPercentage = percentage)
        }
    }

    /**
     * Update target points
     */
    fun updateTargetPoints(targetPoints: Double) {
        _strategyConfigUiState.update {
            it.copy(targetPoints = targetPoints)
        }
    }

    /**
     * Update stop loss points
     */
    fun updateStopLossPoints(stopLossPoints: Double) {
        _strategyConfigUiState.update {
            it.copy(stopLossPoints = stopLossPoints)
        }
    }

    /**
     * Update lot size (affects total quantity: lot size * 75)
     */
    fun updateLotSize(lotSize: Int) {
        if (lotSize > 0) {
            _strategyConfigUiState.update {
                it.copy(lotSize = lotSize)
            }
        }
    }

    /**
     * Update trading hours
     */
    fun updateTradingHours(tradingHours: TradingHoursUiModel) {
        _strategyConfigUiState.update {
            it.copy(tradingHours = tradingHours)
        }
    }

    /**
     * Update advanced settings
     */
    fun updateAdvancedSettings(advancedSettings: AdvancedSettingsUiModel) {
        _strategyConfigUiState.update {
            it.copy(advancedSettings = advancedSettings)
        }
    }

    /**
     * Save strategy configuration
     */
    fun saveConfiguration() {
        viewModelScope.launch {
            _strategyConfigUiState.update { it.copy(isSaving = true) }
            try {
                val updatedConfig = strategyConfig.value.copy(
                    name = _strategyConfigUiState.value.strategyName,
                    maxPositions = _strategyConfigUiState.value.maxPositions,
                    targetPoints = _strategyConfigUiState.value.targetPoints,
                    stopLossPoints = _strategyConfigUiState.value.stopLossPoints,
                    lotSize = _strategyConfigUiState.value.lotSize
                )

                repository.updateStrategyConfig(updatedConfig).onSuccess {
                    _strategyConfigUiState.update {
                        it.copy(
                            isSaving = false,
                            savedSuccessfully = true
                        )
                    }
                    _uiEvent.emit(StrategyConfigUiEvent.ShowSuccess("Configuration saved successfully"))
                }.onFailure { error ->
                    _strategyConfigUiState.update {
                        it.copy(
                            isSaving = false,
                            error = ErrorState(
                                hasError = true,
                                errorMessage = error.message ?: "Failed to save configuration",
                                isRetryable = true
                            )
                        )
                    }
                    _uiEvent.emit(StrategyConfigUiEvent.ShowError(error.message ?: "Failed to save"))
                }
            } catch (e: Exception) {
                _strategyConfigUiState.update {
                    it.copy(
                        isSaving = false,
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to save",
                            isRetryable = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Reset configuration to defaults
     */
    fun resetToDefaults() {
        _strategyConfigUiState.update {
            StrategyConfigUiState()
        }
    }
}

