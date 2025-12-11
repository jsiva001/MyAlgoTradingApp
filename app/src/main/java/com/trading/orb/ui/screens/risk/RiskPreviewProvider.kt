package com.trading.orb.ui.screens.risk

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.RiskAlertUiModel
import com.trading.orb.ui.state.RiskLevelUiModel
import com.trading.orb.ui.state.RiskLimitsUiModel
import com.trading.orb.ui.state.RiskMetricsUiModel

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in RiskScreen
 */
object RiskPreviewProvider {

    fun sampleRiskMetrics(
        portfolioValue: Double = 500000.0,
        dayDrawdown: Double = -2500.0,
        sharpeRatio: Double = 1.45
    ): RiskMetricsUiModel {
        return RiskMetricsUiModel(
            portfolioValue = portfolioValue,
            dayDrawdown = dayDrawdown,
            maxDrawdown = -5000.0,
            sharpeRatio = sharpeRatio,
            exposurePercent = 45.0,
            leverageRatio = 1.5
        )
    }

    fun sampleRiskLevelUiModel(
        symbol: String = "NIFTY50",
        riskLevel: String = "MEDIUM",
        exposure: Double = 50000.0,
        concentration: Double = 10.0
    ): RiskLevelUiModel {
        return RiskLevelUiModel(
            symbol = symbol,
            riskLevel = riskLevel,
            exposure = exposure,
            concentration = concentration
        )
    }

    fun sampleRiskAlertUiModel(
        alertId: String = "ALERT001",
        type: String = "DRAWDOWN",
        severity: String = "WARNING",
        message: String = "Daily drawdown limit reached 50%",
        value: Double = 2500.0,
        threshold: Double = 5000.0
    ): RiskAlertUiModel {
        return RiskAlertUiModel(
            alertId = alertId,
            type = type,
            severity = severity,
            message = message,
            value = value,
            threshold = threshold,
            timestamp = "2024-12-11T14:30:00"
        )
    }

    fun sampleRiskAlertsList(): List<RiskAlertUiModel> {
        return listOf(
            sampleRiskAlertUiModel(
                alertId = "ALERT001",
                type = "DRAWDOWN",
                severity = "WARNING",
                message = "Daily drawdown limit reached 50%"
            ),
            sampleRiskAlertUiModel(
                alertId = "ALERT002",
                type = "EXPOSURE",
                severity = "INFO",
                message = "Exposure limit reached 45%",
                value = 225000.0,
                threshold = 500000.0
            )
        )
    }

    fun sampleRiskLimitsList(): List<RiskLevelUiModel> {
        return listOf(
            sampleRiskLevelUiModel(symbol = "NIFTY50", riskLevel = "MEDIUM"),
            sampleRiskLevelUiModel(symbol = "FINNIFTY", riskLevel = "LOW"),
            sampleRiskLevelUiModel(symbol = "BANKNIFTY", riskLevel = "HIGH")
        )
    }

    fun sampleRiskLimits(): RiskLimitsUiModel {
        return RiskLimitsUiModel(
            dailyLossLimit = 5000.0,
            weeklyLossLimit = 25000.0,
            maxDrawdownLimit = 20.0,
            maxExposureLimit = 50.0,
            maxPositionSize = 10000.0
        )
    }

    fun sampleRiskUiState(
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load risk metrics"
    ): RiskUiState {
        return RiskUiState(
            riskMetrics = sampleRiskMetrics(),
            portfolioHeatmap = sampleRiskLimitsList(),
            riskLimits = sampleRiskLimits(),
            alerts = sampleRiskAlertsList(),
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading risk metrics..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            showAlertDetails = false,
            selectedAlert = null
        )
    }

    fun sampleRiskUiStateLoading(): RiskUiState {
        return sampleRiskUiState(isLoading = true)
    }

    fun sampleRiskUiStateError(): RiskUiState {
        return sampleRiskUiState(hasError = true)
    }

    fun sampleRiskUiStateNoAlerts(): RiskUiState {
        return sampleRiskUiState().copy(alerts = emptyList())
    }

    fun sampleRiskUiStateWithAlertDetails(): RiskUiState {
        val alert = sampleRiskAlertUiModel()
        return sampleRiskUiState().copy(
            showAlertDetails = true,
            selectedAlert = alert
        )
    }
}
