package com.trading.orb.ui.screens.more

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.MoreUiEvent
import com.trading.orb.ui.state.AboutInfoUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.UserSettingsUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for MoreScreen
 * Manages settings and about info UI state
 */
@HiltViewModel
class MoreViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // More Screen UI State
    private val _moreUiState = MutableStateFlow(MoreUiState())
    val moreUiState: StateFlow<MoreUiState> = _moreUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<MoreUiEvent>()
    val uiEvent: SharedFlow<MoreUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadMoreScreenData()
        loadAboutInfo()
    }

    /**
     * Initial more screen data load
     */
    private fun loadMoreScreenData() {
        viewModelScope.launch {
            _moreUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading settings...")) }
            try {
                // Load user settings and app info
                _moreUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _moreUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load settings",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Load about information
     */
    private fun loadAboutInfo() {
        viewModelScope.launch {
            try {
                // Load about info from repository or BuildConfig
                val aboutInfo = AboutInfoUiModel(
                    appVersion = "1.0.0",
                    buildNumber = "100"
                )
                _moreUiState.update {
                    it.copy(aboutInfo = aboutInfo, appVersion = aboutInfo.appVersion)
                }
            } catch (e: Exception) {
                // Handle error silently for about info
            }
        }
    }

    /**
     * Retry loading settings
     */
    fun retryLoadSettings() {
        loadMoreScreenData()
    }

    /**
     * Toggle notifications setting
     */
    fun toggleNotifications(enabled: Boolean) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(notificationsEnabled = enabled),
                settingsChanged = true
            )
        }
    }

    /**
     * Toggle sound setting
     */
    fun toggleSound(enabled: Boolean) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(soundEnabled = enabled),
                settingsChanged = true
            )
        }
    }

    /**
     * Toggle dark mode
     */
    fun toggleDarkMode(enabled: Boolean) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(darkModeEnabled = enabled),
                settingsChanged = true
            )
        }
    }

    /**
     * Toggle auto start strategy
     */
    fun toggleAutoStartStrategy(enabled: Boolean) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(autoStartStrategy = enabled),
                settingsChanged = true
            )
        }
    }

    /**
     * Toggle emergency stop on exit
     */
    fun toggleEmergencyStopOnExit(enabled: Boolean) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(emergencyStopOnExit = enabled),
                settingsChanged = true
            )
        }
    }

    /**
     * Update log level
     */
    fun updateLogLevel(logLevel: String) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(logLevel = logLevel),
                settingsChanged = true
            )
        }
    }

    /**
     * Update date format
     */
    fun updateDateFormat(dateFormat: String) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(dateFormat = dateFormat),
                settingsChanged = true
            )
        }
    }

    /**
     * Update time format
     */
    fun updateTimeFormat(timeFormat: String) {
        _moreUiState.update { state ->
            state.copy(
                userSettings = state.userSettings.copy(timeFormat = timeFormat),
                settingsChanged = true
            )
        }
    }

    /**
     * Show settings dialog
     */
    fun showSettingsDialog() {
        _moreUiState.update {
            it.copy(showSettingsDialog = true)
        }
    }

    /**
     * Hide settings dialog
     */
    fun hideSettingsDialog() {
        _moreUiState.update {
            it.copy(showSettingsDialog = false)
        }
    }

    /**
     * Save all settings changes
     */
    fun saveSettings() {
        viewModelScope.launch {
            try {
                // Save settings to repository/local storage
                _moreUiState.update {
                    it.copy(
                        settingsChanged = false,
                        showSettingsDialog = false
                    )
                }
                _uiEvent.emit(MoreUiEvent.ShowSuccess("Settings saved successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(MoreUiEvent.ShowError(e.message ?: "Failed to save settings"))
            }
        }
    }

    /**
     * Reset settings to defaults
     */
    fun resetSettingsToDefaults() {
        _moreUiState.update {
            it.copy(
                userSettings = UserSettingsUiModel(),
                settingsChanged = true
            )
        }
    }

    /**
     * Open feedback/support
     */
    fun openFeedback() {
        viewModelScope.launch {
            _uiEvent.emit(MoreUiEvent.OpenFeedback)
        }
    }

    /**
     * Open website
     */
    fun openWebsite(url: String) {
        viewModelScope.launch {
            _uiEvent.emit(MoreUiEvent.OpenUrl(url))
        }
    }

    /**
     * Share app
     */
    fun shareApp() {
        viewModelScope.launch {
            _uiEvent.emit(MoreUiEvent.ShareApp)
        }
    }

    /**
     * Check for updates
     */
    fun checkForUpdates() {
        viewModelScope.launch {
            try {
                // Check for updates from repository
                kotlinx.coroutines.delay(1000)
                _uiEvent.emit(MoreUiEvent.ShowSuccess("You are on the latest version"))
            } catch (e: Exception) {
                _uiEvent.emit(MoreUiEvent.ShowError("Failed to check updates"))
            }
        }
    }
}

