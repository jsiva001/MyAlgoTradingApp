package com.trading.orb.ui.screens.more

import com.trading.orb.ui.state.AboutInfoUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.UserSettingsUiModel

/**
 * UI State specific to More Screen
 * Manages all UI-related data for settings and about display
 */
data class MoreUiState(
    val appVersion: String = "",
    val userSettings: UserSettingsUiModel = UserSettingsUiModel(),
    val aboutInfo: AboutInfoUiModel = AboutInfoUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val showSettingsDialog: Boolean = false,
    val settingsChanged: Boolean = false,
    val brokerName: String = "Zerodha",
    val isConnected: Boolean = true
)
