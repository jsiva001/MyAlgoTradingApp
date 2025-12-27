package com.trading.orb.ui.mvi.risk

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.RiskMetricsUiModel
import com.trading.orb.ui.state.RiskLimitsUiModel
import com.trading.orb.ui.state.RiskAlertUiModel

/**
 * Risk Management Screen State
 */
data class RiskManagementScreenState(
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Risk data
    val riskMetrics: RiskMetricsUiModel = RiskMetricsUiModel(),
    val riskLimits: RiskLimitsUiModel = RiskLimitsUiModel(),
    val riskAlerts: List<RiskAlertUiModel> = emptyList(),
    
    // View state
    val isAlertDialogVisible: Boolean = false,
    val selectedAlert: RiskAlertUiModel? = null,
    val isEditLimitsVisible: Boolean = false
) : MviState

sealed class RiskManagementScreenIntent : MviIntent {
    // Lifecycle
    object LoadRiskData : RiskManagementScreenIntent()
    object RetryLoadRiskData : RiskManagementScreenIntent()
    object RefreshRiskData : RiskManagementScreenIntent()

    // Alerts
    data class ViewAlert(val alert: RiskAlertUiModel) : RiskManagementScreenIntent()
    object DismissAlert : RiskManagementScreenIntent()
    object ClearAllAlerts : RiskManagementScreenIntent()

    // Risk Limits
    object ShowEditLimitsDialog : RiskManagementScreenIntent()
    object HideEditLimitsDialog : RiskManagementScreenIntent()
    data class UpdateRiskLimits(val limits: RiskLimitsUiModel) : RiskManagementScreenIntent()
    object SaveRiskLimits : RiskManagementScreenIntent()

    // Actions
    object TriggerEmergencyStop : RiskManagementScreenIntent()
    object ReduceExposure : RiskManagementScreenIntent()

    // Error handling
    data class HandleError(val error: Throwable) : RiskManagementScreenIntent()
}

sealed class RiskManagementScreenEffect : MviEffect {
    // Feedback
    data class ShowToast(val message: String) : RiskManagementScreenEffect()
    data class ShowError(val message: String) : RiskManagementScreenEffect()
    data class ShowSuccess(val message: String) : RiskManagementScreenEffect()

    // Navigation
    object NavigateBack : RiskManagementScreenEffect()

    // Alerts
    data class ShowAlertDetails(val alert: RiskAlertUiModel) : RiskManagementScreenEffect()
    object ShowEmergencyStopConfirm : RiskManagementScreenEffect()

    // Logging
    data class LogEvent(val eventName: String) : RiskManagementScreenEffect()
}
