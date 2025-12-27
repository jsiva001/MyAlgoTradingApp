package com.trading.orb.ui.mvi.risk

import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.RiskMetricsUiModel
import com.trading.orb.ui.state.RiskLimitsUiModel
import com.trading.orb.ui.state.RiskAlertUiModel
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ErrorMessages
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Risk Management Screen ViewModel with MVI Architecture
 *
 * Manages risk metrics, limits, and alerts
 */
@HiltViewModel
class RiskManagementViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<RiskManagementScreenState, RiskManagementScreenIntent, RiskManagementScreenEffect>() {

    override fun createInitialState(): RiskManagementScreenState = RiskManagementScreenState()

    /**
     * Pure reducer function
     */
    override fun reduce(currentState: RiskManagementScreenState, intent: RiskManagementScreenIntent): RiskManagementScreenState {
        return when (intent) {
            is RiskManagementScreenIntent.LoadRiskData -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is RiskManagementScreenIntent.RetryLoadRiskData -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is RiskManagementScreenIntent.RefreshRiskData -> {
                currentState.copy(isRefreshing = true)
            }

            is RiskManagementScreenIntent.ViewAlert -> {
                currentState.copy(
                    isAlertDialogVisible = true,
                    selectedAlert = intent.alert
                )
            }

            is RiskManagementScreenIntent.DismissAlert -> {
                currentState.copy(
                    isAlertDialogVisible = false,
                    selectedAlert = null
                )
            }

            is RiskManagementScreenIntent.ClearAllAlerts -> {
                currentState.copy(riskAlerts = emptyList())
            }

            is RiskManagementScreenIntent.ShowEditLimitsDialog -> {
                currentState.copy(isEditLimitsVisible = true)
            }

            is RiskManagementScreenIntent.HideEditLimitsDialog -> {
                currentState.copy(isEditLimitsVisible = false)
            }

            is RiskManagementScreenIntent.UpdateRiskLimits -> {
                currentState.copy(riskLimits = intent.limits)
            }

            is RiskManagementScreenIntent.HandleError -> {
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
            is RiskManagementScreenIntent.SaveRiskLimits,
            is RiskManagementScreenIntent.TriggerEmergencyStop,
            is RiskManagementScreenIntent.ReduceExposure -> currentState
        }
    }

    /**
     * Handle intents with side effects
     */
    override suspend fun handleIntent(intent: RiskManagementScreenIntent) {
        when (intent) {
            is RiskManagementScreenIntent.LoadRiskData -> loadRiskData()
            is RiskManagementScreenIntent.RetryLoadRiskData -> loadRiskData()
            is RiskManagementScreenIntent.RefreshRiskData -> refreshRiskData()

            is RiskManagementScreenIntent.ViewAlert -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(RiskManagementScreenEffect.ShowAlertDetails(intent.alert))
            }

            is RiskManagementScreenIntent.DismissAlert -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is RiskManagementScreenIntent.ClearAllAlerts -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(RiskManagementScreenEffect.ShowSuccess("All alerts cleared"))
            }

            is RiskManagementScreenIntent.ShowEditLimitsDialog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is RiskManagementScreenIntent.HideEditLimitsDialog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is RiskManagementScreenIntent.UpdateRiskLimits -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is RiskManagementScreenIntent.SaveRiskLimits -> saveRiskLimitsWithSideEffects()
            is RiskManagementScreenIntent.TriggerEmergencyStop -> triggerEmergencyStopWithSideEffects()
            is RiskManagementScreenIntent.ReduceExposure -> reduceExposureWithSideEffects()

            is RiskManagementScreenIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(RiskManagementScreenEffect.ShowError(intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED))
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadRiskData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate loading risk data
            kotlinx.coroutines.delay(300)

            // In real implementation, fetch from repository
            val riskMetrics = RiskMetricsUiModel()
            val riskLimits = RiskLimitsUiModel()
            val riskAlerts = emptyList<RiskAlertUiModel>()

            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    error = ErrorState(),
                    riskMetrics = riskMetrics,
                    riskLimits = riskLimits,
                    riskAlerts = riskAlerts
                )
            )

            emitEffect(RiskManagementScreenEffect.LogEvent("risk_data_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.RISK_FAILED_LOAD)
            processIntent(RiskManagementScreenIntent.HandleError(e))
        }
    }

    private suspend fun refreshRiskData() {
        try {
            kotlinx.coroutines.delay(500)
            updateStateImmediate(state.value.copy(isRefreshing = false))
            emitEffect(RiskManagementScreenEffect.ShowSuccess("Risk data refreshed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.RISK_FAILED_REFRESH)
            emitEffect(RiskManagementScreenEffect.ShowError(e.message ?: ErrorMessages.REFRESH_FAILED))
        }
    }

    private suspend fun saveRiskLimitsWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate save delay
            kotlinx.coroutines.delay(500)

            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    isEditLimitsVisible = false
                )
            )

            emitEffect(RiskManagementScreenEffect.ShowSuccess("Risk limits updated"))
            emitEffect(RiskManagementScreenEffect.LogEvent("limits_saved"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.RISK_FAILED_SAVE)
            emitEffect(RiskManagementScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_LIMITS))
        }
    }

    private suspend fun triggerEmergencyStopWithSideEffects() {
        try {
            Timber.w(TimberLogs.RISK_EMERGENCY_STOP)
            emitEffect(RiskManagementScreenEffect.ShowEmergencyStopConfirm)
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.RISK_FAILED_EMERGENCY_STOP)
            emitEffect(RiskManagementScreenEffect.ShowError(ErrorMessages.FAILED_EMERGENCY_STOP))
        }
    }

    private suspend fun reduceExposureWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate exposure reduction
            kotlinx.coroutines.delay(1000)

            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = false))
            )

            emitEffect(RiskManagementScreenEffect.ShowSuccess("Exposure reduced successfully"))
            emitEffect(RiskManagementScreenEffect.LogEvent("exposure_reduced"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.RISK_FAILED_REDUCE)
            emitEffect(RiskManagementScreenEffect.ShowError(ErrorMessages.FAILED_REDUCE_EXPOSURE))
        }
    }
}
