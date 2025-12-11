package com.trading.orb.ui.event

/**
 * UI Events for Positions Screen
 */
sealed class PositionsUiEvent {
    data class ShowError(val message: String) : PositionsUiEvent()
    data class ShowSuccess(val message: String) : PositionsUiEvent()
    data object NavigateToPositionDetails : PositionsUiEvent()
}
