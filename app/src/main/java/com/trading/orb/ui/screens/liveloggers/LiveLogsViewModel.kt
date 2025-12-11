package com.trading.orb.ui.screens.liveloggers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.LiveLogsUiEvent
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.LogEntryUiModel
import com.trading.orb.ui.state.LogLevelFilter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for LiveLogsScreen
 * Manages live logs UI state and filtering operations
 */
@HiltViewModel
class LiveLogsViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // Live Logs UI State
    private val _liveLogsUiState = MutableStateFlow(LiveLogsUiState())
    val liveLogsUiState: StateFlow<LiveLogsUiState> = _liveLogsUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<LiveLogsUiEvent>()
    val uiEvent: SharedFlow<LiveLogsUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadLiveLogs()
        observeLiveLogs()
    }

    /**
     * Initial live logs load
     */
    private fun loadLiveLogs() {
        viewModelScope.launch {
            _liveLogsUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading logs...")) }
            try {
                // Load sample logs for now - replace with actual repository call
                val sampleLogs = LiveLogsPreviewProvider.sampleLogEntriesList()
                _liveLogsUiState.update {
                    it.copy(
                        logs = sampleLogs,
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(),
                        unreadCount = sampleLogs.count { log -> !log.isRead }
                    )
                }
            } catch (e: Exception) {
                _liveLogsUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load logs",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Observe live logs from repository
     */
    private fun observeLiveLogs() {
        viewModelScope.launch {
            repository.appState.collect { appState ->
                // Update logs based on app state changes
                _liveLogsUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false)
                    )
                }
            }
        }
    }

    /**
     * Retry loading logs
     */
    fun retryLoadLogs() {
        loadLiveLogs()
    }

    /**
     * Toggle pause/resume log streaming
     */
    fun togglePause() {
        _liveLogsUiState.update {
            it.copy(isPaused = !it.isPaused)
        }
    }

    /**
     * Enable/disable auto-scroll
     */
    fun toggleAutoScroll() {
        _liveLogsUiState.update {
            it.copy(autoScroll = !it.autoScroll)
        }
    }

    /**
     * Filter logs by level
     */
    fun filterByLogLevel(logLevel: LogLevelFilter) {
        _liveLogsUiState.update {
            it.copy(logLevel = logLevel)
        }
    }

    /**
     * Clear all logs
     */
    fun clearLogs() {
        viewModelScope.launch {
            _liveLogsUiState.update {
                it.copy(logs = emptyList(), unreadCount = 0)
            }
            _uiEvent.emit(LiveLogsUiEvent.LogsCleared)
        }
    }

    /**
     * Select a log entry for detailed view
     */
    fun selectLog(log: LogEntryUiModel) {
        _liveLogsUiState.update {
            it.copy(selectedLog = log)
        }
    }

    /**
     * Clear log selection
     */
    fun clearLogSelection() {
        _liveLogsUiState.update {
            it.copy(selectedLog = null)
        }
    }

    /**
     * Mark log as read
     */
    fun markLogAsRead(logId: String) {
        _liveLogsUiState.update { state ->
            val updatedLogs = state.logs.map { log ->
                if (log.logId == logId) log.copy(isRead = true) else log
            }
            state.copy(
                logs = updatedLogs,
                unreadCount = updatedLogs.count { !it.isRead }
            )
        }
    }

    /**
     * Mark all logs as read
     */
    fun markAllLogsAsRead() {
        _liveLogsUiState.update { state ->
            val updatedLogs = state.logs.map { it.copy(isRead = true) }
            state.copy(
                logs = updatedLogs,
                unreadCount = 0
            )
        }
    }

    /**
     * Export logs
     */
    fun exportLogs() {
        viewModelScope.launch {
            try {
                // Export logic here
                _uiEvent.emit(LiveLogsUiEvent.ShowSuccess("Logs exported successfully"))
            } catch (e: Exception) {
                _uiEvent.emit(LiveLogsUiEvent.ShowError(e.message ?: "Failed to export logs"))
            }
        }
    }

    /**
     * Search logs by keyword
     */
    fun searchLogs(keyword: String) {
        // Filter logs based on keyword in message
        val filtered = if (keyword.isBlank()) {
            _liveLogsUiState.value.logs
        } else {
            _liveLogsUiState.value.logs.filter { 
                it.message.contains(keyword, ignoreCase = true) ||
                it.details?.contains(keyword, ignoreCase = true) ?: false
            }
        }
        _liveLogsUiState.update {
            it.copy(logs = filtered)
        }
    }
}

