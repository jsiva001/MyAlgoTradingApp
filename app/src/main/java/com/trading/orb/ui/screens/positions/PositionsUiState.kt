package com.trading.orb.ui.screens.positions

import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.PositionFilterType
import com.trading.orb.ui.state.PositionUiModel

/**
 * UI State specific to Positions Screen
 * Manages all UI-related data for positions display
 */
data class PositionsUiState(
    val positions: List<PositionUiModel> = emptyList(),
    val totalOpenPositions: Int = 0,
    val totalProfit: Double = 0.0,
    val totalLoss: Double = 0.0,
    val selectedPosition: PositionUiModel? = null,
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    val filterType: PositionFilterType = PositionFilterType.ALL
)
