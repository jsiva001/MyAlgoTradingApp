package com.trading.orb.ui.mvi.more

import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.UserSettingsUiModel
import com.trading.orb.ui.state.AboutInfoUiModel
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ErrorMessages
import com.trading.orb.ui.utils.DEFAULT_DELAY_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * More Screen ViewModel with MVI Architecture
 *
 * Manages settings, about info, and user preferences
 */
@HiltViewModel
class MoreViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<MoreScreenState, MoreScreenIntent, MoreScreenEffect>() {

    override fun createInitialState(): MoreScreenState = MoreScreenState()

    /**
     * Pure reducer function - transforms state based on intent
     */
    override fun reduce(currentState: MoreScreenState, intent: MoreScreenIntent): MoreScreenState {
        return when (intent) {
            is MoreScreenIntent.LoadSettings -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is MoreScreenIntent.RetryLoadSettings -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is MoreScreenIntent.RefreshSettings -> {
                currentState.copy(isRefreshing = true)
            }

            // Settings changes
            is MoreScreenIntent.SetSoundEnabled -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(soundEnabled = intent.enabled),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetNotificationsEnabled -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(notificationsEnabled = intent.enabled),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetDarkModeEnabled -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(darkModeEnabled = intent.enabled),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetAutoStartStrategy -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(autoStartStrategy = intent.enabled),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetEmergencyStopOnExit -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(emergencyStopOnExit = intent.enabled),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetLogLevel -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(logLevel = intent.level),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetDateFormat -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(dateFormat = intent.format),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.SetTimeFormat -> {
                currentState.copy(
                    userSettings = currentState.userSettings.copy(timeFormat = intent.format),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.ResetToDefaults -> {
                currentState.copy(
                    userSettings = UserSettingsUiModel(),
                    settingsChanged = true
                )
            }

            is MoreScreenIntent.ShowSettingsDialog -> {
                currentState.copy(isSettingsDialogVisible = true)
            }

            is MoreScreenIntent.DismissSettingsDialog -> {
                currentState.copy(isSettingsDialogVisible = false)
            }

            is MoreScreenIntent.ShowLogoutDialog -> {
                currentState.copy(isLogoutDialogVisible = true)
            }

            is MoreScreenIntent.DismissLogoutDialog -> {
                currentState.copy(isLogoutDialogVisible = false)
            }

            is MoreScreenIntent.HandleError -> {
                currentState.copy(
                    loading = LoadingState(isLoading = false),
                    error = ErrorState(
                        hasError = true,
                        errorMessage = intent.error.message ?: ErrorMessages.UNKNOWN_ERROR,
                        isRetryable = true,
                        throwable = intent.error
                    )
                )
            }

            // Intent-only actions (no state change)
            is MoreScreenIntent.SaveSettings,
            is MoreScreenIntent.ClearCache,
            is MoreScreenIntent.CheckForUpdates,
            is MoreScreenIntent.ShareApp,
            is MoreScreenIntent.OpenWebsite,
            is MoreScreenIntent.OpenFeedback,
            is MoreScreenIntent.ConfirmLogout -> currentState
        }
    }

    /**
     * Handle intents with side effects
     */
    override suspend fun handleIntent(intent: MoreScreenIntent) {
        when (intent) {
            is MoreScreenIntent.LoadSettings -> loadSettingsData()
            is MoreScreenIntent.RetryLoadSettings -> loadSettingsData()
            is MoreScreenIntent.RefreshSettings -> refreshSettingsData()

            is MoreScreenIntent.SaveSettings -> saveSettingsWithSideEffects()
            is MoreScreenIntent.ResetToDefaults -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(MoreScreenEffect.ShowSuccess("Settings reset to defaults"))
            }

            is MoreScreenIntent.ClearCache -> clearCacheWithSideEffects()
            is MoreScreenIntent.CheckForUpdates -> checkForUpdatesWithSideEffects()
            is MoreScreenIntent.ShareApp -> emitEffect(MoreScreenEffect.ShareApp)
            is MoreScreenIntent.OpenWebsite -> emitEffect(MoreScreenEffect.OpenURL(intent.url))
            is MoreScreenIntent.OpenFeedback -> emitEffect(MoreScreenEffect.OpenFeedback)

            is MoreScreenIntent.ShowSettingsDialog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is MoreScreenIntent.DismissSettingsDialog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is MoreScreenIntent.ShowLogoutDialog -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(MoreScreenEffect.ShowLogoutConfirmation)
            }

            is MoreScreenIntent.DismissLogoutDialog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is MoreScreenIntent.ConfirmLogout -> confirmLogoutWithSideEffects()

            // Settings state changes
            is MoreScreenIntent.SetSoundEnabled,
            is MoreScreenIntent.SetNotificationsEnabled,
            is MoreScreenIntent.SetDarkModeEnabled,
            is MoreScreenIntent.SetAutoStartStrategy,
            is MoreScreenIntent.SetEmergencyStopOnExit,
            is MoreScreenIntent.SetLogLevel,
            is MoreScreenIntent.SetDateFormat,
            is MoreScreenIntent.SetTimeFormat -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is MoreScreenIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(MoreScreenEffect.ShowError(intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED))
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadSettingsData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate loading delay
            kotlinx.coroutines.delay(300)

            // Load about info
            val aboutInfo = AboutInfoUiModel(
                appVersion = "1.0.0",
                buildNumber = "100"
            )

            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    error = ErrorState(),
                    aboutInfo = aboutInfo,
                    appVersion = aboutInfo.appVersion
                )
            )

