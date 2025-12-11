package com.trading.orb.ui.event

/**
 * UI Events for Live Logs Screen
 */
sealed class LiveLogsUiEvent {
    data class ShowError(val message: String) : LiveLogsUiEvent()
    data class ShowSuccess(val message: String) : LiveLogsUiEvent()
    data object LogsCleared : LiveLogsUiEvent()
    data object LogsExported : LiveLogsUiEvent()
    data class LogSelected(val logId: String) : LiveLogsUiEvent()
}
