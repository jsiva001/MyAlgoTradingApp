package com.trading.orb.ui.screens.positions

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.PositionFilterType
import com.trading.orb.ui.state.PositionUiModel

/**
 * Preview Provider for mocking test data
 * Centralized location for all preview data used in PositionsScreen
 */
object PositionsPreviewProvider {

    fun samplePositionUiModel(
        positionId: String = "POS001",
        symbol: String = "NIFTY50",
        type: String = "LONG",
        quantity: Int = 1,
        entryPrice: Double = 20500.0,
        currentPrice: Double = 20650.0,
        profitLoss: Double = 150.0,
        profitLossPercent: Double = 0.73
    ): PositionUiModel {
        return PositionUiModel(
            positionId = positionId,
            symbol = symbol,
            type = type,
            quantity = quantity,
            entryPrice = entryPrice,
            currentPrice = currentPrice,
            profitLoss = profitLoss,
            profitLossPercent = profitLossPercent,
            stopLoss = 20350.0,
            takeProfit = 20850.0,
            openTime = "2024-12-11T09:30:00",
            riskLevel = "MEDIUM"
        )
    }

    fun samplePositionsList(): List<PositionUiModel> {
        return listOf(
            samplePositionUiModel(
                positionId = "POS001",
                symbol = "NIFTY50",
                profitLoss = 150.0
            ),
            samplePositionUiModel(
                positionId = "POS002",
                symbol = "FINNIFTY",
                type = "SHORT",
                entryPrice = 21500.0,
                currentPrice = 21350.0,
                profitLoss = 150.0,
                profitLossPercent = 0.70
            ),
            samplePositionUiModel(
                positionId = "POS003",
                symbol = "BANKNIFTY",
                profitLoss = -200.0,
                profitLossPercent = -1.25
            )
        )
    }

    fun samplePositionsUiState(
        isLoading: Boolean = false,
        hasError: Boolean = false,
        errorMessage: String = "Failed to load positions",
        filterType: PositionFilterType = PositionFilterType.ALL
    ): PositionsUiState {
        val positions = samplePositionsList()
        return PositionsUiState(
            positions = positions,
            totalOpenPositions = positions.size,
            totalProfit = 300.0,
            totalLoss = -200.0,
            loading = LoadingState(isLoading = isLoading, loadingMessage = "Loading positions..."),
            error = ErrorState(hasError = hasError, errorMessage = errorMessage, isRetryable = true),
            isRefreshing = false,
            filterType = filterType
        )
    }

    fun samplePositionsUiStateLoading(): PositionsUiState {
        return samplePositionsUiState(isLoading = true)
    }

    fun samplePositionsUiStateError(): PositionsUiState {
        return samplePositionsUiState(hasError = true)
    }

    fun samplePositionsUiStateEmpty(): PositionsUiState {
        return PositionsUiState(
            positions = emptyList(),
            totalOpenPositions = 0,
            loading = LoadingState(isLoading = false)
        )
    }
}
