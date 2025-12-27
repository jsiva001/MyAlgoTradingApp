package com.trading.orb.ui.mvi.positions

import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.ExitReason
import com.trading.orb.data.model.Trade
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.mvi.HybridMviViewModel
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.PositionFilterType
import com.trading.orb.ui.utils.TimberLogs
import com.trading.orb.ui.utils.ErrorMessages
import com.trading.orb.ui.utils.POSITION_FILTER_LONG
import com.trading.orb.ui.utils.POSITION_FILTER_SHORT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Positions Screen ViewModel with MVI Architecture
 *
 * Manages active trading positions and their display
 */
@HiltViewModel
class PositionsViewModel @Inject constructor(
    private val repository: TradingRepository
) : HybridMviViewModel<PositionsScreenState, PositionsScreenIntent, PositionsScreenEffect>() {

    override fun createInitialState(): PositionsScreenState = PositionsScreenState()

    /**
     * Pure reducer function
     */
    override fun reduce(currentState: PositionsScreenState, intent: PositionsScreenIntent): PositionsScreenState {
        return when (intent) {
            is PositionsScreenIntent.LoadPositions -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is PositionsScreenIntent.RetryLoadPositions -> {
                currentState.copy(
                    loading = LoadingState(isLoading = true),
                    error = ErrorState()
                )
            }

            is PositionsScreenIntent.RefreshPositions -> {
                currentState.copy(isRefreshing = true)
            }

            is PositionsScreenIntent.SelectPosition -> {
                currentState.copy(selectedPosition = intent.position)
            }

            is PositionsScreenIntent.DeselectPosition -> {
                currentState.copy(selectedPosition = null)
            }

            is PositionsScreenIntent.FilterPositions -> {
                currentState.copy(filterType = intent.filterType)
            }

            is PositionsScreenIntent.ToggleExpandedView -> {
                currentState.copy(isExpandedView = !currentState.isExpandedView)
            }

            is PositionsScreenIntent.ShowClosePositionDialog -> {
                currentState.copy(isClosePositionDialogVisible = true)
            }

            is PositionsScreenIntent.DismissClosePositionDialog -> {
                currentState.copy(isClosePositionDialogVisible = false)
            }

            is PositionsScreenIntent.UpdatePositions -> {
                val filteredPositions = filterPositions(intent.positions, currentState.filterType)
                currentState.copy(
                    positions = filteredPositions,
                    loading = LoadingState(isLoading = false),
                    error = ErrorState()
                )
            }

            is PositionsScreenIntent.HandleError -> {
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

            // Action-only intents (no state change)
            is PositionsScreenIntent.ClosePosition,
            is PositionsScreenIntent.CloseAllPositions -> currentState
        }
    }

    /**
     * Handle intents with side effects
     */
    override suspend fun handleIntent(intent: PositionsScreenIntent) {
        when (intent) {
            is PositionsScreenIntent.LoadPositions -> loadPositionsData()
            is PositionsScreenIntent.RetryLoadPositions -> loadPositionsData()
            is PositionsScreenIntent.RefreshPositions -> refreshPositionsData()

            is PositionsScreenIntent.SelectPosition -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is PositionsScreenIntent.DeselectPosition -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is PositionsScreenIntent.FilterPositions -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is PositionsScreenIntent.ToggleExpandedView -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is PositionsScreenIntent.ClosePosition -> closePositionWithSideEffects(intent.positionId)
            is PositionsScreenIntent.CloseAllPositions -> closeAllPositionsWithSideEffects()

            is PositionsScreenIntent.ShowClosePositionDialog -> {
                updateStateImmediate(reduce(state.value, intent))
                if (state.value.selectedPosition != null) {
                    emitEffect(
                        PositionsScreenEffect.ShowConfirmCloseDialog(
                            state.value.selectedPosition!!.id,
                            state.value.selectedPosition!!.instrument.symbol
                        )
                    )
                }
            }

            is PositionsScreenIntent.DismissClosePositionDialog -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is PositionsScreenIntent.UpdatePositions -> {
                updateStateImmediate(reduce(state.value, intent))
            }

            is PositionsScreenIntent.HandleError -> {
                updateStateImmediate(reduce(state.value, intent))
                emitEffect(PositionsScreenEffect.ShowError(intent.error.message ?: ErrorMessages.AN_ERROR_OCCURRED))
            }
        }
    }

    // ==================== Side Effects Implementation ====================

    private suspend fun loadPositionsData() {
        try {
            updateStateImmediate(
                state.value.copy(loading = LoadingState(isLoading = true))
            )

            // In real implementation, fetch from repository
            val positions = emptyList<com.trading.orb.data.model.Position>()

            processIntent(PositionsScreenIntent.UpdatePositions(positions))
            emitEffect(PositionsScreenEffect.LogEvent("positions_loaded"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.POSITIONS_FAILED_LOAD)
            processIntent(PositionsScreenIntent.HandleError(e))
        }
    }

    private suspend fun refreshPositionsData() {
        try {
            kotlinx.coroutines.delay(500)
            updateStateImmediate(state.value.copy(isRefreshing = false))
            emitEffect(PositionsScreenEffect.ShowSuccess("Positions refreshed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.POSITIONS_FAILED_REFRESH)
            emitEffect(PositionsScreenEffect.ShowError(e.message ?: ErrorMessages.REFRESH_FAILED))
        }
    }

    private suspend fun closePositionWithSideEffects(positionId: String) {
        try {
            val position = state.value.positions.find { it.id == positionId }
            if (position == null) {
                emitEffect(PositionsScreenEffect.ShowError(ErrorMessages.POSITION_NOT_FOUND))
                return
            }

            // Create trade from position
            val trade = Trade(
                id = position.id,
                instrument = position.instrument,
                side = position.side,
                quantity = position.quantity,
                entryPrice = position.entryPrice,
                exitPrice = position.currentPrice,
                entryTime = position.entryTime,
                exitTime = LocalDateTime.now(),
                exitReason = ExitReason.MANUAL_EXIT,
                pnl = position.pnl
            )

            // Update local state
            val updatedPositions = state.value.positions.filter { it.id != positionId }
            processIntent(PositionsScreenIntent.UpdatePositions(updatedPositions))

            emitEffect(PositionsScreenEffect.PositionClosed(trade.id, trade.pnl))
            emitEffect(
                PositionsScreenEffect.ShowSuccess(
                    "Position closed at ₹${String.format("%.2f", trade.exitPrice)}"
                )
            )
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.POSITIONS_FAILED_CLOSE)
            emitEffect(PositionsScreenEffect.ShowError(ErrorMessages.FAILED_CLOSE_ALL_POSITIONS))
        }
    }

    private suspend fun closeAllPositionsWithSideEffects() {
        try {
            val positionsToClose = state.value.positions.toList()
            if (positionsToClose.isEmpty()) {
                emitEffect(PositionsScreenEffect.ShowSuccess("No positions to close"))
                return
            }

            positionsToClose.forEach { position ->
                closePositionWithSideEffects(position.id)
            }

            emitEffect(PositionsScreenEffect.ShowSuccess("All positions closed"))
        } catch (e: Exception) {
            Timber.e(e, TimberLogs.POSITIONS_FAILED_CLOSE_ALL)
            emitEffect(PositionsScreenEffect.ShowError(ErrorMessages.FAILED_CLOSE_ALL_POSITIONS))
        }
    }

    private fun filterPositions(
        positions: List<com.trading.orb.data.model.Position>,
        filterType: PositionFilterType
    ): List<com.trading.orb.data.model.Position> {
        return when (filterType) {
            PositionFilterType.ALL -> positions
            PositionFilterType.LONG -> positions.filter { it.side.name == POSITION_FILTER_LONG }
            PositionFilterType.SHORT -> positions.filter { it.side.name == POSITION_FILTER_SHORT }
            PositionFilterType.PROFIT -> positions.filter { it.pnl > 0 }
            PositionFilterType.LOSS -> positions.filter { it.pnl < 0 }
            PositionFilterType.BREAKEVEN -> positions.filter { it.pnl == 0.0 }
        }
    }
}
