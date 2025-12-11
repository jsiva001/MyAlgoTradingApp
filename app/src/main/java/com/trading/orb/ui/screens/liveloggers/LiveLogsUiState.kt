package com.trading.orb.ui.screens.liveloggers

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.LogEntryUiModel
import com.trading.orb.ui.state.LogLevelFilter

/**
 * UI State specific to Live Logs Screen
 * Manages all UI-related data for live logs display
 */
data class LiveLogsUiState(
    val logs: List<LogEntryUiModel> = emptyList(),
    val logLevel: LogLevelFilter = LogLevelFilter.ALL,
    val autoScroll: Boolean = true,
    val isPaused: Boolean = false,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val selectedLog: LogEntryUiModel? = null,
    val unreadCount: Int = 0
)
