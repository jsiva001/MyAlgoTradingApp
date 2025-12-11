package com.trading.orb.ui.screens.tradehistory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.Trade
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.TradeHistoryUiEvent
import com.trading.orb.ui.state.DateRangeUiModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.SortType
import com.trading.orb.ui.state.TradeHistoryUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for TradeHistoryScreen
 * Manages trade history UI state and filtering/sorting operations
 */
@HiltViewModel
class TradeHistoryViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // Trades data from repository
    val trades: StateFlow<List<Trade>> = repository.trades
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Trade History UI State
    private val _tradeHistoryUiState = MutableStateFlow(TradeHistoryUiState())
    val tradeHistoryUiState: StateFlow<TradeHistoryUiState> = _tradeHistoryUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<TradeHistoryUiEvent>()
    val uiEvent: SharedFlow<TradeHistoryUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadTradeHistory()
        observeTradesHistory()
    }

    /**
     * Initial trade history load
     */
    fun loadTradeHistory() {
        viewModelScope.launch {
            _tradeHistoryUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading trade history...")) }
            try {
                // Load sample data for now
                val sampleTrades = listOf(
                    TradeHistoryUiModel(
                        tradeId = "1",
                        symbol = "AAPL",
                        tradeType = "BUY",
                        quantity = 100,
                        entryPrice = 145.0,
                        exitPrice = 152.0,
                        profitLoss = 700.0,
                        profitLossPercent = 4.83,
                        duration = "2h 30m",
                        status = "PROFIT",
                        entryTime = "2024-01-09 10:00",
                        exitTime = "2024-01-09 12:30",
                        reason = "TARGET"
                    ),
                    TradeHistoryUiModel(
                        tradeId = "2",
                        symbol = "GOOGL",
                        tradeType = "SELL",
                        quantity = 50,
                        entryPrice = 140.0,
                        exitPrice = 138.0,
                        profitLoss = 100.0,
                        profitLossPercent = 1.43,
                        duration = "1h 15m",
                        status = "PROFIT",
                        entryTime = "2024-01-09 11:00",
                        exitTime = "2024-01-09 12:15",
                        reason = "TARGET"
                    ),
                    TradeHistoryUiModel(
                        tradeId = "3",
                        symbol = "MSFT",
                        tradeType = "BUY",
                        quantity = 75,
                        entryPrice = 380.0,
                        exitPrice = 375.0,
                        profitLoss = -375.0,
                        profitLossPercent = -1.32,
                        duration = "45m",
                        status = "LOSS",
                        entryTime = "2024-01-09 14:00",
                        exitTime = "2024-01-09 14:45",
                        reason = "STOPLOSS"
                    ),
                    TradeHistoryUiModel(
                        tradeId = "4",
                        symbol = "TSLA",
                        tradeType = "BUY",
                        quantity = 50,
                        entryPrice = 250.0,
                        exitPrice = 255.0,
                        profitLoss = 250.0,
                        profitLossPercent = 2.0,
                        duration = "3h",
                        status = "PROFIT",
                        entryTime = "2024-01-09 09:30",
                        exitTime = "2024-01-09 12:30",
                        reason = "TARGET"
                    )
                )
                
                val statistics = calculateTradeStatisticsFromUiModels(sampleTrades)
                
                _tradeHistoryUiState.update {
                    it.copy(
                        trades = sampleTrades,
                        statistics = statistics,
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _tradeHistoryUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load trade history",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Observe repository trades changes
     */
    private fun observeTradesHistory() {
        viewModelScope.launch {
            trades.collect { tradeList ->
                val sortedTrades = sortTrades(tradeList, _tradeHistoryUiState.value.sortType)
                _tradeHistoryUiState.update {
                    it.copy(
                        trades = mapTradesToUiModels(sortedTrades),
                        statistics = calculateTradeStatistics(sortedTrades),
                        loading = LoadingState(isLoading = false)
                    )
                }
            }
        }
    }

    /**
     * Retry loading trade history
     */
    fun retryTradeHistory() {
        loadTradeHistory()
    }

    /**
     * Refresh trade history
     */
    fun refreshTradeHistory() {
        viewModelScope.launch {
            _tradeHistoryUiState.update { it.copy(isRefreshing = true) }
            try {
                // Trigger repository refresh
                kotlinx.coroutines.delay(1000)
                _tradeHistoryUiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _tradeHistoryUiState.update {
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
     * Change sort order
     */
    fun changeSortOrder(sortType: SortType) {
        _tradeHistoryUiState.update {
            it.copy(
                sortType = sortType,
                trades = mapTradesToUiModels(
                    sortTrades(trades.value, sortType)
                )
            )
        }
    }

    /**
     * Update date range filter
     */
    fun updateDateRange(dateRange: DateRangeUiModel) {
        _tradeHistoryUiState.update {
            it.copy(filterDateRange = dateRange)
        }
    }

    /**
     * Select a trade for detailed view
     */
    fun selectTrade(trade: TradeHistoryUiModel) {
        _tradeHistoryUiState.update {
            it.copy(selectedTrade = trade)
        }
    }

    /**
     * Clear trade selection
     */
    fun clearTradeSelection() {
        _tradeHistoryUiState.update {
            it.copy(selectedTrade = null)
        }
    }

    /**
     * Export trade history
     */
    fun exportTradeHistory() {
        viewModelScope.launch {
            try {
                // Export logic here
                _uiEvent.emit(TradeHistoryUiEvent.ShowSuccess("Trade history exported"))
            } catch (e: Exception) {
                _uiEvent.emit(TradeHistoryUiEvent.ShowError(e.message ?: "Failed to export"))
            }
        }
    }

    // =========================================================================
    // Helper functions
    // =========================================================================

    private fun calculateTradeStatisticsFromUiModels(trades: List<TradeHistoryUiModel>) = 
        com.trading.orb.ui.state.TradeStatisticsUiModel(
            totalTrades = trades.size,
            winningTrades = trades.count { it.status == "PROFIT" },
            losingTrades = trades.count { it.status == "LOSS" },
            winRate = if (trades.isNotEmpty()) (trades.count { it.status == "PROFIT" }.toDouble() / trades.size) * 100 else 0.0,
            totalProfit = trades.filter { it.status == "PROFIT" }.sumOf { it.profitLoss },
            totalLoss = trades.filter { it.status == "LOSS" }.sumOf { it.profitLoss },
            netProfit = trades.sumOf { it.profitLoss },
            averageWin = if (trades.any { it.status == "PROFIT" }) trades.filter { it.status == "PROFIT" }.map { it.profitLoss }.average() else 0.0,
            averageLoss = if (trades.any { it.status == "LOSS" }) trades.filter { it.status == "LOSS" }.map { it.profitLoss }.average() else 0.0,
            profitFactor = calculateProfitFactorFromUiModels(trades),
            expectancy = if (trades.isNotEmpty()) trades.sumOf { it.profitLoss } / trades.size else 0.0
        )

    private fun calculateProfitFactorFromUiModels(trades: List<TradeHistoryUiModel>): Double {
        val profit = trades.filter { it.status == "PROFIT" }.sumOf { it.profitLoss }
        val loss = kotlin.math.abs(trades.filter { it.status == "LOSS" }.sumOf { it.profitLoss })
        return if (loss > 0) profit / loss else 0.0
    }

    private fun sortTrades(trades: List<Trade>, sortType: SortType): List<Trade> {
        return when (sortType) {
            SortType.RECENT -> trades.sortedByDescending { it.exitTime }
            SortType.OLDEST -> trades.sortedBy { it.exitTime }
            SortType.PROFIT_HIGH -> trades.sortedByDescending { it.pnl }
            SortType.PROFIT_LOW -> trades.sortedBy { it.pnl }
        }
    }

    private fun mapTradesToUiModels(trades: List<Trade>): List<TradeHistoryUiModel> {
        return trades.map { trade ->
            TradeHistoryUiModel(
                tradeId = trade.id,
                symbol = trade.instrument.symbol,
                tradeType = trade.side.name,
                quantity = trade.quantity,
                entryPrice = trade.entryPrice,
                exitPrice = trade.exitPrice,
                profitLoss = trade.netPnl,
                profitLossPercent = trade.pnlPercentage,
                duration = "${trade.duration}m",
                status = if (trade.isProfit) "PROFIT" else if (trade.pnl < 0) "LOSS" else "BREAKEVEN",
                entryTime = trade.entryTime.toString(),
                exitTime = trade.exitTime.toString(),
                reason = trade.exitReason.name
            )
        }
    }

    private fun calculateTradeStatistics(trades: List<Trade>) = 
        com.trading.orb.ui.state.TradeStatisticsUiModel(
            totalTrades = trades.size,
            winningTrades = trades.count { it.isProfit },
            losingTrades = trades.count { !it.isProfit },
            winRate = if (trades.isNotEmpty()) (trades.count { it.isProfit }.toDouble() / trades.size) * 100 else 0.0,
            totalProfit = trades.filter { it.isProfit }.sumOf { it.netPnl },
            totalLoss = trades.filter { !it.isProfit }.sumOf { it.netPnl },
            netProfit = trades.sumOf { it.netPnl },
            averageWin = if (trades.any { it.isProfit }) trades.filter { it.isProfit }.map { it.netPnl }.average() else 0.0,
            averageLoss = if (trades.any { !it.isProfit }) trades.filter { !it.isProfit }.map { it.netPnl }.average() else 0.0,
            profitFactor = calculateProfitFactor(trades),
            expectancy = calculateExpectancy(trades)
        )

    private fun calculateProfitFactor(trades: List<Trade>): Double {
        val profit = trades.filter { it.isProfit }.sumOf { it.netPnl }
        val loss = kotlin.math.abs(trades.filter { !it.isProfit }.sumOf { it.netPnl })
        return if (loss > 0) profit / loss else 0.0
    }

    private fun calculateExpectancy(trades: List<Trade>): Double {
        return if (trades.isNotEmpty()) trades.sumOf { it.netPnl } / trades.size else 0.0
    }

    private fun calculateProfitLossPercent(entryPrice: Double, exitPrice: Double): Double {
        return if (entryPrice > 0) ((exitPrice - entryPrice) / entryPrice) * 100 else 0.0
    }

    private fun calculateDuration(entryTime: Any, exitTime: Any): String {
        // Format the duration string based on entry and exit times
        return "0h 0m"
    }
}

