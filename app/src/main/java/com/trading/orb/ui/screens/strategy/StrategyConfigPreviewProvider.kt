package com.trading.orb.ui.screens.strategy

import com.trading.orb.ui.state.AdvancedSettingsUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.TradingHoursUiModel

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in StrategyConfigScreen
 */
object StrategyConfigPreviewProvider {

    fun sampleTradingHours(
        startTime: String = "09:30",
        endTime: String = "16:00"
    ): TradingHoursUiModel {
        return TradingHoursUiModel(
            startTime = startTime,
            endTime = endTime,
            timeZone = "IST",
            mondayEnabled = true,
            tuesdayEnabled = true,
            wednesdayEnabled = true,
            thursdayEnabled = true,
            fridayEnabled = true,
            saturdayEnabled = false,
            sundayEnabled = false
        )
    }

    fun sampleAdvancedSettings(
        useTrailingStop: Boolean = false,
        maxSlippage: Float = 0.1f
    ): AdvancedSettingsUiModel {
        return AdvancedSettingsUiModel(
            useTrailingStop = useTrailingStop,
            trailingStopPercent = 0.5f,
            maxSlippage = maxSlippage,
            enablePartialExit = false,
            partialExitPercent = 50f,
            useMarketOrders = true,
            enableAutoRebalance = false,
            rebalanceInterval = 60
        )
    }

    fun sampleStrategyConfigUiState(
        strategyName: String = "ORB 15-Min",
        selectedStrategy: String = "ORB",
        riskPercentage: Float = 1.5f,
        maxPositions: Int = 3,
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load configuration"
    ): StrategyConfigUiState {
        return StrategyConfigUiState(
            strategyName = strategyName,
            selectedStrategy = selectedStrategy,
            riskPercentage = riskPercentage,
            maxPositions = maxPositions,
            tradingHours = sampleTradingHours(),
            advancedSettings = sampleAdvancedSettings(),
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading configuration..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            isSaving = false,
            savedSuccessfully = false
        )
    }

    fun sampleStrategyConfigUiStateLoading(): StrategyConfigUiState {
        return sampleStrategyConfigUiState(isLoading = true)
    }

    fun sampleStrategyConfigUiStateError(): StrategyConfigUiState {
        return sampleStrategyConfigUiState(hasError = true)
    }

    fun sampleStrategyConfigUiStateSaving(): StrategyConfigUiState {
        return sampleStrategyConfigUiState().copy(isSaving = true)
    }

    fun sampleStrategyConfigUiStateSaved(): StrategyConfigUiState {
        return sampleStrategyConfigUiState().copy(
            isSaving = false,
            savedSuccessfully = true
        )
    }
}
