package com.trading.orb.ui.event

/**
 * UI Events for Trade History Screen
 */
sealed class TradeHistoryUiEvent {
    data class ShowError(val message: String) : TradeHistoryUiEvent()
    data class ShowSuccess(val message: String) : TradeHistoryUiEvent()
    data object TradeSelected : TradeHistoryUiEvent()
    data object HistoryExported : TradeHistoryUiEvent()
}
