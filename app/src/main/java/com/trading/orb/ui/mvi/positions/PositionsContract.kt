package com.trading.orb.ui.mvi.positions

import com.trading.orb.ui.mvi.MviState
import com.trading.orb.ui.mvi.MviIntent
import com.trading.orb.ui.mvi.MviEffect
import com.trading.orb.ui.state.LoadingState
import com.trading.orb.ui.state.ErrorState
import com.trading.orb.ui.state.PositionFilterType
import com.trading.orb.data.model.Position

/**
 * Positions Screen State
 */
data class PositionsScreenState(
    val loading: LoadingState = LoadingState(),
    val error: ErrorState = ErrorState(),
    val isRefreshing: Boolean = false,
    
    // Data
    val positions: List<Position> = emptyList(),
    val selectedPosition: Position? = null,
    
    // Filters
    val filterType: PositionFilterType = PositionFilterType.ALL,
    val isExpandedView: Boolean = false,
    
    // Dialog state
    val isClosePositionDialogVisible: Boolean = false
) : MviState

sealed class PositionsScreenIntent : MviIntent {
    // Lifecycle
    object LoadPositions : PositionsScreenIntent()
    object RetryLoadPositions : PositionsScreenIntent()
    object RefreshPositions : PositionsScreenIntent()

    // Selection
    data class SelectPosition(val position: Position) : PositionsScreenIntent()
    object DeselectPosition : PositionsScreenIntent()

    // Filtering
    data class FilterPositions(val filterType: PositionFilterType) : PositionsScreenIntent()
    
    // View mode
    object ToggleExpandedView : PositionsScreenIntent()

    // Actions
    data class ClosePosition(val positionId: String) : PositionsScreenIntent()
    object CloseAllPositions : PositionsScreenIntent()

    // Dialog management
    object ShowClosePositionDialog : PositionsScreenIntent()
    object DismissClosePositionDialog : PositionsScreenIntent()

    // Data updates
    data class UpdatePositions(val positions: List<Position>) : PositionsScreenIntent()

    // Error handling
    data class HandleError(val error: Throwable) : PositionsScreenIntent()
}

sealed class PositionsScreenEffect : MviEffect {
    // Feedback
    data class ShowToast(val message: String) : PositionsScreenEffect()
    data class ShowError(val message: String) : PositionsScreenEffect()
    data class ShowSuccess(val message: String) : PositionsScreenEffect()

    // Navigation
    object NavigateBack : PositionsScreenEffect()

    // Dialogs
    data class ShowConfirmCloseDialog(
        val positionId: String,
        val symbol: String
    ) : PositionsScreenEffect()

    // Actions
    data class PositionClosed(val positionId: String, val pnl: Double) : PositionsScreenEffect()

    // Logging
    data class LogEvent(val eventName: String) : PositionsScreenEffect()
}
