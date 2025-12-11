package com.trading.orb.ui.screens.more

import com.trading.orb.ui.state.AboutInfoUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.UserSettingsUiModel

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in MoreScreen
 */
object MorePreviewProvider {

    fun sampleUserSettings(
        soundEnabled: Boolean = true,
        notificationsEnabled: Boolean = true,
        darkModeEnabled: Boolean = false,
        autoStartStrategy: Boolean = false,
        emergencyStopOnExit: Boolean = true,
        logLevel: String = "INFO"
    ): UserSettingsUiModel {
        return UserSettingsUiModel(
            soundEnabled = soundEnabled,
            notificationsEnabled = notificationsEnabled,
            darkModeEnabled = darkModeEnabled,
            autoStartStrategy = autoStartStrategy,
            emergencyStopOnExit = emergencyStopOnExit,
            logLevel = logLevel,
            dateFormat = "MM/dd/yyyy",
            timeFormat = "HH:mm:ss"
        )
    }

    fun sampleAboutInfo(): AboutInfoUiModel {
        return AboutInfoUiModel(
            appVersion = "1.0.0",
            buildNumber = "100",
            buildDate = "2024-12-11",
            developer = "MyAlgoTrade",
            website = "www.myalgotrade.com",
            supportEmail = "support@myalgotrade.com"
        )
    }

    fun sampleMoreUiState(
        appVersion: String = "1.0.0",
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load settings",
        settingsChanged: Boolean = false,
        brokerName: String = "Zerodha",
        isConnected: Boolean = true
    ): MoreUiState {
        return MoreUiState(
            appVersion = appVersion,
            userSettings = sampleUserSettings(),
            aboutInfo = sampleAboutInfo(),
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading settings..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            showSettingsDialog = false,
            settingsChanged = settingsChanged,
            brokerName = brokerName,
            isConnected = isConnected
        )
    }

    fun sampleMoreUiStateLoading(): MoreUiState {
        return sampleMoreUiState(isLoading = true)
    }

    fun sampleMoreUiStateError(): MoreUiState {
        return sampleMoreUiState(hasError = true)
    }

    fun sampleMoreUiStateSettingsChanged(): MoreUiState {
        return sampleMoreUiState(settingsChanged = true)
    }

    fun sampleMoreUiStateWithDialogOpen(): MoreUiState {
        return sampleMoreUiState().copy(showSettingsDialog = true)
    }

    fun sampleMoreUiStateDarkModeEnabled(): MoreUiState {
        return sampleMoreUiState().copy(
            userSettings = sampleUserSettings(darkModeEnabled = true)
        )
    }

    fun sampleMoreUiStateNotificationsDisabled(): MoreUiState {
        return sampleMoreUiState().copy(
            userSettings = sampleUserSettings(notificationsEnabled = false)
        )
    }

    fun sampleMoreUiStateAutoStartEnabled(): MoreUiState {
        return sampleMoreUiState().copy(
            userSettings = sampleUserSettings(autoStartStrategy = true)
        )
    }
}
