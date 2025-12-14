package com.trading.orb.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.*
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.DashboardUiEvent
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for DashboardScreen
 * Manages dashboard-specific UI state and user interactions
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // Global app state from repository
    val appState: StateFlow<AppState> = repository.appState
        .map { it.toAppState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppState()
        )

    // Dashboard UI State
    private val _dashboardUiState = MutableStateFlow(DashboardUiState())
    val dashboardUiState: StateFlow<DashboardUiState> = _dashboardUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<DashboardUiEvent>()
    val uiEvent: SharedFlow<DashboardUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadDashboard()
        observeAppState()
    }

    /**
     * Initial dashboard load
     */
    private fun loadDashboard() {
        viewModelScope.launch {
            _dashboardUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading dashboard...")) }
            try {
                // Data is continuously streamed from repository
                _dashboardUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _dashboardUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load dashboard",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Observe repository app state changes and update dashboard state
     */
    private fun observeAppState() {
        viewModelScope.launch {
            repository.appState.collect { repoState ->
                _dashboardUiState.update {
                    it.copy(
                        dailyStats = repoState.dailyStats,
                        orbLevels = repoState.orbLevels,
                        loading = LoadingState(isLoading = false)
                    )
                }
            }
        }
    }

    /**
     * Retry loading dashboard
     */
    fun retryDashboard() {
        loadDashboard()
    }

    /**
     * Refresh dashboard data
     */
    fun refreshDashboard() {
        viewModelScope.launch {
            _dashboardUiState.update { it.copy(isRefreshing = true) }
            try {
                // Trigger repository refresh - simulate with delay
                kotlinx.coroutines.delay(1000)
                _dashboardUiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _dashboardUiState.update {
                    it.copy(
                        isRefreshing = false,
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to refresh",
                            isRetryable = true
                        )
                    )
                }
            }
        }
    }

    /**
     * Toggle trading strategy on/off
     */
    fun toggleStrategy() {
        viewModelScope.launch {
            if (appState.value.strategyStatus == StrategyStatus.ACTIVE) {
                // Stop strategy
                repository.stopStrategy().onFailure { error ->
                    _uiEvent.emit(DashboardUiEvent.ShowError(error.message ?: "Failed to stop strategy"))
                }.onSuccess {
                    _uiEvent.emit(DashboardUiEvent.ShowSuccess("Strategy stopped"))
                }
            } else {
                // Start strategy via repository
                repository.startStrategy().onFailure { error ->
                    _uiEvent.emit(DashboardUiEvent.ShowError(error.message ?: "Failed to start strategy"))
                }.onSuccess {
                    _uiEvent.emit(DashboardUiEvent.ShowSuccess("Strategy started!"))
                }
            }
        }
    }

    /**
     * Toggle between Paper and Live trading modes
     */
    fun toggleTradingMode() {
        viewModelScope.launch {
            repository.toggleTradingMode().onFailure { error ->
                _uiEvent.emit(DashboardUiEvent.ShowError(error.message ?: "Failed to toggle mode"))
            }
        }
    }

    /**
     * Emergency stop - stops strategy and closes all positions
     */
    fun emergencyStop() {
        viewModelScope.launch {
            repository.stopStrategy()
            repository.closeAllPositions().onFailure { error ->
                _uiEvent.emit(DashboardUiEvent.ShowError(error.message ?: "Emergency stop failed"))
            }.onSuccess {
                _uiEvent.emit(DashboardUiEvent.ShowSuccess("Emergency stop executed"))
            }
        }
    }
}

/**
 * Extension function to convert repository AppState to UI AppState
 */
fun com.trading.orb.data.model.AppState.toAppState(): AppState {
    return AppState(
        tradingMode = this.tradingMode,
        strategyStatus = this.strategyStatus,
        connectionStatus = this.connectionStatus,
        dailyStats = this.dailyStats,
        orbLevels = this.orbLevels
    )
}
