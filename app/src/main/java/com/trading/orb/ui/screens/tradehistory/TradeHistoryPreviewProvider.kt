package com.trading.orb.ui.screens.tradehistory

import com.trading.orb.ui.state.DateRangeUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.SortType
import com.trading.orb.ui.state.TradeHistoryUiModel
import com.trading.orb.ui.state.TradeStatisticsUiModel

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in TradeHistoryScreen
 */
object TradeHistoryPreviewProvider {

    fun sampleTradeHistoryUiModel(
        tradeId: String = "TRADE001",
        symbol: String = "NIFTY50",
        tradeType: String = "BUY",
        quantity: Int = 1,
        entryPrice: Double = 20500.0,
        exitPrice: Double = 20650.0,
        profitLoss: Double = 150.0,
        profitLossPercent: Double = 0.73,
        status: String = "PROFIT"
    ): TradeHistoryUiModel {
        return TradeHistoryUiModel(
            tradeId = tradeId,
            symbol = symbol,
            tradeType = tradeType,
            quantity = quantity,
            entryPrice = entryPrice,
            exitPrice = exitPrice,
            profitLoss = profitLoss,
            profitLossPercent = profitLossPercent,
            duration = "2h 30m",
            status = status,
            entryTime = "2024-12-11T09:30:00",
            exitTime = "2024-12-11T12:00:00",
            reason = "TARGET_HIT"
        )
    }

    fun sampleTradeHistoryList(): List<TradeHistoryUiModel> {
        return listOf(
            sampleTradeHistoryUiModel(
                tradeId = "TRADE001",
                symbol = "NIFTY50",
                profitLoss = 150.0,
                status = "PROFIT"
            ),
            sampleTradeHistoryUiModel(
                tradeId = "TRADE002",
                symbol = "FINNIFTY",
                tradeType = "SELL",
                entryPrice = 21500.0,
                exitPrice = 21350.0,
                profitLoss = 150.0,
                status = "PROFIT"
            ),
            sampleTradeHistoryUiModel(
                tradeId = "TRADE003",
                symbol = "BANKNIFTY",
                entryPrice = 48500.0,
                exitPrice = 48300.0,
                profitLoss = -200.0,
                status = "LOSS"
            ),
            sampleTradeHistoryUiModel(
                tradeId = "TRADE004",
                symbol = "NIFTY50",
                entryPrice = 20600.0,
                exitPrice = 20600.0,
                profitLoss = 0.0,
                status = "BREAKEVEN"
            )
        )
    }

    fun sampleTradeStatistics(): TradeStatisticsUiModel {
        return TradeStatisticsUiModel(
            totalTrades = 4,
            winningTrades = 3,
            losingTrades = 1,
            winRate = 75.0,
            totalProfit = 300.0,
            totalLoss = -200.0,
            netProfit = 100.0,
            averageWin = 100.0,
            averageLoss = -200.0,
            profitFactor = 1.5,
            expectancy = 25.0
        )
    }

    fun sampleTradeHistoryUiState(
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load trade history",
        sortType: SortType = SortType.RECENT
    ): TradeHistoryUiState {
        val trades = sampleTradeHistoryList()
        return TradeHistoryUiState(
            trades = trades,
            statistics = sampleTradeStatistics(),
            selectedTrade = null,
            filterDateRange = DateRangeUiModel(rangeType = "ALL"),
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading trade history..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            isRefreshing = false,
            sortType = sortType
        )
    }

    fun sampleTradeHistoryUiStateLoading(): TradeHistoryUiState {
        return sampleTradeHistoryUiState(isLoading = true)
    }

    fun sampleTradeHistoryUiStateError(): TradeHistoryUiState {
        return sampleTradeHistoryUiState(hasError = true)
    }

    fun sampleTradeHistoryUiStateEmpty(): TradeHistoryUiState {
        return TradeHistoryUiState(
            trades = emptyList(),
            statistics = TradeStatisticsUiModel(),
            loading = LoadingState(isLoading = false)
        )
    }

    fun sampleTradeHistoryUiStateWithSelection(): TradeHistoryUiState {
        val trades = sampleTradeHistoryList()
        return sampleTradeHistoryUiState().copy(
            selectedTrade = trades.firstOrNull()
        )
    }
}
