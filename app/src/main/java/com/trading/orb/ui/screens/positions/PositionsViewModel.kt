package com.trading.orb.ui.screens.positions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trading.orb.data.model.Position
import com.trading.orb.data.repository.TradingRepository
import com.trading.orb.ui.event.PositionsUiEvent
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.PositionFilterType
import com.trading.orb.ui.state.PositionUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for PositionsScreen
 * Manages positions-specific UI state and user interactions
 */
@HiltViewModel
class PositionsViewModel @Inject constructor(
    private val repository: TradingRepository
) : ViewModel() {

    // Positions data from repository
    val positions: StateFlow<List<Position>> = repository.positions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Positions UI State
    private val _positionsUiState = MutableStateFlow(PositionsUiState())
    val positionsUiState: StateFlow<PositionsUiState> = _positionsUiState.asStateFlow()

    // UI events
    private val _uiEvent = MutableSharedFlow<PositionsUiEvent>()
    val uiEvent: SharedFlow<PositionsUiEvent> = _uiEvent.asSharedFlow()

    init {
        loadPositions()
        observePositions()
    }

    /**
     * Initial positions load
     */
    fun loadPositions() {
        viewModelScope.launch {
            _positionsUiState.update { it.copy(loading = LoadingState(isLoading = true, "Loading positions...")) }
            try {
                // Load sample data for now
                val samplePositions = listOf(
                    PositionUiModel(
                        positionId = "1",
                        symbol = "AAPL",
                        type = "BUY",
                        quantity = 100,
                        entryPrice = 150.0,
                        currentPrice = 155.5,
                        profitLoss = 550.0,
                        profitLossPercent = 3.67,
                        stopLoss = 145.0,
                        takeProfit = 160.0,
                        openTime = "2024-01-10 09:30",
                        riskLevel = "LOW"
                    ),
                    PositionUiModel(
                        positionId = "2",
                        symbol = "GOOGL",
                        type = "BUY",
                        quantity = 50,
                        entryPrice = 140.0,
                        currentPrice = 135.0,
                        profitLoss = -250.0,
                        profitLossPercent = -3.57,
                        stopLoss = 135.0,
                        takeProfit = 150.0,
                        openTime = "2024-01-10 10:15",
                        riskLevel = "MEDIUM"
                    ),
                    PositionUiModel(
                        positionId = "3",
                        symbol = "MSFT",
                        type = "SELL",
                        quantity = 75,
                        entryPrice = 380.0,
                        currentPrice = 375.0,
                        profitLoss = 375.0,
                        profitLossPercent = 1.32,
                        stopLoss = 385.0,
                        takeProfit = 370.0,
                        openTime = "2024-01-10 11:00",
                        riskLevel = "LOW"
                    )
                )
                _positionsUiState.update {
                    it.copy(
                        positions = samplePositions,
                        totalOpenPositions = samplePositions.size,
                        totalProfit = samplePositions.filter { it.profitLoss > 0 }.sumOf { it.profitLoss },
                        totalLoss = samplePositions.filter { it.profitLoss < 0 }.sumOf { it.profitLoss },
                        loading = LoadingState(isLoading = false),
                        error = ErrorState()
                    )
                }
            } catch (e: Exception) {
                _positionsUiState.update {
                    it.copy(
                        loading = LoadingState(isLoading = false),
                        error = ErrorState(
                            hasError = true,
                            errorMessage = e.message ?: "Failed to load positions",
                            isRetryable = true,
                            throwable = e
                        )
                    )
                }
            }
        }
    }

    /**
     * Observe repository positions changes and update UI state
     */
    private fun observePositions() {
        viewModelScope.launch {
            positions.collect { positionList ->
                val filteredPositions = filterPositions(positionList, _positionsUiState.value.filterType)
                _positionsUiState.update {
                    it.copy(
                        positions = mapPositionsToUiModels(filteredPositions),
                        totalOpenPositions = filteredPositions.size,
                        totalProfit = calculateTotalProfit(filteredPositions),
                        totalLoss = calculateTotalLoss(filteredPositions),
                        loading = LoadingState(isLoading = false)
                    )
                }
            }
        }
    }

    /**
     * Retry loading positions
     */
    fun retryPositions() {
        loadPositions()
    }

    /**
     * Refresh positions data
     */
    fun refreshPositions() {
        viewModelScope.launch {
            _positionsUiState.update { it.copy(isRefreshing = true) }
            try {
                // Trigger repository refresh
                kotlinx.coroutines.delay(1000)
                _positionsUiState.update { it.copy(isRefreshing = false) }
            } catch (e: Exception) {
                _positionsUiState.update {
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
     * Filter positions by type
     */
    fun filterByType(filterType: PositionFilterType) {
        viewModelScope.launch {
            _positionsUiState.update {
                it.copy(
                    filterType = filterType,
                    positions = mapPositionsToUiModels(
                        filterPositions(positions.value, filterType)
                    )
                )
            }
        }
    }

    /**
     * Close a specific position
     */
    fun closePosition(positionId: String) {
        viewModelScope.launch {
            repository.closePosition(positionId).onFailure { error ->
                _uiEvent.emit(PositionsUiEvent.ShowError(error.message ?: "Failed to close position"))
            }.onSuccess {
                _uiEvent.emit(PositionsUiEvent.ShowSuccess("Position closed successfully"))
            }
        }
    }

    /**
     * Close all open positions
     */
    fun closeAllPositions() {
        viewModelScope.launch {
            repository.closeAllPositions().onFailure { error ->
                _uiEvent.emit(PositionsUiEvent.ShowError(error.message ?: "Failed to close all positions"))
            }.onSuccess {
                _uiEvent.emit(PositionsUiEvent.ShowSuccess("All positions closed"))
            }
        }
    }

    /**
     * Update position details
     */
    fun updatePositionDetails(position: PositionUiModel) {
        _positionsUiState.update {
            it.copy(selectedPosition = position)
        }
    }

    // =========================================================================
    // Helper functions
    // =========================================================================

    private fun filterPositions(
        positions: List<Position>,
        filterType: PositionFilterType
    ): List<Position> {
        return when (filterType) {
            PositionFilterType.LONG -> positions.filter { it.side.name == "BUY" }
            PositionFilterType.SHORT -> positions.filter { it.side.name == "SELL" }
            PositionFilterType.PROFIT -> positions.filter { it.pnl > 0 }
            PositionFilterType.LOSS -> positions.filter { it.pnl < 0 }
            PositionFilterType.BREAKEVEN -> positions.filter { it.pnl == 0.0 }
            PositionFilterType.ALL -> positions
        }
    }

    private fun mapPositionsToUiModels(positions: List<Position>): List<PositionUiModel> {
        return positions.map { position ->
            PositionUiModel(
                positionId = position.id,
                symbol = position.instrument.symbol,
                type = position.side.name,
                quantity = position.quantity,
                entryPrice = position.entryPrice,
                currentPrice = position.currentPrice,
                profitLoss = position.pnl,
                profitLossPercent = position.pnlPercentage,
                stopLoss = position.stopLoss,
                takeProfit = position.target,
                openTime = position.entryTime.toString(),
                riskLevel = if (position.isProfit) "LOW" else "MEDIUM"
            )
        }
    }

    private fun calculateTotalProfit(positions: List<Position>): Double {
        return positions.filter { it.pnl > 0 }.sumOf { it.pnl }
    }

    private fun calculateTotalLoss(positions: List<Position>): Double {
        return positions.filter { it.pnl < 0 }.sumOf { it.pnl }
    }
}
