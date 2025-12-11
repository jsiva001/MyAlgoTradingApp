package com.trading.orb.ui.screens.risk

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.RiskAlertUiModel
import com.trading.orb.ui.state.RiskLevelUiModel
import com.trading.orb.ui.state.RiskLimitsUiModel
import com.trading.orb.ui.state.RiskMetricsUiModel

/**
 * UI State specific to Risk Screen
 * Manages all UI-related data for risk metrics and alerts display
 */
data class RiskUiState(
    val riskMetrics: RiskMetricsUiModel = RiskMetricsUiModel(),
    val portfolioHeatmap: List<RiskLevelUiModel> = emptyList(),
    val riskLimits: RiskLimitsUiModel = RiskLimitsUiModel(),
    val alerts: List<RiskAlertUiModel> = emptyList(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val showAlertDetails: Boolean = false,
    val selectedAlert: RiskAlertUiModel? = null
)
