package com.trading.orb.ui.screens.strategy

import com.trading.orb.ui.state.AdvancedSettingsUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.TradingHoursUiModel

/**
 * UI State specific to Strategy Config Screen
 * Manages all UI-related data for strategy configuration display
 */
data class StrategyConfigUiState(
    val strategyName: String = "",
    val selectedStrategy: String = "",
    val riskPercentage: Float = 1f,
    val maxPositions: Int = 3,
    val tradingHours: TradingHoursUiModel = TradingHoursUiModel(),
    val advancedSettings: AdvancedSettingsUiModel = AdvancedSettingsUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false
)
