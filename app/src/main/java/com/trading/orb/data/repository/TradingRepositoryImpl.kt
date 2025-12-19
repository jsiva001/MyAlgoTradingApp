package com.trading.orb.data.repository

import com.trading.orb.data.model.*
import com.trading.orb.ui.state.HistoryFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TradingRepositoryImpl @Inject constructor() : TradingRepository {

    private val _appState = MutableStateFlow(
        AppState(
            tradingMode = TradingMode.PAPER,
            strategyStatus = StrategyStatus.INACTIVE,
            connectionStatus = ConnectionStatus.DISCONNECTED
        )
    )
    override val appState: Flow<AppState> = _appState

    private val _positions = MutableStateFlow<List<Position>>(emptyList())
    override val positions: Flow<List<Position>> = _positions

    private val _trades = MutableStateFlow<List<Trade>>(emptyList())
    override val trades: Flow<List<Trade>> = _trades

    private val _strategyConfig = MutableStateFlow(
        StrategyConfig(
            instrument = Instrument(
                symbol = "NIFTY24DEC22000CE",
                exchange = "NSE",
                lotSize = 50,
                tickSize = 0.05,
                displayName = "NIFTY 22000 CE"
            ),
            // FOR MOCK TESTING: Set ORB window to be 15 minutes from START button click
            // In real implementation with Angel One API, this will be 9:15-9:30 AM
            orbStartTime = java.time.LocalTime.of(0, 0), // Start immediately when strategy starts
            orbEndTime = java.time.LocalTime.of(23, 59), // Keep it open all day for testing
            autoExitTime = java.time.LocalTime.of(23, 50)
        )
    )
    override val strategyConfig: Flow<StrategyConfig> = _strategyConfig

    private val _riskSettings = MutableStateFlow(RiskSettings())
    override val riskSettings: Flow<RiskSettings> = _riskSettings

    override suspend fun startStrategy(): Result<Unit> {
        return try {
            _appState.value = _appState.value.copy(
                strategyStatus = StrategyStatus.ACTIVE,
                connectionStatus = ConnectionStatus.CONNECTED,
                strategyConfig = _strategyConfig.value
            )
            Timber.d("Strategy started successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to start strategy")
            Result.failure(e)
        }
    }

    override suspend fun stopStrategy(): Result<Unit> {
        return try {
            _appState.value = _appState.value.copy(
                strategyStatus = StrategyStatus.INACTIVE,
                strategyConfig = null,
                orbLevels = null
            )
            Timber.d("Strategy stopped successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop strategy")
            Result.failure(e)
        }
    }

    override suspend fun toggleTradingMode(): Result<Unit> {
        return try {
            val newMode = if (_appState.value.tradingMode == TradingMode.PAPER) {
                TradingMode.LIVE
            } else {
                TradingMode.PAPER
            }
            _appState.value = _appState.value.copy(tradingMode = newMode)
            Timber.d("Trading mode toggled to: $newMode")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle trading mode")
            Result.failure(e)
        }
    }

    override suspend fun pauseStrategy(): Result<Unit> {
        return try {
            _appState.value = _appState.value.copy(
                strategyStatus = StrategyStatus.PAUSED
            )
            Timber.d("Strategy paused")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to pause strategy")
            Result.failure(e)
        }
    }

    override suspend fun closeAllPositions(): Result<Unit> {
        return try {
            _positions.value = emptyList()
            Timber.d("All positions closed")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to close all positions")
            Result.failure(e)
        }
    }

    override suspend fun updateStrategyConfig(config: StrategyConfig): Result<Unit> {
        return try {
            _strategyConfig.value = config
            Timber.d("Strategy configuration updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update strategy configuration")
            Result.failure(e)
        }
    }

    override suspend fun updateRiskSettings(settings: RiskSettings): Result<Unit> {
        return try {
            _riskSettings.value = settings
            Timber.d("Risk settings updated")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update risk settings")
            Result.failure(e)
        }
    }

    override suspend fun updateAppState(state: AppState): Result<Unit> {
        return try {
            _appState.value = state
            // Also update _positions to keep it in sync with AppState
            _positions.value = state.activePositions
            // Also update _trades with closed trades for history screen
            _trades.value = state.closedTrades
            Timber.d("App state updated with ${state.activePositions.size} active positions and ${state.closedTrades.size} closed trades")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update app state")
            Result.failure(e)
        }
    }

    override suspend fun fetchTradesHistory(filter: HistoryFilter): Result<Unit> {
        return try {
            // Placeholder for actual API call
            Timber.d("Trade history fetched with filter: $filter")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch trade history")
            Result.failure(e)
        }
    }
}

