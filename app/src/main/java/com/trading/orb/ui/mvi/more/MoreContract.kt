package com.trading.orb.ui.mvi.more

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.UserSettingsUiModel
import com.trading.orb.ui.state.AboutInfoUiModel

/**
 * More Screen State (Settings/More Options)
 */
data class MoreScreenState(
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Settings
    val userSettings: UserSettingsUiModel = UserSettingsUiModel(),
    
    // App Info
    val aboutInfo: AboutInfoUiModel = AboutInfoUiModel(),
    val appVersion: String = "1.0.0",
    
    // Dialog state
    val isSettingsDialogVisible: Boolean = false,
    val isLogoutDialogVisible: Boolean = false,
    val settingsChanged: Boolean = false
) : MviState

sealed class MoreScreenIntent : MviIntent {
    // Lifecycle
    object LoadSettings : MoreScreenIntent()
    object RetryLoadSettings : MoreScreenIntent()
    object RefreshSettings : MoreScreenIntent()

    // Settings changes
    data class SetSoundEnabled(val enabled: Boolean) : MoreScreenIntent()
    data class SetNotificationsEnabled(val enabled: Boolean) : MoreScreenIntent()
    data class SetDarkModeEnabled(val enabled: Boolean) : MoreScreenIntent()
    data class SetAutoStartStrategy(val enabled: Boolean) : MoreScreenIntent()
    data class SetEmergencyStopOnExit(val enabled: Boolean) : MoreScreenIntent()
    data class SetLogLevel(val level: String) : MoreScreenIntent()
    data class SetDateFormat(val format: String) : MoreScreenIntent()
    data class SetTimeFormat(val format: String) : MoreScreenIntent()

    // Actions
    object SaveSettings : MoreScreenIntent()
    object ResetToDefaults : MoreScreenIntent()
    object ClearCache : MoreScreenIntent()
    object CheckForUpdates : MoreScreenIntent()
    object ShareApp : MoreScreenIntent()
    data class OpenWebsite(val url: String) : MoreScreenIntent()
    object OpenFeedback : MoreScreenIntent()

    // Dialog management
    object ShowSettingsDialog : MoreScreenIntent()
    object DismissSettingsDialog : MoreScreenIntent()
    object ShowLogoutDialog : MoreScreenIntent()
    object DismissLogoutDialog : MoreScreenIntent()
    object ConfirmLogout : MoreScreenIntent()

    // Error handling
    data class HandleError(val error: Throwable) : MoreScreenIntent()
}

sealed class MoreScreenEffect : MviEffect {
    // Feedback
    data class ShowToast(val message: String) : MoreScreenEffect()
    data class ShowError(val message: String) : MoreScreenEffect()
    data class ShowSuccess(val message: String) : MoreScreenEffect()

    // Navigation
    object NavigateToLogin : MoreScreenEffect()
    object NavigateBack : MoreScreenEffect()

    // System actions
    data class ShareFile(val filePath: String, val mimeType: String) : MoreScreenEffect()
    data class OpenURL(val url: String) : MoreScreenEffect()
    object ShareApp : MoreScreenEffect()
    object OpenFeedback : MoreScreenEffect()

    // Dialogs
    object ShowCacheDialog : MoreScreenEffect()
    object ShowLogoutConfirmation : MoreScreenEffect()

    // Logging
    data class LogEvent(val eventName: String) : MoreScreenEffect()
}
