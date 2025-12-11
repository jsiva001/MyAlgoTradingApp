package com.trading.orb.ui.screens.tradehistory

import com.trading.orb.ui.state.DateRangeUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.SortType
import com.trading.orb.ui.state.TradeHistoryUiModel
import com.trading.orb.ui.state.TradeStatisticsUiModel

/**
 * UI State specific to Trade History Screen
 * Manages all UI-related data for trade history display
 */
data class TradeHistoryUiState(
    val trades: List<TradeHistoryUiModel> = emptyList(),
    val statistics: TradeStatisticsUiModel = TradeStatisticsUiModel(),
    val selectedTrade: TradeHistoryUiModel? = null,
    val filterDateRange: DateRangeUiModel = DateRangeUiModel(),
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    val sortType: SortType = SortType.RECENT
)
