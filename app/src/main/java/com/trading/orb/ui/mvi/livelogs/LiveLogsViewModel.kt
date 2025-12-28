package com.trading.orb.ui.mvi.livelogs

import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.LogLevelFilter
import com.trading.orb.ui.state.LogEntryUiModel
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ErrorMessages
import com.trading.orb.ui.utils.DEFAULT_DELAY_MS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Live Logs Screen ViewModel with MVI Architecture
 *
 * Manages real-time log display and filtering
 */
@HiltViewModel
class LiveLogsViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<LiveLogsScreenState, LiveLogsScreenIntent, LiveLogsScreenEffect>() {

    override fun createInitialState(): LiveLogsScreenState = LiveLogsScreenState()

    /**
     * Pure reducer function
     */
    override fun reduce(currentState: LiveLogsScreenState, intent: LiveLogsScreenIntent): LiveLogsScreenState {
        return when (intent) {
            is LiveLogsScreenIntent.LoadLogs -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is LiveLogsScreenIntent.RetryLoadLogs -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is LiveLogsScreenIntent.RefreshLogs -> {
                currentState.copy(isRefreshing = true)
            }

            is LiveLogsScreenIntent.FilterByLevel -> {
                val filtered = filterLogs(currentState.allLogs, intent.level, currentState.searchQuery)
                currentState.copy(
                    selectedLevel = intent.level,
                    logs = filtered
                )
            }

            is LiveLogsScreenIntent.SearchLogs -> {
                val filtered = filterLogs(currentState.allLogs, currentState.selectedLevel, intent.query)
                currentState.copy(
                    searchQuery = intent.query,
                    logs = filtered
                )
            }

            is LiveLogsScreenIntent.ClearFilters -> {
                currentState.copy(
                    selectedLevel = LogLevelFilter.ALL,
                    searchQuery = "",
                    logs = currentState.allLogs
                )
            }

            is LiveLogsScreenIntent.ToggleAutoScroll -> {
                currentState.copy(autoScroll = !currentState.autoScroll)
            }

            is LiveLogsScreenIntent.TogglePause -> {
                currentState.copy(isPaused = !currentState.isPaused)
            }

            is LiveLogsScreenIntent.ScrollToBottom -> {
                currentState.copy(isScrollAtBottom = true)
            }

            is LiveLogsScreenIntent.UpdateLogs -> {
                val filtered = filterLogs(intent.logs, currentState.selectedLevel, currentState.searchQuery)
                currentState.copy(
                    logs = filtered,
                    allLogs = intent.logs,
                    loading = LoadingState(isLoading = false),
                    error = ErrorState()
                )
            }

            is LiveLogsScreenIntent.AddNewLog -> {
                if (!currentState.isPaused) {
                    val updatedAllLogs = listOf(intent.log) + currentState.allLogs
                    val filtered = filterLogs(updatedAllLogs, currentState.selectedLevel, currentState.searchQuery)
                    currentState.copy(
                        logs = filtered,
                        allLogs = updatedAllLogs,
                        isScrollAtBottom = if (currentState.autoScroll) true else currentState.isScrollAtBottom
                    )
                } else {
                    currentState
                }
            }

            is LiveLogsScreenIntent.HandleError -> {
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

            // Action-only intents
            is LiveLogsScreenIntent.ClearAllLogs,
            is LiveLogsScreenIntent.ExportLogs -> currentState
        }
    }

    /**
     * Handle intents with side effects
     */
    override suspend fun handleIntent(intent: LiveLogsScreenIntent) {
        when (intent) {
            is LiveLogsScreenIntent.LoadLogs -> loadLogsData()
            is LiveLogsScreenIntent.RetryLoadLogs -> loadLogsData()
            is LiveLogsScreenIntent.RefreshLogs -> refreshLogsData()

            is LiveLogsScreenIntent.FilterByLevel -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.SearchLogs -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.ClearFilters -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.ToggleAutoScroll -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.TogglePause -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.ScrollToBottom -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(LiveLogsScreenEffect.ScrollToBottomEffect)
            }

            is LiveLogsScreenIntent.ClearAllLogs -> clearAllLogsWithSideEffects()
            is LiveLogsScreenIntent.ExportLogs -> exportLogsWithSideEffects()

            is LiveLogsScreenIntent.UpdateLogs -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.AddNewLog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is LiveLogsScreenIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(LiveLogsScreenEffect.ShowError(intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED))
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadLogsData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate loading logs
            kotlinx.coroutines.delay(300)

            // In real implementation, fetch from repository
            val initialLogs = listOf(
                LogEntryUiModel(
                    logId = "1",
                    level = "INFO",
                    message = "App started",
                    timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_TIME),
                    source = "MainActivity"
                )
            )

            processIntent(LiveLogsScreenIntent.UpdateLogs(initialLogs))
            emitEffect(LiveLogsScreenEffect.LogEvent("logs_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.LOGS_FAILED_LOAD)
            processIntent(LiveLogsScreenIntent.HandleError(e))
        }
    }

    private suspend fun refreshLogsData() {
        try {
            kotlinx.coroutines.delay(500)
            updateStateImmediate(state.value.copy(isRefreshing = false))
            emitEffect(LiveLogsScreenEffect.ShowSuccess("Logs refreshed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.LOGS_FAILED_REFRESH)
            emitEffect(LiveLogsScreenEffect.ShowError(e.message ?: ErrorMessages.REFRESH_FAILED))
        }
    }

    private suspend fun clearAllLogsWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(
                    logs = emptyList(),
                    allLogs = emptyList()
                )
            )
            emitEffect(LiveLogsScreenEffect.ShowSuccess("All logs cleared"))
            emitEffect(LiveLogsScreenEffect.LogEvent("logs_cleared"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.LOGS_FAILED_CLEAR)
            emitEffect(LiveLogsScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private suspend fun exportLogsWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate export delay
            kotlinx.coroutines.delay(DEFAULT_DELAY_MS)

            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = false))
            )

            emitEffect(LiveLogsScreenEffect.ExportLogsFile("/logs/export_${System.currentTimeMillis()}.txt"))
            emitEffect(LiveLogsScreenEffect.ShowSuccess("Logs exported successfully"))
            emitEffect(LiveLogsScreenEffect.LogEvent("logs_exported"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.LOGS_FAILED_EXPORT)
            emitEffect(LiveLogsScreenEffect.ShowError(ErrorMessages.FAILED_SAVE_CONFIGURATION))
        }
    }

    private fun filterLogs(
        logs: List<LogEntryUiModel>,
        levelFilter: LogLevelFilter,
        searchQuery: String
    ): List<LogEntryUiModel> {
        var filtered = logs

        // Filter by level
        if (levelFilter != LogLevelFilter.ALL) {
            filtered = filtered.filter { it.level.uppercase() == levelFilter.name }
        }

        // Filter by search query
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { log ->
                log.message.contains(searchQuery, ignoreCase = true) ||
                log.source.contains(searchQuery, ignoreCase = true)
            }
        }

        return filtered
    }
}
