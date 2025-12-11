package com.trading.orb.ui.screens.liveloggers

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.LogEntryUiModel
import com.trading.orb.ui.state.LogLevelFilter

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in LiveLogsScreen
 */
object LiveLogsPreviewProvider {

    fun sampleLogEntryUiModel(
        logId: String = "LOG001",
        level: String = "INFO",
        message: String = "Strategy started successfully",
        source: String = "StrategyEngine",
        isRead: Boolean = true
    ): LogEntryUiModel {
        return LogEntryUiModel(
            logId = logId,
            level = level,
            message = message,
            timestamp = "2024-12-11T09:30:00",
            source = source,
            details = "Additional details about the log entry",
            isRead = isRead
        )
    }

    fun sampleLogEntriesList(): List<LogEntryUiModel> {
        return listOf(
            sampleLogEntryUiModel(
                logId = "LOG001",
                level = "INFO",
                message = "Strategy started successfully",
                source = "StrategyEngine"
            ),
            sampleLogEntryUiModel(
                logId = "LOG002",
                level = "DEBUG",
                message = "ORB levels calculated: High=188.0, Low=183.0",
                source = "OrbCalculator"
            ),
            sampleLogEntryUiModel(
                logId = "LOG003",
                level = "INFO",
                message = "Position opened: NIFTY50 LONG at 20500.0",
                source = "PositionManager"
            ),
            sampleLogEntryUiModel(
                logId = "LOG004",
                level = "WARNING",
                message = "Drawdown approaching limit: 45% of max allowed",
                source = "RiskManager",
                isRead = false
            ),
            sampleLogEntryUiModel(
                logId = "LOG005",
                level = "ERROR",
                message = "Failed to place order: Connection timeout",
                source = "OrderManager",
                isRead = false
            )
        )
    }

    fun sampleLiveLogsUiState(
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load logs",
        logLevel: LogLevelFilter = LogLevelFilter.ALL,
        isPaused: Boolean = false
    ): LiveLogsUiState {
        val logs = sampleLogEntriesList()
        return LiveLogsUiState(
            logs = logs,
            logLevel = logLevel,
            autoScroll = true,
            isPaused = isPaused,
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading logs..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            selectedLog = null,
            unreadCount = logs.count { !it.isRead }
        )
    }

    fun sampleLiveLogsUiStateLoading(): LiveLogsUiState {
        return sampleLiveLogsUiState(isLoading = true)
    }

    fun sampleLiveLogsUiStateError(): LiveLogsUiState {
        return sampleLiveLogsUiState(hasError = true)
    }

    fun sampleLiveLogsUiStateEmpty(): LiveLogsUiState {
        return LiveLogsUiState(
            logs = emptyList(),
            loading = LoadingState(isLoading = false),
            unreadCount = 0
        )
    }

    fun sampleLiveLogsUiStatePaused(): LiveLogsUiState {
        return sampleLiveLogsUiState(isPaused = true)
    }

    fun sampleLiveLogsUiStateFiltered(logLevel: LogLevelFilter): LiveLogsUiState {
        val allLogs = sampleLogEntriesList()
        val filtered = when (logLevel) {
            LogLevelFilter.INFO -> allLogs.filter { it.level == "INFO" }
            LogLevelFilter.DEBUG -> allLogs.filter { it.level == "DEBUG" }
            LogLevelFilter.WARNING -> allLogs.filter { it.level == "WARNING" }
            LogLevelFilter.ERROR -> allLogs.filter { it.level == "ERROR" }
            LogLevelFilter.ALL -> allLogs
        }
        return sampleLiveLogsUiState(logLevel = logLevel).copy(logs = filtered)
    }

    fun sampleLiveLogsUiStateWithSelectedLog(): LiveLogsUiState {
        val logs = sampleLogEntriesList()
        return sampleLiveLogsUiState().copy(
            selectedLog = logs.firstOrNull()
        )
    }
}
