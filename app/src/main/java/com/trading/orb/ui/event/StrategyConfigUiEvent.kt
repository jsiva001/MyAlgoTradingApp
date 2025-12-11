package com.trading.orb.ui.event

/**
 * UI Events for Strategy Config Screen
 */
sealed class StrategyConfigUiEvent {
    data class ShowError(val message: String) : StrategyConfigUiEvent()
    data class ShowSuccess(val message: String) : StrategyConfigUiEvent()
    data object ConfigurationSaved : StrategyConfigUiEvent()
}
