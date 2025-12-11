package com.trading.orb.ui.event

/**
 * UI Events for Dashboard
 */
sealed class DashboardUiEvent {
    data class ShowError(val message: String) : DashboardUiEvent()
    data class ShowSuccess(val message: String) : DashboardUiEvent()
    data object NavigateToStrategy : DashboardUiEvent()
    data object NavigateToPositions : DashboardUiEvent()
}
