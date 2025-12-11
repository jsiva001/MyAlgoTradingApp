package com.trading.orb.ui.event

/**
 * UI Events for More Screen
 */
sealed class MoreUiEvent {
    data class ShowError(val message: String) : MoreUiEvent()
    data class ShowSuccess(val message: String) : MoreUiEvent()
    data object OpenFeedback : MoreUiEvent()
    data class OpenUrl(val url: String) : MoreUiEvent()
    data object ShareApp : MoreUiEvent()
}
