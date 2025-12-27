package com.trading.orb.ui.mvi.strategy

import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.StrategyConfig
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.TradingHoursUiModel
import com.trading.orb.ui.state.AdvancedSettingsUiModel
import com.trading.orb.ui.utils.*
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ErrorMessages
import com.trading.orb.ui.utils.STRATEGY_CONFIG_EXPORT_FORMAT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Strategy Configuration Screen ViewModel with MVI Architecture
 *
 * Manages strategy configuration and settings
 */
@HiltViewModel
class StrategyConfigurationViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<StrategyConfigurationScreenState, StrategyConfigurationScreenIntent, StrategyConfigurationScreenEffect>() {

    override fun createInitialState(): StrategyConfigurationScreenState = StrategyConfigurationScreenState()

    /**
     * Pure reducer function
     */
    override fun reduce(currentState: StrategyConfigurationScreenState, intent: StrategyConfigurationScreenIntent): StrategyConfigurationScreenState {
        return when (intent) {
            is StrategyConfigurationScreenIntent.LoadConfiguration -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is StrategyConfigurationScreenIntent.RetryLoadConfiguration -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is StrategyConfigurationScreenIntent.RefreshConfiguration -> {
                currentState.copy(isRefreshing = true)
            }

            is StrategyConfigurationScreenIntent.EnableEditMode -> {
                currentState.copy(isEditing = true)
            }

            is StrategyConfigurationScreenIntent.DisableEditMode -> {
                currentState.copy(isEditing = false)
            }

            is StrategyConfigurationScreenIntent.ResetChanges -> {
                currentState.copy(
                    isEditing = false,
                    hasUnsavedChanges = false
                )
            }

            is StrategyConfigurationScreenIntent.UpdateStrategyConfig -> {
                currentState.copy(
                    strategyConfig = intent.config,
                    hasUnsavedChanges = true
                )
            }

            is StrategyConfigurationScreenIntent.UpdateTradingHours -> {
                currentState.copy(
                    tradingHours = intent.hours,
                    hasUnsavedChanges = true
                )
            }

            is StrategyConfigurationScreenIntent.UpdateAdvancedSettings -> {
                currentState.copy(
                    advancedSettings = intent.settings,
                    hasUnsavedChanges = true
                )
            }

            is StrategyConfigurationScreenIntent.SelectTab -> {
                currentState.copy(selectedTabIndex = intent.tabIndex)
            }

            is StrategyConfigurationScreenIntent.HandleError -> {
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

            // Action-only intents
            is StrategyConfigurationScreenIntent.SaveConfiguration,
            is StrategyConfigurationScreenIntent.ApplyDefaults,
            is StrategyConfigurationScreenIntent.ExportConfiguration -> currentState
        }
    }

    /**
     * Handle intents with side effects
     */
    override suspend fun handleIntent(intent: StrategyConfigurationScreenIntent) {
        when (intent) {
            is StrategyConfigurationScreenIntent.LoadConfiguration -> loadConfigurationData()
            is StrategyConfigurationScreenIntent.RetryLoadConfiguration -> loadConfigurationData()
            is StrategyConfigurationScreenIntent.RefreshConfiguration -> refreshConfigurationData()

            is StrategyConfigurationScreenIntent.EnableEditMode -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is StrategyConfigurationScreenIntent.DisableEditMode -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is StrategyConfigurationScreenIntent.ResetChanges -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(StrategyConfigurationScreenEffect.ShowSuccess("Changes discarded"))
            }

            is StrategyConfigurationScreenIntent.UpdateStrategyConfig -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is StrategyConfigurationScreenIntent.UpdateTradingHours -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is StrategyConfigurationScreenIntent.UpdateAdvancedSettings -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is StrategyConfigurationScreenIntent.SelectTab -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is StrategyConfigurationScreenIntent.SaveConfiguration -> saveConfigurationWithSideEffects()
            is StrategyConfigurationScreenIntent.ApplyDefaults -> applyDefaultsWithSideEffects()
            is StrategyConfigurationScreenIntent.ExportConfiguration -> exportConfigurationWithSideEffects()

            is StrategyConfigurationScreenIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(StrategyConfigurationScreenEffect.ShowError(intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED))
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadConfigurationData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate loading configuration
            kotlinx.coroutines.delay(300)

            // In real implementation, fetch from repository
            val config = StrategyConfig(
                instrument = com.trading.orb.data.model.Instrument(
                    symbol = DEFAULT_INSTRUMENT_SYMBOL,
                    exchange = DEFAULT_INSTRUMENT_EXCHANGE,
                    lotSize = DEFAULT_INSTRUMENT_LOT_SIZE,
                    tickSize = DEFAULT_INSTRUMENT_TICK_SIZE,
                    displayName = DEFAULT_INSTRUMENT_DISPLAY_NAME
                )
            )

            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    error = ErrorState(),
                    strategyConfig = config
                )
            )

            emitEffect(StrategyConfigurationScreenEffect.LogEvent("config_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.CONFIG_FAILED_LOAD)
            processIntent(StrategyConfigurationScreenIntent.HandleError(e))
        }
    }

    private suspend fun refreshConfigurationData() {
        try {
            kotlinx.coroutines.delay(500)
            updateStateImmediate(state.value.copy(isRefreshing = false))
            emitEffect(StrategyConfigurationScreenEffect.ShowSuccess("Configuration refreshed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.CONFIG_FAILED_REFRESH)
            emitEffect(StrategyConfigurationScreenEffect.ShowError(e.message ?: ErrorMessages.REFRESH_FAILED))
        }
    }

    private suspend fun saveConfigurationWithSideEffects() {
        try {
            if (!state.value.hasUnsavedChanges) {
                emitEffect(StrategyConfigurationScreenEffect.ShowToast("No changes to save"))
                return
            }

            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate save delay
            kotlinx.coroutines.delay(800)

            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    isEditing = false,
                    hasUnsavedChanges = false
                )
            )

            emitEffect(StrategyConfigurationScreenEffect.ShowSuccess("Configuration saved"))
            emitEffect(StrategyConfigurationScreenEffect.LogEvent("config_saved"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.CONFIG_FAILED_SAVE)
            emitEffect(StrategyConfigurationScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private suspend fun applyDefaultsWithSideEffects() {
        try {
            emitEffect(StrategyConfigurationScreenEffect.ShowApplyDefaultsConfirm)
            
            // After confirmation, apply defaults
            updateStateImmediate(
                state.value.copy(
                    strategyConfig = state.value.strategyConfig?.copy(),
                    tradingHours = TradingHoursUiModel(),
                    advancedSettings = AdvancedSettingsUiModel(),
                    hasUnsavedChanges = true
                )
            )

            emitEffect(StrategyConfigurationScreenEffect.ShowSuccess("Defaults applied"))
            emitEffect(StrategyConfigurationScreenEffect.LogEvent("defaults_applied"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.CONFIG_FAILED_APPLY_DEFAULTS)
            emitEffect(StrategyConfigurationScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private suspend fun exportConfigurationWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate export delay
            kotlinx.coroutines.delay(1000)

            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = false))
            )

            val fileName = String.format(STRATEGY_CONFIG_EXPORT_FORMAT, System.currentTimeMillis())
            emitEffect(StrategyConfigurationScreenEffect.ExportConfigFile("/exports/$fileName"))
            emitEffect(StrategyConfigurationScreenEffect.ShowSuccess("Configuration exported"))
            emitEffect(StrategyConfigurationScreenEffect.LogEvent("config_exported"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.CONFIG_FAILED_EXPORT)
            emitEffect(StrategyConfigurationScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }
}
