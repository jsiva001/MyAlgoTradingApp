package com.trading.orb.ui.mvi.history

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.TradeHistoryUiModel
import com.trading.orb.ui.state.DateRangeUiModel
import com.trading.orb.ui.utils.DEFAULT_SELECTED_TAB

/**
 * Trade History Screen State
 */
data class TradeHistoryScreenState(
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Data
    val trades: List<TradeHistoryUiModel> = emptyList(),
    val selectedTrade: TradeHistoryUiModel? = null,
    
    // Filters
    val dateRange: DateRangeUiModel = DateRangeUiModel(),
    val selectedTab: String = DEFAULT_SELECTED_TAB,
    
    // View state
    val isDetailViewVisible: Boolean = false
) : MviState

sealed class TradeHistoryScreenIntent : MviIntent {
    // Lifecycle
    object LoadTrades : TradeHistoryScreenIntent()
    object RetryLoadTrades : TradeHistoryScreenIntent()
    object RefreshTrades : TradeHistoryScreenIntent()

    // Selection
    data class SelectTrade(val trade: TradeHistoryUiModel) : TradeHistoryScreenIntent()
    object DeselectTrade : TradeHistoryScreenIntent()

    // Filtering
    data class FilterByDateRange(val dateRange: DateRangeUiModel) : TradeHistoryScreenIntent()
    data class FilterByTab(val tab: String) : TradeHistoryScreenIntent()

    // View mode
    object ShowDetailView : TradeHistoryScreenIntent()
    object HideDetailView : TradeHistoryScreenIntent()

    // Actions
    object ExportHistory : TradeHistoryScreenIntent()
    object ClearHistory : TradeHistoryScreenIntent()

    // Data updates
    data class UpdateTrades(val trades: List<TradeHistoryUiModel>) : TradeHistoryScreenIntent()

    // Error handling
    data class HandleError(val error: Throwable) : TradeHistoryScreenIntent()
}

sealed class TradeHistoryScreenEffect : MviEffect {
    // Feedback
    data class ShowToast(val message: String) : TradeHistoryScreenEffect()
    data class ShowError(val message: String) : TradeHistoryScreenEffect()
    data class ShowSuccess(val message: String) : TradeHistoryScreenEffect()

    // Navigation
    object NavigateBack : TradeHistoryScreenEffect()

    // Actions
    data class ExportFile(val filePath: String) : TradeHistoryScreenEffect()

    // Logging
    data class LogEvent(val eventName: String) : TradeHistoryScreenEffect()
}
