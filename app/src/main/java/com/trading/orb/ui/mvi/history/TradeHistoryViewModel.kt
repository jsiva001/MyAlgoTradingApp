package com.trading.orb.ui.mvi.history

import androidx.lifecycle.viewModelScope
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.TradeHistoryUiModel
import com.trading.orb.ui.utils.EXPORT_DIRECTORY
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ErrorMessages
import com.trading.orb.ui.utils.TRADE_FILTER_ALL
import com.trading.orb.ui.utils.TRADE_FILTER_PROFIT
import com.trading.orb.ui.utils.TRADE_FILTER_LOSS
import com.trading.orb.ui.utils.TRADE_HISTORY_EXPORT_FORMAT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDate
import javax.inject.Inject

/**
 * Trade History Screen ViewModel with MVI Architecture
 *
 * Manages historical trade data and filtering
 */
@HiltViewModel
class TradeHistoryViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<TradeHistoryScreenState, TradeHistoryScreenIntent, TradeHistoryScreenEffect>() {

    override fun createInitialState(): TradeHistoryScreenState = TradeHistoryScreenState()

    /**
     * Pure reducer function
     */
    override fun reduce(currentState: TradeHistoryScreenState, intent: TradeHistoryScreenIntent): TradeHistoryScreenState {
        return when (intent) {
            is TradeHistoryScreenIntent.LoadTrades -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is TradeHistoryScreenIntent.RetryLoadTrades -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is TradeHistoryScreenIntent.RefreshTrades -> {
                currentState.copy(isRefreshing = true)
            }

            is TradeHistoryScreenIntent.SelectTrade -> {
                currentState.copy(selectedTrade = intent.trade)
            }

            is TradeHistoryScreenIntent.DeselectTrade -> {
                currentState.copy(selectedTrade = null)
            }

            is TradeHistoryScreenIntent.FilterByDateRange -> {
                val filtered = filterTradesByDate(currentState.trades, intent.dateRange)
                currentState.copy(
                    dateRange = intent.dateRange,
                    trades = filtered
                )
            }

            is TradeHistoryScreenIntent.FilterByTab -> {
                val filtered = filterTradesByTab(currentState.trades, intent.tab)
                currentState.copy(
                    selectedTab = intent.tab,
                    trades = filtered
                )
            }

            is TradeHistoryScreenIntent.ShowDetailView -> {
                currentState.copy(isDetailViewVisible = true)
            }

            is TradeHistoryScreenIntent.HideDetailView -> {
                currentState.copy(isDetailViewVisible = false)
            }

            is TradeHistoryScreenIntent.UpdateTrades -> {
                val filtered = filterTradesByTab(intent.trades, currentState.selectedTab)
                currentState.copy(
                    trades = filtered,
                    loading = LoadingState(isLoading = false),
                    error = ErrorState()
                )
            }

            is TradeHistoryScreenIntent.HandleError -> {
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
            is TradeHistoryScreenIntent.ExportHistory,
            is TradeHistoryScreenIntent.ClearHistory -> currentState
        }
    }

    /**
     * Handle intents with side effects
     */
    override suspend fun handleIntent(intent: TradeHistoryScreenIntent) {
        when (intent) {
            is TradeHistoryScreenIntent.LoadTrades -> loadTradesData()
            is TradeHistoryScreenIntent.RetryLoadTrades -> loadTradesData()
            is TradeHistoryScreenIntent.RefreshTrades -> refreshTradesData()

            is TradeHistoryScreenIntent.SelectTrade -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.DeselectTrade -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.FilterByDateRange -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.FilterByTab -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.ShowDetailView -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.HideDetailView -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.ExportHistory -> exportHistoryWithSideEffects()
            is TradeHistoryScreenIntent.ClearHistory -> clearHistoryWithSideEffects()

            is TradeHistoryScreenIntent.UpdateTrades -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is TradeHistoryScreenIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(TradeHistoryScreenEffect.ShowError(intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED))
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadTradesData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate loading trades
            kotlinx.coroutines.delay(300)

            // In real implementation, fetch from repository
            val trades = emptyList<TradeHistoryUiModel>()

            processIntent(TradeHistoryScreenIntent.UpdateTrades(trades))
            emitEffect(TradeHistoryScreenEffect.LogEvent("trades_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.HISTORY_FAILED_LOAD)
            processIntent(TradeHistoryScreenIntent.HandleError(e))
        }
    }

    private suspend fun refreshTradesData() {
        try {
            kotlinx.coroutines.delay(500)
            updateStateImmediate(state.value.copy(isRefreshing = false))
            emitEffect(TradeHistoryScreenEffect.ShowSuccess("Trade history refreshed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.HISTORY_FAILED_REFRESH)
            emitEffect(TradeHistoryScreenEffect.ShowError(e.message ?: ErrorMessages.REFRESH_FAILED))
        }
    }

    private suspend fun exportHistoryWithSideEffects() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // Simulate export delay
            kotlinx.coroutines.delay(1000)

            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = false))
            )

            val fileName = String.format(TRADE_HISTORY_EXPORT_FORMAT, System.currentTimeMillis())
            emitEffect(TradeHistoryScreenEffect.ExportFile("$EXPORT_DIRECTORY$fileName"))
            emitEffect(TradeHistoryScreenEffect.ShowSuccess("History exported successfully"))
            emitEffect(TradeHistoryScreenEffect.LogEvent("history_exported"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.HISTORY_FAILED_EXPORT)
            emitEffect(TradeHistoryScreenEffect.ShowError("Failed to export history"))
        }
    }

    private suspend fun clearHistoryWithSideEffects() {
        try {
            // Confirm before clearing
            updateStateImmediate(
                state.value.copy(
                    trades = emptyList(),
                    selectedTrade = null
                )
            )
            emitEffect(TradeHistoryScreenEffect.ShowSuccess("Trade history cleared"))
            emitEffect(TradeHistoryScreenEffect.LogEvent("history_cleared"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.HISTORY_FAILED_CLEAR)
            emitEffect(TradeHistoryScreenEffect.ShowError("Failed to clear history"))
        }
    }

    private fun filterTradesByDate(
        trades: List<TradeHistoryUiModel>,
        dateRange: com.trading.orb.ui.state.DateRangeUiModel
    ): List<TradeHistoryUiModel> {
        // Filter trades within date range
        return trades
    }

    private fun filterTradesByTab(
        trades: List<TradeHistoryUiModel>,
        tab: String
    ): List<TradeHistoryUiModel> {
        return when (tab) {
            TRADE_FILTER_PROFIT -> trades.filter { it.profitLoss > 0 }
            TRADE_FILTER_LOSS -> trades.filter { it.profitLoss < 0 }
            else -> trades // ALL
        }
    }
}
