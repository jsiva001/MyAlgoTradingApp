package com.trading.orb.ui.mvi.livelogs

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LogLevelFilter
import com.trading.orb.ui.state.LogEntryUiModel

/**
 * Live Logs Screen State
 */
data class LiveLogsScreenState(
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Data
    val logs: List<LogEntryUiModel> = emptyList(),
    val allLogs: List<LogEntryUiModel> = emptyList(),
    
    // Filters
    val selectedLevel: LogLevelFilter = LogLevelFilter.ALL,
    val searchQuery: String = "",
    
    // View state
    val isScrollAtBottom: Boolean = true,
    val autoScroll: Boolean = true,
    val isPaused: Boolean = false
) : MviState

sealed class LiveLogsScreenIntent : MviIntent {
    // Lifecycle
    object LoadLogs : LiveLogsScreenIntent()
    object RetryLoadLogs : LiveLogsScreenIntent()
    object RefreshLogs : LiveLogsScreenIntent()

    // Filtering
    data class FilterByLevel(val level: LogLevelFilter) : LiveLogsScreenIntent()
    data class SearchLogs(val query: String) : LiveLogsScreenIntent()
    object ClearFilters : LiveLogsScreenIntent()

    // View control
    object ToggleAutoScroll : LiveLogsScreenIntent()
    object TogglePause : LiveLogsScreenIntent()
    object ScrollToBottom : LiveLogsScreenIntent()

    // Actions
    object ClearAllLogs : LiveLogsScreenIntent()
    object ExportLogs : LiveLogsScreenIntent()

    // Data updates
    data class UpdateLogs(val logs: List<LogEntryUiModel>) : LiveLogsScreenIntent()
    data class AddNewLog(val log: LogEntryUiModel) : LiveLogsScreenIntent()

    // Error handling
    data class HandleError(val error: Throwable) : LiveLogsScreenIntent()
}

sealed class LiveLogsScreenEffect : MviEffect {
    // Feedback
    data class ShowToast(val message: String) : LiveLogsScreenEffect()
    data class ShowError(val message: String) : LiveLogsScreenEffect()
    data class ShowSuccess(val message: String) : LiveLogsScreenEffect()

    // Navigation
    object NavigateBack : LiveLogsScreenEffect()

    // Actions
    data class ExportLogsFile(val filePath: String) : LiveLogsScreenEffect()
    object ScrollToBottomEffect : LiveLogsScreenEffect()

    // Logging
    data class LogEvent(val eventName: String) : LiveLogsScreenEffect()
}
