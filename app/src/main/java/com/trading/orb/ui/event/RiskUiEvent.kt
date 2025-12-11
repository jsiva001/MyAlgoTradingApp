package com.trading.orb.ui.event

sealed class RiskUiEvent {
    data class ShowError(val message: String) : RiskUiEvent()
    data class ShowSuccess(val message: String) : RiskUiEvent()
    data class AlertDismissed(val alertId: String) : RiskUiEvent()
    data object AllAlertsAcknowledged : RiskUiEvent()
    data object DataRefreshed : RiskUiEvent()
}