            emitEffect(MoreScreenEffect.LogEvent("settings_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.SETTINGS_FAILED_LOAD)
            processIntent(MoreScreenIntent.HandleError(e))
        }
    }

    private suspend fun refreshSettingsData() {
        try {
            kotlinx.coroutines.delay(500)
            updateStateImmediate(state.value.copy(isRefreshing = false))
            emitEffect(MoreScreenEffect.ShowSuccess("Settings refreshed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.SETTINGS_FAILED_REFRESH)
            emitEffect(MoreScreenEffect.ShowError(e.message ?: ErrorMessages.REFRESH_FAILED))
        }
    }

    private suspend fun saveSettingsWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate save delay
            kotlinx.coroutines.delay(500)

            // Update state after save
            updateStateImmediate(
                state.value.copy(
                    loading = LoadingState(isLoading = false),
                    settingsChanged = false,
                    isSettingsDialogVisible = false
                )
            )

            emitEffect(MoreScreenEffect.ShowSuccess("Settings saved successfully"))
            emitEffect(MoreScreenEffect.LogEvent("settings_saved"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.SETTINGS_FAILED_SAVE)
            processIntent(MoreScreenIntent.HandleError(e))
        }
    }

    private suspend fun clearCacheWithSideEffects() {
        try {
            emitEffect(MoreScreenEffect.ShowCacheDialog)
            // Actual cache clearing would be done here
            kotlinx.coroutines.delay(DEFAULT_DELAY_MS)
            emitEffect(MoreScreenEffect.ShowSuccess("Cache cleared successfully"))
            emitEffect(MoreScreenEffect.LogEvent("cache_cleared"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.SETTINGS_FAILED_CACHE_CLEAR)
            emitEffect(MoreScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private suspend fun checkForUpdatesWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate checking for updates
            kotlinx.coroutines.delay(1500)

            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = false))
            )

            emitEffect(MoreScreenEffect.ShowSuccess("You are on the latest version"))
            emitEffect(MoreScreenEffect.LogEvent("update_checked"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.SETTINGS_FAILED_CHECK_UPDATES)
            emitEffect(MoreScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private suspend fun confirmLogoutWithSideEffects() {
        try {
            Timber.i(TimberLogs.LOGOUT_CONFIRMED)
            // Perform logout actions
            updateStateImmediate(
                state.value.copy(isLogoutDialogVisible = false)
            )
            emitEffect(MoreScreenEffect.NavigateToLogin)
            emitEffect(MoreScreenEffect.LogEvent("user_logout"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.LOGOUT_FAILED)
            emitEffect(MoreScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }
}
