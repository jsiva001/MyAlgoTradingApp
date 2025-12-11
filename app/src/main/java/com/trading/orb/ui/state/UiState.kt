package com.trading.orb.ui.state

/**
 * Base UI State for all screens
 * Represents the loading, error, and success states
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val exception: Throwable? = null) : UiState<Nothing>()
    data object Idle : UiState<Nothing>()
}

/**
 * Common UI Event for user interactions
 */
sealed class UiEvent {
    data object Retry : UiEvent()
    data object Dismiss : UiEvent()
    data class OnAction(val action: String) : UiEvent()
}

/**
 * Common loading state
 * Used across all screens for consistent loading indicators
 */
data class LoadingState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Loading..."
)

/**
 * Common error state
 * Used across all screens for consistent error handling
 */
data class ErrorState(
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val isRetryable: Boolean = true,
    val throwable: Throwable? = null
)

// ============================================================================
// Enum classes for filtering and sorting (Shared across screens)
// ============================================================================

/**
 * Filter types for positions
 */
enum class PositionFilterType {
    ALL, LONG, SHORT, BREAKEVEN, PROFIT, LOSS
}

/**
 * Log level filter for live logs
 */
enum class LogLevelFilter {
    ALL, INFO, DEBUG, WARNING, ERROR
}

/**
 * Sort types for trade history
 */
enum class SortType {
    RECENT, OLDEST, PROFIT_HIGH, PROFIT_LOW
}

/**
 * History filter for trade history
 */
enum class HistoryFilter {
    ALL, TODAY, WEEK, MONTH
}

